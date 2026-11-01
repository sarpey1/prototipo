package com.github.yajatkaul.mega_showdown.fabric.mixin;

import com.cobblemon.mod.common.data.CobblemonDataProvider;
import com.github.yajatkaul.mega_showdown.datapack.CustomTypeStatusRegistry;
import com.github.yajatkaul.mega_showdown.fabric.datapack.showdown.Abilities;
import com.github.yajatkaul.mega_showdown.fabric.datapack.showdown.Conditions;
import com.github.yajatkaul.mega_showdown.fabric.datapack.showdown.HeldItems;
import com.github.yajatkaul.mega_showdown.fabric.datapack.showdown.Moves;
import com.github.yajatkaul.mega_showdown.fabric.datapack.showdown.Scripts;
import com.github.yajatkaul.mega_showdown.fabric.datapack.showdown.TypeCharts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {CobblemonDataProvider.class},
   remap = false
)
public class CobblemonDataProviderMixin {
   @Inject(
      method = {"registerDefaults"},
      at = {@At("HEAD")}
   )
   private void register(CallbackInfo ci) {
      CobblemonDataProvider self = (CobblemonDataProvider)this;
      self.register(CustomTypeStatusRegistry.INSTANCE, true);
      self.register(Moves.INSTANCE, true);
      self.register(Abilities.INSTANCE, true);
      self.register(Conditions.INSTANCE, true);
      self.register(HeldItems.INSTANCE, true);
      self.register(TypeCharts.INSTANCE, true);
      self.register(Scripts.INSTANCE, true);
   }
}
