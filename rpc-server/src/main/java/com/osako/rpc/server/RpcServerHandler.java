package com.osako.rpc.server;

import com.osako.entity.RpcRequest;
import com.osako.entity.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.zookeeper.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Netty / RPC 服务端处理器 （处理rpc请求）
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        //
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId()); // 指定该 response 对应的 request id
        try {
            Object result = handle(request);
            response.setResult(result);
        } catch (Exception e) {
            LOGGER.error("handle result failure", e);
            response.setException(e);
        }
        // 写入 RPC 响应对象并自动关闭连接 //todo ：addListener ???????
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 处理请求 （获取客户端请求的方法和参数，通过反射进行调用）
     * @param request
     * @return 调用结果
     * @throws Exception
     */
    private Object handle(RpcRequest request) throws Exception {
        String serviceName = request.getInterfaceName();
        String serviceVersion = request.getServiceVersion();
        if (serviceVersion != null) {
            serviceVersion = serviceVersion.trim();
            if (!StringUtils.isEmpty(serviceVersion)) {
                serviceName += "-" + serviceVersion;
            }
        }
        // 获取服务对象
        Object serviceBean = handlerMap.get(serviceName);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("Cannot find service bean by key: %s", serviceName));
        }
        // 获取反射调用所需的参数
        Class<?> serviceClass =  serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        // 执行反射调用
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
//        System.out.println("let us see see");
//        for (Object p : parameters) {
//            System.out.println(p);
//        }
//        System.out.println(method.invoke(serviceBean, parameters) instanceof String);
        return method.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.info("Server caught exception", cause);
        ctx.close();
    }
}
