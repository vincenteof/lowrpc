package rpc.client;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.protocol.RpcRequest;
import rpc.srsd.ServiceDiscovery;
import rpc.srsd.ServiceRegistrationInfo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * class $classname
 *
 * The factory class for creating rpc client proxy.
 * The methods in the proxy actually query remote servers.
 *
 * @author Yingpeng.Chen
 * @date 2018/7/26, 15:00
 */

public class RpcClientProxyFactory {
    private static final Logger LOG = LoggerFactory.getLogger(RpcClientProxyFactory.class);

    private RpcClientProxyFactory() {}

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> rpcItf, ServiceDiscovery discovery) {
        LowRpcClient annotation = rpcItf.getDeclaredAnnotation(LowRpcClient.class);
        InvocationHandler handler = new RpcClientInvocationHandler(annotation.serviceName(), discovery);

        return (T) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] {rpcItf},
            handler
        );
    }

    private static class RpcClientInvocationHandler implements InvocationHandler {
        private String serviceName;
        private ServiceDiscovery discovery;

        RpcClientInvocationHandler(String serviceName, ServiceDiscovery discovery) {
            this.serviceName = serviceName;
            this.discovery = discovery;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            RpcRequest request = RpcRequest.create();

            request.setServiceName(serviceName);
            request.setMethodName(method.getName());
            Class<?>[] paramTypes = method.getParameterTypes();
            request.setParamTypes(paramTypes.length == 0 ? null : paramTypes);
            request.setParams(args);

            LowFuture<?> future = asyncInvoke(request, method);
            if (method.getReturnType().equals(LowFuture.class)) {
                return future;
            }

            return future.get();
        }

        // prob: may need some re-connect strategy because the connection return by cache may be no longer valid ???
        private LowFuture<?> asyncInvoke(RpcRequest request, Method method) throws Throwable {
            ServiceRegistrationInfo reg = discovery.getRandomAvailableService(serviceName);
            LOG.info("Select service registration is: {}", reg);
            NettyChannelManager manager = NettyChannelManager.getInstance();

            Channel channel = manager.getChannel(reg.getAddress(), reg.getPort());
            channel.writeAndFlush(request).sync();

            if (method.getDeclaredAnnotation(LazyCreate.class) != null) {
                return LowFuture.lazyCreate(request);
            }

            return LowFuture.create(request);
        }
    }
}
