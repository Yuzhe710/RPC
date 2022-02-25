package com.osako.rpc.registry.zookeeper;

import com.osako.rpc.registry.ServiceDiscovery;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Use Zookeeper to discover service
 */
public class ZookeeperServiceDiscovery implements ServiceDiscovery {
    private static final Logger LOGGER  = LoggerFactory.getLogger(ZookeeperServiceDiscovery.class);

    private String zkAddress;

    public ZookeeperServiceDiscovery(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    /**
     * discover service
     */
    @Override
    public String discovery(String serviceName) {
        ZkClient zkClient = new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
        LOGGER.info("connect Zookeeper");


        try {
            // get service node
            String servicePath = Constant.ZK_REGISTRY_PATH + "/" + serviceName;
            // if the node does not exist, throw an exception
            if (!zkClient.exists(servicePath)) {
                throw new RuntimeException(String.format("can not find any service node on path : %s", servicePath));
            }

            // get all child path of a node
            List<String> addressList = zkClient.getChildren(servicePath);
            if (addressList.isEmpty()) {
                throw new RuntimeException(String.format("can not find any address node on path: %s", servicePath));
            }

            // get address node(one service node can have multiple service address (same service but have multiple dispathches))
            String address;
            int size = addressList.size();
            // if only one address, get that address
            if (size == 1) {
                address = addressList.get(0);
                LOGGER.info("get only address node: {}", address);
            } else {
                // Nice to use in high concurrency environment
                address = addressList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.info("get random address node: {}", address);
            }

            String addressPath = servicePath + "/" + address;
            // read content of the node
            System.out.println("data is !!!!!!!!! " + zkClient.readData(addressPath));
            return zkClient.readData(addressPath);
        } finally {
            zkClient.close();
        }
    }
}
