package rpc.client;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/26, 14:41
 */
@LowRpcClient(host = "127.0.0.1", port = 8322, clzName = "rpc.service.TestService")
public interface TestServiceClient {
    String testPureWithoutParams();

    Integer testStateWithoutParams();

    Boolean testStateWithParams(Integer num);
}
