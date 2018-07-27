package example.server;

import rpc.server.RpcServer;
import rpc.server.SimpleRpcServerBuilder;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 10:23
 */
public class RpcServerMain {
    public static void main(String[] args) throws Exception {
        RpcServer server = SimpleRpcServerBuilder.builder()
            .port(8322)
            .beansPackName("example.server.service")
            .build();

        server.start();
    }
}
