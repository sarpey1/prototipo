package com.github.yajatkaul.mega_showdown.fabric.mixin;

import com.github.yajatkaul.mega_showdown.render.renderTypes.IrisIgnoreShader;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.client.rendering.FabricShaderProgram;
import net.minecraft.class_2960;
import net.minecraft.class_5944;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_5944.class})
public abstract class ShaderInstanceMixin {
   @Shadow
   @Final
   private String field_29494;

   @WrapOperation(
      method = {"<init>"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"
      )},
      allow = 1
   )
   private class_2960 modifyId(String id, Operation<class_2960> original) {
      return this instanceof IrisIgnoreShader ? FabricShaderProgram.rewriteAsId(id, this.field_29494) : (class_2960)original.call(new Object[]{id});
   }
}
