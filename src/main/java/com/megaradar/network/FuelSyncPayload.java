package com.megaradar.network;

import com.megaradar.MegaRadar;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * S2C: il server comunica al client il costo carburante attuale,
 * così il tooltip mostra i valori della config del SERVER e non quelli locali.
 */
public record FuelSyncPayload(Map<ResourceLocation, Integer> fuel) implements CustomPacketPayload {

    public static final Type<FuelSyncPayload> TYPE = new Type<>(MegaRadar.id("fuel_sync"));

    public static final StreamCodec<ByteBuf, FuelSyncPayload> CODEC =
            ByteBufCodecs.<ByteBuf, ResourceLocation, Integer, Map<ResourceLocation, Integer>>map(
                            LinkedHashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT)
                    .map(FuelSyncPayload::new, FuelSyncPayload::fuel);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
