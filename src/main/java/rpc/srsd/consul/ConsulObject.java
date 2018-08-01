package rpc.srsd.consul;


import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import org.apache.commons.configuration2.Configuration;
import rpc.util.ConfigurationUtil;

import java.util.Objects;

import static rpc.util.Constant.*;


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
                String address;
                String port;
                Configuration clientConf = ConfigurationUtil.getPropConfig(RPC_CLIENT_CONFIG);
                if (clientConf != null) {
                    address = clientConf.getString(SERVICE_DISCOVERY_ADDRESS);
                    port = clientConf.getString(SERVICE_DISCOVERY_PORT);
                } else {
                    Configuration serverConf = ConfigurationUtil.getPropConfig(RPC_CLIENT_CONFIG);
                    address = serverConf.getString(SERVICE_REGISTRY_ADDRESS);
                    port = serverConf.getString(SERVICE_REGISTRY_PORT);
                }

                Objects.requireNonNull(address);
                Objects.requireNonNull(port);

                consul = Consul.builder().withHostAndPort(HostAndPort.fromString(address + ":" + port)).build();
            }
            return consul;
        }
    }
}
