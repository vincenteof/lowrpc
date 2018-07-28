package rpc.srsd.consul;


import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import org.apache.commons.configuration2.Configuration;
import rpc.util.ConfigurationUtil;
import rpc.util.Constant;


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
                Configuration config = ConfigurationUtil.getPropConfig(Constant.RPC_CLIENT_CONFIG);
                String address = config.getString(Constant.SERVICE_DISCOVERY_ADDRESS);
                String port = config.getString(Constant.SERVICE_DISCOVERY_PORT);
                consul = Consul.builder().withHostAndPort(HostAndPort.fromString(address + ":" + port)).build();
            }
            return consul;
        }
    }
}
