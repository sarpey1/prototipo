package com.cobblemon.mod.common.api.battles.model.actor

import net.minecraft.class_1309
import net.minecraft.class_243

interface EntityBackedBattleActor<T extends class_1309> {
   public val entity: Any?
   public val initialPos: class_243?
}
