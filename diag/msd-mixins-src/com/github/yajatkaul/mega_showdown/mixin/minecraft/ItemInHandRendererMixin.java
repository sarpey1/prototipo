package com.github.yajatkaul.mega_showdown.mixin.minecraft;

import com.github.yajatkaul.mega_showdown.item.custom.tera.LikosPendant;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_746;
import net.minecraft.class_759;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_759.class})
public class ItemInHandRendererMixin {
   @Shadow
   @Final
   private class_310 field_4050;
   @Shadow
   private class_1799 field_4048;
   @Shadow
   private class_1799 field_4047;

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void tick(CallbackInfo ci) {
      class_746 localplayer = this.field_4050.field_1724;
      class_1799 itemstack = localplayer.method_6047();
      class_1799 itemstack1 = localplayer.method_6079();
      if (itemstack.method_7909() instanceof LikosPendant) {
         this.field_4047 = itemstack;
      }

      if (itemstack1.method_7909() instanceof LikosPendant) {
         this.field_4048 = itemstack1;
      }
   }
}
