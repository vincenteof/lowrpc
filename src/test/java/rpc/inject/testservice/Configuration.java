package rpc.inject.testservice;

import rpc.inject.LowBean;
import rpc.inject.LowRpcInjectConf;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/31, 16:30
 */

@LowRpcInjectConf
public class Configuration {
    @LowBean(name = "compA")
    public ComponentA componentA() {
        return new ComponentA();
    }
}
