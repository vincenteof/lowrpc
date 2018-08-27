package rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.protocol.RpcRequest;
import rpc.protocol.RpcResponse;
import rpc.srsd.ServiceDiscovery;
import rpc.srsd.ServiceRegistrationInfo;
import rpc.util.ConfigurationUtil;
import rpc.util.Constant;
import rpc.util.MathUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.Future;

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

            if (method.getReturnType().equals(LowFuture.class)) {
                return asyncInvoke(request, method);
            }

            return syncInvoke(request);
        }

        // prob: effective ways to notify this thread when result is available, like `countdownlatch` ???
        private Object syncInvoke(RpcRequest request) throws Throwable {
            // get random available services from consul or zookeeper
            ServiceRegistrationInfo reg = discovery.getRandomAvailableService(serviceName);
            LOG.info("Select service registration is: {}", reg);

            RpcResultCollector collector = RpcResultCollector.getInstance();
            Bootstrap bootstrap = NettyBootstrap.oioBootstrap();
            NettyChannelCache channelCache = NettyChannelCache.getInstance();

            Channel channel = channelCache.getChannel(reg.getAddress(), reg.getPort(), bootstrap);
            LOG.info(
                "Cached channel status: `isOpen: {}`, `isActive: {}`, `isRegistered: {}`, `isWritable: {}`",
                channel.isOpen(), channel.isActive(), channel.isRegistered(), channel.isWritable()
            );
            channel.writeAndFlush(request).sync();

            Configuration conf = ConfigurationUtil.getPropConfig(Constant.RPC_CLIENT_CONFIG);
            long timeOut = conf.getLong(Constant.RPC_CLIENT_TIMEOUT);
            long timeAcc = 0;
            Integer requestId = request.getRequestId();
            RpcResponse response;

            while ((response = collector.getIfPresent(requestId)) == null && timeAcc < timeOut) {
                Thread.sleep(100);
                LOG.info("Wait for response for 100ms");
                timeAcc += 100;
            }

            if (response == null) {
                throw new RuntimeException("Client timeout");
            }

            if (response.getStatus() == 0) {
                throw new RuntimeException("Rpc call failed for: " + response.getDescription());
            }

            return response.getValue();
        }

        // prob: may need some re-connect strategy because the connection return by cache may be no longer valid ???
        private LowFuture<?> asyncInvoke(RpcRequest request, Method method) throws Throwable {
            ServiceRegistrationInfo reg = discovery.getRandomAvailableService(serviceName);
            LOG.info("Select service registration is: {}", reg);

            Bootstrap bootstrap = NettyBootstrap.nioBootstrap();
            NettyChannelCache channelCache = NettyChannelCache.getInstance();

            Channel channel = channelCache.getChannel(reg.getAddress(), reg.getPort(), bootstrap);
            channel.writeAndFlush(request).sync();

            if (method.getDeclaredAnnotation(LazyCreate.class) != null) {
                return LowFuture.lazyCreate(request);
            }

            return LowFuture.create(request);
        }
    }
}
