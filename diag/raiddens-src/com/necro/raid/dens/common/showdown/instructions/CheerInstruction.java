package com.necro.raid.dens.common.showdown.instructions;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import kotlin.Unit;
import net.minecraft.class_2561;
import org.jetbrains.annotations.NotNull;

public record CheerInstruction(BattleMessage message) implements InterpreterInstruction {
   public void invoke(@NotNull PokemonBattle battle) {
      battle.dispatchGo(() -> {
         String cheer = this.message.argumentAt(0);
         String origin = this.message.argumentAt(1);
         if (cheer != null && origin != null) {
            battle.broadcastChatMessage(class_2561.method_43469("battle.cobblemonraiddens.cheer." + cheer, new Object[]{origin}));
            return Unit.INSTANCE;
         } else {
            return Unit.INSTANCE;
         }
      });
   }
}
