package com.megaradar.compat;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mega evoluzioni illimitate in battaglia: modifica il motore Showdown su disco.
 *
 * Cobblemon esegue le battaglie con i file JavaScript in ./showdown, che Mega Showdown sovrascrive
 * con le sue versioni. In showdown/sim/battle-actions.js il metodo runMegaEvo, dopo la prima Mega,
 * toglie la possibilità di megaevolversi a tutto il resto della squadra.
 *
 * All'avvio (preLaunch, prima che Cobblemon carichi il motore) questo patcher avvolge runMegaEvo:
 * l'originale viene eseguito, poi agli altri Pokémon viene restituita la loro Mega.
 * Ogni Pokémon resta comunque limitato a una Mega a battaglia.
 *
 * La modifica è idempotente e reversibile: con "unlimitedMegasInBattle": false in
 * config/megaradar_megas.json il file viene riportato all'originale al lancio successivo.
 */
public final class ShowdownMegaPatch implements PreLaunchEntrypoint {

    static final String MARK = "/* megaradar:unlimited-megas */";
    static final String ORIGINAL_NAME = "__megaradarOriginalRunMegaEvo";

    private static final Pattern DEFINITION = Pattern.compile("(?m)^([ \\t]*)runMegaEvo\\(pokemon\\)\\s*\\{");
    private static final Pattern PATCHED = Pattern.compile(
            "(?s)([ \\t]*)runMegaEvo\\(pokemon\\) \\{ " + Pattern.quote(MARK) + ".*?" + ORIGINAL_NAME + "\\(pokemon\\) \\{");
    private static final Pattern ENABLED = Pattern.compile("\"unlimitedMegasInBattle\"\\s*:\\s*(true|false)");

    public enum Result { PATCHED, ALREADY_PATCHED, UNPATCHED, NOT_FOUND, NO_METHOD }

    @Override
    public void onPreLaunch() {
        applyNow("all'avvio");
    }

    /**
     * Applica (o rimuove) la modifica adesso. Chiamato all'avvio e dai mixin subito dopo che
     * Cobblemon / Mega Showdown hanno riscritto i file, prima che il motore venga caricato.
     */
    public static synchronized void applyNow(String when) {
        Logger log = LoggerFactory.getLogger("megaradar");
        try {
            boolean enabled = readEnabled(CONFIG_FILE);
            Path file = ENGINE_FILE;
            Result r = apply(file, enabled);
            switch (r) {
                case PATCHED -> log.info("Mega illimitate: motore Showdown modificato " + when + " (" + file.toAbsolutePath() + ")");
                case ALREADY_PATCHED -> log.info("Mega illimitate: motore Showdown già modificato (" + when + ")");
                case UNPATCHED -> log.info("Mega illimitate disattivate: motore Showdown ripristinato");
                case NOT_FOUND -> log.info("Mega illimitate: " + file.toAbsolutePath()
                        + " non esiste ancora (" + when + ")");
                case NO_METHOD -> log.warn("Mega illimitate: runMegaEvo non trovato in " + file.toAbsolutePath());
            }
        } catch (Throwable t) {
            log.error("Mega illimitate: impossibile modificare il motore Showdown", t);
        }
    }

    public enum Status { ACTIVE, NOT_PATCHED, NO_FILE, DISABLED }

    public static final Path ENGINE_FILE = Path.of("showdown", "sim", "battle-actions.js");
    public static final Path CONFIG_FILE = Path.of("config", "megaradar_megas.json");

    /** Stato attuale della modifica sul motore Showdown (per il messaggio in chat e /megaradar megas). */
    public static Status status() {
        try {
            if (!readEnabled(CONFIG_FILE)) return Status.DISABLED;
            if (!Files.exists(ENGINE_FILE)) return Status.NO_FILE;
            return Files.readString(ENGINE_FILE, StandardCharsets.UTF_8).contains(MARK) ? Status.ACTIVE : Status.NOT_PATCHED;
        } catch (IOException e) {
            return Status.NO_FILE;
        }
    }

    static boolean readEnabled(Path config) throws IOException {
        if (!Files.exists(config)) {
            Files.createDirectories(config.getParent());
            Files.writeString(config, "{\n  \"unlimitedMegasInBattle\": true\n}\n", StandardCharsets.UTF_8);
            return true;
        }
        Matcher m = ENABLED.matcher(Files.readString(config, StandardCharsets.UTF_8));
        return !m.find() || Boolean.parseBoolean(m.group(1));
    }

    public static Result apply(Path file, boolean enabled) throws IOException {
        if (!Files.exists(file)) return Result.NOT_FOUND;
        String js = Files.readString(file, StandardCharsets.UTF_8);
        boolean patched = js.contains(MARK);

        if (!enabled) {
            if (!patched) return Result.ALREADY_PATCHED;
            String restored = PATCHED.matcher(js).replaceFirst("$1runMegaEvo(pokemon) {");
            Files.writeString(file, restored, StandardCharsets.UTF_8);
            return Result.UNPATCHED;
        }
        if (patched) return Result.ALREADY_PATCHED;

        Matcher m = DEFINITION.matcher(js);
        if (!m.find()) return Result.NO_METHOD;
        String indent = m.group(1);
        String wrapper = indent + "runMegaEvo(pokemon) { " + MARK + "\n"
                + indent + "  const side = pokemon.side;\n"
                + indent + "  const wasMega = !!pokemon.canMegaEvo;\n"
                + indent + "  const saved = side.pokemon.filter((p) => p !== pokemon).map((p) => [p, p.canMegaEvo]);\n"
                + indent + "  const result = this." + ORIGINAL_NAME + "(pokemon);\n"
                + indent + "  if (result && wasMega) {\n"
                + indent + "    if (\"hasMegaEvolved\" in side) side.hasMegaEvolved = false;\n"
                + indent + "    for (const [ally, canMega] of saved) ally.canMegaEvo = canMega;\n"
                + indent + "    try { console.log(\"[megaradar] Mega illimitate: \" + pokemon.name + \" si e' megaevoluto, gli altri possono ancora farlo\"); } catch (e) {}\n"
                + indent + "  }\n"
                + indent + "  return result;\n"
                + indent + "}\n"
                + indent + ORIGINAL_NAME + "(pokemon) {";
        String out = js.substring(0, m.start()) + wrapper + js.substring(m.end());
        Files.writeString(file, out, StandardCharsets.UTF_8);
        return Result.PATCHED;
    }

    /** Per i test: java ShowdownMegaPatch <file> <true|false> */
    public static void main(String[] args) throws IOException {
        System.out.println(apply(Path.of(args[0]), Boolean.parseBoolean(args[1])));
    }
}
