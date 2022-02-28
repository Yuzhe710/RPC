# RPC
A light-weight distributed RPC framework based on Spring + Netty + Zookeeper + Protostuff
![RPC项目框架](https://user-images.githubusercontent.com/56336682/155658873-a1e382d0-581d-4aee-970c-50ff6fe16119.png)

## Framework user guide
Sample code to use the framework is in package **rpc-sample_xxx** </br>

To use this framework, we need to wire the service registry module and RPC server into server package **rpc-sample-server**, and wire the service discovery module and RPC client into client package **rpc-sample-client**, Details follow: </br>

### 1. Define the RPC interface
In **rpc-sample-api** module </br>



package com.osako.rpc.sample.api;

public interface HelloService {

    String hello(String name);

}



### 2. Publish the service
In **rpc-sample-server** module </br>

1️⃣ **Add the dependencies** </br>

    <dependencies>
        <!--RPC 接口所在模块的依赖-->
        <dependency>
            <groupId>com.osako</groupId>
            <artifactId>rpc-sample-api</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <!--RPC 服务端框架的依赖-->
        <dependency>
            <groupId>com.osako</groupId>
            <artifactId>rpc-server</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <!--注册中心所在模块的依赖-->
        <dependency>
            <groupId>com.osako</groupId>
            <artifactId>rpc-registry</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <!--Spring-->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>5.3.1</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>

2️⃣ **Implement RPC interface** </br>

Use @RpcService to annotate the implementation class of the service means to expose the service </br>

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

🔔 IF there are multiple implementation class of the interface, we need to have **version** configuration in RpcService annotation </br>


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

3️⃣ **Configure RPC server** </br>

1. spring.xml </br>
Use Spring to register related module </br>

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd">

    <!-- Scan com.osako.rpc.sample.server -->
    <context:component-scan base-package="com.osako.rpc.sample.server"></context:component-scan>

    <!-- Configure arguments for server -->
    <context:property-placeholder location="classpath:rpc.properties"></context:property-placeholder>

    <!-- Configure service registry module Zookeeper -->
    <bean id = "serviceRegistry" class="com.osako.rpc.registry.zookeeper.ZookeeperServiceRegistry">
        <!-- Registry address (Default in Zookeeper) 127.0.0.1:2181 -->
        <constructor-arg name="zkAddress" value="${rpc.registry_address}"></constructor-arg>
    </bean>

    <!-- Configure RPC server -->
    <bean id = "rpcServer" class="com.osako.rpc.server.RpcServer">
        <!--Service Address 127.0.0.1:2181-->
        <constructor-arg name = "serviceAddress" value="${rpc.service_address}"></constructor-arg>
        <!-- Registry Center - Zookeeper -->
        <constructor-arg name = "serviceRegistry" ref="serviceRegistry"></constructor-arg>
    </bean>

</beans>

2. rpc.properties </br>
Following configuration means we connect local Zookeeper server, and publish RPC service at port 8000 </br>

#### zookeeper　Server (registry center)
rpc.registry_address = 127.0.0.1:2181

#### RPC Client
rpc.service_address = 127.0.0.1:8000

4️⃣ **Launch / Publish RPC service** </br>
Run class RpcBootstrap, it will load the spring configuration file (as above), which publish RPC service, and register the service </br>

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


### 3. Call the RPC Service

See rpc-sample-client module </br>

1️⃣ **Add the dependencies** </br>

<dependencies>
        <!--RPC 客户端框架的依赖-->
        <dependency>
            <groupId>com.osako</groupId>
            <artifactId>rpc-client</artifactId>
            <version>1.0-SNAPSHOT</version>
            <scope>compile</scope>
        </dependency>

        <!--RPC 接口所在模块的依赖-->
        <dependency>
            <groupId>com.osako</groupId>
            <artifactId>rpc-sample-api</artifactId>
            <version>1.0-SNAPSHOT</version>
            <scope>compile</scope>
        </dependency>

        <!--注册中心所在模块的依赖-->
        <dependency>
            <groupId>com.osako</groupId>
            <artifactId>rpc-registry</artifactId>
            <version>1.0-SNAPSHOT</version>
            <scope>compile</scope>
        </dependency>

        <!--Spring-->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>5.3.1</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
    
2️⃣  **Configure RPC client** </br>

1. spring.xml </br>

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd">

    <context:property-placeholder location="classpath:rpc.properties"/>

    <!-- service discovery module -->
    <bean id="serviceDiscovery" class="com.osako.rpc.registry.zookeeper.ZookeeperServiceDiscovery">
        <constructor-arg name="zkAddress" value="${rpc.registry_address}"/>
    </bean>

    <bean id="rpcProxy" class="com.osako.rpc.client.RpcProxy">
        <constructor-arg name="serviceDiscovery" ref="serviceDiscovery" />
    </bean>

</beans>

ServiceDiscovery is to use zookeeper to discover the service, need to provide zookeeper address </br>
rpcProxy is to obtain RPC proxy interface </br>

2. rpc.properties

#### zookeeper server address (ip address + port)
rpc.registry_address = 127.0.0.1:2181

3️⃣ **Call the RPC Service** </br>

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
