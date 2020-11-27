package com.dyescape.bungeekube.kubernetes.discovery;

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
}
