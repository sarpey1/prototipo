package com.github.yajatkaul.mega_showdown.fabric.mixin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.github.yajatkaul.mega_showdown.fabric.utils.KotlinHelperFabric;
import com.github.yajatkaul.mega_showdown.utils.TeraHelper;
import java.util.Optional;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {PokemonClientDelegate.class},
   remap = false
)
public class PokemonClientDelegateMixin {
   @Shadow
   public PokemonEntity currentEntity;
   @Unique
   private long mega_showdown$lastTeraParticle;
   @Unique
   private final float mega_showdown$teraParticleCooldown = 2.0F;

   @Unique
   private float mega_showdown$secondsSinceLastTeraParticle() {
      return (float)(System.currentTimeMillis() - this.mega_showdown$lastTeraParticle) / 1000.0F;
   }

   @Inject(
      method = {"tick(Lcom/cobblemon/mod/common/entity/pokemon/PokemonEntity;)V"},
      at = {@At("TAIL")}
   )
   private void tick(PokemonEntity entity, CallbackInfo ci) {
      this.mega_showdown$playTera();
   }

   @Unique
   private void mega_showdown$playTera() {
      PokemonClientDelegate self = (PokemonClientDelegate)this;
      class_310 mc = class_310.method_1551();
      class_1657 player = mc.field_1724;
      if (player != null) {
         PokemonEntity entity = this.currentEntity;
         Optional<String> aspect = entity.getAspects().stream().filter(a -> a.startsWith("msd:tera_")).findFirst();
         if (!aspect.isEmpty()) {
            double distance = player.method_19538().method_1022(entity.method_19538());
            if (!(distance > (double)Cobblemon.config.getShinyNoticeParticlesDistance())) {
               if (this.mega_showdown$secondsSinceLastTeraParticle() > 2.0F) {
                  KotlinHelperFabric.INSTANCE.playParticleEffect(TeraHelper.getTeraAnimationFromAspect(aspect.get()), "root", self.getRuntime());
                  this.mega_showdown$lastTeraParticle = System.currentTimeMillis();
               }
            }
         }
      }
   }
}
