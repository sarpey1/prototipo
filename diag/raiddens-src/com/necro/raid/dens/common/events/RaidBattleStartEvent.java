package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import net.minecraft.class_3222;
import org.jetbrains.annotations.NotNull;

public class RaidBattleStartEvent {
   private final class_3222 player;
   private final RaidBoss raidBoss;
   private final PokemonBattle battle;

   public RaidBattleStartEvent(@NotNull class_3222 player, @NotNull RaidBoss raidBoss, @NotNull PokemonBattle battle) {
      this.player = player;
      this.raidBoss = raidBoss;
      this.battle = battle;
   }

   public class_3222 getPlayer() {
      return this.player;
   }

   public RaidBoss getRaidBoss() {
      return this.raidBoss;
   }

   public PokemonBattle getBattle() {
      return this.battle;
   }
}
