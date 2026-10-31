package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.MiscUtilsKt
import com.cobblemon.mod.common.util.NetExtensionsKt
import com.cobblemon.mod.common.util.PlayerExtensionsKt
import io.netty.buffer.ByteBuf
import java.util.ArrayList
import kotlin.jvm.internal.SourceDebugExtension
import net.minecraft.class_2960
import net.minecraft.class_3222
import net.minecraft.class_9129

@SourceDebugExtension(["SMAP\nShowdownActionRequest.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ShowdownActionRequest.kt\ncom/cobblemon/mod/common/battles/ShowdownActionRequest\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,479:1\n1#2:480\n1869#3,2:481\n1869#3,2:483\n1869#3:485\n1869#3,2:486\n1870#3:488\n*S KotlinDebug\n*F\n+ 1 ShowdownActionRequest.kt\ncom/cobblemon/mod/common/battles/ShowdownActionRequest\n*L\n46#1:481,2\n48#1:483,2\n74#1:485\n75#1:486,2\n74#1:488\n*E\n"])
class ShowdownActionRequest(wait: Boolean, active: MutableList<ShowdownMoveset>?, forceSwitch: List<Boolean>, noCancel: Boolean, side: ShowdownSide?) {
   public final var wait: Boolean
   public final var active: MutableList<ShowdownMoveset>?
   public final var forceSwitch: List<Boolean>
   public final var noCancel: Boolean
   public final var side: ShowdownSide?

   init {
      this.wait = wait;
      this.active = active;
      this.forceSwitch = forceSwitch;
      this.noCancel = noCancel;
      this.side = side;
   }

   public fun <T, E : Targetable> iterate(activePokemon: List<Any>, iterator: (Any, ShowdownMoveset?, Boolean) -> Any): List<Any> {
      var size: Int = Integer.max(if (this.active != null) this.active.size() else 0, this.forceSwitch.size());
      var responses: java.util.List = new ArrayList();

      for (int var5 = 0; var5 < size; var5++) {
         if (activePokemon.size() <= var5) {
            throw new IllegalStateException("No active Pokémon for slot " + var5 + " but needed to choose action for it?");
         }

         var activeBattlePokemon: Targetable = activePokemon.get(var5) as Targetable;
         var var10000: ShowdownMoveset;
         if (this.active != null) {
            var it: java.util.List = this.active;
            var10000 = if (this.active.size() > var5) it.get(var5) as ShowdownMoveset else null;
         } else {
            var10000 = null;
         }

         var var14: java.util.List = this.forceSwitch;
         responses.add(iterator.invoke(activeBattlePokemon, var10000, this.forceSwitch.size() > var5 && var14.get(var5) as java.lang.Boolean));
      }

      return responses;
   }

   public fun saveToBuffer(buffer: class_9129) {
      buffer.method_52964(this.wait);
      NetExtensionsKt.writeSizedInt(buffer as ByteBuf, IntSize.U_BYTE, if (this.active != null) this.active.size() else 0);
      if (this.active != null) {
         var `$i$f$forEach`: java.lang.Iterable;
         for (Object element$iv : $i$f$forEach) {
            (p0 as ShowdownMoveset).saveToBuffer(buffer);
         }
      }

      NetExtensionsKt.writeSizedInt(buffer as ByteBuf, IntSize.U_BYTE, this.forceSwitch.size());

      var `$this$forEach$iv`: java.lang.Iterable;
      for (Object element$iv : $this$forEach$iv) {
         buffer.method_52964(var11 as java.lang.Boolean);
      }

      buffer.method_52964(this.noCancel);
      buffer.method_52964(this.side != null);
      if (this.side != null) {
         this.side.saveToBuffer(buffer);
      }
   }

   public fun loadFromBuffer(buffer: class_9129): ShowdownActionRequest {
      this.wait = buffer.readBoolean();
      var activeSize: Int = NetExtensionsKt.readSizedInt(buffer as ByteBuf, IntSize.U_BYTE);
      if (activeSize > 0) {
         var forceSwitch: java.util.List = new ArrayList();

         for (int var4 = 0; var4 < activeSize; var4++) {
            forceSwitch.add(new ShowdownMoveset().loadFromBuffer(buffer));
         }

         this.active = forceSwitch;
      }

      var var8: java.util.List = new ArrayList();
      var var9: Int = NetExtensionsKt.readSizedInt(buffer as ByteBuf, IntSize.U_BYTE);

      for (int it = 0; it < var9; it++) {
         var8.add(buffer.readBoolean());
      }

      this.forceSwitch = var8;
      this.noCancel = buffer.readBoolean();
      if (buffer.readBoolean()) {
         this.side = new ShowdownSide().loadFromBuffer(buffer);
      }

      return this;
   }

   public fun sanitize(battle: PokemonBattle, battleActor: BattleActor) {
      var var6: java.util.Iterator = battle.getPlayers().iterator();

      var var10000: Any;
      while (true) {
         if (var6.hasNext()) {
            var `element$iv`: Any = var6.next();
            if (!((`element$iv` as class_3222).method_5667() == battleActor.getUuid())) {
               continue;
            }

            var10000 = `element$iv`;
            break;
         }

         var10000 = null;
         break;
      }

      var10000 = var10000 as class_3222;
      if (var10000 as class_3222 != null) {
         var player: class_3222 = (class_3222)var10000;
         if (this.active != null) {
            var `$this$forEach$iv`: java.lang.Iterable;
            for (Object element$iv : $this$forEach$iv) {
               var var20: ShowdownMoveset = var19 as ShowdownMoveset;

               var `$this$forEach$ivx`: java.lang.Iterable;
               for (Object element$ivx : $this$forEach$ivx) {
                  var gimmick: ShowdownMoveset.Gimmick = `element$ivx` as ShowdownMoveset.Gimmick;
                  switch (ShowdownActionRequest.WhenMappings.$EnumSwitchMapping$0[((ShowdownMoveset.Gimmick)element$ivx).ordinal()]) {
                     case 1:
                        var10000 = MiscUtilsKt.cobblemonResource("key_stone");
                        break;
                     case 2:
                        var10000 = MiscUtilsKt.cobblemonResource("dynamax_band");
                        break;
                     case 3:
                        var10000 = MiscUtilsKt.cobblemonResource("tera_orb");
                        break;
                     default:
                        var10000 = MiscUtilsKt.cobblemonResource("z_ring");
                  }

                  if (!PlayerExtensionsKt.hasKeyItem(player, (class_2960)var10000)) {
                     var20.blockGimmick(gimmick);
                  }
               }
            }
         }
      }
   }

   fun ShowdownActionRequest() {
      this(false, null, null, false, null, 31, null);
   }
}
