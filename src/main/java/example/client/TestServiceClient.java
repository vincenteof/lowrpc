package example.client;

import rpc.client.LowRpcClient;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 10:24
 */
@LowRpcClient(host = "127.0.0.1", port = 8322, clzName = "example.server.service.TestService")
public interface TestServiceClient {
    String testPureWithoutParams();

    Integer testStateWithoutParams();

    Boolean testStateWithParams(Integer num);
}
