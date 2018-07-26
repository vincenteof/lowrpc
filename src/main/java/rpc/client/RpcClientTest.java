package rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import rpc.protocol.RpcRequest;
import rpc.service.TestService;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 15:39
 */
public class RpcClientTest {
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8322"));

    public static void main(String[] args) throws Exception {
//        EventLoopGroup group = new NioEventLoopGroup();
//
//        try {
//            Bootstrap b = new Bootstrap();
//            b.group(group)
//                .channel(NioSocketChannel.class)
//                .handler(new RpcClientInitializer());
//
//            Channel ch = b.connect(HOST, PORT).sync().channel();
//
//            RpcRequest request = RpcRequest.create();
////            request.setClzName("rpc.service.TestService");
////            request.setMethodName("testStateWithoutParams");
//            request.setClzName("rpc.service.HellWorldService");
//            request.setMethodName("hello");
//            request.setParamTypes(new Class<?>[] {});
//            request.setParams(new Object[] {});
////            request.setMethodName("testStateWithParams");
////            request.setParamTypes(new Class<?>[] {Integer.class});
////            request.setParams(new Object[] {0});
//            ch.writeAndFlush(request);
//            ch.closeFuture().sync();
//        } finally {
//            group.shutdownGracefully();
//        }
        TestServiceClient client = RpcClientProxyFactory.createProxy(TestServiceClient.class);
        System.out.println(client.testPureWithoutParams());
        System.out.println(client.testStateWithoutParams());
        System.out.println(client.testStateWithoutParams());
        System.out.println(client.testStateWithParams(2));
    }
}
