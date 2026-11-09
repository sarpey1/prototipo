package com.github.yajatkaul.mega_showdown.mixin.battle;

import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.Targetable;
import com.cobblemon.mod.common.battles.ShowdownMoveset.Gimmick;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleGimmickButton.GimmickTile;
import com.cobblemon.mod.common.util.math.SimpleMathExtensionsKt;
import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {GimmickTile.class},
   remap = false
)
public class GimmickTileMixin {
   @Unique
   protected BattleMoveSelection mega_showdown$moveSelection;
   @Unique
   protected InBattleMove mega_showdown$move;
   @Final
   @Shadow
   private Gimmick gimmick;

   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")},
      remap = false
   )
   private void init(Gimmick gimmick, BattleMoveSelection moveSelection, InBattleMove move, float x, float y, CallbackInfo ci) {
      GimmickTile tile = (GimmickTile)this;
      this.mega_showdown$moveSelection = tile.getMoveSelection();
      this.mega_showdown$move = tile.getMove();
      if (gimmick == Gimmick.TERASTALLIZATION || gimmick == Gimmick.MEGA_EVOLUTION) {
         tile.setMoveTemplate(Moves.getByNameOrDummy(move.id));
         tile.setRgb(SimpleMathExtensionsKt.toRGB(tile.getMoveTemplate().getEffectiveElementalType(tile.getPokemon()).getHue()));
      }
   }

   @Inject(
      method = {"getTargetList"},
      at = {@At("TAIL")},
      remap = false,
      cancellable = true
   )
   private void targetList(CallbackInfoReturnable<List<Targetable>> cir) {
      if (this.gimmick == Gimmick.TERASTALLIZATION || this.gimmick == Gimmick.MEGA_EVOLUTION) {
         cir.setReturnValue((List)this.mega_showdown$move.getTarget().getTargetList().invoke(this.mega_showdown$moveSelection.getRequest().getActivePokemon()));
      }
   }
}
