package rpc.server;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 15:20
 */
public class TestService {
    private AtomicInteger counter = new AtomicInteger(0);

    public String testPureWithoutParams() {
        return "Test Ok";
    }

    public Integer testStateWithoutParams() {
        return counter.getAndIncrement();
    }

    public Boolean testStateWithParams(Integer num) {
        return num == counter.get();
    }
}
