package com.osako.rpc.sample.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Server端启动服务器并发布服务 （其实就是加载Spring配置文件）
 */
public class RpcBootstrap {
    public static void main(String[] args) {
        // 加载Spring 配置文件
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
