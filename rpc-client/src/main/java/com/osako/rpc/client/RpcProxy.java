package com.osako.rpc.client;

import com.osako.entity.RpcRequest;
import com.osako.entity.RpcResponse;
import org.apache.commons.lang3.StringUtils;import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.osako.rpc.registry.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RPC Dynamic Proxy, augument the send method in com.osako.rpc.client.RpcClient by setting the field of RpcRequest
 */
public class RpcProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private String serviceAddress;

    private ServiceDiscovery serviceDiscovery;

    /**
     * 该构造函数用于供给用户通过spring配置文件注入服务地址
     * @param serviceAddress
     */
    public RpcProxy(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    /**
     * 该构造函数用于提供给用户通过配置文件注入服务发现组件
     * @param serviceDiscovery
     */
    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * 该方法用于对send方法进行增强，屏蔽远程方法调用的细节
     * 使用JDK动态代理机制 创建客户端请求服务的动态代理对象
     * 适用于一个接口对应一个实现类的情况 ？？？？
     *
     * @param interfaceClass 服务的接口类型
     * @param <T>
     * @return
     *
     * 使用示例 (later in api)
     * com.osako.rpc.client.RpcProxy rpcProxy = context.getBean(com.osako.rpc.client.RpcProxy,class)
     * HelloService helloServiceImpl2 = rpcProxy.create(HelloService.class)
     */
    @SuppressWarnings("unchecked") // suppress compile warnings
    public <T> T create(final Class<?> interfaceClass) {
        return create(interfaceClass, "");
    }

    /**
     * 真实的增强方法
     * @param interfaceClass -- 将会是sample.api中的helloService的hello
     * @param serviceVersion -- 空的
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass, final String serviceVersion) {
        // 使用JDK动态代理机制创建动态代理对象
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // create rpcRequest object and set its fields
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setInterfaceName(method.getDeclaringClass().getName());
                        request.setServiceVersion(serviceVersion);
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);
                        if (serviceDiscovery != null) {
                            // 获取服务名称（被暴露的实现类的接口名称）和版本号
                            String serviceName = interfaceClass.getName();
                            if (serviceVersion != null) {
                                String service_version = serviceVersion.trim();
                                if (!StringUtils.isEmpty(service_version)) {
                                    serviceName += "-" + service_version;
                                }
                            }
                            // 获取服务地址
                            serviceAddress = serviceDiscovery.discovery(serviceName);
                            LOGGER.info("discover service: {} => {}", serviceName, serviceAddress);
                        }
                        if (serviceAddress != null) {
                            serviceAddress = serviceAddress.trim();
                            if (StringUtils.isEmpty(serviceAddress)) {
                                throw new RuntimeException("server address is empty");
                            }
                        }

                        // parse the hostname and port number from serviceAddress ？？？为啥要用apache
                        String[] array = StringUtils.split(serviceAddress, ":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);

                        // create RPC client object, build connection/send request/receive response
                        RpcClient client = new RpcClient(host, port);
                        long time = System.currentTimeMillis(); //当前时间
                        RpcResponse response = client.send(request);
                        LOGGER.info("time: {}ms", System.currentTimeMillis() - time);

                        if (response == null) {
                            throw new RuntimeException("response is null");
                        }
                        // 返回RPC响应结果
                        if (response.hasException()) {
                            throw response.getException();
                        } else {
                            return response.getResult();
                        }
                    }
                }
        );
    }
}
