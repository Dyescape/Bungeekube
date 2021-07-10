package com.dyescape.bungeekube.kubernetes.discovery.watcher;

import com.dyescape.bungeekube.kubernetes.discovery.DiscoveredService;
import com.dyescape.bungeekube.kubernetes.discovery.ServiceDiscovery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class KubernetesDiscoveryWatcher {

    private static final Logger LOGGER = Logger.getLogger("Bungeekube");

    private final Set<DiscoveredService> currentServices = new HashSet<>();

    private final ServiceDiscovery discovery;
    private final DiscoveryWatcher watcher;

    public KubernetesDiscoveryWatcher(ServiceDiscovery discovery, DiscoveryWatcher watcher) {
        this.discovery = discovery;
        this.watcher = watcher;
    }

    public void scan() {

        long start = System.currentTimeMillis();
        LOGGER.info("Performing discovery...");

        Set<DiscoveredService> foundServices = discovery.Discover();

        // Comparison to find deleted pods
        Iterator<DiscoveredService> iterator = this.currentServices.iterator();
        DiscoveredService currentService;
        while (iterator.hasNext()) {
            currentService = iterator.next();

            if (!foundServices.contains(currentService)) {
                iterator.remove();
                this.handleDeletedService(currentService);
                this.watcher.onDelete(currentService);
            }
        }

        // Find newly created services
        List<DiscoveredService> addedServices = new ArrayList<>();
        for (DiscoveredService discoveredService : foundServices) {
            if (!this.currentServices.contains(discoveredService)) {
                addedServices.add(discoveredService);
                this.handleCreatedService(discoveredService);
            }
        }

        // Add them after iteration
        this.currentServices.addAll(addedServices);

        LOGGER.info(String.format("Finished discovering, took %s seconds", (System.currentTimeMillis() - start) / 1000));
    }

    private void handleDeletedService(DiscoveredService service) {
        LOGGER.info(String.format("Pod %s:%s (%s) was deleted, removing...", service.getHost(), service.getPort(),
                service.getName()));
        this.watcher.onDelete(service);
    }

    private void handleCreatedService(DiscoveredService service) {
        LOGGER.info(String.format("Pod %s:%s (%s) was created, adding...", service.getHost(), service.getPort(),
                service.getName()));
        this.watcher.onCreate(service);
    }
}
