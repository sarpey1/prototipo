/* Battaglia con 3 Mega sul motore Showdown generato da Cobblemon + Mega Showdown (+ Mega Radar). */
const path = require("path");
const root = path.resolve(process.argv[2]);
const report = (k, v) => console.log(k, typeof v === "string" ? v : JSON.stringify(v));
let BS, Teams;
try { BS = require(path.join(root, "sim/battle-stream")); } catch (e) { report("require battle-stream FALLITO:", String(e.stack).slice(0, 1500)); process.exit(0); }
try { Teams = require(path.join(root, "sim/teams")).Teams; } catch (e) { report("require teams:", String(e)); }
const fs = require("fs");
const ba = fs.readFileSync(path.join(root, "sim/battle-actions.js"), "utf8");
report("marker presente:", ba.includes("megaradar:unlimited-megas"));
async function battle(format) {
  const mons = [["Charizard", "Charizardite X", "dragonclaw"], ["Venusaur", "Venusaurite", "gigadrain"], ["Blastoise", "Blastoisinite", "surf"]];
  const team = mons.map(([s, i, m]) => `${s}||${i.toLowerCase().replace(/ /g, "")}|${s === "Charizard" ? "blaze" : s === "Venusaur" ? "overgrow" : "torrent"}|${m}||||||100|`).join("]");
  const foe = "Chansey||eviolite|naturalcure|splash||||||100|";
  const stream = new BS.BattleStream(); const out = [];
  (async () => { for await (const c of stream) out.push(c); })();
  const send = (s) => stream.write(s);
  try {
    send(`>start {"formatid":"${format}","seed":[1,2,3,4]}`);
    send(`>player p1 {"name":"A","team":"${team}"}`); send(`>player p2 {"name":"B","team":"${foe}"}`);
    send(">p1 team 123"); send(">p2 team 1"); await new Promise(r => setTimeout(r, 50));
    for (const s of ["move 1 mega", "switch 2", "move 1 mega", "switch 3", "move 1 mega"]) {
      send(`>p1 ${s}`); send(">p2 move 1"); await new Promise(r => setTimeout(r, 50));
    }
  } catch (e) { out.push("EXC " + e.stack); }
  await new Promise(r => setTimeout(r, 200));
  const log = out.join("\n");
  return { megas: (log.match(/\|-mega\|p1a: (\w+)/g) || []).map(m => m.split(": ")[1]), errors: (log.match(/\|error\|[^\n]*/g) || []).slice(0, 6), tail: log.slice(0, 1200) };
}
(async () => {
  for (const f of ["gen9customgame", "gen7customgame", "gen9cobblemonsingles"]) {
    try { report(f + ":", await battle(f)); } catch (e) { report(f + " EXC:", String(e.stack).slice(0, 800)); }
  }
  process.exit(0);
})();
