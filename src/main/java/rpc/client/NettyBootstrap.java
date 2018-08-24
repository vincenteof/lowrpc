package rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/8/22, 11:57
 */
public class NettyBootstrap {
    private NettyBootstrap() {}

    private static Bootstrap nioInstance;
    private static Bootstrap oioInstance;

    public static Bootstrap nioBootstrap() {
        synchronized (NettyBootstrap.class) {
            if (nioInstance == null) {
                EventLoopGroup group = new NioEventLoopGroup();
                Bootstrap b = new Bootstrap();
                b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcClientInitializer());
                nioInstance = b;
            }
        }

        return  nioInstance;
    }

    public static Bootstrap oioBootstrap() {
        synchronized (NettyBootstrap.class) {
            if (oioInstance == null) {
                EventLoopGroup group = new OioEventLoopGroup();
                Bootstrap b = new Bootstrap();
                b.group(group)
                    .channel(OioSocketChannel.class)
                    .handler(new RpcClientInitializer());
                oioInstance = b;
            }
        }
        return oioInstance;
    }
}
