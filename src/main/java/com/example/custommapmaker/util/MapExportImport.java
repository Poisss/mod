package com.example.custommapmaker.util;

import com.example.custommapmaker.data.MapData;
import com.example.custommapmaker.data.WorldCreationParameters;
import com.example.custommapmaker.service.CustomMapManager;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MapExportImport {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapExportImport.class);
    private static final String EXPORT_SUFFIX = ".mapmaker.zip";

    private final CustomMapManager mapManager;
    private final Gson gson;
    private final Path exportsDirectory;

    public MapExportImport(CustomMapManager mapManager, Gson gson, Path exportsDirectory) {
        this.mapManager = mapManager;
        this.gson = gson;
        this.exportsDirectory = exportsDirectory;
    }

    public String exportMap(String mapId) throws IOException {
        MapData mapData = mapManager.getMap(mapId)
            .orElseThrow(() -> new IllegalArgumentException("Map not found: " + mapId));

        String safeName = mapData.mapName().replaceAll("[^a-zA-Z0-9_-]", "_");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String zipName = safeName + "_" + timestamp + EXPORT_SUFFIX;
        Path zipPath = exportsDirectory.resolve(zipName);

        Files.createDirectories(exportsDirectory);

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            ZipEntry mapJsonEntry = new ZipEntry("map_data.json");
            zos.putNextEntry(mapJsonEntry);
            byte[] jsonBytes = gson.toJson(mapData).getBytes("UTF-8");
            zos.write(jsonBytes);
            zos.closeEntry();

            String generatorSettings = gson.toJson(mapData.worldParams().generatorSettings());
            ZipEntry settingsEntry = new ZipEntry("generator_settings.json");
            zos.putNextEntry(settingsEntry);
            zos.write(generatorSettings.getBytes("UTF-8"));
            zos.closeEntry();
        }

        LOGGER.info("Exported map '{}' to {}", mapData.mapName(), zipPath);
        return zipPath.toString();
    }

    public MapData importMap(String fileName, String importerName, java.util.UUID importerUuid) throws IOException {
        Path sourcePath;
        if (fileName.endsWith(EXPORT_SUFFIX)) {
            sourcePath = exportsDirectory.resolve(fileName);
        } else {
            sourcePath = exportsDirectory.resolve(fileName + EXPORT_SUFFIX);
        }

        if (!Files.exists(sourcePath)) {
            sourcePath = Paths.get(fileName);
        }

        if (!Files.exists(sourcePath)) {
            throw new FileNotFoundException("Export file not found: " + fileName);
        }

        MapData importedData;

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(sourcePath))) {
            MapData loadedMap = null;

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("map_data.json")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    loadedMap = gson.fromJson(baos.toString("UTF-8"), MapData.class);
                }
                zis.closeEntry();
            }

            if (loadedMap == null) {
                throw new IOException("Invalid map export: missing map_data.json");
            }

            String newId = java.util.UUID.randomUUID().toString();
            WorldCreationParameters newParams = WorldCreationParameters.defaultParams();

            importedData = MapData.builder()
                .mapId(newId)
                .mapName(loadedMap.mapName() + " (Imported)")
                .creator(importerName)
                .creatorUuid(importerUuid)
                .worldParams(newParams)
                .isDefault(false)
                .description(loadedMap.description())
                .tags(loadedMap.tags())
                .build();
        }

        mapManager.saveMap(importedData);
        LOGGER.info("Imported map '{}' from {}", importedData.mapName(), sourcePath);
        return importedData;
    }

    public Path getExportsDirectory() {
        return exportsDirectory;
    }
}
