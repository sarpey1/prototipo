package com.megaradar.fuel;

import com.megaradar.MegaRadar;
import com.megaradar.config.RadarConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestione del carburante dei radar.
 * Nessun import client-only qui: questa classe è caricata anche sul server dedicato.
 */
public final class FuelManager {

    /** Copia ricevuta dal server via FuelSyncPayload (solo lato client, per il tooltip). */
    private static volatile Map<ResourceLocation, Integer> syncedFromServer = null;

    private FuelManager() {}

    public record FuelEntry(Item item, int count) {}

    /** Costo letto dalla config del server (ignora item non registrati). */
    public static Map<ResourceLocation, Integer> serverFuel() {
        Map<ResourceLocation, Integer> out = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : RadarConfig.get().fuelPerUse.entrySet()) {
            ResourceLocation rl = ResourceLocation.tryParse(e.getKey());
            if (rl == null || BuiltInRegistries.ITEM.getOptional(rl).isEmpty()) {
                MegaRadar.LOGGER.warn("Carburante ignorato: item sconosciuto '{}'", e.getKey());
                continue;
            }
            out.put(rl, e.getValue());
        }
        return out;
    }

    public static void setSynced(Map<ResourceLocation, Integer> fuel) {
        syncedFromServer = fuel == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(fuel));
    }

    /** Per il tooltip: preferisce i valori del server, altrimenti la config locale. */
    public static List<FuelEntry> displayFuel() {
        Map<ResourceLocation, Integer> src = syncedFromServer != null ? syncedFromServer : serverFuel();
        return toEntries(src);
    }

    private static List<FuelEntry> toEntries(Map<ResourceLocation, Integer> src) {
        List<FuelEntry> list = new ArrayList<>();
        src.forEach((rl, count) -> {
            Item item = BuiltInRegistries.ITEM.get(rl);
            if (item != Items.AIR) list.add(new FuelEntry(item, count));
        });
        return list;
    }

    /** Restituisce cosa manca nell'inventario (lista vuota = carburante sufficiente). */
    public static List<FuelEntry> missing(Player player) {
        List<FuelEntry> missing = new ArrayList<>();
        Inventory inv = player.getInventory();
        for (FuelEntry req : toEntries(serverFuel())) {
            int have = inv.countItem(req.item());
            if (have < req.count()) {
                missing.add(new FuelEntry(req.item(), req.count() - have));
            }
        }
        return missing;
    }

    /** Rimuove il carburante. Chiamare SOLO dopo aver verificato con missing(). */
    public static void consume(Player player) {
        Inventory inv = player.getInventory();
        for (FuelEntry req : toEntries(serverFuel())) {
            Item item = req.item();
            inv.clearOrCountMatchingItems(stack -> stack.is(item), req.count(),
                    player.inventoryMenu.getCraftSlots());
        }
        inv.setChanged();
        player.containerMenu.broadcastChanges();
    }
}
