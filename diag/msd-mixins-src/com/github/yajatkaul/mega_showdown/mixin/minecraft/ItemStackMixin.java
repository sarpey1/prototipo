package com.github.yajatkaul.mega_showdown.mixin.minecraft;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.github.yajatkaul.mega_showdown.api.codec.DuFusion;
import com.github.yajatkaul.mega_showdown.api.codec.FormChangeInteractItem;
import com.github.yajatkaul.mega_showdown.api.codec.FormChangeToggleInteractItem;
import com.github.yajatkaul.mega_showdown.api.codec.SoloFusion;
import com.github.yajatkaul.mega_showdown.utils.RegistryLocator;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1271;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_1799.class})
public class ItemStackMixin {
   @Inject(
      method = {"onDestroyed"},
      at = {@At("HEAD")}
   )
   private void onDestroyed(class_1542 itemEntity, CallbackInfo ci) {
      if (!itemEntity.method_37908().field_9236) {
         class_1799 stack = itemEntity.method_6983();
         DuFusion duFusion = (DuFusion)RegistryLocator.getComponent(DuFusion.class, stack);
         if (duFusion != null) {
            duFusion.onDestroyed(itemEntity);
         }

         SoloFusion soloFusion = (SoloFusion)RegistryLocator.getComponent(SoloFusion.class, stack);
         if (soloFusion != null) {
            soloFusion.onDestroyed(itemEntity);
         }
      }
   }

   @Inject(
      method = {"use"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void useOnEntity(class_1937 level, class_1657 player, class_1268 interactionHand, CallbackInfoReturnable<class_1271<class_1799>> cir) {
      if (!level.field_9236) {
         class_1799 stack = player.method_5998(interactionHand);
         DuFusion duFusion = (DuFusion)RegistryLocator.getComponent(DuFusion.class, stack);
         if (duFusion != null) {
            cir.setReturnValue(duFusion.use(level, player, interactionHand));
         }

         SoloFusion soloFusion = (SoloFusion)RegistryLocator.getComponent(SoloFusion.class, stack);
         if (soloFusion != null) {
            cir.setReturnValue(soloFusion.use(level, player, interactionHand));
         }
      }
   }

   @Inject(
      method = {"interactLivingEntity"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void useOnEntity(class_1657 player, class_1309 entity, class_1268 interactionHand, CallbackInfoReturnable<class_1269> cir) {
      if (!player.method_37908().field_9236 && entity instanceof PokemonEntity pokemonEntity) {
         class_1799 stack = player.method_5998(interactionHand);
         FormChangeToggleInteractItem formChangeToggleInteractItem = (FormChangeToggleInteractItem)RegistryLocator.getComponent(
            FormChangeToggleInteractItem.class, stack
         );
         if (formChangeToggleInteractItem != null) {
            cir.setReturnValue(formChangeToggleInteractItem.interactLivingEntity(player, pokemonEntity, stack));
         }

         FormChangeInteractItem formChangeInteractItem = (FormChangeInteractItem)RegistryLocator.getComponent(FormChangeInteractItem.class, stack);
         if (formChangeInteractItem != null) {
            cir.setReturnValue(formChangeInteractItem.interactLivingEntity(player, pokemonEntity, stack));
         }
      }
   }
}
