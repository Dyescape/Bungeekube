package com.dyescape.bungeekube.kubernetes.discovery;

import java.util.Objects;

public class DiscoveredService {

    private final String name;
    private final String host;
    private final int port;

    public DiscoveredService(String name, String host, int port) {
        this.name = name != null && !name.isEmpty() ? name : String.format("%s:%s", host, port);
        this.host = host;
        this.port = port;
    }

    public String getName() {
        return this.name;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscoveredService that = (DiscoveredService) o;
        return port == that.port && Objects.equals(name, that.name) && Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, host, port);
    }
}
