package com.megaradar.fuel;

import com.megaradar.MegaRadar;
import com.megaradar.config.RadarConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Carburante dei radar. Il carburante si inserisce negli slot della GUI a decagono:
 * uno slot per minerale, quindi al massimo {@link #MAX_SLOTS} voci.
 * Nessun import client-only qui: questa classe è caricata anche sul server dedicato.
 */
public final class FuelManager {

    /** Il decagono ha 10 vertici = 10 slot. */
    public static final int MAX_SLOTS = 10;

    /** Copia ricevuta dal server via FuelSyncPayload (solo lato client, per il tooltip). */
    private static volatile Map<ResourceLocation, Integer> syncedFromServer = null;

    private FuelManager() {}

    public record FuelEntry(Item item, int count) {}

    /** Requisito di uno slot, inviato al client all'apertura della GUI. */
    public record FuelReq(ResourceLocation id, int count) {
        public static final StreamCodec<ByteBuf, FuelReq> CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, FuelReq::id,
                ByteBufCodecs.VAR_INT, FuelReq::count,
                FuelReq::new);
        public static final StreamCodec<ByteBuf, List<FuelReq>> LIST_CODEC = CODEC.apply(ByteBufCodecs.list(MAX_SLOTS));
    }

    /**
     * Costo letto dalla config del server: solo item esistenti, al massimo 10 voci,
     * quantità limitata alla dimensione massima dello stack (uno slot = uno stack).
     */
    public static Map<ResourceLocation, Integer> serverFuel() {
        Map<ResourceLocation, Integer> out = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : RadarConfig.get().fuelPerUse.entrySet()) {
            if (out.size() >= MAX_SLOTS) {
                MegaRadar.LOGGER.warn("Carburante: più di {} voci in config, le altre sono ignorate", MAX_SLOTS);
                break;
            }
            ResourceLocation rl = ResourceLocation.tryParse(e.getKey());
            if (rl == null || BuiltInRegistries.ITEM.getOptional(rl).isEmpty()) {
                MegaRadar.LOGGER.warn("Carburante ignorato: item sconosciuto '{}'", e.getKey());
                continue;
            }
            int max = new ItemStack(BuiltInRegistries.ITEM.get(rl)).getMaxStackSize();
            out.put(rl, Math.max(1, Math.min(e.getValue(), max)));
        }
        return out;
    }

    public static List<FuelReq> guiFuel() {
        List<FuelReq> list = new ArrayList<>();
        serverFuel().forEach((rl, count) -> list.add(new FuelReq(rl, count)));
        return list;
    }

    public static void setSynced(Map<ResourceLocation, Integer> fuel) {
        syncedFromServer = fuel == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(fuel));
    }

    /** Per il tooltip: preferisce i valori del server, altrimenti la config locale. */
    public static List<FuelEntry> displayFuel() {
        Map<ResourceLocation, Integer> src = syncedFromServer != null ? syncedFromServer : serverFuel();
        List<FuelEntry> list = new ArrayList<>();
        src.forEach((rl, count) -> {
            Item item = BuiltInRegistries.ITEM.get(rl);
            if (item != Items.AIR) list.add(new FuelEntry(item, count));
        });
        return list;
    }
}
