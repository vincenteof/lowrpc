package example.server;

import rpc.server.RpcServer;
import rpc.server.SimpleRpcServer;
import rpc.srsd.ServiceRegistry;
import rpc.srsd.consul.ConsulServiceRegistry;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 10:23
 */
public class RpcServerMain {
    public static void main(String[] args) throws Exception {
        String beanPackName = "example.server.service";
        ServiceRegistry registry = ConsulServiceRegistry.getInstance();

        RpcServer server = SimpleRpcServer.builder(beanPackName, registry)
            .port(8322)
            .build();

        server.start();
    }
}
