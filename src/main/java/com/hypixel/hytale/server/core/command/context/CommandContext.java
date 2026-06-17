package com.hypixel.hytale.server.core.command.context;

import com.hypixel.hytale.server.core.entity.PlayerEntity;
import com.hypixel.hytale.server.core.text.Text;

public class CommandContext {
    public PlayerEntity getPlayer() { return null; }
    public <T> T getArgument(String name, Class<T> type) { return null; }
    public void sendFeedback(Text text) {}
    public void sendFeedback(String text) {}
}
