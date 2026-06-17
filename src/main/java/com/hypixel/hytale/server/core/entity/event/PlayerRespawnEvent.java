package com.hypixel.hytale.server.core.entity.event;

import com.hypixel.hytale.server.core.entity.PlayerEntity;
import com.hypixel.hytale.server.core.event.Event;

public class PlayerRespawnEvent implements Event {
    public PlayerEntity getPlayer() { return new PlayerEntity(); }
}
