package com.dyescape.bungeekube.kubernetes.discovery;

import java.util.Objects;

public class DiscoveredService {

    private final String name;
    private final String host;
    private final int port;
    private final boolean isDefault;

    public DiscoveredService(String name, String host, int port, boolean isDefault) {
        this.name = name != null && !name.isEmpty() ? name : String.format("%s:%s", host, port);
        this.host = host;
        this.port = port;
        this.isDefault = isDefault;
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

    public boolean isDefault() {
        return this.isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscoveredService that = (DiscoveredService) o;
        return port == that.port && isDefault == that.isDefault && Objects.equals(name, that.name) && Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, host, port, isDefault);
    }
}
