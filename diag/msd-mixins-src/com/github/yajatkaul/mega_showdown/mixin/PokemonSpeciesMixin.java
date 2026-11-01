package com.github.yajatkaul.mega_showdown.mixin;

import com.cobblemon.mod.common.api.data.ShowdownIdentifiable.Companion;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import kotlin.text.Regex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({PokemonSpecies.class})
public class PokemonSpeciesMixin {
   @Redirect(
      method = {"allShowdownSpecies$common"},
      at = @At(
         value = "INVOKE",
         target = "Lcom/cobblemon/mod/common/api/data/ShowdownIdentifiable$Companion;getREGEX$common()Lkotlin/text/Regex;"
      ),
      remap = false
   )
   private Regex redirectRegex(Companion companion) {
      return new Regex("[^a-zA-Z0-9: -]+");
   }
}
