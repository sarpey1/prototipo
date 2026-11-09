package com.cobblemon.mod.common.battles.actor

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.pokemon.experience.BattleExperienceSource
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceSource
import com.cobblemon.mod.common.api.text.TextKt
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.net.messages.client.battle.BattleMusicPacket
import com.cobblemon.mod.common.util.LocalizationUtilsKt
import com.cobblemon.mod.common.util.PlayerExtensionsKt
import java.util.ArrayList
import java.util.UUID
import kotlin.jvm.internal.SourceDebugExtension
import net.minecraft.class_243
import net.minecraft.class_2561
import net.minecraft.class_2960
import net.minecraft.class_3222
import net.minecraft.class_5250

@SourceDebugExtension(["SMAP\nPlayerBattleActor.kt\nKotlin\n*S Kotlin\n*F\n+ 1 PlayerBattleActor.kt\ncom/cobblemon/mod/common/battles/actor/PlayerBattleActor\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,71:1\n1#2:72\n1#2:83\n1617#3,9:73\n1869#3:82\n1870#3:84\n1626#3:85\n*S KotlinDebug\n*F\n+ 1 PlayerBattleActor.kt\ncom/cobblemon/mod/common/battles/actor/PlayerBattleActor\n*L\n68#1:83\n68#1:73,9\n68#1:82\n68#1:84\n68#1:85\n*E\n"])
class PlayerBattleActor(uuid: UUID, pokemonList: List<BattlePokemon>) : BattleActor(uuid, CollectionsKt.toMutableList(pokemonList)),
   EntityBackedBattleActor<class_3222> {
   public open val initialPos: class_243?
   public open val entity: class_3222?
      public open get() {
         return PlayerExtensionsKt.getPlayer(this.getUuid());
      }

   public final var battleTheme: class_2960?
      public final set(value) {
         if (this.isInitialized() && this.getBattle().getStarted()) {
            this.sendUpdate((new BattleMusicPacket(value, 0.0F, 0.0F, false, 14, null)) as NetworkPacket<?>);
         }

         this.battleTheme = value;
      }

   public open val type: ActorType

   init {
      var var10001: class_3222 = this.getEntity();
      this.initialPos = if (var10001 != null) var10001.method_19538() else null;
      this.type = ActorType.PLAYER;
   }

   public override fun getName(): class_5250 {
      var var10000: class_3222 = this.getEntity();
      if (var10000 != null) {
         var var1: class_2561 = var10000.method_5477();
         if (var1 != null) {
            var var2: class_5250 = var1.method_27661();
            if (var2 != null) {
               return var2;
            }
         }
      }

      return TextKt.red("Offline Player");
   }

   public override fun nameOwned(name: String): class_5250 {
      var var10000: class_5250 = LocalizationUtilsKt.battleLang("owned_pokemon", new Object[]{this.getName(), name});
      return var10000;
   }

   public open fun getPlayerUUIDs(): Set<UUID> {
      return SetsKt.setOf(this.getUuid());
   }

   public override fun awardExperience(battlePokemon: BattlePokemon, experience: Int) {
      if (!this.getBattle().isPvP() || Cobblemon.INSTANCE.getConfig().getAllowExperienceFromPvP()) {
         var source: BattleExperienceSource = new BattleExperienceSource(this.getBattle(), CollectionsKt.toList(battlePokemon.getFacedOpponents()));
         if (battlePokemon.getEffectedPokemon() == battlePokemon.getOriginalPokemon() && experience > 0) {
            var var4: class_3222 = PlayerExtensionsKt.getPlayer(this.getUuid());
            if (var4 != null && battlePokemon.getEffectedPokemon().addExperienceWithPlayer(var4, source as ExperienceSource, experience) != null) {
               return;
            }

            var `$this$awardExperience_u24lambda_u241`: PlayerBattleActor = this;
            battlePokemon.getEffectedPokemon().addExperience(source as ExperienceSource, experience);
         }
      }
   }

   public override fun sendUpdate(packet: NetworkPacket<*>) {
      var `$this$mapNotNull$iv`: java.lang.Iterable = this.getPlayerUUIDs();
      var `destination$iv$iv`: java.util.Collection = new ArrayList();

      for (Object element$iv$iv$iv : $this$mapNotNull$iv) {
         var var10000: class_3222 = PlayerExtensionsKt.getPlayer(`element$iv$iv$iv` as UUID);
         if (var10000 != null) {
            `destination$iv$iv`.add(var10000);
         }
      }

      CobblemonNetwork.sendPacketToPlayers(`destination$iv$iv` as java.util.List, packet);
   }
}
