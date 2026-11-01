package com.github.yajatkaul.mega_showdown.mixin.client;

import com.github.yajatkaul.mega_showdown.api.codec.item.ItemRenderingCodec;
import com.github.yajatkaul.mega_showdown.api.codec.item.PerspectivesCodec;
import com.github.yajatkaul.mega_showdown.render.ItemRenderingLoader;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_1087;
import net.minecraft.class_1091;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_763;
import net.minecraft.class_7923;
import net.minecraft.class_811;
import net.minecraft.class_918;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({class_918.class})
public abstract class ItemRendererMixin {
   @Shadow
   @Final
   private class_763 field_4732;

   @Shadow
   public abstract class_763 method_4012();

   @ModifyVariable(
      method = {"render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"},
      at = @At("HEAD"),
      argsOnly = true
   )
   public class_1087 modifyModel(class_1087 bakedModel, @Local(argsOnly = true) class_1799 stack, @Local(argsOnly = true) class_811 renderMode) {
      ItemRenderingCodec itemRenderingCodec = (ItemRenderingCodec)ItemRenderingLoader.REGISTRY
         .getOrDefault(class_7923.field_41178.method_10221(stack.method_7909()), null);
      if (itemRenderingCodec != null) {
         PerspectivesCodec perspectives = itemRenderingCodec.perspectivesCodec();
         if (renderMode == class_811.field_4317) {
            return this.method_4012().method_3303().method_4742(class_1091.method_61078((class_2960)perspectives.guiLoc().get()));
         }

         if (renderMode == class_811.field_4316) {
            return this.method_4012().method_3303().method_4742(class_1091.method_61078((class_2960)perspectives.headLoc().get()));
         }

         if (renderMode == class_811.field_4318) {
            return this.method_4012().method_3303().method_4742(class_1091.method_61078((class_2960)perspectives.groundLoc().get()));
         }

         if (renderMode == class_811.field_4319) {
            return this.method_4012().method_3303().method_4742(class_1091.method_61078((class_2960)perspectives.fixedLoc().get()));
         }

         if (renderMode == class_811.field_4321) {
            return this.method_4012().method_3303().method_4742(class_1091.method_61078((class_2960)perspectives.hand().get()));
         }

         if (renderMode == class_811.field_4322) {
            return this.method_4012().method_3303().method_4742(class_1091.method_61078((class_2960)perspectives.hand().get()));
         }

         if (renderMode == class_811.field_4323) {
            return this.method_4012().method_3303().method_4742(class_1091.method_61078((class_2960)perspectives.hand3rd().get()));
         }

         if (renderMode == class_811.field_4320) {
            return this.method_4012().method_3303().method_4742(class_1091.method_61078((class_2960)perspectives.hand3rd().get()));
         }
      }

      return bakedModel;
   }

   @ModifyVariable(
      method = {"getModel"},
      at = @At("STORE"),
      ordinal = 1
   )
   public class_1087 getHeldItemModelMixin(class_1087 bakedModel, @Local(argsOnly = true) class_1799 stack) {
      ItemRenderingCodec itemRenderingCodec = (ItemRenderingCodec)ItemRenderingLoader.REGISTRY
         .getOrDefault(class_7923.field_41178.method_10221(stack.method_7909()), null);
      return itemRenderingCodec != null ? this.field_4732.method_3303().method_4742(class_1091.method_61078(itemRenderingCodec.itemId_3d())) : bakedModel;
   }
}
