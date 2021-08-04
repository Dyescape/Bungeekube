package com.dyescape.bungeekube.velocity.balancer;

import com.dyescape.bungeekube.api.registry.ServerRegistry;
import com.dyescape.bungeekube.kubernetes.discovery.DiscoveredService;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LoadBalancerListener {

    private final ProxyServer proxyServer;
    private final ServerRegistry serverRegistry;
    private final Logger logger;

    public LoadBalancerListener(ProxyServer proxyServer, ServerRegistry serverRegistry, Logger logger) {
        this.proxyServer = proxyServer;
        this.serverRegistry = serverRegistry;
        this.logger = logger;
    }

    @Subscribe(order = PostOrder.LATE)
    public void onJoin(PlayerChooseInitialServerEvent e) {
        DiscoveredService random = random(this.serverRegistry.getRegisteredDefaultServers());
        if (random == null) {
            e.setInitialServer(null);
            return;
        }
        RegisteredServer server = getApplicableServer();

        logger.info(String.format("Load balancing %s to %s", e.getPlayer().getUsername(), random.getName()));

        e.setInitialServer(server);
    }

    private RegisteredServer getApplicableServer() {
        return this.serverRegistry.getRegisteredDefaultServers().stream()
                .map(s -> this.proxyServer.getServer(s.getName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparingInt(one -> one.getPlayersConnected().size()))
                .collect(Collectors.toList()).get(0);
    }

    private <T> T random(Collection<T> coll) {
        if (coll.size() == 0) {
            return null;
        } else if (coll.size() == 1) {
            return coll.iterator().next();
        } else {
            List<T> list;
            if (coll instanceof List) {
                list = (List<T>)coll;
            } else {
                list = new ArrayList<>(coll);
            }

            int index = ThreadLocalRandom.current().nextInt(list.size());
            return list.get(index);
        }
    }
}
