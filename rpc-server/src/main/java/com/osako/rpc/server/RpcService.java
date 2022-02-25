package com.osako.rpc.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义一个注解@RpcService来标记我们暴露的服务，当用户使用本框架时，
 * 需要将@RpcService注解定义在服务接口的实现类上，表示暴露该方法/服务
 * 这样服务端在寻找客户端请求的方法/服务的时候，只会寻找被暴露的方法/服务 ？？？？
 * 使用示例：@com.osako.rpc.server.RpcService(HelloService.class)
 * //todo：？？？？？
 */
@Target(ElementType.TYPE)  // 表示 @com.osako.rpc.server.RpcService 注解可以放在接口，类，枚举，注解上
@Retention(RetentionPolicy.RUNTIME) // 表示@com.osako.rpc.server.RpcService 注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Component  // 表明 @com.osako.rpc.server.RpcService 注解可被 Spring 扫描
public @interface RpcService {

    /**
     * 被暴露的实现类的接口类型 Class
     */
    Class<?> interfaceName();

    /**
     * 被暴露的实现类的版本号（服务版本号）
     */
    String version() default "";
}
