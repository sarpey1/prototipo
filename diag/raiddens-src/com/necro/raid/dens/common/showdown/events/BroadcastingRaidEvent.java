package com.necro.raid.dens.common.showdown.events;

import com.necro.raid.dens.common.raids.RaidInstance;
import java.util.Collection;
import net.minecraft.class_3222;

public interface BroadcastingRaidEvent extends RaidEvent {
   @Override
   default void execute(RaidContext context) {
      this.broadcast(context.raid(), context.players());
   }

   default void broadcast(RaidInstance raid, Collection<class_3222> players) {
      for (class_3222 player : players) {
         this.run(raid, player);
      }
   }
}
