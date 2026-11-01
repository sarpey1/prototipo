package com.cobblemon.mod.common.entity.npc

import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.scheduling.ScheduledTask
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.BattleEndPacket
import com.cobblemon.mod.common.util.EntityExtensionsKt
import com.cobblemon.mod.common.util.LocalizationUtilsKt
import com.cobblemon.mod.common.util.MiscUtilsKt
import java.util.ArrayList
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.jvm.functions.Function1
import kotlin.jvm.internal.SourceDebugExtension
import net.minecraft.class_1297
import net.minecraft.class_243
import net.minecraft.class_2940
import net.minecraft.class_2945
import net.minecraft.class_5250

@SourceDebugExtension(["SMAP\nNPCBattleActor.kt\nKotlin\n*S Kotlin\n*F\n+ 1 NPCBattleActor.kt\ncom/cobblemon/mod/common/entity/npc/NPCBattleActor\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,89:1\n1#2:90\n1#2:101\n1617#3,9:91\n1869#3:100\n1870#3:102\n1626#3:103\n1563#3:104\n1634#3,3:105\n*S KotlinDebug\n*F\n+ 1 NPCBattleActor.kt\ncom/cobblemon/mod/common/entity/npc/NPCBattleActor\n*L\n57#1:101\n57#1:91,9\n57#1:100\n57#1:102\n57#1:103\n59#1:104\n59#1:105,3\n*E\n"])
open class NPCBattleActor(npc: NPCEntity, pokemonList: List<BattlePokemon>, skill: Int, artificialDecider: BattleAI)
   : AIBattleActor,
   EntityBackedBattleActor<NPCEntity> {
   public final val npc: NPCEntity
   public final val skill: Int
   public open val entity: NPCEntity
   public open val type: ActorType
   public open val initialPos: class_243

   init {
      var var10001: UUID = npc.method_5667();
      super(var10001, if (npc.getNpc().getRandomizePartyOrder()) CollectionsKt.shuffled(pokemonList) else pokemonList, artificialDecider);
      this.npc = npc;
      this.skill = skill;
      this.entity = this.npc;
      this.type = ActorType.NPC;
      this.initialPos = this.getEntity().method_19538();
   }

   public override fun getName(): class_5250 {
      return EntityExtensionsKt.effectiveName(this.npc as class_1297).method_27661();
   }

   public override fun nameOwned(name: String): class_5250 {
      var var2: Array<Any> = new Object[2];
      var var10003: class_5250 = this.getName();
      var2[0] = var10003;
      var2[1] = name;
      return LocalizationUtilsKt.battleLang("owned_pokemon", var2);
   }

   public constructor(npc: NPCEntity, party: PartyStore, skill: Int) : this(
         npc, PartyStore.toBattleTeam$default(party, false, npc.getNpc().getAutoHealParty(), null, 5, null), skill, null, 8, null
      )
   public override fun sendUpdate(packet: NetworkPacket<*>) {
      super.sendUpdate(packet);
      if (packet is BattleEndPacket) {
         if (this.npc.method_5805()) {
            var finalFuture: java.lang.Iterable = this.getPokemonList();
            var `$this$mapTo$iv$iv`: java.util.Collection = new ArrayList();

            for (Object element$iv$iv$iv : $this$mapNotNull$iv) {
               var var10000: PokemonEntity = (pokemonEntity as BattlePokemon).getEntity();
               if (var10000 != null) {
                  `$this$mapTo$iv$iv`.add(var10000);
               }
            }

            var allEntities: java.util.List = CollectionsKt.toMutableList(`$this$mapTo$iv$iv` as java.util.List);
            var var19: CompletableFuture = new CompletableFuture();
            var var20: java.lang.Iterable = allEntities;
            var `destination$iv$ivx`: java.util.Collection = new ArrayList(CollectionsKt.collectionSizeOrDefault(allEntities, 10));

            for (Object item$iv$iv : $this$map$iv) {
               `destination$iv$ivx`.add(NPCBattleActor::sendUpdate$lambda$1$0);
            }

            MiscUtilsKt.chainFutures((`destination$iv$ivx` as java.util.List).iterator(), var19);
            if (allEntities.isEmpty()) {
               var19.complete(Unit.INSTANCE);
            }

            var19.thenApply(NPCBattleActor::sendUpdate$lambda$3);
         } else {
            var var25: class_2945 = this.getEntity().method_5841();
            var var10001: class_2940 = NPCEntity.Companion.getBATTLE_IDS();
            EntityExtensionsKt.update(var25, var10001, NPCBattleActor::sendUpdate$lambda$4);
         }
      }
   }

   public override fun win(otherWinners: List<BattleActor>, losers: List<BattleActor>) {
      super.win(otherWinners, losers);
      PosableEntity.playAnimation$default(this.npc as PosableEntity, "win", null, 2, null);
   }

   public override fun lose(winners: List<BattleActor>, otherLosers: List<BattleActor>) {
      super.lose(winners, otherLosers);
      PosableEntity.playAnimation$default(this.npc as PosableEntity, "lose", null, 2, null);
   }

   @JvmStatic
   fun `sendUpdate$lambda$1$0`(`$pokemonEntity`: PokemonEntity): CompletableFuture {
      return `$pokemonEntity`.recallWithAnimation();
   }

   @JvmStatic
   fun `sendUpdate$lambda$2`(`this$0`: NPCBattleActor, it: Unit): ScheduledTask {
      return `this$0`.npc.after(3.0F, NPCBattleActor::sendUpdate$lambda$2$0);
   }

   @JvmStatic
   fun `sendUpdate$lambda$2$0`(`this$0`: NPCBattleActor): Unit {
      var var10000: class_2945 = `this$0`.getEntity().method_5841();
      var var10001: class_2940 = NPCEntity.Companion.getBATTLE_IDS();
      EntityExtensionsKt.update(var10000, var10001, NPCBattleActor::sendUpdate$lambda$2$0$0);
      return Unit.INSTANCE;
   }

   @JvmStatic
   fun `sendUpdate$lambda$2$0$0`(`this$0`: NPCBattleActor, it: java.util.Set): java.util.Set {
      return SetsKt.minus(it, `this$0`.getBattle().getBattleId());
   }

   @JvmStatic
   fun `sendUpdate$lambda$3`(`$tmp0`: Function1, p0: Any): ScheduledTask {
      return `$tmp0`.invoke(p0) as ScheduledTask;
   }

   @JvmStatic
   fun `sendUpdate$lambda$4`(`this$0`: NPCBattleActor, it: java.util.Set): java.util.Set {
      return SetsKt.minus(it, `this$0`.getBattle().getBattleId());
   }
}
