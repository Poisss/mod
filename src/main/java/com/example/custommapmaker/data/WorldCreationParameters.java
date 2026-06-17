package com.example.custommapmaker.data;

import com.google.gson.annotations.SerializedName;
import com.hypixel.hytale.server.core.util.Identifier;
import com.hypixel.hytale.server.core.util.math.Vec3i;

import java.util.*;

public record WorldCreationParameters(
    @SerializedName("world_name") String worldName,
    @SerializedName("generator_type") String generatorType,
    @SerializedName("generator_settings") Map<String, Object> generatorSettings,
    @SerializedName("dimension_type") String dimensionType,
    @SerializedName("seed") long seed,
    @SerializedName("spawn_position") Vec3i spawnPosition,
    @SerializedName("platform_size") int platformSize,
    @SerializedName("platform_height") int platformHeight,
    @SerializedName("platform_block") String platformBlock,
    @SerializedName("void_world") boolean voidWorld,
    @SerializedName("enable_weather") boolean enableWeather,
    @SerializedName("day_cycle") String dayCycle,
    @SerializedName("difficulty") String difficulty,
    @SerializedName("game_mode") String gameMode,
    @SerializedName("metadata") Map<String, Object> metadata
) {
    public static WorldCreationParameters defaultParams() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("platform_size", 10);
        settings.put("platform_height", 64);
        settings.put("platform_block", "hytale:stone");
        
        return new WorldCreationParameters(
            "map_" + UUID.randomUUID().toString().substring(0, 8),
            "custommapmaker:empty_platform",
            settings,
            "hytale:overworld",
            new Random().nextLong(),
            new Vec3i(0, 65, 0),
            10,
            64,
            "hytale:stone",
            true,
            false,
            "fixed_noon",
            "peaceful",
            "creative",
            new HashMap<>()
        );
    }
    
    public static WorldCreationParameters fromTemplate(String templateName) {
        return switch (templateName.toLowerCase()) {
            case "spawn_arena" -> spawnArenaTemplate();
            case "parkour_course" -> parkourCourseTemplate();
            case "build_plate" -> buildPlateTemplate();
            default -> defaultParams();
        };
    }
    
    private static WorldCreationParameters spawnArenaTemplate() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("platform_size", 50);
        settings.put("platform_height", 64);
        settings.put("platform_block", "hytale:stone_bricks");
        settings.put("add_walls", true);
        settings.put("add_lighting", true);
        settings.put("center_spawn", true);
        
        return new WorldCreationParameters(
            "spawn_arena_" + UUID.randomUUID().toString().substring(0, 8),
            "custommapmaker:empty_platform",
            settings,
            "hytale:overworld",
            12345L,
            new Vec3i(0, 65, 0),
            50,
            64,
            "hytale:stone_bricks",
            true,
            false,
            "fixed_noon",
            "peaceful",
            "creative",
            Map.of("template", "spawn_arena", "description", "A spawn arena with walls and lighting")
        );
    }
    
    private static WorldCreationParameters parkourCourseTemplate() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("platform_size", 100);
        settings.put("platform_height", 64);
        settings.put("platform_block", "hytale:quartz_block");
        settings.put("generate_parkour", true);
        settings.put("checkpoint_interval", 10);
        
        return new WorldCreationParameters(
            "parkour_" + UUID.randomUUID().toString().substring(0, 8),
            "custommapmaker:empty_platform",
            settings,
            "hytale:overworld",
            54321L,
            new Vec3i(0, 65, 0),
            100,
            64,
            "hytale:quartz_block",
            true,
            false,
            "fixed_noon",
            "peaceful",
            "adventure",
            Map.of("template", "parkour_course", "description", "A parkour course with checkpoints")
        );
    }
    
    private static WorldCreationParameters buildPlateTemplate() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("platform_size", 64);
        settings.put("platform_height", 64);
        settings.put("platform_block", "hytale:grass_block");
        settings.put("grid_lines", true);
        settings.put("grid_spacing", 8);
        
        return new WorldCreationParameters(
            "build_plate_" + UUID.randomUUID().toString().substring(0, 8),
            "custommapmaker:empty_platform",
            settings,
            "hytale:overworld",
            99999L,
            new Vec3i(0, 65, 0),
            64,
            64,
            "hytale:grass_block",
            true,
            true,
            "normal",
            "peaceful",
            "creative",
            Map.of("template", "build_plate", "description", "A flat build plate with grid lines")
        );
    }
    
    public WorldCreationParameters withWorldName(String name) {
        return new WorldCreationParameters(name, generatorType, generatorSettings, dimensionType, 
            seed, spawnPosition, platformSize, platformHeight, platformBlock, voidWorld, 
            enableWeather, dayCycle, difficulty, gameMode, metadata);
    }
    
    public WorldCreationParameters withPlatformSize(int size) {
        Map<String, Object> newSettings = new HashMap<>(generatorSettings);
        newSettings.put("platform_size", size);
        return new WorldCreationParameters(worldName, generatorType, newSettings, dimensionType, 
            seed, spawnPosition, size, platformHeight, platformBlock, voidWorld, 
            enableWeather, dayCycle, difficulty, gameMode, metadata);
    }
    
    public WorldCreationParameters withPlatformBlock(String block) {
        Map<String, Object> newSettings = new HashMap<>(generatorSettings);
        newSettings.put("platform_block", block);
        return new WorldCreationParameters(worldName, generatorType, newSettings, dimensionType, 
            seed, spawnPosition, platformSize, platformHeight, block, voidWorld, 
            enableWeather, dayCycle, difficulty, gameMode, metadata);
    }
    
    public WorldCreationParameters withSeed(long newSeed) {
        return new WorldCreationParameters(worldName, generatorType, generatorSettings, dimensionType, 
            newSeed, spawnPosition, platformSize, platformHeight, platformBlock, voidWorld, 
            enableWeather, dayCycle, difficulty, gameMode, metadata);
    }
    
    public WorldCreationParameters withSpawnPosition(Vec3i pos) {
        return new WorldCreationParameters(worldName, generatorType, generatorSettings, dimensionType, 
            seed, pos, platformSize, platformHeight, platformBlock, voidWorld, 
            enableWeather, dayCycle, difficulty, gameMode, metadata);
    }
    
    public Identifier getGeneratorIdentifier() {
        return Identifier.parse(generatorType);
    }
    
    public Identifier getDimensionTypeIdentifier() {
        return Identifier.parse(dimensionType);
    }
    
    public Identifier getPlatformBlockIdentifier() {
        return Identifier.parse(platformBlock);
    }
}