package com.github.yajatkaul.mega_showdown.mixin;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
   value = {Moves.class},
   remap = false
)
public interface MovesAccessor {
   @Accessor("allMoves")
   static Map<String, MoveTemplate> getAllMoves() {
      throw new IllegalStateException("Mixin failed to apply");
   }

   @Accessor("idMapping")
   static Map<Integer, MoveTemplate> getIdMapping() {
      throw new IllegalStateException("Mixin failed to apply");
   }
}
