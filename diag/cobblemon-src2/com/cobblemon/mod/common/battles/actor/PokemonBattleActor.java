package com.cobblemon.mod.common.battles.actor

import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor
import com.cobblemon.mod.common.api.battles.model.actor.FleeableBattleActor
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.BattleEndPacket
import java.util.UUID
import net.minecraft.class_1937
import net.minecraft.class_243
import net.minecraft.class_2561
import net.minecraft.class_3218
import net.minecraft.class_3222
import net.minecraft.class_5250

open class PokemonBattleActor(uuid: UUID, pokemon: BattlePokemon, fleeDistance: Float, artificialDecider: BattleAI) : AIBattleActor(
         uuid, CollectionsKt.listOf(pokemon), artificialDecider
      ),
   EntityBackedBattleActor<PokemonEntity>,
   FleeableBattleActor {
   public final val pokemon: BattlePokemon
   public open val fleeDistance: Float
   public open val initialPos: class_243?
   public open val entity: PokemonEntity?
      public open get() {
         return this.pokemon.getEntity();
      }

   public open val type: ActorType

   init {
      this.pokemon = pokemon;
      this.fleeDistance = fleeDistance;
      var var10001: PokemonEntity = this.pokemon.getEntity();
      this.initialPos = if (var10001 != null) var10001.method_19538() else null;
      this.type = ActorType.WILD;
   }

   public override fun getName(): class_5250 {
      return this.pokemon.getEffectedPokemon().getSpecies().getTranslatedName();
   }

   public override fun nameOwned(name: String): class_5250 {
      var var10000: class_5250 = class_2561.method_43470(name);
      return var10000;
   }

   public override fun getWorldAndPosition(): Pair<class_3218, class_243>? {
      var ownerPlayer: class_3222 = this.pokemon.getEffectedPokemon().getOwnerPlayer();
      if (ownerPlayer != null) {
         return TuplesKt.to(ownerPlayer.method_51469(), ownerPlayer.method_19538());
      } else {
         var var10000: PokemonEntity = this.getEntity();
         label18:
         if (var10000 == null) {
            return null;
         } else {
            var var4: class_1937 = var10000.method_37908();
            return if ((var4 as? class_3218) == null) null else TuplesKt.to(var4 as? class_3218, var10000.method_19538());
         }
      }
   }

   public override fun sendUpdate(packet: NetworkPacket<*>) {
      super.sendUpdate(packet);
      if (packet is BattleEndPacket) {
         var var10000: PokemonEntity = this.getEntity();
         if (var10000 == null) {
            return;
         }

         var10000.setBattleId(null);
      }
   }
}
