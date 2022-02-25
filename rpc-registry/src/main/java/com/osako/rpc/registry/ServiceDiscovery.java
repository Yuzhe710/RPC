package com.osako.rpc.registry;

/**
 * Service discovery interface
 */
public interface ServiceDiscovery {

    /**
     * search for service address using serviceName
     * @param serviceName
     * @return
     */
    String discovery(String serviceName);
}
