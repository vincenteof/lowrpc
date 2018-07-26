package rpc.server;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/26, 13:41
 */
public abstract class RpcServer {
    private int port;

    public RpcServer(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public abstract void start() throws Exception;
}
