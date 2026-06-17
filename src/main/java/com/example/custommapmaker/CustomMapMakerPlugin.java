package com.example.custommapmaker;

import com.example.custommapmaker.command.MapCommandHandler;
import com.example.custommapmaker.data.MapData;
import com.example.custommapmaker.event.MapEventListener;
import com.example.custommapmaker.service.CustomMapManager;
import com.example.custommapmaker.service.WorldCreationService;
import com.example.custommapmaker.ui.MapUIHandler;
import com.example.custommapmaker.world.EmptyPlatformGenerator;
import com.hypixel.hytale.plugin.early.EarlyPlugin;
import com.hypixel.hytale.plugin.java.JavaPlugin;
import com.hypixel.hytale.plugin.java.JavaPluginInit;
import com.hypixel.hytale.server.core.component.Component;
import com.hypixel.hytale.server.core.component.ComponentRegistry;
import com.hypixel.hytale.server.core.entity.Universe;
import com.hypixel.hytale.server.core.event.EventBus;
import com.hypixel.hytale.server.core.registry.MutableRegistry;
import com.hypixel.hytale.server.core.util.Identifier;
import com.hypixel.hytale.server.core.world.WorldGenerator;
import com.hypixel.hytale.server.core.world.dimension.DimensionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class CustomMapMakerPlugin extends JavaPlugin implements EarlyPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomMapMakerPlugin.class);

    public static final String MOD_ID = "custommapmaker";

    private CustomMapManager mapManager;
    private WorldCreationService worldCreationService;
    private MapCommandHandler commandHandler;
    private MapEventListener eventListener;
    private MapUIHandler uiHandler;
    private PluginConfig config;

    @Override
    public void setup(JavaPluginInit init) {
        LOGGER.info("Setting up CustomMapMaker plugin...");

        this.config = new PluginConfig();
        this.config.load(init);

        ComponentRegistry componentRegistry = init.componentRegistry();
        registerComponents(componentRegistry);

        this.mapManager = new CustomMapManager(
            init.assetManager(),
            init.storageManager(),
            init.eventBus(),
            this
        );

        this.worldCreationService = new WorldCreationService(
            init.universe(),
            init.worldGeneratorRegistry(),
            init.dimensionTypeRegistry()
        );

        this.mapManager.initialize(worldCreationService);

        this.uiHandler = new MapUIHandler(this, mapManager, init.eventBus());
        this.commandHandler = new MapCommandHandler(mapManager, init.commandManager(), this);
        this.eventListener = new MapEventListener(mapManager, init.eventBus());

        LOGGER.info("CustomMapMaker plugin setup complete!");
    }

    @Override
    public void start() {
        LOGGER.info("Starting CustomMapMaker plugin...");

        commandHandler.registerCommands();
        eventListener.registerEvents();
        uiHandler.registerEvents();

        LOGGER.info("CustomMapMaker plugin started! {} maps loaded.", mapManager.getAllMaps().size());
    }

    @Override
    public void shutdown() {
        LOGGER.info("Shutting down CustomMapMaker plugin...");

        uiHandler.unregisterEvents();
        eventListener.unregisterEvents();
        mapManager.shutdown();

        LOGGER.info("CustomMapMaker plugin shutdown complete!");
    }

    private void registerComponents(ComponentRegistry registry) {
        registry.register(MapData.class);
        LOGGER.debug("Registered ECS components");
    }

    public CustomMapManager getMapManager() {
        return mapManager;
    }

    public WorldCreationService getWorldCreationService() {
        return worldCreationService;
    }

    public MapUIHandler getMapUI() {
        return uiHandler;
    }

    public PluginConfig getPluginConfig() {
        return config;
    }

    public static class PluginConfig {
        public int maxMapsPerPlayer = 50;
        public int maxMapSize = 512;
        public boolean allowCrossMapTeleport = true;
        public boolean enableMapSharing = true;
        public int autoSaveIntervalMinutes = 5;

        public void load(JavaPluginInit init) {
            try {
                var configValue = init.configManager().load(MOD_ID);
                if (configValue != null) {
                    this.maxMapsPerPlayer = configValue.getInt("max_maps_per_player", 50);
                    this.maxMapSize = configValue.getInt("max_map_size", 512);
                    this.allowCrossMapTeleport = configValue.getBoolean("allow_cross_map_teleport", true);
                    this.enableMapSharing = configValue.getBoolean("enable_map_sharing", true);
                    this.autoSaveIntervalMinutes = configValue.getInt("auto_save_interval_minutes", 5);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to load config, using defaults", e);
            }
        }
    }
}
