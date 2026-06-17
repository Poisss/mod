package com.example.custommapmaker.service;

import com.example.custommapmaker.CustomMapMakerPlugin;
import com.example.custommapmaker.data.MapData;
import com.example.custommapmaker.data.WorldCreationParameters;
import com.example.custommapmaker.util.MapExportImport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.server.core.asset.AssetManager;
import com.hypixel.hytale.server.core.entity.PlayerEntity;
import com.hypixel.hytale.server.core.entity.World;
import com.hypixel.hytale.server.core.event.EventBus;
import com.hypixel.hytale.server.core.storage.StorageManager;
import com.hypixel.hytale.server.core.util.Identifier;
import com.hypixel.hytale.server.core.util.math.Vec3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomMapManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomMapManager.class);

    private final AssetManager assetManager;
    private final StorageManager storageManager;
    private final EventBus eventBus;
    private final CustomMapMakerPlugin plugin;

    private final ConcurrentHashMap<String, MapData> maps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, World> loadedWorlds = new ConcurrentHashMap<>();
    private final Gson gson;
    private final Path mapsDirectory;
    private final Path exportsDirectory;
    private final ScheduledExecutorService autoSaveExecutor;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private WorldCreationService worldCreationService;
    private MapExportImport exportImport;

    public CustomMapManager(AssetManager assetManager, StorageManager storageManager,
                            EventBus eventBus, CustomMapMakerPlugin plugin) {
        this.assetManager = assetManager;
        this.storageManager = storageManager;
        this.eventBus = eventBus;
        this.plugin = plugin;

        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(MapData.MapPermission.class, new MapPermissionAdapter())
            .registerTypeAdapter(Vec3i.class, new Vec3iAdapter())
            .create();

        this.mapsDirectory = storageManager.getDataDirectory()
            .resolve("CustomMapMaker").resolve("maps");
        this.exportsDirectory = storageManager.getDataDirectory()
            .resolve("CustomMapMaker").resolve("exports");
        this.autoSaveExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "CustomMapMaker-AutoSave");
            t.setDaemon(true);
            return t;
        });
    }

    public void initialize(WorldCreationService worldCreationService) {
        this.worldCreationService = worldCreationService;
        this.exportImport = new MapExportImport(this, gson, exportsDirectory);

        try {
            Files.createDirectories(mapsDirectory);
            Files.createDirectories(exportsDirectory);
        } catch (IOException e) {
            LOGGER.error("Failed to create map directories", e);
        }

        loadAllMaps();
        createDefaultMapsIfNeeded();
        startAutoSave();

        initialized.set(true);
        LOGGER.info("CustomMapManager initialized with {} maps", maps.size());
    }

    private void startAutoSave() {
        autoSaveExecutor.scheduleAtFixedRate(
            this::saveAllMaps,
            plugin.getPluginConfig().autoSaveIntervalMinutes,
            plugin.getPluginConfig().autoSaveIntervalMinutes,
            TimeUnit.MINUTES
        );
    }

    public void loadAllMaps() {
        LOGGER.info("Loading maps from {}", mapsDirectory);

        try (Stream<Path> stream = Files.list(mapsDirectory)) {
            stream.filter(path -> path.toString().endsWith(".json"))
                  .forEach(this::loadMapFromFile);
        } catch (IOException e) {
            LOGGER.error("Failed to list map files", e);
        }

        LOGGER.info("Loaded {} maps", maps.size());
    }

    private void loadMapFromFile(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            MapData mapData = gson.fromJson(reader, MapData.class);
            if (mapData != null) {
                maps.put(mapData.mapId(), mapData);
                LOGGER.debug("Loaded map: {} ({})", mapData.mapName(), mapData.mapId());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load map from {}", path, e);
        }
    }

    public void saveAllMaps() {
        maps.values().forEach(this::saveMap);
    }

    public void saveMap(MapData mapData) {
        Path file = mapsDirectory.resolve(mapData.mapId() + ".json");
        try (Writer writer = Files.newBufferedWriter(file)) {
            gson.toJson(mapData, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save map {}", mapData.mapId(), e);
        }
    }

    public void deleteMap(String mapId) {
        MapData map = maps.remove(mapId);
        if (map != null) {
            Path file = mapsDirectory.resolve(mapId + ".json");
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                LOGGER.error("Failed to delete map file {}", mapId, e);
            }

            World world = loadedWorlds.remove(mapId);
            if (world != null) {
                LOGGER.debug("Unloaded world for deleted map: {}", mapId);
            }

            LOGGER.info("Deleted map: {}", mapId);
        }
    }

    public MapData createMap(String name, String creator, UUID creatorUuid,
                             WorldCreationParameters params) {
        MapData mapData = MapData.builder()
            .mapName(name)
            .creator(creator)
            .creatorUuid(creatorUuid)
            .worldParams(params.withWorldName(
                "map_" + UUID.randomUUID().toString().substring(0, 8)))
            .isDefault(false)
            .build();

        maps.put(mapData.mapId(), mapData);
        saveMap(mapData);

        CompletableFuture.runAsync(() -> {
            try {
                World world = worldCreationService.createWorld(mapData);
                loadedWorlds.put(mapData.mapId(), world);
                LOGGER.info("Created world for map: {}", mapData.mapName());
            } catch (Exception e) {
                LOGGER.error("Failed to create world for map {}", mapData.mapId(), e);
            }
        });

        return mapData;
    }

    private void createDefaultMapsIfNeeded() {
        boolean hasDefaults = maps.values().stream().anyMatch(MapData::isDefault);
        if (!hasDefaults) {
            LOGGER.info("Creating default maps...");
            createDefaultMap("spawn_arena", "Spawn Arena",
                "A classic spawn arena with walls and lighting");
            createDefaultMap("parkour_course", "Parkour Course",
                "A challenging parkour course with checkpoints");
            createDefaultMap("build_plate", "Build Plate",
                "A flat grass plate with grid lines for building");
        }
    }

    public MapData createDefaultMap(String templateName, String displayName, String description) {
        WorldCreationParameters params = WorldCreationParameters.fromTemplate(templateName)
            .withWorldName(templateName + "_" + UUID.randomUUID().toString().substring(0, 8));

        MapData mapData = MapData.builder()
            .mapId(templateName)
            .mapName(displayName)
            .creator("System")
            .creatorUuid(UUID.fromString("00000000-0000-0000-0000-000000000000"))
            .worldParams(params)
            .isDefault(true)
            .description(description)
            .tags(Arrays.asList("default", templateName))
            .build();

        maps.put(mapData.mapId(), mapData);
        saveMap(mapData);

        CompletableFuture.runAsync(() -> {
            try {
                World world = worldCreationService.createWorld(mapData);
                loadedWorlds.put(mapData.mapId(), world);
                LOGGER.info("Created default map world: {}", mapData.mapName());
            } catch (Exception e) {
                LOGGER.error("Failed to create world for default map {}", mapData.mapId(), e);
            }
        });

        return mapData;
    }

    public Optional<MapData> getMap(String mapId) {
        return Optional.ofNullable(maps.get(mapId));
    }

    public Collection<MapData> getAllMaps() {
        return Collections.unmodifiableCollection(maps.values());
    }

    public Collection<MapData> getMapsByCreator(UUID creatorUuid) {
        return maps.values().stream()
            .filter(m -> m.creatorUuid().equals(creatorUuid))
            .collect(Collectors.toList());
    }

    public Optional<World> getWorld(String mapId) {
        return Optional.ofNullable(loadedWorlds.get(mapId));
    }

    public CompletableFuture<World> loadWorld(String mapId) {
        return CompletableFuture.supplyAsync(() -> {
            MapData mapData = maps.get(mapId);
            if (mapData == null) {
                throw new IllegalArgumentException("Map not found: " + mapId);
            }

            World world = loadedWorlds.get(mapId);
            if (world != null) {
                return world;
            }

            world = worldCreationService.loadWorld(mapData);
            loadedWorlds.put(mapId, world);
            return world;
        });
    }

    public void teleportPlayerToMap(PlayerEntity player, String mapId) {
        loadWorld(mapId).thenAccept(world -> {
            MapData map = maps.get(mapId);
            if (map != null && world != null) {
                Vec3i spawn = map.worldParams().spawnPosition();
                player.teleport(world, spawn.x(), spawn.y(), spawn.z());
                LOGGER.info("Teleported {} to map {}", player.getName(), map.mapName());
            }
        }).exceptionally(throwable -> {
            LOGGER.error("Failed to teleport player to map {}", mapId, throwable);
            return null;
        });
    }

    public MapExportImport getExportImport() {
        return exportImport;
    }

    public CustomMapMakerPlugin getPlugin() {
        return plugin;
    }

    public void shutdown() {
        autoSaveExecutor.shutdown();
        try {
            if (!autoSaveExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                autoSaveExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            autoSaveExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        saveAllMaps();
    }

    private static class MapPermissionAdapter extends TypeAdapter<MapData.MapPermission> {
        @Override
        public void write(JsonWriter out, MapData.MapPermission value) throws IOException {
            out.value(value.name());
        }

        @Override
        public MapData.MapPermission read(JsonReader in) throws IOException {
            return MapData.MapPermission.valueOf(in.nextString());
        }
    }

    private static class Vec3iAdapter extends TypeAdapter<Vec3i> {
        @Override
        public void write(JsonWriter out, Vec3i value) throws IOException {
            out.beginObject();
            out.name("x").value(value.x());
            out.name("y").value(value.y());
            out.name("z").value(value.z());
            out.endObject();
        }

        @Override
        public Vec3i read(JsonReader in) throws IOException {
            int x = 0, y = 0, z = 0;
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                switch (name) {
                    case "x" -> x = in.nextInt();
                    case "y" -> y = in.nextInt();
                    case "z" -> z = in.nextInt();
                    default -> in.skipValue();
                }
            }
            in.endObject();
            return new Vec3i(x, y, z);
        }
    }
}
