package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.util.BufferUtilsKt
import io.netty.buffer.ByteBuf
import net.minecraft.class_9129

class InBattleGimmickMove {
   @JvmStatic
   public InBattleGimmickMove.Companion Companion = new InBattleGimmickMove.Companion(null);

   public final lateinit var move: String
   public final var target: MoveTarget = MoveTarget.self
   public final var disabled: Boolean


   public fun saveToBuffer(buffer: class_9129) {
      BufferUtilsKt.writeString(buffer as ByteBuf, this.getMove());
      BufferUtilsKt.writeEnumConstant(buffer as ByteBuf, this.target as java.lang.Enum);
      buffer.method_52964(this.disabled);
   }

   companion object Companion private constructor() {
      public fun loadFromBuffer(buffer: class_9129): InBattleGimmickMove {
         var var2: InBattleGimmickMove = new InBattleGimmickMove();
         var2.setMove(BufferUtilsKt.readString(buffer as ByteBuf));
         var2.setTarget(BufferUtilsKt.readEnumConstant(buffer as ByteBuf, MoveTarget.class) as MoveTarget);
         var2.setDisabled(buffer.readBoolean());
         return var2;
      }
   }
}
