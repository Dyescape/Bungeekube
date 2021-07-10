package com.dyescape.bungeekube.kubernetes.discovery;

import java.util.Set;

public interface ServiceDiscovery {

    Set<DiscoveredService> Discover();
}
