package com.github.yajatkaul.mega_showdown.mixin;

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.Set;
import net.minecraft.class_2940;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {PokemonClientDelegate.class},
   remap = false
)
public abstract class PokemonClientDelegateMixin {
   @Shadow
   public PokemonEntity currentEntity;

   @Inject(
      method = {"onSyncedDataUpdated"},
      at = {@At(
         value = "INVOKE",
         target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z",
         shift = Shift.AFTER
      )},
      remap = false
   )
   public void inject(class_2940<?> data, CallbackInfo ci) {
      Set<String> currentAspects = this.currentEntity.getAspects();
      Pokemon pokemon = this.currentEntity.getPokemon();
      pokemon.setForcedAspects(currentAspects);
      pokemon.updateForm();
      this.currentEntity.method_18382();
   }
}
