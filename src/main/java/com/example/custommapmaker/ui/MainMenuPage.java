package com.example.custommapmaker.ui;

import com.example.custommapmaker.data.MapData;
import com.example.custommapmaker.service.CustomMapManager;
import com.hypixel.hytale.server.core.entity.PlayerEntity;
import com.hypixel.hytale.server.core.text.Text;
import com.hypixel.hytale.server.core.text.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class MainMenuPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainMenuPage.class);

    private final CustomMapManager mapManager;
    private final PlayerEntity player;
    private int currentPage = 0;
    private static final int MAPS_PER_PAGE = 7;

    public MainMenuPage(CustomMapManager mapManager, PlayerEntity player) {
        this.mapManager = mapManager;
        this.player = player;
    }

    public void open() {
        LOGGER.debug("Opening main menu for player {}", player.getName());
        renderPage();
    }

    private void renderPage() {
        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of(TextColor.GOLD + "========== MAP MAKER =========="));
        player.sendMessage(Text.of(TextColor.AQUA + "Map Browser"));

        Collection<MapData> allMaps = mapManager.getAllMaps();
        MapData[] mapArray = allMaps.toArray(new MapData[0]);
        int totalPages = Math.max(1, (int) Math.ceil((double) mapArray.length / MAPS_PER_PAGE));

        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }

        int startIdx = currentPage * MAPS_PER_PAGE;
        int endIdx = Math.min(startIdx + MAPS_PER_PAGE, mapArray.length);

        if (mapArray.length == 0) {
            player.sendMessage(Text.of(TextColor.GRAY + "  No maps available. Use " +
                TextColor.YELLOW + "/mapmaker create <name>" + TextColor.GRAY + " to create one."));
        } else {
            for (int i = startIdx; i < endIdx; i++) {
                MapData map = mapArray[i];
                renderMapEntry(map, i - startIdx + 1);
            }
        }

        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of(
            TextColor.GRAY + "Page " + TextColor.WHITE + (currentPage + 1) + "/" + totalPages
        ));

        if (currentPage > 0) {
            player.sendMessage(Text.of(TextColor.YELLOW + "  [Prev Page]"));
        }
        if (currentPage < totalPages - 1) {
            player.sendMessage(Text.of(TextColor.YELLOW + "  [Next Page]"));
        }

        player.sendMessage(Text.of(""));
        player.sendMessage(Text.of(
            TextColor.GREEN + "  [Create New Map]" +
            TextColor.RED + "    [Close]"
        ));
        player.sendMessage(Text.of(TextColor.GOLD + "================================"));
    }

    private void renderMapEntry(MapData map, int number) {
        String defaultTag = map.isDefault() ? TextColor.GRAY + " [Default]" : "";
        String desc = map.description().isEmpty() ? "" : TextColor.GRAY + " - " + map.description();

        player.sendMessage(Text.of(
            TextColor.WHITE + " " + number + ". " +
            TextColor.GREEN + map.mapName() +
            defaultTag + desc
        ));
        player.sendMessage(Text.of(
            TextColor.GRAY + "   Creator: " + TextColor.WHITE + map.creator() +
            TextColor.GRAY + " | Size: " + TextColor.WHITE + map.worldParams().platformSize() + "x" +
            map.worldParams().platformSize() +
            TextColor.GRAY + " | ID: " + TextColor.DARK_GRAY + map.mapId().substring(0, 8) + "..."
        ));
        player.sendMessage(Text.of(
            TextColor.AQUA + "   [Go]" +
            TextColor.YELLOW + "  [Edit]" +
            TextColor.BLUE + "  [Info]" +
            TextColor.AQUA + "  [Export]" +
            TextColor.RED + "  [Delete]"
        ));
    }

    public void nextPage() {
        currentPage++;
        renderPage();
    }

    public void prevPage() {
        if (currentPage > 0) {
            currentPage--;
        }
        renderPage();
    }

    public void refresh() {
        renderPage();
    }

    public void close() {
        player.sendMessage(Text.of(TextColor.GRAY + "Map Maker closed."));
    }
}
