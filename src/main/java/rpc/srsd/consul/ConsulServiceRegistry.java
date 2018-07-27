package rpc.srsd.consul;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.model.agent.ImmutableRegCheck;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import rpc.srsd.ServiceRegistrationInfo;
import rpc.srsd.ServiceRegistry;

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

        ImmutableRegCheck check =  ImmutableRegCheck.builder()
            .tcp("127.0.0.1:22")
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
