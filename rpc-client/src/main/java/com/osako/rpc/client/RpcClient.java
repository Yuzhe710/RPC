package com.osako.rpc.client;

import com.osako.codec.RpcDecoder;
import com.osako.codec.RpcEncoder;
import com.osako.entity.RpcRequest;
import com.osako.entity.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC client (establish connection, send RPC request, receive RPC response)
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private final String host;
    private final int port;

    private RpcResponse response;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Process RpcResponse sent from server
     * @param channelHandlerContext
     * @param response
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {
        this.response = response;
    }

    /**
     * Be called when such there is exception
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("api caught exception", cause);
        ctx.close();
    }

    /**
     * establish connection, send request, receive response
     *
     * @param request
     * @return
     * @throws InterruptedException
     */
    public RpcResponse send(RpcRequest request) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            //Create and initialise Netty client side Bootstrap object
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new RpcEncoder(RpcRequest.class));  // encode RPC request
                    pipeline.addLast(new RpcDecoder(RpcResponse.class)); // decode RPC response
                    pipeline.addLast(RpcClient.this); // process RPC Response
                }
            });
            bootstrap.option(ChannelOption.TCP_NODELAY, true); // ?
            // Connect RPC server
            ChannelFuture future = bootstrap.connect(host, port).sync();
            // write RPC request and close the connection
            Channel channel = future.channel();
            channel.writeAndFlush(request).sync();
            channel.closeFuture().sync();
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }


}
