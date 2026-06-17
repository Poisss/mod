package com.example.custommapmaker.ui;

import com.example.custommapmaker.data.MapData;
import com.example.custommapmaker.data.WorldCreationParameters;
import com.example.custommapmaker.service.CustomMapManager;
import com.hypixel.hytale.server.core.entity.PlayerEntity;
import com.hypixel.hytale.server.core.text.Text;
import com.hypixel.hytale.server.core.text.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateMapDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateMapDialog.class);

    private final CustomMapManager mapManager;
    private final PlayerEntity player;
    private final MainMenuPage parentMenu;
    private String pendingMapName = null;
    private boolean awaitingConfirmation = false;

    public CreateMapDialog(CustomMapManager mapManager, PlayerEntity player, MainMenuPage parentMenu) {
        this.mapManager = mapManager;
        this.player = player;
        this.parentMenu = parentMenu;
    }

    public void open() {
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of(TextColor.GOLD + "======= CREATE NEW MAP ======="));
        player.sendMessage(Text.of(TextColor.AQUA + "Enter a name for your new map."));
        player.sendMessage(Text.of(TextColor.GRAY + "Type the map name in chat, or type " +
            TextColor.RED + "cancel" + TextColor.GRAY + " to go back."));
        player.sendMessage(Text.of(TextColor.GOLD + "=============================="));
    }

    public void openWithTemplate(String templateName) {
        WorldCreationParameters params = WorldCreationParameters.fromTemplate(templateName);
        pendingMapName = templateName + " Template";
        awaitingConfirmation = true;

        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of(TextColor.GOLD + "======= CREATE FROM TEMPLATE ======="));
        player.sendMessage(Text.of(TextColor.AQUA + "Template: " + TextColor.WHITE + templateName));
        player.sendMessage(Text.of(TextColor.GRAY + "This will create a map based on the '" + templateName + "' template."));
        player.sendMessage(Text.of(TextColor.GREEN + "Type 'yes' to confirm or 'no' to cancel."));
        player.sendMessage(Text.of(TextColor.GOLD + "===================================="));
    }

    public boolean isAwaitingInput() {
        return pendingMapName == null || awaitingConfirmation;
    }

    public void handleInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            player.sendMessage(Text.of(TextColor.RED + "Please enter a valid name."));
            return;
        }

        String trimmed = input.trim();

        if (trimmed.equalsIgnoreCase("cancel")) {
            close();
            return;
        }

        if (awaitingConfirmation) {
            handleConfirmation(trimmed);
            return;
        }

        if (trimmed.length() < 3) {
            player.sendMessage(Text.of(TextColor.RED + "Map name must be at least 3 characters."));
            return;
        }

        if (trimmed.length() > 32) {
            player.sendMessage(Text.of(TextColor.RED + "Map name must be 32 characters or less."));
            return;
        }

        if (!trimmed.matches("[a-zA-Z0-9 _-]+")) {
            player.sendMessage(Text.of(TextColor.RED + "Map name can only contain letters, numbers, spaces, _ and -."));
            return;
        }

        pendingMapName = trimmed;
        showConfirmation();
    }

    private void showConfirmation() {
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of(TextColor.GOLD + "======= CONFIRM CREATION ======="));
        player.sendMessage(Text.of(TextColor.AQUA + "Map Name: " + TextColor.WHITE + pendingMapName));
        player.sendMessage(Text.of(TextColor.AQUA + "Platform: " + TextColor.WHITE + "10x10 Stone"));
        player.sendMessage(Text.of(TextColor.AQUA + "Type: " + TextColor.WHITE + "Empty Platform"));
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of(
            TextColor.GREEN + "  [Confirm]" +
            TextColor.RED + "    [Cancel]"
        ));
        player.sendMessage(Text.of(TextColor.GRAY + "Type 'yes' to confirm or 'no' to cancel."));
        player.sendMessage(Text.of(TextColor.GOLD + "================================="));
        awaitingConfirmation = true;
    }

    private void handleConfirmation(String input) {
        if (input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y")) {
            createMap();
        } else if (input.equalsIgnoreCase("no") || input.equalsIgnoreCase("n")) {
            player.sendMessage(Text.of(TextColor.YELLOW + "Map creation cancelled."));
            close();
        } else {
            player.sendMessage(Text.of(TextColor.RED + "Please type 'yes' or 'no'."));
        }
    }

    private void createMap() {
        WorldCreationParameters params = WorldCreationParameters.defaultParams();

        MapData mapData = mapManager.createMap(
            pendingMapName,
            player.getName(),
            player.getUuid(),
            params
        );

        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of(TextColor.GREEN + "=============================="));
        player.sendMessage(Text.of(TextColor.GREEN + "Map created successfully!"));
        player.sendMessage(Text.of(TextColor.AQUA + "Name: " + TextColor.WHITE + mapData.mapName()));
        player.sendMessage(Text.of(TextColor.AQUA + "ID: " + TextColor.WHITE + mapData.mapId()));
        player.sendMessage(Text.of(TextColor.AQUA + "Size: " + TextColor.WHITE +
            mapData.worldParams().platformSize() + "x" + mapData.worldParams().platformSize()));
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of(TextColor.YELLOW + "Use " + TextColor.WHITE +
            "/mapmaker go " + mapData.mapId().substring(0, 8) + TextColor.YELLOW + " to teleport."));
        player.sendMessage(Text.of(TextColor.GREEN + "=============================="));

        LOGGER.info("Player {} created map '{}'", player.getName(), pendingMapName);

        pendingMapName = null;
        awaitingConfirmation = false;
        parentMenu.refresh();
    }

    public void close() {
        pendingMapName = null;
        awaitingConfirmation = false;
        parentMenu.open();
    }

    public String getPendingMapName() {
        return pendingMapName;
    }
}
