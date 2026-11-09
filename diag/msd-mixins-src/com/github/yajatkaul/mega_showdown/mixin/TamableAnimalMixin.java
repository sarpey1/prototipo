package com.github.yajatkaul.mega_showdown.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.class_1299;
import net.minecraft.class_1321;
import net.minecraft.class_1429;
import net.minecraft.class_1937;
import net.minecraft.class_268;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_1321.class})
public abstract class TamableAnimalMixin extends class_1429 {
   protected TamableAnimalMixin(class_1299<? extends class_1429> entityType, class_1937 level) {
      super(entityType, level);
   }

   @Inject(
      method = {"getTeam"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void getTeamInject(CallbackInfoReturnable<class_268> cir) {
      class_1321 tamableAnimal = (class_1321)this;
      if (tamableAnimal instanceof PokemonEntity) {
         cir.setReturnValue(super.method_5781());
         cir.cancel();
      }
   }
}
