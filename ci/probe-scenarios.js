(function () {
  var out = {};
  var mk = function (sp, item, ab, mv, i) {
    return { name: sp, species: sp, item: item, ability: ab, moves: [mv], movesInfo: [{ pp: 16, maxPp: 16 }],
      level: 50, gender: "", nature: "Hardy", evs: { hp: 0, atk: 0, def: 0, spa: 0, spd: 0, spe: 0 },
      ivs: { hp: 31, atk: 31, def: 31, spa: 31, spd: 31, spe: 31 }, uuid: "00000000-0000-0000-0000-00000000000" + i };
  };
  var run = function (format, team, steps) {
    var r = { trace: [], errors: [] };
    try {
      var s = new BS.BattleStream();
      s.write('>start {"formatid":"' + format + '","seed":[1,2,3,4]}');
      var b = s.battle; var orig = b.send;
      b.send = function (t, d) { var x = String(Array.isArray(d) ? d.join("\n") : d); var i = x.indexOf("|error|"); if (i >= 0) r.errors.push(x.substring(i, i + 110)); return orig.apply(this, arguments); };
      s.write(">player p1 " + JSON.stringify({ name: "A", team: team }));
      s.write(">player p2 " + JSON.stringify({ name: "B", team: [(function(){ var m = mk("Snorlax", "leftovers", "thickfat", "splash", 9); m.level = 100; return m; })()] }));
      if (b.teamPreview || (b.format && b.format.teamPreview)) {}
      s.write(">p1 team 12"); s.write(">p2 team 1");
      r.ownRunMegaEvo = Object.prototype.hasOwnProperty.call(b.actions, "runMegaEvo");
      r.runMegaEvoSrc = String(b.actions.runMegaEvo).substring(0, 160);
      for (var k = 0; k < steps.length; k++) {
        r.trace.push(steps[k] + " <- " + b.sides[0].pokemon.map(function (p) { return p.species.name + (p.isActive ? "*" : "") + "=" + (p.canMegaEvo || "no"); }).join(", "));
        s.write(">p1 " + steps[k]); s.write(">p2 move 1");
      }
      r.trace.push("FINE <- " + b.sides[0].pokemon.map(function (p) { return p.species.name + (p.isActive ? "*" : "") + "=" + (p.canMegaEvo || "no"); }).join(", "));
      r.megas = (b.log.join("\n").match(/\|-mega\|[^\n]*/g) || []).map(function (m) { return m.substring(0, 60); });
    } catch (e) { r.exception = String((e && e.stack) || e).substring(0, 500); }
    return r;
  };
  var team = [mk("Gengar", "gengarite", "cursedbody", "shadowball", 1), mk("Blastoise", "blastoisinite", "torrent", "surf", 2)];
  var tough = function (sp, i) { var m = mk(sp, "leftovers", "intimidate", "splash", i); m.level = 100; m.evs.hp = 252; return m; };
  var runFoe = function (format, team2, steps) { return run(format, team2, steps); };
  out.cobblemonsingles = run("cobblemonsingles", team, ["switch 2", "move 1 mega", "switch 2", "move 1 mega"]);
  out.cobblemonsingles_blastoise_prima = run("cobblemonsingles", [team[1], team[0]], ["move 1 mega", "switch 2", "move 1 mega"]);
  try {
    var D = Dex.forFormat ? Dex : null;
    out.gengarite = JSON.stringify(Dex.items.get("gengarite")).substring(0, 400);
    out.blastoisinite = JSON.stringify(Dex.items.get("blastoisinite")).substring(0, 400);
    out.gengarMega = !!Dex.species.get("Gengar-Mega").exists;
    out.scriptsActions = Object.keys((Dex.data.Scripts && Dex.data.Scripts.actions) || {});
    out.modScriptsActions = Object.keys((Dex.mod("cobblemon").data.Scripts.actions) || {});
    out.modRunMegaEvoSrc = String(Dex.mod("cobblemon").data.Scripts.actions.runMegaEvo).substring(0, 1500);
    out.sameObject = (typeof scripts !== "undefined") && scripts.Scripts === Dex.mod("cobblemon").data.Scripts;
  } catch (e) { out.dexErr = String(e).substring(0, 300); }
  return JSON.stringify(out);
})()
