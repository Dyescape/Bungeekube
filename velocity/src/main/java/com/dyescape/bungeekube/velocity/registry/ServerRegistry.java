package com.dyescape.bungeekube.velocity.registry;

import com.dyescape.bungeekube.kubernetes.discovery.DiscoveredService;

import java.util.HashSet;
import java.util.Set;

public class ServerRegistry {

    private final Set<DiscoveredService> registeredServers = new HashSet<>();
    private final Set<DiscoveredService> registeredDefaultServers = new HashSet<>();

    public void register(DiscoveredService info) {
        this.registeredServers.add(info);
        if (info.isDefault()) {
            this.registeredDefaultServers.add(info);
        }
    }

    public void unregister(DiscoveredService info) {
        this.registeredServers.remove(info);
        this.registeredDefaultServers.remove(info);
    }

    public Set<DiscoveredService> getRegisteredDefaultServers() {
        return this.registeredDefaultServers;
    }

    public Set<DiscoveredService> getRegisteredServers() {
        return this.registeredServers;
    }
}
