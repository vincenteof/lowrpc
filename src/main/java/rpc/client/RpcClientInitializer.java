package rpc.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import rpc.protocol.JsonRpcDecoder;
import rpc.protocol.JsonRpcEncoder;

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

        pipeline.addLast(new JsonRpcDecoder());
        pipeline.addLast(new JsonRpcEncoder(true));
        pipeline.addLast(new RpcClientHandler());
    }
}
