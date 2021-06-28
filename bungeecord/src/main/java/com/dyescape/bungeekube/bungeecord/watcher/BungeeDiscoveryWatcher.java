package com.dyescape.bungeekube.bungeecord.watcher;

import com.dyescape.bungeekube.kubernetes.discovery.DiscoveredService;
import com.dyescape.bungeekube.kubernetes.discovery.watcher.DiscoveryWatcher;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;

public class BungeeDiscoveryWatcher implements DiscoveryWatcher {

    private final ProxyServer proxyServer;

    public BungeeDiscoveryWatcher(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public void onCreate(DiscoveredService service) {
        ServerInfo info = this.discoveredServiceAsServerInfo(service);

        this.proxyServer.getServers().put(service.getName(), info);
    }

    @Override
    public void onDelete(DiscoveredService service) {
        this.proxyServer.getServers().remove(service.getName());
    }

    private ServerInfo discoveredServiceAsServerInfo(DiscoveredService service) {
        return this.proxyServer.constructServerInfo(service.getName(),
                new InetSocketAddress(service.getHost(), service.getPort()), "", false);
    }
}
