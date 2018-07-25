package rpc.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import rpc.data.RpcDecoder;
import rpc.data.RpcEncoder;

import java.util.Map;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 13:59
 */
public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {
    private Map<String, Object> beans;

    public RpcServerInitializer(Map<String, Object> beans) {
        this.beans = beans;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new RpcDecoder());
        pipeline.addLast(new RpcEncoder(false));
        pipeline.addLast(new RpcServerHandler(beans));
    }
}
