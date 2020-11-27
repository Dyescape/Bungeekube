package com.dyescape.bungeekube.bungeecord;

import com.dyescape.bungeekube.kubernetes.discovery.DiscoveredService;
import com.dyescape.bungeekube.kubernetes.discovery.KubernetesServiceDiscovery;
import com.dyescape.bungeekube.kubernetes.discovery.ServiceDiscovery;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import net.md_5.bungee.api.plugin.Plugin;

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
        }

        for (DiscoveredService discoveredService : discovery.Discover()) {
            LOGGER.info(String.format("  - %s:%s (%s)", discoveredService.getHost(), discoveredService.getPort(),
                    discoveredService.getName()));
        }
    }

    private KubernetesClient createKubernetesClient() {
        LOGGER.info("Initialising Kubernetes client. This may take a few seconds...");
        return new DefaultKubernetesClient();
    }

    private void initialiseLogging() {
        LOGGER.setParent(this.getProxy().getLogger());
    }
}
