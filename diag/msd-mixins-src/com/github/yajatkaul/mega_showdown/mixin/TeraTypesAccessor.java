package com.github.yajatkaul.mega_showdown.mixin;

import com.cobblemon.mod.common.api.types.tera.TeraType;
import com.cobblemon.mod.common.api.types.tera.TeraTypes;
import java.util.HashMap;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
   value = {TeraTypes.class},
   remap = false
)
public interface TeraTypesAccessor {
   @Accessor("types")
   static HashMap<class_2960, TeraType> getTypes() {
      throw new AssertionError();
   }
}
