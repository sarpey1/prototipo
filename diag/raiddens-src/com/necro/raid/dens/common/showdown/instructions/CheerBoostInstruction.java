package com.necro.raid.dens.common.showdown.instructions;

import com.bedrockk.molang.runtime.MoLangRuntime;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext;
import com.cobblemon.mod.common.api.moves.animations.ActionEffectTimeline;
import com.cobblemon.mod.common.api.moves.animations.ActionEffects;
import com.cobblemon.mod.common.api.moves.animations.UsersProvider;
import com.cobblemon.mod.common.battles.ShowdownInterpreter;
import com.cobblemon.mod.common.battles.dispatch.ActionEffectInstruction;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.dispatch.UntilDispatch;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.util.MiscUtilsKt;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import kotlin.Unit;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.jetbrains.annotations.NotNull;

public class CheerBoostInstruction implements ActionEffectInstruction {
   private final BattleMessage message;
   private CompletableFuture<?> future;
   private Set<String> holds;
   private final BattlePokemon pokemon;

   public CheerBoostInstruction(PokemonBattle battle, BattleMessage message) {
      this.message = message;
      this.future = CompletableFuture.completedFuture(Unit.INSTANCE);
      this.holds = new HashSet<>();
      this.pokemon = this.message.battlePokemon(0, battle);
   }

   @NotNull
   public CompletableFuture<?> getFuture() {
      return this.future;
   }

   public void setFuture(@NotNull CompletableFuture<?> future) {
      this.future = future;
   }

   @NotNull
   public Set<String> getHolds() {
      return this.holds;
   }

   public void setHolds(@NotNull Set<String> set) {
      this.holds = set;
   }

   @NotNull
   public class_2960 getId() {
      return class_2960.method_60655("cobblemonraiddens", "cheer_boost");
   }

   public void preActionEffect(@NotNull PokemonBattle battle) {
   }

   public void runActionEffect(@NotNull PokemonBattle battle, @NotNull MoLangRuntime runtime) {
      if (this.pokemon != null) {
         battle.dispatch(
            () -> {
               ActionEffectTimeline actionEffect = (ActionEffectTimeline)ActionEffects.INSTANCE.getActionEffects().get(MiscUtilsKt.cobblemonResource("boost"));
               List<Object> providers = new ArrayList<>(List.of(battle));
               providers.add(new UsersProvider(new class_1297[]{this.pokemon.getEffectedPokemon().getEntity()}));
               ActionEffectContext context = new ActionEffectContext(
                  actionEffect, new HashSet(), providers, runtime, false, false, new ArrayList(), ((class_3222)battle.getPlayers().getFirst()).method_37908()
               );
               this.setFuture(actionEffect.run(context));
               this.setHolds(context.getHolds());
               this.future.thenAccept(v -> this.holds.clear());
               return DispatchResultKt.getGO();
            }
         );
      }
   }

   public void postActionEffect(@NotNull PokemonBattle battle) {
      if (this.pokemon != null) {
         battle.dispatch(() -> {
            Type boostBucket = Type.BOOST;
            BattleContext context = ShowdownInterpreter.INSTANCE.getContextFromAction(this.message, boostBucket, battle);
            this.pokemon.getContextManager().add(new BattleContext[]{context});
            battle.getMinorBattleActions().put(this.pokemon.getUuid(), this.message);
            return new UntilDispatch(() -> !this.holds.contains("effects"));
         });
      }
   }
}
