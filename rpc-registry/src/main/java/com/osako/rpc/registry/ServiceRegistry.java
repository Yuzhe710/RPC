package com.osako.rpc.registry;

/**
 * Service registry interface
 */
public interface ServiceRegistry {

    /**
     * register serviceName and serviceAddress
     * @param serviceName
     * @param serviceAddress
     */
    void register(String serviceName, String serviceAddress);
}
