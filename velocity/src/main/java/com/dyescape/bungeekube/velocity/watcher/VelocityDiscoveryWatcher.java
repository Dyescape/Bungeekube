package com.dyescape.bungeekube.velocity.watcher;

import com.dyescape.bungeekube.kubernetes.discovery.DiscoveredService;
import com.dyescape.bungeekube.kubernetes.discovery.watcher.DiscoveryWatcher;
import com.dyescape.bungeekube.velocity.manager.ServerManager;

public class VelocityDiscoveryWatcher implements DiscoveryWatcher {

    private final ServerManager serverManager;

    public VelocityDiscoveryWatcher(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    @Override
    public void onCreate(DiscoveredService service) {
        this.serverManager.addServer(service);
    }

    @Override
    public void onDelete(DiscoveredService service) {
        this.serverManager.removeServer(service);
    }
}
