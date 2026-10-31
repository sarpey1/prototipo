package com.necro.raid.dens.common.showdown.events;

import java.util.List;
import net.minecraft.class_3222;

public record WithChanceEvent(float chance, List<AbstractEvent> events) implements AbstractEvent {
   @Override
   public void execute(RaidContext context) {
      class_3222 player = context.player();
      if (player != null) {
         if (player.method_59922().method_43057() < this.chance) {
            this.events.forEach(event -> event.execute(context));
         }
      }
   }
}
