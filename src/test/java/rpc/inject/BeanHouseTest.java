package rpc.inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/31, 16:13
 */
public class BeanHouseTest {
    private BeanHouse beanHouse;

    @BeforeEach
    void init() {
        beanHouse = BeanHouse.create("rpc.inject.testservice");
    }

    @Test
    void getServiceBeansTest() {
        Map<String, Object> beans = beanHouse.getServiceBeans();
        Assertions.assertEquals(2, beans.size());
    }
}
