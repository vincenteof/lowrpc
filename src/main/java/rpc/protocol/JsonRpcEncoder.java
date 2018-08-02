package rpc.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import rpc.util.JsonSerializationUtil;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 13:47
 */
public class JsonRpcEncoder extends MessageToByteEncoder {
    private Boolean isRequest;

    public JsonRpcEncoder(Boolean isRequest) {
        this.isRequest = isRequest;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        byte[] data = JsonSerializationUtil.toBytes(msg);
        int dataLength = data.length;

        if (isRequest) {
            out.writeByte((byte) 'Q');
        } else {
            out.writeByte((byte) 'P');
        }

        out.writeInt(dataLength);
        out.writeBytes(data);
    }
}
