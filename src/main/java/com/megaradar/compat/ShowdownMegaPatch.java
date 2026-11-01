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
    static final String CTOR_MARK = "/* megaradar:unlimited-megas-ctor */";
    static final String CTOR_END = "/* end megaradar:unlimited-megas-ctor */";

    private static final Pattern DEFINITION = Pattern.compile("(?m)^([ \\t]*)runMegaEvo\\(pokemon\\)\\s*\\{");
    private static final Pattern PATCHED = Pattern.compile(
            "(?s)([ \\t]*)runMegaEvo\\(pokemon\\) \\{ " + Pattern.quote(MARK) + ".*?" + ORIGINAL_NAME + "\\(pokemon\\) \\{");
    private static final Pattern CONSTRUCTOR = Pattern.compile("(?m)^([ \\t]*)constructor\\(battle\\)\\s*\\{");
    private static final Pattern CTOR_PATCHED = Pattern.compile(
            "\\n[ \\t]*" + Pattern.quote(CTOR_MARK) + "[\\s\\S]*?" + Pattern.quote(CTOR_END));
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
            return isFullyPatched(Files.readString(ENGINE_FILE, StandardCharsets.UTF_8)) ? Status.ACTIVE : Status.NOT_PATCHED;
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
        String out;

        if (!enabled) {
            out = PATCHED.matcher(js).replaceFirst("$1runMegaEvo(pokemon) {");
            out = CTOR_PATCHED.matcher(out).replaceFirst("");
            if (out.equals(js)) return Result.ALREADY_PATCHED;
            Files.writeString(file, out, StandardCharsets.UTF_8);
            return Result.UNPATCHED;
        }

        out = js;
        if (!out.contains(MARK)) out = patchMethod(out);
        if (!out.contains(CTOR_MARK)) out = patchConstructor(out);
        if (out == null) return Result.NO_METHOD;
        if (out.equals(js)) return Result.ALREADY_PATCHED;
        Files.writeString(file, out, StandardCharsets.UTF_8);
        return Result.PATCHED;
    }

    /** true se il file contiene entrambe le modifiche. */
    public static boolean isFullyPatched(String js) {
        return js.contains(MARK) && js.contains(CTOR_MARK);
    }

    /** Patch 1: avvolge il metodo runMegaEvo della classe BattleActions. */
    private static String patchMethod(String js) {
        if (js == null) return null;
        Matcher m = DEFINITION.matcher(js);
        if (!m.find()) return null;
        String indent = m.group(1);
        String wrapper = indent + "runMegaEvo(pokemon) { " + MARK + "\n"
                + indent + "  const side = pokemon.side;\n"
                + indent + "  const wasMega = !!pokemon.canMegaEvo;\n"
                + indent + "  const saved = side.pokemon.filter((p) => p !== pokemon).map((p) => [p, p.canMegaEvo]);\n"
                + indent + "  const result = this." + ORIGINAL_NAME + "(pokemon);\n"
                + indent + "  if (result && wasMega) {\n"
                + indent + "    if (\"hasMegaEvolved\" in side) side.hasMegaEvolved = false;\n"
                + indent + "    for (const [ally, canMega] of saved) ally.canMegaEvo = canMega;\n"
                + indent + "  }\n"
                + indent + "  return result;\n"
                + indent + "}\n"
                + indent + ORIGINAL_NAME + "(pokemon) {";
        return js.substring(0, m.start()) + wrapper + js.substring(m.end());
    }

    /**
     * Patch 2: alla fine del costruttore di BattleActions, cioè DOPO che Cobblemon ha copiato sull'istanza
     * gli script delle mod (Scripts.actions, es. quello di zamega che ridefinisce runMegaEvo),
     * avvolge il runMegaEvo effettivo. Così il limite viene tolto qualunque mod fornisca runMegaEvo.
     */
    private static String patchConstructor(String js) {
        if (js == null) return null;
        Matcher c = CONSTRUCTOR.matcher(js);
        if (!c.find()) return js; // costruttore non trovato: resta la sola patch 1
        String indent = c.group(1);
        int close = js.indexOf("\n" + indent + "}", c.end());
        if (close < 0) return js;
        String in = indent + "  ";
        String block = "\n" + in + CTOR_MARK + " {\n"
                + in + "  const inner = this.runMegaEvo;\n"
                + in + "  if (typeof inner === \"function\" && !inner.__megaradarUnlimited) {\n"
                + in + "    const wrapped = function (pokemon) {\n"
                + in + "      const side = pokemon.side;\n"
                + in + "      const wasMega = !!pokemon.canMegaEvo;\n"
                + in + "      const saved = side.pokemon.filter((p) => p !== pokemon).map((p) => [p, p.canMegaEvo]);\n"
                + in + "      const result = inner.call(this, pokemon);\n"
                + in + "      if (result && wasMega) {\n"
                + in + "        if (\"hasMegaEvolved\" in side) side.hasMegaEvolved = false;\n"
                + in + "        for (const [ally, canMega] of saved) ally.canMegaEvo = canMega;\n"
                + in + "      }\n"
                + in + "      return result;\n"
                + in + "    };\n"
                + in + "    wrapped.__megaradarUnlimited = true;\n"
                + in + "    this.runMegaEvo = wrapped;\n"
                + in + "  }\n"
                + in + "} " + CTOR_END;
        return js.substring(0, close) + block + js.substring(close);
    }

    /** Per i test: java ShowdownMegaPatch <file> <true|false> */
    public static void main(String[] args) throws IOException {
        System.out.println(apply(Path.of(args[0]), Boolean.parseBoolean(args[1])));
    }
}
