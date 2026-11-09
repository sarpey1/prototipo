package com.cobblemon.mod.common.api.battles.model.actor

import net.minecraft.class_243
import net.minecraft.class_3218

interface FleeableBattleActor {
   public val fleeDistance: Float

   public abstract fun getWorldAndPosition(): Pair<class_3218, class_243>? {
   }
}
