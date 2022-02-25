package com.osako.codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import com.osako.serializer.CustomSerializer;

/**
 * The encoder
 */
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object in, ByteBuf out) throws Exception {
        if (genericClass.isInstance(in)) {
            // ser
            byte[] data = CustomSerializer.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }

}
