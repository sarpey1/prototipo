package com.megaradar.compat;

import com.megaradar.MegaRadar;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

/**
 * Diagnostica: esegue codice JavaScript dentro il motore Showdown VERO di Cobblemon (contesto Graal),
 * cioè con tutti i dati che Cobblemon e Mega Showdown vi caricano a runtime.
 * Tutto via reflection: nessuna dipendenza di compilazione da Cobblemon.
 */
public final class ShowdownProbe {

    private ShowdownProbe() {}

    /** Battaglia sintetica con 3 Mega dentro il motore reale. */
    public static final String MEGA_TEST_JS = """
            (function () {
              var out = {};
              try {
                var mk = function (sp, item, ab, mv, i) {
                  return { name: sp, species: sp, item: item, ability: ab, moves: [mv], movesInfo: [{ pp: 16, maxPp: 16 }],
                    level: 50, gender: "", nature: "Hardy", evs: { hp: 0, atk: 0, def: 0, spa: 0, spd: 0, spe: 0 },
                    ivs: { hp: 31, atk: 31, def: 31, spa: 31, spd: 31, spe: 31 }, uuid: "00000000-0000-0000-0000-00000000000" + i };
                };
                var team = [mk("Charizard", "charizarditex", "blaze", "dragonclaw", 1),
                            mk("Venusaur", "venusaurite", "overgrow", "gigadrain", 2),
                            mk("Gengar", "gengarite", "cursedbody", "shadowball", 3)];
                var foe = [mk("Chansey", "eviolite", "naturalcure", "splash", 4)];
                var s = new BS.BattleStream();
                var errors = [];
                s.write('>start {"formatid":"gen9customgame","seed":[1,2,3,4]}');
                var b = s.battle;
                var origSend = b.send;
                b.send = function (type, data) {
                  var txt = String(Array.isArray(data) ? data.join("\\n") : data);
                  if (txt.indexOf("|error|") >= 0) errors.push(txt.substring(txt.indexOf("|error|"), txt.indexOf("|error|") + 120));
                  return origSend.apply(this, arguments);
                };
                s.write(">player p1 " + JSON.stringify({ name: "A", team: team }));
                s.write(">player p2 " + JSON.stringify({ name: "B", team: foe }));
                s.write(">p1 team 123"); s.write(">p2 team 1");
                var steps = ["move 1 mega", "switch 2", "move 1 mega", "switch 3", "move 1 mega"];
                var trace = [];
                for (var k = 0; k < steps.length; k++) {
                  trace.push(steps[k] + " <- " + b.sides[0].pokemon.map(function (p) {
                    return p.species.name + "=" + (p.canMegaEvo || "no"); }).join(", "));
                  s.write(">p1 " + steps[k]); s.write(">p2 move 1");
                }
                out.megas = (b.log.join("\\n").match(/\\|-mega\\|/g) || []).length;
                out.patched = typeof b.actions.__megaradarOriginalRunMegaEvo === "function";
                out.errors = errors.slice(0, 4);
                out.trace = trace;
              } catch (e) { out.exception = String((e && e.stack) || e).substring(0, 600); }
              return JSON.stringify(out);
            })()
            """;

    /** Stato delle battaglie in corso: per ogni Pokémon, se il motore gli permette la Mega. */
    public static final String MEGA_STATE_JS = """
            (function () {
              var res = [];
              try {
                battleMap.forEach(function (stream, id) {
                  var b = stream.battle;
                  if (!b) return;
                  var info = { battle: String(id).substring(0, 8), turn: b.turn, format: b.format && b.format.id,
                    patched: typeof b.actions.__megaradarOriginalRunMegaEvo === "function", sides: [] };
                  b.sides.forEach(function (side) {
                    info.sides.push({ side: side.id, hasMegaEvolved: side.hasMegaEvolved, choiceMega: side.choice && side.choice.mega,
                      pokemon: side.pokemon.map(function (p) {
                        return p.species.name + (p.isActive ? "*" : "") + " item=" + (p.item || "-") + " canMegaEvo=" + (p.canMegaEvo || "no");
                      }) });
                  });
                  res.push(info);
                });
              } catch (e) { res.push({ exception: String((e && e.stack) || e).substring(0, 600) }); }
              return JSON.stringify(res.length ? res : "nessuna battaglia in corso");
            })()
            """;

    /** Esegue il JS sul thread di Showdown e passa il risultato (stringa) al callback. */
    public static void run(String js, Consumer<String> callback) {
        try {
            Class<?> cobblemonClass = Class.forName("com.cobblemon.mod.common.Cobblemon");
            Object cobblemon = cobblemonClass.getField("INSTANCE").get(null);
            Object thread = cobblemonClass.getMethod("getShowdownThread").invoke(cobblemon);
            Class<?> function1 = Class.forName("kotlin.jvm.functions.Function1");
            Object unit = Class.forName("kotlin.Unit").getField("INSTANCE").get(null);

            Method queue = null;
            for (Method m : thread.getClass().getMethods()) {
                if (m.getName().equals("queue") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == function1) {
                    queue = m;
                }
            }
            if (queue == null) {
                callback.accept("ERRORE: ShowdownThread.queue non trovato");
                return;
            }

            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "invoke" -> {
                        callback.accept(evalOnService(args[0], js));
                        return unit;
                    }
                    case "hashCode" -> { return System.identityHashCode(proxy); }
                    case "equals" -> { return proxy == args[0]; }
                    case "toString" -> { return "megaradar-showdown-probe"; }
                    default -> { return null; }
                }
            };
            Object fn = Proxy.newProxyInstance(ShowdownProbe.class.getClassLoader(), new Class<?>[]{function1}, handler);
            queue.invoke(thread, fn);
        } catch (Throwable t) {
            MegaRadar.LOGGER.error("Sonda Showdown fallita", t);
            callback.accept("ERRORE: " + t);
        }
    }

    private static String evalOnService(Object service, String js) {
        try {
            Object context = service.getClass().getMethod("getContext").invoke(service);
            Method eval = context.getClass().getMethod("eval", String.class, CharSequence.class);
            Object value = eval.invoke(context, "js", js);
            return String.valueOf(value.getClass().getMethod("asString").invoke(value));
        } catch (Throwable t) {
            Throwable cause = t.getCause() != null ? t.getCause() : t;
            return "ERRORE nel motore (" + service.getClass().getSimpleName() + "): " + cause;
        }
    }
}
