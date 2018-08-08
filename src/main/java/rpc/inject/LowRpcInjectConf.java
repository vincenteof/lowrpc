package rpc.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * class $classname
 *
 * Annotation for a central place to define all beans.
 *
 * @author Yingpeng.Chen
 * @date 2018/7/31, 10:40
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LowRpcInjectConf {
}
