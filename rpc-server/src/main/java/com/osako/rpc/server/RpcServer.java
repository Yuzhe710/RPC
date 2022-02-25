package com.osako.rpc.server;

import com.osako.codec.RpcDecoder;
import com.osako.codec.RpcEncoder;
import com.osako.entity.RpcRequest;
import com.osako.entity.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.osako.rpc.registry.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Netty / RPC 服务器，用于发布 RPC 服务
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    // 服务地址（比如服务被暴露在Netty的8000端口，服务地址就是127.0.0.1.8000）
    private String serviceAddress;

    // 注册服务组建（Zookeeper）
    private ServiceRegistry serviceRegistry;

    // 存放服务名称（被暴露的实现类的接口名称）与服务对象（被暴露的实现类）
    private Map<String, Object> handlerMap = new HashMap<>();

    /**
     * // todo：以下两个构造器，用于提供给用户在 Spring 配置文件中通过构造函数注入？？？
     * @param serviceAddress
     */
    public RpcServer(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public RpcServer(String serviceAddress, ServiceRegistry serviceRegistry) {
        this.serviceAddress = serviceAddress;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Spring容器在加载时会自动调用一次 setApplicationContext, 并将上下文 ApplicationContext传递给这个方法，
     * 该方法的作用就是获取带有 @com.osako.rpc.server.RpcService 注解的类的 interfaceName（被暴露的实现类的接口名称）和 version（被暴露的实现类的版本号）
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 扫描所有带有 @RpcService注解的类
        LOGGER.info("We are here !!!!!!!!!");
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                // 获取类上的注解 @com.osako.rpc.server.RpcService
                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
                // 获取注解 @com.osako.rpc.server.RpcService 的value（即被暴露的实现类的接口名称）
                String serviceName = rpcService.interfaceName().getName();
                // 获取注解 @com.osako.rpc.server.RpcService 的value（即被暴露的实现类的版本号，默认为""）
                String serviceVersion = rpcService.version();
                // 判断版本号是否非空
                if (serviceVersion != null) {
                    serviceVersion = serviceVersion.trim();
                    if (!StringUtils.isEmpty(serviceVersion)) {
                        serviceName += "-" + serviceVersion;
                    }
                }
                // 将服务名称-版本号 与 服务对象 存入 handlerMap
                handlerMap.put(serviceName, serviceBean);
            }
        }
    }

    /**
     * 在初始化 Bean 的时候会自动执行该方法
     * 该方法的目的是启动 Netty 服务器进行服务端和客户端的通信，接收并处理客户端发来的的请求
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 1.bossGroup线程用于接受连接，workerGroup线程用于具体处理。
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 创建并初始化 Netty服务端Bootstrap对象，即服务器引导类
            ServerBootstrap bootstrap = new ServerBootstrap();
            // 3.给引导类配置两大线程组,确定了线程模型
            bootstrap.group(bossGroup, workerGroup);
            // 4.指定 IO 模型为 NIO
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new RpcDecoder(RpcRequest.class)); // 解码器，解码RPC请求
                    pipeline.addLast(new RpcEncoder(RpcResponse.class)); // 编码器，编码RPC响应
                    pipeline.addLast(new RpcServerHandler(handlerMap)); // 处理RPC请求
                }
            });
            // todo: ?????
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            // TCP 协议的心跳机制
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            // 获取服务地址与端口号
            String[] addressArray = StringUtils.split(serviceAddress,":");
            String ip = addressArray[0];
            int port = Integer.parseInt(addressArray[1]);
            // 启动 RPC 服务器
            // todo: sync ??? 还是要看看 // 6.绑定端口,调用 sync 方法阻塞直到绑定完成
            ChannelFuture future = bootstrap.bind(ip, port).sync();
            if (serviceRegistry != null) {

                for (String interfaceName : handlerMap.keySet()) {
                    serviceRegistry.register(interfaceName, serviceAddress);
                    LOGGER.info("register service: {} => {}", interfaceName, serviceAddress);
                    // 关闭 RPC 服务器
                }
            }
//            System.out.println("================================");
//            System.out.println(serviceRegistry == null);
            LOGGER.info("server started on port {}", port);
            // todo: 阻塞等待直到服务器Channel关闭 (closeFuture()方法获取Channel 的CloseFuture对象,然后调用sync()方法)
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
