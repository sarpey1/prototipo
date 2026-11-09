package com.github.yajatkaul.mega_showdown.mixin.battle;

import com.cobblemon.mod.common.battles.BattleRegistry;
import com.github.yajatkaul.mega_showdown.utils.AspectUtils;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BattleRegistry.class})
public class BattleRegistryMixin {
   @Inject(
      method = {"onPlayerDisconnect"},
      at = {@At("HEAD")},
      remap = false
   )
   private void onPlayerDisconnect(class_3222 player, CallbackInfo ci) {
      if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
         AspectUtils.battleDisconnecter.add(player.method_5667());
      }
   }
}
