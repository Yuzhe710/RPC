package com.osako.rpc.registry.zookeeper;

import com.osako.rpc.registry.ServiceRegistry;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use com.osako.rpc.registry.zookeeper to implement service registry
 */
public class ZookeeperServiceRegistry implements ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);

    private final ZkClient zkClient;

    public ZookeeperServiceRegistry(String zkAddress) {
        // create com.osako.rpc.registry.zookeeper client
        zkClient = new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
        LOGGER.info("connect com.osako.rpc.registry.zookeeper");
    }

    /**
     * register service
     * @param serviceName - method name
     * @param serviceAddress - method address
     */
    @Override
    public void register(String serviceName, String serviceAddress) {
        String registryPath = Constant.ZK_REGISTRY_PATH;
        if (!zkClient.exists(registryPath)) {
            zkClient.createPersistent(registryPath);
            LOGGER.info("create registry node: {}", registryPath);
        }

        // create service node under registry node
        String servicePath = registryPath + "/" + serviceName;
        if (!zkClient.exists(servicePath)) {
            zkClient.createPersistent(servicePath);
            LOGGER.info("create service node: {}, ", servicePath);
        }
        // create address node (ephemeral)
        String addressPath = servicePath + "/address-";
        // the node's names are sequential
        String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
        LOGGER.debug("create address node: {}", addressNode);
    }
}
