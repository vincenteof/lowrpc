package rpc.util;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static rpc.util.Constant.*;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/26, 9:25
 */

// refactor it to fix bug in junit test
public class ReflectionUtil {
    private static Logger LOG = LoggerFactory.getLogger(ReflectionUtil.class);

    public static String getRootClzPath(Class<?> clz) {
        String  clzPath =  clz.getResource(EMPTY).getPath();
        String symbol = TARGET_CLASSES_SYMBOL;
        int pos = clzPath.indexOf(symbol);
        if (pos < 0) {
            symbol = TARGET_TEST_CLASSES_SYMBOL;
            pos = clzPath.indexOf(symbol);
            if (pos < 0) {
                return null;
            }
        }

        return clzPath.substring(0, pos + symbol.length());
    }

    public static List<Class<?>> getClzFromPack(String packageName, String rootClzPath) {
        String pathStr = rootClzPath + packageName.replace(DOT, SLASH);
        Path path = new File(pathStr).toPath();

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Package `" + packageName + "` does not exist in classpath");
        }

        return getClzIteration(path, ImmutableList.of(), packageName);
    }

    public static List<Class<?>> getClzFromPack(String packageName) {
        String pathStr = ReflectionUtil.class.getResource(SLASH).getPath() + packageName.replace(DOT, SLASH);
        Path path = new File(pathStr).toPath();

       if (!Files.isDirectory(path)) {
           throw new IllegalArgumentException("Package `" + packageName + "` does not exist in classpath");
       }

       return getClzIteration(path, ImmutableList.of(), packageName);
    }

    private static ImmutableList<Class<?>> getClzIteration(Path cur, ImmutableList<Class<?>> acc, String packName) {
        if (Files.isRegularFile(cur)) {
            String fileName = cur.getFileName().toString();
            int index = fileName.indexOf(CLASS_SUFFIX);
            if (index < 0) {
                throw new IllegalStateException("File should be a class");
            }

            Class<?> clz;
            try {
                String clzName = packName + DOT + fileName.substring(0, index);
                clz = Class.forName(clzName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return acc;
            }

            return ImmutableList.<Class<?>>builder()
                .addAll(acc)
                .add(clz)
                .build();
        } else if (Files.isDirectory(cur)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(cur)) {
                for (Path p: stream) {
                    if (Files.isDirectory(p)) {
                        acc = getClzIteration(p, acc, packName + DOT + p.getFileName());
                    } else if (p.toString().contains(CLASS_SUFFIX)) {
                        acc = getClzIteration(p, acc, packName);
                    }
                }
            } catch (IOException e) {
                LOG.error("Error in `ReflectionUtil`: {}", e);
                throw new IllegalStateException(e);
            }

            return acc;
        } else {
            throw new IllegalStateException("Path `" + cur + "` is neither a file nor a package");
        }
    }
}
