package com.dyescape.bungeekube.velocity;

import com.dyescape.bungeekube.kubernetes.discovery.DiscoveredService;
import com.dyescape.bungeekube.kubernetes.discovery.KubernetesPodMapper;
import com.dyescape.bungeekube.kubernetes.discovery.KubernetesServiceDiscovery;
import com.dyescape.bungeekube.kubernetes.discovery.ServiceDiscovery;
import com.dyescape.bungeekube.kubernetes.discovery.watcher.KubernetesDiscoveryWatcher;
import com.dyescape.bungeekube.velocity.manager.ServerManager;
import com.dyescape.bungeekube.velocity.watcher.VelocityDiscoveryWatcher;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Plugin(id = "bungeekube", name = "Bungeekube", version = "1.0.0", authors = {"Dyescape"})
public class Bungeekube {

    private final Logger logger;

    @Inject
    public Bungeekube(ProxyServer server, Logger logger) {
        this.logger = logger;
        ServerManager manager = new ServerManager(server);

        KubernetesClient client = this.createKubernetesClient();
        KubernetesPodMapper mapper = new KubernetesPodMapper();

        ServiceDiscovery discovery = new KubernetesServiceDiscovery(client, mapper);
        VelocityDiscoveryWatcher watcher = new VelocityDiscoveryWatcher(manager);

        List<DiscoveredService> foundServices = discovery.Discover();

        if (!foundServices.isEmpty()) {
            logger.info("Discovered these pods:");
        } else {
            logger.warning("No pods found! Did you follow the installation manual?");
        }

        Set<String> foundPods = new HashSet<>();

        for (DiscoveredService service : foundServices) {
            logger.info(String.format("  - %s:%s (%s)", service.getHost(), service.getPort(),
                    service.getName()));

            foundPods.add(service.getHost());

            manager.addServer(service);
        }

        new KubernetesDiscoveryWatcher(client, watcher, mapper, foundPods).startListening();
    }

    private KubernetesClient createKubernetesClient() {
        logger.info("Initialising Kubernetes client. This may take a few seconds...");
        return new DefaultKubernetesClient();
    }
}
