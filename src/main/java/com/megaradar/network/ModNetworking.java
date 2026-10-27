package com.megaradar.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * Rete:
 * - C2S: non serve. L'uso del radar viaggia col pacchetto vanilla "use item" e il server è autoritativo.
 * - S2C bersagli: non serve un pacchetto dedicato. I bersagli sono salvati nell'item (custom_data)
 *   e Minecraft sincronizza l'inventario al client automaticamente.
 * - S2C FuelSyncPayload: il costo del carburante della config del server, per il tooltip del client.
 */
public final class ModNetworking {

    private ModNetworking() {}

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playS2C().register(FuelSyncPayload.TYPE, FuelSyncPayload.CODEC);
    }

    public static void sendFuelSync(ServerPlayer player, Map<ResourceLocation, Integer> fuel) {
        if (ServerPlayNetworking.canSend(player, FuelSyncPayload.TYPE)) {
            ServerPlayNetworking.send(player, new FuelSyncPayload(fuel));
        }
    }
}
