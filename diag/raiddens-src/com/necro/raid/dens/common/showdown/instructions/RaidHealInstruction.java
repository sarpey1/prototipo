package com.necro.raid.dens.common.showdown.instructions;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.interpreter.Effect;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.data.ShowdownIdentifiable;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import kotlin.Unit;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import org.jetbrains.annotations.NotNull;

public record RaidHealInstruction(BattleActor actor, BattleMessage publicMessage, BattleMessage privateMessage) implements InterpreterInstruction {
   public void invoke(@NotNull PokemonBattle battle) {
      if (((IRaidBattle)battle).crd_isRaidBattle()) {
         RaidInstance raidInstance = ((IRaidBattle)battle).crd_getRaidBattle();
         BattlePokemon battlePokemon = this.publicMessage.battlePokemon(0, this.actor.battle);
         if (battlePokemon != null && battlePokemon.getEntity() != null) {
            if (((IRaidAccessor)battlePokemon.getEntity()).crd_isRaidBoss()) {
               String args = this.privateMessage.argumentAt(1);

               assert args != null;

               float healing = Float.parseFloat(args);
               Effect effect = this.privateMessage.effect("from");
               battle.dispatchWaiting(
                  1.0F,
                  () -> {
                     class_3222 player = (class_3222)battle.getPlayers().getFirst();
                     raidInstance.syncHealth(player, battle, -healing);
                     battlePokemon.getEffectedPokemon().setCurrentHealth((int)raidInstance.getCurrentHealth());
                     if (!this.privateMessage.hasOptionalArgument("silent")) {
                        class_2561 lang = null;
                        if (this.privateMessage.hasOptionalArgument("zeffect")) {
                           lang = LocalizationUtilsKt.battleLang("heal.zeffect", new Object[]{battlePokemon.getName()});
                        } else if (this.privateMessage.hasOptionalArgument("wisher")) {
                           String name = this.privateMessage.optionalArgument("wisher");

                           assert name != null;

                           String showdownId = name.toLowerCase().replace(ShowdownIdentifiable.Companion.getREGEX$common().getPattern(), "");
                           BattlePokemon wisher = this.actor
                              .getPokemonList()
                              .stream()
                              .filter(pokemon -> pokemon.getEffectedPokemon().showdownId().equals(showdownId))
                              .findFirst()
                              .orElse(null);
                           lang = LocalizationUtilsKt.battleLang("heal.wish", new Object[]{wisher != null ? wisher.getName() : this.actor.nameOwned(name)});
                        } else if (this.privateMessage.hasOptionalArgument("from")) {
                           if (effect != null && effect.getId().equals("drain")) {
                              BattlePokemon drained = this.privateMessage.battlePokemonFromOptional(battle, "of");
                              if (drained != null) {
                                 lang = LocalizationUtilsKt.battleLang("heal.drain", new Object[]{drained.getName()});
                              }
                           } else if (effect != null) {
                              lang = LocalizationUtilsKt.battleLang("heal." + effect.getId(), new Object[]{battlePokemon.getName()});
                           }
                        } else {
                           lang = LocalizationUtilsKt.battleLang("heal.generic", new Object[]{battlePokemon.getName()});
                        }

                        if (lang != null) {
                           battle.broadcastChatMessage(lang);
                        }
                     }

                     return Unit.INSTANCE;
                  }
               );
            }
         }
      }
   }
}
