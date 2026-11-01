package com.cobblemon.mod.common.api.types.tera.gimmick

import com.cobblemon.mod.common.api.types.tera.TeraType
import com.cobblemon.mod.common.util.MiscUtilsKt
import net.minecraft.class_2561
import net.minecraft.class_2960
import net.minecraft.class_5250

class StellarTeraType : TeraType {
   @JvmStatic
   public StellarTeraType.Companion Companion = new StellarTeraType.Companion(null);
   @JvmStatic
   private class_2960 ID = MiscUtilsKt.cobblemonResource("stellar");
   @JvmStatic
   private class_5250 LANG = class_2561.method_43471("cobblemon.terra_type.stellar");

   public open val id: class_2960
   public open val name: String
   public open val legalAsStatic: Boolean
   public open val displayName: class_2561


   public open fun showdownId(): String {
      var var10000: java.lang.String = ID.method_12832();
      return var10000;
   }

   companion object Companion private constructor() {
      public final val ID: class_2960
      private final val LANG: class_5250

   }
}
