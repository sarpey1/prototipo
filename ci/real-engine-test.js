/* Battaglia con 3 Mega sul motore Showdown REALE generato da Cobblemon + Mega Showdown (+ Mega Radar). */
const path = require("path");
const fs = require("fs");
const root = path.resolve(process.argv[2]);
const report = (k, v) => console.log(k, typeof v === "string" ? v : JSON.stringify(v));
const BS = require(path.join(root, "sim/battle-stream"));
const ba = fs.readFileSync(path.join(root, "sim/battle-actions.js"), "utf8");
report("marker Mega Radar presente:", ba.includes("megaradar:unlimited-megas"));
try {
  const { Dex } = require(path.join(root, "sim/dex"));
  report("formati cobblemon/custom:", Dex.formats.all().map(f => f.id).filter(id => /cobble|custom/.test(id)).slice(0, 20));
} catch (e) { report("formats:", String(e).slice(0, 300)); }

const set = (species, item, ability, move, i) => ({
  name: species, species, item, ability, moves: [move], movesInfo: [{ pp: 16, maxPp: 16 }],
  level: 100, gender: "", nature: "Hardy", evs: { hp: 252, atk: 0, def: 0, spa: 0, spd: 0, spe: 0 },
  ivs: { hp: 31, atk: 31, def: 31, spa: 31, spd: 31, spe: 31 }, uuid: "00000000-0000-0000-0000-00000000000" + i,
});
const team = [set("Charizard", "Charizardite X", "Blaze", "dragonclaw", 1), set("Venusaur", "Venusaurite", "Overgrow", "gigadrain", 2), set("Blastoise", "Blastoisinite", "Torrent", "surf", 3)];
const foe = [set("Chansey", "Eviolite", "Natural Cure", "splash", 4)];

async function battle(format) {
  const stream = new BS.BattleStream(); const out = [];
  (async () => { for await (const c of stream) out.push(c); })();
  const send = (s) => stream.write(s);
  try {
    send(`>start {"formatid":"${format}","seed":[1,2,3,4]}`);
    send(`>player p1 ${JSON.stringify({ name: "A", team })}`);
    send(`>player p2 ${JSON.stringify({ name: "B", team: foe })}`);
    send(">p1 team 123"); send(">p2 team 1"); await new Promise(r => setTimeout(r, 50));
    for (const s of ["move 1 mega", "switch 2", "move 1 mega", "switch 3", "move 1 mega"]) {
      send(`>p1 ${s}`); send(">p2 move 1"); await new Promise(r => setTimeout(r, 50));
    }
  } catch (e) { out.push("EXC " + String(e.stack).slice(0, 600)); }
  await new Promise(r => setTimeout(r, 200));
  const log = out.join("\n");
  const req = (log.match(/"canMegaEvo":true/g) || []).length;
  return { megas: (log.match(/\|-mega\|p1a: (\w+)/g) || []).map(m => m.split(": ")[1]), canMegaEvoInRequests: req,
           errors: (log.match(/\|error\|[^\n]*/g) || []).slice(0, 6), head: log.slice(0, 400) };
}
(async () => {
  for (const f of ["gen9customgame", "gen7customgame", "gen9cobblemonsingles", "gen9cobblemon"]) {
    try { report(f + ":", await battle(f)); } catch (e) { report(f + " EXC:", String(e.stack).slice(0, 600)); }
  }
  process.exit(0);
})().catch(e => { console.log("EXC", e); process.exit(0); });
