package rpc.inject.testservice;

import rpc.inject.LowBean;
import rpc.server.LowRpcService;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/31, 16:29
 */
@LowRpcService(name = "serviceB")
public class ServiceB {
    private ComponentA componentA;

    public ServiceB(@LowBean(name = "compA") ComponentA componentA) {
        this.componentA = componentA;
    }

    public String g() {
        return componentA.f();
    }
}
