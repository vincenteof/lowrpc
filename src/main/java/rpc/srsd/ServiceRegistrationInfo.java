package rpc.srsd;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/27, 11:46
 */
public class ServiceRegistrationInfo {
    private String id;
    private String name;
    private String address;
    private int port;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "ServiceRegistrationInfo{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", address='" + address + '\'' +
            ", port=" + port +
            '}';
    }
}
