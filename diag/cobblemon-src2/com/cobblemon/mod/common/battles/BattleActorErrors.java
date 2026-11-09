package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import java.util.HashMap
import java.util.LinkedHashSet
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nBattleBuilder.kt\nKotlin\n*S Kotlin\n*F\n+ 1 BattleBuilder.kt\ncom/cobblemon/mod/common/battles/BattleActorErrors\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,561:1\n1#2:562\n*E\n"])
open class BattleActorErrors : HashMap<BattleActor, java.util.Set<BattleStartError>> {
   public open operator fun get(key: BattleActor): MutableSet<BattleStartError> {
      var var10000: java.util.Set = super.get(key) as java.util.Set;
      if (var10000 == null) {
         var var2: java.util.Set = new LinkedHashSet();
         (this as java.util.Map).put(key, var2);
         var10000 = var2;
      }

      return var10000;
   }
}
