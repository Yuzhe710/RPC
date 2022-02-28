# RPC
A light-weight distributed RPC framework based on Spring + Netty + Zookeeper + Protostuff
![RPC项目框架](https://user-images.githubusercontent.com/56336682/155658873-a1e382d0-581d-4aee-970c-50ff6fe16119.png)

## Framework user guide
Sample code to use the framework is in package **rpc-sample_xxx** </br>

To use this framework, we need to wire the service registry module and RPC server into server package **rpc-sample-server**, and wire the service discovery module and RPC client into client package **rpc-sample-client**, Details follow: </br>

### 1. Define the RPC interface
In **rpc-sample-api** module </br>
<img width="452" alt="rpc-github-readme1" src="https://user-images.githubusercontent.com/56336682/155926146-d8224f36-d8a1-49c9-a573-edfd219e7568.png">

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
