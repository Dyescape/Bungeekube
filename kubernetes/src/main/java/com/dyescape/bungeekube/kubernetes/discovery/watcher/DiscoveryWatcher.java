package com.dyescape.bungeekube.kubernetes.discovery.watcher;

import com.dyescape.bungeekube.kubernetes.discovery.DiscoveredService;

public interface DiscoveryWatcher {

    void onCreate(DiscoveredService service);
    void onDelete(DiscoveredService service);
}
