package com.megaradar.data;

import com.megaradar.radar.RadarType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Strutture già raggiunte (o saltate) da ogni giocatore, salvate nel mondo
 * (data/megaradar_visited.dat). Il radar le esclude dalle scansioni successive,
 * così non punta più sempre alla stessa struttura.
 */
public class VisitedData extends SavedData {

    private static final String NAME = "megaradar_visited";

    /** player -> ("tipo|dimensione") -> chunk delle strutture visitate */
    private final Map<UUID, Map<String, Set<Long>>> visited = new HashMap<>();

    public static VisitedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(VisitedData::new, VisitedData::load, null), NAME);
    }

    private static String key(RadarType type, ResourceLocation dimension) {
        return type.id() + "|" + dimension;
    }

    private static long chunkKey(BlockPos pos) {
        return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public boolean isVisited(UUID player, RadarType type, ResourceLocation dimension, BlockPos pos) {
        Map<String, Set<Long>> byKey = visited.get(player);
        if (byKey == null) return false;
        Set<Long> set = byKey.get(key(type, dimension));
        return set != null && set.contains(chunkKey(pos));
    }

    public void markVisited(UUID player, RadarType type, ResourceLocation dimension, BlockPos pos) {
        if (visited.computeIfAbsent(player, p -> new HashMap<>())
                .computeIfAbsent(key(type, dimension), k -> new HashSet<>())
                .add(chunkKey(pos))) {
            setDirty();
        }
    }

    public int count(UUID player) {
        Map<String, Set<Long>> byKey = visited.get(player);
        return byKey == null ? 0 : byKey.values().stream().mapToInt(Set::size).sum();
    }

    public void reset(UUID player) {
        if (visited.remove(player) != null) setDirty();
    }

    // ---- persistenza ----

    private static VisitedData load(CompoundTag tag, HolderLookup.Provider registries) {
        VisitedData data = new VisitedData();
        CompoundTag players = tag.getCompound("players");
        for (String uuidStr : players.getAllKeys()) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                continue;
            }
            CompoundTag perPlayer = players.getCompound(uuidStr);
            Map<String, Set<Long>> byKey = new HashMap<>();
            for (String k : perPlayer.getAllKeys()) {
                Set<Long> set = new HashSet<>();
                for (long l : perPlayer.getLongArray(k)) set.add(l);
                byKey.put(k, set);
            }
            data.visited.put(uuid, byKey);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag players = new CompoundTag();
        visited.forEach((uuid, byKey) -> {
            CompoundTag perPlayer = new CompoundTag();
            byKey.forEach((k, set) -> perPlayer.putLongArray(k, set.stream().mapToLong(Long::longValue).toArray()));
            players.put(uuid.toString(), perPlayer);
        });
        tag.put("players", players);
        return tag;
    }
}
