package com.necro.raid.dens.common.showdown.bagitems;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CheerBagItem(CheerBagItem.CheerType cheerType) implements BagItem {
   @NotNull
   public String getItemName() {
      return this.cheerType.getItemId();
   }

   @NotNull
   public class_1792 getReturnItem() {
      return class_1802.field_8162;
   }

   public boolean canUse(@NotNull class_1799 stack, @NotNull PokemonBattle battle, @NotNull BattlePokemon target) {
      return target.getHealth() > 0;
   }

   @NotNull
   public String getShowdownInput(@NotNull BattleActor actor, @NotNull BattlePokemon pokemon, @Nullable String data) {
      return this.cheerType.getShowdownString() + " " + data;
   }

   public static enum CheerType {
      ATTACK("cheer_attack", "cheer_attack"),
      DEFENSE("cheer_defense", "cheer_defense"),
      HEAL("cheer_heal", "cheer_heal");

      private final String id;
      private final String showdownString;

      private CheerType(String id, String showdownString) {
         this.id = id;
         this.showdownString = showdownString;
      }

      public String getId() {
         return this.id;
      }

      public String getItemId() {
         return "item.cobblemonraiddens." + this.id;
      }

      public String getShowdownString() {
         return this.showdownString;
      }
   }
}
