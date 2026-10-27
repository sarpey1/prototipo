package com.megaradar.client;

import com.megaradar.fuel.FuelManager;
import com.megaradar.network.FuelSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/** Entrypoint solo client: ricevitore del payload S2C e rendering dell'HUD. */
public class MegaRadarClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(FuelSyncPayload.TYPE, (payload, context) ->
                context.client().execute(() -> FuelManager.setSynced(payload.fuel())));

        HudRenderCallback.EVENT.register(RadarHud::render);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FuelManager.setSynced(null));
    }
}
