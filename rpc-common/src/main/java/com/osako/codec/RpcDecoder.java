package com.osako.codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import com.osako.serializer.CustomSerializer;

import java.util.List;

/**
 * the decoder
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass; // deser byte array into object of generic class

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        // the message head is 4 Bytes, so the readable bytes must > 4 bytes
        if (in.readableBytes() < 4) {
            return ;
        }

        // mark current readIndex
        in.markReaderIndex();
        // read the message head, the length of the message, it will increase readIndex
        int dataLength = in.readInt();
        // if readable bytes is less than data length, reset the readIndex and return
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        // de-serialize
        byte[] body = new byte[dataLength];
        in.readBytes(body);
        Object obj = CustomSerializer.deserialize(body, genericClass);
        out.add(obj);
    }

}
