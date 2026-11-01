package com.github.yajatkaul.mega_showdown.mixin.client;

import com.github.yajatkaul.mega_showdown.api.codec.item.ItemRenderingCodec;
import com.github.yajatkaul.mega_showdown.render.ItemRenderingLoader;
import net.minecraft.class_1088;
import net.minecraft.class_1091;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1088.class})
public abstract class ModeBakeryMixin {
   @Shadow
   protected abstract void method_61073(class_1091 var1);

   @Inject(
      method = {"<init>"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/resources/model/ModelBakery;loadSpecialItemModelAndDependencies(Lnet/minecraft/client/resources/model/ModelResourceLocation;)V",
         ordinal = 1
      )}
   )
   private void onInit(CallbackInfo ci) {
      for (ItemRenderingCodec itemRender : ItemRenderingLoader.REGISTRY.values()) {
         this.method_61073(class_1091.method_61078(itemRender.itemId()));
         this.method_61073(class_1091.method_61078(itemRender.itemId_3d()));
      }
   }
}
