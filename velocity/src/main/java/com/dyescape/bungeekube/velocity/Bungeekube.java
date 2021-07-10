package com.dyescape.bungeekube.velocity;

import com.dyescape.bungeekube.kubernetes.discovery.KubernetesPodMapper;
import com.dyescape.bungeekube.kubernetes.discovery.KubernetesServiceDiscovery;
import com.dyescape.bungeekube.kubernetes.discovery.ServiceDiscovery;
import com.dyescape.bungeekube.kubernetes.discovery.watcher.KubernetesDiscoveryWatcher;
import com.dyescape.bungeekube.velocity.watcher.VelocityDiscoveryWatcher;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Plugin(id = "bungeekube", name = "Bungeekube", version = "1.0.0", authors = {"Dyescape"})
public class Bungeekube {

    private final ProxyServer server;
    private final Logger logger;

    private final KubernetesDiscoveryWatcher watcher;

    @Inject
    public Bungeekube(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        KubernetesClient client = this.createKubernetesClient();
        KubernetesPodMapper mapper = new KubernetesPodMapper();

        ServiceDiscovery discovery = new KubernetesServiceDiscovery(client, mapper);
        VelocityDiscoveryWatcher watcher = new VelocityDiscoveryWatcher(server);

        this.watcher = new KubernetesDiscoveryWatcher(discovery, watcher);
        this.watcher.scan();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.server.getScheduler().buildTask(this, this.watcher::scan)
                .delay(10L, TimeUnit.SECONDS)
                .repeat(10L, TimeUnit.SECONDS)
                .schedule();
    }

    private KubernetesClient createKubernetesClient() {
        logger.info("Initialising Kubernetes client. This may take a few seconds...");
        return new DefaultKubernetesClient();
    }
}
