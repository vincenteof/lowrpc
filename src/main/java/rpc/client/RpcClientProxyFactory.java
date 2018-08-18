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

            // get all available services from consul or zookeeper
            List<ServiceRegistrationInfo> regList =  discovery.getAvailableServices(serviceName);
            ServiceRegistrationInfo reg = regList.get(MathUtil.randomIntInRange(0, regList.size()));
            LOG.info("Select service registration is: {}", reg);

            Configuration conf = ConfigurationUtil.getPropConfig(Constant.RPC_CLIENT_CONFIG);
            long timeOut = conf.getLong(Constant.RPC_CLIENT_TIMEOUT);

            EventLoopGroup group = new NioEventLoopGroup();
            RpcResultCollector collector = RpcResultCollector.getInstance();
            long timeAcc = 0;
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcClientInitializer());
                Channel channel = b.connect(reg.getAddress(), reg.getPort()).sync().channel();
                channel.writeAndFlush(request).sync();

                LOG.info("Request received in dynamic proxy: {}", request);

                // use the requestId to retrieve rpc result
                Integer requestId = request.getRequestId();
                RpcResponse response;

                while ((response = collector.getIfPresent(requestId)) == null && timeAcc < timeOut) {
                    Thread.sleep(100);
                    LOG.info("Wait for response for 100ms");
                    timeAcc += 100;
                }
                channel.closeFuture().sync();

                if (response == null) {
                    throw new RuntimeException("Client timeout");
                }

                if (response.getStatus() == 0) {
                    throw new RuntimeException("Rpc call failed for: " + response.getDescription());
                }

                return response.getValue();
            } finally {
                group.shutdownGracefully();
            }
        }
    }
}
