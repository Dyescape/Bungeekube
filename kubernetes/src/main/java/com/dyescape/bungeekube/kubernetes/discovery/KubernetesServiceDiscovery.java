package com.dyescape.bungeekube.kubernetes.discovery;

import com.dyescape.bungeekube.kubernetes.discovery.exception.IllegalPortSetupException;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class KubernetesServiceDiscovery implements ServiceDiscovery {

    public static final String BASE_ANNOTATION = "bungeekube.dyescape.com/enabled";

    private static final Logger LOGGER = Logger.getLogger("Bungeekube");

    private KubernetesClient client;

    @Override
    public List<DiscoveredService> Discover() {
        if (this.client == null) {
            LOGGER.info("Initialising Kubernetes client. This may take a few seconds...");
            this.initialiseClient();
        }

        PodList podList = this.getAllPods();
        return this.getBackendServicesFromPodList(podList);
    }

    private void initialiseClient() {
        this.client = this.createKubernetesClient();
    }

    private int getBackendPortFromPod(Pod pod) {
        List<Integer> ports = pod.getSpec().getContainers().stream()
                .flatMap(c -> c.getPorts().stream())
                .map(this::getApplicableBackendPortFromContainerPort)
                .filter(p -> p != 0)
                .collect(Collectors.toList());

        if (ports.size() != 1) {
            throw new IllegalPortSetupException(String.format("Could not find applicable container ports for pod %s " +
                    "in namespace %s. Make sure the pod has either a port named 'minecraft', or a container port on " +
                    "25565.", pod.getMetadata().getName(), pod.getMetadata().getNamespace()));
        }

        return ports.get(0);
    }

    private int getApplicableBackendPortFromContainerPort(ContainerPort port) {

        // It's probably this port if the name is Minecraft
        if (port.getName() != null && port.getName().equalsIgnoreCase("minecraft")) {
            return port.getContainerPort();
        }

        // It probably this port also if the container port is 25565 (Minecraft's default)
        if (port.getContainerPort() == 25565) {
            return port.getContainerPort();
        }

        return 0;
    }

    private List<DiscoveredService> getBackendServicesFromPodList(PodList podList) {
        return podList.getItems().stream()
                .filter(this::isBackendServer)
                .map(this::tryGetPodAsDiscoveredService)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private DiscoveredService tryGetPodAsDiscoveredService(Pod pod) {
        try {
            return this.getPodAsDiscoveredService(pod);
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
            return null;
        }
    }

    private DiscoveredService getPodAsDiscoveredService(Pod pod) {
        return new DiscoveredService(pod.getMetadata().getName(), pod.getStatus().getPodIP(),
                getBackendPortFromPod(pod));
    }

    private PodList getAllPods() {
        return client.pods()
                .inAnyNamespace()
                .list();
    }

    private KubernetesClient createKubernetesClient() {
        return new DefaultKubernetesClient();
    }

    private boolean isBackendServer(Pod pod) {
        Map<String, String> annotations = pod.getMetadata().getAnnotations();
        if (annotations == null || annotations.isEmpty()) {
            this.logPodMissingAnnotation(pod);
            return false;
        }

        boolean hasAnnotation = annotations.containsKey(BASE_ANNOTATION) &&
                annotations.get(BASE_ANNOTATION).equalsIgnoreCase("true");

        if (!hasAnnotation) {
            this.logPodMissingAnnotation(pod);
        }

        return hasAnnotation;
    }

    private void logPodMissingAnnotation(Pod pod) {
        LOGGER.fine(String.format("Ignoring pod %s in namespace %s because it has does not have the %s: 'true'" +
                        "annotation", pod.getMetadata().getName(), pod.getMetadata().getNamespace(), BASE_ANNOTATION));
    }
}
