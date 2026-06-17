package com.hypixel.hytale.server.core.plugin;

import com.hypixel.hytale.server.core.asset.AssetManager;
import com.hypixel.hytale.server.core.command.CommandManager;
import com.hypixel.hytale.server.core.component.ComponentRegistry;
import com.hypixel.hytale.server.core.config.ConfigManager;
import com.hypixel.hytale.server.core.entity.EntitySystem;
import com.hypixel.hytale.server.core.entity.Universe;
import com.hypixel.hytale.server.core.event.EventBus;
import com.hypixel.hytale.server.core.registry.MutableRegistry;
import com.hypixel.hytale.server.core.storage.StorageManager;
import com.hypixel.hytale.server.core.world.WorldGenerator;
import com.hypixel.hytale.server.core.world.dimension.DimensionType;

public class JavaPluginInit {
    private CommandManager commandManager;
    private EventBus eventBus;
    private ComponentRegistry componentRegistry;
    private StorageManager storageManager;
    private AssetManager assetManager;
    private ConfigManager configManager;
    private Universe universe;
    private MutableRegistry<WorldGenerator> worldGeneratorRegistry;
    private MutableRegistry<DimensionType> dimensionTypeRegistry;

    public CommandManager commandManager() { return commandManager; }
    public EventBus eventBus() { return eventBus; }
    public ComponentRegistry componentRegistry() { return componentRegistry; }
    public StorageManager storageManager() { return storageManager; }
    public AssetManager assetManager() { return assetManager; }
    public ConfigManager configManager() { return configManager; }
    public Universe universe() { return universe; }
    public MutableRegistry<WorldGenerator> worldGeneratorRegistry() { return worldGeneratorRegistry; }
    public MutableRegistry<DimensionType> dimensionTypeRegistry() { return dimensionTypeRegistry; }

    public void addEntitySystem(EntitySystem system) {}
}
