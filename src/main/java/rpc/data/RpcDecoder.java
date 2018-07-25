package rpc.data;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import rpc.util.JsonSerializationUtil;

import java.util.List;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/7/25, 13:49
 */
public class RpcDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 5) {
            return;
        }

        in.markReaderIndex();
        int magicNumber = in.readUnsignedByte();
        if (magicNumber != 'Q' && magicNumber != 'P') {
            in.resetReaderIndex();
            throw new CorruptedFrameException("Invalid magic number: " + magicNumber);
        }

        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        byte[] decoded = new byte[dataLength];
        in.readBytes(decoded);

        if (magicNumber == 'Q') {
            out.add(JsonSerializationUtil.fromBytes(decoded, RpcRequest.class));
        } else {
            out.add(JsonSerializationUtil.fromBytes(decoded, RpcResponse.class));
        }
    }
}
