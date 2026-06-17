package com.hypixel.hytale.server.core.entity.event;

import com.hypixel.hytale.server.core.entity.PlayerEntity;
import com.hypixel.hytale.server.core.event.Event;

public class PlayerChatEvent implements Event {
    private boolean cancelled;
    public PlayerEntity getPlayer() { return new PlayerEntity(); }
    public String getMessage() { return ""; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    public boolean isCancelled() { return cancelled; }
}
