package com.cobblemon.mod.common.api.battles.model.actor

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.molang.MoLangFunctions
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.ForcePassActionResponse
import com.cobblemon.mod.common.battles.ShowdownActionRequest
import com.cobblemon.mod.common.battles.ShowdownActionResponse
import com.cobblemon.mod.common.battles.ShowdownMoveset
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.exception.IllegalActionChoiceException
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.net.messages.client.battle.BattleApplyPassResponsePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleMessagePacket
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.ArrayList
import java.util.UUID
import kotlin.jvm.internal.SourceDebugExtension
import kotlin.jvm.internal.Ref.IntRef
import net.minecraft.class_2561
import net.minecraft.class_3222
import net.minecraft.class_5250

@SourceDebugExtension(["SMAP\nBattleActor.kt\nKotlin\n*S Kotlin\n*F\n+ 1 BattleActor.kt\ncom/cobblemon/mod/common/api/battles/model/actor/BattleActor\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,162:1\n1869#2,2:163\n1788#2,4:165\n1761#2,3:169\n1761#2,3:172\n1583#2,11:175\n1878#2,2:186\n1880#2:189\n1594#2:190\n1878#2,2:191\n1880#2:194\n1#3:188\n1#3:193\n*S KotlinDebug\n*F\n+ 1 BattleActor.kt\ncom/cobblemon/mod/common/api/battles/model/actor/BattleActor\n*L\n37#1:163,2\n61#1:165,4\n74#1:169,3\n79#1:172,3\n95#1:175,11\n95#1:186,2\n95#1:189\n95#1:190\n107#1:191,2\n107#1:194\n95#1:188\n*E\n"])
abstract class BattleActor {
   public final val uuid: UUID
   public final val pokemonList: MutableList<BattlePokemon>
   public final val struct: ObjectValue<BattleActor>
   public final lateinit var showdownId: String
   public final lateinit var battle: PokemonBattle
   public final val activePokemon: MutableList<ActiveBattlePokemon>
   public final var canDynamax: Boolean
   public final var request: ShowdownActionRequest?
   public final var responses: MutableList<ShowdownActionResponse>
   public final val expectingPassActions: MutableList<ShowdownActionResponse>
   public final var mustChoose: Boolean
   public final var itemsUsed: MutableList<BagItem>
   public final var stillSendingOutCount: Int
   public abstract val type: ActorType

   open fun BattleActor(uuid: UUID, pokemonList: MutableList<BattlePokemon>) {
      this.uuid = uuid;
      this.pokemonList = pokemonList;

      var `$this$forEach$iv`: java.lang.Iterable;
      for (Object element$iv : $this$forEach$iv) {
         (`element$iv` as BattlePokemon).setActor(this);
      }

      this.struct = MoLangFunctions.INSTANCE.asMoLangValue(this);
      this.activePokemon = new ArrayList<>();
      this.responses = new ArrayList<>();
      this.expectingPassActions = new ArrayList<>();
      this.itemsUsed = new ArrayList<>();
   }

   public fun isInitialized(): Boolean {
      return this.battle != null && this.showdownId != null;
   }

   public fun canFitForcedAction(): Boolean {
      if (this.mustChoose) {
         var var10000: Boolean;
         if (this.request != null) {
            var request: ShowdownActionRequest = this.request;
            var var13: java.util.List = this.request.getActive();
            var10000 = if (var13 != null) var13.size() else 0;
            var `$this$count$iv`: java.lang.Iterable = request.getForceSwitch();
            if (`$this$count$iv` is java.util.Collection && (`$this$count$iv` as java.util.Collection).isEmpty()) {
               var10000 = 0;
            } else {
               var `count$iv`: Int = 0;

               for (Object element$iv : $this$count$iv) {
                  if (`element$iv` as java.lang.Boolean) {
                     if (++`count$iv` < 0) {
                        CollectionsKt.throwCountOverflow();
                     }
                  }
               }

               var10000 = `count$iv`;
            }

            var10000 = var10000 - var10000 > this.expectingPassActions.size() && !this.getBattle().getEnded();
         } else {
            var10000 = 0;
         }

         if (var10000) {
            return true;
         }
      }

      return false;
   }

   public fun forceChoose(response: ShowdownActionResponse) {
      this.expectingPassActions.add(response);
      this.sendUpdate((new BattleApplyPassResponsePacket()) as NetworkPacket<?>);
   }

   public fun getSide(): BattleSide {
      return if (ArraysKt.contains(this.getBattle().getSide1().getActors(), this)) this.getBattle().getSide1() else this.getBattle().getSide2();
   }

   public open fun getPlayerUUIDs(): Iterable<UUID> {
      return CollectionsKt.emptyList();
   }

   public open fun isForPlayer(serverPlayerEntity: class_3222): Boolean {
      return CollectionsKt.contains(this.getPlayerUUIDs(), serverPlayerEntity.method_5667());
   }

   public open fun isForPokemon(pokemonEntity: PokemonEntity): Boolean {
      var `$this$any$iv`: java.lang.Iterable = this.activePokemon;
      var var10: Boolean;
      if (this.activePokemon is java.util.Collection && this.activePokemon.isEmpty()) {
         var10 = false;
      } else {
         var var4: java.util.Iterator = `$this$any$iv`.iterator();

         while (true) {
            if (!var4.hasNext()) {
               var10 = false;
               break;
            }

            label24: {
               var var10000: BattlePokemon = (var4.next() as ActiveBattlePokemon).getBattlePokemon();
               if (var10000 != null) {
                  var var8: Pokemon = var10000.getEffectedPokemon();
                  if (var8 != null) {
                     var9 = var8.getEntity();
                     break label24;
                  }
               }

               var9 = null;
            }

            if (var9 == pokemonEntity) {
               var10 = true;
               break;
            }
         }
      }

      return var10;
   }

   public fun turn() {
      if (this.request != null) {
         var request: ShowdownActionRequest = this.request;
         this.responses.clear();
         var requestActive: java.lang.Iterable = this.activePokemon;
         var var10000: Boolean;
         if (this.activePokemon is java.util.Collection && this.activePokemon.isEmpty()) {
            var10000 = false;
         } else {
            var var4: java.util.Iterator = requestActive.iterator();

            while (true) {
               if (!var4.hasNext()) {
                  var10000 = false;
                  break;
               }

               if ((var4.next() as ActiveBattlePokemon).isAlive()) {
                  var10000 = true;
                  break;
               }
            }
         }

         if (var10000) {
            this.mustChoose = true;
            this.sendUpdate((new BattleMakeChoicePacket()) as NetworkPacket<?>);
         }

         var var8: java.util.List = request.getActive();
         if (var8 == null || var8.isEmpty() || request.getWait()) {
            this.request = null;
            this.expectingPassActions.clear();
         }
      }
   }

   public fun upkeep() {
      if (this.request != null) {
         var `$this$mapIndexedNotNull$iv`: java.lang.Iterable = this.request.getForceSwitch();
         var `destination$iv$iv`: java.util.Collection = new ArrayList();
         var `index$iv$iv$iv`: Int = 0;

         for (Object item$iv$iv$iv : $this$mapIndexedNotNull$iv) {
            var var13: Int = `index$iv$iv$iv`++;
            if (var13 < 0) {
               CollectionsKt.throwIndexOverflow();
            }

            var var22: ActiveBattlePokemon = if (`item$iv$iv$iv` as java.lang.Boolean) this.activePokemon.get(var13) else null;
            if (var22 != null) {
               `destination$iv$iv`.add(var22);
            }
         }

         if (!(`destination$iv$iv` as java.util.List).isEmpty()) {
            this.sendUpdate((new BattleMakeChoicePacket()) as NetworkPacket<?>);
            this.mustChoose = true;
         }
      }
   }

   public fun setActionResponses(responses: List<ShowdownActionResponse>) {
      if (this.request != null) {
         var request: ShowdownActionRequest = this.request;
         var originalPassActions: java.util.List = CollectionsKt.toList(this.expectingPassActions);
         var `$this$forEachIndexed$iv`: java.lang.Iterable = responses;
         var `index$iv`: Int = 0;

         for (Object item$iv : $this$forEachIndexed$iv) {
            var var9: Int = `index$iv`++;
            if (var9 < 0) {
               CollectionsKt.throwIndexOverflow();
            }

            var response: ShowdownActionResponse = `item$iv` as ShowdownActionResponse;
            var forceSwitch: java.util.List = this.activePokemon;
            if (this.activePokemon.size() <= var9) {
               return;
            }

            var activeBattlePokemon: ActiveBattlePokemon = forceSwitch.get(var9) as ActiveBattlePokemon;
            var var22: java.util.List = request.getActive();
            var var23: ShowdownMoveset = if (var22 != null) (if (var22.size() > var9) var22.get(var9) as ShowdownMoveset else null) else null;
            var itx: java.util.List = request.getForceSwitch();
            if (!response.isValid(activeBattlePokemon, var23, itx.size() > var9 && itx.get(var9) as java.lang.Boolean)) {
               this.expectingPassActions.clear();
               this.expectingPassActions.addAll(originalPassActions);
               var var10003: BattlePokemon = activeBattlePokemon.getBattlePokemon();
               throw new IllegalActionChoiceException(this, "Invalid action choice for " + var10003.getName().getString() + ": " + response);
            }

            if (response is ForcePassActionResponse) {
               this.responses.add(this.expectingPassActions.remove(0));
            } else {
               this.responses.add(response);
            }
         }

         if (this.expectingPassActions.size() > 0) {
            throw new IllegalActionChoiceException(this, "Invalid action choice: a capture was expected. Are you hacking me?");
         } else {
            this.mustChoose = false;
            this.getBattle().checkForInputDispatch();
         }
      }
   }

   public fun writeShowdownResponse() {
      var showdownMessages: java.util.List = new ArrayList();
      var index: IntRef = new IntRef();
      var var10000: ShowdownActionRequest = this.request;
      var10000.iterate(this.activePokemon, BattleActor::writeShowdownResponse$lambda$0);
      this.responses.clear();
      this.request = null;
      this.expectingPassActions.clear();
      this.getBattle()
         .writeShowdownAction(
            new java.lang.String[]{
               ">" + this.getShowdownId() + " " + CollectionsKt.joinToString$default(showdownMessages, null, null, null, 0, null, null, 63, null)
            }
         );
   }

   public abstract fun getName(): class_5250 {
   }

   public abstract fun nameOwned(name: String): class_5250 {
   }

   public open fun sendMessage(component: class_2561) {
      this.sendUpdate((new BattleMessagePacket(new class_2561[]{component})) as NetworkPacket<?>);
   }

   public open fun awardExperience(battlePokemon: BattlePokemon, experience: Int) {
   }

   public open fun sendUpdate(packet: NetworkPacket<*>) {
   }

   public open fun win(otherWinners: List<BattleActor>, losers: List<BattleActor>) {
   }

   public open fun lose(winners: List<BattleActor>, otherLosers: List<BattleActor>) {
   }

   @JvmStatic
   fun `writeShowdownResponse$lambda$0`(
      `$showdownMessages`: java.util.List,
      `this$0`: BattleActor,
      `$index`: IntRef,
      activeBattlePokemon: ActiveBattlePokemon,
      showdownMoveSet: ShowdownMoveset,
      forceSwitch: Boolean
   ): Int {
      `$showdownMessages`.add(`this$0`.responses.get(`$index`.element).toShowdownString(activeBattlePokemon, showdownMoveSet));
      return `$index`.element++;
   }
}
