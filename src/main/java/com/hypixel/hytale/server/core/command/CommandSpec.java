package com.hypixel.hytale.server.core.command;

import com.hypixel.hytale.server.core.command.argument.Argument;
import com.hypixel.hytale.server.core.command.context.CommandContext;
import java.util.function.Consumer;

public class CommandSpec {
    private String name;
    private String description;
    private String permission;
    private Consumer<CommandContext> handler;
    private Argument<?>[] arguments;

    private CommandSpec() {}

    public static Builder builder() { return new Builder(); }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPermission() { return permission; }

    public static class Builder {
        private final CommandSpec spec = new CommandSpec();

        public Builder name(String name) { spec.name = name; return this; }
        public Builder description(String desc) { spec.description = desc; return this; }
        public Builder permission(String perm) { spec.permission = perm; return this; }
        public Builder handler(Consumer<CommandContext> handler) { spec.handler = handler; return this; }
        public Builder argument(Argument<?> arg) { return this; }
        public CommandSpec build() { return spec; }
    }
}
