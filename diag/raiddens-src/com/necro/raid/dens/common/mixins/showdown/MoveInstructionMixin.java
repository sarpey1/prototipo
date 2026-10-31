package com.necro.raid.dens.common.mixins.showdown;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import com.cobblemon.mod.common.battles.interpreter.instructions.MoveInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.data.support.RaidSupport;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.registry.RaidSupportRegistry;
import com.necro.raid.dens.common.showdown.events.ShowdownEvents;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import kotlin.Unit;
import net.minecraft.class_2588;
import net.minecraft.class_3222;
import net.minecraft.class_7417;
import net.minecraft.class_8828;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MoveInstruction.class})
public abstract class MoveInstructionMixin implements InterpreterInstruction {
   @Final
   @Shadow(
      remap = false
   )
   private MoveTemplate move;
   @Shadow(
      remap = false
   )
   public BattlePokemon userPokemon;

   @Inject(
      method = {"invoke(Lcom/cobblemon/mod/common/api/battles/model/PokemonBattle;)V"},
      at = {@At("RETURN")},
      remap = false
   )
   private void invokeInject(PokemonBattle battle, CallbackInfo ci) {
      if (((IRaidBattle)battle).crd_isRaidBattle()) {
         if (this.userPokemon != null && this.userPokemon.getEntity() != null) {
            if (!((IRaidAccessor)this.userPokemon.getEntity()).crd_isRaidBoss()) {
               class_3222 player = this.userPokemon.getEffectedPokemon().getOwnerPlayer();
               if (player != null) {
                  RaidInstance raid = ((IRaidBattle)battle).crd_getRaidBattle();
                  battle.dispatchGo(
                     () -> {
                        class_7417 pokemonContents = this.userPokemon.getEffectedPokemon().getDisplayName(false).method_10851();
                        class_7417 moveContents = this.move.getDisplayName().method_10851();
                        raid.getPlayers()
                           .forEach(
                              p -> RaidDenNetworkMessages.RAID_LOG.accept(p, this.crd_resolveContents(pokemonContents), this.crd_resolveContents(moveContents))
                           );
                        if (!raid.canSync()) {
                           return Unit.INSTANCE;
                        } else {
                           String moveId = this.move.getName();
                           RaidSupport support = RaidSupportRegistry.getSupport(moveId);
                           if (support != null) {
                              support.boosts()
                                 .forEach((stat, stages) -> raid.broadcastToOthers(new ShowdownEvents.RaidSupportShowdownEvent(moveId, stat, stages), battle));
                              if (support.heal() > 0.0F) {
                                 raid.broadcastToOthers(new ShowdownEvents.RaidHealShowdownEvent(support.heal()), battle);
                              }

                              if (support.cure()) {
                                 raid.broadcastToOthers(new ShowdownEvents.RaidCureShowdownEvent(), battle);
                              }

                              if (support.sendUpdate()) {
                                 RaidSupportRegistry.addToQueue(this.userPokemon.getUuid());
                              }
                           } else if (moveId.equals("dragoncheer")) {
                              raid.broadcastToOthers(new ShowdownEvents.DragonCheerShowdownEvent(), battle);
                              RaidSupportRegistry.addToQueue(this.userPokemon.getUuid());
                           }

                           battle.dispatch(() -> {
                              RaidSupportRegistry.removeFromQueue(this.userPokemon.getUuid());
                              return DispatchResultKt.getGO();
                           });
                           return Unit.INSTANCE;
                        }
                     }
                  );
               }
            }
         }
      }
   }

   @Unique
   private String crd_resolveContents(class_7417 contents) {
      if (contents instanceof class_2588 t) {
         return t.method_11022();
      } else {
         return contents instanceof class_8828 p ? p.comp_737() : contents.toString();
      }
   }
}
