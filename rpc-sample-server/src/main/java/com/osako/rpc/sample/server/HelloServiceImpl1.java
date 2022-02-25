package com.osako.rpc.sample.server;

import com.osako.rpc.sample.api.HelloService;
import com.osako.rpc.server.RpcService;

/**
 * 服务接口的实现类
 */
@RpcService(interfaceName = HelloService.class) // 暴露该服务
public class HelloServiceImpl1 implements HelloService {

    @Override
    public String hello(String name) {
        return "Hello!" + name;
    }
}
