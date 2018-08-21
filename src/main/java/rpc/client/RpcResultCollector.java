package rpc.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import rpc.protocol.RpcResponse;

import java.util.concurrent.TimeUnit;

/**
 * class $classname
 *
 * Container class for netty to pass the result out.
 *
 * @author Yingpeng.Chen
 * @date 2018/7/26, 16:01
 */
public class RpcResultCollector {
    private static RpcResultCollector instance;
    private Cache<Integer, RpcResponse> results = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(60, TimeUnit.SECONDS)
        .build();

    private RpcResultCollector() {}

    public static RpcResultCollector getInstance() {
        synchronized (RpcResultCollector.class) {
            if (instance == null) {
                instance = new RpcResultCollector();
            }
        }
        return instance;
    }

    public RpcResponse getIfPresent(Integer requestId) {
        return results.getIfPresent(requestId);
    }

    public void put(Integer requestId, RpcResponse response) {
        if (requestId == null) {
            throw new IllegalArgumentException("RequestId is `null`");
        }
        results.put(requestId, response);
    }
}
