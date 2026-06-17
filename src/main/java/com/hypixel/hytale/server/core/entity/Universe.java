package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.server.core.util.Identifier;
import java.util.Map;
import java.util.Optional;

public class Universe {
    public World createWorld(String name, Identifier dimensionType, Identifier generatorType,
                            Map<String, Object> settings, long seed) { return new World(); }
    public Optional<World> getWorld(String name) { return Optional.empty(); }
}
