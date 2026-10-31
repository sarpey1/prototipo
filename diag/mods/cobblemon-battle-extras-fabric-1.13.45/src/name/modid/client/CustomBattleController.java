package name.modid.client;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.cobblemon.mod.common.client.battle.SingleActionRequest;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import name.modid.CobblemonBattleInfoClient;
import name.modid.client.BattleUiThemePalette.Theme;
import name.modid.client.CustomBattleController.CategoryChipStyle;
import name.modid.client.CustomBattleController.GeneralActionButtonStyle;
import name.modid.client.CustomBattleController.MatchupBorderColor;
import name.modid.client.CustomBattleController.MatchupLineDisplay;
import name.modid.client.CustomBattleController.MoveDisplayInfo;
import name.modid.client.CustomBattleController.MoveEffectivenessTint;
import name.modid.client.CustomBattleController.MoveTileVisualData;
import name.modid.client.CustomBattleController.OpponentMatchupEntry;
import name.modid.client.CustomBattleController.SwitchTooltipCacheData;
import name.modid.client.CustomBattleController.TileSnapshot;
import name.modid.client.MoveDamagePreviewCalculator.AttackContext;
import name.modid.client.MoveDamagePreviewCalculator.DamagePreview;
import name.modid.client.MoveDamagePreviewCalculator.DefenceContext;
import name.modid.client.net.TeraTypeClientData;
import name.modid.platform.Services;
import net.minecraft.class_124;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2588;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

public class CustomBattleController {
   private static boolean initialized = false;
   private static int selectedButton = 0;
   private static boolean wasInBattle = false;
   private static class_437 lastRegisteredScreen = null;
   private static String currentSelectionType = "none";
   private static String lastActionSelectionClassName = "";
   private static int maxButtons = 4;
   private static int numMoveTiles = 4;
   private static int targetGridColumns = 2;
   private static boolean upPressed = false;
   private static boolean downPressed = false;
   private static boolean leftPressed = false;
   private static boolean rightPressed = false;
   private static boolean selectPressed = false;
   private static boolean backPressed = false;
   private static long lastInputTime = 0L;
   private static final long INPUT_COOLDOWN = 150L;
   private static long lastNavigationTime = 0L;
   private static final long NAVIGATION_HIDE_DURATION = 80L;
   private static long battleGuiClosedTime = 0L;
   private static final long JUMP_SUPPRESS_DURATION = 200L;
   private static boolean hasAutoExpandedLog = false;
   private static int BUTTON_WIDTH = 139;
   private static int BUTTON_HEIGHT = 26;
   private static final int TYPE_SHEET_WIDTH = 648;
   private static final int TYPE_SHEET_HEIGHT = 36;
   private static final int TYPE_ICON_SOURCE_SIZE = 36;
   private static final int MOVE_TILE_TYPE_BADGE_SIZE = 16;
   private static final int MOVE_TILE_TYPE_ICON_SIZE = 12;
   private static final int MOVE_TILE_NEUTRAL_SHELL_TOP = -10854549;
   private static final int MOVE_TILE_NEUTRAL_SHELL_BOTTOM = -13947080;
   private static final int MOVE_TILE_NEUTRAL_BORDER_TOP = -2630685;
   private static final int MOVE_TILE_NEUTRAL_BORDER_BOTTOM = -15328992;
   private static final int MOVE_TILE_NEUTRAL_TOP_SHEEN = 1795162111;
   private static final int MOVE_TILE_NEUTRAL_LOWER_BAND_TOP = -500683724;
   private static final int MOVE_TILE_NEUTRAL_LOWER_BAND_BOTTOM = -301199596;
   private static final int MOVE_TILE_NEUTRAL_ACCENT_LINE = -1799897399;
   private static final int MOVE_TILE_NEUTRAL_ACCENT_SHADOW = -1945564400;
   private static final int MOVE_TILE_NEUTRAL_TEXT = -1249291;
   private static final int MOVE_TILE_NEUTRAL_TEXT_SHADOW = -805306368;
   private static final int MOVE_TILE_NEUTRAL_DIM_TEXT = -3748648;
   private static final int MOVE_TILE_NEUTRAL_BADGE_BORDER = -3090720;
   private static final int MOVE_TILE_NEUTRAL_BADGE_TOP = -9670016;
   private static final int MOVE_TILE_NEUTRAL_BADGE_BOTTOM = -14012873;
   private static final int MOVE_TILE_NEUTRAL_BADGE_SHEEN = 1962934271;
   private static final int MOVE_TILE_NEUTRAL_ICON_ZONE_TOP = -11051671;
   private static final int MOVE_TILE_NEUTRAL_ICON_ZONE_BOTTOM = -14670804;
   private static final int MOVE_TILE_NEUTRAL_ICON_ZONE_SEPARATOR = -1462314012;
   private static final int MOVE_TILE_NEUTRAL_ICON_ZONE_SHADOW = -1272962262;
   private static final int MOVE_TILE_NEUTRAL_CHIP_TOP = -598451867;
   private static final int MOVE_TILE_NEUTRAL_CHIP_BOTTOM = -433838032;
   private static final int MOVE_TILE_NEUTRAL_CHIP_BORDER = -943996210;
   private static int MEGA_ICON_OFFSET_X_SWITCH = 5;
   private static int MEGA_ICON_OFFSET_Y_SWITCH = -5;
   private static int MEGA_ICON_SIZE_SWITCH = 8;
   private static Object[] cachedTiles = null;
   private static class_437 cachedScreen = null;
   private static Object cachedActionSelection = null;
   private static long cachedTilesExpiresAt = 0L;
   private static final long TILE_CACHE_TTL_MS = 50L;
   private static final IdentityHashMap<Object, TileSnapshot> tileSnapshotCache = new IdentityHashMap<>();
   private static final long SWITCH_TOOLTIP_CACHE_TTL_MS = 750L;
   private static final Map<String, SwitchTooltipCacheData> switchTooltipCache = new ConcurrentHashMap<>();
   private static final long SWITCH_TYPE_ICON_CACHE_TTL_MS = 1500L;
   private static final Map<Integer, CustomBattleController.SwitchTypeIconCacheData> switchTypeIconCache = new ConcurrentHashMap<>();
   private static final Map<String, class_2960> teraIconResourceCache = new ConcurrentHashMap<>();
   private static boolean deferredSwitchTooltipQueued = false;
   private static Object deferredSwitchTooltipTile = null;
   private static int deferredSwitchTooltipTileX = 0;
   private static int deferredSwitchTooltipTileY = 0;
   private static int deferredSwitchTooltipTileWidth = 0;
   private static int deferredSwitchTooltipTileHeight = 0;
   private static final class_2960 COBBLEMON_TYPES_SHEET = class_2960.method_60654("cobblemon:textures/gui/types.png");
   private static final class_2960 MEGA_ICON_SHEET = class_2960.method_60654("cobblemon-battle-extras:textures/gui/mega_icon.png");
   private static final class_2960 SWITCH_TOOLTIP_TEXTURE = class_2960.method_60654("cobblemon-battle-extras:textures/gui/switch_tooltip.png");
   private static final int SWITCH_TOOLTIP_TEXTURE_WIDTH = 113;
   private static final int SWITCH_TOOLTIP_TEXTURE_HEIGHT = 126;
   private static final String MEGA_SHOWDOWN_TERA_ICON_BASE = "mega_showdown:textures/gui/summary/tera_types/";
   private static Boolean cachedMegaShowdownLoaded = null;
   private static List<OpponentMatchupEntry> cachedOpponentEntries = null;
   private static long opponentEntriesCacheExpiry = 0L;
   private static final long OPPONENT_ENTRIES_CACHE_TTL_MS = 500L;
   private static boolean minimisedBackPressed = false;
   private static final Map<String, Map<String, int[]>> cachedPartyPP = new ConcurrentHashMap<>();
   private static final Map<String, String> cachedTeraTypes = new ConcurrentHashMap<>();
   private static final int GLFW_KEY_UP = 265;
   private static final int GLFW_KEY_DOWN = 264;
   private static final int GLFW_KEY_LEFT = 263;
   private static final int GLFW_KEY_RIGHT = 262;
   private static final int GLFW_KEY_W = 87;
   private static final int GLFW_KEY_A = 65;
   private static final int GLFW_KEY_S = 83;
   private static final int GLFW_KEY_D = 68;
   private static final int GLFW_KEY_ENTER = 257;
   private static final int GLFW_KEY_KP_ENTER = 335;
   private static final int GLFW_KEY_SPACE = 32;
   private static final int GLFW_KEY_BACKSPACE = 259;
   private static final int GLFW_KEY_ESCAPE = 256;
   private static final int GLFW_PRESS = 1;
   private static final float STICK_DEADZONE = 0.5F;
   private static boolean wasInBattleForJump = false;

   public static void init() {
      if (!initialized) {
         initialized = true;
         Services.EVENTS.registerClientTick(CustomBattleController::onClientTick);
         Services.EVENTS.registerScreenRender((screen, graphics, mouseX, mouseY, tickDelta) -> {
            if (isBattleGUI(screen)) {
               onScreenRender(screen, graphics, mouseX, mouseY, tickDelta);
            }
         });
         CobblemonBattleInfoClient.debug("[CustomController] Initialized custom battle controller", new Object[0]);
      }
   }

   private static void onClientTick(class_310 mc) {
      try {
         ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
         boolean inBattle = battle != null;
         if (inBattle && !wasInBattle) {
            onBattleStart();
         }

         if (!inBattle && wasInBattle) {
            onBattleEnd();
         }

         wasInBattle = inBattle;
         if (inBattle && isBattleGUI(mc.field_1755) && !hasAutoExpandedLog && BattleExtrasConfig.isAutoExpandLogEnabled()) {
            autoExpandBattleLog(mc.field_1755);
            hasAutoExpandedLog = true;
         }

         if (!BattleExtrasConfig.isControllerNavigationEnabled()) {
            return;
         }

         if (inBattle && isBattleGUI(mc.field_1755)) {
            handleInput(mc, mc.field_1755);
         }

         if (BattleExtrasConfig.isBButtonReenterBattleEnabled() && inBattle && !isBattleGUI(mc.field_1755) && mc.field_1755 == null) {
            handleMinimisedBattleInput(battle);
         }
      } catch (Throwable var3) {
      }
   }

   private static void handleMinimisedBattleInput(ClientBattle battle) {
      boolean backNow = isBackPressed();
      if (backNow && !minimisedBackPressed) {
         CobblemonBattleInfoClient.debug("[CustomController] B pressed while battle minimised, opening BattleGUI", new Object[0]);
         openBattleGUI();
         backPressed = true;
      }

      minimisedBackPressed = backNow;
   }

   private static void onScreenRender(class_437 screen, class_332 graphics, int mouseX, int mouseY, float delta) {
      try {
         if (!isBattleGUI(screen)) {
            return;
         }

         if (currentSelectionType.equals("switch") && BattleExtrasConfig.isTypeIconsEnabled() && isSwitchScreenActive(screen)) {
            renderSwitchScreenTypeIcons(graphics);
         }

         if (currentSelectionType.equals("switch") && BattleExtrasConfig.isSwitchMatchupBorderEnabled() && isSwitchScreenActive(screen)) {
            renderSwitchScreenMatchupBorders(graphics);
         }

         boolean renderedLingeringGeneralButtons = shouldRenderLingeringGeneralActionButtons(screen);
         if (renderedLingeringGeneralButtons) {
            renderCustomGeneralActionButtons(graphics, mouseX, mouseY);
         }

         renderMouseHoverTooltips(graphics, mouseX, mouseY);
         if (!renderedLingeringGeneralButtons && shouldHideNativeGeneralActionButtons()) {
            renderCustomGeneralActionButtons(graphics, mouseX, mouseY);
         }

         if (shouldRenderCustomMoveTiles()) {
            renderCustomMoveTiles(graphics, mouseX, mouseY);
         }

         if (!BattleExtrasConfig.isSelectionHighlightEnabled()) {
            return;
         }

         if (!BattleExtrasConfig.isControllerNavigationEnabled()) {
            return;
         }

         drawButtonHighlight(graphics, screen, mouseX, mouseY);
      } catch (Throwable var6) {
      }
   }

   private static boolean shouldRenderLingeringGeneralActionButtons(class_437 screen) {
      if (!BattleExtrasConfig.useCustomActionButtons()) {
         return false;
      } else {
         return isBattleGUI(screen) && !shouldHideNativeGeneralActionButtons()
            ? "general".equals(currentSelectionType) && cachedTiles != null && cachedTiles.length > 0 && isSwitchScreenActive(screen)
            : false;
      }
   }

   public static boolean shouldHideNativeGeneralActionButtons() {
      if (!BattleExtrasConfig.useCustomActionButtons()) {
         return false;
      } else {
         class_437 screen = class_310.method_1551().field_1755;
         if (!isBattleGUI(screen)) {
            return false;
         } else {
            try {
               Object actionSelection = getActionSelection(screen);
               return actionSelection != null && actionSelection.getClass().getName().contains("GeneralActionSelection");
            } catch (Throwable var2) {
               return false;
            }
         }
      }
   }

   public static boolean shouldHideNativeBattleOptionTile(Object tile) {
      if (BattleExtrasConfig.useCustomActionButtons() && tile != null) {
         String key = getBattleOptionKey(tile);
         String label = getBattleOptionLabel(tile).toLowerCase(Locale.ROOT);
         return key.contains(".fight")
            || key.contains(".switch")
            || key.contains(".capture")
            || key.contains(".bag")
            || key.contains(".run")
            || key.contains(".forfeit")
            || label.contains("fight")
            || label.contains("switch")
            || label.contains("catch")
            || label.contains("capture")
            || label.contains("bag")
            || label.contains("run")
            || label.contains("forfeit");
      } else {
         return false;
      }
   }

   private static boolean shouldRenderCustomMoveTiles() {
      if (!BattleExtrasConfig.useCustomMoveTiles()) {
         return false;
      } else {
         class_437 screen = class_310.method_1551().field_1755;
         return !isBattleGUI(screen) ? false : hasRenderableMoveTiles(cachedTiles);
      }
   }

   private static void renderCustomGeneralActionButtons(class_332 graphics, int mouseX, int mouseY) {
      Object[] tiles = cachedTiles;
      if (tiles != null && tiles.length != 0 && "general".equals(currentSelectionType)) {
         Theme theme = BattleUiThemePalette.getActiveBattleLogTheme();
         class_327 font = class_310.method_1551().field_1772;

         for (Object tile : tiles) {
            if (tile != null) {
               String tileClassName = tile.getClass().getName();
               if (tileClassName.contains("BattleOptionTile")) {
                  int x = getTileX(tile);
                  int y = getTileY(tile);
                  int width = getTileWidth(tile);
                  int height = getTileHeight(tile);
                  if (x > 0 && y > 0 && width > 0 && height > 0) {
                     String label = getBattleOptionLabel(tile);
                     GeneralActionButtonStyle style = resolveGeneralActionButtonStyle(tile, label);
                     boolean hovered = isTileHovered(tile, mouseX, mouseY);
                     boolean selected = isTileControllerSelected(tile);
                     renderCustomGeneralActionButton(graphics, font, x, y, width, height, label, style, hovered, selected, theme);
                  }
               }
            }
         }
      }
   }

   private static void renderCustomMoveTiles(class_332 graphics, int mouseX, int mouseY) {
      Object[] tiles = cachedTiles;
      if (tiles != null && tiles.length != 0) {
         int tileCount = countRenderableMoveTiles(tiles);
         if (tileCount > 0) {
            class_327 font = class_310.method_1551().field_1772;
            float opacity = getCurrentActionSelectionOpacity();
            if (!(opacity <= 0.05F)) {
               List<OpponentMatchupEntry> targetEntries = getActiveOpponentEntries();

               for (int index = 0; index < tileCount; index++) {
                  Object tile = tiles[index];
                  if (tile != null) {
                     int x = getTileX(tile);
                     int y = getTileY(tile);
                     int width = getTileWidth(tile);
                     int height = getTileHeight(tile);
                     if (x > 0 && y > 0 && width > 0 && height > 0) {
                        MoveTileVisualData data = resolveMoveTileVisualData(tile);
                        if (data != null) {
                           boolean hovered = isTileHovered(tile, mouseX, mouseY);
                           boolean selected = isTileControllerSelected(tile);
                           renderCustomMoveTile(graphics, font, x, y, width, height, data, hovered, selected, opacity, targetEntries);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static float getCurrentActionSelectionOpacity() {
      class_437 screen = class_310.method_1551().field_1755;
      if (!isBattleGUI(screen)) {
         return 1.0F;
      } else {
         try {
            Object actionSelection = getActionSelection(screen);
            if (actionSelection == null) {
               return 1.0F;
            }

            Method getOpacityMethod = actionSelection.getClass().getMethod("getOpacity");
            Object result = getOpacityMethod.invoke(actionSelection);
            if (result instanceof Float value) {
               return value;
            }

            if (result instanceof Number number) {
               return number.floatValue();
            }
         } catch (Throwable var5) {
         }

         return 1.0F;
      }
   }

   private static MoveTileVisualData resolveMoveTileVisualData(Object tile) {
      TileSnapshot snapshot = getTileSnapshot(tile);
      return snapshot != null && snapshot.moveData() != null ? snapshot.moveData() : resolveMoveTileVisualDataUncached(tile);
   }

   private static MoveTileVisualData resolveMoveTileVisualDataUncached(Object tile) {
      try {
         Field moveTemplateField = findFieldInHierarchy(tile.getClass(), "moveTemplate");
         if (moveTemplateField == null) {
            return null;
         } else {
            moveTemplateField.setAccessible(true);
            if (moveTemplateField.get(tile) instanceof MoveTemplate moveTemplate) {
               Object moveObject = null;
               Field moveField = findFieldInHierarchy(tile.getClass(), "move");
               if (moveField != null) {
                  moveField.setAccessible(true);
                  moveObject = moveField.get(tile);
               }

               Object elementalTypeObject = null;

               try {
                  Field elementalTypeField = findFieldInHierarchy(tile.getClass(), "elementalType");
                  if (elementalTypeField != null) {
                     elementalTypeField.setAccessible(true);
                     elementalTypeObject = elementalTypeField.get(tile);
                  }
               } catch (Throwable var27) {
                  elementalTypeObject = moveTemplate.getElementalType();
               }

               if (elementalTypeObject == null) {
                  elementalTypeObject = moveTemplate.getElementalType();
               }

               String moveName;
               try {
                  moveName = moveTemplate.getDisplayName().getString();
               } catch (Throwable var26) {
                  moveName = moveTemplate.getName();
               }

               int currentPp = 100;
               int maxPp = 100;
               if (moveObject != null) {
                  try {
                     Method getPpMethod = moveObject.getClass().getMethod("getPp");
                     if (getPpMethod.invoke(moveObject) instanceof Number number) {
                        currentPp = number.intValue();
                     }
                  } catch (Throwable var25) {
                  }

                  try {
                     Method getMaxPpMethod = moveObject.getClass().getMethod("getMaxpp");
                     if (getMaxPpMethod.invoke(moveObject) instanceof Number number) {
                        maxPp = number.intValue();
                     }
                  } catch (Throwable var24) {
                  }
               }

               boolean selectable = true;

               try {
                  Method getSelectableMethod = tile.getClass().getMethod("getSelectable");
                  if (getSelectableMethod.invoke(tile) instanceof Boolean value) {
                     selectable = value;
                  }
               } catch (Throwable var23) {
               }

               Object damageCategory = moveTemplate.getDamageCategory();
               String categoryKey = "";

               try {
                  Method getNameMethod = damageCategory.getClass().getMethod("getName");
                  Object result = getNameMethod.invoke(damageCategory);
                  categoryKey = result != null ? result.toString().toLowerCase(Locale.ROOT) : "";
               } catch (Throwable var22) {
               }

               int typeColor = -1;
               int typeTextureMultiplier = -1;
               String moveTypeName = "";
               if (elementalTypeObject != null) {
                  try {
                     Method getNameMethod = elementalTypeObject.getClass().getMethod("getName");
                     Object result = getNameMethod.invoke(elementalTypeObject);
                     if (result != null) {
                        moveTypeName = result.toString();
                     }
                  } catch (Throwable var21) {
                  }

                  try {
                     Method getHueMethod = elementalTypeObject.getClass().getMethod("getHue");
                     if (getHueMethod.invoke(elementalTypeObject) instanceof Number number) {
                        typeColor = normalizeOpaqueColor(number.intValue());
                     }
                  } catch (Throwable var20) {
                  }

                  try {
                     Method getTextureXMethod = elementalTypeObject.getClass().getMethod("getTextureXMultiplier");
                     if (getTextureXMethod.invoke(elementalTypeObject) instanceof Number number) {
                        typeTextureMultiplier = number.intValue();
                     }
                  } catch (Throwable var19) {
                  }
               }

               return new MoveTileVisualData(
                  moveName, currentPp, maxPp, selectable, categoryKey, (int)Math.round(moveTemplate.getPower()), moveTypeName, typeColor, typeTextureMultiplier
               );
            } else {
               return null;
            }
         }
      } catch (Throwable var28) {
         return null;
      }
   }

   private static void renderCustomMoveTile(
      class_332 graphics,
      class_327 font,
      int x,
      int y,
      int width,
      int height,
      MoveTileVisualData data,
      boolean hovered,
      boolean selected,
      float opacity,
      List<OpponentMatchupEntry> targetEntries
   ) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException: Cannot read field "classStruct" because "classNode" is null
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifyNewEnumSwitch(SwitchHelper.java:319)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplify(SwitchHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:30)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:376)
      //
      // Bytecode:
      // 000: aload 6
      // 002: invokevirtual name/modid/client/CustomBattleController$MoveTileVisualData.typeColor ()I
      // 005: istore 11
      // 007: aload 6
      // 009: aload 10
      // 00b: invokestatic name/modid/client/CustomBattleController.resolveMoveEffectivenessTint (Lname/modid/client/CustomBattleController$MoveTileVisualData;Ljava/util/List;)Lname/modid/client/CustomBattleController$MoveEffectivenessTint;
      // 00e: astore 12
      // 010: aload 12
      // 012: invokevirtual name/modid/client/CustomBattleController$MoveEffectivenessTint.ordinal ()I
      // 015: lookupswitch 39 2 1 27 2 33
      // 030: ldc_w -10952082
      // 033: goto 03d
      // 036: ldc_w -5096886
      // 039: goto 03d
      // 03c: bipush 0
      // 03d: istore 13
      // 03f: iload 7
      // 041: ifeq 059
      // 044: ldc -10854549
      // 046: ldc_w -14341840
      // 049: ldc_w 0.34
      // 04c: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 04f: bipush -1
      // 050: ldc_w 0.06
      // 053: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 056: goto 064
      // 059: ldc -10854549
      // 05b: ldc_w -14341840
      // 05e: ldc_w 0.34
      // 061: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 064: iload 11
      // 066: iload 7
      // 068: ifeq 071
      // 06b: ldc_w 0.22
      // 06e: goto 074
      // 071: ldc_w 0.16
      // 074: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 077: sipush 236
      // 07a: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 07d: fload 9
      // 07f: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 082: istore 14
      // 084: iload 7
      // 086: ifeq 0a8
      // 089: ldc -13947080
      // 08b: ldc_w -15789545
      // 08e: ldc_w 0.18
      // 091: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 094: ldc -10854549
      // 096: ldc_w -13815495
      // 099: ldc_w 0.2
      // 09c: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 09f: ldc_w 0.08
      // 0a2: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 0a5: goto 0b3
      // 0a8: ldc -13947080
      // 0aa: ldc_w -15789545
      // 0ad: ldc_w 0.18
      // 0b0: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 0b3: iload 11
      // 0b5: iload 7
      // 0b7: ifeq 0c0
      // 0ba: ldc_w 0.48
      // 0bd: goto 0c3
      // 0c0: ldc_w 0.4
      // 0c3: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 0c6: sipush 242
      // 0c9: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 0cc: fload 9
      // 0ce: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 0d1: istore 15
      // 0d3: aload 12
      // 0d5: getstatic name/modid/client/CustomBattleController$MoveEffectivenessTint.NONE Lname/modid/client/CustomBattleController$MoveEffectivenessTint;
      // 0d8: if_acmpne 105
      // 0db: ldc -2630685
      // 0dd: iload 11
      // 0df: ldc_w 0.38
      // 0e2: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 0e5: bipush -1
      // 0e6: iload 8
      // 0e8: ifeq 0f1
      // 0eb: ldc_w 0.4
      // 0ee: goto 0ff
      // 0f1: iload 7
      // 0f3: ifeq 0fc
      // 0f6: ldc_w 0.26
      // 0f9: goto 0ff
      // 0fc: ldc_w 0.14
      // 0ff: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 102: goto 124
      // 105: iload 13
      // 107: bipush -1
      // 108: iload 8
      // 10a: ifeq 113
      // 10d: ldc_w 0.24
      // 110: goto 121
      // 113: iload 7
      // 115: ifeq 11e
      // 118: ldc_w 0.14
      // 11b: goto 121
      // 11e: ldc_w 0.08
      // 121: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 124: sipush 250
      // 127: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 12a: fload 9
      // 12c: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 12f: istore 16
      // 131: aload 12
      // 133: getstatic name/modid/client/CustomBattleController$MoveEffectivenessTint.NONE Lname/modid/client/CustomBattleController$MoveEffectivenessTint;
      // 136: if_acmpne 14f
      // 139: ldc -15328992
      // 13b: iload 11
      // 13d: ldc_w -15592164
      // 140: ldc_w 0.58
      // 143: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 146: ldc_w 0.54
      // 149: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 14c: goto 15a
      // 14f: iload 13
      // 151: ldc_w -15263198
      // 154: ldc_w 0.42
      // 157: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 15a: sipush 245
      // 15d: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 160: fload 9
      // 162: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 165: istore 17
      // 167: ldc 1795162111
      // 169: iload 11
      // 16b: ldc_w 0.1
      // 16e: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 171: iload 7
      // 173: ifeq 17b
      // 176: bipush 118
      // 178: goto 17d
      // 17b: bipush 92
      // 17d: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 180: fload 9
      // 182: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 185: istore 18
      // 187: ldc -500683724
      // 189: ldc_w -16118768
      // 18c: ldc_w 0.18
      // 18f: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 192: iload 11
      // 194: ldc_w 0.1
      // 197: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 19a: sipush 214
      // 19d: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 1a0: fload 9
      // 1a2: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 1a5: istore 19
      // 1a7: ldc -301199596
      // 1a9: ldc_w -16645371
      // 1ac: ldc_w 0.16
      // 1af: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 1b2: iload 11
      // 1b4: ldc_w 0.07
      // 1b7: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 1ba: sipush 226
      // 1bd: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 1c0: fload 9
      // 1c2: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 1c5: istore 20
      // 1c7: ldc -1799897399
      // 1c9: iload 11
      // 1cb: ldc_w 0.24
      // 1ce: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 1d1: sipush 208
      // 1d4: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 1d7: fload 9
      // 1d9: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 1dc: istore 21
      // 1de: ldc -1945564400
      // 1e0: iload 11
      // 1e2: ldc_w 0.16
      // 1e5: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 1e8: sipush 182
      // 1eb: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 1ee: fload 9
      // 1f0: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 1f3: istore 22
      // 1f5: iload 8
      // 1f7: ifeq 1fe
      // 1fa: bipush -1
      // 1fb: goto 200
      // 1fe: ldc -1249291
      // 200: fload 9
      // 202: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 205: istore 23
      // 207: ldc -805306368
      // 209: sipush 220
      // 20c: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 20f: fload 9
      // 211: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 214: istore 24
      // 216: ldc -3748648
      // 218: fload 9
      // 21a: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 21d: istore 25
      // 21f: ldc -3090720
      // 221: iload 11
      // 223: ldc_w 0.34
      // 226: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 229: sipush 244
      // 22c: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 22f: fload 9
      // 231: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 234: istore 26
      // 236: ldc -9670016
      // 238: ldc_w -13091513
      // 23b: ldc_w 0.18
      // 23e: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 241: iload 11
      // 243: ldc_w 0.64
      // 246: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 249: sipush 246
      // 24c: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 24f: fload 9
      // 251: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 254: istore 27
      // 256: ldc -14012873
      // 258: ldc_w -15657959
      // 25b: ldc_w 0.14
      // 25e: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 261: iload 11
      // 263: ldc_w 0.76
      // 266: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 269: sipush 248
      // 26c: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 26f: fload 9
      // 271: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 274: istore 28
      // 276: ldc 1962934271
      // 278: iload 11
      // 27a: ldc_w 0.08
      // 27d: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 280: bipush 120
      // 282: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 285: fload 9
      // 287: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 28a: istore 29
      // 28c: ldc -15328992
      // 28e: ldc -3090720
      // 290: iload 11
      // 292: ldc_w 0.24
      // 295: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 298: ldc_w 0.52
      // 29b: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 29e: sipush 240
      // 2a1: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 2a4: fload 9
      // 2a6: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 2a9: istore 30
      // 2ab: ldc -11051671
      // 2ad: ldc_w -14012616
      // 2b0: ldc_w 0.16
      // 2b3: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 2b6: iload 11
      // 2b8: ldc_w 0.48
      // 2bb: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 2be: sipush 230
      // 2c1: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 2c4: fload 9
      // 2c6: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 2c9: istore 31
      // 2cb: ldc -14670804
      // 2cd: ldc_w -15986924
      // 2d0: ldc_w 0.14
      // 2d3: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 2d6: iload 11
      // 2d8: ldc_w 0.62
      // 2db: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 2de: sipush 236
      // 2e1: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 2e4: fload 9
      // 2e6: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 2e9: istore 32
      // 2eb: ldc -1462314012
      // 2ed: iload 11
      // 2ef: ldc_w 0.2
      // 2f2: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 2f5: sipush 156
      // 2f8: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 2fb: fload 9
      // 2fd: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 300: istore 33
      // 302: ldc -1272962262
      // 304: iload 11
      // 306: ldc_w 0.18
      // 309: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 30c: sipush 180
      // 30f: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 312: fload 9
      // 314: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 317: istore 34
      // 319: iload 2
      // 31a: bipush 11
      // 31c: isub
      // 31d: istore 35
      // 31f: iload 4
      // 321: bipush 11
      // 323: iadd
      // 324: istore 36
      // 326: bipush 20
      // 328: istore 37
      // 32a: iload 35
      // 32c: bipush 2
      // 32d: iadd
      // 32e: istore 38
      // 330: iload 38
      // 332: iload 37
      // 334: iadd
      // 335: istore 39
      // 337: iload 35
      // 339: iload 37
      // 33b: iadd
      // 33c: bipush 5
      // 33d: iadd
      // 33e: istore 40
      // 340: aload 6
      // 342: invokevirtual name/modid/client/CustomBattleController$MoveTileVisualData.currentPp ()I
      // 345: aload 6
      // 347: invokevirtual name/modid/client/CustomBattleController$MoveTileVisualData.maxPp ()I
      // 34a: invokestatic name/modid/client/CustomBattleController.formatMovePpText (II)Ljava/lang/String;
      // 34d: astore 41
      // 34f: aload 1
      // 350: aload 41
      // 352: invokevirtual net/minecraft/class_327.method_1727 (Ljava/lang/String;)I
      // 355: istore 42
      // 357: bipush 16
      // 359: iload 35
      // 35b: iload 36
      // 35d: iadd
      // 35e: bipush 6
      // 360: isub
      // 361: iload 40
      // 363: isub
      // 364: invokestatic java/lang/Math.max (II)I
      // 367: istore 43
      // 369: aload 1
      // 36a: aload 6
      // 36c: invokevirtual name/modid/client/CustomBattleController$MoveTileVisualData.moveName ()Ljava/lang/String;
      // 36f: invokevirtual net/minecraft/class_327.method_1727 (Ljava/lang/String;)I
      // 372: iload 43
      // 374: if_icmple 37d
      // 377: ldc_w 0.9
      // 37a: goto 37e
      // 37d: fconst_1
      // 37e: fstore 44
      // 380: aload 1
      // 381: aload 6
      // 383: invokevirtual name/modid/client/CustomBattleController$MoveTileVisualData.moveName ()Ljava/lang/String;
      // 386: bipush 16
      // 388: iload 43
      // 38a: i2f
      // 38b: fload 44
      // 38d: fdiv
      // 38e: invokestatic java/lang/Math.round (F)I
      // 391: invokestatic java/lang/Math.max (II)I
      // 394: invokevirtual net/minecraft/class_327.method_27523 (Ljava/lang/String;I)Ljava/lang/String;
      // 397: astore 45
      // 399: aload 0
      // 39a: iload 35
      // 39c: iload 3
      // 39d: iload 36
      // 39f: iload 5
      // 3a1: iload 14
      // 3a3: iload 15
      // 3a5: iload 16
      // 3a7: iload 17
      // 3a9: invokestatic name/modid/client/CustomBattleController.drawRoundedGradientBox (Lnet/minecraft/class_332;IIIIIIII)V
      // 3ac: aload 0
      // 3ad: iload 35
      // 3af: bipush 3
      // 3b0: iadd
      // 3b1: iload 3
      // 3b2: bipush 2
      // 3b3: iadd
      // 3b4: iload 35
      // 3b6: iload 36
      // 3b8: iadd
      // 3b9: bipush 3
      // 3ba: isub
      // 3bb: iload 3
      // 3bc: bipush 4
      // 3bd: iadd
      // 3be: iload 18
      // 3c0: invokevirtual net/minecraft/class_332.method_25294 (IIIII)V
      // 3c3: aload 0
      // 3c4: iload 38
      // 3c6: iload 3
      // 3c7: bipush 2
      // 3c8: iadd
      // 3c9: iload 39
      // 3cb: iload 3
      // 3cc: iload 5
      // 3ce: iadd
      // 3cf: bipush 2
      // 3d0: isub
      // 3d1: iload 31
      // 3d3: iload 32
      // 3d5: invokevirtual net/minecraft/class_332.method_25296 (IIIIII)V
      // 3d8: aload 0
      // 3d9: iload 39
      // 3db: iload 3
      // 3dc: bipush 3
      // 3dd: iadd
      // 3de: iload 39
      // 3e0: bipush 1
      // 3e1: iadd
      // 3e2: iload 3
      // 3e3: iload 5
      // 3e5: iadd
      // 3e6: bipush 3
      // 3e7: isub
      // 3e8: iload 33
      // 3ea: invokevirtual net/minecraft/class_332.method_25294 (IIIII)V
      // 3ed: aload 0
      // 3ee: iload 39
      // 3f0: bipush 1
      // 3f1: iadd
      // 3f2: iload 3
      // 3f3: bipush 4
      // 3f4: iadd
      // 3f5: iload 39
      // 3f7: bipush 2
      // 3f8: iadd
      // 3f9: iload 3
      // 3fa: iload 5
      // 3fc: iadd
      // 3fd: bipush 4
      // 3fe: isub
      // 3ff: iload 34
      // 401: invokevirtual net/minecraft/class_332.method_25294 (IIIII)V
      // 404: aload 0
      // 405: iload 35
      // 407: bipush 2
      // 408: iadd
      // 409: iload 3
      // 40a: iload 5
      // 40c: iadd
      // 40d: bipush 10
      // 40f: isub
      // 410: iload 35
      // 412: iload 36
      // 414: iadd
      // 415: bipush 2
      // 416: isub
      // 417: iload 3
      // 418: iload 5
      // 41a: iadd
      // 41b: bipush 2
      // 41c: isub
      // 41d: iload 19
      // 41f: iload 20
      // 421: invokevirtual net/minecraft/class_332.method_25296 (IIIIII)V
      // 424: aload 0
      // 425: iload 35
      // 427: bipush 2
      // 428: iadd
      // 429: iload 3
      // 42a: iload 5
      // 42c: iadd
      // 42d: bipush 10
      // 42f: isub
      // 430: iload 35
      // 432: iload 36
      // 434: iadd
      // 435: bipush 2
      // 436: isub
      // 437: iload 3
      // 438: iload 5
      // 43a: iadd
      // 43b: bipush 9
      // 43d: isub
      // 43e: iload 21
      // 440: invokevirtual net/minecraft/class_332.method_25294 (IIIII)V
      // 443: aload 0
      // 444: iload 35
      // 446: bipush 2
      // 447: iadd
      // 448: iload 3
      // 449: iload 5
      // 44b: iadd
      // 44c: bipush 2
      // 44d: isub
      // 44e: iload 35
      // 450: iload 36
      // 452: iadd
      // 453: bipush 2
      // 454: isub
      // 455: iload 3
      // 456: iload 5
      // 458: iadd
      // 459: bipush 1
      // 45a: isub
      // 45b: iload 22
      // 45d: invokevirtual net/minecraft/class_332.method_25294 (IIIII)V
      // 460: iload 35
      // 462: bipush 3
      // 463: iadd
      // 464: istore 46
      // 466: iload 3
      // 467: bipush 4
      // 468: iadd
      // 469: istore 47
      // 46b: aload 0
      // 46c: iload 46
      // 46e: iload 47
      // 470: bipush 16
      // 472: bipush 16
      // 474: iload 27
      // 476: iload 28
      // 478: iload 26
      // 47a: iload 30
      // 47c: invokestatic name/modid/client/CustomBattleController.drawRoundedGradientBox (Lnet/minecraft/class_332;IIIIIIII)V
      // 47f: aload 0
      // 480: iload 46
      // 482: bipush 2
      // 483: iadd
      // 484: iload 47
      // 486: bipush 1
      // 487: iadd
      // 488: iload 46
      // 48a: bipush 16
      // 48c: iadd
      // 48d: bipush 2
      // 48e: isub
      // 48f: iload 47
      // 491: bipush 3
      // 492: iadd
      // 493: iload 29
      // 495: invokevirtual net/minecraft/class_332.method_25294 (IIIII)V
      // 498: aload 0
      // 499: aload 6
      // 49b: iload 46
      // 49d: bipush 2
      // 49e: iadd
      // 49f: iload 47
      // 4a1: bipush 2
      // 4a2: iadd
      // 4a3: fload 9
      // 4a5: invokestatic name/modid/client/CustomBattleController.renderMoveTypeBadgeIcon (Lnet/minecraft/class_332;Lname/modid/client/CustomBattleController$MoveTileVisualData;IIF)V
      // 4a8: aload 0
      // 4a9: aload 1
      // 4aa: aload 45
      // 4ac: iload 40
      // 4ae: iload 3
      // 4af: bipush 4
      // 4b0: iadd
      // 4b1: iload 23
      // 4b3: iload 24
      // 4b5: fload 44
      // 4b7: invokestatic name/modid/client/CustomBattleController.drawScaledShadowedString (Lnet/minecraft/class_332;Lnet/minecraft/class_327;Ljava/lang/String;IIIIF)V
      // 4ba: aload 6
      // 4bc: invokevirtual name/modid/client/CustomBattleController$MoveTileVisualData.categoryKey ()Ljava/lang/String;
      // 4bf: invokestatic name/modid/client/CustomBattleController.resolveMoveCategoryChipStyle (Ljava/lang/String;)Lname/modid/client/CustomBattleController$CategoryChipStyle;
      // 4c2: astore 48
      // 4c4: aload 0
      // 4c5: aload 1
      // 4c6: iload 40
      // 4c8: iload 3
      // 4c9: iload 5
      // 4cb: iadd
      // 4cc: bipush 10
      // 4ce: isub
      // 4cf: aload 48
      // 4d1: invokevirtual name/modid/client/CustomBattleController$CategoryChipStyle.width ()I
      // 4d4: aload 48
      // 4d6: invokevirtual name/modid/client/CustomBattleController$CategoryChipStyle.label ()Ljava/lang/String;
      // 4d9: aload 48
      // 4db: invokevirtual name/modid/client/CustomBattleController$CategoryChipStyle.bgTop ()I
      // 4de: fload 9
      // 4e0: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 4e3: aload 48
      // 4e5: invokevirtual name/modid/client/CustomBattleController$CategoryChipStyle.bgBottom ()I
      // 4e8: fload 9
      // 4ea: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 4ed: aload 48
      // 4ef: invokevirtual name/modid/client/CustomBattleController$CategoryChipStyle.border ()I
      // 4f2: fload 9
      // 4f4: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 4f7: aload 48
      // 4f9: invokevirtual name/modid/client/CustomBattleController$CategoryChipStyle.text ()I
      // 4fc: fload 9
      // 4fe: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 501: iload 24
      // 503: ldc_w 0.84
      // 506: invokestatic name/modid/client/CustomBattleController.renderMoveInfoChip (Lnet/minecraft/class_332;Lnet/minecraft/class_327;IIILjava/lang/String;IIIIIF)V
      // 509: aload 6
      // 50b: invokevirtual name/modid/client/CustomBattleController$MoveTileVisualData.power ()I
      // 50e: ifle 63e
      // 511: aload 12
      // 513: invokevirtual name/modid/client/CustomBattleController$MoveEffectivenessTint.ordinal ()I
      // 516: lookupswitch 38 2 1 26 2 32
      // 530: ldc_w -10952082
      // 533: goto 53e
      // 536: ldc_w -2073508
      // 539: goto 53e
      // 53c: iload 11
      // 53e: istore 49
      // 540: aload 12
      // 542: getstatic name/modid/client/CustomBattleController$MoveEffectivenessTint.NONE Lname/modid/client/CustomBattleController$MoveEffectivenessTint;
      // 545: if_acmpne 560
      // 548: ldc -598451867
      // 54a: iload 11
      // 54c: ldc_w 0.24
      // 54f: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 552: sipush 220
      // 555: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 558: fload 9
      // 55a: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 55d: goto 575
      // 560: ldc -598451867
      // 562: iload 49
      // 564: ldc_w 0.38
      // 567: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 56a: sipush 226
      // 56d: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 570: fload 9
      // 572: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 575: istore 50
      // 577: aload 12
      // 579: getstatic name/modid/client/CustomBattleController$MoveEffectivenessTint.NONE Lname/modid/client/CustomBattleController$MoveEffectivenessTint;
      // 57c: if_acmpne 597
      // 57f: ldc -433838032
      // 581: iload 11
      // 583: ldc_w 0.16
      // 586: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 589: sipush 228
      // 58c: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 58f: fload 9
      // 591: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 594: goto 5ac
      // 597: ldc -433838032
      // 599: iload 49
      // 59b: ldc_w 0.18
      // 59e: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 5a1: sipush 234
      // 5a4: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 5a7: fload 9
      // 5a9: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 5ac: istore 51
      // 5ae: aload 12
      // 5b0: getstatic name/modid/client/CustomBattleController$MoveEffectivenessTint.NONE Lname/modid/client/CustomBattleController$MoveEffectivenessTint;
      // 5b3: if_acmpne 5ce
      // 5b6: ldc -943996210
      // 5b8: iload 11
      // 5ba: ldc_w 0.28
      // 5bd: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 5c0: sipush 180
      // 5c3: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 5c6: fload 9
      // 5c8: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 5cb: goto 5e3
      // 5ce: ldc -943996210
      // 5d0: iload 49
      // 5d2: ldc_w 0.68
      // 5d5: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 5d8: sipush 214
      // 5db: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 5de: fload 9
      // 5e0: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 5e3: istore 52
      // 5e5: aload 12
      // 5e7: getstatic name/modid/client/CustomBattleController$MoveEffectivenessTint.NONE Lname/modid/client/CustomBattleController$MoveEffectivenessTint;
      // 5ea: if_acmpne 5f2
      // 5ed: iload 25
      // 5ef: goto 600
      // 5f2: bipush -1
      // 5f3: iload 49
      // 5f5: ldc_w 0.3
      // 5f8: invokestatic name/modid/client/CustomBattleController.mixColors (IIF)I
      // 5fb: fload 9
      // 5fd: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 600: istore 53
      // 602: aload 0
      // 603: aload 1
      // 604: iload 40
      // 606: aload 48
      // 608: invokevirtual name/modid/client/CustomBattleController$CategoryChipStyle.width ()I
      // 60b: iadd
      // 60c: bipush 4
      // 60d: iadd
      // 60e: iload 3
      // 60f: iload 5
      // 611: iadd
      // 612: bipush 10
      // 614: isub
      // 615: aload 6
      // 617: invokevirtual name/modid/client/CustomBattleController$MoveTileVisualData.power ()I
      // 61a: bipush 100
      // 61c: if_icmplt 624
      // 61f: bipush 16
      // 621: goto 626
      // 624: bipush 12
      // 626: aload 6
      // 628: invokevirtual name/modid/client/CustomBattleController$MoveTileVisualData.power ()I
      // 62b: invokestatic java/lang/Integer.toString (I)Ljava/lang/String;
      // 62e: iload 50
      // 630: iload 51
      // 632: iload 52
      // 634: iload 53
      // 636: iload 24
      // 638: ldc_w 0.84
      // 63b: invokestatic name/modid/client/CustomBattleController.renderMoveInfoChip (Lnet/minecraft/class_332;Lnet/minecraft/class_327;IIILjava/lang/String;IIIIIF)V
      // 63e: aload 6
      // 640: invokevirtual name/modid/client/CustomBattleController$MoveTileVisualData.currentPp ()I
      // 643: aload 6
      // 645: invokevirtual name/modid/client/CustomBattleController$MoveTileVisualData.maxPp ()I
      // 648: invokestatic name/modid/client/CustomBattleController.resolveMovePpColor (II)I
      // 64b: fload 9
      // 64d: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 650: istore 49
      // 652: aload 1
      // 653: aload 41
      // 655: invokevirtual net/minecraft/class_327.method_1727 (Ljava/lang/String;)I
      // 658: istore 42
      // 65a: aload 0
      // 65b: aload 1
      // 65c: aload 41
      // 65e: iload 35
      // 660: iload 36
      // 662: iadd
      // 663: bipush 7
      // 665: isub
      // 666: iload 42
      // 668: isub
      // 669: bipush 1
      // 66a: iadd
      // 66b: iload 3
      // 66c: iload 5
      // 66e: iadd
      // 66f: bipush 8
      // 671: isub
      // 672: iload 24
      // 674: bipush 0
      // 675: invokevirtual net/minecraft/class_332.method_51433 (Lnet/minecraft/class_327;Ljava/lang/String;IIIZ)I
      // 678: pop
      // 679: aload 0
      // 67a: aload 1
      // 67b: aload 41
      // 67d: iload 35
      // 67f: iload 36
      // 681: iadd
      // 682: bipush 7
      // 684: isub
      // 685: iload 42
      // 687: isub
      // 688: iload 3
      // 689: iload 5
      // 68b: iadd
      // 68c: bipush 9
      // 68e: isub
      // 68f: iload 49
      // 691: bipush 0
      // 692: invokevirtual net/minecraft/class_332.method_51433 (Lnet/minecraft/class_327;Ljava/lang/String;IIIZ)I
      // 695: pop
      // 696: aload 6
      // 698: invokevirtual name/modid/client/CustomBattleController$MoveTileVisualData.selectable ()Z
      // 69b: ifne 6ef
      // 69e: ldc_w -16316404
      // 6a1: sipush 138
      // 6a4: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 6a7: fload 9
      // 6a9: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 6ac: istore 50
      // 6ae: ldc -805306368
      // 6b0: sipush 170
      // 6b3: invokestatic name/modid/client/CustomBattleController.withAlpha (II)I
      // 6b6: fload 9
      // 6b8: invokestatic name/modid/client/BattleOpacityHelper.applyOpacity (IF)I
      // 6bb: istore 51
      // 6bd: aload 0
      // 6be: iload 35
      // 6c0: bipush 1
      // 6c1: iadd
      // 6c2: iload 3
      // 6c3: bipush 1
      // 6c4: iadd
      // 6c5: iload 35
      // 6c7: iload 36
      // 6c9: iadd
      // 6ca: bipush 1
      // 6cb: isub
      // 6cc: iload 3
      // 6cd: iload 5
      // 6cf: iadd
      // 6d0: bipush 1
      // 6d1: isub
      // 6d2: iload 50
      // 6d4: invokevirtual net/minecraft/class_332.method_25294 (IIIII)V
      // 6d7: aload 0
      // 6d8: iload 40
      // 6da: iload 3
      // 6db: bipush 12
      // 6dd: iadd
      // 6de: iload 35
      // 6e0: iload 36
      // 6e2: iadd
      // 6e3: bipush 8
      // 6e5: isub
      // 6e6: iload 3
      // 6e7: bipush 13
      // 6e9: iadd
      // 6ea: iload 51
      // 6ec: invokevirtual net/minecraft/class_332.method_25294 (IIIII)V
      // 6ef: return
   }

   private static void renderMoveTypeBadgeIcon(class_332 graphics, MoveTileVisualData data, int x, int y, float opacity) {
      if (data.typeTextureMultiplier() >= 0) {
         int textureX = data.typeTextureMultiplier() * 36;
         graphics.method_25293(COBBLEMON_TYPES_SHEET, x, y, 12, 12, (float)textureX, 0.0F, 36, 36, 648, 36);
      }
   }

   private static void drawScaledShadowedString(class_332 graphics, class_327 font, String text, int x, int y, int textColor, int shadowColor, float scale) {
      if (scale == 1.0F) {
         graphics.method_51433(font, text, x + 1, y + 1, shadowColor, false);
         graphics.method_51433(font, text, x, y, textColor, false);
      } else {
         graphics.method_51448().method_22903();
         graphics.method_51448().method_46416((float)x, (float)y, 0.0F);
         graphics.method_51448().method_22905(scale, scale, 1.0F);
         graphics.method_51433(font, text, 1, 1, shadowColor, false);
         graphics.method_51433(font, text, 0, 0, textColor, false);
         graphics.method_51448().method_22909();
      }
   }

   private static MoveEffectivenessTint resolveMoveEffectivenessTint(MoveTileVisualData data, List<OpponentMatchupEntry> targetEntries) {
      if (data == null || isStatusMoveCategory(data.categoryKey())) {
         return MoveEffectivenessTint.NONE;
      } else if (data.moveTypeName() != null && !data.moveTypeName().isBlank()) {
         if (targetEntries != null && !targetEntries.isEmpty()) {
            MoveEffectivenessTint bestTint = MoveEffectivenessTint.NOT_VERY_EFFECTIVE;
            int bestRank = moveEffectivenessRank(bestTint);
            String moveType = data.moveTypeName().toLowerCase(Locale.ROOT);

            for (OpponentMatchupEntry targetEntry : targetEntries) {
               if (targetEntry != null && targetEntry.types != null && !targetEntry.types.isEmpty()) {
                  String defenderType1 = ((ElementalType)targetEntry.types.get(0)).getName().toLowerCase(Locale.ROOT);
                  String defenderType2 = targetEntry.types.size() > 1 ? ((ElementalType)targetEntry.types.get(1)).getName().toLowerCase(Locale.ROOT) : null;
                  float effectiveness = TypeChart.getEffectivenessAgainstTypes(moveType, defenderType1, defenderType2);
                  MoveEffectivenessTint tint = effectiveness >= 2.0F
                     ? MoveEffectivenessTint.SUPER_EFFECTIVE
                     : (effectiveness < 1.0F ? MoveEffectivenessTint.NOT_VERY_EFFECTIVE : MoveEffectivenessTint.NONE);
                  int tintRank = moveEffectivenessRank(tint);
                  if (tintRank > bestRank) {
                     bestTint = tint;
                     bestRank = tintRank;
                  }
               }
            }

            return bestTint;
         } else {
            return MoveEffectivenessTint.NONE;
         }
      } else {
         return MoveEffectivenessTint.NONE;
      }
   }

   private static int moveEffectivenessRank(MoveEffectivenessTint tint) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException: Cannot read field "classStruct" because "classNode" is null
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifyNewEnumSwitch(SwitchHelper.java:319)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplify(SwitchHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:30)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:376)
      //
      // Bytecode:
      // 00: aload 0
      // 01: invokevirtual name/modid/client/CustomBattleController$MoveEffectivenessTint.ordinal ()I
      // 04: tableswitch 28 0 2 42 38 46
      // 20: new java/lang/MatchException
      // 23: dup
      // 24: aconst_null
      // 25: aconst_null
      // 26: invokespecial java/lang/MatchException.<init> (Ljava/lang/String;Ljava/lang/Throwable;)V
      // 29: athrow
      // 2a: bipush 2
      // 2b: goto 33
      // 2e: bipush 1
      // 2f: goto 33
      // 32: bipush 0
      // 33: ireturn
   }

   private static boolean isStatusMoveCategory(String categoryKey) {
      return !"physical".equals(categoryKey) && !"special".equals(categoryKey);
   }

   private static CategoryChipStyle resolveMoveCategoryChipStyle(String categoryKey) {
      if ("physical".equals(categoryKey)) {
         return new CategoryChipStyle("PHY", 16, -4892107, -8705251, -2519974, -1);
      } else {
         return "special".equals(categoryKey)
            ? new CategoryChipStyle("SPC", 16, -12945449, -14729334, -9001217, -1)
            : new CategoryChipStyle("STA", 16, -8683114, -11907231, -3814695, -1);
      }
   }

   private static void renderMoveInfoChip(
      class_332 graphics,
      class_327 font,
      int x,
      int y,
      int width,
      String text,
      int topColor,
      int bottomColor,
      int borderColor,
      int textColor,
      int shadowColor,
      float textScale
   ) {
      fillChipBox(graphics, x, y, width, 7, topColor, bottomColor, borderColor);
      int scaledTextWidth = Math.max(1, Math.round((float)font.method_1727(text) * textScale));
      int textX = x + Math.max(1, (width - scaledTextWidth) / 2);
      drawScaledShadowedString(graphics, font, text, textX, y + 1, textColor, shadowColor, textScale);
   }

   private static void fillChipBox(class_332 graphics, int x, int y, int width, int height, int topColor, int bottomColor, int borderColor) {
      graphics.method_25294(x, y + 1, x + width, y + height - 1, borderColor);
      graphics.method_25294(x + 1, y, x + width - 1, y + height, borderColor);
      graphics.method_25296(x + 1, y + 1, x + width - 1, y + height - 1, topColor, bottomColor);
   }

   private static String formatMovePpText(int currentPp, int maxPp) {
      return currentPp == 100 && maxPp == 100 ? "--/--" : currentPp + "/" + maxPp;
   }

   private static int resolveMovePpColor(int currentPp, int maxPp) {
      if (currentPp == 100 && maxPp == 100) {
         return -3748648;
      } else if (currentPp <= 0) {
         return -35467;
      } else if (maxPp > 0 && currentPp <= Math.max(1, maxPp / 2)) {
         return currentPp <= Math.max(1, maxPp / 4) ? -671909 : -8058;
      } else {
         return -1249291;
      }
   }

   private static int normalizeOpaqueColor(int color) {
      return (color & 0xFF000000) == 0 ? 0xFF000000 | color & 16777215 : color;
   }

   private static void clearTileCaches() {
      cachedTiles = null;
      cachedScreen = null;
      cachedActionSelection = null;
      cachedTilesExpiresAt = 0L;
      tileSnapshotCache.clear();
   }

   private static void rebuildTileSnapshotCache(Object[] tiles) {
      tileSnapshotCache.clear();
      if (tiles != null && tiles.length != 0) {
         for (Object tile : tiles) {
            if (tile != null) {
               tileSnapshotCache.put(tile, buildTileSnapshot(tile));
            }
         }
      }
   }

   private static TileSnapshot getTileSnapshot(Object tile) {
      if (tile == null) {
         return null;
      } else {
         TileSnapshot cached = tileSnapshotCache.get(tile);
         if (cached != null) {
            return cached;
         } else {
            TileSnapshot snapshot = buildTileSnapshot(tile);
            tileSnapshotCache.put(tile, snapshot);
            return snapshot;
         }
      }
   }

   private static TileSnapshot buildTileSnapshot(Object tile) {
      String tileClassName = tile.getClass().getName();
      MoveTileVisualData moveData = !tileClassName.contains("MoveTile") && !tileClassName.contains("GimmickTile") && !tileClassName.contains("DynamaxTile")
         ? null
         : resolveMoveTileVisualDataUncached(tile);
      return new TileSnapshot(
         getTileXUncached(tile),
         getTileYUncached(tile),
         getTileWidthUncached(tile),
         getTileHeightUncached(tile),
         getBattleOptionLabelUncached(tile),
         getBattleOptionKeyUncached(tile),
         moveData
      );
   }

   private static String getBattleOptionLabel(Object tile) {
      TileSnapshot snapshot = getTileSnapshot(tile);
      return snapshot != null ? snapshot.label() : getBattleOptionLabelUncached(tile);
   }

   private static String getBattleOptionLabelUncached(Object tile) {
      if (tile == null) {
         return "";
      } else {
         try {
            Field textField = tile.getClass().getDeclaredField("text");
            textField.setAccessible(true);
            Object textObject = textField.get(tile);
            if (textObject instanceof class_2561 component) {
               return component.getString();
            } else {
               return textObject != null ? textObject.toString() : "";
            }
         } catch (Throwable var4) {
            return "";
         }
      }
   }

   private static String getBattleOptionKey(Object tile) {
      TileSnapshot snapshot = getTileSnapshot(tile);
      return snapshot != null ? snapshot.key() : getBattleOptionKeyUncached(tile);
   }

   private static String getBattleOptionKeyUncached(Object tile) {
      if (tile == null) {
         return "";
      } else {
         try {
            Field textField = tile.getClass().getDeclaredField("text");
            textField.setAccessible(true);
            if (textField.get(tile) instanceof class_2561 component && component.method_10851() instanceof class_2588 contents) {
               return contents.method_11022().toLowerCase(Locale.ROOT);
            }
         } catch (Throwable var6) {
         }

         return "";
      }
   }

   private static boolean isTileHovered(Object tile, int mouseX, int mouseY) {
      if (tile == null) {
         return false;
      } else {
         int tileX = getTileX(tile);
         int tileY = getTileY(tile);
         int tileWidth = getTileWidth(tile);
         int tileHeight = getTileHeight(tile);
         if (tileX > 0 && tileY > 0 && tileWidth > 0 && tileHeight > 0) {
            String tileClassName = tile.getClass().getName();
            if (tileClassName.contains("MoveTile") || tileClassName.contains("GimmickTile") || tileClassName.contains("DynamaxTile")) {
               tileX -= 10;
               tileWidth += 10;
            }

            return mouseX >= tileX && mouseX <= tileX + tileWidth && mouseY >= tileY && mouseY <= tileY + tileHeight;
         } else {
            return false;
         }
      }
   }

   private static GeneralActionButtonStyle resolveGeneralActionButtonStyle(Object tile, String label) {
      String key = getBattleOptionKey(tile);
      String lowerLabel = label == null ? "" : label.toLowerCase(Locale.ROOT);
      if (key.contains(".fight") || lowerLabel.contains("fight")) {
         return new GeneralActionButtonStyle("fight", -2006926, -7393724);
      } else if (key.contains(".switch") || lowerLabel.contains("switch")) {
         return new GeneralActionButtonStyle("switch", -9644163, -13731004);
      } else if (key.contains(".capture")
         || key.contains(".bag")
         || lowerLabel.contains("catch")
         || lowerLabel.contains("capture")
         || lowerLabel.contains("bag")) {
         return new GeneralActionButtonStyle("catch", -998070, -6654940);
      } else {
         return !key.contains(".forfeit") && !lowerLabel.contains("forfeit")
            ? new GeneralActionButtonStyle("run", -9262593, -13346145)
            : new GeneralActionButtonStyle("forfeit", -9262593, -13346145);
      }
   }

   private static void renderCustomGeneralActionButton(
      class_332 graphics,
      class_327 font,
      int x,
      int y,
      int width,
      int height,
      String label,
      GeneralActionButtonStyle style,
      boolean hovered,
      boolean selected,
      Theme theme
   ) {
      int baseTop = withAlpha(mixColors(theme.headerBgLight(), -855310, hovered ? 0.36F : 0.26F), 238);
      int baseBottom = withAlpha(mixColors(theme.headerBg(), -10591893, hovered ? 0.22F : 0.16F), 236);
      int borderTop = withAlpha(mixColors(theme.borderHighlight(), -1, hovered ? 0.4F : 0.24F), 250);
      int borderBottom = withAlpha(mixColors(theme.borderColor(), -15197664, 0.42F), 245);
      int innerSheen = withAlpha(mixColors(-1, style.accent(), 0.12F), hovered ? 112 : 88);
      int textColor = selected ? -1 : mixColors(theme.headerText(), -1, 0.18F);
      int textShadow = withAlpha(theme.headerTextShadow(), 220);
      int accent = selected ? mixColors(style.accent(), -1, 0.18F) : style.accent();
      int accentDark = style.accentDark();
      int accentGlow = withAlpha(accent, hovered ? 118 : 90);
      int iconZoneWidth = 26;
      int accentWidth = 3;
      int iconSeparatorX = x + width - iconZoneWidth;
      int accentX = iconSeparatorX - accentWidth - 1;
      drawRoundedGradientBox(graphics, x, y, width, height, baseTop, baseBottom, borderTop, borderBottom);
      graphics.method_25294(x + 3, y + 2, x + width - 3, y + 4, innerSheen);
      graphics.method_25294(accentX, y + 2, accentX + accentWidth, y + height - 2, accentGlow);
      graphics.method_25294(accentX + 1, y + 3, accentX + accentWidth, y + height - 3, accentDark);
      graphics.method_25294(iconSeparatorX - 1, y + 4, iconSeparatorX, y + height - 4, withAlpha(theme.panelBgBottom(), 120));
      graphics.method_25294(iconSeparatorX, y + 4, iconSeparatorX + 1, y + height - 4, withAlpha(theme.borderHighlight(), 82));
      String trimmedLabel = font.method_27523(label, width - iconZoneWidth - 12);
      graphics.method_51433(font, trimmedLabel, x + 7, y + 9, textShadow, false);
      graphics.method_51433(font, trimmedLabel, x + 6, y + 8, textColor, false);
   }

   private static void renderGeneralActionIcon(class_332 graphics, String id, int centerX, int centerY, int accentColor, int detailColor, int shadowColor) {
      switch (id) {
         case "fight":
            renderFightIcon(graphics, centerX, centerY, accentColor, detailColor, shadowColor);
            break;
         case "switch":
            renderSwitchIcon(graphics, centerX, centerY, accentColor, detailColor, shadowColor);
            break;
         case "catch":
            renderCatchIcon(graphics, centerX, centerY, accentColor, detailColor, shadowColor);
            break;
         case "forfeit":
            renderForfeitIcon(graphics, centerX, centerY, accentColor, detailColor, shadowColor);
            break;
         default:
            renderRunIcon(graphics, centerX, centerY, accentColor, detailColor, shadowColor);
      }
   }

   private static void renderFightIcon(class_332 graphics, int centerX, int centerY, int accentColor, int detailColor, int shadowColor) {
      graphics.method_25294(centerX - 6, centerY - 6, centerX - 2, centerY - 5, shadowColor);
      graphics.method_25294(centerX + 2, centerY - 6, centerX + 6, centerY - 5, shadowColor);
      graphics.method_25294(centerX - 5, centerY - 5, centerX + 4, centerY - 4, accentColor);
      graphics.method_25294(centerX - 4, centerY - 4, centerX + 5, centerY - 3, accentColor);
      graphics.method_25294(centerX - 1, centerY - 2, centerX + 1, centerY + 5, detailColor);
      graphics.method_25294(centerX - 5, centerY + 3, centerX + 2, centerY + 5, accentColor);
      graphics.method_25294(centerX - 3, centerY + 5, centerX + 4, centerY + 7, accentColor);
      graphics.method_25294(centerX - 6, centerY - 1, centerX - 4, centerY + 1, detailColor);
      graphics.method_25294(centerX + 4, centerY - 1, centerX + 6, centerY + 1, detailColor);
   }

   private static void renderSwitchIcon(class_332 graphics, int centerX, int centerY, int accentColor, int detailColor, int shadowColor) {
      drawIconHLine(graphics, centerX - 4, centerY - 3, 5, shadowColor);
      drawIconPixel(graphics, centerX + 1, centerY - 4, shadowColor);
      drawIconPixel(graphics, centerX + 2, centerY - 5, shadowColor);
      drawIconPixel(graphics, centerX + 3, centerY - 4, shadowColor);
      drawIconHLine(graphics, centerX - 5, centerY + 2, 5, shadowColor);
      drawIconPixel(graphics, centerX - 2, centerY + 1, shadowColor);
      drawIconPixel(graphics, centerX - 3, centerY, shadowColor);
      drawIconPixel(graphics, centerX - 4, centerY + 1, shadowColor);
      drawIconHLine(graphics, centerX - 5, centerY - 4, 6, accentColor);
      drawIconPixel(graphics, centerX + 1, centerY - 5, detailColor);
      drawIconPixel(graphics, centerX + 2, centerY - 6, detailColor);
      drawIconPixel(graphics, centerX + 3, centerY - 5, detailColor);
      drawIconHLine(graphics, centerX, centerY - 4, 2, detailColor);
      drawIconHLine(graphics, centerX - 1, centerY + 1, 6, accentColor);
      drawIconPixel(graphics, centerX - 3, centerY + 1, detailColor);
      drawIconPixel(graphics, centerX - 4, centerY, detailColor);
      drawIconPixel(graphics, centerX - 5, centerY + 1, detailColor);
      drawIconHLine(graphics, centerX - 1, centerY + 1, 2, detailColor);
   }

   private static void renderCatchIcon(class_332 graphics, int centerX, int centerY, int accentColor, int detailColor, int shadowColor) {
      int outline = mixColors(shadowColor, -16777216, 0.45F);
      int lowerFill = mixColors(accentColor, -15065820, 0.34F);
      drawIconHLine(graphics, centerX - 3, centerY - 5, 7, outline);
      drawIconHLine(graphics, centerX - 5, centerY - 4, 11, outline);
      drawIconHLine(graphics, centerX - 6, centerY - 3, 13, outline);
      drawIconHLine(graphics, centerX - 6, centerY - 2, 13, outline);
      drawIconHLine(graphics, centerX - 6, centerY - 1, 13, outline);
      drawIconHLine(graphics, centerX - 6, centerY, 13, outline);
      drawIconHLine(graphics, centerX - 6, centerY + 1, 13, outline);
      drawIconHLine(graphics, centerX - 5, centerY + 2, 11, outline);
      drawIconHLine(graphics, centerX - 3, centerY + 3, 7, outline);
      drawIconHLine(graphics, centerX - 3, centerY - 4, 7, detailColor);
      drawIconHLine(graphics, centerX - 5, centerY - 3, 11, detailColor);
      drawIconHLine(graphics, centerX - 5, centerY - 2, 11, detailColor);
      drawIconHLine(graphics, centerX - 5, centerY - 1, 11, detailColor);
      drawIconHLine(graphics, centerX - 5, centerY, 11, accentColor);
      drawIconHLine(graphics, centerX - 5, centerY + 1, 11, lowerFill);
      drawIconHLine(graphics, centerX - 4, centerY + 2, 9, lowerFill);
      drawIconHLine(graphics, centerX - 5, centerY - 1, 11, mixColors(detailColor, -1, 0.18F));
      drawIconHLine(graphics, centerX - 5, centerY, 11, outline);
      drawIconPixel(graphics, centerX - 1, centerY - 1, outline);
      drawIconPixel(graphics, centerX, centerY - 1, outline);
      drawIconPixel(graphics, centerX + 1, centerY - 1, outline);
      drawIconPixel(graphics, centerX - 1, centerY + 1, outline);
      drawIconPixel(graphics, centerX, centerY + 1, outline);
      drawIconPixel(graphics, centerX + 1, centerY + 1, outline);
      drawIconPixel(graphics, centerX - 1, centerY, accentColor);
      drawIconPixel(graphics, centerX, centerY, accentColor);
      drawIconPixel(graphics, centerX + 1, centerY, accentColor);
      drawIconPixel(graphics, centerX, centerY, -1);
   }

   private static void renderRunIcon(class_332 graphics, int centerX, int centerY, int accentColor, int detailColor, int shadowColor) {
      drawIconHLine(graphics, centerX - 6, centerY - 2, 3, shadowColor);
      drawIconHLine(graphics, centerX - 7, centerY, 4, shadowColor);
      drawIconPixel(graphics, centerX, centerY - 5, shadowColor);
      drawIconPixel(graphics, centerX + 1, centerY - 4, shadowColor);
      drawIconHLine(graphics, centerX - 1, centerY - 3, 4, shadowColor);
      drawIconPixel(graphics, centerX - 2, centerY - 2, shadowColor);
      drawIconPixel(graphics, centerX + 3, centerY - 2, shadowColor);
      drawIconHLine(graphics, centerX - 1, centerY - 1, 4, shadowColor);
      drawIconHLine(graphics, centerX + 1, centerY, 4, shadowColor);
      drawIconPixel(graphics, centerX - 1, centerY + 1, shadowColor);
      drawIconPixel(graphics, centerX, centerY + 2, shadowColor);
      drawIconPixel(graphics, centerX + 1, centerY + 3, shadowColor);
      drawIconHLine(graphics, centerX + 2, centerY + 4, 4, shadowColor);
      drawIconHLine(graphics, centerX - 6, centerY - 3, 3, accentColor);
      drawIconHLine(graphics, centerX - 7, centerY - 1, 4, accentColor);
      drawIconPixel(graphics, centerX, centerY - 6, detailColor);
      drawIconPixel(graphics, centerX + 1, centerY - 5, detailColor);
      drawIconHLine(graphics, centerX - 1, centerY - 4, 4, detailColor);
      drawIconPixel(graphics, centerX - 2, centerY - 3, detailColor);
      drawIconPixel(graphics, centerX + 3, centerY - 3, detailColor);
      drawIconHLine(graphics, centerX - 1, centerY - 2, 4, accentColor);
      drawIconHLine(graphics, centerX + 1, centerY - 1, 4, accentColor);
      drawIconPixel(graphics, centerX - 1, centerY, detailColor);
      drawIconPixel(graphics, centerX, centerY + 1, detailColor);
      drawIconPixel(graphics, centerX + 1, centerY + 2, detailColor);
      drawIconHLine(graphics, centerX + 2, centerY + 3, 4, detailColor);
   }

   private static void renderForfeitIcon(class_332 graphics, int centerX, int centerY, int accentColor, int detailColor, int shadowColor) {
      drawIconVLine(graphics, centerX - 5, centerY - 6, 12, shadowColor);
      drawIconHLine(graphics, centerX - 4, centerY - 5, 8, shadowColor);
      drawIconHLine(graphics, centerX - 3, centerY - 4, 8, shadowColor);
      drawIconHLine(graphics, centerX - 2, centerY - 3, 7, shadowColor);
      drawIconHLine(graphics, centerX - 4, centerY + 2, 6, shadowColor);
      drawIconVLine(graphics, centerX - 6, centerY - 6, 12, detailColor);
      drawIconHLine(graphics, centerX - 5, centerY - 6, 8, accentColor);
      drawIconHLine(graphics, centerX - 4, centerY - 5, 8, mixColors(accentColor, -1, 0.16F));
      drawIconHLine(graphics, centerX - 3, centerY - 4, 7, detailColor);
      drawIconHLine(graphics, centerX - 2, centerY - 3, 6, detailColor);
      drawIconHLine(graphics, centerX - 5, centerY + 1, 5, accentColor);
   }

   private static void drawIconPixel(class_332 graphics, int x, int y, int color) {
      graphics.method_25294(x, y, x + 1, y + 1, color);
   }

   private static void drawIconHLine(class_332 graphics, int x, int y, int width, int color) {
      graphics.method_25294(x, y, x + width, y + 1, color);
   }

   private static void drawIconVLine(class_332 graphics, int x, int y, int height, int color) {
      graphics.method_25294(x, y, x + 1, y + height, color);
   }

   private static void drawRoundedGradientBox(
      class_332 graphics, int x, int y, int width, int height, int topColor, int bottomColor, int borderTop, int borderBottom
   ) {
      int r = 3;
      graphics.method_25294(x + r, y, x + width - r, y + height, -16777216);
      graphics.method_25294(x, y + r, x + width, y + height - r, -16777216);
      graphics.method_25296(x + r, y + 1, x + width - r, y + height - 1, topColor, bottomColor);
      graphics.method_25296(x + 1, y + r, x + width - 1, y + height - r, topColor, bottomColor);
      graphics.method_25296(x + r, y, x + width - r, y + 1, borderTop, borderTop);
      graphics.method_25296(x + r, y + height - 1, x + width - r, y + height, borderBottom, borderBottom);
      graphics.method_25296(x, y + r, x + 1, y + height - r, borderTop, borderBottom);
      graphics.method_25296(x + width - 1, y + r, x + width, y + height - r, borderTop, borderBottom);
      graphics.method_25294(x + 1, y + 1, x + 2, y + 2, borderTop);
      graphics.method_25294(x + width - 2, y + 1, x + width - 1, y + 2, borderTop);
      graphics.method_25294(x + 1, y + height - 2, x + 2, y + height - 1, borderBottom);
      graphics.method_25294(x + width - 2, y + height - 2, x + width - 1, y + height - 1, borderBottom);
   }

   private static int withAlpha(int color, int alpha) {
      return (alpha & 0xFF) << 24 | color & 16777215;
   }

   private static void renderMouseHoverTooltips(class_332 graphics, int mouseX, int mouseY) {
      class_437 currentScreen = class_310.method_1551().field_1755;
      if (currentScreen != null && isBattleGUI(currentScreen)) {
         Object[] newTiles = findBattleOptionTiles(currentScreen);
         if (newTiles != null && newTiles.length != 0) {
            if (cachedTiles != null && cachedTiles.length != 0) {
               for (Object tile : cachedTiles) {
                  if (tile != null) {
                     try {
                        int tileX = getTileX(tile);
                        int tileY = getTileY(tile);
                        int tileWidth = getTileWidth(tile);
                        int tileHeight = getTileHeight(tile);
                        if (tileX > 0 && tileY > 0 && tileWidth > 0 && tileHeight > 0) {
                           String tileClassName = tile.getClass().getName();
                           if (mouseX >= tileX && mouseX <= tileX + tileWidth && mouseY >= tileY && mouseY <= tileY + tileHeight) {
                              if (tileClassName.contains("BackButton")) {
                                 renderBackButtonTooltip(graphics, tileX + 4, tileY);
                                 return;
                              }

                              if (isGimmickButton(tile)) {
                                 renderGimmickTooltip(graphics, tile, tileX, tileY);
                                 return;
                              }

                              if (currentSelectionType.equals("switch") && tileClassName.contains("SwitchTile")) {
                                 queueDeferredSwitchTooltip(tile, tileX, tileY, tileWidth, tileHeight);
                                 return;
                              }
                           }
                        }
                     } catch (Throwable var13) {
                     }
                  }
               }
            }
         } else {
            clearTileCaches();
         }
      } else {
         clearTileCaches();
      }
   }

   private static boolean isMouseOverAnyTile(int mouseX, int mouseY) {
      if (cachedTiles != null && cachedTiles.length != 0) {
         for (Object tile : cachedTiles) {
            if (tile != null) {
               try {
                  int tileX = getTileX(tile);
                  int tileY = getTileY(tile);
                  int tileWidth = getTileWidth(tile);
                  int tileHeight = getTileHeight(tile);
                  if (tileX > 0 && tileY > 0 && tileWidth > 0 && tileHeight > 0) {
                     String tileClassName = tile.getClass().getName();
                     if (tileClassName.contains("MoveTile") || tileClassName.contains("GimmickTile") || tileClassName.contains("DynamaxTile")) {
                        tileX -= 10;
                        tileWidth += 10;
                     }

                     if (mouseX >= tileX && mouseX <= tileX + tileWidth && mouseY >= tileY && mouseY <= tileY + tileHeight) {
                        return true;
                     }
                  }
               } catch (Throwable var11) {
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static void drawButtonHighlight(class_332 graphics, class_437 gui, int mouseX, int mouseY) {
      try {
         if (System.currentTimeMillis() - lastNavigationTime < 80L) {
            return;
         }

         Object[] tiles = findBattleOptionTiles(gui);
         if (tiles == null || tiles.length == 0) {
            return;
         }

         boolean mouseOverTile = isMouseOverAnyTile(mouseX, mouseY);
         if (selectedButton >= tiles.length) {
            selectedButton = 0;
         }

         Object selectedTile = tiles[selectedButton];
         if (selectedTile == null) {
            return;
         }

         int x = getTileX(selectedTile);
         int y = getTileY(selectedTile);
         int width = getTileWidth(selectedTile);
         int height = getTileHeight(selectedTile);
         if (x <= 0 || y <= 0 || width <= 0 || height <= 0) {
            return;
         }

         String tileClassName = selectedTile.getClass().getName();
         if (tileClassName.contains("MoveTile") || tileClassName.contains("GimmickTile") || tileClassName.contains("DynamaxTile")) {
            x -= 10;
            width += 10;
         }

         if (tileClassName.contains("BackButton")) {
            x += 4;
         }

         if (tileClassName.contains("MoveTile") || tileClassName.contains("GimmickTile") || tileClassName.contains("DynamaxTile")) {
            boolean isLeftColumn = selectedButton % 2 == 0;
            renderSelectionArrows(graphics, x, y, width, height, isLeftColumn);
         } else if (currentSelectionType.equals("general") && tileClassName.contains("BattleOptionTile")) {
            boolean isLeftColumn = selectedButton % 2 == 0;
            renderSelectionArrows(graphics, x, y, width, height, isLeftColumn);
         } else if (tileClassName.contains("BackButton")) {
            renderSelectionArrows(graphics, x, y, width, height, true);
         } else if (isGimmickButton(selectedTile)) {
            renderSelectionArrows(graphics, x, y, width, height, false);
         } else {
            int brightnessOverlay = 822083583;
            graphics.method_25294(x, y, x + width, y + height, brightnessOverlay);
         }

         if (!mouseOverTile) {
            if (isGimmickButton(selectedTile)) {
               renderGimmickTooltip(graphics, selectedTile, x, y);
            }

            if (tileClassName.contains("BackButton")) {
               renderBackButtonTooltip(graphics, x, y);
            }

            if (currentSelectionType.equals("switch") && tileClassName.contains("SwitchTile")) {
               queueDeferredSwitchTooltip(selectedTile, x, y, width, height);
            }
         }
      } catch (Throwable var13) {
      }
   }

   private static boolean isSwitchScreenActive(class_437 screen) {
      try {
         Object actionSelection = getActionSelection(screen);
         if (actionSelection == null) {
            return false;
         } else {
            String selectionClassName = actionSelection.getClass().getName();
            return selectionClassName.contains("SwitchPokemonSelection");
         }
      } catch (Throwable var3) {
         return false;
      }
   }

   private static void queueDeferredSwitchTooltip(Object switchTile, int tileX, int tileY, int tileWidth, int tileHeight) {
      deferredSwitchTooltipTile = switchTile;
      deferredSwitchTooltipTileX = tileX;
      deferredSwitchTooltipTileY = tileY;
      deferredSwitchTooltipTileWidth = tileWidth;
      deferredSwitchTooltipTileHeight = tileHeight;
      deferredSwitchTooltipQueued = true;
   }

   public static void renderDeferredSwitchTooltip(class_332 graphics) {
      if (deferredSwitchTooltipQueued) {
         if (!"switch".equals(currentSelectionType)) {
            deferredSwitchTooltipQueued = false;
         } else if (deferredSwitchTooltipTile == null) {
            deferredSwitchTooltipQueued = false;
         } else {
            try {
               renderSwitchMovesTooltip(
                  graphics,
                  deferredSwitchTooltipTile,
                  deferredSwitchTooltipTileX,
                  deferredSwitchTooltipTileY,
                  deferredSwitchTooltipTileWidth,
                  deferredSwitchTooltipTileHeight
               );
            } catch (Throwable var5) {
            } finally {
               deferredSwitchTooltipQueued = false;
            }
         }
      }
   }

   public static void clearDeferredSwitchTooltip() {
      deferredSwitchTooltipQueued = false;
   }

   private static void drawFallbackHighlight(class_332 graphics, class_437 gui) {
      try {
         int OPTION_ROOT_X = 6;
         int OPTION_VERTICAL_OFFSET = 101;
         int OPTION_HORIZONTAL_SPACING = 3;
         int OPTION_VERTICAL_SPACING = 3;
         int TILE_WIDTH = 139;
         int TILE_HEIGHT = 39;

         try {
            Class<?> battleGUIClass = gui.getClass();
            Field rootXField = battleGUIClass.getDeclaredField("OPTION_ROOT_X");
            rootXField.setAccessible(true);
            OPTION_ROOT_X = (Integer)rootXField.get(null);
            Field vertOffsetField = battleGUIClass.getDeclaredField("OPTION_VERTICAL_OFFSET");
            vertOffsetField.setAccessible(true);
            OPTION_VERTICAL_OFFSET = (Integer)vertOffsetField.get(null);
            Field hSpacingField = battleGUIClass.getDeclaredField("OPTION_HORIZONTAL_SPACING");
            hSpacingField.setAccessible(true);
            OPTION_HORIZONTAL_SPACING = (Integer)hSpacingField.get(null);
            Field vSpacingField = battleGUIClass.getDeclaredField("OPTION_VERTICAL_SPACING");
            vSpacingField.setAccessible(true);
            OPTION_VERTICAL_SPACING = (Integer)vSpacingField.get(null);
         } catch (Throwable var16) {
         }

         try {
            Class<?> tileClass = Class.forName("com.cobblemon.mod.common.client.gui.battle.widgets.BattleOptionTile");
            Field widthField = tileClass.getDeclaredField("OPTION_WIDTH");
            widthField.setAccessible(true);
            TILE_WIDTH = (Integer)widthField.get(null);
            Field heightField = tileClass.getDeclaredField("OPTION_HEIGHT");
            heightField.setAccessible(true);
            TILE_HEIGHT = (Integer)heightField.get(null);
         } catch (Throwable var15) {
         }

         int screenHeight = class_310.method_1551().method_22683().method_4502();
         int row = selectedButton / 2;
         int col = selectedButton % 2;
         int bottomRowBottom = screenHeight - 10;
         int y;
         if (row == 1) {
            y = bottomRowBottom - TILE_HEIGHT;
         } else {
            y = bottomRowBottom - TILE_HEIGHT - OPTION_VERTICAL_SPACING - TILE_HEIGHT;
         }

         int x = OPTION_ROOT_X + col * (TILE_WIDTH + OPTION_HORIZONTAL_SPACING);
         int color = -1;
         graphics.method_25294(x - 2, y - 2, x + TILE_WIDTH + 2, y - 1, color);
         graphics.method_25294(x - 2, y + TILE_HEIGHT + 1, x + TILE_WIDTH + 2, y + TILE_HEIGHT + 2, color);
         graphics.method_25294(x - 2, y - 1, x - 1, y + TILE_HEIGHT + 1, color);
         graphics.method_25294(x + TILE_WIDTH + 1, y - 1, x + TILE_WIDTH + 2, y + TILE_HEIGHT + 1, color);
      } catch (Throwable var17) {
      }
   }

   private static Object[] findBattleOptionTiles(class_437 gui) {
      new ArrayList();

      try {
         Object actionSelection = getActionSelection(gui);
         if (actionSelection == null) {
            lastActionSelectionClassName = "";
            clearTileCaches();
            return null;
         }

         long now = System.currentTimeMillis();
         if (gui == cachedScreen && actionSelection == cachedActionSelection && cachedTiles != null && cachedTiles.length > 0 && cachedTilesExpiresAt >= now) {
            return cachedTiles;
         }

         String selectionClassName = actionSelection.getClass().getName();
         boolean selectionChanged = !selectionClassName.equals(lastActionSelectionClassName);
         List tiles;
         if (selectionClassName.contains("GeneralActionSelection")) {
            currentSelectionType = "general";
            tiles = getTilesFromSelection(actionSelection, "getTiles");
            maxButtons = 4;
         } else if (selectionClassName.contains("MoveSelection")) {
            currentSelectionType = "moves";
            tiles = getTilesFromSelection(actionSelection, "getMoveTiles");
            if (tiles.isEmpty()) {
               tiles = getTilesFromSelection(actionSelection, "getBaseTiles");
            }

            numMoveTiles = tiles.size();
            if (!tiles.isEmpty()) {
               Object[] sorted = sortTilesByPosition(tiles);
               tiles.clear();

               for (Object t : sorted) {
                  if (t != null) {
                     tiles.add(t);
                  }
               }

               numMoveTiles = tiles.size();
            }

            Object backButton = getBackButton(actionSelection);
            if (backButton != null) {
               tiles.add(backButton);
            }

            for (Object gimmickButton : getAllGimmickButtons(actionSelection)) {
               tiles.add(gimmickButton);
            }

            maxButtons = tiles.size();
         } else if (selectionClassName.contains("SwitchPokemonSelection")) {
            currentSelectionType = "switch";
            tiles = getTilesFromSelection(actionSelection, "getTiles");
            Object backButton = getBackButton(actionSelection);
            if (backButton != null) {
               tiles.add(backButton);
            }

            maxButtons = tiles.size();
            refreshPartyPPData();
         } else if (selectionClassName.contains("TargetSelection")) {
            currentSelectionType = "target";
            tiles = getTilesFromSelection(actionSelection, "getTiles");
            if (tiles.isEmpty()) {
               tiles = findTilesInFields(actionSelection);
            }

            maxButtons = tiles.size();
         } else {
            currentSelectionType = "unknown";
            tiles = getTilesFromSelection(actionSelection, "getTiles");
            if (tiles.isEmpty()) {
               tiles = findTilesInFields(actionSelection);
            }

            maxButtons = tiles.size();
         }

         if (selectedButton >= maxButtons && maxButtons > 0) {
            selectedButton = 0;
         }

         if (!tiles.isEmpty()) {
            Object[] result;
            if (currentSelectionType.equals("general")) {
               result = sortTilesByPosition(tiles);
            } else if (currentSelectionType.equals("target")) {
               result = sortTilesTopToBottomLeftToRight(tiles);
            } else {
               result = tiles.toArray();
            }

            if (currentSelectionType.equals("target")) {
               targetGridColumns = calculateTargetGridColumns(result);
               if (selectionChanged) {
                  selectedButton = 0;
               }
            }

            if (selectedButton >= result.length && result.length > 0) {
               selectedButton = 0;
            }

            cachedTiles = result;
            cachedScreen = gui;
            cachedActionSelection = actionSelection;
            cachedTilesExpiresAt = now + 50L;
            rebuildTileSnapshotCache(result);
            lastActionSelectionClassName = selectionClassName;
            return result;
         }

         lastActionSelectionClassName = selectionClassName;
      } catch (Throwable var12) {
         CobblemonBattleInfoClient.LOGGER.error("[CustomController] Error finding tiles: {}", var12.getMessage());
      }

      clearTileCaches();
      return null;
   }

   private static Object getActionSelection(class_437 gui) {
      try {
         Method getSelectionMethod = gui.getClass().getMethod("getCurrentActionSelection");
         return getSelectionMethod.invoke(gui);
      } catch (NoSuchMethodException var10) {
         for (Class<?> currentClass = gui.getClass(); currentClass != null && currentClass != Object.class; currentClass = currentClass.getSuperclass()) {
            for (Field f : currentClass.getDeclaredFields()) {
               String fieldName = f.getName().toLowerCase();
               if (fieldName.contains("action") || fieldName.contains("selection")) {
                  try {
                     f.setAccessible(true);
                     Object value = f.get(gui);
                     if (value != null && value.getClass().getName().contains("ActionSelection")) {
                        return value;
                     }
                  } catch (Throwable var9) {
                  }
               }
            }
         }
      } catch (Throwable var11) {
      }

      return null;
   }

   private static List<Object> getTilesFromSelection(Object actionSelection, String methodName) {
      List<Object> tiles = new ArrayList<>();

      try {
         Method getTilesMethod = actionSelection.getClass().getMethod(methodName);
         Object tilesObj = getTilesMethod.invoke(actionSelection);
         if (tilesObj instanceof List) {
            for (Object tile : (List)tilesObj) {
               if (tile != null) {
                  tiles.add(tile);
               }
            }
         }
      } catch (NoSuchMethodException var8) {
      } catch (Throwable var9) {
      }

      return tiles;
   }

   private static List<Object> findTilesInFields(Object actionSelection) {
      List<Object> tiles = new ArrayList<>();

      try {
         for (Field f : actionSelection.getClass().getDeclaredFields()) {
            if (f.getName().toLowerCase().contains("tile") || f.getType().getName().contains("List")) {
               try {
                  f.setAccessible(true);
                  Object value = f.get(actionSelection);
                  if (value instanceof List) {
                     for (Object tile : (List)value) {
                        if (tile != null && tile.getClass().getName().contains("Tile")) {
                           tiles.add(tile);
                        }
                     }

                     if (!tiles.isEmpty()) {
                        break;
                     }
                  }
               } catch (Throwable var10) {
               }
            }
         }
      } catch (Throwable var11) {
      }

      return tiles;
   }

   private static Object getBackButton(Object actionSelection) {
      try {
         Method getBackButtonMethod = actionSelection.getClass().getMethod("getBackButton");
         return getBackButtonMethod.invoke(actionSelection);
      } catch (NoSuchMethodException var4) {
         try {
            Field backButtonField = actionSelection.getClass().getDeclaredField("backButton");
            backButtonField.setAccessible(true);
            return backButtonField.get(actionSelection);
         } catch (Throwable var3) {
         }
      } catch (Throwable var5) {
      }

      return null;
   }

   private static Object getGimmickButton(Object actionSelection) {
      try {
         try {
            Method method = actionSelection.getClass().getMethod("getGimmickButtons");
            if (method.invoke(actionSelection) instanceof List<?> buttonList && !buttonList.isEmpty()) {
               return buttonList.get(0);
            }
         } catch (NoSuchMethodException var11) {
         }

         try {
            Method method = actionSelection.getClass().getMethod("getMegaTeraButton");
            Object button = method.invoke(actionSelection);
            if (button != null) {
               return button;
            }
         } catch (NoSuchMethodException var10) {
         }

         try {
            Method method = actionSelection.getClass().getMethod("getGimmickButton");
            Object button = method.invoke(actionSelection);
            if (button != null) {
               return button;
            }
         } catch (NoSuchMethodException var9) {
         }

         for (Class<?> currentClass = actionSelection.getClass();
            currentClass != null && currentClass != Object.class;
            currentClass = currentClass.getSuperclass()
         ) {
            for (Field f : currentClass.getDeclaredFields()) {
               String fieldName = f.getName().toLowerCase();
               if (fieldName.contains("mega")
                  || fieldName.contains("tera")
                  || fieldName.contains("gimmick")
                  || fieldName.contains("zmove")
                  || fieldName.contains("dynamax")) {
                  try {
                     f.setAccessible(true);
                     Object value = f.get(actionSelection);
                     if (value != null) {
                        String className = value.getClass().getName().toLowerCase();
                        if (className.contains("button") || className.contains("widget") || className.contains("tile") || className.contains("component")) {
                           return value;
                        }
                     }
                  } catch (Throwable var12) {
                  }
               }
            }
         }
      } catch (Throwable var13) {
         CobblemonBattleInfoClient.LOGGER.error("[CustomController] Error finding gimmick button: {}", var13.getMessage());
      }

      return null;
   }

   private static List<Object> getAllGimmickButtons(Object actionSelection) {
      List<Object> result = new ArrayList<>();

      try {
         try {
            Method method = actionSelection.getClass().getMethod("getGimmickButtons");
            Object buttons = method.invoke(actionSelection);
            if (buttons instanceof List) {
               for (Object button : (List)buttons) {
                  if (button != null && isGimmickButtonEnabled(button)) {
                     result.add(button);
                  }
               }

               if (!result.isEmpty()) {
                  return result;
               }
            }
         } catch (NoSuchMethodException var7) {
         }

         Object singleButton = getGimmickButton(actionSelection);
         if (singleButton != null && isGimmickButtonEnabled(singleButton)) {
            result.add(singleButton);
         }
      } catch (Throwable var8) {
         CobblemonBattleInfoClient.LOGGER.error("[CustomController] Error finding all gimmick buttons: {}", var8.getMessage());
      }

      return result;
   }

   private static boolean isGimmickButtonEnabled(Object button) {
      if (button == null) {
         return false;
      } else {
         try {
            try {
               Field activeField = null;

               for (Class<?> cls = button.getClass(); cls != null && cls != Object.class; cls = cls.getSuperclass()) {
                  try {
                     activeField = cls.getDeclaredField("active");
                     break;
                  } catch (NoSuchFieldException var9) {
                  }
               }

               if (activeField != null) {
                  activeField.setAccessible(true);
                  Object value = activeField.get(button);
                  if (value instanceof Boolean) {
                     boolean active = (Boolean)value;
                     CobblemonBattleInfoClient.debug("[CustomController] Gimmick button active field: {}", new Object[]{active});
                     return active;
                  }
               }
            } catch (Throwable var10) {
            }

            try {
               Method isActiveMethod = button.getClass().getMethod("isActive");
               Object result = isActiveMethod.invoke(button);
               if (result instanceof Boolean) {
                  boolean active = (Boolean)result;
                  CobblemonBattleInfoClient.debug("[CustomController] Gimmick button isActive(): {}", new Object[]{active});
                  return active;
               }
            } catch (NoSuchMethodException var6) {
            }

            try {
               Method getEnabledMethod = button.getClass().getMethod("getEnabled");
               Object result = getEnabledMethod.invoke(button);
               if (result instanceof Boolean) {
                  boolean enabled = (Boolean)result;
                  CobblemonBattleInfoClient.debug("[CustomController] Gimmick button getEnabled(): {}", new Object[]{enabled});
                  return enabled;
               }
            } catch (NoSuchMethodException var5) {
            }

            try {
               Field enabledField = null;

               for (Class<?> cls = button.getClass(); cls != null && cls != Object.class; cls = cls.getSuperclass()) {
                  try {
                     enabledField = cls.getDeclaredField("enabled");
                     break;
                  } catch (NoSuchFieldException var7) {
                  }
               }

               if (enabledField != null) {
                  enabledField.setAccessible(true);
                  Object value = enabledField.get(button);
                  if (value instanceof Boolean) {
                     boolean enabled = (Boolean)value;
                     CobblemonBattleInfoClient.debug("[CustomController] Gimmick button enabled field: {}", new Object[]{enabled});
                     return enabled;
                  }
               }
            } catch (Throwable var8) {
            }

            CobblemonBattleInfoClient.debug("[CustomController] Could not determine gimmick button state, defaulting to enabled", new Object[0]);
            return true;
         } catch (Throwable var11) {
            CobblemonBattleInfoClient.LOGGER.error("[CustomController] Error checking gimmick button enabled state: {}", var11.getMessage());
            return true;
         }
      }
   }

   private static boolean isGimmickButton(Object tile) {
      if (tile == null) {
         return false;
      } else {
         String className = tile.getClass().getName();
         return !className.contains("GimmickTile") && !className.contains("Tile")
            ? className.contains("GimmickButton")
               || className.contains("DynamaxButton")
               || className.contains("ZPowerButton")
               || className.contains("MegaButton")
               || className.contains("TeraButton")
            : false;
      }
   }

   private static void renderSelectionArrows(class_332 graphics, int x, int y, int width, int height, boolean isLeftColumn) {
      class_310 mc = class_310.method_1551();
      class_327 font = mc.field_1772;
      long time = System.currentTimeMillis();
      float bobOffset = (float)Math.sin((double)time / 150.0) * 3.0F;
      float colorPhase = (float)(0.5 + 0.5 * Math.sin((double)time / 300.0));
      int red1 = 255;
      int green1 = 50;
      int blue1 = 50;
      int red2 = 255;
      int green2 = 180;
      int blue2 = 50;
      int r = (int)((float)red1 + (float)(red2 - red1) * colorPhase);
      int g = (int)((float)green1 + (float)(green2 - green1) * colorPhase);
      int b = (int)((float)blue1 + (float)(blue2 - blue1) * colorPhase);
      float pulse = (float)(0.85 + 0.15 * Math.sin((double)time / 200.0));
      int alpha = (int)(255.0F * pulse);
      int arrowColor = alpha << 24 | r << 16 | g << 8 | b;
      int shadowColor = Integer.MIN_VALUE;
      int glowAlpha = (int)(180.0F * pulse);
      int glowColor = glowAlpha << 24 | Math.min(255, r + 30) << 16 | Math.min(255, g + 30) << 8 | Math.min(255, b + 30);
      String leftArrow = ">>";
      String rightArrow = "<<";
      int centerY = y + (height - 9) / 2;
      float arrowScale = 1.2F;
      if (isLeftColumn) {
         int arrowWidth = (int)((float)font.method_1727(leftArrow) * arrowScale);
         int leftArrowX = x - arrowWidth - 4 + (int)bobOffset;
         if (leftArrowX < 2) {
            leftArrowX = 2;
         }

         graphics.method_51448().method_22903();
         graphics.method_51448().method_46416((float)leftArrowX, (float)centerY, 0.0F);
         graphics.method_51448().method_22905(arrowScale, arrowScale, 1.0F);
         graphics.method_51433(font, leftArrow, 1, 1, shadowColor, false);
         graphics.method_51433(font, leftArrow, -1, 0, glowColor, false);
         graphics.method_51433(font, leftArrow, 1, 0, glowColor, false);
         graphics.method_51433(font, leftArrow, 0, 0, arrowColor, false);
         graphics.method_51448().method_22909();
      } else {
         int rightArrowX = x + width + 4 - (int)bobOffset;
         graphics.method_51448().method_22903();
         graphics.method_51448().method_46416((float)rightArrowX, (float)centerY, 0.0F);
         graphics.method_51448().method_22905(arrowScale, arrowScale, 1.0F);
         graphics.method_51433(font, rightArrow, 1, 1, shadowColor, false);
         graphics.method_51433(font, rightArrow, -1, 0, glowColor, false);
         graphics.method_51433(font, rightArrow, 1, 0, glowColor, false);
         graphics.method_51433(font, rightArrow, 0, 0, arrowColor, false);
         graphics.method_51448().method_22909();
      }
   }

   private static void renderGimmickTooltip(class_332 graphics, Object gimmickButton, int buttonX, int buttonY) {
      if (BattleExtrasConfig.isButtonTooltipEnabled()) {
         if (BattleExtrasConfig.isClassicButtonTooltipEnabled()) {
            renderClassicGimmickTooltip(graphics, gimmickButton, buttonX, buttonY);
         } else {
            try {
               String className = gimmickButton.getClass().getName().toLowerCase();
               boolean isTerastallize = false;
               class_2561 tooltipText;
               int tooltipColor;
               if (className.contains("mega")) {
                  tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.mega");
                  tooltipColor = -5635841;
               } else if (className.contains("tera")) {
                  tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.tera");
                  tooltipColor = -16733441;
                  isTerastallize = true;
               } else if (className.contains("dynamax") || className.contains("gigantamax")) {
                  tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.dynamax");
                  tooltipColor = -65434;
               } else if (className.contains("zpower") || className.contains("zmove")) {
                  tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.zmove");
                  tooltipColor = -22016;
               } else if (className.contains("ultra")) {
                  tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.ultra");
                  tooltipColor = -8960;
               } else {
                  String gimmickType = getGimmickType(gimmickButton);
                  if (gimmickType.contains("burst") || gimmickType.contains("ultra")) {
                     tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.ultra");
                     tooltipColor = -8960;
                  } else if (gimmickType.contains("mega")) {
                     tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.mega");
                     tooltipColor = -5635841;
                  } else if (gimmickType.contains("tera")) {
                     tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.tera");
                     tooltipColor = -16733441;
                     isTerastallize = true;
                  } else if (gimmickType.contains("dynamax")) {
                     tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.dynamax");
                     tooltipColor = -65434;
                  } else if (gimmickType.contains("z")) {
                     tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.zmove");
                     tooltipColor = -22016;
                  } else {
                     tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.generic");
                     tooltipColor = -1;
                  }
               }

               String teraTypeName = null;
               if (isTerastallize) {
                  teraTypeName = getActiveTeraType();
               }

               class_327 font = class_310.method_1551().field_1772;
               int textWidth = font.method_27525(tooltipText);
               int textHeight = 9;
               int iconSize = 14;
               int iconPadding = 3;
               int totalWidth = textWidth;
               if (teraTypeName != null) {
                  totalWidth = textWidth + iconSize + iconPadding;
               }

               int paddingH = 4;
               int paddingV = 3;
               int boxWidth = totalWidth + paddingH * 2;
               int boxHeight = textHeight + paddingV * 2;
               int tooltipX = buttonX + 8 - boxWidth / 2;
               int tooltipY = buttonY - boxHeight - 3;
               int screenWidth = class_310.method_1551().method_22683().method_4486();
               if (tooltipX < 5) {
                  tooltipX = 5;
               }

               if (tooltipX + boxWidth > screenWidth - 5) {
                  tooltipX = screenWidth - boxWidth - 5;
               }

               if (tooltipY < 5) {
                  tooltipY = 5;
               }

               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               graphics.method_51448().method_22903();
               graphics.method_51448().method_22904(0.0, 0.0, 520.0);
               int bgColor = -535816152;
               int borderEnd = darkenColor(tooltipColor, 0.6F);
               drawFancyTooltipBox(graphics, tooltipX, tooltipY, boxWidth, boxHeight, bgColor, tooltipColor, borderEnd);
               int textX = tooltipX + paddingH;
               int textY = tooltipY + paddingV;
               Theme theme = BattleUiThemePalette.getActiveBattleLogTheme();
               int tooltipTextColor = mixColors(theme.headerText(), -1, 0.14F);
               int tooltipTextShadow = withAlpha(theme.headerTextShadow(), 220);
               graphics.method_51439(font, tooltipText, textX + 1, textY + 1, tooltipTextShadow, false);
               graphics.method_51439(font, tooltipText, textX, textY, tooltipTextColor, false);
               if (teraTypeName != null) {
                  int iconX = textX + textWidth + iconPadding;
                  int iconY = textY + (textHeight - iconSize) / 2;
                  boolean megaShowdownLoaded = Services.PLATFORM.isModLoaded("mega_showdown");
                  if (megaShowdownLoaded) {
                     class_2960 teraIcon = class_2960.method_60654("mega_showdown:textures/gui/summary/tera_types/" + teraTypeName.toLowerCase() + ".png");
                     graphics.method_25293(teraIcon, iconX, iconY, iconSize, iconSize, 0.0F, 0.0F, 32, 32, 32, 32);
                  } else {
                     ElementalType elementType = TypeIconRenderer.stringToElementalType(teraTypeName);
                     if (elementType != null) {
                        int textureX = elementType.getTextureXMultiplier() * 36;
                        graphics.method_25293(COBBLEMON_TYPES_SHEET, iconX, iconY, iconSize, iconSize, (float)textureX, 0.0F, 36, 36, 648, 36);
                     }
                  }
               }

               graphics.method_51448().method_22909();
               RenderSystem.disableBlend();
            } catch (Throwable var35) {
            }
         }
      }
   }

   private static void renderBackButtonTooltip(class_332 graphics, int buttonX, int buttonY) {
      if (BattleExtrasConfig.isButtonTooltipEnabled()) {
         if (BattleExtrasConfig.isClassicButtonTooltipEnabled()) {
            renderClassicBackButtonTooltip(graphics, buttonX, buttonY);
         } else {
            try {
               class_2561 tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.back");
               int tooltipColor = -3355444;
               class_327 font = class_310.method_1551().field_1772;
               int textWidth = font.method_27525(tooltipText);
               int paddingH = 6;
               int paddingV = 4;
               int boxWidth = textWidth + paddingH * 2;
               int boxHeight = 9 + paddingV * 2;
               int tooltipX = buttonX + 8 - boxWidth / 2;
               int tooltipY = buttonY - boxHeight - 4;
               int screenWidth = class_310.method_1551().method_22683().method_4486();
               if (tooltipX < 5) {
                  tooltipX = 5;
               }

               if (tooltipX + boxWidth > screenWidth - 5) {
                  tooltipX = screenWidth - boxWidth - 5;
               }

               if (tooltipY < 5) {
                  tooltipY = 5;
               }

               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               graphics.method_51448().method_22903();
               graphics.method_51448().method_22904(0.0, 0.0, 520.0);
               int bgColor = -535816152;
               int borderEnd = darkenColor(tooltipColor, 0.6F);
               drawFancyTooltipBox(graphics, tooltipX, tooltipY, boxWidth, boxHeight, bgColor, tooltipColor, borderEnd);
               int textX = tooltipX + paddingH;
               int textY = tooltipY + paddingV;
               Theme theme = BattleUiThemePalette.getActiveBattleLogTheme();
               int tooltipTextColor = mixColors(theme.headerText(), -1, 0.1F);
               int tooltipTextShadow = withAlpha(theme.headerTextShadow(), 220);
               graphics.method_51439(font, tooltipText, textX + 1, textY + 1, tooltipTextShadow, false);
               graphics.method_51439(font, tooltipText, textX, textY, tooltipTextColor, false);
               graphics.method_51448().method_22909();
               RenderSystem.disableBlend();
            } catch (Throwable var22) {
            }
         }
      }
   }

   private static void renderClassicGimmickTooltip(class_332 graphics, Object gimmickButton, int buttonX, int buttonY) {
      try {
         String className = gimmickButton.getClass().getName().toLowerCase();
         boolean isTerastallize = false;
         class_2561 tooltipText;
         if (className.contains("mega")) {
            tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.mega");
         } else if (className.contains("tera")) {
            tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.tera");
            isTerastallize = true;
         } else if (className.contains("dynamax") || className.contains("gigantamax")) {
            tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.dynamax");
         } else if (className.contains("zpower") || className.contains("zmove")) {
            tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.zmove");
         } else if (className.contains("ultra")) {
            tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.ultra");
         } else {
            String gimmickType = getGimmickType(gimmickButton);
            if (gimmickType.contains("burst") || gimmickType.contains("ultra")) {
               tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.ultra");
            } else if (gimmickType.contains("mega")) {
               tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.mega");
            } else if (gimmickType.contains("tera")) {
               tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.tera");
               isTerastallize = true;
            } else if (gimmickType.contains("dynamax")) {
               tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.dynamax");
            } else if (gimmickType.contains("z")) {
               tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.zmove");
            } else {
               tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.gimmick.generic");
            }
         }

         String teraTypeName = null;
         if (isTerastallize) {
            teraTypeName = getActiveTeraType();
         }

         class_327 font = class_310.method_1551().field_1772;
         int textWidth = font.method_27525(tooltipText);
         int iconSize = 14;
         int iconPadding = 3;
         int totalWidth = textWidth;
         if (teraTypeName != null) {
            totalWidth = textWidth + iconSize + iconPadding;
         }

         int tooltipTextureWidth = 64;
         int tooltipTextureHeight = 16;
         int paddingH = 6;
         int tooltipWidth = totalWidth + paddingH * 2;
         int tooltipX = buttonX + 8 - tooltipWidth / 2;
         int tooltipY = buttonY - tooltipTextureHeight - 3;
         int screenWidth = class_310.method_1551().method_22683().method_4486();
         if (tooltipX < 5) {
            tooltipX = 5;
         }

         if (tooltipX + tooltipWidth > screenWidth - 5) {
            tooltipX = screenWidth - tooltipWidth - 5;
         }

         if (tooltipY < 5) {
            tooltipY = 5;
         }

         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         graphics.method_51448().method_22903();
         graphics.method_51448().method_22904(0.0, 0.0, 520.0);
         drawClassicButtonTooltipBackground(graphics, tooltipX, tooltipY, tooltipWidth, tooltipTextureHeight);
         int textX = tooltipX + paddingH;
         int textY = tooltipY + (tooltipTextureHeight - 9) / 2 + 1;
         graphics.method_51439(font, tooltipText, textX, textY, -1, true);
         if (teraTypeName != null) {
            int iconX = textX + textWidth + iconPadding;
            int iconY = textY + (9 - iconSize) / 2 - 1;
            boolean megaShowdownLoaded = Services.PLATFORM.isModLoaded("mega_showdown");
            if (megaShowdownLoaded) {
               class_2960 teraIcon = class_2960.method_60654("mega_showdown:textures/gui/summary/tera_types/" + teraTypeName.toLowerCase() + ".png");
               graphics.method_25293(teraIcon, iconX, iconY, iconSize, iconSize, 0.0F, 0.0F, 32, 32, 32, 32);
            } else {
               ElementalType elementType = TypeIconRenderer.stringToElementalType(teraTypeName);
               if (elementType != null) {
                  int textureX = elementType.getTextureXMultiplier() * 36;
                  graphics.method_25293(COBBLEMON_TYPES_SHEET, iconX, iconY, iconSize, iconSize, (float)textureX, 0.0F, 36, 36, 648, 36);
               }
            }
         }

         graphics.method_51448().method_22909();
         RenderSystem.disableBlend();
      } catch (Throwable var28) {
      }
   }

   private static void renderClassicBackButtonTooltip(class_332 graphics, int buttonX, int buttonY) {
      try {
         class_2561 tooltipText = class_2561.method_43471("tooltip.cobblemon-battle-extras.back");
         class_327 font = class_310.method_1551().field_1772;
         int textWidth = font.method_27525(tooltipText);
         int tooltipTextureWidth = 64;
         int tooltipTextureHeight = 16;
         int paddingH = 6;
         int tooltipWidth = textWidth + paddingH * 2;
         int tooltipX = buttonX + 8 - tooltipWidth / 2;
         int tooltipY = buttonY - tooltipTextureHeight - 4;
         int screenWidth = class_310.method_1551().method_22683().method_4486();
         if (tooltipX < 5) {
            tooltipX = 5;
         }

         if (tooltipX + tooltipWidth > screenWidth - 5) {
            tooltipX = screenWidth - tooltipWidth - 5;
         }

         if (tooltipY < 5) {
            tooltipY = 5;
         }

         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         graphics.method_51448().method_22903();
         graphics.method_51448().method_22904(0.0, 0.0, 520.0);
         drawClassicButtonTooltipBackground(graphics, tooltipX, tooltipY, tooltipWidth, tooltipTextureHeight);
         int textX = tooltipX + paddingH;
         int textY = tooltipY + (tooltipTextureHeight - 9) / 2 + 1;
         graphics.method_51439(font, tooltipText, textX, textY, -1, true);
         graphics.method_51448().method_22909();
         RenderSystem.disableBlend();
      } catch (Throwable var16) {
      }
   }

   private static void drawClassicButtonTooltipBackground(class_332 graphics, int x, int y, int width, int height) {
      class_2960 tooltipTexture = class_2960.method_60655("cobblemon-battle-extras", "textures/gui/button_tooltip.png");
      int textureWidth = 64;
      int textureHeight = 16;
      int capWidth = 4;
      int centerSourceWidth = 2;
      int centerSourceX = 31;
      int renderWidth = Math.max(width, 9);
      int centerTargetWidth = renderWidth - 8;
      graphics.method_25293(tooltipTexture, x, y, 4, height, 0.0F, 0.0F, 4, 16, 64, 16);
      graphics.method_25293(tooltipTexture, x + 4, y, centerTargetWidth, height, 31.0F, 0.0F, 2, 16, 64, 16);
      graphics.method_25293(tooltipTexture, x + 4 + centerTargetWidth, y, 4, height, 60.0F, 0.0F, 4, 16, 64, 16);
   }

   private static void renderSwitchScreenTypeIcons(class_332 graphics) {
      if (cachedTiles != null && cachedTiles.length != 0) {
         try {
            int iconSrcSize = 36;
            int sheetWidth = 648;
            int sheetHeight = 36;
            int displaySize = 12;
            int iconSpacing = 1;
            int teraIconSize = 32;
            boolean megaShowdownLoaded = isMegaShowdownLoadedCached();

            for (Object tile : cachedTiles) {
               if (tile != null) {
                  String tileClassName = tile.getClass().getName();
                  if (tileClassName.contains("SwitchTile")) {
                     try {
                        int tileX = getTileX(tile);
                        int tileY = getTileY(tile);
                        int tileWidth = getTileWidth(tile);
                        int tileHeight = getTileHeight(tile);
                        if (tileX > 0 && tileY > 0) {
                           Object pokemon = getPokemonFromSwitchTile(tile);
                           if (pokemon != null) {
                              String pokemonName = getPokemonSpeciesName(pokemon);
                              CustomBattleController.SwitchTypeIconCacheData typeData = getSwitchTypeIconCacheData(pokemon, pokemonName);
                              if (typeData != null) {
                                 boolean isTerastallized = typeData.terastallized;
                                 String teraType = typeData.teraType;
                                 List<ElementalType> types = typeData.types;
                                 boolean isStellar = isTerastallized && megaShowdownLoaded && teraType != null && teraType.equalsIgnoreCase("stellar");
                                 if (types != null && !types.isEmpty() || isStellar) {
                                    int iconX = tileX + tileWidth - 18;
                                    int iconY = tileY + tileHeight - displaySize - 4;
                                    RenderSystem.enableBlend();
                                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                                    int offsetX = 0;
                                    if (isStellar) {
                                       class_2960 stellarIcon = getTeraIconResource("stellar");
                                       graphics.method_25293(
                                          stellarIcon,
                                          iconX - offsetX - displaySize,
                                          iconY,
                                          displaySize,
                                          displaySize,
                                          0.0F,
                                          0.0F,
                                          teraIconSize,
                                          teraIconSize,
                                          teraIconSize,
                                          teraIconSize
                                       );
                                    } else {
                                       for (ElementalType type : types) {
                                          if (isTerastallized && megaShowdownLoaded) {
                                             String typeName = type.getName();
                                             class_2960 teraIcon = getTeraIconResource(typeName);
                                             graphics.method_25293(
                                                teraIcon,
                                                iconX - offsetX - displaySize,
                                                iconY,
                                                displaySize,
                                                displaySize,
                                                0.0F,
                                                0.0F,
                                                teraIconSize,
                                                teraIconSize,
                                                teraIconSize,
                                                teraIconSize
                                             );
                                          } else {
                                             int textureX = type.getTextureXMultiplier() * iconSrcSize;
                                             graphics.method_25293(
                                                COBBLEMON_TYPES_SHEET,
                                                iconX - offsetX - displaySize,
                                                iconY,
                                                displaySize,
                                                displaySize,
                                                (float)textureX,
                                                0.0F,
                                                iconSrcSize,
                                                iconSrcSize,
                                                sheetWidth,
                                                sheetHeight
                                             );
                                          }

                                          offsetX += displaySize + iconSpacing;
                                       }
                                    }

                                    if (typeData.megaEvolved) {
                                       int megaIconSize = 64;
                                       int megaDisplaySize = MEGA_ICON_SIZE_SWITCH;
                                       int typeIconsWidth = types.size() * displaySize + (types.size() - 1) * iconSpacing;
                                       int megaIconX = iconX - typeIconsWidth - megaDisplaySize - 2 + MEGA_ICON_OFFSET_X_SWITCH;
                                       int megaIconY = iconY + MEGA_ICON_OFFSET_Y_SWITCH;
                                       graphics.method_25293(
                                          MEGA_ICON_SHEET,
                                          megaIconX,
                                          megaIconY,
                                          megaDisplaySize,
                                          megaDisplaySize,
                                          0.0F,
                                          0.0F,
                                          megaIconSize,
                                          megaIconSize,
                                          megaIconSize,
                                          megaIconSize
                                       );
                                    }
                                 }
                              }
                           }
                        }
                     } catch (Throwable var32) {
                     }
                  }
               }
            }
         } catch (Throwable var33) {
         }
      }
   }

   private static void renderSwitchScreenMatchupBorders(class_332 graphics) {
      if (cachedTiles != null && cachedTiles.length != 0) {
         List<OpponentMatchupEntry> opponents = getActiveOpponentEntries();
         if (!opponents.isEmpty()) {
            for (Object tile : cachedTiles) {
               if (tile != null) {
                  String tileClassName = tile.getClass().getName();
                  if (tileClassName.contains("SwitchTile")) {
                     try {
                        int tileX = getTileX(tile);
                        int tileY = getTileY(tile);
                        int tileWidth = getTileWidth(tile);
                        int tileHeight = getTileHeight(tile);
                        if (tileX > 0 && tileY > 0 && tileWidth > 0 && tileHeight > 0) {
                           Object pokemon = getPokemonFromSwitchTile(tile);
                           if (pokemon != null) {
                              List<ElementalType> partyTypes = getTypesFromPokemon(pokemon);
                              if (partyTypes != null && !partyTypes.isEmpty()) {
                                 MatchupBorderColor borderColor = computeBestCaseBorderColor(partyTypes, opponents);
                                 int colorARGB = getMatchupBorderColorARGB(borderColor);
                                 if (colorARGB != 0) {
                                    int pipSize = 5;
                                    int pipMargin = 3;
                                    graphics.method_25294(
                                       tileX + pipMargin, tileY + pipMargin, tileX + pipMargin + pipSize, tileY + pipMargin + pipSize, colorARGB
                                    );
                                 }
                              }
                           }
                        }
                     } catch (Throwable var17) {
                     }
                  }
               }
            }
         }
      }
   }

   private static List<OpponentMatchupEntry> getActiveOpponentEntries() {
      long now = System.currentTimeMillis();
      if (cachedOpponentEntries != null && now < opponentEntriesCacheExpiry) {
         return cachedOpponentEntries;
      } else {
         List<OpponentMatchupEntry> result = new ArrayList<>();

         try {
            ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
            if (battle != null) {
               ClientBattleSide opponentSide = BattleSideHelper.getOpponentSide(battle);
               if (opponentSide != null) {
                  for (ClientBattleActor actor : opponentSide.getActors()) {
                     for (ActiveClientBattlePokemon activePokemon : actor.getActivePokemon()) {
                        if (activePokemon != null) {
                           ClientBattlePokemon battlePokemon = activePokemon.getBattlePokemon();
                           if (battlePokemon != null) {
                              String speciesName = null;

                              try {
                                 speciesName = battlePokemon.getSpecies().getName();
                              } catch (Throwable var14) {
                              }

                              if (speciesName != null) {
                                 String displayName = battlePokemon.getDisplayName().getString();
                                 List<ElementalType> types = TypeIconRenderer.getDisplayTypes(
                                    battlePokemon, speciesName, "opponent", battlePokemon.getUuid() != null ? battlePokemon.getUuid().toString() : null
                                 );
                                 if (types != null && !types.isEmpty()) {
                                    Object matchedForm = resolveBattlePokemonForm(battlePokemon.getSpecies(), battlePokemon.getProperties().getForm());
                                    result.add(new OpponentMatchupEntry(displayName, types, speciesName, matchedForm, battlePokemon));
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         } catch (Throwable var15) {
            CobblemonBattleInfoClient.debug("[MatchupBorder] Error building opponent entries: {}", new Object[]{var15.getMessage()});
         }

         cachedOpponentEntries = result;
         opponentEntriesCacheExpiry = now + 500L;
         return result;
      }
   }

   private static Object resolveBattlePokemonForm(Object species, String formName) {
      if (species != null && formName != null && !formName.isBlank()) {
         try {
            Method getForms = species.getClass().getMethod("getForms");
            if (!(getForms.invoke(species) instanceof Iterable<?> forms)) {
               return null;
            }

            for (Object form : forms) {
               if (form != null) {
                  String availableFormName;
                  try {
                     Method getName = form.getClass().getMethod("getName");
                     Object rawName = getName.invoke(form);
                     availableFormName = rawName == null ? null : rawName.toString();
                  } catch (Throwable var10) {
                     continue;
                  }

                  if (availableFormName != null && formNamesMatch(availableFormName, formName)) {
                     return form;
                  }
               }
            }
         } catch (Throwable var11) {
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean formNamesMatch(String availableFormName, String formName) {
      if (availableFormName == null || formName == null) {
         return false;
      } else if (availableFormName.equalsIgnoreCase(formName)) {
         return true;
      } else if (availableFormName.equalsIgnoreCase(formName.replace("_", "-"))) {
         return true;
      } else if (availableFormName.equalsIgnoreCase(formName.replace("-", "_"))) {
         return true;
      } else {
         String normalizedAvailable = availableFormName.replace(" ", "").replace("-", "").replace("_", "");
         String normalizedRequested = formName.replace(" ", "").replace("-", "").replace("_", "");
         return normalizedAvailable.equalsIgnoreCase(normalizedRequested);
      }
   }

   private static MatchupBorderColor computeMatchupForOpponent(List<ElementalType> opponentTypes, List<ElementalType> partyTypes) {
      if (opponentTypes != null && !opponentTypes.isEmpty()) {
         if (partyTypes != null && !partyTypes.isEmpty()) {
            String partyType1 = partyTypes.get(0).getName().toLowerCase();
            String partyType2 = partyTypes.size() > 1 ? partyTypes.get(1).getName().toLowerCase() : null;
            boolean hasResist = false;
            boolean hasWeak = false;

            for (ElementalType opType : opponentTypes) {
               float eff = TypeChart.getEffectivenessAgainstTypes(opType.getName().toLowerCase(), partyType1, partyType2);
               if (eff <= 0.5F) {
                  hasResist = true;
               }

               if (eff >= 2.0F) {
                  hasWeak = true;
               }
            }

            if (hasResist && hasWeak) {
               return MatchupBorderColor.YELLOW;
            } else if (hasWeak) {
               return MatchupBorderColor.RED;
            } else {
               return hasResist ? MatchupBorderColor.GREEN : MatchupBorderColor.GRAY;
            }
         } else {
            return MatchupBorderColor.NONE;
         }
      } else {
         return MatchupBorderColor.NONE;
      }
   }

   private static MatchupBorderColor computeBestCaseBorderColor(List<ElementalType> partyTypes, List<OpponentMatchupEntry> opponents) {
      if (opponents != null && !opponents.isEmpty()) {
         MatchupBorderColor best = MatchupBorderColor.NONE;

         for (OpponentMatchupEntry opponent : opponents) {
            MatchupBorderColor color = computeMatchupForOpponent(opponent.types, partyTypes);
            if (color != MatchupBorderColor.NONE && matchupColorRank(color) > matchupColorRank(best)) {
               best = color;
            }
         }

         return best;
      } else {
         return MatchupBorderColor.NONE;
      }
   }

   private static int matchupColorRank(MatchupBorderColor c) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException: Cannot read field "classStruct" because "classNode" is null
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifyNewEnumSwitch(SwitchHelper.java:319)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplify(SwitchHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:30)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:376)
      //
      // Bytecode:
      // 00: aload 0
      // 01: invokevirtual name/modid/client/CustomBattleController$MatchupBorderColor.ordinal ()I
      // 04: tableswitch 48 1 4 32 36 40 44
      // 24: bipush 4
      // 25: goto 35
      // 28: bipush 3
      // 29: goto 35
      // 2c: bipush 2
      // 2d: goto 35
      // 30: bipush 1
      // 31: goto 35
      // 34: bipush 0
      // 35: ireturn
   }

   private static int getMatchupBorderColorARGB(MatchupBorderColor color) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException: Cannot read field "classStruct" because "classNode" is null
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifyNewEnumSwitch(SwitchHelper.java:319)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplify(SwitchHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:30)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:376)
      //
      // Bytecode:
      // 00: aload 0
      // 01: invokevirtual name/modid/client/CustomBattleController$MatchupBorderColor.ordinal ()I
      // 04: tableswitch 56 1 4 32 38 44 50
      // 24: ldc_w -866779290
      // 27: goto 3d
      // 2a: ldc_w -855646942
      // 2d: goto 3d
      // 30: ldc_w -862348903
      // 33: goto 3d
      // 36: ldc_w -855686076
      // 39: goto 3d
      // 3c: bipush 0
      // 3d: ireturn
   }

   private static List<MatchupLineDisplay> computeMatchupDisplayLines(List<OpponentMatchupEntry> opponents, List<ElementalType> partyTypes) {
      List<MatchupLineDisplay> lines = new ArrayList<>();
      if (opponents != null && !opponents.isEmpty()) {
         if (partyTypes != null && !partyTypes.isEmpty()) {
            String partyType1 = partyTypes.get(0).getName().toLowerCase();
            String partyType2 = partyTypes.size() > 1 ? partyTypes.get(1).getName().toLowerCase() : null;

            for (OpponentMatchupEntry opponent : opponents) {
               if (opponent.types != null && !opponent.types.isEmpty()) {
                  boolean hasResist = false;
                  boolean hasWeak = false;
                  boolean hasImmune = false;
                  boolean hasSuperWeak = false;

                  for (ElementalType opType : opponent.types) {
                     float eff = TypeChart.getEffectivenessAgainstTypes(opType.getName().toLowerCase(), partyType1, partyType2);
                     if (eff == 0.0F) {
                        hasImmune = true;
                        hasResist = true;
                     } else if (eff <= 0.5F) {
                        hasResist = true;
                     }

                     if (eff >= 4.0F) {
                        hasSuperWeak = true;
                     }

                     if (eff >= 2.0F) {
                        hasWeak = true;
                     }
                  }

                  class_5250 typeComponent = class_2561.method_43473();

                  for (int i = 0; i < opponent.types.size(); i++) {
                     if (i > 0) {
                        typeComponent.method_10852(class_2561.method_43470("/").method_27694(s -> s.method_36139(-7829368)));
                     }

                     typeComponent.method_10852(BattleMessageColorizer.colorizeTypeComponent(((ElementalType)opponent.types.get(i)).getDisplayName()));
                  }

                  String labelKey;
                  int labelColor;
                  if (hasResist && hasWeak) {
                     labelKey = "switch.tooltip.matchup.mixed";
                     labelColor = -8926;
                  } else if (hasSuperWeak) {
                     labelKey = "switch.tooltip.matchup.very_weak";
                     labelColor = -48060;
                  } else if (hasWeak) {
                     labelKey = "switch.tooltip.matchup.weak";
                     labelColor = -39322;
                  } else if (hasImmune) {
                     labelKey = "switch.tooltip.matchup.immune";
                     labelColor = -11141274;
                  } else if (hasResist) {
                     labelKey = "switch.tooltip.matchup.resists";
                     labelColor = -11141274;
                  } else {
                     labelKey = "switch.tooltip.matchup.neutral";
                     labelColor = -5592406;
                  }

                  class_5250 line = class_2561.method_43471("switch.tooltip.matchup.vs")
                     .method_27694(s -> s.method_36139(-7829368))
                     .method_10852(class_2561.method_43470(" ").method_27694(s -> s.method_36139(-7829368)))
                     .method_10852(class_2561.method_43470(opponent.displayName).method_27694(s -> s.method_36139(-2236963)))
                     .method_10852(class_2561.method_43470(" (").method_27694(s -> s.method_36139(-7829368)))
                     .method_10852(typeComponent)
                     .method_10852(class_2561.method_43470("): ").method_27694(s -> s.method_36139(-7829368)))
                     .method_10852(class_2561.method_43471(labelKey).method_27694(s -> s.method_36139(labelColor)));
                  lines.add(new MatchupLineDisplay(line));
               }
            }

            return lines;
         } else {
            return lines;
         }
      } else {
         return lines;
      }
   }

   private static List<ElementalType> getTypesFromPokemon(Object pokemon) {
      List<ElementalType> result = new ArrayList<>();

      try {
         String pokemonName = getPokemonSpeciesName(pokemon);
         String pokemonUUID = getPokemonUuidString(pokemon);

         try {
            Class<?> randomizerClientClass = Class.forName("cobblemon.randomizer.client.RandomizerClient");
            Method isRunActive = randomizerClientClass.getMethod("isRunActive");
            if ((Boolean)isRunActive.invoke(null)) {
               String[] randomizedTypes = null;
               if (pokemonUUID != null && !pokemonUUID.isEmpty()) {
                  try {
                     Method hasOpponentTypes = randomizerClientClass.getMethod("hasOpponentPokemonTypes");
                     if ((Boolean)hasOpponentTypes.invoke(null)) {
                        Method getOpponentTypes = randomizerClientClass.getMethod("getOpponentPokemonTypes", String.class);
                        randomizedTypes = (String[])getOpponentTypes.invoke(null, pokemonUUID);
                     }
                  } catch (Throwable var17) {
                  }

                  if (randomizedTypes == null || randomizedTypes.length == 0) {
                     try {
                        Method getWildTypes = randomizerClientClass.getMethod("getWildPokemonTypes", String.class);
                        randomizedTypes = (String[])getWildTypes.invoke(null, pokemonUUID);
                     } catch (Throwable var16) {
                     }
                  }
               }

               if ((randomizedTypes == null || randomizedTypes.length == 0) && pokemonUUID != null && !pokemonUUID.isEmpty() && pokemonName != null) {
                  Method getTypesForPokemon = randomizerClientClass.getMethod("getTypesForPokemon", String.class, String.class);
                  randomizedTypes = (String[])getTypesForPokemon.invoke(null, pokemonName, pokemonUUID);
               }

               if ((randomizedTypes == null || randomizedTypes.length == 0) && pokemonName != null) {
                  Method getTypesForSpecies = randomizerClientClass.getMethod("getTypesForSpecies", String.class);
                  randomizedTypes = (String[])getTypesForSpecies.invoke(null, pokemonName);
               }

               if (randomizedTypes != null && randomizedTypes.length > 0) {
                  for (String typeName : randomizedTypes) {
                     ElementalType type = stringToElementalType(typeName);
                     if (type != null) {
                        result.add(type);
                     }
                  }

                  if (!result.isEmpty()) {
                     CobblemonBattleInfoClient.debug(
                        "[CustomBattleController] Got {} randomized types from Randomizer for {} (UUID: {})",
                        new Object[]{result.size(), pokemonName, pokemonUUID}
                     );
                     return result;
                  }
               }
            }
         } catch (ClassNotFoundException var18) {
         } catch (Throwable var19) {
            CobblemonBattleInfoClient.debug("[CustomBattleController] Failed to get randomized types: {}", new Object[]{var19.getMessage()});
         }

         if (pokemonName != null && BattleMessageSubscriber.isSoaked(pokemonName)) {
            ElementalType waterType = stringToElementalType("water");
            if (waterType != null) {
               result.add(waterType);
               return result;
            }
         }

         if (pokemonName != null) {
            String teraType = BattleMessageSubscriber.getTeraType(pokemonName);
            if (teraType != null && !teraType.isEmpty()) {
               ElementalType teraElementType = stringToElementalType(teraType);
               if (teraElementType != null) {
                  result.add(teraElementType);
                  return result;
               }
            }
         }

         if (pokemonName != null) {
            List<String> megaTypes = BattleMessageSubscriber.getMegaTypes(pokemonName);
            if (megaTypes != null && !megaTypes.isEmpty()) {
               for (String typeNamex : megaTypes) {
                  ElementalType megaType = stringToElementalType(typeNamex);
                  if (megaType != null) {
                     result.add(megaType);
                  }
               }

               if (!result.isEmpty()) {
                  if (BattleMessageSubscriber.isRoosted(pokemonName)) {
                     result.removeIf(typex -> typex.getName().equalsIgnoreCase("flying"));
                     if (result.isEmpty()) {
                        ElementalType normalType = stringToElementalType("normal");
                        if (normalType != null) {
                           result.add(normalType);
                        }
                     }
                  }

                  return result;
               }
            }
         }

         try {
            Method getPrimary = pokemon.getClass().getMethod("getPrimaryType");
            Object primary = getPrimary.invoke(pokemon);
            if (primary instanceof ElementalType) {
               result.add((ElementalType)primary);
            }

            Method getSecondary = pokemon.getClass().getMethod("getSecondaryType");
            Object secondary = getSecondary.invoke(pokemon);
            if (secondary instanceof ElementalType) {
               result.add((ElementalType)secondary);
            }
         } catch (Throwable var15) {
         }

         if (!result.isEmpty()) {
            CobblemonBattleInfoClient.debug(
               "[CustomBattleController] Got {} types from getPrimaryType/getSecondaryType for {} (UUID: {})",
               new Object[]{result.size(), pokemonName, pokemonUUID}
            );
         } else {
            Object types = null;

            try {
               Method getForm = pokemon.getClass().getMethod("getForm");
               Object form = getForm.invoke(pokemon);
               if (form != null) {
                  Method getTypes = form.getClass().getMethod("getTypes");
                  types = getTypes.invoke(form);
               }
            } catch (Throwable var14) {
            }

            if (types == null) {
               try {
                  Method getSpecies = pokemon.getClass().getMethod("getSpecies");
                  Object species = getSpecies.invoke(pokemon);
                  if (species != null) {
                     Method getTypes = species.getClass().getMethod("getTypes");
                     types = getTypes.invoke(species);
                  }
               } catch (Throwable var13) {
               }
            }

            if (types == null) {
               try {
                  Method getTypes = pokemon.getClass().getMethod("getTypes");
                  types = getTypes.invoke(pokemon);
               } catch (Throwable var12) {
               }
            }

            if (types instanceof Iterable) {
               for (Object type : (Iterable)types) {
                  if (type instanceof ElementalType) {
                     result.add((ElementalType)type);
                  }
               }
            }
         }

         if (pokemonName != null && BattleMessageSubscriber.isRoosted(pokemonName) && !result.isEmpty()) {
            result.removeIf(typex -> {
               String typeNamexx = typex.getName().toLowerCase();
               return typeNamexx.equals("flying");
            });
            if (result.isEmpty()) {
               ElementalType normalType = stringToElementalType("normal");
               if (normalType != null) {
                  result.add(normalType);
               }
            }
         }

         if (pokemonName != null && BattleMessageSubscriber.hasBurnedUp(pokemonName) && !result.isEmpty()) {
            result.removeIf(typex -> {
               String typeNamexx = typex.getName().toLowerCase();
               return typeNamexx.equals("fire");
            });
         }
      } catch (Throwable var20) {
      }

      return result;
   }

   private static List<ElementalType> getTypesDirectlyFromPokemon(Object pokemon, String pokemonName) {
      List<ElementalType> result = new ArrayList<>();
      if (pokemonName != null) {
         String teraType = BattleMessageSubscriber.getTeraType(pokemonName);
         if (teraType != null && !teraType.isEmpty()) {
            if (teraType.equalsIgnoreCase("Stellar")) {
               return null;
            }

            ElementalType teraElementType = stringToElementalType(teraType);
            if (teraElementType != null) {
               result.add(teraElementType);
               return result;
            }
         }

         if (BattleMessageSubscriber.isSoaked(pokemonName)) {
            ElementalType waterType = stringToElementalType("water");
            if (waterType != null) {
               result.add(waterType);
            }

            return result;
         }
      }

      try {
         Method getPrimary = pokemon.getClass().getMethod("getPrimaryType");
         Object primary = getPrimary.invoke(pokemon);
         if (primary instanceof ElementalType) {
            result.add((ElementalType)primary);
         }

         Method getSecondary = pokemon.getClass().getMethod("getSecondaryType");
         Object secondary = getSecondary.invoke(pokemon);
         if (secondary instanceof ElementalType) {
            result.add((ElementalType)secondary);
         }

         CobblemonBattleInfoClient.debug(
            "[CustomBattleController] Got {} types from Pokemon.getPrimaryType/getSecondaryType for {}", new Object[]{result.size(), pokemonName}
         );
      } catch (Throwable var7) {
         CobblemonBattleInfoClient.debug("[CustomBattleController] Failed to get types from Pokemon for {}: {}", new Object[]{pokemonName, var7.getMessage()});
      }

      return result;
   }

   private static ElementalType stringToElementalType(String typeName) {
      if (typeName != null && !typeName.isEmpty()) {
         try {
            Class<?> elementalTypesClass = Class.forName("com.cobblemon.mod.common.api.types.ElementalTypes");
            Method getMethod = elementalTypesClass.getMethod("get", String.class);
            Object result = getMethod.invoke(null, typeName.toLowerCase());
            if (result instanceof ElementalType) {
               return (ElementalType)result;
            }
         } catch (Throwable var4) {
            CobblemonBattleInfoClient.debug("Could not resolve type: {}", new Object[]{typeName});
         }

         return null;
      } else {
         return null;
      }
   }

   private static void renderSwitchMovesTooltip(class_332 graphics, Object switchTile, int tileX, int tileY, int tileWidth, int tileHeight) {
      if (BattleExtrasConfig.isSwitchTooltipsEnabled()) {
         try {
            Object pokemon = getPokemonFromSwitchTile(switchTile);
            if (pokemon == null) {
               return;
            }

            int iconDisplaySize = 10;
            class_327 font = class_310.method_1551().field_1772;
            Theme theme = BattleUiThemePalette.getActiveBattleLogTheme();
            int lineHeight = 12;
            int padding = 6;
            float previewScale = 0.75F;
            int previewLineHeight = (int)Math.ceil(7.5);
            String cacheKey = buildSwitchTooltipCacheKey(pokemon);
            SwitchTooltipCacheData cached = getSwitchTooltipCache(cacheKey);
            List<MoveDisplayInfo> moveInfos;
            String abilityName;
            Integer speedStat;
            int boxWidth;
            int boxHeight;
            if (cached != null) {
               moveInfos = cached.moveInfos;
               abilityName = cached.abilityName;
               speedStat = cached.speedStat;
               boxWidth = cached.boxWidth;
               boxHeight = cached.boxHeight;
            } else {
               moveInfos = getMovesWithPPFromPokemon(pokemon);
               if (moveInfos.isEmpty()) {
                  return;
               }

               abilityName = getAbilityFromPokemon(pokemon);
               speedStat = getActualSwitchTooltipSpeed(pokemon);
               int maxWidth = 0;

               for (MoveDisplayInfo info : moveInfos) {
                  String displayText = info.displayName + " " + info.currentPP + "/" + info.maxPP;
                  int width = font.method_1727(displayText);
                  if (width > maxWidth) {
                     maxWidth = width;
                  }
               }

               if (abilityName != null && !abilityName.isEmpty()) {
                  class_2561 abilityComponent = class_2561.method_43469("move.battleinfo.ability", new Object[]{abilityName});
                  int abilityWidth = font.method_27525(abilityComponent);
                  if (abilityWidth > maxWidth) {
                     maxWidth = abilityWidth;
                  }
               }

               if (speedStat != null) {
                  class_2561 speedComponent = class_2561.method_43469("move.battleinfo.speed", new Object[]{String.valueOf(speedStat)});
                  int speedWidth = font.method_27525(speedComponent);
                  if (speedWidth > maxWidth) {
                     maxWidth = speedWidth;
                  }
               }

               boxWidth = maxWidth + padding * 2 + iconDisplaySize + 8;
               boxHeight = moveInfos.size() * lineHeight + padding * 2;
               int metadataLines = 0;
               if (abilityName != null && !abilityName.isEmpty()) {
                  metadataLines++;
               }

               if (speedStat != null) {
                  metadataLines++;
               }

               if (metadataLines > 0) {
                  boxHeight += metadataLines * lineHeight + 4;
               }

               cacheSwitchTooltipData(cacheKey, moveInfos, abilityName, speedStat, boxWidth, boxHeight);
            }

            List<OpponentMatchupEntry> activeOpponents = getActiveOpponentEntries();
            List<class_2561> movePreviewLines = new ArrayList<>();
            int movePreviewHeight = 0;
            String attackerName = getPokemonSpeciesName(pokemon);
            boolean multipleTargets = activeOpponents.size() > 1;
            if (activeOpponents.isEmpty()) {
               for (int i = 0; i < moveInfos.size(); i++) {
                  movePreviewLines.add(null);
               }
            } else {
               for (MoveDisplayInfo infox : moveInfos) {
                  class_2561 previewLine = buildBestSwitchMovePreviewLine(pokemon, attackerName, infox, activeOpponents, multipleTargets);
                  movePreviewLines.add(previewLine);
                  if (previewLine != null) {
                     movePreviewHeight += previewLineHeight;
                     int previewWidth = (int)Math.ceil((double)((float)font.method_27525(previewLine) * 0.75F));
                     int totalPreviewWidth = padding + iconDisplaySize + 4 + previewWidth + padding;
                     if (totalPreviewWidth > boxWidth) {
                        boxWidth = totalPreviewWidth;
                     }
                  }
               }

               boxHeight += movePreviewHeight;
            }

            List<MatchupLineDisplay> matchupLines = Collections.emptyList();
            int matchupSectionHeight = 0;
            if (BattleExtrasConfig.isSwitchMatchupBorderEnabled()) {
               List<ElementalType> partyTypes = getTypesFromPokemon(pokemon);
               matchupLines = computeMatchupDisplayLines(activeOpponents, partyTypes);
               if (!matchupLines.isEmpty()) {
                  float matchupScale = 0.75F;
                  int scaledLineHeight = (int)Math.ceil(9.0);
                  matchupSectionHeight = matchupLines.size() * scaledLineHeight + 13;
                  int padding6 = 6;

                  for (MatchupLineDisplay line : matchupLines) {
                     int lineW = (int)((float)class_310.method_1551().field_1772.method_27525(line.component) * 0.75F) + padding6 * 2 + 4;
                     if (lineW > boxWidth) {
                        boxWidth = lineW;
                     }
                  }
               }
            }

            class_2960 typeSheet = COBBLEMON_TYPES_SHEET;
            int iconSrcSize = 36;
            int sheetWidth = 648;
            int sheetHeight = 36;
            int screenWidth = class_310.method_1551().method_22683().method_4486();
            int screenHeight = class_310.method_1551().method_22683().method_4502();
            int tileCenterX = tileX + tileWidth / 2;
            boolean isLeftColumn = tileCenterX < screenWidth / 2;
            if (BattleExtrasConfig.isClassicSwitchTooltipsEnabled()) {
               int tooltipX = isLeftColumn ? tileX - 113 - 8 : tileX + tileWidth + 8;
               int tooltipY = tileY + tileHeight / 2 - 63;
               if (tooltipX < 5) {
                  tooltipX = 5;
               }

               if (tooltipX + 113 > screenWidth - 5) {
                  tooltipX = screenWidth - 113 - 5;
               }

               if (tooltipY < 5) {
                  tooltipY = 5;
               }

               if (tooltipY + 126 > screenHeight - 5) {
                  tooltipY = screenHeight - 126 - 5;
               }

               renderClassicSwitchMovesTooltip(graphics, font, moveInfos, movePreviewLines, abilityName, speedStat, matchupLines, tooltipX, tooltipY);
               return;
            }

            int tooltipXx;
            if (isLeftColumn) {
               tooltipXx = tileX - boxWidth - 8;
            } else {
               tooltipXx = tileX + tileWidth + 8;
            }

            int totalBoxHeight = boxHeight + matchupSectionHeight;
            int tooltipYx = tileY + tileHeight / 2 - totalBoxHeight / 2;
            if (tooltipXx < 5) {
               tooltipXx = 5;
            }

            if (tooltipXx + boxWidth > screenWidth - 5) {
               tooltipXx = screenWidth - boxWidth - 5;
            }

            if (tooltipYx < 5) {
               tooltipYx = 5;
            }

            if (tooltipYx + totalBoxHeight > screenHeight - 5) {
               tooltipYx = screenHeight - totalBoxHeight - 5;
            }

            drawFancySwitchTooltipBox(graphics, tooltipXx, tooltipYx, boxWidth, totalBoxHeight);
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int yOffset = tooltipYx + padding;

            for (int moveIndex = 0; moveIndex < moveInfos.size(); moveIndex++) {
               MoveDisplayInfo infoxx = moveInfos.get(moveIndex);
               int iconX = tooltipXx + padding;
               if (infoxx.typeTextureX >= 0) {
                  graphics.method_25293(
                     typeSheet,
                     iconX,
                     yOffset,
                     iconDisplaySize,
                     iconDisplaySize,
                     (float)infoxx.typeTextureX,
                     0.0F,
                     iconSrcSize,
                     iconSrcSize,
                     sheetWidth,
                     sheetHeight
                  );
               } else {
                  graphics.method_25294(iconX, yOffset + 1, iconX + iconDisplaySize, yOffset + 1 + iconDisplaySize - 2, infoxx.fallbackTypeColor);
               }

               String displayName = infoxx.displayName;
               int nameX = tooltipXx + padding + iconDisplaySize + 4;
               graphics.method_25303(font, displayName, nameX, yOffset + 1, theme.headerText());
               int ppX = nameX + infoxx.displayNameWidth;
               graphics.method_25303(font, infoxx.ppText, ppX, yOffset + 1, infoxx.ppColor);
               yOffset += lineHeight;
               class_2561 previewLine = movePreviewLines.size() > moveIndex ? movePreviewLines.get(moveIndex) : null;
               if (previewLine != null) {
                  graphics.method_51448().method_22903();
                  graphics.method_51448().method_46416((float)nameX, (float)(yOffset - 1), 0.0F);
                  graphics.method_51448().method_22905(0.75F, 0.75F, 1.0F);
                  graphics.method_51439(font, previewLine, 0, 0, -1, false);
                  graphics.method_51448().method_22909();
                  yOffset += previewLineHeight;
               }
            }

            if (abilityName != null && !abilityName.isEmpty() || speedStat != null) {
               yOffset += 3;
               drawFancySwitchSeparator(graphics, tooltipXx + padding, yOffset, boxWidth - padding * 2);
               yOffset += 6;
               if (abilityName != null && !abilityName.isEmpty()) {
                  class_2561 abilityComponent = class_2561.method_43469("move.battleinfo.ability", new Object[]{abilityName});
                  graphics.method_27535(font, abilityComponent, tooltipXx + padding, yOffset, mixColors(theme.headerAccent(), -6360604, 0.38F));
                  yOffset += lineHeight;
               }

               if (speedStat != null) {
                  class_2561 speedComponent = class_2561.method_43469("move.battleinfo.speed", new Object[]{String.valueOf(speedStat)});
                  graphics.method_25303(font, speedComponent.getString(), tooltipXx + padding, yOffset, -728213);
                  yOffset += lineHeight;
               }
            }

            if (!matchupLines.isEmpty()) {
               float matchupScale = 0.75F;
               int scaledLineHeight = (int)Math.ceil(9.0);
               yOffset += 3;
               drawFancySwitchSeparator(graphics, tooltipXx + padding, yOffset, boxWidth - padding * 2);
               yOffset += 6;

               for (MatchupLineDisplay linex : matchupLines) {
                  graphics.method_51448().method_22903();
                  graphics.method_51448().method_46416((float)(tooltipXx + padding), (float)yOffset, 0.0F);
                  graphics.method_51448().method_22905(0.75F, 0.75F, 1.0F);
                  graphics.method_51439(font, linex.component, 0, 0, -1, false);
                  graphics.method_51448().method_22909();
                  yOffset += scaledLineHeight;
               }
            }
         } catch (Throwable var48) {
         }
      }
   }

   private static void renderClassicSwitchMovesTooltip(
      class_332 graphics,
      class_327 font,
      List<MoveDisplayInfo> moveInfos,
      List<class_2561> movePreviewLines,
      String abilityName,
      Integer speedStat,
      List<MatchupLineDisplay> matchupLines,
      int tooltipX,
      int tooltipY
   ) {
      drawClassicSwitchTooltipBox(graphics, tooltipX, tooltipY);
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      int iconDisplaySize = 10;
      int iconSrcSize = 36;
      int sheetWidth = 648;
      int sheetHeight = 36;
      int[] rowY = new int[]{6, 24, 42, 60};
      int[] rowYOffset = new int[]{-1, -3, -5, -7};
      float previewScale = 0.55F;
      float footerScale = 0.58F;
      float moveLabelScale = 0.7F;
      float ppScale = 0.5F;
      float metadataScale = 0.65F;

      for (int i = 0; i < 4; i++) {
         if (i < moveInfos.size()) {
            MoveDisplayInfo info = moveInfos.get(i);
            int rowTop = tooltipY + rowY[i] + rowYOffset[i];
            int iconX = tooltipX + 5;
            if (info.typeTextureX >= 0) {
               graphics.method_25293(COBBLEMON_TYPES_SHEET, iconX, rowTop, 10, 10, (float)info.typeTextureX, 0.0F, 36, 36, 648, 36);
            } else {
               graphics.method_25294(iconX, rowTop + 1, iconX + 10, rowTop + 10 - 1, info.fallbackTypeColor);
            }

            drawScaledShadowedString(graphics, font, info.displayName, tooltipX + 18, rowTop + 1, -986896, -1073741824, 0.7F);
            drawScaledShadowedString(graphics, font, info.ppText, tooltipX + 82, rowTop + 4, info.ppColor, -1073741824, 0.5F);
            class_2561 previewLine = movePreviewLines.size() > i ? movePreviewLines.get(i) : null;
            if (previewLine != null) {
               class_2561 classicPreviewLine = removeClassicSwitchPreviewMarker(previewLine);
               graphics.method_51448().method_22903();
               graphics.method_51448().method_46416((float)(tooltipX + 17), (float)(rowTop + 10), 0.0F);
               graphics.method_51448().method_22905(0.55F, 0.55F, 1.0F);
               graphics.method_51439(font, classicPreviewLine, 0, 0, -1, false);
               graphics.method_51448().method_22909();
            }
         }
      }

      if (abilityName != null && !abilityName.isEmpty()) {
         class_2561 abilityComponent = class_2561.method_43469("move.battleinfo.ability", new Object[]{abilityName});
         drawScaledShadowedString(graphics, font, abilityComponent.getString(), tooltipX + 6, tooltipY + 77, -2234393, -1073741824, 0.65F);
      }

      if (speedStat != null) {
         class_2561 speedComponent = class_2561.method_43469("move.battleinfo.speed", new Object[]{String.valueOf(speedStat)});
         drawScaledShadowedString(graphics, font, speedComponent.getString(), tooltipX + 35, tooltipY + 84, -728213, -1073741824, 0.65F);
      }

      if (!matchupLines.isEmpty()) {
         int footerBaseY = tooltipY + 99;
         int footerLineSpacing = 8;
         int classicFooterLines = Math.min(3, matchupLines.size());
         int[] footerLineOffsets = new int[]{-1, 0, 1};

         for (int ix = 0; ix < classicFooterLines; ix++) {
            graphics.method_51448().method_22903();
            graphics.method_51448().method_46416((float)(tooltipX + 6), (float)(footerBaseY + ix * footerLineSpacing + footerLineOffsets[ix]), 0.0F);
            graphics.method_51448().method_22905(0.58F, 0.58F, 1.0F);
            graphics.method_51439(font, matchupLines.get(ix).component, 0, 0, -1, true);
            graphics.method_51448().method_22909();
         }
      }
   }

   private static class_2561 removeClassicSwitchPreviewMarker(class_2561 previewLine) {
      if (previewLine == null) {
         return class_2561.method_43473();
      } else {
         if (previewLine instanceof class_5250 mutable) {
            List<class_2561> siblings = mutable.method_10855();
            if (!siblings.isEmpty()) {
               class_5250 result = class_2561.method_43473();

               for (class_2561 sibling : siblings) {
                  result.method_10852(sibling.method_27661());
               }

               return result;
            }
         }

         String text = previewLine.getString();
         if (text.startsWith("⚔ ")) {
            text = text.substring(2);
         }

         return class_2561.method_43470(text).method_27696(previewLine.method_10866());
      }
   }

   private static void drawFancySwitchTooltipBox(class_332 graphics, int x, int y, int width, int height) {
      Theme theme = BattleUiThemePalette.getActiveBattleLogTheme();
      int accent = withAlpha(theme.headerAccent(), 232);
      int accentDark = withAlpha(darkenColor(theme.headerAccent(), 0.6F), 224);
      int backgroundColor = withAlpha(theme.panelBg(), 232);
      int fillTop = withAlpha(mixColors(theme.headerBgLight(), theme.headerAccent(), 0.14F), 232);
      int fillBottom = withAlpha(mixColors(theme.panelBgBottom(), theme.headerAccent(), 0.08F), 240);
      int sheen = withAlpha(mixColors(theme.headerText(), -1, 0.3F), 74);
      int lowerAccent = withAlpha(mixColors(theme.borderColor(), accentDark, 0.42F), 206);
      drawRoundedGradientBox(graphics, x, y, width, height, accent, accentDark, accent, accentDark);
      graphics.method_25296(x + 1, y + 1, x + width - 1, y + height - 1, fillTop, fillBottom);
      graphics.method_25294(x + 2, y + 2, x + width - 2, y + height - 2, backgroundColor);
      graphics.method_25294(x + 3, y + 1, x + width - 3, y + 3, sheen);
      graphics.method_25294(x + 2, y + height - 3, x + width - 2, y + height - 2, lowerAccent);
   }

   private static void drawClassicSwitchTooltipBox(class_332 graphics, int x, int y) {
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.method_25290(SWITCH_TOOLTIP_TEXTURE, x, y, 0.0F, 0.0F, 113, 126, 113, 126);
   }

   private static void drawFancySwitchSeparator(class_332 graphics, int x, int y, int width) {
      Theme theme = BattleUiThemePalette.getActiveBattleLogTheme();
      int centerX = x + width / 2;
      int diamondSize = 2;
      int adjustedCenterX = centerX - 1;
      int leftEnd = adjustedCenterX - diamondSize - 2;
      int rightStart = adjustedCenterX + diamondSize + 3;
      int lineColor = withAlpha(mixColors(theme.borderHighlight(), theme.headerAccent(), 0.46F), 144);
      if (leftEnd > x) {
         graphics.method_25294(x, y, leftEnd, y + 1, lineColor);
      }

      if (x + width > rightStart) {
         graphics.method_25294(rightStart, y, x + width, y + 1, lineColor);
      }

      int diamondColor = withAlpha(theme.headerAccent(), 212);
      graphics.method_25294(adjustedCenterX, y - diamondSize, adjustedCenterX + 1, y + diamondSize + 1, diamondColor);
      graphics.method_25294(adjustedCenterX - diamondSize, y, adjustedCenterX + diamondSize + 1, y + 1, diamondColor);
   }

   private static int getPPColor(int current, int max) {
      if (max <= 0) {
         return -5592406;
      } else {
         float ratio = (float)current / (float)max;
         if (current == 0) {
            return -48060;
         } else if (ratio <= 0.25F) {
            return -22016;
         } else {
            return ratio <= 0.5F ? -256 : -12255420;
         }
      }
   }

   private static int resolveTypeTextureX(Object moveType) {
      if (moveType == null) {
         return -1;
      } else {
         try {
            Method getXMultiplier = moveType.getClass().getMethod("getTextureXMultiplier");
            Object xMultResult = getXMultiplier.invoke(moveType);
            if (xMultResult instanceof Number) {
               return ((Number)xMultResult).intValue() * 36;
            }
         } catch (Exception var3) {
         }

         return -1;
      }
   }

   private static int resolveFallbackTypeColor(Object moveType, String rawName) {
      if (moveType != null) {
         try {
            Method getHue = moveType.getClass().getMethod("getHue");
            Object hueResult = getHue.invoke(moveType);
            if (hueResult instanceof Number) {
               return ((Number)hueResult).intValue() | 0xFF000000;
            }
         } catch (Exception var4) {
         }
      }

      Integer typeColor = BattleMessageColorizer.getMoveTypeColor(rawName);
      if (typeColor == null) {
         typeColor = -1;
      }

      return typeColor | 0xFF000000;
   }

   private static String buildSwitchTooltipCacheKey(Object pokemon) {
      if (pokemon == null) {
         return null;
      } else {
         StringBuilder key = new StringBuilder();
         String uuid = getPokemonUuidString(pokemon);
         if (uuid != null && !uuid.isEmpty()) {
            key.append("uuid:").append(uuid);
         } else {
            String species = getPokemonSpeciesName(pokemon);
            if (species != null && !species.isEmpty()) {
               key.append("species:").append(species.toLowerCase());
            } else {
               key.append("obj:").append(System.identityHashCode(pokemon));
            }
         }

         key.append("|turn=")
            .append(BattleMessageSubscriber.getLastTurnNumber())
            .append("|weather=")
            .append(safeCachePart(BattleMessageSubscriber.getCurrentWeather()))
            .append("|terrain=")
            .append(safeCachePart(BattleMessageSubscriber.getCurrentTerrain()))
            .append("|wr=")
            .append(BattleMessageSubscriber.getWonderRoomTurns())
            .append("|oppHp=")
            .append(getActiveOpponentHpCacheKey());
         String statusId = getPokemonStatusId(pokemon);
         if (statusId != null && !statusId.isBlank()) {
            key.append("|status=").append(statusId);
         }

         return key.toString();
      }
   }

   private static String safeCachePart(String value) {
      return value != null && !value.isBlank() ? value.replace('|', '/').trim() : "";
   }

   private static int getActiveOpponentHpCacheKey() {
      int hpKey = 0;

      try {
         ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
         if (battle == null) {
            return 0;
         }

         ClientBattleSide opponentSide = BattleSideHelper.getOpponentSide(battle);
         if (opponentSide == null) {
            return 0;
         }

         for (ClientBattleActor actor : opponentSide.getActors()) {
            for (ActiveClientBattlePokemon activePokemon : actor.getActivePokemon()) {
               if (activePokemon != null) {
                  ClientBattlePokemon battlePokemon = activePokemon.getBattlePokemon();
                  if (battlePokemon != null) {
                     hpKey += (int)(getBattlePokemonHpRatio(battlePokemon) * 1000.0F);
                  }
               }
            }
         }
      } catch (Throwable var8) {
      }

      return hpKey;
   }

   private static float getHpRatio(Pokemon pokemon) {
      try {
         int max = pokemon.getMaxHealth();
         return max > 0 ? Math.max(0.0F, Math.min(1.0F, (float)pokemon.getCurrentHealth() / (float)max)) : 1.0F;
      } catch (Throwable var2) {
         return 1.0F;
      }
   }

   private static String getHeldItemPath(Pokemon pokemon) {
      try {
         class_1799 stack = pokemon.heldItem();
         if (stack != null && !stack.method_7960()) {
            class_2960 key = class_7923.field_41178.method_10221(stack.method_7909());
            return key != null ? key.method_12832() : "";
         } else {
            return "";
         }
      } catch (Throwable var3) {
         return "";
      }
   }

   private static String getAbilityName(Pokemon pokemon) {
      try {
         return pokemon.getAbility().getName().toLowerCase(Locale.ROOT);
      } catch (Throwable var2) {
         return "";
      }
   }

   private static String getPokemonStatusId(Object pokemon) {
      if (pokemon == null) {
         return null;
      } else {
         try {
            Method getStatus = pokemon.getClass().getMethod("getStatus");
            Object statusContainer = getStatus.invoke(pokemon);
            if (statusContainer == null) {
               return null;
            } else {
               Method getPersistentStatus = statusContainer.getClass().getMethod("getStatus");
               Object persistentStatus = getPersistentStatus.invoke(statusContainer);
               if (persistentStatus == null) {
                  return null;
               } else {
                  Method getShowdownName = persistentStatus.getClass().getMethod("getShowdownName");
                  Object showdownName = getShowdownName.invoke(persistentStatus);
                  return showdownName == null ? null : showdownName.toString();
               }
            }
         } catch (Throwable var7) {
            return null;
         }
      }
   }

   private static class_2561 buildBestSwitchMovePreviewLine(
      Object pokemon, String attackerName, MoveDisplayInfo moveInfo, List<OpponentMatchupEntry> opponents, boolean includeTargetName
   ) {
      if (pokemon instanceof Pokemon attackerPokemon
         && attackerName != null
         && !attackerName.isBlank()
         && moveInfo != null
         && moveInfo.moveTemplate != null
         && opponents != null
         && !opponents.isEmpty()) {
         DamagePreview bestPreview = null;
         String bestTargetName = null;
         float bestEffectiveness = 1.0F;
         boolean hasAttackingMove = moveInfo.moveTemplate.getPower() > 0.0;

         for (OpponentMatchupEntry opponent : opponents) {
            DamagePreview preview = calculateSwitchDamagePreview(attackerPokemon, attackerName, moveInfo.moveTemplate, opponent);
            if (preview != null) {
               if (hasAttackingMove) {
                  float effectiveness = 1.0F;
                  if (opponent.types != null && !opponent.types.isEmpty()) {
                     String moveType = moveInfo.moveTemplate.getElementalType().getName().toLowerCase();

                     for (ElementalType defenderType : opponent.types) {
                        if (defenderType != null) {
                           effectiveness *= TypeChart.getEffectivenessAgainstTypes(moveType, defenderType.getName().toLowerCase(), null);
                        }
                     }
                  }

                  if (effectiveness > bestEffectiveness) {
                     bestEffectiveness = effectiveness;
                  }
               }

               if (bestPreview == null
                  || preview.maxPercent() > bestPreview.maxPercent()
                  || preview.maxPercent() == bestPreview.maxPercent() && preview.minPercent() > bestPreview.minPercent()) {
                  bestPreview = preview;
                  bestTargetName = opponent.displayName;
               }
            }
         }

         if (!BattleExtrasConfig.showSwitchTooltipDamageRange()) {
            if (hasAttackingMove) {
               return buildEffectivenessComponent(bestEffectiveness);
            }

            return null;
         }

         return MoveDamagePreviewCalculator.buildSwitchSummaryComponent(bestTargetName, bestPreview, includeTargetName);
      }

      return null;
   }

   private static class_5250 buildEffectivenessComponent(float effectiveness) {
      String multiplierText = formatEffectivenessMultiplier(effectiveness);
      String translationKey;
      class_124 formatting;
      if (effectiveness == 0.0F) {
         translationKey = "move.battleinfo.immune";
         formatting = class_124.field_1080;
      } else if (effectiveness <= 0.25F) {
         translationKey = "move.battleinfo.mostly_ineffective";
         formatting = class_124.field_1063;
      } else if (effectiveness < 1.0F) {
         translationKey = "move.battleinfo.not_effective";
         formatting = class_124.field_1061;
      } else if (effectiveness >= 4.0F) {
         translationKey = "move.battleinfo.extremely_effective";
         formatting = class_124.field_1060;
      } else if (effectiveness > 1.0F) {
         translationKey = "move.battleinfo.super_effective";
         formatting = class_124.field_1060;
      } else {
         translationKey = "move.battleinfo.normal_effective";
         formatting = class_124.field_1068;
      }

      return class_2561.method_43469(translationKey, new Object[]{multiplierText}).method_27692(formatting);
   }

   private static String formatEffectivenessMultiplier(float effectiveness) {
      if (effectiveness == 0.0F) {
         return "×0";
      } else if (Math.abs(effectiveness - 1.0F) < 1.0E-4F) {
         return "×1";
      } else if (Math.abs(effectiveness - (float)Math.round(effectiveness)) < 1.0E-4F) {
         return "×" + Math.round(effectiveness);
      } else {
         String value = String.format(Locale.ROOT, "%.2f", effectiveness);

         while (value.endsWith("0")) {
            value = value.substring(0, value.length() - 1);
         }

         if (value.endsWith(".")) {
            value = value.substring(0, value.length() - 1);
         }

         return "×" + value;
      }
   }

   private static DamagePreview calculateSwitchDamagePreview(Pokemon attackerPokemon, String attackerName, MoveTemplate move, OpponentMatchupEntry opponent) {
      if (attackerPokemon != null && move != null && opponent != null) {
         if (move.getPower() <= 0.0) {
            return null;
         } else {
            float typeEffectiveness = 1.0F;
            if (opponent.types != null && !opponent.types.isEmpty()) {
               String moveType = move.getElementalType().getName().toLowerCase();

               for (ElementalType defenderType : opponent.types) {
                  if (defenderType != null) {
                     typeEffectiveness *= TypeChart.getEffectivenessAgainstTypes(moveType, defenderType.getName().toLowerCase(), null);
                  }
               }

               if (typeEffectiveness <= 0.0F) {
                  return null;
               } else {
                  int baseHp = getBaseStatValue(opponent.matchedForm, opponent.battlePokemon.getSpecies(), Stats.HP);
                  int baseDef = getBaseStatValue(opponent.matchedForm, opponent.battlePokemon.getSpecies(), Stats.DEFENCE);
                  int baseSpecialDef = getBaseStatValue(opponent.matchedForm, opponent.battlePokemon.getSpecies(), Stats.SPECIAL_DEFENCE);
                  return baseHp > 0 && baseDef > 0 && baseSpecialDef > 0
                     ? MoveDamagePreviewCalculator.calculatePreview(
                        new AttackContext(
                           Math.max(1, attackerPokemon.getLevel()),
                           attackerPokemon.getStat(Stats.ATTACK),
                           attackerPokemon.getStat(Stats.SPECIAL_ATTACK),
                           resolveTrackedStatStage(
                              "player", "attack", attackerName, getPokemonDisplayName(attackerPokemon), attackerPokemon.getSpecies().getName()
                           ),
                           resolveTrackedStatStage(
                              "player", "special-attack", attackerName, getPokemonDisplayName(attackerPokemon), attackerPokemon.getSpecies().getName()
                           ),
                           getPokemonStatusId(attackerPokemon),
                           isPokemonGrounded("player", attackerName),
                           getHeldItemPath(attackerPokemon),
                           getAbilityName(attackerPokemon),
                           getHpRatio(attackerPokemon),
                           attackerPokemon.getStat(Stats.DEFENCE),
                           resolveTrackedStatStage(
                              "player", "defense", attackerName, getPokemonDisplayName(attackerPokemon), attackerPokemon.getSpecies().getName()
                           )
                        ),
                        new DefenceContext(
                           Math.max(1, opponent.battlePokemon.getLevel()),
                           baseHp,
                           baseDef,
                           baseSpecialDef,
                           opponent.battlePokemon.getMaxHp(),
                           opponent.battlePokemon.isHpFlat(),
                           resolveTrackedStatStage(
                              "opponent", "defense", opponent.speciesName, opponent.displayName, getBattlePokemonSpeciesName(opponent.battlePokemon)
                           ),
                           resolveTrackedStatStage(
                              "opponent", "special-defense", opponent.speciesName, opponent.displayName, getBattlePokemonSpeciesName(opponent.battlePokemon)
                           ),
                           getBattlePokemonHpRatio(opponent.battlePokemon),
                           isPokemonGrounded("opponent", opponent.speciesName),
                           isRockType(opponent.types)
                        ),
                        move,
                        typeEffectiveness,
                        calculateSwitchStabMultiplier(move, attackerPokemon, attackerName)
                     )
                     : null;
               }
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static float calculateSwitchStabMultiplier(MoveTemplate move, Object pokemon, String pokemonName) {
      if (move != null && pokemon != null && pokemonName != null) {
         String moveType = move.getElementalType().getName().toLowerCase();
         String teraType = BattleMessageSubscriber.getTeraType("player", pokemonName);
         String transformedType = BattleMessageSubscriber.getTransformedType("player", pokemonName);
         if (transformedType != null && !transformedType.isBlank()) {
            return resolveTeraAwareStabMultiplier(transformedType.equalsIgnoreCase(moveType), teraType, moveType);
         } else {
            List<String> megaTypes = BattleMessageSubscriber.getMegaTypes("player", pokemonName);
            if (megaTypes != null && !megaTypes.isEmpty()) {
               boolean currentTypeMatch = false;

               for (String megaType : megaTypes) {
                  if (megaType != null && megaType.equalsIgnoreCase(moveType)) {
                     currentTypeMatch = true;
                     break;
                  }
               }

               return resolveTeraAwareStabMultiplier(currentTypeMatch, teraType, moveType);
            } else {
               boolean currentTypeMatch = false;

               for (ElementalType type : getBaseTypesFromPokemon(pokemon)) {
                  if (type != null && type.getName().equalsIgnoreCase(moveType)) {
                     currentTypeMatch = true;
                     break;
                  }
               }

               String addedType = BattleMessageSubscriber.getAddedType("player", pokemonName);
               if (addedType != null && addedType.equalsIgnoreCase(moveType)) {
                  currentTypeMatch = true;
               }

               if (teraType == null || teraType.isBlank()) {
                  for (ElementalType displayType : getTypesFromPokemon(pokemon)) {
                     if (displayType != null && displayType.getName().equalsIgnoreCase(moveType)) {
                        return 1.5F;
                     }
                  }
               }

               return resolveTeraAwareStabMultiplier(currentTypeMatch, teraType, moveType);
            }
         }
      } else {
         return 1.0F;
      }
   }

   private static float resolveTeraAwareStabMultiplier(boolean currentTypeMatch, String teraType, String moveType) {
      boolean teraTypeMatch = teraType != null && !teraType.isBlank() && teraType.equalsIgnoreCase(moveType);
      if (currentTypeMatch && teraTypeMatch) {
         return 2.0F;
      } else {
         return !currentTypeMatch && !teraTypeMatch ? 1.0F : 1.5F;
      }
   }

   private static List<ElementalType> getBaseTypesFromPokemon(Object pokemon) {
      List<ElementalType> result = new ArrayList<>();
      if (pokemon == null) {
         return result;
      } else {
         try {
            Method getPrimaryType = pokemon.getClass().getMethod("getPrimaryType");
            if (getPrimaryType.invoke(pokemon) instanceof ElementalType type) {
               result.add(type);
            }
         } catch (Throwable var6) {
         }

         try {
            Method getSecondaryType = pokemon.getClass().getMethod("getSecondaryType");
            if (getSecondaryType.invoke(pokemon) instanceof ElementalType type) {
               result.add(type);
            }
         } catch (Throwable var5) {
         }

         return result;
      }
   }

   private static int getBaseStatValue(Object formObj, Object speciesObj, Stat stat) {
      if (formObj != null) {
         try {
            if (formObj.getClass().getMethod("getBaseStats").invoke(formObj) instanceof Map<?, ?> map
               && map.get(stat) instanceof Number number
               && number.intValue() > 0) {
               return number.intValue();
            }
         } catch (Throwable var8) {
         }
      }

      if (speciesObj != null) {
         try {
            if (speciesObj.getClass().getMethod("getBaseStats").invoke(speciesObj) instanceof Map<?, ?> map && map.get(stat) instanceof Number number) {
               return number.intValue();
            }
         } catch (Throwable var7) {
         }
      }

      return 0;
   }

   private static float getBattlePokemonHpRatio(ClientBattlePokemon battlePokemon) {
      if (battlePokemon == null) {
         return 1.0F;
      } else {
         try {
            return battlePokemon.isHpFlat() && battlePokemon.getMaxHp() > 0.0F
               ? battlePokemon.getHpValue() / battlePokemon.getMaxHp()
               : battlePokemon.getHpValue();
         } catch (Throwable var2) {
            return 1.0F;
         }
      }
   }

   private static boolean isPokemonGrounded(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (BattleMessageSubscriber.getGravityTurns() > 0) {
         return true;
      } else {
         return BattleMessageSubscriber.getUngroundedTurns(side, pokemonName) > 0
            ? false
            : BattleMessageSubscriber.isGrounded(pokemonName) || BattleMessageSubscriber.isSmackDownGrounded(side, pokemonName);
      }
   }

   private static boolean isRockType(List<ElementalType> types) {
      if (types == null) {
         return false;
      } else {
         for (ElementalType type : types) {
            if (type != null && "rock".equalsIgnoreCase(type.getName())) {
               return true;
            }
         }

         return false;
      }
   }

   private static int resolveTrackedStatStage(String side, String stat, String... candidateNames) {
      LinkedHashSet<String> candidates = new LinkedHashSet<>();
      if (candidateNames != null) {
         for (String candidateName : candidateNames) {
            if (candidateName != null && !candidateName.isBlank()) {
               candidates.add(candidateName);
            }
         }
      }

      for (String candidate : candidates) {
         Map<String, Integer> stages = BattleMessageSubscriber.getStatStagesForPokemon(side, candidate);
         if (!stages.isEmpty()) {
            return BattleMessageSubscriber.getStatStage(side, candidate, stat);
         }
      }

      return 0;
   }

   private static String getPokemonDisplayName(Pokemon pokemon) {
      if (pokemon == null) {
         return null;
      } else {
         try {
            return PokemonNameHelper.getDisplayNameString(pokemon);
         } catch (Throwable var2) {
            return null;
         }
      }
   }

   private static String getBattlePokemonSpeciesName(ClientBattlePokemon battlePokemon) {
      if (battlePokemon == null) {
         return null;
      } else {
         try {
            return battlePokemon.getSpecies() != null ? battlePokemon.getSpecies().getName() : null;
         } catch (Throwable var2) {
            return null;
         }
      }
   }

   private static SwitchTooltipCacheData getSwitchTooltipCache(String key) {
      if (key == null) {
         return null;
      } else {
         SwitchTooltipCacheData cached = switchTooltipCache.get(key);
         long now = System.currentTimeMillis();
         if (cached != null && now <= cached.expiresAt) {
            return cached;
         } else {
            if (cached != null) {
               switchTooltipCache.remove(key);
            }

            return null;
         }
      }
   }

   private static void cacheSwitchTooltipData(String key, List<MoveDisplayInfo> moveInfos, String abilityName, Integer speedStat, int boxWidth, int boxHeight) {
      if (key != null && moveInfos != null) {
         switchTooltipCache.put(
            key, new SwitchTooltipCacheData(key, System.currentTimeMillis() + 750L, new ArrayList<>(moveInfos), abilityName, speedStat, boxWidth, boxHeight)
         );
      }
   }

   private static boolean isMegaShowdownLoadedCached() {
      if (cachedMegaShowdownLoaded == null) {
         cachedMegaShowdownLoaded = Services.PLATFORM.isModLoaded("mega_showdown");
      }

      return cachedMegaShowdownLoaded;
   }

   private static class_2960 getTeraIconResource(String teraTypeName) {
      if (teraTypeName != null && !teraTypeName.isEmpty()) {
         String key = teraTypeName.toLowerCase();
         return teraIconResourceCache.computeIfAbsent(key, type -> class_2960.method_60654("mega_showdown:textures/gui/summary/tera_types/" + type + ".png"));
      } else {
         return null;
      }
   }

   private static CustomBattleController.SwitchTypeIconCacheData getSwitchTypeIconCacheData(Object pokemon, String pokemonName) {
      if (pokemon == null) {
         return null;
      } else {
         long now = System.currentTimeMillis();
         pruneSwitchTypeIconCache(now);
         int key = System.identityHashCode(pokemon);
         CustomBattleController.SwitchTypeIconCacheData cached = switchTypeIconCache.get(key);
         if (cached != null && now <= cached.expiresAt && Objects.equals(cached.pokemonName, pokemonName)) {
            return cached;
         } else {
            boolean terastallized = pokemonName != null && BattleMessageSubscriber.isTerastallized(pokemonName);
            String teraType = pokemonName != null ? BattleMessageSubscriber.getTeraType(pokemonName) : null;
            boolean megaEvolved = pokemonName != null && BattleMessageSubscriber.isMegaEvolved(pokemonName);
            List<ElementalType> types = getTypesDirectlyFromPokemon(pokemon, pokemonName);
            CustomBattleController.SwitchTypeIconCacheData data = new CustomBattleController.SwitchTypeIconCacheData(
               now + 1500L, pokemonName, terastallized, teraType, megaEvolved, types != null ? new ArrayList<>(types) : null
            );
            switchTypeIconCache.put(key, data);
            return data;
         }
      }
   }

   private static void pruneSwitchTypeIconCache(long now) {
      if (switchTypeIconCache.size() > 24) {
         switchTypeIconCache.entrySet().removeIf(entry -> now > entry.getValue().expiresAt);
         if (switchTypeIconCache.size() > 64) {
            switchTypeIconCache.clear();
         }
      }
   }

   private static List<MoveDisplayInfo> getMovesWithPPFromPokemon(Object pokemon) {
      List<MoveDisplayInfo> result = new ArrayList<>();

      try {
         String pokemonName = getPokemonSpeciesName(pokemon);
         Object moveSet = null;

         for (String methodName : new String[]{"getMoveSet", "getMoves", "moveSet", "moves", "getMoveset"}) {
            try {
               Method method = pokemon.getClass().getMethod(methodName);
               moveSet = method.invoke(pokemon);
               if (moveSet != null) {
                  break;
               }
            } catch (NoSuchMethodException var34) {
            }
         }

         if (moveSet == null) {
            return result;
         }

         Iterable<?> moveIterable = null;

         try {
            Method getMovesMethod = moveSet.getClass().getMethod("getMoves");
            Object movesObj = getMovesMethod.invoke(moveSet);
            if (movesObj instanceof Iterable) {
               moveIterable = (Iterable<?>)movesObj;
            }
         } catch (NoSuchMethodException var32) {
         }

         if (moveIterable == null && moveSet instanceof Iterable) {
            moveIterable = (Iterable<?>)moveSet;
         }

         if (moveIterable != null) {
            for (Object move : moveIterable) {
               if (move != null) {
                  String moveName = null;
                  int currentPP = 0;
                  int maxPP = 0;

                  for (String methodName : new String[]{"getName", "getDisplayName", "name"}) {
                     try {
                        Method method = move.getClass().getMethod(methodName);
                        Object nameResult = method.invoke(move);
                        if (nameResult != null) {
                           moveName = nameResult.toString();
                           break;
                        }
                     } catch (NoSuchMethodException var33) {
                     }
                  }

                  if (moveName == null) {
                     try {
                        Method getTemplate = move.getClass().getMethod("getTemplate");
                        Object template = getTemplate.invoke(move);
                        if (template != null) {
                           Method nameMethod = template.getClass().getMethod("getName");
                           Object nameResult = nameMethod.invoke(template);
                           if (nameResult != null) {
                              moveName = nameResult.toString();
                           }
                        }
                     } catch (NoSuchMethodException var31) {
                     }
                  }

                  try {
                     Method getPP = move.getClass().getMethod("getCurrentPp");
                     Object ppResult = getPP.invoke(move);
                     if (ppResult instanceof Number) {
                        currentPP = ((Number)ppResult).intValue();
                     }
                  } catch (NoSuchMethodException var30) {
                     try {
                        Method getPPx = move.getClass().getMethod("getPp");
                        Object ppResultx = getPPx.invoke(move);
                        if (ppResultx instanceof Number) {
                           currentPP = ((Number)ppResultx).intValue();
                        }
                     } catch (NoSuchMethodException var29) {
                     }
                  }

                  try {
                     Method getMaxPP = move.getClass().getMethod("getMaxPp");
                     Object maxPPResult = getMaxPP.invoke(move);
                     if (maxPPResult instanceof Number) {
                        maxPP = ((Number)maxPPResult).intValue();
                     }
                  } catch (NoSuchMethodException var28) {
                     try {
                        Method getTemplate = move.getClass().getMethod("getTemplate");
                        Object template = getTemplate.invoke(move);
                        if (template != null) {
                           Method ppMethod = template.getClass().getMethod("getPp");
                           Object ppResult = ppMethod.invoke(template);
                           if (ppResult instanceof Number) {
                              maxPP = ((Number)ppResult).intValue();
                           }
                        }
                     } catch (NoSuchMethodException var27) {
                     }
                  }

                  if (maxPP == 0 && currentPP > 0) {
                     maxPP = currentPP;
                  }

                  if (moveName != null) {
                     int[] battleTrackedPP = BattleMessageSubscriber.getMovePP(pokemonName, moveName);
                     if (battleTrackedPP != null) {
                        currentPP = battleTrackedPP[0];
                        maxPP = battleTrackedPP[1];
                        CobblemonBattleInfoClient.debug(
                           "[CustomController] Using BATTLE-TRACKED PP for {}'s {} -> {}/{}", new Object[]{pokemonName, moveName, currentPP, maxPP}
                        );
                     } else {
                        BattleMessageSubscriber.initializeMovePP(pokemonName, moveName, currentPP, maxPP);
                        CobblemonBattleInfoClient.debug(
                           "[CustomController] Initialized PP tracking for {}'s {} -> {}/{}", new Object[]{pokemonName, moveName, currentPP, maxPP}
                        );
                     }
                  }

                  Object moveType = null;
                  MoveTemplate moveTemplate = null;

                  try {
                     Method getTemplate = move.getClass().getMethod("getTemplate");
                     Object template = getTemplate.invoke(move);
                     if (template != null) {
                        if (template instanceof MoveTemplate typedTemplate) {
                           moveTemplate = typedTemplate;
                        }

                        Method getElementalType = template.getClass().getMethod("getElementalType");
                        moveType = getElementalType.invoke(template);
                     }
                  } catch (NoSuchMethodException var26) {
                     try {
                        Method getElementalType = move.getClass().getMethod("getElementalType");
                        moveType = getElementalType.invoke(move);
                     } catch (NoSuchMethodException var25) {
                     }
                  }

                  if (moveType == null) {
                     try {
                        Method getType = move.getClass().getMethod("getType");
                        moveType = getType.invoke(move);
                     } catch (NoSuchMethodException var24) {
                     }
                  }

                  if (moveName != null && !moveName.isEmpty()) {
                     String displayName = moveName;
                     double effectChance = 0.0;
                     String effectDescription = null;

                     try {
                        Method getTemplate = move.getClass().getMethod("getTemplate");
                        Object template = getTemplate.invoke(move);
                        if (template != null) {
                           try {
                              Method getDisplayName = template.getClass().getMethod("getDisplayName");
                              Object displayNameComponent = getDisplayName.invoke(template);
                              if (displayNameComponent != null) {
                                 Method getString = displayNameComponent.getClass().getMethod("getString");
                                 displayName = getString.invoke(displayNameComponent).toString();
                              }
                           } catch (Exception var22) {
                              displayName = formatMoveName(moveName);
                           }

                           try {
                              Method getEffectChance = template.getClass().getMethod("getEffectChance");
                              Object chanceResult = getEffectChance.invoke(template);
                              if (chanceResult instanceof Number) {
                                 effectChance = ((Number)chanceResult).doubleValue();
                              }
                           } catch (Exception var21) {
                           }

                           if (effectChance > 0.0) {
                              effectDescription = "";
                           }
                        }
                     } catch (Exception var23) {
                        displayName = formatMoveName(moveName);
                     }

                     result.add(new MoveDisplayInfo(moveName, displayName, moveTemplate, currentPP, maxPP, moveType, effectChance, effectDescription));
                  }
               }
            }
         }
      } catch (Throwable var35) {
      }

      return result;
   }

   private static String getPokemonSpeciesName(Object pokemon) {
      if (pokemon == null) {
         return null;
      } else {
         try {
            try {
               Method getSpecies = pokemon.getClass().getMethod("getSpecies");
               Object species = getSpecies.invoke(pokemon);
               if (species != null) {
                  Method getName = species.getClass().getMethod("getName");
                  Object name = getName.invoke(species);
                  if (name != null) {
                     return name.toString();
                  }
               }
            } catch (NoSuchMethodException var7) {
            }

            try {
               Method getDisplayName = pokemon.getClass().getMethod("getDisplayName");
               Object name = getDisplayName.invoke(pokemon);
               if (name != null) {
                  return name.toString();
               }
            } catch (NoSuchMethodException var6) {
            }

            try {
               Method getName = pokemon.getClass().getMethod("getName");
               Object name = getName.invoke(pokemon);
               if (name != null) {
                  return name.toString();
               }
            } catch (NoSuchMethodException var5) {
            }
         } catch (Throwable var8) {
         }

         return null;
      }
   }

   private static String getPokemonUuidString(Object pokemon) {
      if (pokemon == null) {
         return null;
      } else {
         for (String methodName : new String[]{"getUuid", "getUUID", "uuid", "getId", "getIdAsString"}) {
            try {
               Method getUuidMethod = pokemon.getClass().getMethod(methodName);
               Object uuidObj = getUuidMethod.invoke(pokemon);
               if (uuidObj != null) {
                  String uuidStr = uuidObj.toString();
                  if (isValidUuidString(uuidStr)) {
                     return uuidStr;
                  }
               }
            } catch (Throwable var10) {
            }
         }

         try {
            for (Class<?> clazz = pokemon.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
               for (Field field : clazz.getDeclaredFields()) {
                  String fieldName = field.getName().toLowerCase();
                  if (fieldName.contains("uuid") || fieldName.equals("id")) {
                     field.setAccessible(true);
                     Object uuidObj = field.get(pokemon);
                     if (uuidObj != null) {
                        String uuidStr = uuidObj.toString();
                        if (isValidUuidString(uuidStr)) {
                           return uuidStr;
                        }
                     }
                  }
               }
            }
         } catch (Throwable var11) {
         }

         for (String methodName : new String[]{"getPokemon", "pokemon", "getOriginalPokemon", "getEffectedPokemon"}) {
            try {
               Method method = pokemon.getClass().getMethod(methodName);
               Object inner = method.invoke(pokemon);
               if (inner != null && inner != pokemon) {
                  String innerUuid = getPokemonUuidString(inner);
                  if (innerUuid != null && !innerUuid.isEmpty()) {
                     return innerUuid;
                  }
               }
            } catch (Throwable var9) {
            }
         }

         return null;
      }
   }

   private static boolean isValidUuidString(String value) {
      if (value != null && !value.isEmpty()) {
         try {
            UUID.fromString(value);
            return true;
         } catch (IllegalArgumentException var2) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static Integer getActualSwitchTooltipSpeed(Object pokemon) {
      return BattlePokemonSpeedResolver.resolveDisplayedSpeed(pokemon);
   }

   private static void refreshPartyPPData() {
      cachedPartyPP.clear();

      try {
         Class<?> cobblemonClass = Class.forName("com.cobblemon.mod.common.Cobblemon");
         Method getStorage = cobblemonClass.getMethod("getStorage");
         Object storage = getStorage.invoke(null);
         if (storage == null) {
            return;
         }

         class_310 mc = class_310.method_1551();
         if (mc.field_1724 == null) {
            return;
         }

         Method getParty = storage.getClass().getMethod("getParty", class_1657.class);
         Object party = getParty.invoke(storage, mc.field_1724);
         if (party == null) {
            return;
         }

         Method iterator = party.getClass().getMethod("iterator");
         if (iterator.invoke(party) instanceof Iterator<?> it) {
            while (it.hasNext()) {
               Object pokemon = it.next();
               if (pokemon != null) {
                  String pokemonName = getPokemonSpeciesName(pokemon);
                  if (pokemonName != null) {
                     Map<String, int[]> movePPMap = new ConcurrentHashMap<>();
                     Object moveSet = null;

                     try {
                        Method getMoveSet = pokemon.getClass().getMethod("getMoveSet");
                        moveSet = getMoveSet.invoke(pokemon);
                     } catch (NoSuchMethodException var30) {
                     }

                     if (moveSet != null) {
                        Iterable<?> moveIterable = null;

                        try {
                           Method getMoves = moveSet.getClass().getMethod("getMoves");
                           Object movesObj = getMoves.invoke(moveSet);
                           if (movesObj instanceof Iterable) {
                              moveIterable = (Iterable<?>)movesObj;
                           }
                        } catch (NoSuchMethodException var29) {
                        }

                        if (moveIterable == null && moveSet instanceof Iterable) {
                           moveIterable = (Iterable<?>)moveSet;
                        }

                        if (moveIterable != null) {
                           for (Object move : moveIterable) {
                              if (move != null) {
                                 String moveName = null;
                                 int currentPP = 0;
                                 int maxPP = 0;

                                 try {
                                    Method getName = move.getClass().getMethod("getName");
                                    Object nameResult = getName.invoke(move);
                                    if (nameResult != null) {
                                       moveName = nameResult.toString();
                                    }
                                 } catch (NoSuchMethodException var28) {
                                    try {
                                       Method getTemplate = move.getClass().getMethod("getTemplate");
                                       Object template = getTemplate.invoke(move);
                                       if (template != null) {
                                          Method templateName = template.getClass().getMethod("getName");
                                          Object nameResultx = templateName.invoke(template);
                                          if (nameResultx != null) {
                                             moveName = nameResultx.toString();
                                          }
                                       }
                                    } catch (NoSuchMethodException var27) {
                                    }
                                 }

                                 try {
                                    Method getCurrentPp = move.getClass().getMethod("getCurrentPp");
                                    Object ppResult = getCurrentPp.invoke(move);
                                    if (ppResult instanceof Number) {
                                       currentPP = ((Number)ppResult).intValue();
                                    }
                                 } catch (NoSuchMethodException var26) {
                                    try {
                                       Method getPp = move.getClass().getMethod("getPp");
                                       Object ppResultx = getPp.invoke(move);
                                       if (ppResultx instanceof Number) {
                                          currentPP = ((Number)ppResultx).intValue();
                                       }
                                    } catch (NoSuchMethodException var25) {
                                    }
                                 }

                                 try {
                                    Method getMaxPp = move.getClass().getMethod("getMaxPp");
                                    Object maxResult = getMaxPp.invoke(move);
                                    if (maxResult instanceof Number) {
                                       maxPP = ((Number)maxResult).intValue();
                                    }
                                 } catch (NoSuchMethodException var24) {
                                 }

                                 if (moveName != null && !moveName.isEmpty()) {
                                    movePPMap.put(moveName.toLowerCase(), new int[]{currentPP, maxPP});
                                 }
                              }
                           }
                        }

                        if (!movePPMap.isEmpty()) {
                           cachedPartyPP.put(pokemonName.toLowerCase(), movePPMap);
                           CobblemonBattleInfoClient.debug("[CustomController] Cached PP for {}: {}", new Object[]{pokemonName, movePPMap.size() + " moves"});
                        }
                     }
                  }
               }
            }
         }
      } catch (Throwable var31) {
         CobblemonBattleInfoClient.debug("[CustomController] Error refreshing party PP: {}", new Object[]{var31.getMessage()});
      }
   }

   private static int[] getPartyPokemonMovePP(String pokemonName, String moveName) {
      if (pokemonName != null && moveName != null) {
         String lowerPokemon = pokemonName.toLowerCase();
         String lowerMove = moveName.toLowerCase();
         Map<String, int[]> moves = cachedPartyPP.get(lowerPokemon);
         if (moves != null) {
            return moves.get(lowerMove);
         } else {
            for (Map<String, int[]> moveMap : cachedPartyPP.values()) {
               if (moveMap.containsKey(lowerMove)) {
                  return moveMap.get(lowerMove);
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   public static void clearTeraTypeCache() {
      cachedTeraTypes.clear();
      CobblemonBattleInfoClient.debug("[CustomController] Tera type cache cleared", new Object[0]);
   }

   public static void recordSwitchSelectionFromMouseClick(class_437 gui, double mouseX, double mouseY, int button) {
      if (button == 0 && gui != null) {
         Object[] tiles = cachedTiles;
         if (tiles == null || tiles.length == 0 || cachedScreen != gui || !"switch".equals(currentSelectionType)) {
            tiles = findBattleOptionTiles(gui);
         }

         if ("switch".equals(currentSelectionType) && tiles != null) {
            for (Object tile : tiles) {
               if (tile != null) {
                  String tileClassName = tile.getClass().getName();
                  if (tileClassName.contains("SwitchTile")) {
                     int tileX = getTileX(tile);
                     int tileY = getTileY(tile);
                     int tileWidth = getTileWidth(tile);
                     int tileHeight = getTileHeight(tile);
                     if (mouseX >= (double)tileX && mouseX < (double)(tileX + tileWidth) && mouseY >= (double)tileY && mouseY < (double)(tileY + tileHeight)) {
                        rememberSwitchSelectionFromTile(tile);
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   private static String getActiveTeraType() {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException: Cannot invoke "org.jetbrains.java.decompiler.struct.gen.VarType.isGeneric()" because "newRet" is null
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.getInferredExprType(InvocationExprent.java:634)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.getCastedExprent(ExprProcessor.java:966)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent.toJava(AssignmentExprent.java:154)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.listToJava(ExprProcessor.java:895)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement.toJava(BasicBlockStatement.java:90)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:203)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:241)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:241)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:241)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:241)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.CatchStatement.toJava(CatchStatement.java:166)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:241)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:241)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:261)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:261)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:261)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:261)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.CatchStatement.toJava(CatchStatement.java:166)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement.toJava(RootStatement.java:36)
      //   at org.jetbrains.java.decompiler.main.ClassWriter.writeMethod(ClassWriter.java:1283)
      //
      // Bytecode:
      // 000: getstatic com/cobblemon/mod/common/client/CobblemonClient.INSTANCE Lcom/cobblemon/mod/common/client/CobblemonClient;
      // 003: invokevirtual com/cobblemon/mod/common/client/CobblemonClient.getBattle ()Lcom/cobblemon/mod/common/client/battle/ClientBattle;
      // 006: astore 0
      // 007: aload 0
      // 008: ifnonnull 00d
      // 00b: aconst_null
      // 00c: areturn
      // 00d: aload 0
      // 00e: invokevirtual com/cobblemon/mod/common/client/battle/ClientBattle.getFirstUnansweredRequest ()Lcom/cobblemon/mod/common/client/battle/SingleActionRequest;
      // 011: astore 1
      // 012: aload 1
      // 013: ifnonnull 018
      // 016: aconst_null
      // 017: areturn
      // 018: aload 1
      // 019: invokevirtual com/cobblemon/mod/common/client/battle/SingleActionRequest.getActivePokemon ()Lcom/cobblemon/mod/common/client/battle/ActiveClientBattlePokemon;
      // 01c: astore 2
      // 01d: aload 2
      // 01e: ifnonnull 023
      // 021: aconst_null
      // 022: areturn
      // 023: aload 2
      // 024: invokevirtual com/cobblemon/mod/common/client/battle/ActiveClientBattlePokemon.getBattlePokemon ()Lcom/cobblemon/mod/common/client/battle/ClientBattlePokemon;
      // 027: astore 3
      // 028: aload 3
      // 029: ifnonnull 02e
      // 02c: aconst_null
      // 02d: areturn
      // 02e: aconst_null
      // 02f: astore 4
      // 031: aload 3
      // 032: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 035: ldc_w "getUuid"
      // 038: bipush 0
      // 039: anewarray 471
      // 03c: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 03f: astore 5
      // 041: aload 5
      // 043: aload 3
      // 044: bipush 0
      // 045: anewarray 4
      // 048: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 04b: astore 6
      // 04d: aload 6
      // 04f: instanceof java/util/UUID
      // 052: ifeq 05c
      // 055: aload 6
      // 057: checkcast java/util/UUID
      // 05a: astore 4
      // 05c: goto 061
      // 05f: astore 5
      // 061: aload 4
      // 063: ifnull 08f
      // 066: aload 4
      // 068: invokevirtual java/util/UUID.toString ()Ljava/lang/String;
      // 06b: invokestatic name/modid/client/net/TeraTypeClientData.getTeraType (Ljava/lang/String;)Ljava/lang/String;
      // 06e: astore 5
      // 070: aload 5
      // 072: ifnull 08f
      // 075: aload 5
      // 077: invokevirtual java/lang/String.isEmpty ()Z
      // 07a: ifne 08f
      // 07d: ldc_w "[CustomController] Got tera type from synced data (PRIORITY 1): {}"
      // 080: bipush 1
      // 081: anewarray 4
      // 084: dup
      // 085: bipush 0
      // 086: aload 5
      // 088: aastore
      // 089: invokestatic name/modid/CobblemonBattleInfoClient.debug (Ljava/lang/String;[Ljava/lang/Object;)V
      // 08c: aload 5
      // 08e: areturn
      // 08f: aconst_null
      // 090: astore 5
      // 092: aload 3
      // 093: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 096: ldc_w "getSpecies"
      // 099: bipush 0
      // 09a: anewarray 471
      // 09d: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 0a0: astore 6
      // 0a2: aload 6
      // 0a4: aload 3
      // 0a5: bipush 0
      // 0a6: anewarray 4
      // 0a9: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 0ac: astore 7
      // 0ae: aload 7
      // 0b0: ifnull 0d4
      // 0b3: aload 7
      // 0b5: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 0b8: ldc_w "getName"
      // 0bb: bipush 0
      // 0bc: anewarray 471
      // 0bf: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 0c2: astore 8
      // 0c4: aload 8
      // 0c6: aload 7
      // 0c8: bipush 0
      // 0c9: anewarray 4
      // 0cc: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 0cf: checkcast java/lang/String
      // 0d2: astore 5
      // 0d4: goto 0d9
      // 0d7: astore 6
      // 0d9: aload 5
      // 0db: ifnull 130
      // 0de: ldc_w "player"
      // 0e1: aload 5
      // 0e3: invokestatic name/modid/client/BattleMessageSubscriber.getTeraType (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
      // 0e6: astore 6
      // 0e8: aload 6
      // 0ea: ifnull 0f5
      // 0ed: aload 6
      // 0ef: invokevirtual java/lang/String.isEmpty ()Z
      // 0f2: ifeq 0fc
      // 0f5: aload 5
      // 0f7: invokestatic name/modid/client/BattleMessageSubscriber.getTeraType (Ljava/lang/String;)Ljava/lang/String;
      // 0fa: astore 6
      // 0fc: aload 6
      // 0fe: ifnull 130
      // 101: aload 6
      // 103: invokevirtual java/lang/String.isEmpty ()Z
      // 106: ifne 130
      // 109: ldc_w "[CustomController] Got tera type from BattleMessageSubscriber: {}"
      // 10c: bipush 1
      // 10d: anewarray 4
      // 110: dup
      // 111: bipush 0
      // 112: aload 6
      // 114: aastore
      // 115: invokestatic name/modid/CobblemonBattleInfoClient.debug (Ljava/lang/String;[Ljava/lang/Object;)V
      // 118: aload 4
      // 11a: ifnull 12d
      // 11d: getstatic name/modid/client/CustomBattleController.cachedTeraTypes Ljava/util/Map;
      // 120: aload 4
      // 122: invokevirtual java/util/UUID.toString ()Ljava/lang/String;
      // 125: aload 6
      // 127: invokeinterface java/util/Map.put (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; 3
      // 12c: pop
      // 12d: aload 6
      // 12f: areturn
      // 130: aload 4
      // 132: ifnull 336
      // 135: invokestatic net/minecraft/class_310.method_1551 ()Lnet/minecraft/class_310;
      // 138: getfield net/minecraft/class_310.field_1724 Lnet/minecraft/class_746;
      // 13b: astore 6
      // 13d: aload 6
      // 13f: ifnull 336
      // 142: ldc_w "com.cobblemon.mod.common.Cobblemon"
      // 145: invokestatic java/lang/Class.forName (Ljava/lang/String;)Ljava/lang/Class;
      // 148: astore 7
      // 14a: aload 7
      // 14c: ldc_w "INSTANCE"
      // 14f: invokevirtual java/lang/Class.getField (Ljava/lang/String;)Ljava/lang/reflect/Field;
      // 152: astore 8
      // 154: aload 8
      // 156: aconst_null
      // 157: invokevirtual java/lang/reflect/Field.get (Ljava/lang/Object;)Ljava/lang/Object;
      // 15a: astore 9
      // 15c: aload 9
      // 15e: ifnull 31f
      // 161: aload 9
      // 163: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 166: ldc_w "getStorage"
      // 169: bipush 0
      // 16a: anewarray 471
      // 16d: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 170: astore 10
      // 172: aload 10
      // 174: aload 9
      // 176: bipush 0
      // 177: anewarray 4
      // 17a: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 17d: astore 11
      // 17f: aload 11
      // 181: ifnull 31f
      // 184: aconst_null
      // 185: astore 12
      // 187: aconst_null
      // 188: astore 13
      // 18a: aconst_null
      // 18b: astore 14
      // 18d: aload 11
      // 18f: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 192: invokevirtual java/lang/Class.getMethods ()[Ljava/lang/reflect/Method;
      // 195: astore 15
      // 197: aload 15
      // 199: arraylength
      // 19a: istore 16
      // 19c: bipush 0
      // 19d: istore 17
      // 19f: iload 17
      // 1a1: iload 16
      // 1a3: if_icmpge 1e8
      // 1a6: aload 15
      // 1a8: iload 17
      // 1aa: aaload
      // 1ab: astore 18
      // 1ad: aload 18
      // 1af: invokevirtual java/lang/reflect/Method.getName ()Ljava/lang/String;
      // 1b2: ldc_w "getParty"
      // 1b5: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 1b8: ifeq 1e2
      // 1bb: aload 18
      // 1bd: invokevirtual java/lang/reflect/Method.getParameterCount ()I
      // 1c0: bipush 2
      // 1c1: if_icmpne 1e2
      // 1c4: aload 18
      // 1c6: invokevirtual java/lang/reflect/Method.getParameterTypes ()[Ljava/lang/Class;
      // 1c9: astore 19
      // 1cb: aload 19
      // 1cd: bipush 0
      // 1ce: aaload
      // 1cf: ldc_w java/util/UUID
      // 1d2: if_acmpne 1e2
      // 1d5: aload 18
      // 1d7: astore 13
      // 1d9: aload 19
      // 1db: bipush 1
      // 1dc: aaload
      // 1dd: astore 14
      // 1df: goto 1e8
      // 1e2: iinc 17 1
      // 1e5: goto 19f
      // 1e8: aload 13
      // 1ea: ifnull 31f
      // 1ed: aload 14
      // 1ef: ifnull 31f
      // 1f2: aconst_null
      // 1f3: astore 15
      // 1f5: aload 14
      // 1f7: invokevirtual java/lang/Class.isEnum ()Z
      // 1fa: ifeq 218
      // 1fd: aload 14
      // 1ff: invokevirtual java/lang/Class.getEnumConstants ()[Ljava/lang/Object;
      // 202: astore 16
      // 204: aload 16
      // 206: ifnull 215
      // 209: aload 16
      // 20b: arraylength
      // 20c: ifle 215
      // 20f: aload 16
      // 211: bipush 0
      // 212: aaload
      // 213: astore 15
      // 215: goto 24b
      // 218: aload 14
      // 21a: invokevirtual java/lang/Class.getName ()Ljava/lang/String;
      // 21d: ldc_w "class_5455"
      // 220: invokevirtual java/lang/String.contains (Ljava/lang/CharSequence;)Z
      // 223: ifne 234
      // 226: aload 14
      // 228: invokevirtual java/lang/Class.getName ()Ljava/lang/String;
      // 22b: ldc_w "RegistryAccess"
      // 22e: invokevirtual java/lang/String.contains (Ljava/lang/CharSequence;)Z
      // 231: ifeq 24b
      // 234: invokestatic net/minecraft/class_310.method_1551 ()Lnet/minecraft/class_310;
      // 237: astore 16
      // 239: aload 16
      // 23b: getfield net/minecraft/class_310.field_1687 Lnet/minecraft/class_638;
      // 23e: ifnull 24b
      // 241: aload 16
      // 243: getfield net/minecraft/class_310.field_1687 Lnet/minecraft/class_638;
      // 246: invokevirtual net/minecraft/class_638.method_30349 ()Lnet/minecraft/class_5455;
      // 249: astore 15
      // 24b: aload 15
      // 24d: ifnull 31f
      // 250: aload 13
      // 252: aload 11
      // 254: bipush 2
      // 255: anewarray 4
      // 258: dup
      // 259: bipush 0
      // 25a: aload 6
      // 25c: invokevirtual net/minecraft/class_746.method_5667 ()Ljava/util/UUID;
      // 25f: aastore
      // 260: dup
      // 261: bipush 1
      // 262: aload 15
      // 264: aastore
      // 265: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 268: astore 12
      // 26a: aload 12
      // 26c: ifnull 31f
      // 26f: aload 12
      // 271: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 274: ldc_w "iterator"
      // 277: bipush 0
      // 278: anewarray 471
      // 27b: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 27e: astore 16
      // 280: aload 16
      // 282: aload 12
      // 284: bipush 0
      // 285: anewarray 4
      // 288: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 28b: checkcast java/util/Iterator
      // 28e: astore 17
      // 290: aload 17
      // 292: invokeinterface java/util/Iterator.hasNext ()Z 1
      // 297: ifeq 31f
      // 29a: aload 17
      // 29c: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
      // 2a1: astore 18
      // 2a3: aload 18
      // 2a5: ifnonnull 2ab
      // 2a8: goto 290
      // 2ab: aload 18
      // 2ad: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 2b0: ldc_w "getUuid"
      // 2b3: bipush 0
      // 2b4: anewarray 471
      // 2b7: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 2ba: astore 19
      // 2bc: aload 19
      // 2be: aload 18
      // 2c0: bipush 0
      // 2c1: anewarray 4
      // 2c4: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 2c7: astore 20
      // 2c9: aload 4
      // 2cb: aload 20
      // 2cd: invokevirtual java/util/UUID.equals (Ljava/lang/Object;)Z
      // 2d0: ifeq 317
      // 2d3: aload 18
      // 2d5: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 2d8: ldc_w "getTeraType"
      // 2db: bipush 0
      // 2dc: anewarray 471
      // 2df: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 2e2: astore 21
      // 2e4: aload 21
      // 2e6: aload 18
      // 2e8: bipush 0
      // 2e9: anewarray 4
      // 2ec: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 2ef: astore 22
      // 2f1: aload 22
      // 2f3: ifnull 314
      // 2f6: aload 22
      // 2f8: invokestatic name/modid/client/CustomBattleController.getTeraTypeName (Ljava/lang/Object;)Ljava/lang/String;
      // 2fb: astore 23
      // 2fd: aload 23
      // 2ff: ifnull 314
      // 302: ldc_w "[CustomController] Got tera type from party (PRIORITY 1): {}"
      // 305: bipush 1
      // 306: anewarray 4
      // 309: dup
      // 30a: bipush 0
      // 30b: aload 23
      // 30d: aastore
      // 30e: invokestatic name/modid/CobblemonBattleInfoClient.debug (Ljava/lang/String;[Ljava/lang/Object;)V
      // 311: aload 23
      // 313: areturn
      // 314: goto 31f
      // 317: goto 31c
      // 31a: astore 19
      // 31c: goto 290
      // 31f: goto 336
      // 322: astore 7
      // 324: ldc_w "[CustomController] Failed to get tera type from party: {}"
      // 327: bipush 1
      // 328: anewarray 4
      // 32b: dup
      // 32c: bipush 0
      // 32d: aload 7
      // 32f: invokevirtual java/lang/Exception.getMessage ()Ljava/lang/String;
      // 332: aastore
      // 333: invokestatic name/modid/CobblemonBattleInfoClient.debug (Ljava/lang/String;[Ljava/lang/Object;)V
      // 336: aload 3
      // 337: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 33a: ldc_w "getTeraType"
      // 33d: bipush 0
      // 33e: anewarray 471
      // 341: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 344: astore 6
      // 346: aload 6
      // 348: aload 3
      // 349: bipush 0
      // 34a: anewarray 4
      // 34d: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 350: astore 7
      // 352: aload 7
      // 354: ifnull 375
      // 357: aload 7
      // 359: invokestatic name/modid/client/CustomBattleController.getTeraTypeName (Ljava/lang/Object;)Ljava/lang/String;
      // 35c: astore 8
      // 35e: aload 8
      // 360: ifnull 375
      // 363: ldc_w "[CustomController] Got tera type from battlePokemon: {}"
      // 366: bipush 1
      // 367: anewarray 4
      // 36a: dup
      // 36b: bipush 0
      // 36c: aload 8
      // 36e: aastore
      // 36f: invokestatic name/modid/CobblemonBattleInfoClient.debug (Ljava/lang/String;[Ljava/lang/Object;)V
      // 372: aload 8
      // 374: areturn
      // 375: goto 391
      // 378: astore 6
      // 37a: goto 391
      // 37d: astore 6
      // 37f: ldc_w "[CustomController] Failed to get tera type from battlePokemon: {}"
      // 382: bipush 1
      // 383: anewarray 4
      // 386: dup
      // 387: bipush 0
      // 388: aload 6
      // 38a: invokevirtual java/lang/Exception.getMessage ()Ljava/lang/String;
      // 38d: aastore
      // 38e: invokestatic name/modid/CobblemonBattleInfoClient.debug (Ljava/lang/String;[Ljava/lang/Object;)V
      // 391: aload 3
      // 392: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 395: ldc_w "getProperties"
      // 398: bipush 0
      // 399: anewarray 471
      // 39c: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 39f: astore 6
      // 3a1: aload 6
      // 3a3: aload 3
      // 3a4: bipush 0
      // 3a5: anewarray 4
      // 3a8: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 3ab: astore 7
      // 3ad: aload 7
      // 3af: ifnull 416
      // 3b2: aload 7
      // 3b4: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 3b7: ldc_w "create"
      // 3ba: bipush 0
      // 3bb: anewarray 471
      // 3be: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 3c1: astore 8
      // 3c3: aload 8
      // 3c5: aload 7
      // 3c7: bipush 0
      // 3c8: anewarray 4
      // 3cb: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 3ce: astore 9
      // 3d0: aload 9
      // 3d2: ifnull 416
      // 3d5: aload 9
      // 3d7: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 3da: ldc_w "getTeraType"
      // 3dd: bipush 0
      // 3de: anewarray 471
      // 3e1: invokevirtual java/lang/Class.getMethod (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
      // 3e4: astore 10
      // 3e6: aload 10
      // 3e8: aload 9
      // 3ea: bipush 0
      // 3eb: anewarray 4
      // 3ee: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
      // 3f1: astore 11
      // 3f3: aload 11
      // 3f5: ifnull 416
      // 3f8: aload 11
      // 3fa: invokestatic name/modid/client/CustomBattleController.getTeraTypeName (Ljava/lang/Object;)Ljava/lang/String;
      // 3fd: astore 12
      // 3ff: aload 12
      // 401: ifnull 416
      // 404: ldc_w "[CustomController] Got tera type from properties.create() (fallback): {}"
      // 407: bipush 1
      // 408: anewarray 4
      // 40b: dup
      // 40c: bipush 0
      // 40d: aload 12
      // 40f: aastore
      // 410: invokestatic name/modid/CobblemonBattleInfoClient.debug (Ljava/lang/String;[Ljava/lang/Object;)V
      // 413: aload 12
      // 415: areturn
      // 416: goto 42d
      // 419: astore 6
      // 41b: ldc_w "[CustomController] Failed to get tera type from properties.create(): {}"
      // 41e: bipush 1
      // 41f: anewarray 4
      // 422: dup
      // 423: bipush 0
      // 424: aload 6
      // 426: invokevirtual java/lang/Exception.getMessage ()Ljava/lang/String;
      // 429: aastore
      // 42a: invokestatic name/modid/CobblemonBattleInfoClient.debug (Ljava/lang/String;[Ljava/lang/Object;)V
      // 42d: aconst_null
      // 42e: areturn
      // 42f: astore 0
      // 430: ldc_w "[CustomController] Error getting active tera type: {}"
      // 433: bipush 1
      // 434: anewarray 4
      // 437: dup
      // 438: bipush 0
      // 439: aload 0
      // 43a: invokevirtual java/lang/Throwable.getMessage ()Ljava/lang/String;
      // 43d: aastore
      // 43e: invokestatic name/modid/CobblemonBattleInfoClient.debug (Ljava/lang/String;[Ljava/lang/Object;)V
      // 441: aconst_null
      // 442: areturn
   }

   private static String getTeraTypeName(Object teraType) {
      if (teraType == null) {
         return null;
      } else {
         try {
            Method showdownIdMethod = teraType.getClass().getMethod("showdownId");
            return (String)showdownIdMethod.invoke(teraType);
         } catch (NoSuchMethodException var6) {
            try {
               Method getTypeMethod = teraType.getClass().getMethod("getType");
               Object elementalType = getTypeMethod.invoke(teraType);
               if (elementalType != null) {
                  Method getNameMethod = elementalType.getClass().getMethod("getName");
                  return (String)getNameMethod.invoke(elementalType);
               }
            } catch (Exception var5) {
               CobblemonBattleInfoClient.debug("[CustomController] Failed to get tera type name: {}", new Object[]{var5.getMessage()});
            }
         } catch (Exception var7) {
            CobblemonBattleInfoClient.debug("[CustomController] Failed to call showdownId: {}", new Object[]{var7.getMessage()});
         }

         return null;
      }
   }

   private static String getAbilityFromPokemon(Object pokemon) {
      try {
         Object ability = null;

         for (String methodName : new String[]{"getAbility", "ability"}) {
            try {
               Method method = pokemon.getClass().getMethod(methodName);
               ability = method.invoke(pokemon);
               if (ability != null) {
                  break;
               }
            } catch (NoSuchMethodException var15) {
            }
         }

         if (ability == null) {
            return null;
         }

         String abilityName = null;

         try {
            Method getDisplayName = ability.getClass().getMethod("getDisplayName");
            Object displayName = getDisplayName.invoke(ability);
            if (displayName != null) {
               try {
                  Method getString = displayName.getClass().getMethod("getString");
                  Object str = getString.invoke(displayName);
                  if (str != null) {
                     abilityName = str.toString();
                  }
               } catch (NoSuchMethodException var12) {
                  abilityName = displayName.toString();
               }
            }
         } catch (NoSuchMethodException var13) {
         }

         if (abilityName == null || abilityName.isEmpty()) {
            try {
               Method getName = ability.getClass().getMethod("getName");
               Object name = getName.invoke(ability);
               if (name != null) {
                  abilityName = name.toString();
               }
            } catch (NoSuchMethodException var11) {
            }
         }

         if (abilityName == null || abilityName.isEmpty()) {
            try {
               Method getTemplate = ability.getClass().getMethod("getTemplate");
               Object template = getTemplate.invoke(ability);
               if (template != null) {
                  try {
                     Method getDisplayName = template.getClass().getMethod("getDisplayName");
                     Object displayName = getDisplayName.invoke(template);
                     if (displayName != null) {
                        try {
                           Method getString = displayName.getClass().getMethod("getString");
                           Object str = getString.invoke(displayName);
                           if (str != null) {
                              abilityName = str.toString();
                           }
                        } catch (NoSuchMethodException var9) {
                           abilityName = displayName.toString();
                        }
                     }
                  } catch (NoSuchMethodException var10) {
                  }

                  if (abilityName == null || abilityName.isEmpty()) {
                     Method getName = template.getClass().getMethod("getName");
                     Object name = getName.invoke(template);
                     if (name != null) {
                        abilityName = name.toString();
                     }
                  }
               }
            } catch (NoSuchMethodException var14) {
            }
         }

         if (abilityName != null && !abilityName.isEmpty()) {
            if (abilityName.contains(":")) {
               abilityName = abilityName.substring(abilityName.indexOf(":") + 1);
            }

            if (abilityName.contains(".")) {
               int lastDot = abilityName.lastIndexOf(".");
               if (lastDot >= 0 && lastDot < abilityName.length() - 1) {
                  abilityName = abilityName.substring(lastDot + 1);
               }
            }

            String translationKey = "cobblemon.ability." + abilityName.toLowerCase();
            class_2561 translated = class_2561.method_43471(translationKey);
            String translatedName = translated.getString();
            if (translatedName.equals(translationKey)) {
               return formatMoveName(abilityName);
            }

            return translatedName;
         }
      } catch (Throwable var16) {
      }

      return null;
   }

   private static Object getPokemonFromSwitchTile(Object switchTile) {
      try {
         for (String methodName : new String[]{"getPokemon", "pokemon", "getBattlePokemon"}) {
            try {
               Method method = switchTile.getClass().getMethod(methodName);
               Object result = method.invoke(switchTile);
               if (result != null) {
                  return result;
               }
            } catch (NoSuchMethodException var8) {
            }
         }

         for (Class<?> clazz = switchTile.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
               String fieldName = field.getName().toLowerCase();
               if (fieldName.contains("pokemon")) {
                  field.setAccessible(true);
                  Object result = field.get(switchTile);
                  if (result != null) {
                     return result;
                  }
               }
            }
         }
      } catch (Throwable var9) {
      }

      return null;
   }

   private static void rememberSwitchSelectionFromTile(Object tile) {
      if (tile != null) {
         Object pokemon = getPokemonFromSwitchTile(tile);
         if (pokemon != null) {
            LocalBattlePartyTracker.rememberSwitchSelectionForCurrentRequest(pokemon);
         }
      }
   }

   private static List<String> getMovesFromPokemon(Object pokemon) {
      List<String> moves = new ArrayList<>();

      try {
         Object moveSet = null;

         for (String methodName : new String[]{"getMoveSet", "getMoves", "moveSet", "moves", "getMoveset"}) {
            try {
               Method method = pokemon.getClass().getMethod(methodName);
               moveSet = method.invoke(pokemon);
               if (moveSet != null) {
                  break;
               }
            } catch (NoSuchMethodException var19) {
            }
         }

         if (moveSet == null) {
            for (Class<?> clazz = pokemon.getClass(); clazz != null && clazz != Object.class && moveSet == null; clazz = clazz.getSuperclass()) {
               for (Field field : clazz.getDeclaredFields()) {
                  String fieldName = field.getName().toLowerCase();
                  if (fieldName.contains("move")) {
                     field.setAccessible(true);
                     moveSet = field.get(pokemon);
                     if (moveSet != null) {
                        break;
                     }
                  }
               }
            }
         }

         if (moveSet == null) {
            return moves;
         }

         Iterable<?> moveIterable = null;

         try {
            Method getMovesMethod = moveSet.getClass().getMethod("getMoves");
            Object movesObj = getMovesMethod.invoke(moveSet);
            if (movesObj instanceof Iterable) {
               moveIterable = (Iterable<?>)movesObj;
            }
         } catch (NoSuchMethodException var16) {
         }

         if (moveIterable == null && moveSet instanceof Iterable) {
            moveIterable = (Iterable<?>)moveSet;
         }

         if (moveIterable == null) {
            try {
               Method toListMethod = moveSet.getClass().getMethod("toList");
               Object listObj = toListMethod.invoke(moveSet);
               if (listObj instanceof Iterable) {
                  moveIterable = (Iterable<?>)listObj;
               }
            } catch (NoSuchMethodException var15) {
            }
         }

         if (moveIterable != null) {
            for (Object move : moveIterable) {
               if (move != null) {
                  String moveName = null;

                  for (String methodName : new String[]{"getName", "getDisplayName", "name", "getTemplate"}) {
                     try {
                        Method method = move.getClass().getMethod(methodName);
                        Object result = method.invoke(move);
                        if (result != null) {
                           if (!methodName.equals("getTemplate") || result == null) {
                              moveName = result.toString();
                              break;
                           }

                           try {
                              Method nameMethod = result.getClass().getMethod("getName");
                              Object nameResult = nameMethod.invoke(result);
                              if (nameResult != null) {
                                 moveName = nameResult.toString();
                                 break;
                              }
                           } catch (NoSuchMethodException var17) {
                           }
                        }
                     } catch (NoSuchMethodException var18) {
                     }
                  }

                  if (moveName != null && !moveName.isEmpty()) {
                     moves.add(moveName);
                  }
               }
            }
         }
      } catch (Throwable var20) {
      }

      return moves;
   }

   private static String formatMoveName(String moveName) {
      if (moveName != null && !moveName.isEmpty()) {
         if (moveName.contains(":")) {
            moveName = moveName.substring(moveName.indexOf(":") + 1);
         }

         String processed = moveName.replace("_", " ");
         if (!processed.contains(" ") && processed.length() > 6) {
            processed = splitConcatenatedWords(processed.toLowerCase());
         }

         String[] words = processed.split(" ");
         StringBuilder result = new StringBuilder();

         for (String word : words) {
            if (!word.isEmpty()) {
               if (result.length() > 0) {
                  result.append(" ");
               }

               result.append(Character.toUpperCase(word.charAt(0)));
               if (word.length() > 1) {
                  result.append(word.substring(1).toLowerCase());
               }
            }
         }

         return result.toString();
      } else {
         return "";
      }
   }

   private static String splitConcatenatedWords(String name) {
      String[] wordParts = new String[]{
         "thunder",
         "shadow",
         "dragon",
         "psychic",
         "sacred",
         "ancient",
         "bullet",
         "cosmic",
         "double",
         "energy",
         "extreme",
         "future",
         "giga",
         "harsh",
         "heavy",
         "hyper",
         "leafy",
         "mega",
         "night",
         "origin",
         "phantom",
         "power",
         "quick",
         "rapid",
         "razor",
         "seismic",
         "signal",
         "solar",
         "spacial",
         "stealth",
         "strange",
         "super",
         "water",
         "weather",
         "wicked",
         "destiny",
         "natural",
         "refresh",
         "punishment",
         "confusion",
         "fire",
         "ice",
         "rock",
         "steel",
         "fairy",
         "dark",
         "ghost",
         "grass",
         "ground",
         "flying",
         "poison",
         "bug",
         "electric",
         "fighting",
         "normal",
         "beam",
         "blast",
         "blitz",
         "blow",
         "bomb",
         "breath",
         "buzz",
         "cannon",
         "charge",
         "claw",
         "combat",
         "crash",
         "cutter",
         "dance",
         "drain",
         "edge",
         "energy",
         "fang",
         "force",
         "gem",
         "gleam",
         "hammer",
         "head",
         "horn",
         "impact",
         "kick",
         "leaf",
         "meteor",
         "mash",
         "palm",
         "peck",
         "power",
         "pulse",
         "punch",
         "rage",
         "rush",
         "seed",
         "slam",
         "slash",
         "slide",
         "smash",
         "sneak",
         "speed",
         "spin",
         "storm",
         "strike",
         "sweep",
         "swing",
         "tackle",
         "tail",
         "terrain",
         "throw",
         "toss",
         "veil",
         "wave",
         "wheel",
         "whip",
         "wind",
         "wing",
         "gift",
         "bond",
         "switch",
         "gun",
         "ally",
         "stomping",
         "tantrum",
         "plot",
         "smack",
         "attack",
         "ball",
         "burst",
         "ray",
         "shot"
      };
      String remaining = name.toLowerCase();
      StringBuilder result = new StringBuilder();

      while (!remaining.isEmpty()) {
         boolean found = false;

         for (String part : wordParts) {
            if (remaining.startsWith(part)) {
               if (result.length() > 0) {
                  result.append(" ");
               }

               result.append(part);
               remaining = remaining.substring(part.length());
               found = true;
               break;
            }
         }

         if (!found) {
            int earliestMatch = remaining.length();
            String matchedPart = null;

            for (String partx : wordParts) {
               int idx = remaining.indexOf(partx);
               if (idx > 0 && idx < earliestMatch) {
                  earliestMatch = idx;
                  matchedPart = partx;
               }
            }

            if (matchedPart == null || earliestMatch <= 0) {
               if (result.length() > 0) {
                  result.append(" ");
               }

               result.append(remaining);
               break;
            }

            if (result.length() > 0) {
               result.append(" ");
            }

            result.append(remaining.substring(0, earliestMatch));
            remaining = remaining.substring(earliestMatch);
         }
      }

      return result.toString();
   }

   private static String getGimmickType(Object gimmickButton) {
      try {
         try {
            Method getGimmick = gimmickButton.getClass().getMethod("getGimmick");
            Object gimmick = getGimmick.invoke(gimmickButton);
            if (gimmick != null) {
               return gimmick.toString().toLowerCase();
            }
         } catch (NoSuchMethodException var10) {
         }

         try {
            Method getType = gimmickButton.getClass().getMethod("getType");
            Object type = getType.invoke(gimmickButton);
            if (type != null) {
               return type.toString().toLowerCase();
            }
         } catch (NoSuchMethodException var9) {
         }

         try {
            Method getName = gimmickButton.getClass().getMethod("getName");
            Object name = getName.invoke(gimmickButton);
            if (name != null) {
               return name.toString().toLowerCase();
            }
         } catch (NoSuchMethodException var8) {
         }

         for (Class<?> clazz = gimmickButton.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field f : clazz.getDeclaredFields()) {
               try {
                  f.setAccessible(true);
                  Object value = f.get(gimmickButton);
                  if (value != null) {
                     String valueStr = value.toString().toLowerCase();
                     if (valueStr.contains("mega")
                        || valueStr.contains("tera")
                        || valueStr.contains("dynamax")
                        || valueStr.contains("zmove")
                        || valueStr.contains("z_move")
                        || valueStr.contains("zpower")
                        || valueStr.contains("z_power")
                        || valueStr.contains("gigantamax")
                        || valueStr.contains("ultra")
                        || valueStr.contains("burst")) {
                        return valueStr;
                     }
                  }
               } catch (Throwable var11) {
               }
            }
         }
      } catch (Throwable var12) {
         CobblemonBattleInfoClient.LOGGER.error("[CustomController] Error getting gimmick type: {}", var12.getMessage());
      }

      return "";
   }

   private static boolean isBackButton(Object tile) {
      if (tile == null) {
         return false;
      } else {
         String className = tile.getClass().getName();
         return className.contains("BackButton") || className.contains("BattleBackButton");
      }
   }

   private static Object[] sortTilesByPosition(List<Object> tiles) {
      int[][] positions = new int[tiles.size()][2];

      for (int i = 0; i < tiles.size(); i++) {
         positions[i][0] = getTileX(tiles.get(i));
         positions[i][1] = getTileY(tiles.get(i));
      }

      int minX = Integer.MAX_VALUE;
      int maxX = Integer.MIN_VALUE;
      int minY = Integer.MAX_VALUE;
      int maxY = Integer.MIN_VALUE;

      for (int[] pos : positions) {
         minX = Math.min(minX, pos[0]);
         maxX = Math.max(maxX, pos[0]);
         minY = Math.min(minY, pos[1]);
         maxY = Math.max(maxY, pos[1]);
      }

      int midX = (minX + maxX) / 2;
      int midY = (minY + maxY) / 2;
      Object[] result = new Object[4];

      for (int i = 0; i < tiles.size() && i < 4; i++) {
         Object tile = tiles.get(i);
         int x = positions[i][0];
         int y = positions[i][1];
         boolean isLeft = x < midX;
         boolean isTop = y < midY;
         int idx;
         if (isTop && isLeft) {
            idx = 0;
         } else if (isTop && !isLeft) {
            idx = 1;
         } else if (!isTop && isLeft) {
            idx = 2;
         } else {
            idx = 3;
         }

         result[idx] = tile;
      }

      return result;
   }

   private static Object[] sortTilesTopToBottomLeftToRight(List<Object> tiles) {
      List<Object> sorted = new ArrayList<>(tiles);
      sorted.sort((a, b) -> {
         int ay = getTileY(a);
         int by = getTileY(b);
         return Math.abs(ay - by) > 8 ? Integer.compare(ay, by) : Integer.compare(getTileX(a), getTileX(b));
      });
      return sorted.toArray(new Object[0]);
   }

   private static int calculateTargetGridColumns(Object[] tiles) {
      if (tiles != null && tiles.length != 0) {
         List<Integer> xPositions = new ArrayList<>();

         for (Object tile : tiles) {
            if (tile != null) {
               int x = getTileX(tile);
               if (x > 0) {
                  xPositions.add(x);
               }
            }
         }

         if (xPositions.isEmpty()) {
            return Math.max(1, maxButtons);
         } else {
            xPositions.sort(Integer::compareTo);
            int uniqueColumns = 0;
            int lastX = Integer.MIN_VALUE;

            for (int x : xPositions) {
               if (uniqueColumns == 0 || Math.abs(x - lastX) > 10) {
                  uniqueColumns++;
                  lastX = x;
               }
            }

            return Math.max(1, uniqueColumns);
         }
      } else {
         return 1;
      }
   }

   private static int getTileX(Object tile) {
      TileSnapshot snapshot = getTileSnapshot(tile);
      return snapshot != null ? snapshot.x() : getTileXUncached(tile);
   }

   private static int getTileXUncached(Object tile) {
      try {
         Method method = tile.getClass().getMethod("getX");
         Object result = method.invoke(tile);
         return result instanceof Float ? ((Float)result).intValue() : ((Number)result).intValue();
      } catch (Throwable var5) {
         try {
            Field field = tile.getClass().getDeclaredField("x");
            field.setAccessible(true);
            Object resultx = field.get(tile);
            return resultx instanceof Float ? ((Float)resultx).intValue() : ((Number)resultx).intValue();
         } catch (Throwable var4) {
            return 0;
         }
      }
   }

   private static int getTileY(Object tile) {
      TileSnapshot snapshot = getTileSnapshot(tile);
      return snapshot != null ? snapshot.y() : getTileYUncached(tile);
   }

   private static int getTileYUncached(Object tile) {
      try {
         Method method = tile.getClass().getMethod("getY");
         Object result = method.invoke(tile);
         return result instanceof Float ? ((Float)result).intValue() : ((Number)result).intValue();
      } catch (Throwable var5) {
         try {
            Field field = tile.getClass().getDeclaredField("y");
            field.setAccessible(true);
            Object resultx = field.get(tile);
            return resultx instanceof Float ? ((Float)resultx).intValue() : ((Number)resultx).intValue();
         } catch (Throwable var4) {
            return 0;
         }
      }
   }

   private static int getTileWidth(Object tile) {
      TileSnapshot snapshot = getTileSnapshot(tile);
      return snapshot != null ? snapshot.width() : getTileWidthUncached(tile);
   }

   private static int getTileWidthUncached(Object tile) {
      String tileClassName = tile.getClass().getName();
      if (tileClassName.contains("BackButton")) {
         return 24;
      } else if (isGimmickButton(tile)) {
         return 20;
      } else if (tileClassName.contains("MoveTile") || tileClassName.contains("GimmickTile") || tileClassName.contains("DynamaxTile")) {
         return 91;
      } else if (tileClassName.contains("BattleTargetSelection")) {
         return 93;
      } else if (tileClassName.contains("SwitchPokemonSelection")) {
         return 91;
      } else {
         try {
            Field widthField = tile.getClass().getDeclaredField("OPTION_WIDTH");
            widthField.setAccessible(true);
            Object value = widthField.get(null);
            if (value instanceof Number) {
               return ((Number)value).intValue();
            }
         } catch (Throwable var6) {
         }

         try {
            Method method = tile.getClass().getMethod("getWidth");
            Object result = method.invoke(tile);
            if (result instanceof Number) {
               int width = ((Number)result).intValue();
               if (width > 0 && width <= 200) {
                  return width;
               }
            }
         } catch (Throwable var5) {
         }

         return BUTTON_WIDTH;
      }
   }

   private static int getTileHeight(Object tile) {
      TileSnapshot snapshot = getTileSnapshot(tile);
      return snapshot != null ? snapshot.height() : getTileHeightUncached(tile);
   }

   private static int getTileHeightUncached(Object tile) {
      String tileClassName = tile.getClass().getName();
      CobblemonBattleInfoClient.debug("[CustomController] getTileHeight for class: {}", new Object[]{tileClassName});
      if (tileClassName.contains("BackButton")) {
         return 15;
      } else if (isGimmickButton(tile)) {
         return 15;
      } else if (tileClassName.contains("MoveTile") || tileClassName.contains("GimmickTile") || tileClassName.contains("DynamaxTile")) {
         return 24;
      } else if (tileClassName.contains("BattleTargetSelection")) {
         return 33;
      } else if (tileClassName.contains("SwitchPokemonSelection")) {
         return 23;
      } else {
         try {
            Field heightField = tile.getClass().getDeclaredField("OPTION_HEIGHT");
            heightField.setAccessible(true);
            Object value = heightField.get(null);
            if (value instanceof Number) {
               return ((Number)value).intValue();
            }
         } catch (Throwable var6) {
         }

         try {
            Method method = tile.getClass().getMethod("getHeight");
            Object result = method.invoke(tile);
            if (result instanceof Number) {
               int height = ((Number)result).intValue();
               if (height > 0 && height <= 100) {
                  return height;
               }
            }
         } catch (Throwable var5) {
         }

         return BUTTON_HEIGHT;
      }
   }

   private static boolean isBattleGUI(class_437 screen) {
      return screen == null ? false : screen.getClass().getName().contains("BattleGUI");
   }

   private static void invokeMouseMoved(class_437 gui, double x, double y) {
      try {
         Method method = gui.getClass().getMethod("mouseMoved", double.class, double.class);
         method.invoke(gui, x, y);
      } catch (Throwable var6) {
      }
   }

   private static void handleInput(class_310 mc, class_437 gui) {
      long now = System.currentTimeMillis();
      if (now - lastInputTime >= 150L) {
         boolean upNow = isUpPressed();
         boolean downNow = isDownPressed();
         boolean leftNow = isLeftPressed();
         boolean rightNow = isRightPressed();
         boolean selectNow = isSelectPressed();
         boolean backNow = isBackPressed();
         if (upNow && !upPressed) {
            navigateUp();
            lastInputTime = now;
         }

         if (downNow && !downPressed) {
            navigateDown();
            lastInputTime = now;
         }

         if (leftNow && !leftPressed) {
            navigateLeft();
            lastInputTime = now;
         }

         if (rightNow && !rightPressed) {
            navigateRight();
            lastInputTime = now;
         }

         if (selectNow && !selectPressed) {
            CobblemonBattleInfoClient.debug("[CustomController] SELECT pressed! Activating button {}", new Object[]{selectedButton});
            activateButton(gui);
            lastInputTime = now;
         }

         if (backNow && !backPressed) {
            CobblemonBattleInfoClient.debug("[CustomController] BACK pressed! Going back from {}", new Object[]{currentSelectionType});
            goBack(gui);
            lastInputTime = now;
         }

         upPressed = upNow;
         downPressed = downNow;
         leftPressed = leftNow;
         rightPressed = rightNow;
         selectPressed = selectNow;
         backPressed = backNow;
      }
   }

   private static void navigateUp() {
      int cols = getGridColumns();
      if (currentSelectionType.equals("moves") && selectedButton >= numMoveTiles) {
         int bottomMoveRow = (numMoveTiles - 1) / cols;
         int bottomLeftMove = bottomMoveRow * cols;
         if (bottomLeftMove < numMoveTiles) {
            selectedButton = bottomLeftMove;
         } else {
            selectedButton = 0;
         }

         lastNavigationTime = System.currentTimeMillis();
      } else if (currentSelectionType.equals("switch")) {
         int backButtonIndex = maxButtons - 1;
         if (selectedButton == backButtonIndex) {
            int bottomPokemonRow = (backButtonIndex - 1) / cols;
            int bottomLeftPokemon = bottomPokemonRow * cols;
            if (bottomLeftPokemon < backButtonIndex) {
               selectedButton = bottomLeftPokemon;
            } else {
               selectedButton = 0;
            }

            lastNavigationTime = System.currentTimeMillis();
         } else {
            int newButton = selectedButton - cols;
            if (newButton >= 0) {
               selectedButton = newButton;
               lastNavigationTime = System.currentTimeMillis();
            }
         }
      } else {
         int newButton = selectedButton - cols;
         if (newButton >= 0) {
            selectedButton = newButton;
            lastNavigationTime = System.currentTimeMillis();
         }
      }

      CobblemonBattleInfoClient.debug(
         "[CustomController] UP -> Button {} (type: {}, numMoves: {})", new Object[]{selectedButton, currentSelectionType, numMoveTiles}
      );
   }

   private static void navigateDown() {
      int cols = getGridColumns();
      if (currentSelectionType.equals("moves")) {
         int row = selectedButton / cols;
         int col = selectedButton % cols;
         int bottomMoveRow = (numMoveTiles - 1) / cols;
         if (selectedButton < numMoveTiles) {
            if (row == bottomMoveRow) {
               int backButtonIndex = numMoveTiles;
               int firstGimmickIndex = numMoveTiles + 1;
               if (col == 0) {
                  selectedButton = backButtonIndex;
                  lastNavigationTime = System.currentTimeMillis();
               } else if (col == 1 && maxButtons > firstGimmickIndex) {
                  selectedButton = firstGimmickIndex;
                  lastNavigationTime = System.currentTimeMillis();
               } else if (maxButtons > backButtonIndex) {
                  selectedButton = backButtonIndex;
                  lastNavigationTime = System.currentTimeMillis();
               }
            } else {
               int newButton = selectedButton + cols;
               if (newButton < numMoveTiles) {
                  selectedButton = newButton;
                  lastNavigationTime = System.currentTimeMillis();
               } else if (newButton >= numMoveTiles) {
                  int backButtonIndex = numMoveTiles;
                  selectedButton = backButtonIndex;
                  lastNavigationTime = System.currentTimeMillis();
               }
            }
         }
      } else if (currentSelectionType.equals("switch")) {
         int backButtonIndex = maxButtons - 1;
         if (selectedButton < backButtonIndex) {
            int newButton = selectedButton + cols;
            if (newButton < backButtonIndex) {
               selectedButton = newButton;
               lastNavigationTime = System.currentTimeMillis();
            } else {
               selectedButton = backButtonIndex;
               lastNavigationTime = System.currentTimeMillis();
            }
         }
      } else {
         int newButton = selectedButton + cols;
         if (newButton < maxButtons) {
            selectedButton = newButton;
            lastNavigationTime = System.currentTimeMillis();
         }
      }

      CobblemonBattleInfoClient.debug(
         "[CustomController] DOWN -> Button {} (type: {}, numMoves: {})", new Object[]{selectedButton, currentSelectionType, numMoveTiles}
      );
   }

   private static void navigateLeft() {
      int cols = getGridColumns();
      if (currentSelectionType.equals("moves") && selectedButton >= numMoveTiles) {
         int backButtonIndex = numMoveTiles;
         if (selectedButton > backButtonIndex) {
            selectedButton--;
            lastNavigationTime = System.currentTimeMillis();
         }
      } else if (selectedButton % cols > 0) {
         selectedButton--;
         lastNavigationTime = System.currentTimeMillis();
      }

      CobblemonBattleInfoClient.debug("[CustomController] LEFT -> Button {} (type: {})", new Object[]{selectedButton, currentSelectionType});
   }

   private static void navigateRight() {
      int cols = getGridColumns();
      if (currentSelectionType.equals("moves") && selectedButton >= numMoveTiles) {
         if (selectedButton + 1 < maxButtons) {
            selectedButton++;
            lastNavigationTime = System.currentTimeMillis();
         }
      } else if (currentSelectionType.equals("moves") && selectedButton < numMoveTiles) {
         int row = selectedButton / cols;
         int col = selectedButton % cols;
         int bottomMoveRow = (numMoveTiles - 1) / cols;
         int rightNeighbor = selectedButton + 1;
         if (col + 1 < cols && rightNeighbor < numMoveTiles) {
            selectedButton = rightNeighbor;
            lastNavigationTime = System.currentTimeMillis();
         } else if (col == 0 && row == bottomMoveRow && numMoveTiles % cols == 1) {
            int firstGimmickIndex = numMoveTiles + 1;
            if (maxButtons > firstGimmickIndex) {
               selectedButton = firstGimmickIndex;
               lastNavigationTime = System.currentTimeMillis();
            }
         }
      } else if (selectedButton % cols < cols - 1 && selectedButton + 1 < maxButtons) {
         selectedButton++;
         lastNavigationTime = System.currentTimeMillis();
      }

      CobblemonBattleInfoClient.debug("[CustomController] RIGHT -> Button {} (type: {})", new Object[]{selectedButton, currentSelectionType});
   }

   private static int getGridColumns() {
      String var0 = currentSelectionType;
      switch (var0) {
         case "general":
         case "moves":
            return 2;
         case "switch":
            return 2;
         case "target":
            return Math.max(1, targetGridColumns);
         default:
            return 2;
      }
   }

   private static void activateButton(class_437 gui) {
      try {
         Object[] tiles = findBattleOptionTiles(gui);
         if ("general".equals(currentSelectionType) && selectedButton == 3) {
            setBattleGuiClosed();
            CobblemonBattleInfoClient.debug("[CustomController] Run button clicked, marking GUI closed for jump suppression", new Object[0]);
         }

         if (tiles != null && selectedButton < tiles.length && tiles[selectedButton] != null) {
            Object selectedTile = tiles[selectedButton];
            int x = getTileX(selectedTile);
            int y = getTileY(selectedTile);
            int width = getTileWidth(selectedTile);
            int height = getTileHeight(selectedTile);
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            CobblemonBattleInfoClient.debug(
               "[CustomController] ACTIVATE {} at tile ({}, {}) type: {}", new Object[]{selectedButton, x, y, currentSelectionType}
            );
            boolean clicked = false;
            if (isBackButton(selectedTile)) {
               boolean backHandled = false;

               try {
                  Method onClickMethod = selectedTile.getClass().getMethod("onClick");
                  onClickMethod.invoke(selectedTile);
                  backHandled = true;
                  CobblemonBattleInfoClient.debug("[CustomController] back onClick() invoked", new Object[0]);
               } catch (Throwable var30) {
               }

               if (!backHandled) {
                  try {
                     Method m = selectedTile.getClass().getMethod("mouseClicked", double.class, double.class, int.class);
                     Object res = m.invoke(selectedTile, (double)centerX, (double)centerY, 0);
                     if (res instanceof Boolean && (Boolean)res) {
                        backHandled = true;
                     }
                  } catch (Throwable var29) {
                  }
               }

               if (!backHandled) {
                  try {
                     Method m2 = selectedTile.getClass().getMethod("mousePrimaryClicked", double.class, double.class);
                     Object res2 = m2.invoke(selectedTile, (double)centerX, (double)centerY);
                     if (res2 instanceof Boolean && (Boolean)res2) {
                        backHandled = true;
                     }
                  } catch (Throwable var28) {
                  }
               }

               if (!backHandled) {
                  try {
                     Method getOn = selectedTile.getClass().getMethod("getOnClick");
                     Object onClick = getOn.invoke(selectedTile);
                     if (onClick != null) {
                        Method invokeMethod = onClick.getClass().getMethod("invoke");
                        invokeMethod.invoke(onClick);
                        backHandled = true;
                     }
                  } catch (Throwable var27) {
                  }
               }

               if (!backHandled) {
                  goBack(gui);
               }

               return;
            }

            if (isGimmickButton(selectedTile)) {
               boolean gimmickHandled = false;
               CobblemonBattleInfoClient.debug("[CustomController] Activating gimmick button: {}", new Object[]{selectedTile.getClass().getName()});
               Object actionSelection = getActionSelection(gui);
               if (actionSelection != null) {
                  try {
                     Method getGimmickButtons = actionSelection.getClass().getMethod("getGimmickButtons");
                     Object buttons = getGimmickButtons.invoke(actionSelection);
                     if (buttons instanceof List) {
                        for (Object button : (List)buttons) {
                           if (button != selectedTile) {
                              try {
                                 Method setToggled = button.getClass().getMethod("setToggled", boolean.class);
                                 setToggled.invoke(button, false);
                              } catch (Throwable var26) {
                              }
                           }
                        }
                     }

                     Method toggleMethod = selectedTile.getClass().getMethod("toggle");
                     Object toggleResult = toggleMethod.invoke(selectedTile);
                     boolean isToggled = toggleResult instanceof Boolean && (Boolean)toggleResult;
                     Method setMoveTiles = actionSelection.getClass().getMethod("setMoveTiles", List.class);
                     if (isToggled) {
                        Method getTiles = selectedTile.getClass().getMethod("getTiles");
                        Object gimmickTiles = getTiles.invoke(selectedTile);
                        setMoveTiles.invoke(actionSelection, gimmickTiles);
                        CobblemonBattleInfoClient.debug("[CustomController] Gimmick toggled ON - using gimmick tiles", new Object[0]);
                     } else {
                        Method getBaseTiles = actionSelection.getClass().getMethod("getBaseTiles");
                        Object baseTiles = getBaseTiles.invoke(actionSelection);
                        setMoveTiles.invoke(actionSelection, baseTiles);
                        CobblemonBattleInfoClient.debug("[CustomController] Gimmick toggled OFF - using base tiles", new Object[0]);
                     }

                     gimmickHandled = true;
                  } catch (Throwable var37) {
                     CobblemonBattleInfoClient.debug("[CustomController] Gimmick full toggle failed: {}", new Object[]{var37.getMessage()});
                  }
               }

               if (!gimmickHandled) {
                  try {
                     Method toggleMethod = selectedTile.getClass().getMethod("toggle");
                     Object result = toggleMethod.invoke(selectedTile);
                     gimmickHandled = true;
                     CobblemonBattleInfoClient.debug("[CustomController] gimmick toggle() succeeded, result: {}", new Object[]{result});
                  } catch (Throwable var25) {
                     CobblemonBattleInfoClient.debug("[CustomController] gimmick toggle() failed: {}", new Object[]{var25.getMessage()});
                  }
               }

               if (!gimmickHandled) {
                  try {
                     Method onClickMethod = selectedTile.getClass().getMethod("onClick");
                     onClickMethod.invoke(selectedTile);
                     gimmickHandled = true;
                     CobblemonBattleInfoClient.debug("[CustomController] gimmick onClick() succeeded", new Object[0]);
                  } catch (Throwable var24) {
                     CobblemonBattleInfoClient.debug("[CustomController] gimmick onClick() failed: {}", new Object[]{var24.getMessage()});
                  }
               }

               if (!gimmickHandled) {
                  try {
                     Method m = selectedTile.getClass().getMethod("mouseClicked", double.class, double.class, int.class);
                     Object res = m.invoke(selectedTile, (double)centerX, (double)centerY, 0);
                     if (res instanceof Boolean && (Boolean)res) {
                        gimmickHandled = true;
                        CobblemonBattleInfoClient.debug("[CustomController] gimmick mouseClicked succeeded", new Object[0]);
                     }
                  } catch (Throwable var23) {
                     CobblemonBattleInfoClient.debug("[CustomController] gimmick mouseClicked failed: {}", new Object[]{var23.getMessage()});
                  }
               }

               if (!gimmickHandled) {
                  try {
                     Method m2 = selectedTile.getClass().getMethod("mousePrimaryClicked", double.class, double.class);
                     Object res2 = m2.invoke(selectedTile, (double)centerX, (double)centerY);
                     if (res2 instanceof Boolean && (Boolean)res2) {
                        gimmickHandled = true;
                        CobblemonBattleInfoClient.debug("[CustomController] gimmick mousePrimaryClicked succeeded", new Object[0]);
                     }
                  } catch (Throwable var22) {
                     CobblemonBattleInfoClient.debug("[CustomController] gimmick mousePrimaryClicked failed: {}", new Object[]{var22.getMessage()});
                  }
               }

               if (!gimmickHandled) {
                  try {
                     Method getOn = selectedTile.getClass().getMethod("getOnClick");
                     Object onClick = getOn.invoke(selectedTile);
                     if (onClick != null) {
                        Method invokeMethod = onClick.getClass().getMethod("invoke");
                        invokeMethod.invoke(onClick);
                        gimmickHandled = true;
                        CobblemonBattleInfoClient.debug("[CustomController] gimmick getOnClick() succeeded", new Object[0]);
                     }
                  } catch (Throwable var21) {
                     CobblemonBattleInfoClient.debug("[CustomController] gimmick getOnClick failed: {}", new Object[]{var21.getMessage()});
                  }
               }

               if (!gimmickHandled) {
                  try {
                     gui.method_25402((double)centerX, (double)centerY, 0);
                     gimmickHandled = true;
                     CobblemonBattleInfoClient.debug("[CustomController] gimmick gui.mouseClicked fallback succeeded", new Object[0]);
                  } catch (Throwable var20) {
                     CobblemonBattleInfoClient.debug("[CustomController] gimmick gui.mouseClicked failed: {}", new Object[]{var20.getMessage()});
                  }
               }

               if (gimmickHandled) {
                  CobblemonBattleInfoClient.debug("[CustomController] Gimmick button activated successfully", new Object[0]);
               } else {
                  CobblemonBattleInfoClient.LOGGER.warn("[CustomController] Failed to activate gimmick button");
               }

               return;
            }

            if (currentSelectionType.equals("moves")) {
               try {
                  gui.method_25402((double)centerX, (double)centerY, 0);
                  clicked = true;
                  selectedButton = 0;
                  CobblemonBattleInfoClient.debug("[CustomController] moves gui.mouseClicked routing succeeded!", new Object[0]);
               } catch (Throwable var36) {
                  CobblemonBattleInfoClient.debug("[CustomController] moves gui.mouseClicked routing failed: {}", new Object[]{var36.getMessage()});
               }
            }

            if (!clicked && currentSelectionType.equals("switch")) {
               rememberSwitchSelectionFromTile(selectedTile);

               try {
                  Method onClickMethod = selectedTile.getClass().getMethod("onClick");
                  onClickMethod.invoke(selectedTile);
                  clicked = true;
                  selectedButton = 0;
                  CobblemonBattleInfoClient.debug("[CustomController] onClick() succeeded!", new Object[0]);
               } catch (Throwable var35) {
                  CobblemonBattleInfoClient.debug("[CustomController] onClick() failed: {}", new Object[]{var35.getMessage()});
               }
            }

            if (!clicked) {
               try {
                  Method method = selectedTile.getClass().getMethod("mouseClicked", double.class, double.class, int.class);
                  Object result = method.invoke(selectedTile, (double)centerX, (double)centerY, 0);
                  if (result instanceof Boolean && (Boolean)result) {
                     clicked = true;
                     selectedButton = 0;
                     CobblemonBattleInfoClient.debug("[CustomController] mouseClicked succeeded!", new Object[0]);
                  }
               } catch (Throwable var34) {
                  CobblemonBattleInfoClient.debug("[CustomController] mouseClicked failed: {}", new Object[]{var34.getMessage()});
               }
            }

            if (!clicked) {
               try {
                  Method method = selectedTile.getClass().getMethod("mousePrimaryClicked", double.class, double.class);
                  Object result = method.invoke(selectedTile, (double)centerX, (double)centerY);
                  if (result instanceof Boolean && (Boolean)result) {
                     clicked = true;
                     selectedButton = 0;
                     CobblemonBattleInfoClient.debug("[CustomController] mousePrimaryClicked succeeded!", new Object[0]);
                  }
               } catch (Throwable var33) {
                  CobblemonBattleInfoClient.debug("[CustomController] mousePrimaryClicked failed: {}", new Object[]{var33.getMessage()});
               }
            }

            if (!clicked) {
               try {
                  Method method = selectedTile.getClass().getMethod("getOnClick");
                  Object onClick = method.invoke(selectedTile);
                  if (onClick != null) {
                     Method invokeMethod = onClick.getClass().getMethod("invoke");
                     invokeMethod.invoke(onClick);
                     clicked = true;
                     selectedButton = 0;
                     CobblemonBattleInfoClient.debug("[CustomController] getOnClick().invoke() succeeded!", new Object[0]);
                  }
               } catch (Throwable var32) {
                  CobblemonBattleInfoClient.debug("[CustomController] getOnClick failed: {}", new Object[]{var32.getMessage()});
               }
            }

            if (!clicked) {
               try {
                  gui.method_25402((double)centerX, (double)centerY, 0);
                  clicked = true;
                  selectedButton = 0;
                  CobblemonBattleInfoClient.debug("[CustomController] gui.mouseClicked fallback succeeded!", new Object[0]);
               } catch (Throwable var31) {
                  CobblemonBattleInfoClient.debug("[CustomController] gui.mouseClicked failed: {}", new Object[]{var31.getMessage()});
               }
            }

            if (clicked) {
               return;
            }
         }

         CobblemonBattleInfoClient.debug("[CustomController] Using fallback click for button {}", new Object[]{selectedButton});
         activateButtonFallback(gui);
      } catch (Throwable var38) {
         CobblemonBattleInfoClient.LOGGER.error("[CustomController] Failed to activate button", var38);
         activateButtonFallback(gui);
      }
   }

   private static void activateButtonFallback(class_437 gui) {
      try {
         int OPTION_ROOT_X = 6;
         int OPTION_VERTICAL_OFFSET = 101;
         int OPTION_HORIZONTAL_SPACING = 3;
         int OPTION_VERTICAL_SPACING = 3;
         int TILE_WIDTH = 139;
         int TILE_HEIGHT = 39;

         try {
            Class<?> battleGUIClass = gui.getClass();
            Field rootXField = battleGUIClass.getDeclaredField("OPTION_ROOT_X");
            rootXField.setAccessible(true);
            OPTION_ROOT_X = (Integer)rootXField.get(null);
            Field vertOffsetField = battleGUIClass.getDeclaredField("OPTION_VERTICAL_OFFSET");
            vertOffsetField.setAccessible(true);
            OPTION_VERTICAL_OFFSET = (Integer)vertOffsetField.get(null);
            Field hSpacingField = battleGUIClass.getDeclaredField("OPTION_HORIZONTAL_SPACING");
            hSpacingField.setAccessible(true);
            OPTION_HORIZONTAL_SPACING = (Integer)hSpacingField.get(null);
            Field vSpacingField = battleGUIClass.getDeclaredField("OPTION_VERTICAL_SPACING");
            vSpacingField.setAccessible(true);
            OPTION_VERTICAL_SPACING = (Integer)vSpacingField.get(null);
         } catch (Throwable var16) {
         }

         try {
            Class<?> tileClass = Class.forName("com.cobblemon.mod.common.client.gui.battle.widgets.BattleOptionTile");
            Field widthField = tileClass.getDeclaredField("OPTION_WIDTH");
            widthField.setAccessible(true);
            TILE_WIDTH = (Integer)widthField.get(null);
            Field heightField = tileClass.getDeclaredField("OPTION_HEIGHT");
            heightField.setAccessible(true);
            TILE_HEIGHT = (Integer)heightField.get(null);
         } catch (Throwable var15) {
         }

         int screenHeight = class_310.method_1551().method_22683().method_4502();
         int row = selectedButton / 2;
         int col = selectedButton % 2;
         int bottomRowBottom = screenHeight - 10;
         int y;
         if (row == 1) {
            y = bottomRowBottom - TILE_HEIGHT;
         } else {
            y = bottomRowBottom - TILE_HEIGHT - OPTION_VERTICAL_SPACING - TILE_HEIGHT;
         }

         int x = OPTION_ROOT_X + col * (TILE_WIDTH + OPTION_HORIZONTAL_SPACING);
         int centerX = x + TILE_WIDTH / 2;
         int centerY = y + TILE_HEIGHT / 2;
         CobblemonBattleInfoClient.debug("[CustomController] Fallback ACTIVATE {} at ({}, {})", new Object[]{getButtonName(selectedButton), centerX, centerY});
         invokeMouseClicked(gui, (double)centerX, (double)centerY, 0);
      } catch (Throwable var17) {
         CobblemonBattleInfoClient.LOGGER.error("[CustomController] Fallback activation failed", var17);
      }
   }

   private static void invokeMouseClicked(class_437 gui, double x, double y, int button) {
      try {
         gui.method_25402(x, y, button);
         CobblemonBattleInfoClient.debug("[CustomController] mouseClicked({}, {}, {}) called successfully", new Object[]{x, y, button});
      } catch (Throwable var7) {
         CobblemonBattleInfoClient.LOGGER.error("[CustomController] Failed to invoke mouseClicked: {}", var7.getMessage());
      }
   }

   private static void onBattleStart() {
      selectedButton = 0;
      currentSelectionType = "none";
      lastActionSelectionClassName = "";
      targetGridColumns = 2;
      hasAutoExpandedLog = false;
      switchTooltipCache.clear();
      switchTypeIconCache.clear();
      clearTileCaches();
      cachedOpponentEntries = null;
      opponentEntriesCacheExpiry = 0L;
      LocalBattlePartyTracker.clear();
      CobblemonBattleInfoClient.debug("[CustomController] Battle started, selected Fight", new Object[0]);
      BattleLogger.onBattleStart();
   }

   private static void onBattleEnd() {
      selectedButton = 0;
      currentSelectionType = "none";
      lastActionSelectionClassName = "";
      targetGridColumns = 2;
      hasAutoExpandedLog = false;
      switchTooltipCache.clear();
      switchTypeIconCache.clear();
      clearTileCaches();
      cachedOpponentEntries = null;
      opponentEntriesCacheExpiry = 0L;
      LocalBattlePartyTracker.clear();
      CobblemonBattleInfoClient.debug("[CustomController] Battle ended", new Object[0]);
      String result = determineBattleResult();
      BattleLogger.onBattleEnd(result);
   }

   private static String determineBattleResult() {
      try {
         return "Battle Ended";
      } catch (Exception var1) {
         return "Unknown";
      }
   }

   private static void autoExpandBattleLog(class_437 gui) {
      try {
         Field messagePaneField = null;

         for (Class<?> currentClass = gui.getClass(); currentClass != null && currentClass != Object.class; currentClass = currentClass.getSuperclass()) {
            for (Field f : currentClass.getDeclaredFields()) {
               String fieldName = f.getName().toLowerCase();
               if (fieldName.contains("message") || fieldName.contains("log")) {
                  f.setAccessible(true);
                  Object value = f.get(gui);
                  if (value != null && value.getClass().getName().contains("BattleMessagePane")) {
                     setMessagePaneExpanded(value);
                     CobblemonBattleInfoClient.debug("[CustomController] Auto-expanded battle log via field '{}'", new Object[]{f.getName()});
                     return;
                  }
               }
            }
         }

         for (Class<?> var11 = gui.getClass(); var11 != null && var11 != Object.class; var11 = var11.getSuperclass()) {
            for (Field fx : var11.getDeclaredFields()) {
               try {
                  fx.setAccessible(true);
                  Object value = fx.get(gui);
                  if (value != null && value.getClass().getName().contains("BattleMessagePane")) {
                     setMessagePaneExpanded(value);
                     CobblemonBattleInfoClient.debug("[CustomController] Auto-expanded battle log via field '{}'", new Object[]{fx.getName()});
                     return;
                  }
               } catch (Throwable var9) {
               }
            }
         }

         CobblemonBattleInfoClient.debug("[CustomController] Could not find BattleMessagePane to auto-expand", new Object[0]);
      } catch (Throwable var10) {
         CobblemonBattleInfoClient.LOGGER.error("[CustomController] Error auto-expanding battle log: {}", var10.getMessage());
      }
   }

   private static void setMessagePaneExpanded(Object messagePane) {
      try {
         for (Class<?> currentClass = messagePane.getClass();
            currentClass != null && currentClass != Object.class;
            CobblemonBattleInfoClient.debug("[CustomController] Could not find minimised field to set", new Object[0])
         ) {
            for (Field f : currentClass.getDeclaredFields()) {
               String fieldName = f.getName().toLowerCase();
               if (fieldName.contains("minimi") || fieldName.equals("expanded") || fieldName.contains("collapse")) {
                  f.setAccessible(true);
                  if (f.getType() == boolean.class || f.getType() == Boolean.class) {
                     boolean currentValue = f.getBoolean(messagePane);
                     if (fieldName.contains("expand")) {
                        if (!currentValue) {
                           f.setBoolean(messagePane, true);
                           CobblemonBattleInfoClient.debug("[CustomController] Set {} to true", new Object[]{f.getName()});
                        }
                     } else if (currentValue) {
                        f.setBoolean(messagePane, false);
                        CobblemonBattleInfoClient.debug("[CustomController] Set {} to false", new Object[]{f.getName()});
                     }

                     return;
                  }
               }
            }

            currentClass = currentClass.getSuperclass();

            try {
               Method toggleMethod = messagePane.getClass().getMethod("toggleMinimised");

               try {
                  Method isMinimisedMethod = messagePane.getClass().getMethod("isMinimised");
                  boolean isMinimised = (Boolean)isMinimisedMethod.invoke(messagePane);
                  if (isMinimised) {
                     toggleMethod.invoke(messagePane);
                     CobblemonBattleInfoClient.debug("[CustomController] Toggled battle log to expanded via toggleMinimised()", new Object[0]);
                  }
               } catch (NoSuchMethodException var8) {
               }
            } catch (NoSuchMethodException var9) {
            }
         }
      } catch (Throwable var10) {
         CobblemonBattleInfoClient.LOGGER.error("[CustomController] Error setting message pane expanded: {}", var10.getMessage());
      }
   }

   private static String getButtonName(int index) {
      switch (index) {
         case 0:
            return "Fight";
         case 1:
            return "Bag";
         case 2:
            return "Switch";
         case 3:
            return "Run";
         default:
            return "Unknown";
      }
   }

   private static int getFieldInt(Object obj, String fieldName, int defaultValue) {
      try {
         Field field = obj.getClass().getDeclaredField(fieldName);
         field.setAccessible(true);
         Object value = field.get(obj);
         if (value instanceof Integer) {
            return (Integer)value;
         }
      } catch (Throwable var5) {
      }

      return defaultValue;
   }

   private static Field findFieldInHierarchy(Class<?> startClass, String fieldName) {
      for (Class<?> currentClass = startClass; currentClass != null && currentClass != Object.class; currentClass = currentClass.getSuperclass()) {
         try {
            return currentClass.getDeclaredField(fieldName);
         } catch (NoSuchFieldException var4) {
         }
      }

      return null;
   }

   private static boolean isMoveLikeTile(Object tile) {
      if (tile == null) {
         return false;
      } else {
         String tileClassName = tile.getClass().getName();
         return tileClassName.contains("MoveTile") || tileClassName.contains("GimmickTile") || tileClassName.contains("DynamaxTile");
      }
   }

   private static int countRenderableMoveTiles(Object[] tiles) {
      if (tiles != null && tiles.length != 0) {
         int count = 0;

         for (Object tile : tiles) {
            if (isMoveLikeTile(tile)) {
               count++;
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   private static boolean hasRenderableMoveTiles(Object[] tiles) {
      return countRenderableMoveTiles(tiles) > 0;
   }

   private static void drawFancyTooltipBox(class_332 graphics, int x, int y, int width, int height, int backgroundColor, int borderStart, int borderEnd) {
      Theme theme = BattleUiThemePalette.getActiveBattleLogTheme();
      int accent = mixColors(borderStart, borderEnd, 0.28F);
      int outerBorder = withAlpha(mixColors(theme.borderHighlight(), accent, 0.42F), 244);
      int lowerBorder = withAlpha(mixColors(theme.borderColor(), darkenColor(accent, 0.65F), 0.5F), 234);
      int fillTop = withAlpha(mixColors(theme.headerBgLight(), accent, 0.16F), 234);
      int fillBottom = withAlpha(mixColors(theme.panelBgBottom(), accent, 0.1F), 240);
      int sheen = withAlpha(mixColors(theme.headerText(), -1, 0.34F), 84);
      int accentLine = withAlpha(mixColors(accent, -1, 0.1F), 188);
      int innerShadow = withAlpha(mixColors(theme.panelBgBottom(), -16447732, 0.46F), 138);
      int accentTab = withAlpha(mixColors(accent, -1, 0.04F), 206);
      drawRoundedGradientBox(graphics, x, y, width, height, outerBorder, lowerBorder, outerBorder, lowerBorder);
      graphics.method_25296(x + 1, y + 1, x + width - 1, y + height - 1, fillTop, fillBottom);
      graphics.method_25294(x + 3, y + 1, x + width - 3, y + 3, sheen);
      graphics.method_25294(x + 1, y + height - 2, x + width - 1, y + height - 1, innerShadow);
      graphics.method_25294(x + 2, y + height - 3, x + width - 2, y + height - 2, accentLine);
      graphics.method_25294(x + 2, y + 2, x + 4, y + height - 2, accentTab);
   }

   private static int darkenColor(int color, float factor) {
      int a = color >> 24 & 0xFF;
      int r = (int)((float)(color >> 16 & 0xFF) * factor);
      int g = (int)((float)(color >> 8 & 0xFF) * factor);
      int b = (int)((float)(color & 0xFF) * factor);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static int mixColors(int color1, int color2, float ratio) {
      int a1 = color1 >> 24 & 0xFF;
      int r1 = color1 >> 16 & 0xFF;
      int g1 = color1 >> 8 & 0xFF;
      int b1 = color1 & 0xFF;
      int a2 = color2 >> 24 & 0xFF;
      int r2 = color2 >> 16 & 0xFF;
      int g2 = color2 >> 8 & 0xFF;
      int b2 = color2 & 0xFF;
      int a = (int)((float)a1 * (1.0F - ratio) + (float)a2 * ratio);
      int r = (int)((float)r1 * (1.0F - ratio) + (float)r2 * ratio);
      int g = (int)((float)g1 * (1.0F - ratio) + (float)g2 * ratio);
      int b = (int)((float)b1 * (1.0F - ratio) + (float)b2 * ratio);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static boolean isUpPressed() {
      class_310 mc = class_310.method_1551();
      long window = mc.method_22683().method_4490();
      if (GLFW.glfwGetKey(window, 265) == 1) {
         return true;
      } else if (GLFW.glfwGetKey(window, 87) == 1) {
         return true;
      } else if (mc.field_1690.field_1894.method_1434()) {
         return true;
      } else if (isGlfwGamepadUp()) {
         return true;
      } else {
         try {
            return isControllerButtonPressed("DPAD_UP") || isControllerAxisUp();
         } catch (Throwable var5) {
            try {
               return isControlifyUp();
            } catch (Throwable var4) {
               return false;
            }
         }
      }
   }

   private static boolean isDownPressed() {
      class_310 mc = class_310.method_1551();
      long window = mc.method_22683().method_4490();
      if (GLFW.glfwGetKey(window, 264) == 1) {
         return true;
      } else if (GLFW.glfwGetKey(window, 83) == 1) {
         return true;
      } else if (mc.field_1690.field_1881.method_1434()) {
         return true;
      } else if (isGlfwGamepadDown()) {
         return true;
      } else {
         try {
            return isControllerButtonPressed("DPAD_DOWN") || isControllerAxisDown();
         } catch (Throwable var4) {
            return false;
         }
      }
   }

   private static boolean isLeftPressed() {
      class_310 mc = class_310.method_1551();
      long window = mc.method_22683().method_4490();
      if (GLFW.glfwGetKey(window, 263) == 1) {
         return true;
      } else if (GLFW.glfwGetKey(window, 65) == 1) {
         return true;
      } else if (mc.field_1690.field_1913.method_1434()) {
         return true;
      } else if (isGlfwGamepadLeft()) {
         return true;
      } else {
         try {
            return isControllerButtonPressed("DPAD_LEFT") || isControllerAxisLeft();
         } catch (Throwable var4) {
            return false;
         }
      }
   }

   private static boolean isRightPressed() {
      class_310 mc = class_310.method_1551();
      long window = mc.method_22683().method_4490();
      if (GLFW.glfwGetKey(window, 262) == 1) {
         return true;
      } else if (GLFW.glfwGetKey(window, 68) == 1) {
         return true;
      } else if (mc.field_1690.field_1849.method_1434()) {
         return true;
      } else if (isGlfwGamepadRight()) {
         return true;
      } else {
         try {
            return isControllerButtonPressed("DPAD_RIGHT") || isControllerAxisRight();
         } catch (Throwable var4) {
            return false;
         }
      }
   }

   private static boolean isSelectPressed() {
      class_310 mc = class_310.method_1551();
      long window = mc.method_22683().method_4490();
      if (GLFW.glfwGetKey(window, 257) == 1) {
         return true;
      } else if (GLFW.glfwGetKey(window, 335) == 1) {
         return true;
      } else if (GLFW.glfwGetKey(window, 32) == 1) {
         return true;
      } else if (mc.field_1690.field_1904.method_1434()) {
         return true;
      } else if (isGlfwGamepadButtonA()) {
         return true;
      } else {
         try {
            return isControllerButtonPressed("A") || isControllerButtonPressed("SOUTH");
         } catch (Throwable var4) {
            return false;
         }
      }
   }

   private static boolean isBackPressed() {
      class_310 mc = class_310.method_1551();
      long window = mc.method_22683().method_4490();
      int backKey = BattleExtrasConfig.getBattleBackKey();
      if (backKey != -1 && GLFW.glfwGetKey(window, backKey) == 1) {
         return true;
      } else if (isGlfwGamepadButtonB()) {
         return true;
      } else {
         try {
            return isControllerButtonPressed("B") || isControllerButtonPressed("EAST");
         } catch (Throwable var5) {
            return false;
         }
      }
   }

   private static int findConnectedGamepad() {
      for (int jid = 0; jid <= 15; jid++) {
         if (GLFW.glfwJoystickPresent(jid) && GLFW.glfwJoystickIsGamepad(jid)) {
            return jid;
         }
      }

      return -1;
   }

   private static boolean isGlfwGamepadUp() {
      int gamepad = findConnectedGamepad();
      if (gamepad == -1) {
         return false;
      } else {
         try {
            GLFWGamepadState state = GLFWGamepadState.create();
            if (GLFW.glfwGetGamepadState(gamepad, state)) {
               if (state.buttons(11) == 1) {
                  return true;
               }

               if (state.axes(1) < -0.5F) {
                  return true;
               }
            }
         } catch (Throwable var2) {
         }

         return false;
      }
   }

   private static boolean isGlfwGamepadDown() {
      int gamepad = findConnectedGamepad();
      if (gamepad == -1) {
         return false;
      } else {
         try {
            GLFWGamepadState state = GLFWGamepadState.create();
            if (GLFW.glfwGetGamepadState(gamepad, state)) {
               if (state.buttons(13) == 1) {
                  return true;
               }

               if (state.axes(1) > 0.5F) {
                  return true;
               }
            }
         } catch (Throwable var2) {
         }

         return false;
      }
   }

   private static boolean isGlfwGamepadLeft() {
      int gamepad = findConnectedGamepad();
      if (gamepad == -1) {
         return false;
      } else {
         try {
            GLFWGamepadState state = GLFWGamepadState.create();
            if (GLFW.glfwGetGamepadState(gamepad, state)) {
               if (state.buttons(14) == 1) {
                  return true;
               }

               if (state.axes(0) < -0.5F) {
                  return true;
               }
            }
         } catch (Throwable var2) {
         }

         return false;
      }
   }

   private static boolean isGlfwGamepadRight() {
      int gamepad = findConnectedGamepad();
      if (gamepad == -1) {
         return false;
      } else {
         try {
            GLFWGamepadState state = GLFWGamepadState.create();
            if (GLFW.glfwGetGamepadState(gamepad, state)) {
               if (state.buttons(12) == 1) {
                  return true;
               }

               if (state.axes(0) > 0.5F) {
                  return true;
               }
            }
         } catch (Throwable var2) {
         }

         return false;
      }
   }

   private static boolean isGlfwGamepadButtonA() {
      int gamepad = findConnectedGamepad();
      if (gamepad == -1) {
         return false;
      } else {
         try {
            GLFWGamepadState state = GLFWGamepadState.create();
            if (GLFW.glfwGetGamepadState(gamepad, state)) {
               return state.buttons(0) == 1;
            }
         } catch (Throwable var2) {
         }

         return false;
      }
   }

   private static boolean isGlfwGamepadButtonB() {
      int gamepad = findConnectedGamepad();
      if (gamepad == -1) {
         return false;
      } else {
         try {
            GLFWGamepadState state = GLFWGamepadState.create();
            if (GLFW.glfwGetGamepadState(gamepad, state)) {
               return state.buttons(1) == 1;
            }
         } catch (Throwable var2) {
         }

         return false;
      }
   }

   private static boolean isControlifyUp() {
      return false;
   }

   private static boolean isControlifyDown() {
      return false;
   }

   private static boolean isControlifyLeft() {
      return false;
   }

   private static boolean isControlifyRight() {
      return false;
   }

   private static boolean isControlifyA() {
      return false;
   }

   private static boolean isControlifyB() {
      return false;
   }

   private static void toggleBattleMinimised() {
      try {
         Class<?> cobblemonClientClass = Class.forName("com.cobblemon.mod.common.client.CobblemonClient");
         Field battleField = cobblemonClientClass.getDeclaredField("battle");
         battleField.setAccessible(true);
         Object battle = battleField.get(null);
         if (battle != null) {
            Field minimisedField = battle.getClass().getDeclaredField("minimised");
            minimisedField.setAccessible(true);
            boolean currentMinimised = minimisedField.getBoolean(battle);
            minimisedField.setBoolean(battle, !currentMinimised);
            CobblemonBattleInfoClient.debug("[CustomController] Toggled battle minimised: {} -> {}", new Object[]{currentMinimised, !currentMinimised});
            if (currentMinimised && !class_310.method_1551().field_1690.field_1842) {
               Class<?> battleGUIClass = Class.forName("com.cobblemon.mod.common.client.gui.battle.BattleGUI");
               Object battleGUI = battleGUIClass.getDeclaredConstructor().newInstance();
               class_310.method_1551().method_1507((class_437)battleGUI);
               CobblemonBattleInfoClient.debug("[CustomController] Opened BattleGUI after un-minimising", new Object[0]);
            } else {
               class_310.method_1551().method_1507(null);
            }
         } else {
            CobblemonBattleInfoClient.debug("[CustomController] No active battle to toggle minimised state", new Object[0]);
            class_310.method_1551().method_1507(null);
         }
      } catch (Throwable var7) {
         CobblemonBattleInfoClient.LOGGER.error("[CustomController] Failed to toggle battle minimised: {}", var7.getMessage());
         class_310.method_1551().method_1507(null);
      }
   }

   private static void openBattleGUI() {
      try {
         Class<?> cobblemonClientClass = Class.forName("com.cobblemon.mod.common.client.CobblemonClient");
         Field battleField = cobblemonClientClass.getDeclaredField("battle");
         battleField.setAccessible(true);
         Object battle = battleField.get(null);
         if (battle != null) {
            Field minimisedField = battle.getClass().getDeclaredField("minimised");
            minimisedField.setAccessible(true);
            minimisedField.setBoolean(battle, false);
            CobblemonBattleInfoClient.debug("[CustomController] Set battle minimised to false", new Object[0]);
            if (!class_310.method_1551().field_1690.field_1842) {
               Class<?> battleGUIClass = Class.forName("com.cobblemon.mod.common.client.gui.battle.BattleGUI");
               Object battleGUI = battleGUIClass.getDeclaredConstructor().newInstance();
               class_310.method_1551().method_1507((class_437)battleGUI);
               CobblemonBattleInfoClient.debug("[CustomController] Opened BattleGUI", new Object[0]);
            }
         } else {
            CobblemonBattleInfoClient.debug("[CustomController] No active battle to open GUI for", new Object[0]);
         }
      } catch (Throwable var6) {
         CobblemonBattleInfoClient.LOGGER.error("[CustomController] Failed to open BattleGUI: {}", var6.getMessage());
      }
   }

   private static void goBack(class_437 gui) {
      try {
         if (currentSelectionType.equals("general") || currentSelectionType.equals("none")) {
            CobblemonBattleInfoClient.debug("[CustomController] On main menu, toggling battle minimised mode", new Object[0]);
            toggleBattleMinimised();
            minimisedBackPressed = true;
            return;
         }

         Object actionSelection = getActionSelection(gui);
         if (actionSelection == null) {
            CobblemonBattleInfoClient.debug("[CustomController] No action selection, toggling battle minimised mode", new Object[0]);
            toggleBattleMinimised();
            minimisedBackPressed = true;
            return;
         }

         try {
            Method getBattleGUIMethod = actionSelection.getClass().getMethod("getBattleGUI");
            Object battleGUI = getBattleGUIMethod.invoke(actionSelection);
            if (battleGUI != null) {
               Method getActorMethod = battleGUI.getClass().getMethod("getActor");
               Object actor = getActorMethod.invoke(battleGUI);
               Method getRequestMethod = actionSelection.getClass().getMethod("getRequest");
               Object request = getRequestMethod.invoke(actionSelection);
               if (actor != null && request != null) {
                  Method deriveMethod = battleGUI.getClass().getMethod("deriveRootActionSelection", actor.getClass(), request.getClass());
                  Object rootSelection = deriveMethod.invoke(battleGUI, actor, request);
                  if (rootSelection != null) {
                     Method changeMethod = battleGUI.getClass().getMethod("changeActionSelection", rootSelection.getClass().getSuperclass());
                     changeMethod.invoke(battleGUI, rootSelection);
                     selectedButton = 0;
                     CobblemonBattleInfoClient.debug("[CustomController] Went back to main menu!", new Object[0]);
                     return;
                  }
               }
            }
         } catch (Throwable var12) {
            CobblemonBattleInfoClient.debug("[CustomController] deriveRootActionSelection failed: {}", new Object[]{var12.getMessage()});
         }

         try {
            Method getBackButtonMethod = actionSelection.getClass().getMethod("getBackButton");
            Object backButton = getBackButtonMethod.invoke(actionSelection);
            if (backButton != null) {
               Method getXMethod = backButton.getClass().getMethod("getX");
               Method getYMethod = backButton.getClass().getMethod("getY");
               float x = (Float)getXMethod.invoke(backButton);
               float y = (Float)getYMethod.invoke(backButton);
               gui.method_25402((double)(x + 10.0F), (double)(y + 10.0F), 0);
               selectedButton = 0;
               CobblemonBattleInfoClient.debug("[CustomController] Clicked back button at ({}, {})", new Object[]{x, y});
               return;
            }
         } catch (Throwable var11) {
            CobblemonBattleInfoClient.debug("[CustomController] Back button click failed: {}", new Object[]{var11.getMessage()});
         }

         CobblemonBattleInfoClient.debug("[CustomController] Could not go back", new Object[0]);
      } catch (Throwable var13) {
         CobblemonBattleInfoClient.LOGGER.error("[CustomController] Error going back: {}", var13.getMessage());
      }
   }

   private static boolean isControllerButtonPressed(String buttonName) {
      try {
         Class<?> inputManager = Class.forName("eu.midnightdust.midnightcontrols.client.controller.InputManager");
         Class<?> controllerButton = Class.forName("eu.midnightdust.midnightcontrols.client.enums.ControllerButton");
         Object button = Enum.valueOf(controllerButton, buttonName);
         Method method = inputManager.getMethod("isButtonPressed", controllerButton);
         return (Boolean)method.invoke(null, button);
      } catch (Throwable var5) {
         return false;
      }
   }

   private static boolean isControllerAxisUp() {
      try {
         Class<?> inputManager = Class.forName("eu.midnightdust.midnightcontrols.client.controller.InputManager");
         Method method = inputManager.getMethod("getLeftStickY");
         float y = (Float)method.invoke(null);
         return y < -0.5F;
      } catch (Throwable var3) {
         return false;
      }
   }

   private static boolean isControllerAxisDown() {
      try {
         Class<?> inputManager = Class.forName("eu.midnightdust.midnightcontrols.client.controller.InputManager");
         Method method = inputManager.getMethod("getLeftStickY");
         float y = (Float)method.invoke(null);
         return y > 0.5F;
      } catch (Throwable var3) {
         return false;
      }
   }

   private static boolean isControllerAxisLeft() {
      try {
         Class<?> inputManager = Class.forName("eu.midnightdust.midnightcontrols.client.controller.InputManager");
         Method method = inputManager.getMethod("getLeftStickX");
         float x = (Float)method.invoke(null);
         return x < -0.5F;
      } catch (Throwable var3) {
         return false;
      }
   }

   private static boolean isControllerAxisRight() {
      try {
         Class<?> inputManager = Class.forName("eu.midnightdust.midnightcontrols.client.controller.InputManager");
         Method method = inputManager.getMethod("getLeftStickX");
         float x = (Float)method.invoke(null);
         return x > 0.5F;
      } catch (Throwable var3) {
         return false;
      }
   }

   public static boolean isTileControllerSelected(Object tile) {
      if (!BattleExtrasConfig.isControllerNavigationEnabled()) {
         return false;
      } else if (tile == null) {
         return false;
      } else if (!isMovesSelectionActive()) {
         return false;
      } else if (cachedTiles == null || cachedTiles.length == 0) {
         return false;
      } else {
         return selectedButton >= 0 && selectedButton < cachedTiles.length ? tile == cachedTiles[selectedButton] : false;
      }
   }

   public static int[] getSelectedTilePosition() {
      if (!isMovesSelectionActive()) {
         return null;
      } else if (cachedTiles == null || cachedTiles.length == 0) {
         return null;
      } else if (selectedButton >= 0 && selectedButton < cachedTiles.length) {
         Object tile = cachedTiles[selectedButton];
         if (tile == null) {
            return null;
         } else {
            try {
               int x = getTileX(tile);
               int y = getTileY(tile);
               int width = getTileWidth(tile);
               int height = getTileHeight(tile);
               if (x > 0 && y > 0 && width > 0 && height > 0) {
                  return new int[]{x, y, width, height};
               }
            } catch (Throwable var5) {
            }

            return null;
         }
      } else {
         return null;
      }
   }

   public static int[] getTilePosition(Object tile) {
      if (tile == null) {
         return null;
      } else {
         try {
            int x = getTileX(tile);
            int y = getTileY(tile);
            int width = getTileWidth(tile);
            int height = getTileHeight(tile);
            if (x > 0 && y > 0 && width > 0 && height > 0) {
               return new int[]{x, y, width, height};
            }
         } catch (Throwable var5) {
         }

         return null;
      }
   }

   public static boolean isMovesSelectionActive() {
      return hasRenderableMoveTiles(cachedTiles);
   }

   public static int getSelectedTileIndex() {
      if (!isMovesSelectionActive()) {
         return -1;
      } else if (cachedTiles == null || cachedTiles.length == 0) {
         return -1;
      } else {
         return selectedButton >= 0 && selectedButton < cachedTiles.length ? selectedButton : -1;
      }
   }

   public static boolean isMouseHoveringAnyMoveTile() {
      try {
         if (!isMovesSelectionActive()) {
            return false;
         }

         if (cachedTiles == null || cachedTiles.length == 0) {
            return false;
         }

         class_310 mc = class_310.method_1551();
         if (mc == null || mc.method_22683() == null) {
            return false;
         }

         double mouseX = mc.field_1729.method_1603() * (double)mc.method_22683().method_4486() / (double)mc.method_22683().method_4480();
         double mouseY = mc.field_1729.method_1604() * (double)mc.method_22683().method_4502() / (double)mc.method_22683().method_4507();

         for (Object tile : cachedTiles) {
            if (tile != null) {
               int x = getTileX(tile);
               int y = getTileY(tile);
               int w = getTileWidth(tile);
               int h = getTileHeight(tile);
               if (w > 0 && h > 0 && mouseX >= (double)x && mouseX <= (double)(x + w) && mouseY >= (double)y && mouseY <= (double)(y + h)) {
                  return true;
               }
            }
         }
      } catch (Throwable var13) {
      }

      return false;
   }

   public static void setBattleGuiClosed() {
      battleGuiClosedTime = System.currentTimeMillis();
   }

   public static boolean shouldSuppressJump() {
      if (!BattleExtrasConfig.isControllerNavigationEnabled()) {
         return false;
      } else {
         class_310 mc = class_310.method_1551();
         ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
         boolean inBattle = battle != null;
         if (wasInBattleForJump && !inBattle) {
            battleGuiClosedTime = System.currentTimeMillis();
         }

         wasInBattleForJump = inBattle;
         if (mc.field_1755 != null && isBattleGUI(mc.field_1755)) {
            return true;
         } else {
            return inBattle ? true : System.currentTimeMillis() - battleGuiClosedTime < 200L;
         }
      }
   }

   private static final class SwitchTypeIconCacheData {
      private final long expiresAt;
      private final String pokemonName;
      private final boolean terastallized;
      private final String teraType;
      private final boolean megaEvolved;
      private final List<ElementalType> types;

      private SwitchTypeIconCacheData(
         long expiresAt, String pokemonName, boolean terastallized, String teraType, boolean megaEvolved, List<ElementalType> types
      ) {
         this.expiresAt = expiresAt;
         this.pokemonName = pokemonName;
         this.terastallized = terastallized;
         this.teraType = teraType;
         this.megaEvolved = megaEvolved;
         this.types = types;
      }
   }
}
