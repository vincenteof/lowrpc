package rpc.inject;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.server.LowRpcService;
import rpc.util.ReflectionUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * class $classname
 *
 * A container for all beans.
 *
 * @author Yingpeng.Chen
 * @date 2018/7/31, 10:40
 */
// prob: how to make it a global instance and thread-safe ???
public class BeanHouse {
    private static Logger LOG = LoggerFactory.getLogger(BeanHouse.class);

    private MutableGraph<String> dependencyGraph = GraphBuilder.directed().build();
    private Map<String, Object> beans = new HashMap<>();
    private Map<String, DependencyEntry> dependencyEntries = new HashMap<>();
    private Object confObject;
    private String rootClzPath;

    private BeanHouse() {}

    public static BeanHouse create(String packName) {
        BeanHouse result = new BeanHouse();
        result.readClzThenConstruct(packName);
        return result;
    }

    public static BeanHouse create(String packName, String rootClzPath) {
        BeanHouse result = new BeanHouse();
        result.rootClzPath = rootClzPath;
        result.readClzThenConstruct(packName);
        return result;
    }

    public Map<String, Object> getServiceBeans() {
        return beans.entrySet().stream()
            .filter(beanEntry -> {
                DependencyEntry dependencyEntry = dependencyEntries.get(beanEntry.getKey());
                return !dependencyEntry.isMethod();
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Read all class in some package and construct all instances of these classes.
     *
     * @param packName package name of the classes
     */
    private void readClzThenConstruct(String packName) {
        List<Class<?>> allClz = rootClzPath == null
            ? ReflectionUtil.getClzFromPack(packName)
            : ReflectionUtil.getClzFromPack(packName, rootClzPath);

        // construct the dependency graph
        allClz.stream()
            .filter(clz -> clz.getDeclaredAnnotation(LowRpcService.class) != null ||
                clz.getDeclaredAnnotation(LowRpcInjectConf.class) != null
            )
            .forEach(clz -> {
                if (clz.getDeclaredAnnotation(LowRpcService.class) != null) {
                    doForService(clz);
                } else {
                    doForConf(clz);
                }
            });

        // construct instances based on the dependency graph
        dependenciesAfterTopologicalSort().forEach(name -> {
            DependencyEntry entry = dependencyEntries.get(name);
            if (entry == null) {
                throw new IllegalStateException("The whole dependencies is not complete");
            }

            if (entry.isMethod()) {
                Method method = entry.getExecAsMethod();
                Annotation[][] paramAnnotations = method.getParameterAnnotations();

                Object[] actualParams = makeActualParams(paramAnnotations);

                if (actualParams.length != paramAnnotations.length) {
                    throw new IllegalStateException(
                        "You may have not annotated some needed arguments for dependency `" + name + "`"
                    );
                }

                try {
                    method.setAccessible(true);
                    Object bean = method.invoke(confObject, actualParams);
                    beans.put(name, bean);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LOG.error("Refection error in `BeanHouse`: ", e);
                }
            } else {
                Constructor constructor = entry.getExecAsConstructor();
                Annotation[][] paramAnnotations = constructor.getParameterAnnotations();

                Object[] actualParams = makeActualParams(paramAnnotations);
                if (actualParams.length != paramAnnotations.length) {
                    throw new IllegalStateException(
                        "You may have not annotated some needed arguments for dependency `" + name + "`"
                    );
                }

                try {
                    Object bean = constructor.newInstance(actualParams);
                    beans.put(name, bean);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    LOG.error("Refection error in `BeanHouse`: ", e);
                }
            }
        });

    }

    /**
     * A simple implementation of topological sort.
     * After sorting these dependencies, we can construct each one of it.
     *
     * @return A list of all services's names that conform to topological order
     */
    private List<String> dependenciesAfterTopologicalSort() {
        Queue<String> queue = new LinkedList<>();
        List<String> sortedDependencies = new LinkedList<>();

        dependencyGraph.nodes().forEach(cur -> {
            if (dependencyGraph.inDegree(cur) == 0) {
                queue.add(cur);
            }
        });

        while (!queue.isEmpty()) {
            String some = queue.poll();
            sortedDependencies.add(some);
            dependencyGraph.removeNode(some);
            dependencyGraph.nodes().forEach(cur -> {
                if (dependencyGraph.inDegree(cur) == 0) {
                    queue.add(cur);
                }
            });
        }

        if (dependencyGraph.nodes().size() != 0) {
            throw new IllegalStateException("There is a circular in the whole dependencies");
        }

        return sortedDependencies;
    }

    /**
     * The constructor has the form: C(@LowBean(name = "comp") Component component)
     * This method will find all needed beans for the constructor based on the annotation.
     *
     * @param paramAnnotations annotations on the every constructor parameter
     * @return
     */
    private Object[] makeActualParams(Annotation[][] paramAnnotations) {
        List<Object> actualParams = new LinkedList<>();

        for (int i = 0; i < paramAnnotations.length; i++) {
            for (int j = 0; j < paramAnnotations[i].length; j++) {
                if (paramAnnotations[i][j] instanceof LowBean) {
                    LowBean lowBean = (LowBean) paramAnnotations[i][j];
                    Object param = beans.get(lowBean.name());
                    if (param == null) {
                        throw new IllegalStateException("something is really wrong");
                    }
                    actualParams.add(param);
                }
            }
        }
        return actualParams.toArray();
    }

    /**
     * Construct the dependency graph for service definition.
     *
     * @param clz the class which defines the service
     */
    private void doForService(Class<?> clz) {
        Constructor<?>[] constructors = clz.getDeclaredConstructors();
        if (constructors.length == 0) {
            return;
        }
        // this is an contract -- the class only has one constructor
        Constructor<?> constructor = constructors[0];
        String beanName = clz.getDeclaredAnnotation(LowRpcService.class).name();
        dependencyEntries.put(beanName, new DependencyEntry(beanName, constructor, false));

        Class<?>[] paramTypes = constructor.getParameterTypes();
        if (paramTypes.length == 0) {
            dependencyGraph.addNode(beanName);
            return;
        }

        Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
        this.searchDependencyThenAdd(beanName, paramTypes, paramAnnotations);
    }

    /**
     * Construct the dependency graph for the configuration class which contains bean definitions.
     *
     * @param clz
     */
    private void doForConf(Class<?> clz) {
        try {
            confObject = clz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException |
                IllegalAccessException |
                InstantiationException |
                InvocationTargetException e) {
            LOG.error("Refection error in `BeanHouse`: ", e);
            throw new IllegalStateException("Configuration object cannot be constructed");
        }

        Stream.of(clz.getDeclaredMethods())
            .forEach(method -> {
                LowBean lowBean = method.getAnnotation(LowBean.class);
                if (lowBean == null) {
                    return;
                }

                String beanName = lowBean.name();
                dependencyEntries.put(beanName, new DependencyEntry(beanName, method, true));

                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == 0) {
                    dependencyGraph.addNode(beanName);
                    return;
                }

                Annotation[][] paramAnnotations = method.getParameterAnnotations();
                this.searchDependencyThenAdd(beanName, paramTypes, paramAnnotations);
            });
    }


    private void searchDependencyThenAdd(String name, Class<?>[] paramTypes, Annotation[][] paramAnnotations) {
        for (int i = 0; i < paramTypes.length; i++) {
            for (int j = 0; j < paramAnnotations[i].length; j++) {
                if (paramAnnotations[i][j] instanceof LowBean) {
                    LowBean lowBean = (LowBean) paramAnnotations[i][j];
                    dependencyGraph.putEdge(lowBean.name(), name);
                }
            }
        }
    }

    /**
     * This class represents dependency.
     */
    private class DependencyEntry {
        DependencyEntry(String name, Executable executable, Boolean method) {
            this.name = name;
            this.executable = executable;
            this.method = method;
        }

        private String name;
        private Executable executable;
        private Boolean method;

        Method getExecAsMethod() {
            return (Method) executable;
        }

        Constructor getExecAsConstructor() {
            return (Constructor) executable;
        }

        String getName() {
            return name;
        }

        Boolean isMethod() {
            return method;
        }
    }
}
