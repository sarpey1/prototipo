package com.megaradar;

import com.megaradar.command.RadarCommand;
import com.megaradar.compat.UnlimitedMegas;
import com.megaradar.config.RadarConfig;
import com.megaradar.fuel.FuelManager;
import com.megaradar.network.ModNetworking;
import com.megaradar.registry.ModItems;
import com.megaradar.registry.ModMenus;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrypoint comune: gira sia sul client fisico che sul server dedicato.
 * Qui si registrano item, tipi di payload e logica server.
 */
public class MegaRadar implements ModInitializer {

    public static final String MOD_ID = "megaradar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        RadarConfig.load();
        ModItems.register();
        ModMenus.register();
        ModNetworking.registerPayloadTypes();
        UnlimitedMegas.register();

        // Quando un giocatore entra, gli mandiamo il costo carburante deciso dal server
        // (così il tooltip lato client è sempre coerente con la config del server).
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                ModNetworking.sendFuelSync(handler.getPlayer(), FuelManager.serverFuel()));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                RadarCommand.register(dispatcher));

        LOGGER.info("Mega Radar inizializzato.");
    }
}
