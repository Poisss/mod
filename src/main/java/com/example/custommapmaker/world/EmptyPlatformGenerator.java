package com.example.custommapmaker.world;

import com.hypixel.hytale.server.core.world.WorldGenerator;
import com.hypixel.hytale.server.core.world.chunk.ChunkGenerator;
import com.hypixel.hytale.server.core.world.chunk.Chunk;
import com.hypixel.hytale.server.core.world.block.BlockState;
import com.hypixel.hytale.server.core.world.block.Block;
import com.hypixel.hytale.server.core.util.Identifier;
import com.hypixel.hytale.server.core.util.math.Vec3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EmptyPlatformGenerator implements WorldGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmptyPlatformGenerator.class);

    private int platformSize = 10;
    private int platformHeight = 64;
    private Identifier platformBlock = Identifier.of("hytale", "stone");

    public EmptyPlatformGenerator() {}

    public EmptyPlatformGenerator(int platformSize, int platformHeight, Identifier platformBlock) {
        this.platformSize = platformSize;
        this.platformHeight = platformHeight;
        this.platformBlock = platformBlock;
    }

    @Override
    public ChunkGenerator createChunkGenerator(Map<String, Object> settings) {
        applySettings(settings);
        return new PlatformChunkGenerator(platformSize, platformHeight, platformBlock);
    }

    private void applySettings(Map<String, Object> settings) {
        if (settings == null) return;
        if (settings.containsKey("platform_size")) {
            this.platformSize = ((Number) settings.get("platform_size")).intValue();
        }
        if (settings.containsKey("platform_height")) {
            this.platformHeight = ((Number) settings.get("platform_height")).intValue();
        }
        if (settings.containsKey("platform_block")) {
            this.platformBlock = Identifier.parse((String) settings.get("platform_block"));
        }
    }

    @Override
    public Vec3i getSpawnPosition() {
        return new Vec3i(0, platformHeight + 1, 0);
    }

    public int getPlatformSize() { return platformSize; }
    public int getPlatformHeight() { return platformHeight; }
    public Identifier getPlatformBlock() { return platformBlock; }

    public static class PlatformChunkGenerator implements ChunkGenerator {
        private final int platformSize;
        private final int platformHeight;
        private final Identifier platformBlockId;

        private static final int CHUNK_SIZE = 16;

        public PlatformChunkGenerator(int platformSize, int platformHeight, Identifier platformBlock) {
            this.platformSize = platformSize;
            this.platformHeight = platformHeight;
            this.platformBlockId = platformBlock;
        }

        @Override
        public void generateChunk(Chunk chunk, long seed) {
            int chunkX = chunk.getChunkX();
            int chunkZ = chunk.getChunkZ();

            int halfSize = platformSize / 2;

            int startX = chunkX * CHUNK_SIZE;
            int startZ = chunkZ * CHUNK_SIZE;

            for (int localX = 0; localX < CHUNK_SIZE; localX++) {
                for (int localZ = 0; localZ < CHUNK_SIZE; localZ++) {
                    int worldX = startX + localX;
                    int worldZ = startZ + localZ;

                    if (worldX >= -halfSize && worldX < halfSize &&
                        worldZ >= -halfSize && worldZ < halfSize) {
                        Vec3i pos = new Vec3i(localX, platformHeight, localZ);
                        BlockState blockState = new BlockState(platformBlockId, Map.of());
                        chunk.setBlockState(pos, blockState, false);
                    }
                }
            }

            LOGGER.debug("Generated platform chunk at ({}, {})", chunkX, chunkZ);
        }
    }
}
