package com.cobblemon.mod.common.battles.actor

import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.util.LocalizationUtilsKt
import com.cobblemon.mod.common.util.MiscUtilsKt
import java.util.UUID
import net.minecraft.class_5250

open class TrainerBattleActor(trainerName: String, uuid: UUID, pokemonList: List<BattlePokemon>, artificialDecider: BattleAI) : AIBattleActor(
      uuid, pokemonList, artificialDecider
   ) {
   public final val trainerName: String
   public open val type: ActorType

   init {
      this.trainerName = trainerName;
      this.type = ActorType.NPC;
   }

   public override fun getName(): class_5250 {
      return MiscUtilsKt.asTranslated(this.trainerName);
   }

   public override fun nameOwned(name: String): class_5250 {
      var var2: Array<Any> = new Object[2];
      var var10003: class_5250 = this.getName();
      var2[0] = var10003;
      var2[1] = name;
      var var10000: class_5250 = LocalizationUtilsKt.battleLang("owned_pokemon", var2);
      return var10000;
   }
}
