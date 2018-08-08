package rpc.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.protocol.RpcResponse;

/**
 * class $classname
 *
 * Handler for receiving response from rpc server.
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 15:26
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger LOG = LoggerFactory.getLogger(RpcClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) {
        LOG.info("The result is: {}", msg);
        RpcResultCollector.getInstance().put(msg.getRequestId(), msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
