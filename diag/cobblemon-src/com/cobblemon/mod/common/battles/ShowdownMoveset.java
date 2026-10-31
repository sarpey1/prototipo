package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.BufferUtilsKt
import com.cobblemon.mod.common.util.NetExtensionsKt
import io.netty.buffer.ByteBuf
import java.util.ArrayList
import kotlin.enums.EnumEntries
import kotlin.jvm.functions.Function1
import kotlin.jvm.functions.Function2
import kotlin.jvm.internal.SourceDebugExtension
import net.minecraft.class_2540
import net.minecraft.class_9129

@SourceDebugExtension(["SMAP\nShowdownActionRequest.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ShowdownActionRequest.kt\ncom/cobblemon/mod/common/battles/ShowdownMoveset\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,479:1\n1869#2,2:480\n1878#2,3:482\n1869#2,2:485\n1869#2,2:487\n1#3:489\n*S KotlinDebug\n*F\n+ 1 ShowdownActionRequest.kt\ncom/cobblemon/mod/common/battles/ShowdownMoveset\n*L\n297#1:480,2\n363#1:482,3\n302#1:485,2\n308#1:487,2\n*E\n"])
class ShowdownMoveset {
   public final lateinit var moves: List<InBattleMove>
   public final var trapped: Boolean
   public final var canMegaEvo: Boolean
   public final var canUltraBurst: Boolean
   public final var canZMove: List<InBattleGimmickMove?>?
   public final var canDynamax: Boolean
   public final var maxMoves: List<InBattleGimmickMove?>?
   public final var canTerastallize: String?
   public final val pendingGimmickUsedThisTurn: MutableList<com.cobblemon.mod.common.battles.ShowdownMoveset.Gimmick> = (new ArrayList()) as java.util.List


   public fun saveToBuffer(buffer: class_9129) {
      NetExtensionsKt.writeSizedInt(buffer as ByteBuf, IntSize.U_BYTE, this.getMoves().size());

      var `$this$forEach$iv`: java.lang.Iterable;
      for (Object element$iv : $this$forEach$iv) {
         (`element$iv` as InBattleMove).saveToBuffer(buffer);
      }

      buffer.method_52964(this.trapped);
      buffer.method_52964(this.canMegaEvo);
      buffer.method_52964(this.canUltraBurst);
      buffer.method_43826(this.canZMove, ShowdownMoveset::saveToBuffer$lambda$2);
      buffer.method_52964(this.canDynamax);
      buffer.method_43826(this.maxMoves, ShowdownMoveset::saveToBuffer$lambda$4);
      buffer.method_43826(this.canTerastallize, ShowdownMoveset::saveToBuffer$lambda$6);
   }

   public fun loadFromBuffer(buffer: class_9129): ShowdownMoveset {
      var moves: java.util.List = new ArrayList();
      var var3: Int = NetExtensionsKt.readSizedInt(buffer as ByteBuf, IntSize.U_BYTE);

      for (int var4 = 0; var4 < var3; var4++) {
         moves.add(InBattleMove.Companion.loadFromBuffer(buffer));
      }

      this.setMoves(moves);
      this.trapped = buffer.readBoolean();
      this.canMegaEvo = buffer.readBoolean();
      this.canUltraBurst = buffer.readBoolean();
      this.canZMove = buffer.method_43827(ShowdownMoveset::loadFromBuffer$lambda$2) as java.util.List<InBattleGimmickMove>;
      this.canDynamax = buffer.readBoolean();
      this.maxMoves = buffer.method_43827(ShowdownMoveset::loadFromBuffer$lambda$4) as java.util.List<InBattleGimmickMove>;
      this.canTerastallize = buffer.method_43827(ShowdownMoveset::loadFromBuffer$lambda$6) as java.lang.String;
      this.setGimmickMapping();
      return this;
   }

   public fun hasActiveGimmick(): Boolean {
      return !this.canDynamax && this.maxMoves != null;
   }

   public fun getGimmicks(): List<com.cobblemon.mod.common.battles.ShowdownMoveset.Gimmick> {
      var var10000: java.util.List;
      if (!this.hasActiveGimmick()) {
         var var1: java.util.List = CollectionsKt.createListBuilder();
         if (this.canMegaEvo) {
            var1.add(ShowdownMoveset.Gimmick.MEGA_EVOLUTION);
         }

         if (this.canUltraBurst) {
            var1.add(ShowdownMoveset.Gimmick.ULTRA_BURST);
         }

         if (this.canZMove != null) {
            var1.add(ShowdownMoveset.Gimmick.Z_POWER);
         }

         if (this.canDynamax) {
            var1.add(ShowdownMoveset.Gimmick.DYNAMAX);
         }

         if (this.canTerastallize != null) {
            var1.add(ShowdownMoveset.Gimmick.TERASTALLIZATION);
         }

         var10000 = CollectionsKt.toList(CollectionsKt.build(var1));
      } else {
         var10000 = CollectionsKt.emptyList();
      }

      return var10000;
   }

   public fun setGimmickMapping(): Unit? {
      var var10000: java.util.List = this.canZMove;
      if (this.canZMove == null) {
         var10000 = this.maxMoves;
      }

      var var13: Unit;
      if (var10000 != null) {
         var gimmickMoves: java.util.List = var10000;
         var `$this$forEachIndexed$iv`: java.lang.Iterable = this.getMoves();
         var `index$iv`: Int = 0;

         for (Object item$iv : $this$forEachIndexed$iv) {
            var var8: Int = `index$iv`++;
            if (var8 < 0) {
               CollectionsKt.throwIndexOverflow();
            }

            (`item$iv` as InBattleMove).setGimmickMove(gimmickMoves.get(var8) as InBattleGimmickMove);
         }

         var13 = Unit.INSTANCE;
      } else {
         var13 = null;
      }

      return var13;
   }

   public fun blockGimmick(gimmick: com.cobblemon.mod.common.battles.ShowdownMoveset.Gimmick) {
      switch (ShowdownMoveset.WhenMappings.$EnumSwitchMapping$0[gimmick.ordinal()]) {
         case 1:
            this.canMegaEvo = false;
            break;
         case 2:
            this.canDynamax = false;
            break;
         case 3:
            this.canUltraBurst = false;
            break;
         case 4:
            this.canZMove = null;
            break;
         default:
            this.canTerastallize = null;
      }
   }

   @JvmStatic
   fun `saveToBuffer$lambda$1`(`$buffer`: class_9129, var1: class_2540, canZMove: java.util.List): Unit {
      var `$this$forEach$iv`: java.lang.Iterable;
      for (Object element$iv : $this$forEach$iv) {
         `$buffer`.method_43826(`element$iv` as InBattleGimmickMove, ShowdownMoveset::saveToBuffer$lambda$1$0$1);
      }

      return Unit.INSTANCE;
   }

   @JvmStatic
   fun `saveToBuffer$lambda$1$0$0`(`$buffer`: class_9129, var1: class_2540, zmove: InBattleGimmickMove): Unit {
      zmove.saveToBuffer(`$buffer`);
      return Unit.INSTANCE;
   }

   @JvmStatic
   fun `saveToBuffer$lambda$1$0$1`(`$tmp0`: Function2, `object`: Any, object2: InBattleGimmickMove) {
      `$tmp0`.invoke(`object`, object2);
   }

   @JvmStatic
   fun `saveToBuffer$lambda$2`(`$tmp0`: Function2, `object`: Any, object2: java.util.List) {
      `$tmp0`.invoke(`object`, object2);
   }

   @JvmStatic
   fun `saveToBuffer$lambda$3`(`$buffer`: class_9129, var1: class_2540, maxMoves: java.util.List): Unit {
      var `$this$forEach$iv`: java.lang.Iterable;
      for (Object element$iv : $this$forEach$iv) {
         `$buffer`.method_43826(`element$iv` as InBattleGimmickMove, ShowdownMoveset::saveToBuffer$lambda$3$0$1);
      }

      return Unit.INSTANCE;
   }

   @JvmStatic
   fun `saveToBuffer$lambda$3$0$0`(`$buffer`: class_9129, var1: class_2540, maxMove: InBattleGimmickMove): Unit {
      maxMove.saveToBuffer(`$buffer`);
      return Unit.INSTANCE;
   }

   @JvmStatic
   fun `saveToBuffer$lambda$3$0$1`(`$tmp0`: Function2, `object`: Any, object2: InBattleGimmickMove) {
      `$tmp0`.invoke(`object`, object2);
   }

   @JvmStatic
   fun `saveToBuffer$lambda$4`(`$tmp0`: Function2, `object`: Any, object2: java.util.List) {
      `$tmp0`.invoke(`object`, object2);
   }

   @JvmStatic
   fun `saveToBuffer$lambda$5`(`$buffer`: class_9129, var1: class_2540, teraType: java.lang.String): Unit {
      var var10000: ByteBuf = `$buffer` as ByteBuf;
      BufferUtilsKt.writeString(var10000, teraType);
      return Unit.INSTANCE;
   }

   @JvmStatic
   fun `saveToBuffer$lambda$6`(`$tmp0`: Function2, `object`: Any, object2: java.lang.String) {
      `$tmp0`.invoke(`object`, object2);
   }

   @JvmStatic
   fun `loadFromBuffer$lambda$1`(`$moves`: java.util.List, `$buffer`: class_9129, it: class_2540): java.util.List {
      var zMoves: java.util.List = new ArrayList();
      var var4: Int = `$moves`.size();

      for (int var5 = 0; var5 < var4; var5++) {
         zMoves.add(`$buffer`.method_43827(ShowdownMoveset::loadFromBuffer$lambda$1$0$1));
      }

      return zMoves;
   }

   @JvmStatic
   fun `loadFromBuffer$lambda$1$0$0`(`$buffer`: class_9129, it: class_2540): InBattleGimmickMove {
      return InBattleGimmickMove.Companion.loadFromBuffer(`$buffer`);
   }

   @JvmStatic
   fun `loadFromBuffer$lambda$1$0$1`(`$tmp0`: Function1, `object`: Any): InBattleGimmickMove {
      return `$tmp0`.invoke(`object`) as InBattleGimmickMove;
   }

   @JvmStatic
   fun `loadFromBuffer$lambda$2`(`$tmp0`: Function1, `object`: Any): java.util.List {
      return `$tmp0`.invoke(`object`) as java.util.List;
   }

   @JvmStatic
   fun `loadFromBuffer$lambda$3`(`$moves`: java.util.List, `$buffer`: class_9129, it: class_2540): java.util.List {
      var maxMoves: java.util.List = new ArrayList();
      var var4: Int = `$moves`.size();

      for (int var5 = 0; var5 < var4; var5++) {
         maxMoves.add(`$buffer`.method_43827(ShowdownMoveset::loadFromBuffer$lambda$3$0$1));
      }

      return maxMoves;
   }

   @JvmStatic
   fun `loadFromBuffer$lambda$3$0$0`(`$buffer`: class_9129, it: class_2540): InBattleGimmickMove {
      return InBattleGimmickMove.Companion.loadFromBuffer(`$buffer`);
   }

   @JvmStatic
   fun `loadFromBuffer$lambda$3$0$1`(`$tmp0`: Function1, `object`: Any): InBattleGimmickMove {
      return `$tmp0`.invoke(`object`) as InBattleGimmickMove;
   }

   @JvmStatic
   fun `loadFromBuffer$lambda$4`(`$tmp0`: Function1, `object`: Any): java.util.List {
      return `$tmp0`.invoke(`object`) as java.util.List;
   }

   @JvmStatic
   fun `loadFromBuffer$lambda$5`(`$buffer`: class_9129, it: class_2540): java.lang.String {
      return BufferUtilsKt.readString(`$buffer` as ByteBuf);
   }

   @JvmStatic
   fun `loadFromBuffer$lambda$6`(`$tmp0`: Function1, `object`: Any): java.lang.String {
      return `$tmp0`.invoke(`object`) as java.lang.String;
   }

   enum class Gimmick {
      @JvmStatic
      MEGA_EVOLUTION("mega"),
      @JvmStatic
      ULTRA_BURST("ultra"),
      @JvmStatic
      Z_POWER("zmove"),
      @JvmStatic
      DYNAMAX("max"),
      @JvmStatic
      TERASTALLIZATION("terastal");
      private java.lang.String id;

      public final val id: String

      init {
         this.id = id;
      }

      @JvmStatic
      fun getEntries(): EnumEntries<ShowdownMoveset.Gimmick> {
         return $ENTRIES;
      }
   }
}
