package com.hypixel.hytale.server.core.plugin;

public abstract class JavaPlugin {
    protected abstract void setup(JavaPluginInit init);
    protected abstract void start();
    public void shutdown() {}
}
