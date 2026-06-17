package com.example.custommapmaker.command;

import com.example.custommapmaker.CustomMapMakerPlugin;
import com.example.custommapmaker.data.MapData;
import com.example.custommapmaker.data.WorldCreationParameters;
import com.example.custommapmaker.service.CustomMapManager;
import com.hypixel.hytale.server.core.command.CommandManager;
import com.hypixel.hytale.server.core.command.CommandSpec;
import com.hypixel.hytale.server.core.command.argument.StringArgument;
import com.hypixel.hytale.server.core.command.argument.IntegerArgument;
import com.hypixel.hytale.server.core.command.context.CommandContext;
import com.hypixel.hytale.server.core.entity.PlayerEntity;
import com.hypixel.hytale.server.core.text.Text;
import com.hypixel.hytale.server.core.text.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class MapCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapCommandHandler.class);
    private static final String PREFIX = TextColor.AQUA + "[MapMaker] " + TextColor.RESET;

    private final CustomMapManager mapManager;
    private final CommandManager commandManager;
    private final CustomMapMakerPlugin.PluginConfig config;

    public MapCommandHandler(CustomMapManager mapManager, CommandManager commandManager,
                             CustomMapMakerPlugin.PluginConfig config) {
        this.mapManager = mapManager;
        this.commandManager = commandManager;
        this.config = config;
    }

    public void registerCommands() {
        registerMenuCommand();
        registerCreateCommand();
        registerListCommand();
        registerGoCommand();
        registerDeleteCommand();
        registerInfoCommand();
        registerExportCommand();
        registerImportCommand();
        LOGGER.info("Registered all MapMaker commands");
    }

    private void registerMenuCommand() {
        commandManager.register(CommandSpec.builder()
            .name("menu")
            .description("Open the Map Maker menu")
            .permission("custommapmaker.menu")
            .handler(this::handleMenu)
            .build());
    }

    private void handleMenu(CommandContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        if (player == null) {
            ctx.sendFeedback(Text.of(PREFIX + TextColor.RED + "This command can only be used by players."));
            return;
        }
        mapManager.getPlugin().getMapUI().openMainMenu(player);
    }

    private void registerCreateCommand() {
        commandManager.register(CommandSpec.builder()
            .name("create")
            .description("Create a new map")
            .permission("custommapmaker.create")
            .argument(StringArgument.of("name"))
            .handler(this::handleCreate)
            .build());
    }

    private void handleCreate(CommandContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        if (player == null) return;

        String name = ctx.getArgument("name", String.class);
        WorldCreationParameters params = WorldCreationParameters.defaultParams();

        MapData mapData = mapManager.createMap(
            name,
            player.getName(),
            player.getUuid(),
            params
        );

        ctx.sendFeedback(Text.of(PREFIX + TextColor.GREEN + "Map '" + name + "' created successfully!"));
        ctx.sendFeedback(Text.of(PREFIX + TextColor.GRAY + "Use /mapmaker go " + mapData.mapId() + " to teleport."));
    }

    private void registerListCommand() {
        commandManager.register(CommandSpec.builder()
            .name("list")
            .description("List all your maps")
            .permission("custommapmaker.list")
            .handler(this::handleList)
            .build());
    }

    private void handleList(CommandContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        if (player == null) return;

        var maps = mapManager.getMapsByCreator(player.getUuid());
        if (maps.isEmpty()) {
            ctx.sendFeedback(Text.of(PREFIX + TextColor.YELLOW + "You have no maps."));
            return;
        }

        ctx.sendFeedback(Text.of(PREFIX + TextColor.AQUA + "Your maps (" + maps.size() + "):"));
        for (MapData map : maps) {
            String defaultTag = map.isDefault() ? TextColor.GRAY + " [Default]" : "";
            ctx.sendFeedback(Text.of(TextColor.WHITE + " - " + TextColor.GREEN + map.mapName()
                + TextColor.GRAY + " (ID: " + map.mapId() + ")" + defaultTag));
        }
    }

    private void registerGoCommand() {
        commandManager.register(CommandSpec.builder()
            .name("go")
            .description("Teleport to a map")
            .permission("custommapmaker.go")
            .argument(StringArgument.of("mapId"))
            .handler(this::handleGo)
            .build());
    }

    private void handleGo(CommandContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        if (player == null) return;

        String mapId = ctx.getArgument("mapId", String.class);
        Optional<MapData> mapData = mapManager.getMap(mapId);

        if (mapData.isEmpty()) {
            ctx.sendFeedback(Text.of(PREFIX + TextColor.RED + "Map not found: " + mapId));
            return;
        }

        mapManager.teleportPlayerToMap(player, mapId);
        ctx.sendFeedback(Text.of(PREFIX + TextColor.GREEN + "Teleporting to '" + mapData.get().mapName() + "'..."));
    }

    private void registerDeleteCommand() {
        commandManager.register(CommandSpec.builder()
            .name("delete")
            .description("Delete a map")
            .permission("custommapmaker.delete")
            .argument(StringArgument.of("mapId"))
            .handler(this::handleDelete)
            .build());
    }

    private void handleDelete(CommandContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        if (player == null) return;

        String mapId = ctx.getArgument("mapId", String.class);
        Optional<MapData> mapData = mapManager.getMap(mapId);

        if (mapData.isEmpty()) {
            ctx.sendFeedback(Text.of(PREFIX + TextColor.RED + "Map not found: " + mapId));
            return;
        }

        MapData map = mapData.get();
        if (map.isDefault()) {
            ctx.sendFeedback(Text.of(PREFIX + TextColor.RED + "Cannot delete default maps."));
            return;
        }

        if (!map.creatorUuid().equals(player.getUuid())) {
            ctx.sendFeedback(Text.of(PREFIX + TextColor.RED + "You can only delete your own maps."));
            return;
        }

        mapManager.deleteMap(mapId);
        ctx.sendFeedback(Text.of(PREFIX + TextColor.GREEN + "Map '" + map.mapName() + "' deleted."));
    }

    private void registerInfoCommand() {
        commandManager.register(CommandSpec.builder()
            .name("info")
            .description("Show info about a map")
            .permission("custommapmaker.info")
            .argument(StringArgument.of("mapId"))
            .handler(this::handleInfo)
            .build());
    }

    private void handleInfo(CommandContext ctx) {
        String mapId = ctx.getArgument("mapId", String.class);
        Optional<MapData> mapData = mapManager.getMap(mapId);

        if (mapData.isEmpty()) {
            ctx.sendFeedback(Text.of(PREFIX + TextColor.RED + "Map not found: " + mapId));
            return;
        }

        MapData map = mapData.get();
        ctx.sendFeedback(Text.of(PREFIX + TextColor.AQUA + "=== " + map.mapName() + " ==="));
        ctx.sendFeedback(Text.of(TextColor.WHITE + "ID: " + TextColor.GRAY + map.mapId()));
        ctx.sendFeedback(Text.of(TextColor.WHITE + "Creator: " + TextColor.GRAY + map.creator()));
        ctx.sendFeedback(Text.of(TextColor.WHITE + "Size: " + TextColor.GRAY + map.worldParams().platformSize() + "x" + map.worldParams().platformSize()));
        ctx.sendFeedback(Text.of(TextColor.WHITE + "Block: " + TextColor.GRAY + map.worldParams().platformBlock()));
        ctx.sendFeedback(Text.of(TextColor.WHITE + "Default: " + TextColor.GRAY + map.isDefault()));
    }

    private void registerExportCommand() {
        commandManager.register(CommandSpec.builder()
            .name("export")
            .description("Export a map for sharing")
            .permission("custommapmaker.export")
            .argument(StringArgument.of("mapId"))
            .handler(this::handleExport)
            .build());
    }

    private void handleExport(CommandContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        if (player == null) return;

        String mapId = ctx.getArgument("mapId", String.class);
        Optional<MapData> mapData = mapManager.getMap(mapId);

        if (mapData.isEmpty()) {
            ctx.sendFeedback(Text.of(PREFIX + TextColor.RED + "Map not found: " + mapId));
            return;
        }

        try {
            String exportPath = mapManager.getExportImport().exportMap(mapId);
            ctx.sendFeedback(Text.of(PREFIX + TextColor.GREEN + "Map exported to: " + exportPath));
        } catch (Exception e) {
            ctx.sendFeedback(Text.of(PREFIX + TextColor.RED + "Export failed: " + e.getMessage()));
        }
    }

    private void registerImportCommand() {
        commandManager.register(CommandSpec.builder()
            .name("import")
            .description("Import a shared map")
            .permission("custommapmaker.import")
            .argument(StringArgument.of("fileName"))
            .handler(this::handleImport)
            .build());
    }

    private void handleImport(CommandContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        if (player == null) return;

        String fileName = ctx.getArgument("fileName", String.class);

        try {
            MapData imported = mapManager.getExportImport().importMap(fileName, player.getName(), player.getUuid());
            ctx.sendFeedback(Text.of(PREFIX + TextColor.GREEN + "Map imported: '" + imported.mapName() + "'"));
        } catch (Exception e) {
            ctx.sendFeedback(Text.of(PREFIX + TextColor.RED + "Import failed: " + e.getMessage()));
        }
    }
}
