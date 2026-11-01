package com.github.yajatkaul.mega_showdown.mixin;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
   value = {ElementalTypes.class},
   remap = false
)
public interface ElementsAccessor {
   @Accessor("allTypes")
   static List<ElementalType> getTypes() {
      throw new AssertionError();
   }
}
