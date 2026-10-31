/* Riproduce receiveScriptData di Mega Showdown (index.js) e prova una battaglia con 3 Mega. */
const fs = require("fs");
const { Dex, BattleStream, getPlayerStreams, Teams } = require("pokemon-showdown");

const FORMAT = "gen7customgame";
const scripts = { Scripts: Dex.forFormat(FORMAT).data.Scripts };
function receiveScriptData(scriptId, scriptData) {
  const newFunctions = eval(`(${scriptData})`);
  if (!scripts.Scripts[scriptId]) scripts.Scripts[scriptId] = {};
  Object.assign(scripts.Scripts[scriptId], newFunctions);
}

async function battle(withScript, reloads) {
  if (withScript) {
    const js = fs.readFileSync(process.argv[2], "utf8").replace(/\n/g, " ");  /* come Scripts.java */
    for (let i = 0; i < reloads; i++) receiveScriptData("actions", js);
  } else if (scripts.Scripts.actions) {
    delete scripts.Scripts.actions.runMegaEvo;
  }
  const p1 = Teams.pack([
    { species: "Charizard", item: "Charizardite X", moves: ["dragonclaw"], level: 100, ability: "Blaze", evs: {hp:252} },
    { species: "Venusaur", item: "Venusaurite", moves: ["gigadrain"], level: 100, ability: "Overgrow", evs: {hp:252} },
    { species: "Blastoise", item: "Blastoisinite", moves: ["surf"], level: 100, ability: "Torrent", evs: {hp:252} },
  ]);
  const p2 = Teams.pack([{ species: "Chansey", item: "Eviolite", moves: ["splash"], level: 100, ability: "Natural Cure", evs: {hp:252, def:252, spd:4} }]);
  const stream = new BattleStream();
  const out = [];
  (async () => { for await (const chunk of stream) out.push(chunk); })();
  const send = (s) => stream.write(s);
  send(`>start {"formatid":"${FORMAT}","seed":[1,2,3,4]}`);
  send(`>player p1 {"name":"A","team":"${p1}"}`);
  send(`>player p2 {"name":"B","team":"${p2}"}`);
  send(">p1 team 123"); send(">p2 team 1");
  await new Promise(r => setTimeout(r, 30));
  const steps = ["move 1 mega", "switch 2", "move 1 mega", "switch 3", "move 1 mega"];
  for (const s of steps) {
    send(`>p1 ${s}`); send(`>p2 move 1`);
    await new Promise(r => setTimeout(r, 30));
  }
  await new Promise(r => setTimeout(r, 100));
  const log = out.join("\n");
  const megas = (log.match(/\|-mega\|p1a: (\w+)/g) || []).map(m => m.split(": ")[1]);
  const errors = (log.match(/\|error\|[^\n]*/g) || []);
  return { megas, errors };
}

(async () => {
  const before = await battle(false, 0);
  console.log("SENZA script :", JSON.stringify(before));
  const after = await battle(true, 1);
  console.log("CON script   :", JSON.stringify(after));
  const reloaded = await battle(true, 3);
  console.log("dopo 3 reload:", JSON.stringify(reloaded));
  const ok = before.megas.length === 1 && after.megas.length === 3 && reloaded.megas.length === 3;
  console.log(ok ? "TEST OK" : "TEST FALLITO");
  process.exit(ok ? 0 : 1);
})();
