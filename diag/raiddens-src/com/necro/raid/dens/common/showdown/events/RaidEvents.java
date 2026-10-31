package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.CobblemonNetwork;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMusicPacket;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.scripts.triggers.RaidTrigger;
import com.necro.raid.dens.common.raids.scripts.triggers.TurnTrigger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.class_2338;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public class RaidEvents {
   public static record AddScriptRaidEvent(RaidTrigger<?> trigger) implements RaidEvent {
      @Override
      public void run(RaidInstance raid, @Nullable class_3222 player) {
         if (player != null && this.trigger instanceof TurnTrigger turnTrigger) {
            PokemonBattle battle = BattleRegistry.getBattleByParticipatingPlayer(player);
            if (battle != null) {
               turnTrigger.setOffset(battle.getTurn());
            }
         }

         raid.addTrigger(this.trigger);
      }
   }

   public static record HealRaidEvent(float ratio) implements RaidEvent {
      @Override
      public void run(RaidInstance raid, @Nullable class_3222 player) {
         raid.healBoss(this.ratio);
      }
   }

   public static record ModifyCatchRateAllRaidEvent(float mod, String operation) implements BroadcastingRaidEvent {
      @Override
      public void run(RaidInstance raid, @Nullable class_3222 player) {
         if (player != null) {
            if ("add".equals(this.operation)) {
               raid.addCatchRate(player, this.mod);
            } else if ("multiply".equals(this.operation)) {
               raid.mulCatchRate(player, this.mod);
            }
         }
      }
   }

   public static record ModifyCatchRateRaidEvent(float mod, String operation) implements RaidEvent {
      @Override
      public void run(RaidInstance raid, @Nullable class_3222 player) {
         if (player != null) {
            if ("add".equals(this.operation)) {
               raid.addCatchRate(player, this.mod);
            } else if ("multiply".equals(this.operation)) {
               raid.mulCatchRate(player, this.mod);
            }
         }
      }
   }

   public static record PlaySoundRaidEvent(class_2960 sound, boolean isMusic) implements BroadcastingRaidEvent {
      @Override
      public void broadcast(RaidInstance raid, Collection<class_3222> players) {
         if (this.isMusic) {
            CobblemonNetwork.sendPacketToPlayers(players, new BattleMusicPacket(this.sound, 1.0F, 1.0F, true));
         } else {
            raid.getBossEntity()
               .method_37908()
               .method_45447(null, raid.getBossEntity().method_24515(), class_3414.method_47908(this.sound), class_3419.field_15254);
         }
      }

      @Override
      public void run(RaidInstance raid, @Nullable class_3222 player) {
      }
   }

   public static record ReduceTimerRaidEvent(float ratio) implements RaidEvent {
      @Override
      public void run(RaidInstance raid, @Nullable class_3222 player) {
         raid.reduceTimer(this.ratio);
      }
   }

   public static record RunCommandRaidEvent(String target, String command) implements BroadcastingRaidEvent {
      @Override
      public void broadcast(RaidInstance raid, Collection<class_3222> players) {
         if (!players.isEmpty()) {
            MinecraftServer server = ((class_3222)players.toArray()[0]).method_5682();
            if (server != null) {
               if (!"player".equalsIgnoreCase(this.target) && !"players".equalsIgnoreCase(this.target)) {
                  this.runCommand(server, raid, this.command);
               } else {
                  for (class_3222 player : players) {
                     this.runCommand(server, raid, this.command.replace("%PLAYER%", player.method_5477().getString()));
                  }
               }
            }
         }
      }

      @Override
      public void run(RaidInstance raid, @Nullable class_3222 player) {
      }

      private void runCommand(MinecraftServer server, RaidInstance raid, String command) {
         command = command.startsWith("/") ? command.substring(1) : command;
         server.method_3734()
            .method_44252(
               server.method_3739(),
               command.replace("%POS%", this.getPos(raid.getBossEntity().method_24515())).replace("%BOSS%", raid.getBossEntity().method_5845())
            );
      }

      private String getPos(class_2338 blockPos) {
         return blockPos.method_10263() + " " + blockPos.method_10264() + " " + blockPos.method_10260();
      }
   }

   public static record ScaleBossRaidEvent(float scale, float rate) implements RaidEvent {
      private static final List<RaidEvents.ScaleBossRaidEvent.ScaleData> SCALE_QUEUE = new ArrayList<>();

      @Override
      public void run(RaidInstance raid, @Nullable class_3222 player) {
         SCALE_QUEUE.add(new RaidEvents.ScaleBossRaidEvent.ScaleData(raid.getBossEntity(), this.scale, this.rate));
      }

      public static void tick() {
         SCALE_QUEUE.removeIf(RaidEvents.ScaleBossRaidEvent.ScaleData::scale);
      }

      private static class ScaleData {
         private final PokemonEntity pokemonEntity;
         private float scale;
         private final float targetScale;
         private final float rate;
         private final int direction;

         private ScaleData(PokemonEntity pokemonEntity, float scale, float rate) {
            this.pokemonEntity = pokemonEntity;
            this.scale = pokemonEntity.getPokemon().getScaleModifier();
            this.targetScale = scale * this.scale;
            this.rate = Math.min(rate, 1.0F) * (this.targetScale - this.scale);
            this.direction = this.targetScale < this.scale ? -1 : 1;
         }

         private boolean scale() {
            if (this.pokemonEntity == null) {
               return true;
            } else {
               this.scale = this.scale + this.rate;
               this.pokemonEntity.getPokemon().setScaleModifier(this.scale);
               return this.isFinished();
            }
         }

         private boolean isFinished() {
            return this.direction == -1 ? this.scale <= this.targetScale : this.scale >= this.targetScale;
         }
      }
   }

   public static record TimerRaidEvent(int time) implements RaidEvent {
      @Override
      public void run(RaidInstance raid, @Nullable class_3222 player) {
         raid.initTimer(this.time);
      }
   }
}
