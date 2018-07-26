package rpc.client;


/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 15:39
 */
public class RpcClientTest {
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8322"));

    public static void main(String[] args) {
        TestServiceClient client = RpcClientProxyFactory.createProxy(TestServiceClient.class);
        System.out.println(client.testPureWithoutParams());
        System.out.println(client.testStateWithoutParams());
        System.out.println(client.testStateWithoutParams());
        System.out.println(client.testStateWithParams(2));
    }
}
