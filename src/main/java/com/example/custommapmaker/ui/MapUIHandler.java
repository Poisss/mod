package com.example.custommapmaker.ui;

import com.example.custommapmaker.CustomMapMakerPlugin;
import com.example.custommapmaker.data.MapData;
import com.example.custommapmaker.service.CustomMapManager;
import com.hypixel.hytale.server.core.entity.PlayerEntity;
import com.hypixel.hytale.server.core.event.EventBus;
import com.hypixel.hytale.server.core.event.EventHandler;
import com.hypixel.hytale.server.core.event.EventPriority;
import com.hypixel.hytale.server.core.entity.event.PlayerChatEvent;
import com.hypixel.hytale.server.core.text.Text;
import com.hypixel.hytale.server.core.text.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MapUIHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapUIHandler.class);
    private static final String PREFIX = TextColor.AQUA + "[MapMaker] " + TextColor.RESET;

    private final CustomMapMakerPlugin plugin;
    private final CustomMapManager mapManager;
    private final EventBus eventBus;

    private final Map<UUID, PlayerUIState> playerStates = new ConcurrentHashMap<>();

    public MapUIHandler(CustomMapMakerPlugin plugin, CustomMapManager mapManager, EventBus eventBus) {
        this.plugin = plugin;
        this.mapManager = mapManager;
        this.eventBus = eventBus;
    }

    public void registerEvents() {
        eventBus.subscribe(PlayerChatEvent.class, EventPriority.HIGH, this::onPlayerChat);
        LOGGER.info("MapUIHandler events registered");
    }

    public void unregisterEvents() {
        eventBus.unsubscribeAll(this);
        playerStates.clear();
        LOGGER.info("MapUIHandler events unregistered");
    }

    public void openMainMenu(PlayerEntity player) {
        PlayerUIState state = getOrCreateState(player);

        MainMenuPage mainMenu = new MainMenuPage(mapManager, player);
        state.setMainMenu(mainMenu);
        state.setActiveUI(UIState.MAIN_MENU);
        mainMenu.open();
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        PlayerEntity player = event.getPlayer();
        UUID playerId = player.getUuid();

        PlayerUIState state = playerStates.get(playerId);
        if (state == null || state.getActiveUI() == UIState.NONE) {
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage().trim();

        switch (state.getActiveUI()) {
            case MAIN_MENU -> handleMainMenuInput(player, state, message);
            case CREATE_DIALOG -> handleCreateDialogInput(player, state, message);
            default -> {}
        }
    }

    private void handleMainMenuInput(PlayerEntity player, PlayerUIState state, String input) {
        MainMenuPage menu = state.getMainMenu();
        if (menu == null) return;

        switch (input.toLowerCase()) {
            case "1", "2", "3", "4", "5", "6", "7" -> {
                int index = Integer.parseInt(input) - 1;
                handleMapAction(player, state, index, "go");
            }
            case "prev", "back", "<-" -> menu.prevPage();
            case "next", "->" -> menu.nextPage();
            case "create", "new" -> {
                CreateMapDialog dialog = new CreateMapDialog(mapManager, player, menu);
                state.setCreateDialog(dialog);
                state.setActiveUI(UIState.CREATE_DIALOG);
                dialog.open();
            }
            case "close", "exit", "quit" -> {
                menu.close();
                state.setActiveUI(UIState.NONE);
            }
            default -> {
                if (input.toLowerCase().startsWith("go ")) {
                    String mapId = input.substring(3).trim();
                    teleportPlayerToMap(player, mapId);
                } else if (input.toLowerCase().startsWith("info ")) {
                    String mapId = input.substring(5).trim();
                    showMapInfo(player, mapId);
                } else if (input.toLowerCase().startsWith("export ")) {
                    String mapId = input.substring(7).trim();
                    exportMap(player, mapId);
                } else if (input.toLowerCase().startsWith("delete ")) {
                    String mapId = input.substring(7).trim();
                    deleteMap(player, state, mapId);
                } else {
                    player.sendMessage(Text.of(PREFIX + TextColor.RED + "Unknown command. Type a number, 'create', or 'close'."));
                }
            }
        }
    }

    private void handleCreateDialogInput(PlayerEntity player, PlayerUIState state, String input) {
        CreateMapDialog dialog = state.getCreateDialog();
        if (dialog == null) return;

        dialog.handleInput(input);
    }

    private void handleMapAction(PlayerEntity player, PlayerUIState state, int index, String action) {
        MainMenuPage menu = state.getMainMenu();
        if (menu == null) return;

        Object[] maps = mapManager.getAllMaps().toArray();
        if (index < 0 || index >= maps.length) {
            player.sendMessage(Text.of(PREFIX + TextColor.RED + "Invalid map number."));
            return;
        }

        MapData map = (MapData) maps[index];
        teleportPlayerToMap(player, map.mapId());
        state.setActiveUI(UIState.NONE);
    }

    private void teleportPlayerToMap(PlayerEntity player, String mapId) {
        Optional<MapData> mapData = mapManager.getMap(mapId);
        if (mapData.isEmpty()) {
            player.sendMessage(Text.of(PREFIX + TextColor.RED + "Map not found: " + mapId));
            return;
        }

        mapManager.teleportPlayerToMap(player, mapId);
        player.sendMessage(Text.of(PREFIX + TextColor.GREEN +
            "Teleporting to '" + mapData.get().mapName() + "'..."));
    }

    private void showMapInfo(PlayerEntity player, String mapId) {
        Optional<MapData> mapData = mapManager.getMap(mapId);
        if (mapData.isEmpty()) {
            player.sendMessage(Text.of(PREFIX + TextColor.RED + "Map not found: " + mapId));
            return;
        }

        MapData map = mapData.get();
        player.sendMessage(Text.of(PREFIX + TextColor.AQUA + "=== " + map.mapName() + " ==="));
        player.sendMessage(Text.of(TextColor.WHITE + "ID: " + TextColor.GRAY + map.mapId()));
        player.sendMessage(Text.of(TextColor.WHITE + "Creator: " + TextColor.GRAY + map.creator()));
        player.sendMessage(Text.of(TextColor.WHITE + "Description: " + TextColor.GRAY +
            (map.description().isEmpty() ? "None" : map.description())));
        player.sendMessage(Text.of(TextColor.WHITE + "Platform: " + TextColor.GRAY +
            map.worldParams().platformSize() + "x" + map.worldParams().platformSize() + " " +
            map.worldParams().platformBlock()));
        player.sendMessage(Text.of(TextColor.WHITE + "Default: " + TextColor.GRAY + map.isDefault()));
        player.sendMessage(Text.of(TextColor.WHITE + "Created: " + TextColor.GRAY + map.createdAt()));
    }

    private void exportMap(PlayerEntity player, String mapId) {
        try {
            String path = mapManager.getExportImport().exportMap(mapId);
            player.sendMessage(Text.of(PREFIX + TextColor.GREEN + "Map exported to: " + path));
        } catch (Exception e) {
            player.sendMessage(Text.of(PREFIX + TextColor.RED + "Export failed: " + e.getMessage()));
        }
    }

    private void deleteMap(PlayerEntity player, PlayerUIState state, String mapId) {
        Optional<MapData> mapData = mapManager.getMap(mapId);
        if (mapData.isEmpty()) {
            player.sendMessage(Text.of(PREFIX + TextColor.RED + "Map not found: " + mapId));
            return;
        }

        MapData map = mapData.get();
        if (map.isDefault()) {
            player.sendMessage(Text.of(PREFIX + TextColor.RED + "Cannot delete default maps."));
            return;
        }

        if (!map.creatorUuid().equals(player.getUuid())) {
            player.sendMessage(Text.of(PREFIX + TextColor.RED + "You can only delete your own maps."));
            return;
        }

        mapManager.deleteMap(mapId);
        player.sendMessage(Text.of(PREFIX + TextColor.GREEN + "Map '" + map.mapName() + "' deleted."));

        if (state.getMainMenu() != null) {
            state.getMainMenu().refresh();
        }
    }

    private PlayerUIState getOrCreateState(PlayerEntity player) {
        return playerStates.computeIfAbsent(player.getUuid(), id -> new PlayerUIState());
    }

    public void clearState(PlayerEntity player) {
        playerStates.remove(player.getUuid());
    }

    public void clearState(UUID playerId) {
        playerStates.remove(playerId);
    }

    public enum UIState {
        NONE,
        MAIN_MENU,
        CREATE_DIALOG,
        EDIT_DIALOG,
        TEMPLATE_SELECT
    }

    public static class PlayerUIState {
        private UIState activeUI = UIState.NONE;
        private MainMenuPage mainMenu;
        private CreateMapDialog createDialog;

        public UIState getActiveUI() { return activeUI; }
        public void setActiveUI(UIState activeUI) { this.activeUI = activeUI; }

        public MainMenuPage getMainMenu() { return mainMenu; }
        public void setMainMenu(MainMenuPage mainMenu) { this.mainMenu = mainMenu; }

        public CreateMapDialog getCreateDialog() { return createDialog; }
        public void setCreateDialog(CreateMapDialog createDialog) { this.createDialog = createDialog; }
    }
}
