package rpc.srsd;

import java.util.List;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 11:30
 */
public interface ServiceDiscovery {
    List<ServiceRegistrationInfo> getAvailableServices(String name);
}
