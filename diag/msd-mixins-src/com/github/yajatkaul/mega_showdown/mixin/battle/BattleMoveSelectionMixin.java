package com.github.yajatkaul.mega_showdown.mixin.battle;

import com.cobblemon.mod.common.battles.ShiftActionResponse;
import com.cobblemon.mod.common.client.battle.SingleActionRequest;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleBackButton;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleGimmickButton;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleTargetSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection.MoveTile;
import com.github.yajatkaul.mega_showdown.cobblemon.battle.button.BattleShiftButton;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import java.util.List;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BattleMoveSelection.class})
public class BattleMoveSelectionMixin {
   @Final
   @Shadow(
      remap = false
   )
   private BattleBackButton backButton;
   @Unique
   private BattleShiftButton mega_showdown$cachedShiftButton = null;

   @Inject(
      method = {"<init>"},
      at = {@At("RETURN")},
      remap = false
   )
   private void onInit(BattleGUI battleGUI, SingleActionRequest request, CallbackInfo ci) {
      if (this.mega_showdown$cachedShiftButton == null) {
         BattleMoveSelection self = (BattleMoveSelection)this;
         this.mega_showdown$cachedShiftButton = new BattleShiftButton(
            this.backButton.getX() + 22.5F, (float)class_310.method_1551().method_22683().method_4502() - 22.0F, self.getRequest()
         );
      }
   }

   @Overwrite(
      remap = false
   )
   public boolean mousePrimaryClicked(double mouseX, double mouseY) {
      BattleMoveSelection self = (BattleMoveSelection)this;
      List<MoveTile> moveTiles = self.getMoveTiles();
      BattleGUI battleGUI = self.getBattleGUI();
      MoveTile move = moveTiles.stream().filter(m -> m.isHovered(mouseX, mouseY)).findFirst().orElse(null);
      BattleGimmickButton gimmick = self.getGimmickButtons().stream().filter(g -> g.isHovered(mouseX, mouseY)).findFirst().orElse(null);
      if (move != null) {
         if (self.getRequest().getActivePokemon().getFormat().getBattleType().getPokemonPerSide() == 1) {
            move.onClick();
            return true;
         }

         if (move.getSelectable()) {
            battleGUI.changeActionSelection(
               new BattleTargetSelection(battleGUI, self.getRequest(), move.getMove(), move.getResponse().getGimmickID(), move.getMove().getGimmickMove())
            );
            self.method_25354(class_310.method_1551().method_1483());
            return true;
         }
      } else if (self.getBackButton().isHovered(mouseX, mouseY)) {
         self.method_25354(class_310.method_1551().method_1483());
         battleGUI.changeActionSelection(null);
      } else if (gimmick != null) {
         for (BattleGimmickButton button : self.getGimmickButtons()) {
            if (button != gimmick) {
               button.setToggled(false);
            }
         }

         self.setMoveTiles(gimmick.toggle() ? gimmick.getTiles() : self.getBaseTiles());
      } else if (this.mega_showdown$cachedShiftButton.isHovered(mouseX, mouseY) && this.mega_showdown$cachedShiftButton.getEnabled()) {
         self.method_25354(class_310.method_1551().method_1483());
         battleGUI.selectAction(self.getRequest(), new ShiftActionResponse());
      }

      return false;
   }

   @WrapWithCondition(
      method = {"renderWidget"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/cobblemon/mod/common/client/gui/battle/subscreen/BattleShiftButton;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
      )}
   )
   private boolean onlyRenderIfAllowed(
      com.cobblemon.mod.common.client.gui.battle.subscreen.BattleShiftButton instance, class_332 context, int mouseX, int mouseY, float delta
   ) {
      return false;
   }

   @Inject(
      method = {"renderWidget"},
      at = {@At("HEAD")}
   )
   private void renderButton(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      this.mega_showdown$cachedShiftButton.render(context, mouseX, mouseY, delta);
   }
}
