package com.github.yajatkaul.mega_showdown.mixin.battle;

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.BattleSide;
import com.cobblemon.mod.common.battles.interpreter.ContextManager;
import com.github.yajatkaul.mega_showdown.battle.effect.HazardEffect;
import com.github.yajatkaul.mega_showdown.battle.effect.RoomEffect;
import com.github.yajatkaul.mega_showdown.battle.effect.ScreenEffect;
import com.github.yajatkaul.mega_showdown.battle.effect.TailwindEffect;
import com.github.yajatkaul.mega_showdown.battle.effect.TerrainEffect;
import com.github.yajatkaul.mega_showdown.battle.effect.WeatherEffect;
import com.github.yajatkaul.mega_showdown.battle.messaging.BattlePacketManager;
import java.util.Collection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {PokemonBattle.class},
   remap = false
)
public abstract class PokemonBattleMixin {
   @Shadow
   private int ticks;

   @Shadow
   public abstract ContextManager getContextManager();

   @Shadow
   public abstract Iterable<BattleSide> getSides();

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void applyFieldEffects(CallbackInfo info) {
      if (this.ticks % 20 == 0) {
         Collection<BattleContext> terrain = this.getContextManager().get(Type.TERRAIN);
         Collection<BattleContext> weather = this.getContextManager().get(Type.WEATHER);
         Collection<BattleContext> room = this.getContextManager().get(Type.ROOM);
         if (terrain != null) {
            terrain.stream().findAny().ifPresent(context -> TerrainEffect.handleTerrain((PokemonBattle)this, context.getId(), this.ticks));
         }

         if (weather != null) {
            weather.stream().findAny().ifPresent(context -> WeatherEffect.handleWeather((PokemonBattle)this, context.getId()));
         }

         if (room != null) {
            room.stream().findAny().ifPresent(context -> RoomEffect.handleRoom((PokemonBattle)this, context.getId()));
         }

         for (BattleSide side : this.getSides()) {
            Collection<BattleContext> hazard = side.getContextManager().get(Type.HAZARD);
            Collection<BattleContext> screen = side.getContextManager().get(Type.SCREEN);
            Collection<BattleContext> tailwind = side.getContextManager().get(Type.TAILWIND);
            if (hazard != null) {
               hazard.forEach(context -> HazardEffect.handleHazard(side, context.getId(), this.ticks));
            }

            if (screen != null) {
               screen.forEach(context -> ScreenEffect.handleScreen(side, context.getId(), this.ticks));
            }

            if (tailwind != null) {
               tailwind.stream().findAny().ifPresent(context -> TailwindEffect.handleTailwind(side, context.getId()));
            }
         }
      }
   }

   @Inject(
      method = {"turn"},
      at = {@At("HEAD")}
   )
   private void updateBattleGuiPackets(int newTurnNumber, CallbackInfo info) {
      BattlePacketManager.update((PokemonBattle)this);
   }
}
