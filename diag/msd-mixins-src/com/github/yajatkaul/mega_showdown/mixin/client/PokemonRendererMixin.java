package com.github.yajatkaul.mega_showdown.mixin.client;

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel;
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockActiveAnimation;
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockAnimationRepository;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext.RenderState;
import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.github.yajatkaul.mega_showdown.api.codec.sizer.LayerCodec.Settings;
import com.github.yajatkaul.mega_showdown.config.MegaShowdownConfig;
import com.github.yajatkaul.mega_showdown.render.LayerDataLoader;
import com.github.yajatkaul.mega_showdown.render.layerEntities.DynamaxCloudsLayer;
import com.github.yajatkaul.mega_showdown.render.layerEntities.TeraHatsLayer;
import com.github.yajatkaul.mega_showdown.render.layerEntities.states.TeraCrystalState;
import com.github.yajatkaul.mega_showdown.utils.duck.cobblemon.interfaces.PokemonEntityDuck;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import kotlin.Unit;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_7833;
import net.minecraft.class_5617.class_5618;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PokemonRenderer.class})
public class PokemonRendererMixin {
   @Unique
   private final RenderContext mega_showdown$context = new RenderContext();
   @Unique
   private final class_2960 mega_showdown$teraCrystalPoserId = class_2960.method_60655("cobblemon", "terastal_transformation");
   @Unique
   private final Set<String> mega_showdown$teraCrystalAspects = new HashSet<>();
   @Unique
   private DynamaxCloudsLayer mega_showdown$dynamaxCloudsLayer = new DynamaxCloudsLayer();
   @Unique
   private TeraHatsLayer mega_showdown$teraHatsLayer = new TeraHatsLayer();

   @Inject(
      method = {"<init>"},
      at = {@At("RETURN")}
   )
   public void init(class_5618 context, CallbackInfo ci) {
      this.mega_showdown$context.put(RenderContext.Companion.getRENDER_STATE(), RenderState.WORLD);
      this.mega_showdown$context.put(RenderContext.Companion.getDO_QUIRKS(), true);
   }

   @Inject(
      method = {"render*"},
      at = {@At("TAIL")}
   )
   public void render(PokemonEntity entity, float entityYaw, float partialTicks, class_4587 poseStack, class_4597 buffer, int packedLight, CallbackInfo ci) {
      PokemonClientDelegate clientDelegate = (PokemonClientDelegate)entity.getDelegate();
      Pokemon pokemon = entity.getPokemon();
      boolean tera_play = pokemon.getAspects().contains("play_tera");
      Optional<String> aspect = pokemon.getAspects().stream().filter(a -> a.startsWith("msd:tera_")).findFirst();
      boolean mega_showdown$teraCrystalPlayed = ((PokemonEntityDuck)entity).mega_showdown$isTeraCrystalPlayed();
      boolean mega_showdown$teraCrystalPass = ((PokemonEntityDuck)entity).mega_showdown$isTeraCrystalPass();
      if (tera_play && (!mega_showdown$teraCrystalPlayed || mega_showdown$teraCrystalPass)) {
         this.mega_showdown$renderTeraCrystals(entity, pokemon, clientDelegate, poseStack, buffer, packedLight);
         if (!mega_showdown$teraCrystalPass) {
            return;
         }
      }

      boolean dmax_aspect = pokemon.getAspects().contains("msd:dmax") || pokemon.getAspects().contains("gmax");
      if (aspect.isPresent() && MegaShowdownConfig.teraHats) {
         this.mega_showdown$teraHatsLayer
            .render(aspect.get(), this.mega_showdown$context, clientDelegate, entity, pokemon, entityYaw, partialTicks, poseStack, buffer, packedLight);
      }

      if (dmax_aspect) {
         this.mega_showdown$dynamaxCloudsLayer.render(this.mega_showdown$context, clientDelegate, entity, pokemon, entityYaw, poseStack, buffer, packedLight);
      }
   }

   @Unique
   private void mega_showdown$renderTeraCrystals(
      PokemonEntity entity, Pokemon pokemon, PokemonClientDelegate clientDelegate, class_4587 poseStack, class_4597 buffer, int packedLight
   ) {
      PokemonEntityDuck duck = (PokemonEntityDuck)entity;
      long mega_showdown$lastCrystalTimeNs = duck.mega_showdown$getLastCrystalTimeNs();
      double mega_showdown$animCrystalSeconds = duck.mega_showdown$getAnimCrystalSeconds();
      if (!class_310.method_1551().method_1493()) {
         long now = System.nanoTime();
         if (mega_showdown$lastCrystalTimeNs != -1L) {
            double deltaSeconds = (double)(now - mega_showdown$lastCrystalTimeNs) / 1.0E9;
            duck.mega_showdown$setAnimCrystalSeconds(mega_showdown$animCrystalSeconds + deltaSeconds);
         }

         duck.mega_showdown$setLastCrystalTimeNs(now);
      } else {
         duck.mega_showdown$setLastCrystalTimeNs(System.nanoTime());
      }

      float mega_showdown$teraCrystalDuration = new BedrockActiveAnimation(
            BedrockAnimationRepository.INSTANCE.getAnimation("terastal_transformation", "animation.terastal_transformation.transform")
         )
         .getDuration();
      TeraCrystalState mega_showdown$teraCrystalState = ((PokemonEntityDuck)entity).mega_showdown$getTeraCrystalState();
      if (mega_showdown$teraCrystalState.getAnimationSeconds() >= mega_showdown$teraCrystalDuration) {
         duck.mega_showdown$setTeraCrystalPlayed(true);
         duck.mega_showdown$setTeraCrystalPass(false);
         duck.mega_showdown$setAnimCrystalSeconds(0.0);
         duck.mega_showdown$setLastCrystalTimeNs(-1L);
         mega_showdown$teraCrystalState.resetAnimation();
         entity.after(3.0F, () -> {
            duck.mega_showdown$setTeraCrystalPlayed(false);
            return Unit.INSTANCE;
         });
      } else {
         if ((double)mega_showdown$teraCrystalState.getAnimationSeconds() >= (double)mega_showdown$teraCrystalDuration - 0.3) {
            duck.mega_showdown$setTeraCrystalPass(true);
         }

         float ticks = (float)(mega_showdown$animCrystalSeconds * 20.0);
         int age = (int)ticks;
         float pt = ticks - (float)age;
         mega_showdown$teraCrystalState.updateAge(age);
         mega_showdown$teraCrystalState.updatePartialTicks(pt);
         mega_showdown$teraCrystalState.setCurrentAspects(this.mega_showdown$teraCrystalAspects);
         Map<String, MatrixWrapper> locatorStates = clientDelegate.getLocatorStates();
         MatrixWrapper rootLocator = locatorStates.get("root");
         if (rootLocator != null) {
            PosableModel model = VaryingModelRepository.INSTANCE.getPoser(this.mega_showdown$teraCrystalPoserId, mega_showdown$teraCrystalState);
            model.context = this.mega_showdown$context;
            class_2960 texture = VaryingModelRepository.INSTANCE.getTexture(this.mega_showdown$teraCrystalPoserId, mega_showdown$teraCrystalState);
            class_4588 vertexConsumer = buffer.getBuffer(class_1921.method_23576(texture));
            model.setBufferProvider(buffer);
            mega_showdown$teraCrystalState.setCurrentModel(model);
            this.mega_showdown$context.put(RenderContext.Companion.getASPECTS(), this.mega_showdown$teraCrystalAspects);
            this.mega_showdown$context.put(RenderContext.Companion.getTEXTURE(), texture);
            this.mega_showdown$context.put(RenderContext.Companion.getSPECIES(), this.mega_showdown$teraCrystalPoserId);
            this.mega_showdown$context.put(RenderContext.Companion.getPOSABLE_STATE(), mega_showdown$teraCrystalState);
            Settings settings = LayerDataLoader.getSettings(pokemon, "msd:tera_crystal");
            poseStack.method_22903();
            poseStack.method_34425(rootLocator.getMatrix());
            poseStack.method_22907(class_7833.field_40714.rotationDegrees(180.0F));
            poseStack.method_22907(class_7833.field_40716.rotationDegrees(180.0F));
            poseStack.method_22904(0.08, 0.0, 0.0);
            if (settings != null) {
               settings.translate().ifPresent(translate -> poseStack.method_46416((Float)translate.get(0), (Float)translate.get(1), (Float)translate.get(2)));
            }

            poseStack.method_22905(1.5F, 1.5F, 1.5F);
            if (settings != null) {
               settings.rotation().ifPresent(rotation -> {
                  poseStack.method_22907(class_7833.field_40714.rotationDegrees((Float)rotation.get(0)));
                  poseStack.method_22907(class_7833.field_40716.rotationDegrees((Float)rotation.get(1)));
                  poseStack.method_22907(class_7833.field_40718.rotationDegrees((Float)rotation.get(2)));
               });
               settings.scale().ifPresent(scale -> poseStack.method_22905((Float)scale.get(0), (Float)scale.get(1), (Float)scale.get(2)));
            }

            model.applyAnimations(null, mega_showdown$teraCrystalState, 0.0F, 0.0F, ticks, 0.0F, 0.0F);
            model.render(this.mega_showdown$context, poseStack, vertexConsumer, packedLight, class_4608.field_21444, -1);
            model.withLayerContext(
               buffer,
               mega_showdown$teraCrystalState,
               VaryingModelRepository.INSTANCE.getLayers(this.mega_showdown$teraCrystalPoserId, mega_showdown$teraCrystalState),
               () -> {
                  model.render(this.mega_showdown$context, poseStack, vertexConsumer, packedLight, class_4608.field_21444, -1);
                  return Unit.INSTANCE;
               }
            );
            model.setDefault();
            poseStack.method_22909();
         }
      }
   }
}
