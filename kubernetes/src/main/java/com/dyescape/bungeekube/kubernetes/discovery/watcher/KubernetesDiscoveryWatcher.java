package com.dyescape.bungeekube.kubernetes.discovery.watcher;

import com.dyescape.bungeekube.kubernetes.discovery.DiscoveredService;
import com.dyescape.bungeekube.kubernetes.discovery.KubernetesPodMapper;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;

import java.util.Set;
import java.util.logging.Logger;

public class KubernetesDiscoveryWatcher implements Watcher<Pod> {

    public static final String BASE_ANNOTATION = "bungeekube.dyescape.com/enabled";

    private static final Logger LOGGER = Logger.getLogger("Bungeekube");

    private final KubernetesClient client;
    private final DiscoveryWatcher watcher;
    private final KubernetesPodMapper mapper;
    private final Set<String> initiallyFound;

    public KubernetesDiscoveryWatcher(KubernetesClient client, DiscoveryWatcher watcher, KubernetesPodMapper mapper,
                                      Set<String> initiallyFound) {
        this.client = client;
        this.watcher = watcher;
        this.mapper = mapper;
        this.initiallyFound = initiallyFound;
    }

    public void startListening() {
        this.client.pods()
                .withLabel(BASE_ANNOTATION, "true")
                .watch(this);
    }

    @Override
    public void eventReceived(Action action, Pod pod) {
        switch (action) {
            case ADDED:
                this.handleAddedPod(pod);
                return;
            case DELETED:
                this.handleDeletedPod(pod);
                return;
            case MODIFIED:
                this.handleModifiedPod(pod);
        }
    }

    @Override
    public void onClose(KubernetesClientException e) { }

    private void handleAddedPod(Pod pod) {

        String name = pod.getMetadata().getName();
        String ip = pod.getStatus().getPodIP();

        if (this.initiallyFound.contains(ip)) {
            // Dirty work-around to not immediately re-process the pods we already found during startup
            this.initiallyFound.remove(ip);
            return;
        }

        LOGGER.info(String.format("Pod %s was created, waiting until ready...", name));
    }

    private void handleDeletedPod(Pod pod) {

        String name = pod.getMetadata().getName();
        String ip = pod.getStatus().getPodIP();
        int port = mapper.getBackendPortFromPod(pod);

        LOGGER.info(String.format("Pod %s:%s (%s) was deleted, removing...", ip, port, name));
        this.watcher.onDelete(new DiscoveredService(name, ip, port));
    }

    private void handleModifiedPod(Pod pod) {

        if (!pod.getStatus().getPhase().equalsIgnoreCase("running")) return;

        for (PodCondition condition : pod.getStatus().getConditions()) {
            if (!condition.getType().equalsIgnoreCase("ready")) {
                if (!condition.getStatus().equalsIgnoreCase("true")) {
                    return;
                }
            }
        }

        String name = pod.getMetadata().getName();
        String ip = pod.getStatus().getPodIP();
        int port = mapper.getBackendPortFromPod(pod);

        LOGGER.info(String.format("Pod %s:%s (%s) is ready, adding...", ip, port, name));
        this.watcher.onCreate(new DiscoveredService(name, ip, port));
    }
}
