package example.client;

import rpc.client.LowFuture;
import rpc.client.NettyChannelManager;
import rpc.client.RpcClientProxyFactory;
import rpc.srsd.consul.ConsulServiceDiscovery;

import java.util.concurrent.ExecutionException;


/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 10:27
 */
public class RpcClientMain {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        TestServiceClient client = RpcClientProxyFactory.createProxy(
            TestServiceClient.class,
            ConsulServiceDiscovery.getInstance()
        );
        System.out.println(client.testPureWithoutParams());
        System.out.println(client.testStateWithoutParams());
        System.out.println(client.testStateWithParams(2));
        LowFuture<String> future = client.testAsync();
        System.out.println(future.get());
        LowFuture<String> lazyFuture = client.testCallback();
        lazyFuture.withCallback(ret -> System.out.println("The result is: " + ret));
        lazyFuture.startCompute();

        NettyChannelManager.getInstance().shutdown();
    }
}
