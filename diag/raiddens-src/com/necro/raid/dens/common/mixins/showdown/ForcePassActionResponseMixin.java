package com.necro.raid.dens.common.mixins.showdown;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.ForcePassActionResponse;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.exception.IllegalActionChoiceException;
import com.necro.raid.dens.common.util.IRaidAccessor;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ForcePassActionResponse.class})
public abstract class ForcePassActionResponseMixin {
   @Inject(
      method = {"isValid(Lcom/cobblemon/mod/common/battles/ActiveBattlePokemon;Lcom/cobblemon/mod/common/battles/ShowdownMoveset;Z)Z"},
      at = {@At("HEAD")},
      remap = false
   )
   private void isValidInject(
      ActiveBattlePokemon activeBattlePokemon, ShowdownMoveset showdownMoveSet, boolean forceSwitch, CallbackInfoReturnable<Boolean> cir
   ) {
      List<ActiveBattlePokemon> targetPokemon = activeBattlePokemon.getActor().getBattle().getSide2().getActivePokemon();
      if (!targetPokemon.isEmpty()) {
         if (targetPokemon.getFirst() != activeBattlePokemon) {
            BattlePokemon battlePokemon = targetPokemon.getFirst().getBattlePokemon();
            if (battlePokemon != null) {
               PokemonEntity pokemonEntity = battlePokemon.getEntity();
               if (pokemonEntity != null) {
                  if (((IRaidAccessor)pokemonEntity).crd_isRaidBoss()) {
                     BattleActor battleActor = activeBattlePokemon.getActor();
                     class_3222 player = (class_3222)battleActor.getBattle().getPlayers().getFirst();
                     if (battleActor.isForPlayer(player)) {
                        if (!battleActor.getExpectingPassActions().isEmpty()) {
                           battleActor.getExpectingPassActions().removeFirst();
                        }

                        throw new IllegalActionChoiceException(
                           battleActor, class_2561.method_43471("message.cobblemonraiddens.raid.forbidden_item").getString()
                        );
                     }
                  }
               }
            }
         }
      }
   }
}
