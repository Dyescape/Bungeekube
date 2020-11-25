package com.dyescape.bungeekube.bungeecord;

import com.dyescape.bungeekube.kubernetes.discovery.DiscoveredService;
import com.dyescape.bungeekube.kubernetes.discovery.KubernetesServiceDiscovery;
import com.dyescape.bungeekube.kubernetes.discovery.ServiceDiscovery;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.List;
import java.util.logging.Logger;

public class Bungeekube extends Plugin {

    private static final Logger LOGGER = Logger.getLogger("Bungeekube");

    @Override
    public void onEnable() {
        this.initialiseLogging();

        ServiceDiscovery discovery = new KubernetesServiceDiscovery();
        List<DiscoveredService> foundServices = discovery.Discover();

        if (!foundServices.isEmpty()) {
            LOGGER.info("Discovered these services:");
        }

        for (DiscoveredService discoveredService : discovery.Discover()) {
            LOGGER.info(String.format("  - %s:%s (%s)", discoveredService.getHost(), discoveredService.getPort(),
                    discoveredService.getName()));
        }
    }

    private void initialiseLogging() {
        LOGGER.setParent(this.getProxy().getLogger());
    }
}
