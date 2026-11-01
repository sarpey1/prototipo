package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.net.messages.client.battle.BattleInitializePacket.ActiveBattlePokemonDTO
import com.cobblemon.mod.common.net.messages.client.battle.BattleInitializePacket.BattleActorDTO
import java.util.UUID
import net.minecraft.class_5250

data class `BattleInitializePacket$BattleActorDTO`(uuid: UUID,
   displayName: class_5250,
   showdownId: String,
   activePokemon: List<ActiveBattlePokemonDTO?>,
   type: ActorType
) {
   public final val uuid: UUID
   public final val displayName: class_5250
   public final val showdownId: String
   public final val activePokemon: List<ActiveBattlePokemonDTO?>
   public final val type: ActorType

   init {
      this.uuid = uuid;
      this.displayName = displayName;
      this.showdownId = showdownId;
      this.activePokemon = activePokemon;
      this.type = type;
   }

   public operator fun component1(): UUID {
      return this.uuid;
   }

   public operator fun component2(): class_5250 {
      return this.displayName;
   }

   public operator fun component3(): String {
      return this.showdownId;
   }

   public operator fun component4(): List<ActiveBattlePokemonDTO?> {
      return this.activePokemon;
   }

   public operator fun component5(): ActorType {
      return this.type;
   }

   public fun copy(uuid: UUID, displayName: class_5250, showdownId: String, activePokemon: List<ActiveBattlePokemonDTO?>, type: ActorType): BattleActorDTO {
      return new BattleInitializePacket$BattleActorDTO(uuid, displayName, showdownId, activePokemon, type);
   }

   public override fun toString(): String {
      return "BattleActorDTO(uuid="
         + this.uuid
         + ", displayName="
         + this.displayName
         + ", showdownId="
         + this.showdownId
         + ", activePokemon="
         + this.activePokemon
         + ", type="
         + this.type
         + ")";
   }

   public override fun hashCode(): Int {
      return (((this.uuid.hashCode() * 31 + this.displayName.hashCode()) * 31 + this.showdownId.hashCode()) * 31 + this.activePokemon.hashCode()) * 31
         + this.type.hashCode();
   }

   public override operator fun equals(other: Any?): Boolean {
      if (this === other) {
         return true;
      } else if (other !is BattleInitializePacket$BattleActorDTO) {
         return false;
      } else {
         var var2: BattleInitializePacket$BattleActorDTO = other as BattleInitializePacket$BattleActorDTO;
         if (!(this.uuid == (other as BattleInitializePacket$BattleActorDTO).uuid)) {
            return false;
         } else if (!(this.displayName == var2.displayName)) {
            return false;
         } else if (!(this.showdownId == var2.showdownId)) {
            return false;
         } else if (!(this.activePokemon == var2.activePokemon)) {
            return false;
         } else {
            return this.type === var2.type;
         }
      }
   }
}
