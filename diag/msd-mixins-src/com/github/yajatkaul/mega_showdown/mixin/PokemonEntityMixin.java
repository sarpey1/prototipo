package com.github.yajatkaul.mega_showdown.mixin;

import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatureAssignments;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.github.yajatkaul.mega_showdown.gimmick.MegaGimmick;
import com.github.yajatkaul.mega_showdown.gimmick.UltraGimmick;
import com.github.yajatkaul.mega_showdown.networking.client.packet.InteractionWheelPacket;
import com.github.yajatkaul.mega_showdown.render.layerEntities.states.TeraCrystalState;
import com.github.yajatkaul.mega_showdown.utils.duck.cobblemon.interfaces.PokemonEntityDuck;
import dev.architectury.networking.NetworkManager;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {PokemonEntity.class},
   remap = false
)
public abstract class PokemonEntityMixin implements PokemonEntityDuck {
   @Unique
   private final TeraCrystalState mega_showdown$teraCrystalState = new TeraCrystalState();
   @Unique
   private boolean mega_showdown$teraCrystalPlayed = false;
   @Unique
   private boolean mega_showdown$teraCrystalPass = false;
   @Unique
   private double mega_showdown$animCrystalSeconds = 0.0;
   @Unique
   private long mega_showdown$lastCrystalTimeNs = -1L;

   @Inject(
      method = {"recallWithAnimation"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void cancelRecallDuringEvolution(CallbackInfoReturnable<CompletableFuture<Pokemon>> cir) {
      PokemonEntity self = (PokemonEntity)this;
      boolean form_changing = self.getPokemon().getPersistentData().method_10577("form_changing");
      if (form_changing) {
         CompletableFuture<Pokemon> future = new CompletableFuture<>();
         future.complete(self.getPokemon());
         cir.setReturnValue(future);
      }
   }

   @Inject(
      method = {"showInteractionWheel"},
      at = {@At("HEAD")}
   )
   private void showInteractionWheelInject(class_3222 player, class_1799 itemStack, CallbackInfo ci) {
      PokemonEntity self = (PokemonEntity)this;
      Pokemon pokemon = self.getPokemon();
      if (self.isBattling()) {
         NetworkManager.sendToPlayer(player, new InteractionWheelPacket(false, false, false, false));
      } else if (pokemon.getOwnerPlayer() == player) {
         boolean shouldPokemonMega = SpeciesFeatureAssignments.getFeatures(pokemon.getSpecies()).contains("mega_evolution")
            || pokemon.getSpecies().getFeatures().contains("mega_evolution");
         boolean shouldPokemonUltra = pokemon.getSpecies().getName().equals("Necrozma");
         boolean canPokemonMega = MegaGimmick.isMega(pokemon) || MegaGimmick.canMega(pokemon);
         boolean canPokemonUltra = UltraGimmick.isUltra(pokemon) || UltraGimmick.canUltraBurst(pokemon);
         NetworkManager.sendToPlayer(player, new InteractionWheelPacket(shouldPokemonMega, shouldPokemonUltra, canPokemonMega, canPokemonUltra));
      }
   }

   @Inject(
      method = {"tryMountingShoulder"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void injectShoulder(class_3222 player, CallbackInfoReturnable<Boolean> cir) {
      PokemonEntity self = (PokemonEntity)this;
      Pokemon pokemon = self.getPokemon();
      if (pokemon.getPersistentData().method_10577("form_changing")) {
         cir.cancel();
      }
   }

   @Inject(
      method = {"offerItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void injectShoulder(class_1657 player, class_1799 stack, boolean isCosmetic, CallbackInfoReturnable<Boolean> cir) {
      PokemonEntity self = (PokemonEntity)this;
      Pokemon pokemon = self.getPokemon();
      if (pokemon.getPersistentData().method_10577("form_changing")) {
         cir.cancel();
      }
   }

   public TeraCrystalState mega_showdown$getTeraCrystalState() {
      return this.mega_showdown$teraCrystalState;
   }

   public boolean mega_showdown$isTeraCrystalPlayed() {
      return this.mega_showdown$teraCrystalPlayed;
   }

   public void mega_showdown$setTeraCrystalPlayed(boolean value) {
      this.mega_showdown$teraCrystalPlayed = value;
   }

   public boolean mega_showdown$isTeraCrystalPass() {
      return this.mega_showdown$teraCrystalPass;
   }

   public void mega_showdown$setTeraCrystalPass(boolean value) {
      this.mega_showdown$teraCrystalPass = value;
   }

   public double mega_showdown$getAnimCrystalSeconds() {
      return this.mega_showdown$animCrystalSeconds;
   }

   public void mega_showdown$setAnimCrystalSeconds(double value) {
      this.mega_showdown$animCrystalSeconds = value;
   }

   public long mega_showdown$getLastCrystalTimeNs() {
      return this.mega_showdown$lastCrystalTimeNs;
   }

   public void mega_showdown$setLastCrystalTimeNs(long value) {
      this.mega_showdown$lastCrystalTimeNs = value;
   }
}
