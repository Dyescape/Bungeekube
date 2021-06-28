package com.dyescape.bungeekube.bungeecord;

import com.dyescape.bungeekube.bungeecord.watcher.BungeeDiscoveryWatcher;
import com.dyescape.bungeekube.kubernetes.discovery.DiscoveredService;
import com.dyescape.bungeekube.kubernetes.discovery.KubernetesPodMapper;
import com.dyescape.bungeekube.kubernetes.discovery.KubernetesServiceDiscovery;
import com.dyescape.bungeekube.kubernetes.discovery.ServiceDiscovery;
import com.dyescape.bungeekube.kubernetes.discovery.watcher.KubernetesDiscoveryWatcher;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Bungeekube extends Plugin {

    private static final Logger LOGGER = Logger.getLogger("Bungeekube");

    @Override
    public void onEnable() {
        this.initialiseLogging();

        KubernetesClient client = this.createKubernetesClient();
        KubernetesPodMapper mapper = new KubernetesPodMapper();

        ServiceDiscovery discovery = new KubernetesServiceDiscovery(client, mapper);
        BungeeDiscoveryWatcher watcher = new BungeeDiscoveryWatcher(this.getProxy());

        List<DiscoveredService> foundServices = discovery.Discover();

        if (!foundServices.isEmpty()) {
            LOGGER.info("Discovered these pods:");
        } else {
            LOGGER.warning("No pods found! Did you follow the installation manual?");
        }

        Set<String> foundPods = new HashSet<>();

        for (DiscoveredService service : foundServices) {
            LOGGER.info(String.format("  - %s:%s (%s)", service.getHost(), service.getPort(),
                    service.getName()));

            foundPods.add(service.getHost());

            this.registerServer(service);
        }

        new KubernetesDiscoveryWatcher(client, watcher, mapper, foundPods).startListening();
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
