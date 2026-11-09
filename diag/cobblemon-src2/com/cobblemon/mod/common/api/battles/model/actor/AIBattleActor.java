package com.cobblemon.mod.common.api.battles.model.actor

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.PassActionResponse
import com.cobblemon.mod.common.battles.ShowdownActionRequest
import com.cobblemon.mod.common.battles.ShowdownActionResponse
import com.cobblemon.mod.common.battles.ShowdownMoveset
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.exception.IllegalActionChoiceException
import com.cobblemon.mod.common.net.messages.client.battle.BattleFaintPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleHealthChangePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattlePersistentStatusPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleReplacePokemonPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwapPokemonPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwitchPokemonPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleTransformPokemonPacket
import java.util.ArrayList
import java.util.UUID
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nAIBattleActor.kt\nKotlin\n*S Kotlin\n*F\n+ 1 AIBattleActor.kt\ncom/cobblemon/mod/common/api/battles/model/actor/AIBattleActor\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,84:1\n1869#2,2:85\n*S KotlinDebug\n*F\n+ 1 AIBattleActor.kt\ncom/cobblemon/mod/common/api/battles/model/actor/AIBattleActor\n*L\n57#1:85,2\n*E\n"])
abstract class AIBattleActor : BattleActor {
   public final val battleAI: BattleAI

   open fun AIBattleActor(gameId: UUID, pokemonList: MutableList<BattlePokemon>, battleAI: BattleAI) {
      super(gameId, CollectionsKt.toMutableList(pokemonList));
      this.battleAI = battleAI;
   }

   public override fun sendUpdate(packet: NetworkPacket<*>) {
      super.sendUpdate(packet);
      if (packet is BattleMakeChoicePacket) {
         this.onChoiceRequested();
      } else if (packet !is BattlePersistentStatusPacket && packet !is BattleFaintPacket) {
         if (packet is BattleSwitchPokemonPacket) {
            if (!(packet as BattleSwitchPokemonPacket).isAlly()) {
            }
         } else if (packet !is BattleSwapPokemonPacket) {
            if (packet is BattleHealthChangePacket) {
               this.battleAI.onHealthChange(packet as BattleHealthChangePacket);
            } else if (if (packet is BattleTransformPokemonPacket)
               !(packet as BattleTransformPokemonPacket).isAlly()
               else
               packet is BattleReplacePokemonPacket && !(packet as BattleReplacePokemonPacket).isAlly()) {
            }
         }
      }
   }

   public open fun onChoiceRequested() {
      try {
         var var13: ShowdownActionRequest = this.getRequest();
         if (var13 != null) {
            this.setActionResponses(var13.iterate(this.getActivePokemon(), AIBattleActor::onChoiceRequested$lambda$0$0));

            var var11: java.lang.Iterable;
            for (Object element$iv : var11) {
               (`element$iv` as BattlePokemon).setWillBeSwitchedIn(false);
            }
         } else {
            AIBattleActor::onChoiceRequested$lambda$1;
         }
      } catch (IllegalActionChoiceException var10) {
         Cobblemon.LOGGER.error("AI was unable to choose an action, we're going to need to pass!");
         var10.printStackTrace();
         var var10000: ShowdownActionRequest = this.getRequest();
         if (var10000 != null) {
            this.setActionResponses(var10000.iterate(this.getActivePokemon(), AIBattleActor::onChoiceRequested$lambda$2$0));
         } else {
            AIBattleActor::onChoiceRequested$lambda$3;
         }
      }
   }

   @JvmStatic
   fun `onChoiceRequested$lambda$0$0`(`this$0`: AIBattleActor, battleMon: ActiveBattlePokemon, moveset: ShowdownMoveset, forceSwitch: Boolean): ShowdownActionResponse {
      return `this$0`.battleAI.choose(battleMon, `this$0`.getBattle(), `this$0`.getSide(), moveset, forceSwitch);
   }

   @JvmStatic
   fun `onChoiceRequested$lambda$1`(`this$0`: AIBattleActor): Unit {
      var response: java.util.List = new ArrayList();
      var var2: Int = `this$0`.getActivePokemon().size();

      for (int var3 = 0; var3 < var2; var3++) {
         response.add(PassActionResponse.INSTANCE);
      }

      `this$0`.setActionResponses(response);
      Cobblemon.LOGGER.warn("AI requested choice, but no request was set. Returning PassActionResponses.");
      return Unit.INSTANCE;
   }

   @JvmStatic
   fun `onChoiceRequested$lambda$2$0`(var0: ActiveBattlePokemon, var1: ShowdownMoveset, var2: Boolean): PassActionResponse {
      return PassActionResponse.INSTANCE;
   }

   @JvmStatic
   fun `onChoiceRequested$lambda$3`(`this$0`: AIBattleActor): Unit {
      var response: java.util.List = new ArrayList();
      var var2: Int = `this$0`.getActivePokemon().size();

      for (int var3 = 0; var3 < var2; var3++) {
         response.add(PassActionResponse.INSTANCE);
      }

      `this$0`.setActionResponses(response);
      return Unit.INSTANCE;
   }
}
