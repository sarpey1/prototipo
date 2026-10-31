package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.runner.ShowdownService;

public interface ShowdownEvent extends AbstractEvent {
   @Override
   default void execute(RaidContext context) {
      if (context.battle() != null) {
         this.send(context.battle());
      }
   }

   String build(PokemonBattle var1);

   default void send(PokemonBattle battle) {
      String[] message = new String[]{this.build(battle)};

      try {
         ShowdownService.Companion.getService().send(battle.getBattleId(), message);
      } catch (Exception var4) {
      }
   }
}
