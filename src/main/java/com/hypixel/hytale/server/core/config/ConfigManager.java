package com.hypixel.hytale.server.core.config;

public class ConfigManager {
    public ConfigSpec getSpec(String name) { return new ConfigSpec(); }
    public ConfigValue load(String name) { return null; }
    public void save(String name, ConfigValue value) {}
}
