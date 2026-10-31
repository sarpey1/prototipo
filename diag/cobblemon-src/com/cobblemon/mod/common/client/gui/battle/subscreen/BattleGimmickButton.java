package com.cobblemon.mod.common.client.gui.battle.subscreen

import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.cobblemon.mod.common.api.gui.GuiUtilsKt
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.moves.categories.DamageCategory
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.battles.InBattleGimmickMove
import com.cobblemon.mod.common.battles.InBattleMove
import com.cobblemon.mod.common.battles.MoveActionResponse
import com.cobblemon.mod.common.battles.MoveTarget
import com.cobblemon.mod.common.battles.ShowdownMoveset
import com.cobblemon.mod.common.battles.Targetable
import com.cobblemon.mod.common.battles.ShowdownMoveset.Gimmick
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection.MoveTile
import com.cobblemon.mod.common.util.MiscUtilsKt
import com.cobblemon.mod.common.util.math.SimpleMathExtensionsKt
import java.util.ArrayList
import java.util.Locale
import net.minecraft.class_1109
import net.minecraft.class_1113
import net.minecraft.class_310
import net.minecraft.class_3417
import net.minecraft.class_4587

abstract class BattleGimmickButton {
   @JvmStatic
   public BattleGimmickButton.Companion Companion = new BattleGimmickButton.Companion(null);
   @JvmStatic
   public int WIDTH = 36;
   @JvmStatic
   public int HEIGHT = 34;
   @JvmStatic
   public float SCALE = 0.5F;
   @JvmStatic
   public float XOFF = 18.0F;
   @JvmStatic
   public float YOFF = 17.0F;
   @JvmStatic
   public int SPACING = 26;

   public final val x: Float
   public final val y: Float
   public abstract val tiles: List<MoveTile>
   public final var toggled: Boolean
   private final val sfx: class_1109
   private final val texture: String

   open fun BattleGimmickButton(gimmick: ShowdownMoveset.Gimmick, x: Float, y: Float) {
      this.x = x;
      this.y = y;
      this.sfx = class_1109.method_4758(class_3417.field_14833, 1.0F);
      this.texture = gimmick.getId();
   }

   public fun render(matrices: class_4587, mouseX: Int, mouseY: Int, delta: Float) {
      GuiUtilsKt.blitk$default(
         matrices,
         MiscUtilsKt.cobblemonResource("textures/gui/battle/battle_gimmick_" + this.texture + ".png"),
         this.x * (float)2,
         this.y * (float)2,
         34,
         36,
         null,
         if (!this.toggled && !this.isHovered((double)mouseX, (double)mouseY)) 0 else 34,
         null,
         68,
         null,
         null,
         null,
         null,
         null,
         false,
         0.5F,
         64832,
         null
      );
   }

   public fun isHovered(mouseX: Double, mouseY: Double): Boolean {
      return mouseX >= (double)this.x && mouseX <= (double)(this.x + 18.0F) && mouseY >= (double)this.y && mouseY <= (double)(this.y + 17.0F);
   }

   public fun toggle(): Boolean {
      this.toggled = !this.toggled;
      class_310.method_1551().method_1483().method_4873(this.sfx as class_1113);
      return this.toggled;
   }

   companion object Companion private constructor() {
      public const val WIDTH: Int
      public const val HEIGHT: Int
      public const val SCALE: Float
      public const val XOFF: Float
      public const val YOFF: Float
      public const val SPACING: Int


      public fun create(gimmick: Gimmick, moveSelection: BattleMoveSelection, x: Float, y: Float): BattleGimmickButton {
         var var10000: BattleGimmickButton;
         switch (BattleGimmickButton.Companion.WhenMappings.$EnumSwitchMapping$0[gimmick.ordinal()]) {
            case 1:
            case 2:
               var10000 = (new ZPowerButton(moveSelection, x, y)) as BattleGimmickButton;
               break;
            case 3:
               var10000 = (new DynamaxButton(moveSelection, x, y)) as BattleGimmickButton;
               break;
            default:
               var10000 = new BattleGimmickButton(moveSelection, gimmick, x, y) {
                  private java.util.List<? extends BattleMoveSelection.MoveTile> tiles;

                  {
                     super(`$gimmick`, `$x`, `$y`);
                     var `$this$map$iv`: java.lang.Iterable = `$moveSelection`.getBaseTiles();
                     var `destination$iv$iv`: java.util.Collection = new ArrayList(CollectionsKt.collectionSizeOrDefault(`$this$map$iv`, 10));

                     for (Object item$iv$iv : $this$map$iv) {
                        `destination$iv$iv`.add(
                           new BattleGimmickButton.GimmickTile(
                              `$gimmick`,
                              `$moveSelection`,
                              (`item$iv$iv` as BattleMoveSelection.MoveTile).getMove(),
                              (`item$iv$iv` as BattleMoveSelection.MoveTile).getX(),
                              (`item$iv$iv` as BattleMoveSelection.MoveTile).getY()
                           )
                        );
                     }

                     this.tiles = `destination$iv$iv` as java.util.List<? extends BattleMoveSelection.MoveTile>;
                  }

                  @Override
                  public java.util.List<BattleMoveSelection.MoveTile> getTiles() {
                     return this.tiles;
                  }

                  public void setTiles(java.util.List<? extends BattleMoveSelection.MoveTile> <set-?>) {
                     this.tiles = `<set-?>`;
                  }
               };
         }

         return var10000;
      }
   }

   open class GimmickTile(gimmick: Gimmick, moveSelection: BattleMoveSelection, move: InBattleMove, x: Float, y: Float) : BattleMoveSelection.MoveTile(
         moveSelection, move, x, y
      ) {
      private final val gimmick: Gimmick
      protected final val gimmickMove: InBattleGimmickMove?
      public open val response: MoveActionResponse
         public open get() {
            return new MoveActionResponse(this.getMove().getId(), this.getTargetPnx(), this.gimmick.getId());
         }

      public open val targetList: List<Targetable>?
         public open get() {
            return if (this.gimmickMove != null)
               this.gimmickMove.getTarget().getTargetList().invoke(this.getMoveSelection().getRequest().getActivePokemon()) as java.util.List
               else
               super.getTargetList();
         }

      public open val selectable: Boolean
         public open get() {
            return if (this.gimmickMove != null) !this.gimmickMove.getDisabled() else super.getSelectable();
         }

      private final val gimmickMoveTemplate: MoveTemplate?
         private final get() {
            var var10000: InBattleGimmickMove = this.getMove().getGimmickMove();
            if (var10000 != null) {
               var var6: java.lang.String = var10000.getMove();
               if (var6 != null) {
                  var var7: java.lang.String = var6.toLowerCase(Locale.ROOT);
                  if (var7 != null) {
                     var var8: java.lang.String = ShowdownIdentifiable.Companion.getREGEX$common().replace(var7, "");
                     if (var8 != null) {
                        var gimmickTemplate: MoveTemplate;
                        var var10003: Int;
                        var var10004: ElementalType;
                        label61: {
                           gimmickTemplate = Moves.getByName(var8);
                           var9 = new MoveTemplate;
                           var10003 = if (gimmickTemplate != null) gimmickTemplate.getNum() else -1;
                           if (gimmickTemplate != null) {
                              var10004 = gimmickTemplate.getElementalType();
                              if (var10004 != null) {
                                 break label61;
                              }
                           }

                           var10004 = this.getMoveTemplate().getElementalType();
                        }

                        var var10005: DamageCategory;
                        var var10006: Double;
                        var var10007: MoveTarget;
                        label55: {
                           var10005 = this.getMoveTemplate().getDamageCategory();
                           var10006 = if (gimmickTemplate != null) gimmickTemplate.getPower() else this.getMoveTemplate().getPower();
                           if (gimmickTemplate != null) {
                              var10007 = gimmickTemplate.getTarget();
                              if (var10007 != null) {
                                 break label55;
                              }
                           }

                           var10007 = this.getMoveTemplate().getTarget();
                        }

                        var var10008: Double;
                        var var10009: Int;
                        var var10010: Int;
                        var var10011: Double;
                        var var10012: Array<java.lang.Double>;
                        label49: {
                           var10008 = if (gimmickTemplate != null) gimmickTemplate.getAccuracy() else this.getMoveTemplate().getAccuracy();
                           var10009 = if (gimmickTemplate != null) gimmickTemplate.getPp() else this.getMoveTemplate().getPp();
                           var10010 = if (gimmickTemplate != null) gimmickTemplate.getPriority() else this.getMoveTemplate().getPriority();
                           var10011 = if (gimmickTemplate != null) gimmickTemplate.getCritRatio() else this.getMoveTemplate().getCritRatio();
                           if (gimmickTemplate != null) {
                              var10012 = gimmickTemplate.getEffectChances();
                              if (var10012 != null) {
                                 break label49;
                              }
                           }

                           var10012 = this.getMoveTemplate().getEffectChances();
                        }

                        var9./* $VF: Unable to resugar constructor */<init>(
                           var8, var10003, var10004, var10005, var10006, var10007, var10008, var10009, var10010, var10011, var10012
                        );
                        return var9;
                     }
                  }
               }
            }

            return null;
         }


      init {
         this.gimmick = gimmick;
         var var10000: MoveTemplate = this.getGimmickMoveTemplate();
         if (var10000 != null) {
            this.setMoveTemplate(var10000);
            this.setRgb(SimpleMathExtensionsKt.toRGB(var10000.getElementalType().getHue()));
         }

         this.gimmickMove = move.getGimmickMove();
      }
   }
}
