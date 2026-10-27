package com.megaradar.radar;

import com.megaradar.MegaRadar;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Scansione lato SERVER, più potente di /locate:
 * - restituisce le N strutture più vicine invece di una sola;
 * - salta quelle che il giocatore ha già raggiunto (predicate {@code excluded}).
 *
 * Per i posizionamenti "random_spread" (quello di Megaroid e Megasite) percorre la griglia
 * delle posizioni candidate ad anelli crescenti, come fa il vanilla, ma senza fermarsi alla prima.
 * Funziona anche su chunk non ancora generati (calcola solo gli "structure starts").
 */
public final class StructureScanner {

    private StructureScanner() {}

    /** Bersagli: tag datapack megaradar:<tipo>_targets + ID extra da config. */
    public static List<Holder<Structure>> targets(ServerLevel level, RadarType type) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        List<Holder<Structure>> holders = new ArrayList<>();

        registry.getTag(type.targetTag()).ifPresent(named -> named.forEach(holders::add));

        for (String raw : type.extraStructuresFromConfig()) {
            ResourceLocation rl = ResourceLocation.tryParse(raw);
            if (rl == null) {
                MegaRadar.LOGGER.warn("ID struttura non valido in config: '{}'", raw);
                continue;
            }
            registry.getHolder(ResourceKey.create(Registries.STRUCTURE, rl)).ifPresentOrElse(
                    h -> { if (!holders.contains(h)) holders.add(h); },
                    () -> MegaRadar.LOGGER.warn("Struttura '{}' non registrata (mod mancante?)", raw));
        }
        return holders;
    }

    public static List<BlockPos> findNearest(ServerLevel level, List<Holder<Structure>> targets, BlockPos origin,
                                             int radiusChunks, int maxResults, Predicate<BlockPos> excluded) {
        List<BlockPos> results = new ArrayList<>();
        if (targets.isEmpty() || maxResults <= 0) return results;

        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        StructureManager structureManager = level.structureManager();

        // Raggruppa le strutture per tipo di posizionamento
        Map<StructurePlacement, Set<Holder<Structure>>> byPlacement = new LinkedHashMap<>();
        for (Holder<Structure> holder : targets) {
            for (StructurePlacement placement : state.getPlacementsForStructure(holder)) {
                byPlacement.computeIfAbsent(placement, p -> new LinkedHashSet<>()).add(holder);
            }
        }

        Set<Long> seen = new HashSet<>();
        long t0 = System.nanoTime();

        for (Map.Entry<StructurePlacement, Set<Holder<Structure>>> entry : byPlacement.entrySet()) {
            if (entry.getKey() instanceof RandomSpreadStructurePlacement spread) {
                scanRandomSpread(level, structureManager, state, spread, entry.getValue(), origin,
                        radiusChunks, maxResults, excluded, results, seen);
            } else {
                // Altri posizionamenti (es. anelli concentrici): fallback alla ricerca vanilla.
                Pair<BlockPos, Holder<Structure>> r = level.getChunkSource().getGenerator().findNearestMapStructure(
                        level, HolderSet.direct(new ArrayList<>(entry.getValue())), origin, radiusChunks, false);
                if (r != null && !excluded.test(r.getFirst())
                        && seen.add(ChunkPos.asLong(r.getFirst().getX() >> 4, r.getFirst().getZ() >> 4))) {
                    results.add(r.getFirst());
                }
            }
        }

        results.sort(Comparator.comparingDouble(p -> horizontalDistSqr(p, origin)));
        if (results.size() > maxResults) results.subList(maxResults, results.size()).clear();

        MegaRadar.LOGGER.debug("Scansione radar: {} risultati in {} ms", results.size(),
                (System.nanoTime() - t0) / 1_000_000);
        return results;
    }

    private static void scanRandomSpread(ServerLevel level, StructureManager structureManager,
                                         ChunkGeneratorStructureState state, RandomSpreadStructurePlacement placement,
                                         Set<Holder<Structure>> structures, BlockPos origin, int radiusChunks,
                                         int maxResults, Predicate<BlockPos> excluded,
                                         List<BlockPos> results, Set<Long> seen) {
        int spacing = Math.max(1, placement.spacing());
        long seed = state.getLevelSeed();
        int centerX = SectionPos.blockToSectionCoord(origin.getX());
        int centerZ = SectionPos.blockToSectionCoord(origin.getZ());
        int maxRing = Math.max(1, radiusChunks / spacing);

        int found = 0;
        int stopRing = Integer.MAX_VALUE;

        for (int ring = 0; ring <= maxRing && ring <= stopRing; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue; // solo il bordo dell'anello

                    ChunkPos candidate = placement.getPotentialStructureChunk(seed,
                            centerX + spacing * dx, centerZ + spacing * dz);
                    BlockPos pos = structureAt(level, structureManager, placement, structures, candidate);
                    if (pos == null) continue;
                    if (!seen.add(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4))) continue;
                    if (excluded.test(pos)) continue;

                    results.add(pos);
                    found++;
                    // Un anello in più garantisce di non perdere strutture più vicine in diagonale.
                    if (found >= maxResults && stopRing == Integer.MAX_VALUE) stopRing = ring + 1;
                }
            }
        }
    }

    /** Stessa verifica che fa il vanilla in ChunkGenerator#getStructureGeneratedAt. */
    private static BlockPos structureAt(ServerLevel level, StructureManager structureManager,
                                        StructurePlacement placement, Set<Holder<Structure>> structures,
                                        ChunkPos chunkPos) {
        for (Holder<Structure> holder : structures) {
            StructureCheckResult check = structureManager.checkStructurePresence(chunkPos, holder.value(), placement, false);
            if (check == StructureCheckResult.START_NOT_PRESENT) continue;
            if (check == StructureCheckResult.START_PRESENT) return placement.getLocatePos(chunkPos);

            ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart start = structureManager.getStartForStructure(SectionPos.bottomOf(chunk), holder.value(), chunk);
            if (start != null && start.isValid()) return placement.getLocatePos(start.getChunkPos());
        }
        return null;
    }

    public static double horizontalDistSqr(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }
}
