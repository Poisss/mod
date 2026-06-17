package com.hypixel.hytale.server.core.world.chunk;

import com.hypixel.hytale.server.core.world.block.BlockState;
import com.hypixel.hytale.server.core.util.math.Vec3i;

public class Chunk {
    public int getChunkX() { return 0; }
    public int getChunkZ() { return 0; }
    public void setBlockState(Vec3i pos, BlockState state, boolean flag) {}
}
