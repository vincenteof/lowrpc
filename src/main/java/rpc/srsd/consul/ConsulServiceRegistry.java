package rpc.srsd.consul;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.model.agent.ImmutableRegCheck;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import org.apache.commons.configuration2.Configuration;
import rpc.srsd.ServiceRegistrationInfo;
import rpc.srsd.ServiceRegistry;
import rpc.util.ConfigurationUtil;
import rpc.util.Constant;
import rpc.util.NetworkUtil;

import java.util.Objects;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 11:54
 */
public class ConsulServiceRegistry implements ServiceRegistry {
    private static ConsulServiceRegistry instance;

    private ConsulServiceRegistry() {}

    public static ConsulServiceRegistry getInstance() {
        synchronized (ServiceRegistry.class) {
            if (instance == null) {
                instance = new ConsulServiceRegistry();
            }
        }
        return instance;
    }

    @Override
    public void register(ServiceRegistrationInfo regInfo) {
        AgentClient agentClient = ConsulObject.consul().agentClient();

        Configuration conf = ConfigurationUtil.getPropConfig(Constant.RPC_SERVER_CONFIG);
        String address = NetworkUtil.getLocalIp();
        Objects.requireNonNull(address);
        String addressWithPort = String.format("%s:22", address);

        ImmutableRegCheck check =  ImmutableRegCheck.builder()
            .tcp(addressWithPort)
            .interval("10s")
            .timeout("15s")
            .build();

        ImmutableRegistration registration = ImmutableRegistration.builder()
            .id(regInfo.getId() == null ? regInfo.getName() : regInfo.getId())
            .name(regInfo.getName())
            .address(regInfo.getAddress())
            .port(regInfo.getPort())
            .addChecks(check)
            .build();

        agentClient.register(registration);
    }

    public static void main(String[] args) {
        ServiceRegistrationInfo regInfo =  new ServiceRegistrationInfo();
        regInfo.setName("test");
        regInfo.setPort(8999);
        regInfo.setAddress("127.0.0.1");
        regInfo.setId("test1");
        ConsulServiceRegistry.getInstance().register(regInfo);
    }
}
