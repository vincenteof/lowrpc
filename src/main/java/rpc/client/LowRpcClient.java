package rpc.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * class $classname
 *
 * Annotation for rpc client interface definition.
 * `serviceName` stands for the service registered in Consul or Zookeeper.
 *
 * @author Yingpeng.Chen
 * @date 2018/7/26, 14:45
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LowRpcClient {
    String serviceName();
}
