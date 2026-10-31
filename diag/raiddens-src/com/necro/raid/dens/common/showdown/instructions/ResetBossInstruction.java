package com.necro.raid.dens.common.showdown.instructions;

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.ShowdownInterpreter;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidBattle;
import kotlin.Unit;
import net.minecraft.class_2561;
import org.jetbrains.annotations.NotNull;

public class ResetBossInstruction implements InterpreterInstruction {
   private final BattleMessage message;
   private final BattlePokemon pokemon;
   private final BattlePokemon origin;

   public ResetBossInstruction(PokemonBattle battle, BattleMessage message) {
      this.message = message;
      this.pokemon = this.message.battlePokemon(0, battle);
      this.origin = this.message.pokemonByUuid(1, battle);
   }

   public void invoke(@NotNull PokemonBattle battle) {
      if (this.pokemon != null && this.origin != null) {
         if (((IRaidBattle)battle).crd_isRaidBattle()) {
            RaidInstance raid = ((IRaidBattle)battle).crd_getRaidBattle();
            battle.dispatchGo(() -> {
               battle.broadcastChatMessage(class_2561.method_43469("battle.cobblemonraiddens.reset.boss", new Object[]{this.origin.getName()}));
               Type boostBucket = Type.BOOST;
               BattleContext context = ShowdownInterpreter.INSTANCE.getContextFromAction(this.message, boostBucket, battle);
               raid.updateBattleState(battle, battleState -> battleState.bossSide.pokemon.clearNegativeBoosts());
               raid.updateBattleContext(battle, b -> {
                  b.broadcastChatMessage(class_2561.method_43469("battle.cobblemonraiddens.reset.boss", new Object[]{this.origin.getName()}));
                  BattlePokemon pokemon = ((ActiveBattlePokemon)b.getSide2().getActivePokemon().getFirst()).getBattlePokemon();
                  if (pokemon != null) {
                     pokemon.getContextManager().add(new BattleContext[]{context});
                  }
               });
               this.pokemon.getContextManager().add(new BattleContext[]{context});
               battle.getMinorBattleActions().put(this.pokemon.getUuid(), this.message);
               return Unit.INSTANCE;
            });
         }
      }
   }
}
