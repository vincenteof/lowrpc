package example.server.service;

import rpc.server.LowRpcService;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 10:22
 */
@LowRpcService(name = "testService")
public class TestService {
    private AtomicInteger counter = new AtomicInteger(0);

    public String testPureWithoutParams() {
        return "Hello";
    }

    public Integer testStateWithoutParams() {
        return counter.getAndIncrement();
    }

    public Boolean testStateWithParams(Integer num) {
        return num == counter.get();
    }
}
