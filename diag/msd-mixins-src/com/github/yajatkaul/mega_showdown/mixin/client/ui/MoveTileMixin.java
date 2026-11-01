package com.github.yajatkaul.mega_showdown.mixin.client.ui;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection.MoveTile;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.github.yajatkaul.mega_showdown.client.battle.hud.MovePreviewWidget;
import com.github.yajatkaul.mega_showdown.config.MegaShowdownConfig;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {MoveTile.class},
   remap = false
)
public abstract class MoveTileMixin {
   @Shadow
   private MoveTemplate moveTemplate;
   @Shadow
   @Final
   private Pokemon pokemon;
   @Shadow
   @Final
   private ElementalType elementalType;
   @Unique
   private MovePreviewWidget mega_showdown$previewWidget;

   @Shadow
   public abstract boolean isHovered(double var1, double var3);

   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   private void addPreview(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo info) {
      if (this.isHovered((double)mouseX, (double)mouseY) && MegaShowdownConfig.showMoveInspector) {
         if (this.mega_showdown$previewWidget == null) {
            this.mega_showdown$previewWidget = new MovePreviewWidget(this.moveTemplate);
         }

         this.mega_showdown$previewWidget.setSTAB(this.pokemon, this.elementalType);
         this.mega_showdown$previewWidget.method_25394(context, mouseX, mouseY, delta);
      }
   }
}
