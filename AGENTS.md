# Hytale Modding Agent Instructions

You are an expert in Hytale modding, including Server Plugins (Java), Data Assets (JSON), Art Assets (Blockbench), and Save Files. You understand the Hytale Server API, Entity Component System (ECS), Event System, Plugin System, Registry System, Asset System, and World Generation.

## Code Style and Structure

### Server Plugins (Java)
- Write clean, efficient, well-documented Java 21+ code targeting the Hytale Server runtime
- Follow Hytale plugin conventions: extend `JavaPlugin`, implement lifecycle methods (`setup`, `start`, `shutdown`)
- Use descriptive class/method/variable names following camelCase (methods, variables) and PascalCase (classes, interfaces)
- Organize packages logically: `com.<author>.<modname>.plugin`, `.command`, `.event`, `.system`, `.component`, `.registry`
- Use Gradle with the Hytale plugin template (Kotlin DSL preferred)
- Register via `META-INF/services/com.hypixel.hytale.plugin.early.EarlyPlugin`

### Data Assets (JSON)
- Follow Hytale asset schema: `asset_type/namespace/path.json`
- Use snake_case for asset IDs (e.g., `my_mod:custom_block`)
- Reference vanilla assets with `hytale:` namespace
- Validate JSON against asset codec system before testing
- Organize in `src/main/resources/assets/<namespace>/<asset_type>/`

### Art Assets (Blockbench)
- Use Blockbench with official Hytale plugin
- Export as `.bbmodel` for editing, compile to Hytale format via Asset Editor
- Follow Hytale texture atlas constraints (power-of-2 dimensions)
- Use Hytale animation format for animated models

### ECS Architecture
- Define components as immutable data records (Java `record` or plain classes with final fields)
- Systems implement `EntitySystem` with `update(World, float deltaTime)` or query-based tick methods
- Use `ComponentQuery` with `allOf`, `anyOf`, `noneOf` for efficient archetype matching
- Register components in `ComponentRegistry` during plugin `setup()`

### Event System
- Subscribe via `@EventHandler` annotation or `EventBus.subscribe()`
- Use appropriate priority: `HIGH` (first), `NORMAL` (default), `LOW` (last)
- Cancel events with `event.setCancelled(true)` when applicable
- Prefer specific event types over generic ones

### Registry System
- Register custom assets via `MutableRegistry<T>` in `setup()`
- Use `RegistryKey` for type-safe lookups
- Follow naming convention: `namespace:path`

## Testing and Verification

- Test plugins on local Hytale dedicated server (run `./hytale-server.sh` or `hytale-server.bat`)
- Use Asset Editor in-game for data asset validation (`/asset_editor` command)
- Verify JSON assets load without errors in server console
- Test multiplayer behavior with multiple clients
- Check server logs for `[ERROR]` or `[WARN]` related to your mod

## Build and Deployment

- Build plugin: `./gradlew shadowJar` (produces fat JAR with dependencies)
- Install plugin: place JAR in `server/plugins/` directory
- Install data/art assets: place in `server/asset_packs/<pack_name>/`
- Server reload: `/reload plugins` or `/reload assets` (hot-reload supported for assets)

## Key APIs and Patterns

### Plugin Entry Point
```java
public class MyPlugin extends JavaPlugin {
    @Override
    public void setup(JavaPluginInit init) {
        // Register components, commands, registries
    }
    @Override
    public void start() {
        // Subscribe events, start systems
    }
    @Override
    public void shutdown() {
        // Cleanup
    }
}
```

### Command Registration
```java
commandManager.register(CommandSpec.builder()
    .name("mycommand")
    .permission("myplugin.command.mycommand")
    .handler(ctx -> { /* logic */ })
    .build());
```

### ECS Component + System
```java
// Component
public record HealthComponent(float current, float max) implements Component {}

// System
public class HealthRegenSystem implements EntitySystem {
    private final ComponentQuery query = ComponentQuery.builder()
        .allOf(HealthComponent.class)
        .build();
    
    @Override
    public void update(World world, float deltaTime) {
        query.forEach(world, (entity, health) -> {
            if (health.current() < health.max()) {
                // regen logic
            }
        });
    }
}
```

### Data Asset Example (Block)
```json
{
  "type": "block",
  "id": "my_mod:glowing_block",
  "display_name": "Glowing Block",
  "properties": {
    "light_level": 15,
    "hardness": 2.0,
    "resistance": 6.0
  },
  "model": "my_mod:block/glowing_block",
  "texture": "my_mod:block/glowing_block"
}
```

## Documentation References

- Official Modding Strategy: https://hytale.com/news/2025/11/hytale-modding-strategy-and-status
- Server API Docs: https://hytale-docs.dev/ | https://release.server.docs.hytale.com/
- Community Docs: https://britakee-studios.gitbook.io/hytale-modding-documentation
- Asset Editor: In-game (`/asset_editor`) or standalone tool
- Blockbench Plugin: https://blockbench.net/ (official Hytale plugin)

## Common Tasks

1. **Create plugin scaffold** → Gradle project with Hytale plugin template
2. **Add custom block/item** → JSON asset + model/texture in asset pack
3. **Implement game mechanic** → ECS component + system + event listeners
4. **Add command** → CommandSpec registration in plugin start()
5. **Modify world gen** → WorldGenerator asset or custom chunk generator system
6. **Create NPC behavior** → AI task components + flock system integration

## Notes

- Hytale server is **not obfuscated** — decompile with Vineflower/Fernflower for reference
- Server source code release expected ~March 2026 (per official roadmap)
- Plugin API is **server-side only**; client mods not officially supported yet
- Visual Scripting planned for future — currently Java + JSON only
- Always test on dedicated server, not just singleplayer