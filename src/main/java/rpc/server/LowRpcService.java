package rpc.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * class $classname
 *
 * Annotation for exposed service.
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 16:16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LowRpcService {
    String name();
}
