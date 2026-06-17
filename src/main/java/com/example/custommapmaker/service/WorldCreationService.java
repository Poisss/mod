package com.example.custommapmaker.service;

import com.example.custommapmaker.data.MapData;
import com.example.custommapmaker.data.WorldCreationParameters;
import com.example.custommapmaker.world.EmptyPlatformGenerator;
import com.hypixel.hytale.server.core.entity.World;
import com.hypixel.hytale.server.core.entity.Universe;
import com.hypixel.hytale.server.core.world.WorldGenerator;
import com.hypixel.hytale.server.core.world.dimension.DimensionType;
import com.hypixel.hytale.server.core.util.Identifier;
import com.hypixel.hytale.server.core.util.math.Vec3i;
import com.hypixel.hytale.server.core.registry.MutableRegistry;
import com.hypixel.hytale.server.core.registry.RegistryKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorldCreationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldCreationService.class);

    private final Universe universe;
    private final MutableRegistry<WorldGenerator> generatorRegistry;
    private final MutableRegistry<DimensionType> dimensionRegistry;
    private final EmptyPlatformGenerator emptyPlatformGenerator;
    private final Map<String, World> createdWorlds = new ConcurrentHashMap<>();

    public WorldCreationService(Universe universe, MutableRegistry<WorldGenerator> generatorRegistry,
                                MutableRegistry<DimensionType> dimensionRegistry) {
        this.universe = universe;
        this.generatorRegistry = generatorRegistry;
        this.dimensionRegistry = dimensionRegistry;
        this.emptyPlatformGenerator = new EmptyPlatformGenerator();

        registerGenerators();
    }

    private void registerGenerators() {
        RegistryKey<WorldGenerator> generatorKey = RegistryKey.of(
            Identifier.of("custommapmaker", "empty_platform"),
            WorldGenerator.class
        );
        generatorRegistry.register(generatorKey, emptyPlatformGenerator, true);
        LOGGER.info("Registered EmptyPlatformGenerator");
    }

    public World createWorld(MapData mapData) {
        WorldCreationParameters params = mapData.worldParams();

        Map<String, Object> genSettings = convertGeneratorSettings(params);

        World world = universe.createWorld(
            params.worldName(),
            params.getDimensionTypeIdentifier(),
            params.getGeneratorIdentifier(),
            genSettings,
            params.seed()
        );
        createdWorlds.put(mapData.mapId(), world);

        LOGGER.info("Created world '{}' for map '{}'", world.getName(), mapData.mapName());
        return world;
    }

    public World loadWorld(MapData mapData) {
        String worldName = mapData.worldParams().worldName();
        Optional<World> existing = universe.getWorld(worldName);
        if (existing.isPresent()) {
            return existing.get();
        }
        return createWorld(mapData);
    }

    public Optional<World> getWorld(String mapId) {
        return Optional.ofNullable(createdWorlds.get(mapId));
    }

    public void unloadWorld(String mapId) {
        World world = createdWorlds.remove(mapId);
        if (world != null) {
            LOGGER.debug("Unloaded world for map: {}", mapId);
        }
    }

    private Map<String, Object> convertGeneratorSettings(WorldCreationParameters params) {
        Map<String, Object> settings = new HashMap<>(params.generatorSettings());
        settings.put("platform_size", params.platformSize());
        settings.put("platform_height", params.platformHeight());
        settings.put("platform_block", params.platformBlock());
        settings.put("void_world", params.voidWorld());
        return settings;
    }

    public EmptyPlatformGenerator getEmptyPlatformGenerator() {
        return emptyPlatformGenerator;
    }
}
