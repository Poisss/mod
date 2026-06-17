package com.hypixel.hytale.server.core.world.block;

import com.hypixel.hytale.server.core.util.Identifier;
import java.util.Map;

public record BlockState(Identifier blockId, Map<String, Object> properties) {}
