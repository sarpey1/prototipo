package com.github.yajatkaul.mega_showdown.mixin.client.ui;

import com.cobblemon.mod.common.client.gui.summary.Summary;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.github.yajatkaul.mega_showdown.utils.KotlinHelper;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Summary.class})
public abstract class SummaryMixin {
   @Inject(
      method = {"render"},
      at = {@At("RETURN")}
   )
   private void displayIcons(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      Summary summary = (Summary)this;
      float SCALE = 0.5F;
      int x = (summary.field_22789 - 331) / 2;
      int y = (summary.field_22790 - 161) / 2;
      class_4587 matrices = context.method_51448();
      matrices.method_22903();
      matrices.method_22904(0.0, 0.0, 2000.0);
      Pokemon pokemon = summary.getSelectedPokemon$common();
      KotlinHelper.INSTANCE
         .gimmikInfo(
            matrices,
            class_2960.method_60655("mega_showdown", "textures/gui/summary/tera_types/" + pokemon.getTeraType().showdownId() + ".png"),
            (float)(x + 58) / 0.5F,
            (float)(y + 84) / 0.5F,
            32,
            30,
            0.5F
         );
      if (pokemon.getGmaxFactor()) {
         KotlinHelper.INSTANCE
            .gimmikInfo(
               matrices,
               class_2960.method_60655("mega_showdown", "textures/gui/summary/gmax.png"),
               (float)(x + 6) / 0.5F,
               ((double)y + 31.5) / 0.5,
               24,
               24,
               0.5F
            );
      }

      matrices.method_22909();
   }
}
