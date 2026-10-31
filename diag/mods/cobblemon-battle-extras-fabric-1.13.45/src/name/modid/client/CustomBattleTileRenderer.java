package name.modid.client;

import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.pokemon.status.PersistentStatus;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import name.modid.client.BattleUiThemePalette.Theme;
import name.modid.client.CustomBattleTileRenderer.ScreenDisplay;
import name.modid.client.CustomBattleTileRenderer.SpeedInfo;
import name.modid.client.CustomBattleTileRenderer.TilePlacement;
import name.modid.client.CustomBattleTileRenderer.TileThemeColors;
import name.modid.platform.Services;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

public final class CustomBattleTileRenderer {
   private static final int COMPACT_TILE_HEIGHT = 32;
   private static final int NORMAL_TILE_HEIGHT = 40;
   private static final int COMPACT_DEFAULT_VERTICAL_SPACING = 34;
   private static final int COMPACT_NATIVE_EXPANDED_VERTICAL_SPACING = 44;
   private static final int COMPACT_EXPANDED_VERTICAL_SPACING = 52;
   private static final class_2960 TYPE_SPRITE_SHEET = class_2960.method_60654("cobblemon:textures/gui/types.png");
   private static final class_2960 MEGA_ICON = class_2960.method_60654("cobblemon-battle-extras:textures/gui/mega_icon.png");
   private static final String MEGA_SHOWDOWN_TERA_PATH = "mega_showdown:textures/gui/summary/tera_types/";
   private static final boolean MEGA_SHOWDOWN_LOADED = Services.PLATFORM.isModLoaded("mega_showdown");
   private static boolean megaShowdownMemoryReflectionChecked = false;
   private static Field megaShowdownBattleHudMemoryField;
   private static Field megaShowdownRenderablePokemonField;
   private static final int TYPE_ICON_SIZE = 36;
   private static final int TYPE_SHEET_WIDTH = 648;
   private static final int TERA_ICON_SIZE = 32;
   private static final int TERA_DISPLAY_SIZE = 12;
   private static final int MEGA_ICON_SIZE = 64;
   private static final int MEGA_DISPLAY_SIZE = 9;
   private static final int MEGA_BADGE_Z_LAYER = 8200;
   private static final int STATUS_TEXT = -1;
   private static final int OWNED_MARK = -1519509;
   private static final int HEADER_HEIGHT = 10;
   private static final String[] SCREEN_DISPLAY_ORDER = new String[]{"Light Screen", "Reflect", "Aurora Veil", "Safeguard", "Mist", "Lucky Chant"};
   private static final int SCREEN_OVERLAY_ALPHA = 120;
   private static final int TYPE_AND_STATUS_Y_OFFSET = 9;
   private static final int TRIPLE_BOTTOM_ATTACHMENT_Y_OFFSET = 4;
   private static final long TILE_RENDER_CACHE_TTL_MS = 250L;
   private static final int MAX_TILE_RENDER_CACHE_ENTRIES = 32;
   private static final int MAX_TEXT_WIDTH_CACHE_ENTRIES = 512;
   private static Method drawPosablePortraitMethod;
   private static boolean drawPosablePortraitResolved = false;
   private static final Map<String, CustomBattleTileRenderer.TileRenderSnapshot> TILE_RENDER_CACHE = new HashMap<>();
   private static final Map<String, Integer> TEXT_WIDTH_CACHE = new HashMap<>();
   private static final Map<UUID, Integer> OPPONENT_TILE_Y_POSITIONS = new HashMap<>();
   private static Theme cachedTileTheme;
   private static TileThemeColors cachedResolvedTileTheme;

   private CustomBattleTileRenderer() {
   }

   public static void renderTile(
      class_332 gui,
      float tickDelta,
      ActiveClientBattlePokemon activeBattlePokemon,
      boolean left,
      int rank,
      PokedexEntryProgress dexState,
      boolean hasCommand,
      boolean isHovered,
      boolean isCompact
   ) {
      if (activeBattlePokemon != null) {
         ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
         ClientBattlePokemon battlePokemon = activeBattlePokemon.getBattlePokemon();
         class_310 mc = class_310.method_1551();
         if (battle != null && battlePokemon != null && mc.field_1724 != null) {
            TilePlacement placement = updateAnimationAndGetPlacement(mc, battle, activeBattlePokemon, left, rank, isCompact, tickDelta);
            int tileX = Math.round(placement.x());
            int tileY = placement.y();
            int tileWidth = placement.width();
            int tileHeight = placement.height();
            int portraitSize = isCompact ? 19 : 28;
            int portraitInset = isCompact ? 4 : 5;
            int portraitX = left ? tileX + portraitInset : tileX + tileWidth - portraitInset - portraitSize;
            int portraitY = tileY + (isCompact ? 5 : 7);
            int contentX = left ? portraitX + portraitSize + 6 : tileX + 6;
            int contentRight = left ? tileX + tileWidth - 6 : portraitX - 6;
            int contentWidth = Math.max(40, contentRight - contentX);
            trackOpponentTileY(activeBattlePokemon, left, tileY);
            float opacity = BattleOpacityHelper.getBattleOpacity();
            if (!(opacity <= 0.01F)) {
               float textScale = isCompact ? 0.8F : 0.85F;
               TileThemeColors colors = resolveTileThemeColors();
               float speedTextScale = Math.max(0.6F, textScale - 0.1F);
               String side = left ? "player" : "opponent";
               Map<String, Integer> sideScreens = BattleExtrasConfig.isScreensDisplayEnabled() ? BattleMessageSubscriber.getScreensForSide(side) : Map.of();
               boolean renderTileScreens = !sideScreens.isEmpty();
               CustomBattleTileRenderer.TileRenderSnapshot snapshot = getTileRenderSnapshot(
                  mc.field_1772, activeBattlePokemon, battlePokemon, side, isCompact, textScale, speedTextScale
               );
               String speciesName = snapshot.speciesName;
               String displayName = snapshot.displayName;
               drawPanel(gui, tileX, tileY, tileWidth, tileHeight, isHovered, hasCommand, opacity, colors);
               drawPortraitFrame(gui, portraitX, portraitY, portraitSize, opacity, colors);
               renderSharedPosablePortrait(gui, battlePokemon.getSpecies(), battlePokemon.getState(), portraitX, portraitY, portraitSize, !left, tickDelta);
               if (renderTileScreens) {
                  renderPortraitScreenOverlay(gui, sideScreens, portraitX, portraitY, portraitSize, opacity);
               }

               if (snapshot.megaEvolved) {
                  renderMegaPortraitBadge(gui, portraitX, portraitY, portraitSize, left, opacity);
               }

               renderNameRow(
                  gui,
                  mc.field_1772,
                  displayName,
                  battlePokemon.getLevel(),
                  battlePokemon.getGender(),
                  dexState == PokedexEntryProgress.CAUGHT,
                  contentX,
                  tileY + (isCompact ? 2 : 3),
                  contentWidth,
                  opacity,
                  colors,
                  textScale
               );
               if (renderTileScreens) {
                  renderScreenStrip(gui, mc.field_1772, sideScreens, contentX, tileY + (isCompact ? 11 : 13), contentWidth, opacity, colors, isCompact, left);
               }

               renderHealthBar(gui, mc.field_1772, battlePokemon, contentX, tileY + (isCompact ? 14 : 18), contentWidth, left, opacity, colors, textScale);
               int bottomRowY = tileY + tileHeight - (isCompact ? 12 : 14);
               SpeedInfo speedInfo = snapshot.speedInfo;
               int speedWidth = 0;
               int speedX = left ? contentRight : contentX;
               if (speedInfo != null) {
                  int speedLabelWidth = getScaledTextWidth(mc.field_1772, "SPE", speedTextScale);
                  int minSpeedWidth = left ? speedLabelWidth + snapshot.speedTextWidth + 12 : speedLabelWidth + snapshot.speedTextWidth + 10;
                  speedWidth = Math.min(Math.max(minSpeedWidth, left ? 32 : 36), 40 + snapshot.speedTextWidth);
                  speedX = left ? contentRight - speedWidth : contentX;
               }

               List<ElementalType> types = snapshot.types;
               int typeWidth = snapshot.typeWidth;
               String statusText = snapshot.statusText;
               int statusWidth = snapshot.statusWidth;
               int typeStatusGap = statusText != null && !statusText.isBlank() ? (left ? 1 : 3) : 3;
               int iconStartX;
               int statusX;
               if (left) {
                  iconStartX = contentX;
                  statusX = contentX + typeWidth + (typeWidth > 0 ? typeStatusGap : 0);
               } else {
                  iconStartX = contentRight - typeWidth;
                  int renderedTypeStartX = iconStartX + 2;
                  int minimumStatusX = speedWidth > 0 ? speedX + speedWidth + 4 : contentX;
                  int preferredStatusX = renderedTypeStartX - (statusWidth > 0 ? statusWidth + 4 : 0);
                  statusX = Math.max(minimumStatusX, preferredStatusX);
               }

               int attachmentYOffset = 9;
               if (isCompact && isTripleOrMoreBattle(battle) && rank == 2) {
                  attachmentYOffset += 4;
               }

               renderTypeIcons(gui, types, left ? iconStartX - 2 : iconStartX + 2, bottomRowY + attachmentYOffset, opacity, snapshot.teraTypeName);
               int bottomRowLimit = left ? (speedWidth > 0 ? speedX - 4 : contentRight) : contentRight;
               if (statusText != null && statusX >= contentX && statusX + statusWidth <= bottomRowLimit) {
                  renderStatusPill(gui, mc.field_1772, statusText, statusX, bottomRowY + 1 + attachmentYOffset, opacity, colors, textScale);
               }

               if (speedInfo != null) {
                  renderSpeedBadge(gui, mc.field_1772, speedInfo, speedX, bottomRowY + 3, speedWidth, left, opacity, colors, speedTextScale);
               }

               if (battle.getBattleFormat() != null
                  && battle.getBattleFormat().getBattleType() != null
                  && battle.getBattleFormat().getBattleType().getActorsPerSide() > 1) {
                  String actorName = activeBattlePokemon.getActor().getDisplayName().getString();
                  renderActorLabel(gui, mc.field_1772, actorName, left ? tileX + 4 : tileX + tileWidth - 4, tileY - 6, left, opacity, colors, textScale);
               }

               PokemonCountRenderer.drawPokemonCount(gui, activeBattlePokemon, left, isCompact);
            }
         }
      }
   }

   private static CustomBattleTileRenderer.TileRenderSnapshot getTileRenderSnapshot(
      class_327 font,
      ActiveClientBattlePokemon activeBattlePokemon,
      ClientBattlePokemon battlePokemon,
      String side,
      boolean isCompact,
      float textScale,
      float speedTextScale
   ) {
      long now = System.currentTimeMillis();
      String speciesName = battlePokemon.getSpecies().getName();
      String displayName = battlePokemon.getDisplayName().getString();
      String cacheKey = buildTileRenderCacheKey(activeBattlePokemon, battlePokemon, side, isCompact);
      CustomBattleTileRenderer.TileRenderSnapshot cached = TILE_RENDER_CACHE.get(cacheKey);
      if (cached != null && cached.expiresAt >= now) {
         return cached;
      } else {
         SpeedInfo speedInfo = resolveSpeedInfo(activeBattlePokemon, battlePokemon, side);
         int speedTextWidth = speedInfo != null ? getScaledTextWidth(font, speedInfo.text(), speedTextScale) : 0;
         List<ElementalType> resolvedTypes = TypeIconRenderer.getDisplayTypes(
            battlePokemon, speciesName, side, battlePokemon.getUuid() != null ? battlePokemon.getUuid().toString() : null
         );
         String teraTypeName = BattleMessageSubscriber.getTeraType(side, speciesName);
         boolean megaEvolved = isMegaEvolvedForTile(battlePokemon, side, speciesName, displayName);
         List<ElementalType> types = resolvedTypes != null ? List.copyOf(resolvedTypes) : List.of();
         String statusText = null;
         PersistentStatus status = battlePokemon.getStatus();
         if (status != null) {
            statusText = status.getShowdownName();
         }

         if ((statusText == null || statusText.isBlank()) && "player".equals(side)) {
            statusText = LocalBattlePartyTracker.getEffectivePlayerStatusId(activeBattlePokemon, battlePokemon);
         }

         int statusWidth = getStatusPillWidth(font, statusText, textScale);
         CustomBattleTileRenderer.TileRenderSnapshot snapshot = new CustomBattleTileRenderer.TileRenderSnapshot(
            now + 250L,
            speciesName,
            displayName,
            speedInfo,
            speedTextWidth,
            types,
            teraTypeName,
            megaEvolved,
            getTypeIconRowWidth(types, teraTypeName),
            statusText,
            statusWidth
         );
         if (TILE_RENDER_CACHE.size() >= 32) {
            TILE_RENDER_CACHE.clear();
         }

         TILE_RENDER_CACHE.put(cacheKey, snapshot);
         return snapshot;
      }
   }

   private static String buildTileRenderCacheKey(
      ActiveClientBattlePokemon activeBattlePokemon, ClientBattlePokemon battlePokemon, String side, boolean isCompact
   ) {
      StringBuilder key = new StringBuilder();
      key.append(side);
      key.append('|');
      if (activeBattlePokemon != null && activeBattlePokemon.getActorShowdownId() != null) {
         key.append(activeBattlePokemon.getActorShowdownId());
      } else {
         key.append('?');
      }

      key.append('|');
      key.append(
         battlePokemon != null && battlePokemon.getUuid() != null
            ? battlePokemon.getUuid()
            : (battlePokemon != null ? battlePokemon.getSpecies().getName() : "unknown")
      );
      key.append('|');
      key.append((char)(isCompact ? '1' : '0'));
      key.append('|');
      key.append((char)(BattleExtrasConfig.isSpeedRangeEnabled() ? '1' : '0'));
      return key.toString();
   }

   private static TilePlacement updateAnimationAndGetPlacement(
      class_310 mc, ClientBattle battle, ActiveClientBattlePokemon activeBattlePokemon, boolean left, int rank, boolean isCompact, float tickDelta
   ) {
      int tileWidth = isCompact ? 128 : 140;
      int tileHeight = isCompact ? 32 : 40;
      int verticalSpacing = isCompact ? getCompactTileVerticalSpacing() : 40;
      int playerNumberOffset = (Character.digit(activeBattlePokemon.getActorShowdownId().charAt(1), 10) - 1) / 2 * 10;
      int slotCount = battle.getBattleFormat().getBattleType().getSlotsPerActor();
      float x = 12.0F + (float)(slotCount - rank - 1) * 4.0F;
      int y = 10
         + rank * verticalSpacing
         + (left ? playerNumberOffset : (battle.getBattleFormat().getBattleType().getActorsPerSide() - 1) * 10 - playerNumberOffset);
      if (!left) {
         x = (float)mc.method_22683().method_4486() - x - (float)tileWidth;
      }

      activeBattlePokemon.setInvisibleX(left ? (float)(-tileWidth) - 1.0F : (float)mc.method_22683().method_4486());
      activeBattlePokemon.setXDisplacement(x);
      activeBattlePokemon.animate(tickDelta);
      return new TilePlacement(activeBattlePokemon.getXDisplacement(), y, tileWidth, tileHeight);
   }

   static TilePlacement calculateTilePlacement(
      class_310 mc, ClientBattle battle, ActiveClientBattlePokemon activeBattlePokemon, boolean left, int rank, boolean isCompact
   ) {
      int tileWidth = isCompact ? 128 : 140;
      int tileHeight = isCompact ? 32 : 40;
      int verticalSpacing = isCompact ? getCompactTileVerticalSpacing() : 40;
      int playerNumberOffset = (Character.digit(activeBattlePokemon.getActorShowdownId().charAt(1), 10) - 1) / 2 * 10;
      int slotCount = battle.getBattleFormat().getBattleType().getSlotsPerActor();
      float x = 12.0F + (float)(slotCount - rank - 1) * 4.0F;
      int y = 10
         + rank * verticalSpacing
         + (left ? playerNumberOffset : (battle.getBattleFormat().getBattleType().getActorsPerSide() - 1) * 10 - playerNumberOffset);
      if (!left) {
         x = (float)mc.method_22683().method_4486() - x - (float)tileWidth;
      }

      return new TilePlacement(x, y, tileWidth, tileHeight);
   }

   public static int getCompactTileVerticalSpacing() {
      if (!useCompactUnderTileStageStripSpacing()) {
         return 34;
      } else {
         return BattleExtrasConfig.useCustomTopBattleTiles() ? 52 : 44;
      }
   }

   private static boolean useCompactUnderTileStageStripSpacing() {
      return BattleExtrasConfig.shouldUseExpandedCompactTileSpacing();
   }

   private static void drawPanel(
      class_332 gui, int x, int y, int width, int height, boolean isHovered, boolean hasCommand, float opacity, TileThemeColors colors
   ) {
      int border = BattleOpacityHelper.applyOpacity(isHovered ? colors.borderHighlight() : colors.border(), opacity);
      int borderLight = BattleOpacityHelper.applyOpacity(colors.borderHighlight(), opacity);
      int panelTop = BattleOpacityHelper.applyOpacity(colors.panelTop(), opacity);
      int panelBottom = BattleOpacityHelper.applyOpacity(colors.panelBottom(), opacity);
      int headerTop = BattleOpacityHelper.applyOpacity(colors.headerTop(), opacity);
      int headerBottom = BattleOpacityHelper.applyOpacity(colors.headerBottom(), opacity);
      int inner = BattleOpacityHelper.applyOpacity(colors.inner(), opacity);
      int innerTop = BattleOpacityHelper.applyOpacity(withAlpha(mixColors(colors.inner(), colors.panelTop(), 0.22F), 118), opacity);
      int innerBottom = BattleOpacityHelper.applyOpacity(withAlpha(mixColors(colors.inner(), colors.panelBottom(), 0.42F), 126), opacity);
      int headerSheen = BattleOpacityHelper.applyOpacity(withAlpha(mixColors(colors.headerTop(), -1, 0.3F), 76), opacity);
      int sideEdge = BattleOpacityHelper.applyOpacity(withAlpha(colors.borderHighlight(), 66), opacity);
      int lowerShadow = BattleOpacityHelper.applyOpacity(withAlpha(mixColors(colors.panelBottom(), -16447992, 0.52F), 96), opacity);
      fillBeveledRect(gui, x, y, width, height, border);
      fillBeveledGradient(gui, x + 1, y + 1, width - 2, height - 2, panelTop, panelBottom);
      fillTopBeveledGradient(gui, x + 1, y + 1, width - 2, 9, headerTop, headerBottom);
      gui.method_25294(x + 1, y + 10, x + width - 1, y + 10 + 1, borderLight);
      fillBottomBeveledGradient(gui, x + 2, y + 10 + 1, width - 4, height - 10 - 3, innerTop, innerBottom);
      gui.method_25296(x + 3, y + 2, x + width - 3, y + 4, headerSheen, 16777215);
      gui.method_25294(x + 1, y + 3, x + 2, y + height - 3, sideEdge);
      gui.method_25294(x + width - 2, y + 3, x + width - 1, y + height - 3, sideEdge);
      gui.method_25296(x + 3, y + height - 4, x + width - 3, y + height - 2, 16777215, lowerShadow);
      gui.method_25294(x + 3, y + 2, x + width - 3, y + 3, inner);
      if (hasCommand) {
         int glow = BattleOpacityHelper.applyOpacity(colors.selectionGlow(), opacity);
         gui.method_25294(x + 3, y + 1, x + width - 3, y + 2, glow);
         gui.method_25294(x + 3, y + height - 2, x + width - 3, y + height - 1, glow);
      }
   }

   public static int getOpponentTileY(UUID battlePokemonUuid) {
      return battlePokemonUuid == null ? Integer.MAX_VALUE : OPPONENT_TILE_Y_POSITIONS.getOrDefault(battlePokemonUuid, Integer.MAX_VALUE);
   }

   private static void trackOpponentTileY(ActiveClientBattlePokemon activeBattlePokemon, boolean left, int tileY) {
      if (!left && activeBattlePokemon != null && activeBattlePokemon.getBattlePokemon() != null) {
         UUID battlePokemonUuid = activeBattlePokemon.getBattlePokemon().getUuid();
         if (battlePokemonUuid != null) {
            OPPONENT_TILE_Y_POSITIONS.put(battlePokemonUuid, tileY);
         }
      }
   }

   private static void drawPortraitFrame(class_332 gui, int x, int y, int size, float opacity, TileThemeColors colors) {
      int border = BattleOpacityHelper.applyOpacity(colors.border(), opacity);
      int borderHighlight = BattleOpacityHelper.applyOpacity(withAlpha(colors.borderHighlight(), 178), opacity);
      int portraitTop = BattleOpacityHelper.applyOpacity(withAlpha(mixColors(colors.portraitBg(), -1, 0.14F), 220), opacity);
      int portraitBottom = BattleOpacityHelper.applyOpacity(withAlpha(mixColors(colors.portraitBg(), colors.panelBottom(), 0.38F), 220), opacity);
      int portraitSheen = BattleOpacityHelper.applyOpacity(withAlpha(mixColors(colors.borderHighlight(), -1, 0.24F), 92), opacity);
      int portraitShadow = BattleOpacityHelper.applyOpacity(withAlpha(mixColors(colors.portraitBg(), -16513785, 0.42F), 104), opacity);
      fillBeveledRect(gui, x - 1, y - 1, size + 2, size + 2, border);
      fillBeveledRect(gui, x, y, size, size, borderHighlight);
      fillBeveledGradient(gui, x + 1, y + 1, size - 2, size - 2, portraitTop, portraitBottom);
      gui.method_25294(x + 3, y + 1, x + size - 3, y + 2, portraitSheen);
      gui.method_25294(x + 3, y + size - 2, x + size - 3, y + size - 1, portraitShadow);
   }

   static void renderSharedPosablePortrait(
      class_332 gui, Species species, PosableState state, int portraitX, int portraitY, int portraitSize, boolean reversed, float partialTicks
   ) {
      if (species != null && state != null) {
         gui.method_44379(portraitX, portraitY, portraitX + portraitSize, portraitY + portraitSize);
         gui.method_51448().method_22903();
         gui.method_51448().method_22904((double)portraitX + (double)portraitSize / 2.0, (double)portraitY - (portraitSize == 19 ? 15.0 : 5.0), 0.0);
         invokeDrawPosablePortrait(species, gui, portraitSize, reversed, state, partialTicks);
         gui.method_51448().method_22909();
         gui.method_44380();
      }
   }

   private static void invokeDrawPosablePortrait(Species species, class_332 gui, int portraitSize, boolean reversed, PosableState state, float partialTicks) {
      Method method = getDrawPosablePortraitMethod();
      if (method != null) {
         try {
            float portraitScale = 18.0F * ((float)portraitSize / 28.0F);
            method.invoke(
               null,
               species.getResourceIdentifier(),
               gui.method_51448(),
               portraitScale,
               species.getForm(state.getCurrentAspects()).getBaseScale(),
               reversed,
               state,
               partialTicks,
               0.0F,
               0.0F,
               0.0F,
               0.0F,
               0.0F,
               false,
               1.0F,
               1.0F,
               1.0F,
               1.0F
            );
         } catch (Throwable var8) {
         }
      }
   }

   private static Method getDrawPosablePortraitMethod() {
      if (drawPosablePortraitResolved) {
         return drawPosablePortraitMethod;
      } else {
         drawPosablePortraitResolved = true;

         try {
            Class<?> guiUtilsClass = Class.forName("com.cobblemon.mod.common.api.gui.GuiUtilsKt");

            for (Method method : guiUtilsClass.getMethods()) {
               if (method.getName().equals("drawPosablePortrait")) {
                  Class<?>[] parameterTypes = method.getParameterTypes();
                  if (parameterTypes.length == 17
                     && parameterTypes[0] == class_2960.class
                     && parameterTypes[4] == boolean.class
                     && parameterTypes[5] == PosableState.class) {
                     drawPosablePortraitMethod = method;
                     break;
                  }
               }
            }
         } catch (Throwable var6) {
         }

         return drawPosablePortraitMethod;
      }
   }

   static void renderMegaPortraitBadge(class_332 gui, int portraitX, int portraitY, int portraitSize, boolean playerSide, float opacity) {
      int badgeSize = portraitSize == 19 ? 7 : 9;
      int badgeX = playerSide ? portraitX - 1 : portraitX + portraitSize - badgeSize + 1;
      int badgeY = portraitY - 1;
      gui.method_51448().method_22903();
      gui.method_51448().method_46416(0.0F, 0.0F, 8200.0F);
      gui.method_51422(1.0F, 1.0F, 1.0F, opacity);
      gui.method_25293(MEGA_ICON, badgeX, badgeY, badgeSize, badgeSize, 0.0F, 0.0F, 64, 64, 64, 64);
      gui.method_51422(1.0F, 1.0F, 1.0F, 1.0F);
      gui.method_51448().method_22909();
   }

   private static void renderNameRow(
      class_332 gui,
      class_327 font,
      String displayName,
      int level,
      Gender gender,
      boolean owned,
      int x,
      int y,
      int width,
      float opacity,
      TileThemeColors colors,
      float textScale
   ) {
      int safeWidth = Math.max(20, width);
      int textColor = BattleOpacityHelper.applyOpacity(colors.textMain(), opacity);
      int shadowColor = BattleOpacityHelper.applyOpacity(colors.textShadow(), opacity);
      String levelText = "Lv." + level;
      int levelWidth = getScaledTextWidth(font, levelText, textScale);
      int levelX = x + safeWidth - levelWidth;
      String nameText = trimToWidth(font, displayName, safeWidth - levelWidth - 14, textScale);
      drawShadowedTextScaled(gui, font, nameText, x, y, textColor, shadowColor, textScale);
      drawShadowedTextScaled(gui, font, levelText, levelX, y, textColor, shadowColor, textScale);
      if (gender != null && gender != Gender.GENDERLESS) {
         String symbol = gender == Gender.MALE ? "♂" : "♀";
         int genderColor = BattleOpacityHelper.applyOpacity(gender == Gender.MALE ? -10237441 : -33646, opacity);
         drawShadowedTextScaled(
            gui, font, symbol, Math.min(levelX - 8, x + getScaledTextWidth(font, nameText, textScale) + 4), y, genderColor, shadowColor, textScale
         );
      }

      if (owned) {
         gui.method_25294(x, y + 8, x + 6, y + 9, BattleOpacityHelper.applyOpacity(-1519509, opacity));
      }
   }

   private static void renderHealthBar(
      class_332 gui,
      class_327 font,
      ClientBattlePokemon battlePokemon,
      int x,
      int y,
      int width,
      boolean playerSide,
      float opacity,
      TileThemeColors colors,
      float textScale
   ) {
      int barWidth = Math.max(34, width);
      float ratio = battlePokemon.isHpFlat()
         ? Math.max(0.0F, Math.min(1.0F, battlePokemon.getHpValue() / Math.max(1.0F, battlePokemon.getMaxHp())))
         : Math.max(0.0F, Math.min(1.0F, battlePokemon.getHpValue()));
      int fillWidth = Math.max(1, Math.round((float)(barWidth - 2) * ratio));
      gui.method_25294(x, y, x + barWidth, y + 6, BattleOpacityHelper.applyOpacity(colors.hpTrack(), opacity));
      gui.method_25294(x, y, x + barWidth, y + 1, BattleOpacityHelper.applyOpacity(colors.hpTrackEdge(), opacity));
      if (playerSide) {
         gui.method_25294(x + 1, y + 1, x + 1 + fillWidth, y + 5, BattleOpacityHelper.applyOpacity(getHpColor(ratio), opacity));
      } else {
         gui.method_25294(x + barWidth - 1 - fillWidth, y + 1, x + barWidth - 1, y + 5, BattleOpacityHelper.applyOpacity(getHpColor(ratio), opacity));
      }

      String hpText = battlePokemon.isHpFlat()
         ? (int)battlePokemon.getHpValue() + "/" + battlePokemon.getMaxHp()
         : (int)Math.ceil((double)(ratio * 100.0F)) + "%";
      int textWidth = getScaledTextWidth(font, hpText, textScale);
      int textX = x + (barWidth - textWidth) / 2;
      drawShadowedTextScaled(
         gui,
         font,
         hpText,
         textX,
         y - 1,
         BattleOpacityHelper.applyOpacity(colors.textMain(), opacity),
         BattleOpacityHelper.applyOpacity(colors.textShadow(), opacity),
         textScale
      );
   }

   static int renderNativeTileTypeIcons(class_332 gui, List<ElementalType> types, int x, int y, float opacity, String teraTypeName) {
      return renderTypeIcons(gui, types, x, y, opacity, teraTypeName);
   }

   private static int renderTypeIcons(class_332 gui, List<ElementalType> types, int x, int y, float opacity, String teraTypeName) {
      int totalWidth = getTypeIconRowWidth(types, teraTypeName);
      if (totalWidth <= 0) {
         return 0;
      } else {
         int iconSize = 10;
         int spacing = 1;
         int offsetX = 0;
         boolean renderTeraIcon = teraTypeName != null && !teraTypeName.isBlank() && MEGA_SHOWDOWN_LOADED;
         gui.method_51422(1.0F, 1.0F, 1.0F, opacity);
         if (renderTeraIcon) {
            class_2960 teraIcon = class_2960.method_60654("mega_showdown:textures/gui/summary/tera_types/" + teraTypeName.toLowerCase(Locale.ROOT) + ".png");
            gui.method_25293(teraIcon, x, y - 1, 12, 12, 0.0F, 0.0F, 32, 32, 32, 32);
            gui.method_51422(1.0F, 1.0F, 1.0F, 1.0F);
            return totalWidth;
         } else {
            for (ElementalType type : types) {
               if (type != null) {
                  gui.method_25293(TYPE_SPRITE_SHEET, x + offsetX, y, iconSize, iconSize, (float)(type.getTextureXMultiplier() * 36), 0.0F, 36, 36, 648, 36);
                  offsetX += iconSize + spacing;
               }
            }

            gui.method_51422(1.0F, 1.0F, 1.0F, 1.0F);
            return totalWidth;
         }
      }
   }

   static boolean isMegaEvolvedForTile(ClientBattlePokemon battlePokemon, String side, String speciesName, String displayName) {
      if (BattleMessageSubscriber.isMegaEvolved(side, speciesName) || BattleMessageSubscriber.isMegaEvolved(side, displayName)) {
         return true;
      } else if (isMegaEvolvedFromRenderablePokemon(battlePokemon)) {
         return true;
      } else if (isMegaEvolvedFromMegaShowdownMemory(battlePokemon)) {
         return true;
      } else if (isMegaEvolvedFromBattleStateAspects(battlePokemon)) {
         return true;
      } else if (containsMegaFormName(invokeNoArg(battlePokemon, "getForm"))) {
         return true;
      } else {
         Object properties = invokeNoArg(battlePokemon, "getProperties");
         if (containsMegaFormName(invokeNoArg(properties, "getForm"))) {
            return true;
         } else {
            Object createdPokemon = invokeNoArg(properties, "create");
            if (hasMegaPersistentTag(createdPokemon)) {
               return true;
            } else if (containsMegaFormName(invokeNoArg(createdPokemon, "getForm"))) {
               return true;
            } else {
               Object aspects = invokeNoArg(createdPokemon, "getAspects");
               return aspects != null && aspects.toString().toLowerCase(Locale.ROOT).contains("mega")
                  ? true
                  : isMegaEvolvedFromLivePokemonEntity(battlePokemon);
            }
         }
      }
   }

   private static boolean containsMegaFormName(Object formObject) {
      if (formObject == null) {
         return false;
      } else {
         if (formObject instanceof String formName && formName.toLowerCase(Locale.ROOT).contains("mega")) {
            return true;
         }

         Object name = invokeNoArg(formObject, "getName");
         return name != null && name.toString().toLowerCase(Locale.ROOT).contains("mega");
      }
   }

   private static boolean isMegaEvolvedFromBattleStateAspects(ClientBattlePokemon battlePokemon) {
      if (battlePokemon != null && battlePokemon.getState() != null) {
         try {
            Set<String> aspects = battlePokemon.getState().getCurrentAspects();
            if (aspects != null) {
               for (String aspect : aspects) {
                  if (aspect != null && aspect.toLowerCase(Locale.ROOT).contains("mega")) {
                     return true;
                  }
               }
            }
         } catch (Throwable var5) {
         }

         try {
            Species species = battlePokemon.getSpecies();
            if (species != null) {
               Object form = species.getForm(battlePokemon.getState().getCurrentAspects());
               if (containsMegaFormName(form)) {
                  return true;
               }
            }
         } catch (Throwable var4) {
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean isMegaEvolvedFromRenderablePokemon(ClientBattlePokemon battlePokemon) {
      if (battlePokemon == null) {
         return false;
      } else {
         Object properties = invokeNoArg(battlePokemon, "getProperties");
         Object renderablePokemon = invokeNoArg(properties, "asRenderablePokemon");
         if (renderablePokemon == null) {
            return false;
         } else {
            Object form = invokeNoArg(renderablePokemon, "getForm");
            if (containsMegaFormName(form)) {
               return true;
            } else {
               Object showdownFormId = invokeNoArg(form, "formOnlyShowdownId");
               return showdownFormId != null && showdownFormId.toString().toLowerCase(Locale.ROOT).contains("mega");
            }
         }
      }
   }

   private static boolean isMegaEvolvedFromMegaShowdownMemory(ClientBattlePokemon battlePokemon) {
      if (MEGA_SHOWDOWN_LOADED && battlePokemon != null) {
         try {
            ensureMegaShowdownMemoryReflection();
            if (megaShowdownBattleHudMemoryField != null && megaShowdownRenderablePokemonField != null && battlePokemon.getUuid() != null) {
               if (megaShowdownBattleHudMemoryField.get(null) instanceof Map<?, ?> memoryMap) {
                  Object memory = memoryMap.get(battlePokemon.getUuid());
                  if (memory == null) {
                     return false;
                  } else {
                     Object renderablePokemon = megaShowdownRenderablePokemonField.get(memory);
                     if (renderablePokemon == null) {
                        return false;
                     } else {
                        Object form = invokeNoArg(renderablePokemon, "getForm");
                        if (containsMegaFormName(form)) {
                           return true;
                        } else {
                           Object showdownFormId = invokeNoArg(form, "formOnlyShowdownId");
                           return showdownFormId != null && showdownFormId.toString().toLowerCase(Locale.ROOT).contains("mega");
                        }
                     }
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } catch (Throwable var7) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static void ensureMegaShowdownMemoryReflection() {
      if (!megaShowdownMemoryReflectionChecked) {
         megaShowdownMemoryReflectionChecked = true;

         try {
            Class<?> battleHudClass = Class.forName("com.github.yajatkaul.mega_showdown.client.battle.hud.BattleHUD");
            megaShowdownBattleHudMemoryField = battleHudClass.getDeclaredField("memory");
            megaShowdownBattleHudMemoryField.setAccessible(true);
            Class<?> battlePokemonMemoryClass = Class.forName("com.github.yajatkaul.mega_showdown.client.battle.storage.BattlePokemonMemory");
            megaShowdownRenderablePokemonField = battlePokemonMemoryClass.getDeclaredField("renderablePokemon");
            megaShowdownRenderablePokemonField.setAccessible(true);
         } catch (Throwable var2) {
            megaShowdownBattleHudMemoryField = null;
            megaShowdownRenderablePokemonField = null;
         }
      }
   }

   private static boolean isMegaEvolvedFromLivePokemonEntity(ClientBattlePokemon battlePokemon) {
      Object pokemonData = resolveBattlePokemonDataFromEntity(battlePokemon);
      if (pokemonData == null) {
         return false;
      } else if (hasMegaPersistentTag(pokemonData)) {
         return true;
      } else if (containsMegaFormName(invokeNoArg(pokemonData, "getForm"))) {
         return true;
      } else {
         Object aspects = invokeNoArg(pokemonData, "getAspects");
         return aspects != null && aspects.toString().toLowerCase(Locale.ROOT).contains("mega");
      }
   }

   private static Object resolveBattlePokemonDataFromEntity(ClientBattlePokemon battlePokemon) {
      if (battlePokemon != null && battlePokemon.getUuid() != null) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1687 == null) {
            return null;
         } else {
            UUID targetUuid = battlePokemon.getUuid();

            for (class_1297 entity : mc.field_1687.method_18112()) {
               if (entity != null && entity.getClass().getName().contains("PokemonEntity")) {
                  Object pokemonData = invokeNoArg(entity, "getPokemon");
                  Object pokemonUuid = invokeNoArg(pokemonData, "getUuid");
                  if (targetUuid.equals(pokemonUuid)) {
                     return pokemonData;
                  }
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private static boolean hasMegaPersistentTag(Object pokemonObject) {
      Object persistentData = invokeNoArg(pokemonObject, "getPersistentData");
      if (persistentData == null) {
         return false;
      } else {
         if (invokeSingleArg(persistentData, "getBoolean", String.class, "is_mega") instanceof Boolean megaTag && megaTag) {
            return true;
         }

         return false;
      }
   }

   static int getNativeTileTypeIconRowWidth(List<ElementalType> types, String teraTypeName) {
      return getTypeIconRowWidth(types, teraTypeName);
   }

   private static int getTypeIconRowWidth(List<ElementalType> types, String teraTypeName) {
      if (teraTypeName != null && !teraTypeName.isBlank() && MEGA_SHOWDOWN_LOADED) {
         return 12;
      } else if (types != null && !types.isEmpty()) {
         int count = 0;

         for (ElementalType type : types) {
            if (type != null) {
               count++;
            }
         }

         if (count <= 0) {
            return 0;
         } else {
            int iconSize = 10;
            int spacing = 1;
            return count * iconSize + (count - 1) * spacing;
         }
      } else {
         return 0;
      }
   }

   private static int countVisibleTypes(List<ElementalType> types, String teraTypeName) {
      if (teraTypeName != null && !teraTypeName.isBlank() && MEGA_SHOWDOWN_LOADED) {
         return 1;
      } else if (types != null && !types.isEmpty()) {
         int count = 0;

         for (ElementalType type : types) {
            if (type != null) {
               count++;
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   static int renderNativeStatusPill(class_332 gui, class_327 font, String status, int x, int y, float opacity, float textScale) {
      return renderStatusPill(gui, font, status, x, y, opacity, resolveTileThemeColors(), textScale, 5, 3);
   }

   static int renderNativeStatusPillCompact(class_332 gui, class_327 font, String status, int x, int y, float opacity, float textScale) {
      return renderStatusPill(gui, font, status, x, y, opacity, resolveTileThemeColors(), textScale, 4, 2, 7);
   }

   private static boolean isTripleOrMoreBattle(ClientBattle battle) {
      return battle != null
         && battle.getBattleFormat() != null
         && battle.getBattleFormat().getBattleType() != null
         && battle.getBattleFormat().getBattleType().getPokemonPerSide() >= 3;
   }

   private static boolean isBottomVisibleCompactSlot(
      class_310 mc, ClientBattle battle, ActiveClientBattlePokemon activePokemon, boolean left, int rank, boolean isCompact
   ) {
      if (mc != null && battle != null && activePokemon != null && isCompact && isTripleOrMoreBattle(battle)) {
         ClientBattleSide side = left ? BattleSideHelper.getPlayerSide(battle) : BattleSideHelper.getOpponentSide(battle);
         List<ActiveClientBattlePokemon> activeMons = getOrderedActiveMons(side);
         if (activeMons.isEmpty()) {
            return false;
         } else {
            TilePlacement currentPlacement = calculateTilePlacement(mc, battle, activePokemon, left, rank, isCompact);
            if (currentPlacement == null) {
               return false;
            } else {
               int maxY = Integer.MIN_VALUE;
               int totalMons = activeMons.size();

               for (int index = 0; index < totalMons; index++) {
                  ActiveClientBattlePokemon other = activeMons.get(index);
                  if (other != null && other.getBattlePokemon() != null) {
                     int visibleRank = left ? index : totalMons - index - 1;
                     TilePlacement otherPlacement = calculateTilePlacement(mc, battle, other, left, visibleRank, isCompact);
                     if (otherPlacement != null) {
                        maxY = Math.max(maxY, otherPlacement.y());
                     }
                  }
               }

               return currentPlacement.y() >= maxY;
            }
         }
      } else {
         return false;
      }
   }

   private static List<ActiveClientBattlePokemon> getOrderedActiveMons(ClientBattleSide side) {
      List<ActiveClientBattlePokemon> activeMons = new ArrayList<>();
      if (side == null) {
         return activeMons;
      } else {
         for (ActiveClientBattlePokemon activePokemon : side.getActiveClientBattlePokemon()) {
            if (activePokemon != null && activePokemon.getBattlePokemon() != null) {
               activeMons.add(activePokemon);
            }
         }

         if (!activeMons.isEmpty()) {
            return activeMons;
         } else {
            for (ClientBattleActor actor : side.getActors()) {
               for (ActiveClientBattlePokemon activePokemonx : actor.getActivePokemon()) {
                  if (activePokemonx != null && activePokemonx.getBattlePokemon() != null) {
                     activeMons.add(activePokemonx);
                  }
               }
            }

            return activeMons;
         }
      }
   }

   private static int renderStatusPill(
      class_332 gui, class_327 font, String status, int x, int y, float opacity, TileThemeColors colors, float textScale, int widthPadding, int textInset
   ) {
      return renderStatusPill(gui, font, status, x, y, opacity, colors, textScale, widthPadding, textInset, 8);
   }

   private static int renderStatusPill(
      class_332 gui,
      class_327 font,
      String status,
      int x,
      int y,
      float opacity,
      TileThemeColors colors,
      float textScale,
      int widthPadding,
      int textInset,
      int pillHeight
   ) {
      if (status != null && !status.isBlank()) {
         String display = status.toUpperCase(Locale.ROOT);
         String width = status.toLowerCase(Locale.ROOT);

         int baseColor = switch (width) {
            case "slp" -> -7303008;
            case "brn" -> -2068432;
            case "psn", "tox" -> -6274912;
            case "par" -> -3100640;
            case "frz" -> -9385776;
            default -> -8355712;
         };
         int width = getStatusPillWidth(font, display, textScale, widthPadding);
         int borderColor = mixColors(baseColor, -16250868, 0.42F);
         int fillTop = mixColors(baseColor, -1, 0.16F);
         int fillBottom = mixColors(baseColor, -16250868, 0.2F);
         int highlight = withAlpha(mixColors(baseColor, -1, 0.38F), 172);
         int lowerShadow = withAlpha(mixColors(baseColor, -16250868, 0.46F), 152);
         int gradientBottom = y + pillHeight - 1;
         int shadowStart = gradientBottom - 1;
         gui.method_25294(x, y, x + width, y + pillHeight, BattleOpacityHelper.applyOpacity(borderColor, opacity));
         gui.method_25296(
            x + 1,
            y + 1,
            x + width - 1,
            gradientBottom,
            BattleOpacityHelper.applyOpacity(fillTop, opacity),
            BattleOpacityHelper.applyOpacity(fillBottom, opacity)
         );
         gui.method_25294(x + 1, y + 1, x + width - 1, y + 2, BattleOpacityHelper.applyOpacity(highlight, opacity));
         gui.method_25294(x + 1, shadowStart, x + width - 1, gradientBottom, BattleOpacityHelper.applyOpacity(lowerShadow, opacity));
         drawShadowedTextScaled(
            gui,
            font,
            display,
            x + textInset,
            y + 1,
            BattleOpacityHelper.applyOpacity(-1, opacity),
            BattleOpacityHelper.applyOpacity(colors.textShadow(), opacity),
            textScale
         );
         return width;
      } else {
         return 0;
      }
   }

   static int getNativeStatusPillWidth(class_327 font, String status, float textScale) {
      return getStatusPillWidth(font, status, textScale, 5);
   }

   static int getNativeStatusPillWidthCompact(class_327 font, String status, float textScale) {
      return getStatusPillWidth(font, status, textScale, 4);
   }

   static SpeedInfo resolveSpeedInfoForTile(ActiveClientBattlePokemon activeBattlePokemon, ClientBattlePokemon battlePokemon, String side) {
      return resolveSpeedInfo(activeBattlePokemon, battlePokemon, side);
   }

   static int getScaledTextWidthForTile(class_327 font, String text, float scale) {
      return getScaledTextWidth(font, text, scale);
   }

   static void renderSpeedBadgeForTile(
      class_332 gui, class_327 font, SpeedInfo info, int x, int y, int width, boolean playerSide, float opacity, float textScale
   ) {
      renderSpeedBadge(gui, font, info, x, y, width, playerSide, opacity, resolveTileThemeColors(), textScale);
   }

   private static int renderStatusPill(class_332 gui, class_327 font, String status, int x, int y, float opacity, TileThemeColors colors, float textScale) {
      return renderStatusPill(gui, font, status, x, y, opacity, colors, textScale, 8, 3);
   }

   private static int getStatusPillWidth(class_327 font, String status, float textScale, int widthPadding) {
      return status != null && !status.isBlank() ? getScaledTextWidth(font, status.toUpperCase(Locale.ROOT), textScale) + widthPadding : 0;
   }

   private static int getStatusPillWidth(class_327 font, String status, float textScale) {
      return getStatusPillWidth(font, status, textScale, 8);
   }

   private static void renderPortraitScreenOverlay(class_332 gui, Map<String, Integer> screens, int x, int y, int size, float opacity) {
      if (screens != null && !screens.isEmpty()) {
         List<ScreenDisplay> displays = getActiveScreenDisplays(screens);
         if (!displays.isEmpty()) {
            ScreenDisplay overlay = displays.get(0);
            int overlayTop = withAlpha(mixColors(overlay.color(), -1, 0.18F), 120);
            int overlayBottom = withAlpha(mixColors(overlay.color(), -16513785, 0.12F), 120);
            gui.method_51448().method_22903();
            gui.method_51448().method_46416(0.0F, 0.0F, 100.0F);
            gui.method_25296(
               x + 1,
               y + 1,
               x + size - 1,
               y + size - 1,
               BattleOpacityHelper.applyOpacity(overlayTop, opacity),
               BattleOpacityHelper.applyOpacity(overlayBottom, opacity)
            );
            gui.method_25294(x + 2, y + 1, x + size - 2, y + 2, BattleOpacityHelper.applyOpacity(withAlpha(mixColors(overlay.color(), -1, 0.42F), 92), opacity));
            gui.method_51448().method_22909();
         }
      }
   }

   private static void renderScreenStrip(
      class_332 gui,
      class_327 font,
      Map<String, Integer> screens,
      int x,
      int y,
      int width,
      float opacity,
      TileThemeColors colors,
      boolean isCompact,
      boolean leftAligned
   ) {
      List<ScreenDisplay> displays = getActiveScreenDisplays(screens);
      if (!displays.isEmpty()) {
         float scale = isCompact ? 0.43F : 0.52F;
         float minScale = isCompact ? 0.35F : 0.4F;
         int availableWidth = Math.max(20, width);

         while (scale > minScale && getScreenStripWidth(font, displays, scale) > availableWidth) {
            scale -= 0.02F;
         }

         int currentX = x;
         int gap = Math.max(2, Math.round(scale >= 0.5F ? 3.0F : 2.0F));
         int shadowColor = BattleOpacityHelper.applyOpacity(colors.textShadow(), opacity);
         if (leftAligned) {
            for (int i = 0; i < displays.size(); i++) {
               ScreenDisplay display = displays.get(i);
               drawShadowedTextScaled(gui, font, display.label(), currentX, y, BattleOpacityHelper.applyOpacity(display.color(), opacity), shadowColor, scale);
               currentX += getScaledTextWidth(font, display.label(), scale);
               if (i < displays.size() - 1) {
                  currentX += gap;
               }
            }
         } else {
            currentX = x + width;

            for (int ix = 0; ix < displays.size(); ix++) {
               ScreenDisplay display = displays.get(ix);
               int labelWidth = getScaledTextWidth(font, display.label(), scale);
               currentX -= labelWidth;
               drawShadowedTextScaled(gui, font, display.label(), currentX, y, BattleOpacityHelper.applyOpacity(display.color(), opacity), shadowColor, scale);
               if (ix < displays.size() - 1) {
                  currentX -= gap;
               }
            }
         }
      }
   }

   private static int getScreenStripWidth(class_327 font, List<ScreenDisplay> displays, float scale) {
      int width = 0;
      int gap = Math.max(2, Math.round(scale >= 0.5F ? 3.0F : 2.0F));

      for (int i = 0; i < displays.size(); i++) {
         width += getScaledTextWidth(font, displays.get(i).label(), scale);
         if (i < displays.size() - 1) {
            width += gap;
         }
      }

      return width;
   }

   private static List<ScreenDisplay> getActiveScreenDisplays(Map<String, Integer> screens) {
      List<ScreenDisplay> displays = new ArrayList<>();
      if (screens != null && !screens.isEmpty()) {
         for (String screenName : SCREEN_DISPLAY_ORDER) {
            Integer turns = screens.get(screenName);
            if (turns != null && turns > 0) {
               displays.add(new ScreenDisplay(getScreenAbbreviation(screenName) + turns, getScreenColor(screenName)));
            }
         }

         for (Entry<String, Integer> entry : screens.entrySet()) {
            String screenNamex = entry.getKey();
            Integer turns = entry.getValue();
            if (turns != null && turns > 0 && !isKnownScreen(screenNamex)) {
               displays.add(new ScreenDisplay(getScreenAbbreviation(screenNamex) + turns, getScreenColor(screenNamex)));
            }
         }

         return displays;
      } else {
         return displays;
      }
   }

   private static boolean isKnownScreen(String screenName) {
      for (String knownScreen : SCREEN_DISPLAY_ORDER) {
         if (knownScreen.equals(screenName)) {
            return true;
         }
      }

      return false;
   }

   private static String getScreenAbbreviation(String screenName) {
      return switch (screenName) {
         case "Light Screen" -> "LS";
         case "Reflect" -> "Ref";
         case "Aurora Veil" -> "AV";
         case "Safeguard" -> "SG";
         case "Mist" -> "Mst";
         case "Lucky Chant" -> "LC";
         default -> screenName;
      };
   }

   private static int getScreenColor(String screenName) {
      return switch (screenName) {
         case "Light Screen" -> -171;
         case "Reflect" -> -43606;
         case "Aurora Veil" -> -11141121;
         case "Safeguard" -> -11141291;
         case "Mist" -> -5579265;
         case "Lucky Chant" -> -10496;
         default -> -1;
      };
   }

   private static void renderSpeedBadge(
      class_332 gui, class_327 font, SpeedInfo info, int x, int y, int width, boolean playerSide, float opacity, TileThemeColors colors, float textScale
   ) {
      int labelX = x + (playerSide ? 9 : -1);
      int labelWidth = getScaledTextWidth(font, "SPE", textScale);
      int minGap = playerSide ? 4 : 5;
      drawShadowedTextScaled(
         gui,
         font,
         "SPE",
         labelX,
         y,
         BattleOpacityHelper.applyOpacity(-2634673, opacity),
         BattleOpacityHelper.applyOpacity(colors.textShadow(), opacity),
         textScale
      );
      int valueRightInset = playerSide ? 2 : 14;
      int valueX = Math.max(labelX + labelWidth + minGap, x + width - valueRightInset - getScaledTextWidth(font, info.text(), textScale));
      drawShadowedTextScaled(
         gui,
         font,
         info.text(),
         valueX,
         y,
         BattleOpacityHelper.applyOpacity(info.color(), opacity),
         BattleOpacityHelper.applyOpacity(colors.textShadow(), opacity),
         textScale
      );
   }

   private static void renderActorLabel(
      class_332 gui, class_327 font, String actorName, int anchorX, int y, boolean alignLeft, float opacity, TileThemeColors colors, float textScale
   ) {
      if (actorName != null && !actorName.isBlank()) {
         String label = trimToWidth(font, actorName, 64, textScale);
         int textWidth = getScaledTextWidth(font, label, textScale);
         int x = alignLeft ? anchorX : anchorX - textWidth;
         gui.method_25294(x - 2, y - 1, x + textWidth + 2, y + 8, BattleOpacityHelper.applyOpacity(colors.actorLabelBg(), opacity));
         drawShadowedTextScaled(
            gui,
            font,
            label,
            x,
            y,
            BattleOpacityHelper.applyOpacity(colors.textDim(), opacity),
            BattleOpacityHelper.applyOpacity(colors.textShadow(), opacity),
            textScale
         );
      }
   }

   private static SpeedInfo resolveSpeedInfo(ActiveClientBattlePokemon activeBattlePokemon, ClientBattlePokemon battlePokemon, String side) {
      if (!BattleExtrasConfig.isSpeedRangeEnabled()) {
         return null;
      } else {
         String lookupName = battlePokemon.getDisplayName().getString();
         int speedStage = resolveSpeedStage(side, lookupName, battlePokemon.getSpecies().getName());
         boolean hasTailwind = BattleMessageSubscriber.getTailwindTurns(side) > 0;
         if ("player".equals(side)) {
            boolean choiceBoosted = LocalBattlePartyTracker.isChoiceItemBoostedStat(activeBattlePokemon, battlePokemon, "speed");
            Integer exactSpeed = LocalBattlePartyTracker.getExactPlayerSpeed(activeBattlePokemon, battlePokemon);
            if (exactSpeed != null && exactSpeed > 0) {
               int displayedSpeed = applySpeedStage(exactSpeed, speedStage);
               if (hasTailwind) {
                  displayedSpeed *= 2;
               }

               return new SpeedInfo(Integer.toString(displayedSpeed), getSpeedTextColor(speedStage, choiceBoosted));
            }

            Pokemon actorPokemon = BattlePokemonSpeedResolver.findBattleActorPokemon(activeBattlePokemon);
            Integer fallbackSpeed = BattlePokemonSpeedResolver.resolveDisplayedSpeed(actorPokemon);
            if (fallbackSpeed != null && fallbackSpeed > 0) {
               int displayedSpeed = applySpeedStage(fallbackSpeed, speedStage);
               if (hasTailwind) {
                  displayedSpeed *= 2;
               }

               return new SpeedInfo(Integer.toString(displayedSpeed), getSpeedTextColor(speedStage, choiceBoosted));
            }
         }

         int baseSpeed = resolveBaseSpeed(battlePokemon);
         if (baseSpeed <= 0) {
            return null;
         } else {
            int minSpeed = calculateMinSpeed(baseSpeed, battlePokemon.getLevel());
            int maxSpeed = calculateMaxSpeed(baseSpeed, battlePokemon.getLevel());
            minSpeed = applySpeedStage(minSpeed, speedStage);
            maxSpeed = applySpeedStage(maxSpeed, speedStage);
            if (hasTailwind) {
               minSpeed *= 2;
               maxSpeed *= 2;
            }

            return new SpeedInfo(minSpeed + "-" + maxSpeed, getSpeedTextColor(speedStage, false));
         }
      }
   }

   private static int resolveSpeedStage(String side, String displayName, String speciesName) {
      Map<String, Integer> displayStages = BattleMessageSubscriber.getStatStagesForPokemon(side, displayName);
      if (!displayStages.isEmpty()) {
         return BattleMessageSubscriber.getStatStage(side, displayName, "speed");
      } else {
         Map<String, Integer> speciesStages = BattleMessageSubscriber.getStatStagesForPokemon(side, speciesName);
         return !speciesStages.isEmpty() ? BattleMessageSubscriber.getStatStage(side, speciesName, "speed") : 0;
      }
   }

   static int resolveBaseStat(ClientBattlePokemon battlePokemon, String statId) {
      if (battlePokemon == null) {
         return 0;
      } else {
         Object pokemon = getActualPokemon(battlePokemon);
         if (pokemon != null) {
            int stat = extractBaseStatFromObject(invokeNoArg(pokemon, "getForm"), statId);
            if (stat > 0) {
               return stat;
            }
         }

         int direct = extractBaseStatFromObject(invokeNoArg(battlePokemon, "getForm"), statId);
         if (direct > 0) {
            return direct;
         } else {
            Object properties = invokeNoArg(battlePokemon, "getProperties");
            if (properties != null) {
               Object propertyForm = invokeNoArg(properties, "getForm");
               Species species = battlePokemon.getSpecies();
               String formName = null;
               if (propertyForm instanceof String s) {
                  formName = s;
               } else if (propertyForm != null) {
                  formName = propertyForm.toString();
               }

               if (formName != null && species != null && !formName.isBlank()) {
                  try {
                     Method getForm = species.getClass().getMethod("getForm", String.class);
                     Object resolvedForm = getForm.invoke(species, formName);
                     int stat = extractBaseStatFromObject(resolvedForm, statId);
                     if (stat > 0) {
                        return stat;
                     }
                  } catch (Throwable var17) {
                     try {
                        Method getForms = species.getClass().getMethod("getForms");
                        Object forms = getForms.invoke(species);
                        if (forms instanceof Iterable) {
                           for (Object f : (Iterable)forms) {
                              Method getName = f.getClass().getMethod("getName");
                              if (formName.equalsIgnoreCase(getName.invoke(f).toString())) {
                                 int statx = extractBaseStatFromObject(f, statId);
                                 if (statx > 0) {
                                    return statx;
                                 }
                              }
                           }
                        }
                     } catch (Throwable var16) {
                     }
                  }
               }

               Object createdPokemon = invokeNoArg(properties, "create");
               int createdStat = extractBaseStatFromObject(invokeNoArg(createdPokemon, "getForm"), statId);
               if (createdStat > 0) {
                  return createdStat;
               }
            }

            return extractBaseStatFromObject(battlePokemon.getSpecies(), statId);
         }
      }
   }

   private static Object getActualPokemon(ClientBattlePokemon battlePokemon) {
      String[] getters = new String[]{"getEffectedPokemon", "effectedPokemon", "getPokemon", "pokemon", "getOriginalPokemon", "getEntity"};

      for (String getter : getters) {
         try {
            Object result = invokeNoArg(battlePokemon, getter);
            if (result != null) {
               if (!getter.equals("getEntity")) {
                  return result;
               }

               Object fromEntity = invokeNoArg(result, "getPokemon");
               if (fromEntity != null) {
                  return fromEntity;
               }
            }
         } catch (Throwable var8) {
         }
      }

      return null;
   }

   private static int resolveBaseSpeed(ClientBattlePokemon battlePokemon) {
      return resolveBaseStat(battlePokemon, "speed");
   }

   private static int extractBaseStatFromObject(Object object, String statId) {
      if (object == null) {
         return 0;
      } else if (invokeNoArg(object, "getBaseStats") instanceof Map<?, ?> stats) {
         for (Entry<?, ?> entry : stats.entrySet()) {
            if (entry.getValue() instanceof Number number && matchesBaseStatKey(String.valueOf(entry.getKey()), statId)) {
               return number.intValue();
            }
         }

         return 0;
      } else {
         return 0;
      }
   }

   private static boolean matchesBaseStatKey(String rawKey, String statId) {
      if (rawKey != null && statId != null && !statId.isBlank()) {
         String key = rawKey.toLowerCase(Locale.ROOT).replace("_", "");
         String normalized = statId.toLowerCase(Locale.ROOT).replace("_", "");

         return switch (normalized) {
            case "attack", "atk" -> key.equals("attack") || key.contains("attack") && !key.contains("special");
            case "defence", "defense", "def" -> key.equals("defence") || key.equals("defense") || key.contains("def") && !key.contains("special");
            case "specialattack", "spatk", "spa" -> key.contains("special") && (key.contains("attack") || key.contains("atk"));
            case "specialdefence", "specialdefense", "spdef", "spd" -> key.contains("special") && (key.contains("def") || key.contains("defen"));
            case "speed", "spe" -> key.contains("speed") || key.equals("spe");
            default -> false;
         };
      } else {
         return false;
      }
   }

   private static Object invokeNoArg(Object target, String methodName) {
      if (target == null) {
         return null;
      } else {
         try {
            return target.getClass().getMethod(methodName).invoke(target);
         } catch (Throwable var3) {
            return null;
         }
      }
   }

   private static Object invokeSingleArg(Object target, String methodName, Class<?> parameterType, Object argument) {
      if (target == null) {
         return null;
      } else {
         try {
            return target.getClass().getMethod(methodName, parameterType).invoke(target, argument);
         } catch (Throwable var5) {
            return null;
         }
      }
   }

   private static int calculateMinSpeed(int baseSpeed, int level) {
      return (int)Math.floor(((double)(2 * baseSpeed * level) / 100.0 + 5.0) * 0.9);
   }

   private static int calculateMaxSpeed(int baseSpeed, int level) {
      return (int)Math.floor(((double)((2 * baseSpeed + 31 + 63) * level) / 100.0 + 5.0) * 1.1);
   }

   private static int applySpeedStage(int speed, int stage) {
      if (stage > 0) {
         return (int)((double)speed * ((2.0 + (double)stage) / 2.0));
      } else {
         return stage < 0 ? (int)((double)speed * (2.0 / (2.0 - (double)stage))) : speed;
      }
   }

   private static int getHpColor(float ratio) {
      if (ratio > 0.5F) {
         return -11221131;
      } else {
         return ratio > 0.2F ? -1587622 : -1807259;
      }
   }

   private static int getSpeedTextColor(int stage, boolean choiceBoosted) {
      if (stage > 0) {
         return -8650862;
      } else if (stage < 0) {
         return -30070;
      } else {
         return choiceBoosted ? -8650862 : -8071425;
      }
   }

   private static TileThemeColors resolveTileThemeColors() {
      Theme theme = BattleUiThemePalette.getActiveBattleLogTheme();
      if (theme == cachedTileTheme && cachedResolvedTileTheme != null) {
         return cachedResolvedTileTheme;
      } else {
         int panelTop = withAlpha(theme.panelBgGradient(), 225);
         int panelBottom = withAlpha(theme.panelBgBottom(), 221);
         int headerTop = withAlpha(theme.headerBgLight(), 240);
         int headerBottom = withAlpha(theme.headerBg(), 236);
         int border = withAlpha(theme.borderColor(), 204);
         int borderHighlight = withAlpha(theme.borderHighlight(), 188);
         int inner = withAlpha(mixColors(theme.panelBgBottom(), theme.headerBg(), 0.18F), 102);
         int textMain = theme.headerText();
         int textDim = mixColors(theme.dimText(), theme.headerText(), 0.22F);
         int textShadow = withAlpha(theme.headerTextShadow(), 168);
         int hpTrack = withAlpha(mixColors(theme.panelBgBottom(), theme.headerBg(), 0.28F), 172);
         int hpTrackEdge = withAlpha(theme.borderColor(), 110);
         int portraitBg = withAlpha(mixColors(theme.panelBgBottom(), theme.panelBg(), 0.35F), 208);
         int speedBg = withAlpha(mixColors(theme.panelBg(), theme.headerBg(), 0.38F), 214);
         int speedEdge = withAlpha(theme.borderHighlight(), 168);
         int selectionGlow = withAlpha(theme.headerAccent(), 122);
         int actorLabelBg = withAlpha(mixColors(theme.panelBgBottom(), theme.headerBg(), 0.45F), 168);
         cachedTileTheme = theme;
         cachedResolvedTileTheme = new TileThemeColors(
            panelTop,
            panelBottom,
            headerTop,
            headerBottom,
            inner,
            border,
            borderHighlight,
            textMain,
            textDim,
            textShadow,
            hpTrack,
            hpTrackEdge,
            portraitBg,
            speedBg,
            speedEdge,
            selectionGlow,
            actorLabelBg
         );
         return cachedResolvedTileTheme;
      }
   }

   private static void fillBeveledRect(class_332 gui, int x, int y, int width, int height, int color) {
      if (width > 0 && height > 0) {
         if (width >= 5 && height >= 5) {
            gui.method_25294(x + 2, y, x + width - 2, y + 1, color);
            gui.method_25294(x + 1, y + 1, x + width - 1, y + 2, color);
            gui.method_25294(x, y + 2, x + width, y + height - 2, color);
            gui.method_25294(x + 1, y + height - 2, x + width - 1, y + height - 1, color);
            gui.method_25294(x + 2, y + height - 1, x + width - 2, y + height, color);
         } else {
            gui.method_25294(x, y, x + width, y + height, color);
         }
      }
   }

   private static void fillBeveledGradient(class_332 gui, int x, int y, int width, int height, int topColor, int bottomColor) {
      if (width > 0 && height > 0) {
         if (width >= 5 && height >= 5) {
            gui.method_25296(x + 2, y, x + width - 2, y + 1, topColor, topColor);
            gui.method_25296(x + 1, y + 1, x + width - 1, y + 2, topColor, topColor);
            gui.method_25296(x, y + 2, x + width, y + height - 2, topColor, bottomColor);
            gui.method_25296(x + 1, y + height - 2, x + width - 1, y + height - 1, bottomColor, bottomColor);
            gui.method_25296(x + 2, y + height - 1, x + width - 2, y + height, bottomColor, bottomColor);
         } else {
            gui.method_25296(x, y, x + width, y + height, topColor, bottomColor);
         }
      }
   }

   private static void fillTopBeveledGradient(class_332 gui, int x, int y, int width, int height, int topColor, int bottomColor) {
      if (width > 0 && height > 0) {
         if (width >= 5 && height >= 3) {
            gui.method_25296(x + 2, y, x + width - 2, y + 1, topColor, topColor);
            gui.method_25296(x + 1, y + 1, x + width - 1, y + 2, topColor, topColor);
            gui.method_25296(x, y + 2, x + width, y + height, topColor, bottomColor);
         } else {
            gui.method_25296(x, y, x + width, y + height, topColor, bottomColor);
         }
      }
   }

   private static void fillBottomBeveledGradient(class_332 gui, int x, int y, int width, int height, int topColor, int bottomColor) {
      if (width > 0 && height > 0) {
         if (width >= 5 && height >= 3) {
            gui.method_25296(x, y, x + width, y + height - 2, topColor, bottomColor);
            gui.method_25296(x + 1, y + height - 2, x + width - 1, y + height - 1, bottomColor, bottomColor);
            gui.method_25296(x + 2, y + height - 1, x + width - 2, y + height, bottomColor, bottomColor);
         } else {
            gui.method_25296(x, y, x + width, y + height, topColor, bottomColor);
         }
      }
   }

   private static int withAlpha(int color, int alpha) {
      return alpha << 24 | color & 16777215;
   }

   private static int mixColors(int first, int second, float secondWeight) {
      float clampedWeight = Math.max(0.0F, Math.min(1.0F, secondWeight));
      float firstWeight = 1.0F - clampedWeight;
      int firstRed = first >> 16 & 0xFF;
      int firstGreen = first >> 8 & 0xFF;
      int firstBlue = first & 0xFF;
      int secondRed = second >> 16 & 0xFF;
      int secondGreen = second >> 8 & 0xFF;
      int secondBlue = second & 0xFF;
      int red = Math.round((float)firstRed * firstWeight + (float)secondRed * clampedWeight);
      int green = Math.round((float)firstGreen * firstWeight + (float)secondGreen * clampedWeight);
      int blue = Math.round((float)firstBlue * firstWeight + (float)secondBlue * clampedWeight);
      return 0xFF000000 | red << 16 | green << 8 | blue;
   }

   private static void drawShadowedText(class_332 gui, class_327 font, String text, int x, int y, int color, int shadowColor) {
      gui.method_51433(font, text, x + 1, y + 1, shadowColor, false);
      gui.method_51433(font, text, x, y, color, false);
   }

   private static void drawShadowedTextScaled(class_332 gui, class_327 font, String text, int x, int y, int color, int shadowColor, float scale) {
      if (Math.abs(scale - 1.0F) < 0.001F) {
         drawShadowedText(gui, font, text, x, y, color, shadowColor);
      } else {
         float inverseScale = 1.0F / scale;
         int scaledX = Math.round((float)x * inverseScale);
         int scaledY = Math.round((float)y * inverseScale);
         gui.method_51448().method_22903();
         gui.method_51448().method_22905(scale, scale, 1.0F);
         gui.method_51433(font, text, scaledX + 1, scaledY + 1, shadowColor, false);
         gui.method_51433(font, text, scaledX, scaledY, color, false);
         gui.method_51448().method_22909();
      }
   }

   private static int getScaledTextWidth(class_327 font, String text, float scale) {
      if (text != null && !text.isEmpty()) {
         String cacheKey = scale + "|" + text;
         Integer cached = TEXT_WIDTH_CACHE.get(cacheKey);
         if (cached != null) {
            return cached;
         } else {
            int width = Math.round((float)font.method_1727(text) * scale);
            if (TEXT_WIDTH_CACHE.size() >= 512) {
               TEXT_WIDTH_CACHE.clear();
            }

            TEXT_WIDTH_CACHE.put(cacheKey, width);
            return width;
         }
      } else {
         return 0;
      }
   }

   private static String trimToWidth(class_327 font, String text, int maxWidth) {
      return trimToWidth(font, text, maxWidth, 1.0F);
   }

   private static String trimToWidth(class_327 font, String text, int maxWidth, float scale) {
      if (text == null) {
         return "";
      } else if (getScaledTextWidth(font, text, scale) <= maxWidth) {
         return text;
      } else {
         String ellipsis = "...";
         int ellipsisWidth = getScaledTextWidth(font, ellipsis, scale);
         String working = text;

         while (!working.isEmpty() && getScaledTextWidth(font, working, scale) + ellipsisWidth > maxWidth) {
            working = working.substring(0, working.length() - 1);
         }

         return working + ellipsis;
      }
   }

   private static final class TileRenderSnapshot {
      private final long expiresAt;
      private final String speciesName;
      private final String displayName;
      private final SpeedInfo speedInfo;
      private final int speedTextWidth;
      private final List<ElementalType> types;
      private final String teraTypeName;
      private final boolean megaEvolved;
      private final int typeWidth;
      private final String statusText;
      private final int statusWidth;

      private TileRenderSnapshot(
         long expiresAt,
         String speciesName,
         String displayName,
         SpeedInfo speedInfo,
         int speedTextWidth,
         List<ElementalType> types,
         String teraTypeName,
         boolean megaEvolved,
         int typeWidth,
         String statusText,
         int statusWidth
      ) {
         this.expiresAt = expiresAt;
         this.speciesName = speciesName;
         this.displayName = displayName;
         this.speedInfo = speedInfo;
         this.speedTextWidth = speedTextWidth;
         this.types = types;
         this.teraTypeName = teraTypeName;
         this.megaEvolved = megaEvolved;
         this.typeWidth = typeWidth;
         this.statusText = statusText;
         this.statusWidth = statusWidth;
      }
   }
}
