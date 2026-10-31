package com.megaradar.compat;

import com.megaradar.MegaRadar;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Field;

/**
 * Mega evoluzioni illimitate in battaglia (con Mega Showdown).
 *
 * In Mega Showdown il limite "una Mega per battaglia" sta in due punti:
 *  1. motore Showdown (JavaScript): dopo la prima Mega, runMegaEvo toglie la Mega a tutta la squadra.
 *     Lo risolve lo script data/megaradar/mega_showdown/showdown/scripts/actions.js, che Mega Showdown
 *     carica da solo dai datapack e inietta nel motore di battaglia;
 *  2. lato Java: se hai già un Pokémon Mega, Mega Showdown ti toglie la Key Stone (hasMega).
 *     Lo risolve l'opzione di Mega Showdown "multipleMegas", che qui viene attivata in automatico.
 *
 * Entrambe le parti stanno nel datapack integrato "megaradar:unlimited_megas", attivo di default.
 * Per tornare alla regola classica: /datapack disable "megaradar:unlimited_megas"
 */
public final class UnlimitedMegas {

    public static final String PACK_ID = MegaRadar.MOD_ID + ":unlimited_megas";
    private static final String MSD_CONFIG_CLASS = "com.github.yajatkaul.mega_showdown.config.MegaShowdownConfig";
    private static final int CHECK_EVERY_TICKS = 100;

    private static Field multipleMegasField;
    private static boolean lookedUp = false;
    /** Valore originale di multipleMegas prima che lo forzassimo (null = non forzato). */
    private static Boolean originalValue = null;

    private UnlimitedMegas() {}

    public static void register() {
        ModContainer container = FabricLoader.getInstance().getModContainer(MegaRadar.MOD_ID).orElseThrow();
        boolean ok = ResourceManagerHelper.registerBuiltinResourcePack(
                MegaRadar.id("unlimited_megas"), container,
                Component.literal("Mega illimitate (Mega Radar)"),
                ResourcePackActivationType.DEFAULT_ENABLED);
        if (!ok) MegaRadar.LOGGER.warn("Impossibile registrare il datapack {}", PACK_ID);

        // Mega Showdown può (ri)caricare la sua config in qualsiasi momento: ricontrolliamo spesso, costa nulla.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            sync(server);
            if (field() != null) MegaRadar.LOGGER.info("Mega illimitate - stato motore Showdown: {} ({})",
                    ShowdownMegaPatch.status(), ShowdownMegaPatch.ENGINE_FILE.toAbsolutePath());
        });
        // Messaggio in chat all'ingresso: dice subito se le Mega illimitate sono attive
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (field() != null) handler.getPlayer().sendSystemMessage(statusMessage(server));
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, rm, success) -> sync(server));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % CHECK_EVERY_TICKS == 0) sync(server);
        });
    }

    /** Una riga di stato, usata all'ingresso e da /megaradar megas. */
    public static Component statusMessage(MinecraftServer server) {
        if (field() == null) {
            return Component.literal("[Mega Radar] Mega Showdown non trovato: Mega illimitate non disponibili")
                    .withStyle(ChatFormatting.GRAY);
        }
        boolean keyStoneOk = isMultipleMegasOn();
        return switch (ShowdownMegaPatch.status()) {
            case ACTIVE -> keyStoneOk
                    ? Component.literal("[Mega Radar] Mega illimitate ATTIVE: in battaglia puoi megaevolvere tutti i Pokémon con la loro Mega Pietra")
                        .withStyle(ChatFormatting.GREEN)
                    : Component.literal("[Mega Radar] Motore OK, ma multipleMegas di Mega Showdown è spento: il tasto Mega può sparire")
                        .withStyle(ChatFormatting.GOLD);
            case NOT_PATCHED -> Component.literal("[Mega Radar] Mega illimitate NON attive: il file del motore è stato sovrascritto. Riavvia il gioco; se il messaggio resta, mandami logs/latest.log")
                    .withStyle(ChatFormatting.RED);
            case NO_FILE -> Component.literal("[Mega Radar] Mega illimitate NON attive: non trovo " + ShowdownMegaPatch.ENGINE_FILE.toAbsolutePath())
                    .withStyle(ChatFormatting.RED);
            case DISABLED -> Component.literal("[Mega Radar] Mega illimitate disattivate da config/megaradar_megas.json")
                    .withStyle(ChatFormatting.GRAY);
        };
    }

    private static boolean isMultipleMegasOn() {
        try {
            return field() != null && field().getBoolean(null);
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    public static boolean isPackEnabled(MinecraftServer server) {
        return server.getPackRepository().getSelectedIds().contains(PACK_ID);
    }

    private static void sync(MinecraftServer server) {
        Field field = field();
        if (field == null) return;
        try {
            boolean current = field.getBoolean(null);
            if (isPackEnabled(server)) {
                if (!current) {
                    if (originalValue == null) originalValue = false;
                    field.setBoolean(null, true);
                    MegaRadar.LOGGER.info("Mega illimitate attive: multipleMegas di Mega Showdown impostato a true");
                }
            } else if (originalValue != null) {
                field.setBoolean(null, originalValue);
                MegaRadar.LOGGER.info("Mega illimitate disattivate: multipleMegas ripristinato a {}", originalValue);
                originalValue = null;
            }
        } catch (IllegalAccessException e) {
            MegaRadar.LOGGER.error("Impossibile modificare multipleMegas di Mega Showdown", e);
        }
    }

    private static Field field() {
        if (!lookedUp) {
            lookedUp = true;
            try {
                Field f = Class.forName(MSD_CONFIG_CLASS).getField("multipleMegas");
                if (f.getType() == boolean.class) {
                    multipleMegasField = f;
                    MegaRadar.LOGGER.info("Mega Showdown trovato: supporto Mega illimitate pronto");
                }
            } catch (ClassNotFoundException e) {
                MegaRadar.LOGGER.info("Mega Showdown non installato: Mega illimitate non necessarie");
            } catch (NoSuchFieldException e) {
                MegaRadar.LOGGER.warn("Questa versione di Mega Showdown non ha l'opzione multipleMegas");
            }
        }
        return multipleMegasField;
    }
}
