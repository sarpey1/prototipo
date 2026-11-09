package com.github.yajatkaul.mega_showdown.mixin.battle;

import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay;
import com.github.yajatkaul.mega_showdown.client.battle.hud.BattleHUD;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BattleOverlay.class})
public abstract class BattleOverlayMixin extends class_329 {
   private BattleOverlayMixin(class_310 client) {
      super(client);
   }

   @Inject(
      method = {"drawTile"},
      at = {@At("TAIL")}
   )
   private void drawStatChanges(
      class_332 context,
      float tickDelta,
      ActiveClientBattlePokemon activeBattlePokemon,
      boolean left,
      int rank,
      PokedexEntryProgress dexState,
      boolean hasCommand,
      boolean isHovered,
      boolean isCompact,
      CallbackInfo info
   ) {
      BattleHUD.drawStatChanges(context, activeBattlePokemon, left, rank, isCompact);
   }
}
