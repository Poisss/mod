package com.hypixel.hytale.server.core.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigValue {
    private final Map<String, Object> data = new HashMap<>();

    public int getInt(String key, int def) {
        Object v = data.get(key);
        return v instanceof Number ? ((Number) v).intValue() : def;
    }

    public boolean getBoolean(String key, boolean def) {
        Object v = data.get(key);
        return v instanceof Boolean ? (Boolean) v : def;
    }

    public static Builder object() { return new Builder(); }

    public static class Builder {
        private final ConfigValue value = new ConfigValue();
        public Builder put(String key, Object val) { value.data.put(key, val); return this; }
        public ConfigValue build() { return value; }
    }
}
