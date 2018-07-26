package rpc.server;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 15:17
 */
public class RpcServerTest {
    public static void main(String[] args) throws Exception {
        RpcServer server = SimpleRpcServerBuilder.builder()
            .port(8322)
            .beansPackName("rpc.service")
            .build();

        server.start();
    }
}
