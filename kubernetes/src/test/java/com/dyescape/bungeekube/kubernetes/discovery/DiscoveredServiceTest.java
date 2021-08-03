package com.dyescape.bungeekube.kubernetes.discovery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DiscoveredService object")
public class DiscoveredServiceTest {

    @Test
    @DisplayName("GetName - Should return provided name")
    public void testGetName_ShouldReturnProvidedName() {
        DiscoveredService service = new DiscoveredService("service", "127.0.0.1", 25565, true);

        assertEquals("service", service.getName());
    }

    @Test
    @DisplayName("GetName - Should return service plus port when null name is provided")
    public void testGetName_ShouldReturnServicePlusPortWhenNullNameIsProvided() {
        DiscoveredService service = new DiscoveredService(null, "127.0.0.1", 25565, true);

        assertEquals("127.0.0.1:25565", service.getName());
    }

    @Test
    @DisplayName("GetName - Should return service plus port when empty name is provided")
    public void testGetName_ShouldReturnServicePlusPortWhenEmptyNameIsProvided() {
        DiscoveredService service = new DiscoveredService("", "127.0.0.1", 25565, true);

        assertEquals("127.0.0.1:25565", service.getName());
    }

    @Test
    @DisplayName("GetName - Should return provided host")
    public void testGetName_ShouldReturnProvidedHost() {
        DiscoveredService service = new DiscoveredService("service", "127.0.0.1", 25565, true);

        assertEquals("127.0.0.1", service.getHost());
    }

    @Test
    @DisplayName("GetName - Should return provided port")
    public void testGetName_ShouldReturnProvidedPort() {
        DiscoveredService service = new DiscoveredService("service", "127.0.0.1", 25565, true);

        assertEquals(25565, service.getPort());
    }

    @Test
    @DisplayName("Should return passed isDefault value")
    public void testIsDefault() {
        DiscoveredService service = new DiscoveredService("service", "127.0.0.1", 25565, true);

        assertTrue(service.isDefault());
    }
}
