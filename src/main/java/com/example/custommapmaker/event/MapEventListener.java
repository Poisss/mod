package com.example.custommapmaker.event;

import com.example.custommapmaker.service.CustomMapManager;
import com.hypixel.hytale.server.core.entity.PlayerEntity;
import com.hypixel.hytale.server.core.event.EventBus;
import com.hypixel.hytale.server.core.event.EventHandler;
import com.hypixel.hytale.server.core.event.EventPriority;
import com.hypixel.hytale.server.core.entity.event.PlayerJoinEvent;
import com.hypixel.hytale.server.core.entity.event.PlayerQuitEvent;
import com.hypixel.hytale.server.core.entity.event.PlayerRespawnEvent;
import com.hypixel.hytale.server.core.entity.event.EntityDamageEvent;
import com.hypixel.hytale.server.core.text.Text;
import com.hypixel.hytale.server.core.text.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MapEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapEventListener.class);
    private static final String PREFIX = TextColor.AQUA + "[MapMaker] " + TextColor.RESET;

    private final CustomMapManager mapManager;
    private final EventBus eventBus;
    private final Map<UUID, Long> joinCooldowns = new ConcurrentHashMap<>();

    public MapEventListener(CustomMapManager mapManager, EventBus eventBus) {
        this.mapManager = mapManager;
        this.eventBus = eventBus;
    }

    public void registerEvents() {
        eventBus.subscribe(PlayerJoinEvent.class, EventPriority.NORMAL, this::onPlayerJoin);
        eventBus.subscribe(PlayerQuitEvent.class, EventPriority.NORMAL, this::onPlayerQuit);
        eventBus.subscribe(PlayerRespawnEvent.class, EventPriority.NORMAL, this::onPlayerRespawn);
        LOGGER.info("MapEventListener registered");
    }

    public void unregisterEvents() {
        eventBus.unsubscribeAll(this);
        LOGGER.info("MapEventListener unregistered");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerEntity player = event.getPlayer();
        UUID playerId = player.getUuid();

        long now = System.currentTimeMillis();
        Long lastJoin = joinCooldowns.get(playerId);
        if (lastJoin != null && now - lastJoin < 5000) {
            return;
        }
        joinCooldowns.put(playerId, now);

        player.sendMessage(Text.of(
            PREFIX + TextColor.GREEN + "Welcome! Use " +
            TextColor.YELLOW + "/menu" +
            TextColor.GREEN + " to open the Map Maker."
        ));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUuid();
        joinCooldowns.remove(playerId);
        LOGGER.debug("Player {} left, cleaned up MapMaker data", event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        LOGGER.debug("Player {} respawned", event.getPlayer().getName());
    }
}
