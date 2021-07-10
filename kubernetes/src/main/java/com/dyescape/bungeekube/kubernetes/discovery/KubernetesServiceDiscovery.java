package com.dyescape.bungeekube.kubernetes.discovery;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.Set;

public class KubernetesServiceDiscovery implements ServiceDiscovery {

    private final KubernetesClient client;
    private final KubernetesPodMapper podMapper;

    public KubernetesServiceDiscovery(KubernetesClient client, KubernetesPodMapper podMapper) {
        this.client = client;
        this.podMapper = podMapper;
    }

    @Override
    public Set<DiscoveredService> Discover() {
        PodList podList = this.getAllPods();
        return this.podMapper.getBackendServicesFromPodList(podList);
    }

    private PodList getAllPods() {
        return client.pods()
                .inAnyNamespace()
                .withLabel(KubernetesPodMapper.BASE_ANNOTATION, "true")
                .list();
    }
}
