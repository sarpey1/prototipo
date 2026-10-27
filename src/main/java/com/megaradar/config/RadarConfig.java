package com.megaradar.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megaradar.MegaRadar;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Config JSON in config/megaradar.json.
 * Il server è autoritativo: carburante, raggio e cooldown si decidono qui.
 */
public final class RadarConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("megaradar.json");
    private static RadarConfig instance = new RadarConfig();

    // ---- Valori (con default) ----

    /** Raggio di ricerca in chunk (come /locate: 100). Valori alti = più lag al momento della scansione. */
    public int searchRadiusChunks = 100;

    /** Cooldown dopo ogni scansione, in tick (20 tick = 1 secondo). */
    public int cooldownTicks = 200;

    /** Se true, il carburante viene consumato solo quando il radar trova qualcosa. */
    public boolean consumeFuelOnlyIfFound = true;

    /** Quante strutture (le più vicine non ancora visitate) ogni scansione memorizza nel radar. */
    public int resultsPerScan = 3;

    /** Distanza orizzontale (blocchi) entro cui una struttura conta come "raggiunta" e viene esclusa in futuro. */
    public int arrivalRadius = 24;

    /** Carburante per UN utilizzo: itemId -> quantità. Default = uno stack di ogni minerale. */
    public LinkedHashMap<String, Integer> fuelPerUse = defaultFuel();

    /** ID di strutture extra (oltre al tag megaradar:megaroid_targets). */
    public List<String> megaroidExtraStructures = new ArrayList<>();

    /** ID di strutture extra (oltre al tag megaradar:mega_city_targets). */
    public List<String> megaCityExtraStructures = new ArrayList<>();

    private static LinkedHashMap<String, Integer> defaultFuel() {
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        m.put("minecraft:coal", 64);
        m.put("minecraft:copper_ingot", 64);
        m.put("minecraft:iron_ingot", 64);
        m.put("minecraft:gold_ingot", 64);
        m.put("minecraft:redstone", 64);
        m.put("minecraft:lapis_lazuli", 64);
        m.put("minecraft:quartz", 64);
        m.put("minecraft:amethyst_shard", 64);
        m.put("minecraft:emerald", 64);
        m.put("minecraft:diamond", 64);
        return m;
    }

    public static RadarConfig get() {
        return instance;
    }

    public static synchronized void load() {
        try {
            if (Files.exists(PATH)) {
                try (Reader r = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                    RadarConfig loaded = GSON.fromJson(r, RadarConfig.class);
                    if (loaded != null) {
                        instance = loaded.sanitize();
                    }
                }
            } else {
                instance = new RadarConfig();
            }
            save(); // riscrive il file con eventuali campi nuovi
            MegaRadar.LOGGER.info("Config Mega Radar caricata da {}", PATH);
        } catch (Exception e) {
            MegaRadar.LOGGER.error("Impossibile leggere {}: uso i valori di default", PATH, e);
            instance = new RadarConfig();
        }
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer w = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(instance, w);
            }
        } catch (IOException e) {
            MegaRadar.LOGGER.error("Impossibile salvare {}", PATH, e);
        }
    }

    private RadarConfig sanitize() {
        if (searchRadiusChunks < 1) searchRadiusChunks = 1;
        if (searchRadiusChunks > 512) searchRadiusChunks = 512;
        if (cooldownTicks < 0) cooldownTicks = 0;
        if (resultsPerScan < 1) resultsPerScan = 1;
        if (resultsPerScan > 10) resultsPerScan = 10;
        if (arrivalRadius < 4) arrivalRadius = 4;
        if (fuelPerUse == null) fuelPerUse = defaultFuel();
        if (megaroidExtraStructures == null) megaroidExtraStructures = new ArrayList<>();
        if (megaCityExtraStructures == null) megaCityExtraStructures = new ArrayList<>();
        fuelPerUse.values().removeIf(v -> v == null || v <= 0);
        return this;
    }
}
