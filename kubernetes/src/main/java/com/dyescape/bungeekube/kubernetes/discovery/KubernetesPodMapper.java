package com.dyescape.bungeekube.kubernetes.discovery;

import com.dyescape.bungeekube.kubernetes.discovery.exception.IllegalPortSetupException;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.PodList;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class KubernetesPodMapper {

    public static final String BASE_ANNOTATION = "bungeekube.dyescape.com/enabled";
    public static final String DEFAULT_ANNOTATION = "bungeekube.dyescape.com/default";

    private static final Logger LOGGER = Logger.getLogger("Bungeekube");

    public int getBackendPortFromPod(Pod pod) {
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

    public Set<DiscoveredService> getBackendServicesFromPodList(PodList podList) {
        return podList.getItems().stream()
                .filter(this::isReady)
                .map(this::tryGetPodAsDiscoveredService)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private boolean isReady(Pod pod) {
        for (PodCondition condition : pod.getStatus().getConditions()) {
            if (!condition.getType().equalsIgnoreCase("ready")) {
                if (!condition.getStatus().equalsIgnoreCase("true")) {
                    return false;
                }
            }
        }

        return true;
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
                getBackendPortFromPod(pod), pod.getMetadata().getLabels() != null &&
                pod.getMetadata().getLabels().containsKey(DEFAULT_ANNOTATION));
    }
}
