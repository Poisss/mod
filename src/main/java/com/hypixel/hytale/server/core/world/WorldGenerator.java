package com.hypixel.hytale.server.core.world;

import com.hypixel.hytale.server.core.world.chunk.ChunkGenerator;
import com.hypixel.hytale.server.core.util.math.Vec3i;
import java.util.Map;

public interface WorldGenerator {
    ChunkGenerator createChunkGenerator(Map<String, Object> settings);
    Vec3i getSpawnPosition();
}
