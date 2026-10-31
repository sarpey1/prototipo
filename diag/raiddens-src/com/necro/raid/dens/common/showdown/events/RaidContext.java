package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.necro.raid.dens.common.raids.RaidInstance;
import java.util.List;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;

public class RaidContext {
   private final RaidInstance raid;
   @Nullable
   private final class_3222 sourcePlayer;
   private final List<class_3222> allPlayers;
   @Nullable
   private final PokemonBattle sourceBattle;
   private final List<PokemonBattle> allBattles;

   public RaidContext(RaidInstance raid, @Nullable PokemonBattle battle) {
      this.raid = raid;
      this.allPlayers = raid.getPlayers();
      this.allBattles = raid.getBattles();
      this.sourcePlayer = battle != null && !battle.getPlayers().isEmpty() ? (class_3222)battle.getPlayers().getFirst() : null;
      if (battle != null) {
         this.sourceBattle = battle;
      } else if (this.allBattles.isEmpty()) {
         this.sourceBattle = null;
      } else {
         this.sourceBattle = this.allBattles.getFirst();
      }
   }

   public RaidInstance raid() {
      return this.raid;
   }

   @Nullable
   public class_3222 player() {
      return this.sourcePlayer;
   }

   public List<class_3222> players() {
      return this.allPlayers;
   }

   @Nullable
   public PokemonBattle battle() {
      return this.sourceBattle;
   }

   public List<PokemonBattle> battles() {
      return this.allBattles;
   }
}
