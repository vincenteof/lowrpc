package example.server;

import rpc.server.RpcServer;
import rpc.server.SimpleRpcServerBuilder;
import rpc.srsd.consul.ConsulServiceRegistry;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 10:23
 */
public class RpcServerMain {
    public static void main(String[] args) throws Exception {
        RpcServer server = SimpleRpcServerBuilder.builder(ConsulServiceRegistry.getInstance())
            .port(8322)
            .beansPackName("example.server.service")
            .build();

        server.start();
    }
}
