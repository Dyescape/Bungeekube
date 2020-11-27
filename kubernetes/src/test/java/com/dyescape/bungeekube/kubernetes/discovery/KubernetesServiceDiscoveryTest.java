package com.dyescape.bungeekube.kubernetes.discovery;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.dsl.FilterWatchListMultiDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KubernetesServiceDiscoveryTest {

    private KubernetesClient client;
    private KubernetesServiceDiscovery discovery;
    private MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> mixedOperationMock;
    private FilterWatchListMultiDeletable<Pod, PodList, Boolean, Watch> filter;

    @BeforeEach
    protected void setup() {
        client = mock(KubernetesClient.class);
        this.discovery = new KubernetesServiceDiscovery(this.client);

        mixedOperationMock = mock(MixedOperation.class);
        when(this.client.pods()).thenReturn(this.mixedOperationMock);

        filter = mock(FilterWatchListMultiDeletable.class);
        when(this.mixedOperationMock.inAnyNamespace()).thenReturn(this.filter);
    }

    @Test
    @DisplayName("Discover - No annotated services")
    public void testDiscover_NoAnnotatedServices() {

        Pod pod = this.createEmptyPod();
        this.initialisePodListMock(pod);

        assertTrue(this.discovery.Discover().isEmpty());
    }

    @Test
    @DisplayName("Discover - Wrong annotated service")
    public void testDiscover_WrongAnnotatedServer() {

        Pod pod = this.createEmptyPod();
        pod.getMetadata().getAnnotations().put("foo", "bar");
        this.initialisePodListMock(pod);

        assertTrue(this.discovery.Discover().isEmpty());
    }

    @Test
    @DisplayName("Discover - Annotated services with not enough ports")
    public void testDiscover_AnnotatedNotEnoughPorts() {

        Pod pod = this.createEmptyPod();
        pod.getMetadata().getAnnotations().put(KubernetesServiceDiscovery.BASE_ANNOTATION, "true");
        this.initialisePodListMock(pod);

        assertTrue(this.discovery.Discover().isEmpty());
    }

    @Test
    @DisplayName("Discover - Annotated services with too many ports")
    public void testDiscover_AnnotatedTooManyPorts() {

        Pod pod = this.createEmptyPod();
        pod.getMetadata().getAnnotations().put(KubernetesServiceDiscovery.BASE_ANNOTATION, "true");
        ContainerPort port = new ContainerPort();
        port.setContainerPort(25565);

        List<ContainerPort> ports = new ArrayList<>();
        ports.add(port);
        ports.add(port);

        pod.getSpec().getContainers().get(0).setPorts(ports);
        this.initialisePodListMock(pod);

        assertTrue(this.discovery.Discover().isEmpty());
    }

    @Test
    @DisplayName("Discover - Annotated services with no applicable port")
    public void testDiscover_AnnotatedNoApplicablePort() {

        Pod pod = this.createEmptyPod();
        pod.getMetadata().getAnnotations().put(KubernetesServiceDiscovery.BASE_ANNOTATION, "true");
        ContainerPort port = new ContainerPort();
        port.setContainerPort(1234);
        port.setName("my-port");

        List<ContainerPort> ports = new ArrayList<>();
        ports.add(port);

        pod.getSpec().getContainers().get(0).setPorts(ports);
        this.initialisePodListMock(pod);

        assertTrue(this.discovery.Discover().isEmpty());
    }

    @Test
    @DisplayName("Discover - Annotated services with correct named port")
    public void testDiscover_AnnotatedCorrectNamedPort() {

        Pod pod = this.createEmptyPod();
        pod.getMetadata().getAnnotations().put(KubernetesServiceDiscovery.BASE_ANNOTATION, "true");
        ContainerPort port = new ContainerPort();
        port.setContainerPort(1234);
        port.setName("minecraft");

        List<ContainerPort> ports = new ArrayList<>();
        ports.add(port);

        pod.getSpec().getContainers().get(0).setPorts(ports);
        this.initialisePodListMock(pod);

        assertFalse(this.discovery.Discover().isEmpty());
    }

    @Test
    @DisplayName("Discover - Annotated services with correct port number")
    public void testDiscover_AnnotatedCorrectPortNumber() {

        Pod pod = this.createEmptyPod();
        pod.getMetadata().getAnnotations().put(KubernetesServiceDiscovery.BASE_ANNOTATION, "true");
        ContainerPort port = new ContainerPort();
        port.setContainerPort(25565);
        port.setName("something");

        List<ContainerPort> ports = new ArrayList<>();
        ports.add(port);

        pod.getSpec().getContainers().get(0).setPorts(ports);
        this.initialisePodListMock(pod);

        assertFalse(this.discovery.Discover().isEmpty());
    }

    private Pod createEmptyPod() {
        Pod pod = new Pod();
        pod.setMetadata(new ObjectMeta());
        pod.getMetadata().setAnnotations(new HashMap<>());
        pod.getMetadata().setName("test-pod");
        pod.getMetadata().setNamespace("test-namespace");

        PodSpec spec = new PodSpec();
        Container container = new Container();
        spec.setContainers(List.of(container));
        pod.setSpec(spec);

        PodStatus status = new PodStatus();
        status.setPodIP("127.0.0.1");
        pod.setStatus(status);

        return pod;
    }

    private void initialisePodListMock(Pod pod) {
        PodList podList = new PodList("", List.of(pod), "", null);
        when(this.filter.list()).thenReturn(podList);
    }
}
