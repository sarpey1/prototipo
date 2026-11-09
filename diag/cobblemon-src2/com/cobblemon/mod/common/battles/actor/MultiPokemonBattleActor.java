package com.cobblemon.mod.common.battles.actor

import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI
import com.cobblemon.mod.common.api.text.TextKt
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import java.util.UUID
import net.minecraft.class_2561
import net.minecraft.class_5250

open class MultiPokemonBattleActor(pokemonList: List<BattlePokemon>, artificialDecider: BattleAI, uuid: UUID) : AIBattleActor(
      uuid, pokemonList, artificialDecider
   ) {
   public open val type: ActorType = ActorType.WILD


   public override fun getName(): class_5250 {
      return TextKt.text("Wild Pokémon");
   }

   public override fun nameOwned(name: String): class_5250 {
      var var10000: class_5250 = class_2561.method_43470(name);
      return var10000;
   }
}
