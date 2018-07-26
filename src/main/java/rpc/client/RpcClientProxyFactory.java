package rpc.client;

import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import rpc.protocol.RpcRequest;
import rpc.protocol.RpcResponse;
import rpc.service.TestService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/26, 15:00
 */

// shutdown and clean resources !!!
public class RpcClientProxyFactory {
    private RpcClientProxyFactory() {}

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> rpcItf) {
        LowRpcClient annotation = rpcItf.getDeclaredAnnotation(LowRpcClient.class);
        InvocationHandler handler = new RpcClientInvocationHandler(
            annotation.host(),
            annotation.port(),
            annotation.clzName()
        );

        return (T) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] {rpcItf},
            handler
        );
    }

    private static class RpcClientInvocationHandler implements InvocationHandler {
        private String host;
        private int port;
        private String clzName;

        RpcClientInvocationHandler(String host, int port, String clzName) {
            this.host = host;
            this.port = port;
            this.clzName = clzName;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            RpcRequest request = RpcRequest.create();

            request.setClzName(clzName);
            request.setMethodName(method.getName());
            Class<?>[] paramTypes = method.getParameterTypes();
            request.setParamTypes(paramTypes.length == 0 ? null : paramTypes);
            request.setParams(args);

            EventLoopGroup group = new NioEventLoopGroup();
            RpcResultCollector collector = RpcResultCollector.getInstance();
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcClientInitializer());
                Channel channel = b.connect(host, port).sync().channel();
                channel.writeAndFlush(request).sync();


                Integer requestId = request.getRequestId();
                System.err.println(request);
                while (!collector.contains(requestId)) {
                    Thread.sleep(100);
                }

                channel.closeFuture().sync();
            } finally {
                group.shutdownGracefully();
            }

            RpcResponse response = collector.get(request.getRequestId());

            if (response.getStatus() == 0) {
                throw new RuntimeException("Rpc call failed for: " + response.getDescription());
            }

            return response.getValue();
        }
    }

    public static void main(String[] args) throws NoSuchMethodException {
        Class<TestService> clz = TestService.class;
        Method method = clz.getMethod("testStateWithParams", Integer.class);
        System.out.println(method.getDeclaringClass().getName());
        System.out.println(method.getName());
        for (Class c: method.getParameterTypes()) {
            System.out.println(c);
        }
        System.out.println(TestServiceClient.class.getDeclaredAnnotation(LowRpcClient.class).host());
        System.out.println(JSONObject.toJSONString(RpcRequest.create()));
    }
}
