package rpc.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * class $classname
 *
 * Annotation for labeling beans.
 *
 * @author Yingpeng.Chen
 * @date 2018/7/31, 11:23
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface LowBean {
    String name();
}
