package com.necro.raid.dens.common.showdown.events;

import com.necro.raid.dens.common.raids.RaidInstance;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;

public interface RaidEvent extends AbstractEvent {
   @Override
   default void execute(RaidContext context) {
      this.run(context.raid(), context.player());
   }

   void run(RaidInstance var1, @Nullable class_3222 var2);
}
