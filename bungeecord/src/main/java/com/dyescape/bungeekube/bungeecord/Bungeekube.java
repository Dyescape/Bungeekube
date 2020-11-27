package com.dyescape.bungeekube.bungeecord;

import com.dyescape.bungeekube.kubernetes.discovery.DiscoveredService;
import com.dyescape.bungeekube.kubernetes.discovery.KubernetesServiceDiscovery;
import com.dyescape.bungeekube.kubernetes.discovery.ServiceDiscovery;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Logger;

public class Bungeekube extends Plugin {

    private static final Logger LOGGER = Logger.getLogger("Bungeekube");

    @Override
    public void onEnable() {
        this.initialiseLogging();

        ServiceDiscovery discovery = new KubernetesServiceDiscovery(this.createKubernetesClient());
        List<DiscoveredService> foundServices = discovery.Discover();

        if (!foundServices.isEmpty()) {
            LOGGER.info("Discovered these services:");
        } else {
            LOGGER.warning("No services found! Did you follow the installation manual?");
        }

        for (DiscoveredService service : discovery.Discover()) {
            LOGGER.info(String.format("  - %s:%s (%s)", service.getHost(), service.getPort(),
                    service.getName()));

            this.registerServer(service);
        }
    }

    private void registerServer(DiscoveredService service) {
        ServerInfo info = this.getProxy().constructServerInfo(service.getName(),
                new InetSocketAddress(service.getHost(), service.getPort()), "", false);
        this.getProxy().getServers().put(service.getName(), info);
    }

    private KubernetesClient createKubernetesClient() {
        LOGGER.info("Initialising Kubernetes client. This may take a few seconds...");
        return new DefaultKubernetesClient();
    }

    private void initialiseLogging() {
        LOGGER.setParent(this.getProxy().getLogger());
    }
}
