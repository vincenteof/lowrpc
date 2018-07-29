package example.client;

import rpc.client.RpcClientProxyFactory;
import rpc.srsd.consul.ConsulServiceDiscovery;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 10:27
 */
public class RpcClientMain {
    public static void main(String[] args) {
        TestServiceClient client = RpcClientProxyFactory.createProxy(
            TestServiceClient.class,
            ConsulServiceDiscovery.getInstance()
        );
        System.out.println(client.testPureWithoutParams());
        System.out.println(client.testStateWithoutParams());
        System.out.println(client.testStateWithParams(2));
    }
}
