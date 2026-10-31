(function () {
  /*
   * Mega Radar - Mega evoluzioni illimitate in battaglia.
   * Caricato da Mega Showdown come script Showdown (Scripts.actions) e copiato dentro ogni BattleActions.
   * Avvolge runMegaEvo originale: dopo una Mega NON toglie piu' la possibilita' di megaevolversi
   * agli altri Pokemon della squadra. Ogni Pokemon puo' comunque megaevolversi una sola volta.
   * NB: niente commenti a riga singola qui dentro, Mega Showdown unisce le righe prima di eseguirlo.
   */
  var current = (typeof scripts !== "undefined" && scripts && scripts.Scripts && scripts.Scripts.actions)
    ? scripts.Scripts.actions.runMegaEvo : undefined;
  var base = (current && current.__megaradarBase !== undefined) ? current.__megaradarBase : current;

  function unlimitedRunMegaEvo(pokemon) {
    var original = base || Object.getPrototypeOf(this).runMegaEvo;
    var side = pokemon.side;
    var wasMega = !!pokemon.canMegaEvo;
    var saved = [];
    for (var i = 0; i < side.pokemon.length; i++) {
      var ally = side.pokemon[i];
      if (ally !== pokemon) saved.push([ally, ally.canMegaEvo]);
    }
    var result = original.call(this, pokemon);
    if (result && wasMega) {
      if ("hasMegaEvolved" in side) side.hasMegaEvolved = false;
      for (var j = 0; j < saved.length; j++) {
        saved[j][0].canMegaEvo = saved[j][1];
      }
    }
    return result;
  }

  unlimitedRunMegaEvo.__megaradarBase = base || null;
  return { runMegaEvo: unlimitedRunMegaEvo };
})()
