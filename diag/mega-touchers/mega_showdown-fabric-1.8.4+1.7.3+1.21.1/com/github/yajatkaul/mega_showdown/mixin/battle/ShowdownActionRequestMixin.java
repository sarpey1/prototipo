package com.github.yajatkaul.mega_showdown.mixin.battle;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.storage.player.GeneralPlayerData;
import com.cobblemon.mod.common.battles.ShowdownActionRequest;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.ShowdownMoveset.Gimmick;
import com.github.yajatkaul.mega_showdown.gimmick.GimmickTurnCheck;
import java.util.List;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {ShowdownActionRequest.class},
   remap = false
)
public class ShowdownActionRequestMixin {
   @Shadow
   private List<ShowdownMoveset> active;

   @Inject(
      method = {"sanitize"},
      at = {@At("HEAD")},
      remap = false
   )
   private void beforeSanitize(PokemonBattle battle, BattleActor battleActor, CallbackInfo ci) {
      battle.getPlayers().forEach(GimmickTurnCheck::check);
   }

   @Inject(
      method = {"sanitize"},
      at = {@At("TAIL")},
      remap = false
   )
   private void afterSanitize(PokemonBattle battle, BattleActor battleActor, CallbackInfo ci) {
      class_2960 dynamaxBandId = class_2960.method_60655("cobblemon", "dynamax_band");

      for (class_3222 player : battle.getPlayers()) {
         GeneralPlayerData data = Cobblemon.INSTANCE.getPlayerDataManager().getGenericData(player);
         boolean hasBand = data.getKeyItems().contains(dynamaxBandId);
         if (player.method_5667().equals(battleActor.getUuid())) {
            List<ShowdownMoveset> activeMovesets = this.active;
            if (!hasBand && activeMovesets != null) {
               for (ShowdownMoveset moveset : activeMovesets) {
                  moveset.blockGimmick(Gimmick.DYNAMAX);
                  moveset.setMaxMoves(null);
               }
            }
         }
      }
   }
}
