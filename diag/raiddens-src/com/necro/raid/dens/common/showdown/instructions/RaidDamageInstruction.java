package com.necro.raid.dens.common.showdown.instructions;

import com.bedrockk.molang.runtime.MoLangRuntime;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.interpreter.Effect;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext;
import com.cobblemon.mod.common.api.moves.animations.ActionEffectTimeline;
import com.cobblemon.mod.common.api.moves.animations.ActionEffects;
import com.cobblemon.mod.common.api.moves.animations.UsersProvider;
import com.cobblemon.mod.common.api.pokemon.status.Status;
import com.cobblemon.mod.common.api.pokemon.status.Statuses;
import com.cobblemon.mod.common.battles.ShowdownInterpreter;
import com.cobblemon.mod.common.battles.dispatch.ActionEffectInstruction;
import com.cobblemon.mod.common.battles.dispatch.CauserInstruction;
import com.cobblemon.mod.common.battles.dispatch.DispatchResult;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.dispatch.InstructionSet;
import com.cobblemon.mod.common.battles.dispatch.UntilDispatch;
import com.cobblemon.mod.common.battles.interpreter.instructions.MoveInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.net.messages.client.battle.BattleHealthChangePacket;
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer;
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.PoisonStatus;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
import com.cobblemon.mod.common.util.MiscUtilsKt;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import kotlin.Pair;
import kotlin.Unit;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.jetbrains.annotations.NotNull;

public class RaidDamageInstruction implements ActionEffectInstruction {
   private final InstructionSet instructionSet;
   private final BattleActor actor;
   private final BattleMessage publicMessage;
   private final BattleMessage privateMessage;
   private final BattlePokemon expectedTarget;
   private CompletableFuture<?> future;
   private Set<String> holds;

   public RaidDamageInstruction(InstructionSet instructionSet, BattleActor actor, BattleMessage publicMessage, BattleMessage privateMessage) {
      this.instructionSet = instructionSet;
      this.actor = actor;
      this.publicMessage = publicMessage;
      this.privateMessage = privateMessage;
      this.expectedTarget = this.publicMessage.battlePokemon(0, this.actor.getBattle());
      this.future = CompletableFuture.completedFuture(Unit.INSTANCE);
      this.holds = new HashSet<>();
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
      return class_2960.method_60655("cobblemonraiddens", "raid_damage");
   }

   public void preActionEffect(@NotNull PokemonBattle battle) {
      if (this.expectedTarget != null) {
         BattlePokemon source = this.privateMessage.battlePokemonFromOptional(battle, "of");
         if (source != null) {
            Effect effect = this.privateMessage.effect("from");
            ShowdownInterpreter.INSTANCE.broadcastOptionalAbility(battle, effect, source);
         }
      }
   }

   public void runActionEffect(@NotNull PokemonBattle battle, @NotNull MoLangRuntime runtime) {
      battle.dispatch(
         () -> {
            Effect effect = this.privateMessage.effect("from");
            Status status = effect == null ? null : Statuses.getStatus(effect.getId());
            BattlePokemon pokemon = this.privateMessage.battlePokemon(0, battle);
            if (pokemon == null) {
               return DispatchResultKt.getGO();
            } else {
               if (status instanceof PoisonStatus) {
                  PersistentStatusContainer container = pokemon.getEffectedPokemon().getStatus();
                  status = (Status)(container == null ? status : container.getStatus());
               }

               ActionEffectTimeline actionEffect = null;
               if (status != null) {
                  actionEffect = status.getActionEffect();
               }

               if (actionEffect == null && effect != null) {
                  String key = "damage_" + effect.getId();
                  actionEffect = (ActionEffectTimeline)ActionEffects.INSTANCE.getActionEffects().get(MiscUtilsKt.cobblemonResource(key));
               }

               if (actionEffect == null) {
                  actionEffect = (ActionEffectTimeline)ActionEffects.INSTANCE.getActionEffects().get(MiscUtilsKt.cobblemonResource("generic_damage"));
               }

               if (actionEffect == null) {
                  return DispatchResultKt.getGO();
               } else {
                  List<Object> providers = new ArrayList<>(List.of(battle));
                  providers.add(new UsersProvider(new class_1297[]{this.expectedTarget.getEffectedPokemon().getEntity()}));
                  ActionEffectContext context = new ActionEffectContext(
                     actionEffect,
                     new HashSet(),
                     providers,
                     runtime,
                     false,
                     false,
                     new ArrayList(),
                     ((class_3222)battle.getPlayers().getFirst()).method_37908()
                  );
                  this.setFuture(actionEffect.run(context));
                  this.setHolds(context.getHolds());
                  this.future.thenAccept(v -> this.holds.clear());
                  return DispatchResultKt.getGO();
               }
            }
         }
      );
   }

   public void postActionEffect(@NotNull PokemonBattle battle) {
      RaidInstance raidInstance = ((IRaidBattle)battle).crd_getRaidBattle();
      BattlePokemon battlePokemon = this.publicMessage.battlePokemon(0, this.actor.battle);
      if (battlePokemon != null && battlePokemon.getEntity() != null) {
         if (((IRaidAccessor)battlePokemon.getEntity()).crd_isRaidBoss()) {
            String args = this.privateMessage.argumentAt(1);

            assert args != null;

            String damageStr = args.split(" ")[0];
            CauserInstruction lastCauser = this.instructionSet.getMostRecentCauser(this);
            Effect effect = this.privateMessage.effect("from");
            battle.dispatch(
               () -> {
                  class_2561 pokemonName = battlePokemon.getName();
                  BattlePokemon source = this.privateMessage.battlePokemonFromOptional(battle, "of");
                  if (effect != null) {
                     class_2561 lang = null;
                     if (Set.of("brn", "psn", "tox").contains(effect.getId())) {
                        Status status = Statuses.getStatus(effect.getId());
                        if (status != null) {
                           lang = LocalizationUtilsKt.lang(String.format("status.%s.hurt", status.getName().method_12832()), new Object[]{pokemonName});
                        }
                     } else if (Set.of("aftermath", "innardsout").contains(effect.getId())) {
                        lang = LocalizationUtilsKt.battleLang("damage.generic", new Object[]{pokemonName});
                     } else if (Set.of("chloroblast", "steelbeam").contains(effect.getId())) {
                        lang = LocalizationUtilsKt.battleLang("damage.mindblown", new Object[]{pokemonName});
                     } else if (effect.getId().equals("jumpkick")) {
                        lang = LocalizationUtilsKt.battleLang("damage.highjumpkick", new Object[]{pokemonName});
                     } else {
                        lang = LocalizationUtilsKt.battleLang(
                           "damage." + effect.getId(), new Object[]{pokemonName, source != null ? source.getName() : class_2561.method_43470("UNKOWN")}
                        );
                     }

                     if (lang != null) {
                        battle.broadcastChatMessage(lang);
                     }
                  }

                  float damage = Float.parseFloat(damageStr);
                  class_3222 player = (class_3222)battle.getPlayers().getFirst();
                  raidInstance.syncHealth(player, battle, damage);
                  battlePokemon.getEffectedPokemon().setCurrentHealth((int)raidInstance.getCurrentHealth());
                  boolean causedFaint = raidInstance.getCurrentHealth() <= 0.0F;
                  if (causedFaint) {
                     battle.getDispatches().clear();
                     battle.dispatchToFront(() -> {
                        battlePokemon.getEffectedPokemon().setCurrentHealth(0);
                        battlePokemon.sendUpdate();
                        return DispatchResultKt.getGO();
                     });
                  }

                  Pair<String, String> pnxUuid = this.privateMessage.pnxAndUuid(0);
                  if (pnxUuid != null) {
                     String pnx = (String)pnxUuid.getFirst();
                     float remainingHealth = raidInstance.getCurrentHealth();
                     float healthRatio = remainingHealth / raidInstance.getMaxHealth();
                     battle.sendSidedUpdate(
                        this.actor, new BattleHealthChangePacket(pnx, remainingHealth, null), new BattleHealthChangePacket(pnx, healthRatio, null), false
                     );
                  }

                  if (lastCauser instanceof MoveInstruction && ((MoveInstruction)lastCauser).getActionEffect() != null && !causedFaint) {
                     return new UntilDispatch(() -> ((MoveInstruction)lastCauser).getFuture().isDone());
                  } else {
                     return (DispatchResult)(causedFaint ? DispatchResultKt.getGO() : new UntilDispatch(() -> !this.getHolds().contains("effects")));
                  }
               }
            );
         }
      }
   }
}
