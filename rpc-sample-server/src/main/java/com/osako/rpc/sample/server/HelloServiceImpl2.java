package com.osako.rpc.sample.server;

import com.osako.rpc.sample.api.HelloService;
import com.osako.rpc.server.RpcService;

/**
 * com.osako.rpc.sample.api.HelloService 接口实现类2（暴露该服务，需要指明version）
 */
@RpcService(interfaceName = HelloService.class, version="helloServiceImpl2") // 指定接口类型和版本
public class HelloServiceImpl2 implements HelloService {

    @Override
    public String hello(String name) {
        return "Hello!" + name + ", I am helloServiceImpl2";
    }
}
