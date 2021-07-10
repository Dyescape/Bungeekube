package com.dyescape.bungeekube.bungeecord;

import com.dyescape.bungeekube.bungeecord.watcher.BungeeDiscoveryWatcher;
import com.dyescape.bungeekube.kubernetes.discovery.KubernetesPodMapper;
import com.dyescape.bungeekube.kubernetes.discovery.KubernetesServiceDiscovery;
import com.dyescape.bungeekube.kubernetes.discovery.ServiceDiscovery;
import com.dyescape.bungeekube.kubernetes.discovery.watcher.KubernetesDiscoveryWatcher;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.Logger;

public class Bungeekube extends Plugin {

    private static final Logger LOGGER = Logger.getLogger("Bungeekube");

    private KubernetesDiscoveryWatcher watcher;

    @Override
    public void onEnable() {
        this.initialiseLogging();

        KubernetesClient client = this.createKubernetesClient();
        KubernetesPodMapper mapper = new KubernetesPodMapper();

        ServiceDiscovery discovery = new KubernetesServiceDiscovery(client, mapper);
        BungeeDiscoveryWatcher watcher = new BungeeDiscoveryWatcher(this.getProxy());

        this.watcher = new KubernetesDiscoveryWatcher(discovery, watcher);
        this.watcher.scan();
    }

    private KubernetesClient createKubernetesClient() {
        LOGGER.info("Initialising Kubernetes client. This may take a few seconds...");
        return new DefaultKubernetesClient();
    }

    private void initialiseLogging() {
        LOGGER.setParent(this.getProxy().getLogger());
    }
}
