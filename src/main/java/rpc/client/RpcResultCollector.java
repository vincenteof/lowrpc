package rpc.client;

import rpc.protocol.RpcResponse;

import java.util.concurrent.ConcurrentHashMap;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/26, 16:01
 */
public class RpcResultCollector {
    private static RpcResultCollector instance;
    private ConcurrentHashMap<Integer, RpcResponse> results = new ConcurrentHashMap<>();

    private RpcResultCollector() {}

    public static RpcResultCollector getInstance() {
        synchronized (RpcResultCollector.class) {
            if (instance == null) {
                instance = new RpcResultCollector();
            }
        }
        return instance;
    }

    public RpcResponse get(Integer requestId) {
        return results.get(requestId);
    }

    public RpcResponse put(RpcResponse response) {
        return results.put(response.getRequestId(), response);
    }

    public boolean contains(Integer requestId) {
        return results.containsKey(requestId);
    }
}
