package com.example.custommapmaker.data;

import com.google.gson.annotations.SerializedName;
import com.hypixel.hytale.server.core.util.Identifier;
import com.hypixel.hytale.server.core.util.math.Vec3i;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public record MapData(
    @SerializedName("map_id") String mapId,
    @SerializedName("map_name") String mapName,
    @SerializedName("creator") String creator,
    @SerializedName("creator_uuid") UUID creatorUuid,
    @SerializedName("world_params") WorldCreationParameters worldParams,
    @SerializedName("is_default") boolean isDefault,
    @SerializedName("created_at") long createdAt,
    @SerializedName("last_modified") long lastModified,
    @SerializedName("version") int version,
    @SerializedName("description") String description,
    @SerializedName("tags") List<String> tags,
    @SerializedName("thumbnail_path") String thumbnailPath,
    @SerializedName("metadata") Map<String, Object> metadata,
    @SerializedName("permissions") Map<UUID, MapPermission> permissions
) {
    public static Builder builder() {
        return new Builder();
    }
    
    public MapData withMapName(String newName) {
        return new MapData(mapId, newName, creator, creatorUuid, worldParams, isDefault, 
            createdAt, System.currentTimeMillis(), version + 1, description, tags, 
            thumbnailPath, metadata, permissions);
    }
    
    public MapData withDescription(String newDescription) {
        return new MapData(mapId, mapName, creator, creatorUuid, worldParams, isDefault, 
            createdAt, System.currentTimeMillis(), version + 1, newDescription, tags, 
            thumbnailPath, metadata, permissions);
    }
    
    public MapData withThumbnail(String path) {
        return new MapData(mapId, mapName, creator, creatorUuid, worldParams, isDefault, 
            createdAt, System.currentTimeMillis(), version + 1, description, tags, 
            path, metadata, permissions);
    }
    
    public MapData addTag(String tag) {
        List<String> newTags = new ArrayList<>(tags);
        if (!newTags.contains(tag)) {
            newTags.add(tag);
        }
        return new MapData(mapId, mapName, creator, creatorUuid, worldParams, isDefault, 
            createdAt, System.currentTimeMillis(), version + 1, description, newTags, 
            thumbnailPath, metadata, permissions);
    }
    
    public MapData removeTag(String tag) {
        List<String> newTags = new ArrayList<>(tags);
        newTags.remove(tag);
        return new MapData(mapId, mapName, creator, creatorUuid, worldParams, isDefault, 
            createdAt, System.currentTimeMillis(), version + 1, description, newTags, 
            thumbnailPath, metadata, permissions);
    }
    
    public MapData grantPermission(UUID playerId, MapPermission permission) {
        Map<UUID, MapPermission> newPerms = new HashMap<>(permissions);
        newPerms.put(playerId, permission);
        return new MapData(mapId, mapName, creator, creatorUuid, worldParams, isDefault, 
            createdAt, System.currentTimeMillis(), version + 1, description, tags, 
            thumbnailPath, metadata, newPerms);
    }
    
    public MapData revokePermission(UUID playerId) {
        Map<UUID, MapPermission> newPerms = new HashMap<>(permissions);
        newPerms.remove(playerId);
        return new MapData(mapId, mapName, creator, creatorUuid, worldParams, isDefault, 
            createdAt, System.currentTimeMillis(), version + 1, description, tags, 
            thumbnailPath, metadata, newPerms);
    }
    
    public boolean hasPermission(UUID playerId, MapPermission required) {
        MapPermission perm = permissions.get(playerId);
        if (perm == null) return false;
        return perm.ordinal() >= required.ordinal();
    }
    
    public static class Builder {
        private String mapId = UUID.randomUUID().toString();
        private String mapName = "Unnamed Map";
        private String creator = "Unknown";
        private UUID creatorUuid = UUID.randomUUID();
        private WorldCreationParameters worldParams = WorldCreationParameters.defaultParams();
        private boolean isDefault = false;
        private long createdAt = Instant.now().toEpochMilli();
        private long lastModified = Instant.now().toEpochMilli();
        private int version = 1;
        private String description = "";
        private List<String> tags = new ArrayList<>();
        private String thumbnailPath = "";
        private Map<String, Object> metadata = new HashMap<>();
        private Map<UUID, MapPermission> permissions = new ConcurrentHashMap<>();
        
        public Builder mapId(String mapId) { this.mapId = mapId; return this; }
        public Builder mapName(String mapName) { this.mapName = mapName; return this; }
        public Builder creator(String creator) { this.creator = creator; return this; }
        public Builder creatorUuid(UUID creatorUuid) { this.creatorUuid = creatorUuid; return this; }
        public Builder worldParams(WorldCreationParameters worldParams) { this.worldParams = worldParams; return this; }
        public Builder isDefault(boolean isDefault) { this.isDefault = isDefault; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder tags(List<String> tags) { this.tags = tags; return this; }
        public Builder thumbnailPath(String path) { this.thumbnailPath = path; return this; }
        public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }
        public Builder permissions(Map<UUID, MapPermission> permissions) { this.permissions = permissions; return this; }
        
        public MapData build() {
            return new MapData(mapId, mapName, creator, creatorUuid, worldParams, isDefault,
                createdAt, lastModified, version, description, tags, thumbnailPath, metadata, permissions);
        }
    }
    
    public enum MapPermission {
        OWNER(3),
        EDITOR(2),
        VIEWER(1),
        NONE(0);
        
        private final int level;
        
        MapPermission(int level) {
            this.level = level;
        }
        
        public int getLevel() {
            return level;
        }
        
        public boolean implies(MapPermission other) {
            return this.level >= other.level;
        }
    }
}