package rpc.srsd.consul;

import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.srsd.ServiceDiscovery;
import rpc.srsd.ServiceRegistrationInfo;
import rpc.util.MathUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 14:03
 */
public class ConsulServiceDiscovery implements ServiceDiscovery {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceDiscovery.class);
    private static ConsulServiceDiscovery instance;

    private ConsulServiceDiscovery() {}

    public static ConsulServiceDiscovery getInstance() {
        synchronized (ConsulServiceDiscovery.class) {
            if (instance == null) {
                instance = new ConsulServiceDiscovery();
            }
        }
        return instance;
    }

    @Override
    public List<ServiceRegistrationInfo> getAvailableServices(String name) {
        HealthClient healthClient = ConsulObject.consul().healthClient();

        List<ServiceHealth> serviceHealths = healthClient.getAllServiceInstances(name).getResponse();
        LOG.info("All healthy service for name `{}`: {}", name, serviceHealths);

        // get all health services from consul server
        return serviceHealths.stream()
            .map(sh -> {
                Service service = sh.getService();
                ServiceRegistrationInfo info = new ServiceRegistrationInfo();
                info.setName(service.getService());
                info.setAddress(service.getAddress());
                info.setPort(service.getPort());
                info.setId(service.getId());
                return info;
            })
            .collect(Collectors.toList());
    }

    @Override
    public ServiceRegistrationInfo getRandomAvailableService(String name) {
        List<ServiceRegistrationInfo> regList =  this.getAvailableServices(name);
        return regList.get(MathUtil.randomIntInRange(0, regList.size()));
    }


    public static void main(String[] args) {
        List<ServiceRegistrationInfo> lst = ConsulServiceDiscovery.getInstance().getAvailableServices("test");
        lst.forEach(info ->
            LOG.info("{}", info)
        );
    }
}
