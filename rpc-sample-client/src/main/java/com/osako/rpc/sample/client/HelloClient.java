package com.osako.rpc.sample.client;

import com.osako.rpc.client.RpcProxy;
import com.osako.rpc.sample.api.HelloService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HelloClient {
    public static void main(String[] args) throws Exception {
        // 加载Spring配置文件
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        // 获取RpcProxy动态代理对象
        RpcProxy rpcProxy = context.getBean(RpcProxy.class);

        /**
         * 测试HelloService接口的实现类 1
         */
        // 调用 RpcProxy 对象的, jdk 动态代理
        HelloService helloService = rpcProxy.create(HelloService.class);
        // 调用 RPC 代理接口的方法（调用远程接口方法就跟调用本地方法一样简单）
        String result = helloService.hello("World");
        System.out.println(result);

        /**
         * 测试HelloService接口的实现类 2
         */
//        HelloService helloServiceImpl2 = rpcProxy.create(HelloService.class, "helloServiceImpl2");
//        String result2 = helloServiceImpl2.hello("Java");
//        System.out.println(result2);

        System.exit(0);
    }
}
