package com.cobblemon.mod.common.api.events.battles.instruction

import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.util.MoLangExtensionsKt
import java.util.ArrayList
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nMegaEvolutionEvent.kt\nKotlin\n*S Kotlin\n*F\n+ 1 MegaEvolutionEvent.kt\ncom/cobblemon/mod/common/api/events/battles/instruction/MegaEvolutionEvent\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,33:1\n774#2:34\n865#2,2:35\n774#2:37\n865#2,2:38\n774#2:40\n865#2,2:41\n*S KotlinDebug\n*F\n+ 1 MegaEvolutionEvent.kt\ncom/cobblemon/mod/common/api/events/battles/instruction/MegaEvolutionEvent\n*L\n27#1:34\n27#1:35,2\n28#1:37\n28#1:38,2\n29#1:40\n29#1:41,2\n*E\n"])
data class MegaEvolutionEvent(battle: PokemonBattle, pokemon: BattlePokemon) {
   public final val battle: PokemonBattle
   public final val pokemon: BattlePokemon
   public final val context: MutableMap<String, MoValue>

   init {
      this.battle = battle;
      this.pokemon = pokemon;
      var var3: Array<Pair> = new Pair[5];
      var3[0] = TuplesKt.to("battle", this.battle.getStruct());
      var `$this$filter$iv`: java.lang.Iterable = this.battle.getActors();
      var `destination$iv$iv`: java.util.Collection = new ArrayList();

      for (Object element$iv$iv : $this$filter$iv) {
         if ((`element$iv$iv` as BattleActor).getType() === ActorType.PLAYER) {
            `destination$iv$iv`.add(`element$iv$iv`);
         }
      }

      var3[1] = TuplesKt.to("players", MoLangExtensionsKt.asArrayValue(`destination$iv$iv` as java.util.List, MegaEvolutionEvent::context$lambda$1));
      `$this$filter$iv` = this.battle.getActors();
      `destination$iv$iv` = new ArrayList();

      for (Object element$iv$ivx : $this$filter$iv) {
         if ((`element$iv$ivx` as BattleActor).getType() === ActorType.NPC) {
            `destination$iv$iv`.add(`element$iv$ivx`);
         }
      }

      var3[2] = TuplesKt.to("npcs", MoLangExtensionsKt.asArrayValue(`destination$iv$iv` as java.util.List, MegaEvolutionEvent::context$lambda$3));
      `$this$filter$iv` = this.battle.getActors();
      `destination$iv$iv` = new ArrayList();

      for (Object element$iv$ivxx : $this$filter$iv) {
         if ((`element$iv$ivxx` as BattleActor).getType() === ActorType.WILD) {
            `destination$iv$iv`.add(`element$iv$ivxx`);
         }
      }

      var3[3] = TuplesKt.to("wild_pokemon", MoLangExtensionsKt.asArrayValue(`destination$iv$iv` as java.util.List, MegaEvolutionEvent::context$lambda$5));
      var3[4] = TuplesKt.to("pokemon", this.pokemon.getStruct());
      this.context = MapsKt.mutableMapOf(var3);
   }

   public operator fun component1(): PokemonBattle {
      return this.battle;
   }

   public operator fun component2(): BattlePokemon {
      return this.pokemon;
   }

   public fun copy(battle: PokemonBattle, pokemon: BattlePokemon): MegaEvolutionEvent {
      return new MegaEvolutionEvent(battle, pokemon);
   }

   public override fun toString(): String {
      return "MegaEvolutionEvent(battle=" + this.battle + ", pokemon=" + this.pokemon + ")";
   }

   public override fun hashCode(): Int {
      return this.battle.hashCode() * 31 + this.pokemon.hashCode();
   }

   public override operator fun equals(other: Any?): Boolean {
      if (this === other) {
         return true;
      } else if (other !is MegaEvolutionEvent) {
         return false;
      } else {
         var var2: MegaEvolutionEvent = other as MegaEvolutionEvent;
         if (!(this.battle == (other as MegaEvolutionEvent).battle)) {
            return false;
         } else {
            return this.pokemon == var2.pokemon;
         }
      }
   }

   @JvmStatic
   fun `context$lambda$1`(it: BattleActor): MoValue {
      return it.getStruct() as MoValue;
   }

   @JvmStatic
   fun `context$lambda$3`(it: BattleActor): MoValue {
      return it.getStruct() as MoValue;
   }

   @JvmStatic
   fun `context$lambda$5`(it: BattleActor): MoValue {
      return it.getStruct() as MoValue;
   }
}
