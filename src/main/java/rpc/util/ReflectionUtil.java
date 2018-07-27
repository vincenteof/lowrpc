package rpc.util;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/26, 9:25
 */

public class ReflectionUtil {
    private static final String DOT = ".";
    private static final String SLASH = "/";
    private static final String EMPTY = "";
    private static final String CLASS_SUFFIX = ".class";

    public static List<Class<?>> getClzFromPack(String packageName) {
        String uriStr = ClassLoader.getSystemResource(EMPTY).toString() + packageName.replace(DOT, SLASH);
        Path path = Paths.get(URI.create(uriStr));

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
                e.printStackTrace();
            }

            return acc;
        } else {
            throw new IllegalStateException("Path `" + cur + "` is neither a file nor a package");
        }
    }
}
