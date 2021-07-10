package com.dyescape.bungeekube.velocity.watcher;

import com.dyescape.bungeekube.kubernetes.discovery.DiscoveredService;
import com.dyescape.bungeekube.kubernetes.discovery.watcher.DiscoveryWatcher;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import java.net.InetSocketAddress;

public class VelocityDiscoveryWatcher implements DiscoveryWatcher {

    private final ProxyServer proxyServer;

    public VelocityDiscoveryWatcher(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    public void addServer(DiscoveredService service) {
        ServerInfo info = this.discoveredServiceAsServerInfo(service);
        this.proxyServer.registerServer(info);

        // Not exactly clean, but works for now
        this.proxyServer.getConfiguration().getAttemptConnectionOrder().add(service.getName());
    }

    public void removeServer(DiscoveredService service) {
        ServerInfo info = this.discoveredServiceAsServerInfo(service);

        // Not exactly clean, but works for now
        this.proxyServer.getConfiguration().getAttemptConnectionOrder().remove(service.getName());
        this.proxyServer.unregisterServer(info);
    }

    private ServerInfo discoveredServiceAsServerInfo(DiscoveredService service) {
        return new ServerInfo(service.getName(), new InetSocketAddress(service.getHost(), service.getPort()));
    }

    @Override
    public void onCreate(DiscoveredService service) {
        this.addServer(service);
    }

    @Override
    public void onDelete(DiscoveredService service) {
        this.removeServer(service);
    }
}
