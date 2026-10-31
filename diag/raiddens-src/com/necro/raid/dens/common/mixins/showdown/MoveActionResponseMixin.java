package com.necro.raid.dens.common.mixins.showdown;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.MoveActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.exception.IllegalActionChoiceException;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.RaidUtils;
import java.util.List;
import net.minecraft.class_2561;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MoveActionResponse.class})
public abstract class MoveActionResponseMixin {
   @Shadow(
      remap = false
   )
   private String moveName;

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
         if (!targetPokemon.contains(activeBattlePokemon)) {
            BattlePokemon battlePokemon = targetPokemon.getFirst().getBattlePokemon();
            if (battlePokemon != null) {
               PokemonEntity pokemonEntity = battlePokemon.getEntity();
               if (pokemonEntity != null) {
                  if (((IRaidAccessor)pokemonEntity).crd_isRaidBoss()) {
                     if (RaidUtils.isMoveBlacklisted(this.moveName)) {
                        BattleActor battleActor = activeBattlePokemon.getActor();
                        List<ShowdownActionResponse> originalActions = battleActor.getExpectingPassActions().stream().toList();
                        battleActor.getExpectingPassActions().clear();
                        battleActor.getExpectingPassActions().addAll(originalActions);
                        throw new IllegalActionChoiceException(
                           battleActor,
                           class_2561.method_43469(
                                 "message.cobblemonraiddens.raid.forbidden_move", new Object[]{class_2561.method_43471("cobblemon.move." + this.moveName)}
                              )
                              .getString()
                        );
                     }
                  }
               }
            }
         }
      }
   }
}
