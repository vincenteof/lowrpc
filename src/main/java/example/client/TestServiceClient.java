package example.client;

import rpc.client.LowRpcClient;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 10:24
 */
@LowRpcClient(serviceName = "testService")
public interface TestServiceClient {
    String testPureWithoutParams();

    Integer testStateWithoutParams();

    Boolean testStateWithParams(Integer num);
}
