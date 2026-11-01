package com.cobblemon.mod.common.client.battle

import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.ArrayList
import java.util.UUID
import net.minecraft.class_5250

class ClientBattleActor(showdownId: String, displayName: class_5250, uuid: UUID, type: ActorType) {
   public final val showdownId: String
   public final val displayName: class_5250
   public final val uuid: UUID
   public final val type: ActorType
   public final lateinit var side: ClientBattleSide
   public final var pokemon: MutableList<Pokemon>
   public final val activePokemon: MutableList<ActiveClientBattlePokemon>

   init {
      this.showdownId = showdownId;
      this.displayName = displayName;
      this.uuid = uuid;
      this.type = type;
      this.pokemon = new ArrayList<>();
      this.activePokemon = new ArrayList<>();
   }
}
