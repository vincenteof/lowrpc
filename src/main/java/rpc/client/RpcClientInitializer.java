package rpc.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import rpc.protocol.RpcDecoder;
import rpc.protocol.RpcEncoder;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 15:37
 */
public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new RpcDecoder());
        pipeline.addLast(new RpcEncoder(true));
        pipeline.addLast(new RpcClientHandler());
    }
}
