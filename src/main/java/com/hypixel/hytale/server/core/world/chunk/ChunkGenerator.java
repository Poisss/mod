package com.hypixel.hytale.server.core.world.chunk;

import com.hypixel.hytale.server.core.world.block.BlockState;
import com.hypixel.hytale.server.core.util.math.Vec3i;

public interface ChunkGenerator {
    void generateChunk(Chunk chunk, long seed);
}
