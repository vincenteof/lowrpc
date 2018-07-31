package rpc.inject.testservice;

import rpc.server.LowRpcService;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/31, 16:29
 */
@LowRpcService(name = "serviceA")
public class ServiceA {
    public String f() {
        return "A";
    }
}
