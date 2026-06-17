package com.hypixel.hytale.server.core.event;

import java.util.function.Consumer;

public class EventBus {
    public <T extends Event> void subscribe(Class<T> eventClass, EventPriority priority, Consumer<T> handler) {}
    public void unsubscribeAll(Object subscriber) {}
    public void post(Event event) {}
}
