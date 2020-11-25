package com.dyescape.bungeekube.kubernetes.discovery;

import java.util.List;

public interface ServiceDiscovery {

    List<DiscoveredService> Discover();
}
