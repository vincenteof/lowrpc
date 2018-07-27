package rpc.srsd.consul;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 14:10
 */
public class ConsulObject {
    private static Consul consul;

    private ConsulObject() {}

    public static Consul consul() {
        synchronized (ConsulObject.class) {
            if (consul == null) {
                consul = Consul.builder().withHostAndPort(HostAndPort.fromString("127.0.0.1:8500")).build();
            }
            return consul;
        }
    }
}
