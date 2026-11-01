package com.github.yajatkaul.mega_showdown.mixin.battle;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.InBattleGimmickMove;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.MoveActionResponse;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.Targetable;
import java.util.List;
import kotlin.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(
   value = {MoveActionResponse.class},
   remap = false
)
public class MoveActionResponseMixin {
   @Shadow
   private String moveName;
   @Shadow
   private String targetPnx;
   @Shadow
   private String gimmickID;

   @Overwrite
   public boolean isValid(ActiveBattlePokemon activeBattlePokemon, ShowdownMoveset showdownMoveSet, boolean forceSwitch) {
      if (!forceSwitch && showdownMoveSet != null) {
         InBattleMove move = showdownMoveSet.getMoves().stream().filter(m -> m.getId().equals(this.moveName)).findFirst().orElse(null);
         if (move == null) {
            return false;
         } else {
            InBattleGimmickMove gimmickMove = move.getGimmickMove();
            boolean validGimmickMove = gimmickMove != null && !gimmickMove.getDisabled();
            if (!validGimmickMove && !move.canBeUsed()) {
               return false;
            } else {
               List<Targetable> availableTargets;
               if (this.gimmickID != null && validGimmickMove && !this.gimmickID.equalsIgnoreCase("mega") && !this.gimmickID.equalsIgnoreCase("terastal")) {
                  availableTargets = (List<Targetable>)gimmickMove.getTarget().getTargetList().invoke(activeBattlePokemon);
               } else {
                  availableTargets = (List<Targetable>)move.getTarget().getTargetList().invoke(activeBattlePokemon);
               }

               if (availableTargets == null || availableTargets.isEmpty()) {
                  return true;
               } else if (this.targetPnx == null) {
                  return false;
               } else {
                  Pair<BattleActor, ActiveBattlePokemon> targetPair = activeBattlePokemon.getActor().getBattle().getActorAndActiveSlotFromPNX(this.targetPnx);
                  return availableTargets.contains(targetPair.getSecond());
               }
            }
         }
      } else {
         return false;
      }
   }
}
