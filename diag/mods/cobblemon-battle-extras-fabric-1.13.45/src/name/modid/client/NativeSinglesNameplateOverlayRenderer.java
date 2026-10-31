package name.modid.client;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.pokemon.status.PersistentStatus;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import name.modid.client.CustomBattleTileRenderer.TilePlacement;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

public final class NativeSinglesNameplateOverlayRenderer {
   private static final class_2960 PLAYER_TEXTURE = class_2960.method_60655("cobblemon-battle-extras", "textures/gui/top_tile_singles_left.png");
   private static final class_2960 OPPONENT_TEXTURE = class_2960.method_60655("cobblemon-battle-extras", "textures/gui/top_tile_singles_right.png");
   private static final class_2960 PLAYER_MULTI_TEXTURE = class_2960.method_60655("cobblemon-battle-extras", "textures/gui/top_tile_multi_left.png");
   private static final class_2960 PLAYER_MULTI_SECONDARY_TEXTURE = class_2960.method_60655("cobblemon-battle-extras", "textures/gui/top_tile_multi_left2.png");
   private static final class_2960 OPPONENT_MULTI_TEXTURE = class_2960.method_60655("cobblemon-battle-extras", "textures/gui/top_tile_multi_right.png");
   private static final class_2960 OPPONENT_MULTI_SECONDARY_TEXTURE = class_2960.method_60655(
      "cobblemon-battle-extras", "textures/gui/top_tile_multi_right2.png"
   );
   private static final int TEXTURE_WIDTH = 140;
   private static final int TEXTURE_HEIGHT = 53;
   private static final int COMPACT_TEXTURE_WIDTH = 128;
   private static final int COMPACT_TEXTURE_HEIGHT = 41;
   private static final int HORIZONTAL_INSET = 12;
   private static final int VERTICAL_INSET = 10;
   private static final int VERTICAL_SPACING = 40;
   private static final int TILE_WIDTH = 140;
   private static final int TILE_HEIGHT = 40;
   private static final int COMPACT_TILE_WIDTH = 128;
   private static final int OVERLAY_Y_OFFSET = -13;
   private static final int PLAYER_COMPACT_SECONDARY_OVERLAY_Y_OFFSET = 0;
   private static final int PLAYER_COMPACT_TERTIARY_OVERLAY_Y_OFFSET = 0;
   private static final int OPPONENT_COMPACT_SECONDARY_OVERLAY_Y_OFFSET = 0;
   private static final int OPPONENT_COMPACT_TERTIARY_OVERLAY_Y_OFFSET = 0;
   private static final int PLAYER_TYPE_ICON_X_OFFSET = 2;
   private static final int OPPONENT_TYPE_ICON_X_OFFSET = -2;
   private static final int TYPE_ICON_Y_OFFSET = 2;
   private static final int PLAYER_STATUS_X_OFFSET = 0;
   private static final int OPPONENT_STATUS_X_OFFSET = 0;
   private static final int STATUS_PILL_Y_OFFSET = 2;
   private static final int SINGLES_MEGA_ICON_Y_OFFSET = 35;
   private static final int PLAYER_SINGLES_MEGA_ICON_X_OFFSET = -1;
   private static final int OPPONENT_SINGLES_MEGA_ICON_X_OFFSET = 3;
   private static final float TEXT_SCALE = 0.85F;

   private NativeSinglesNameplateOverlayRenderer() {
   }

   public static void render(class_332 gui, ActiveClientBattlePokemon activeBattlePokemon, boolean left, int rank, boolean isCompact) {
      if (!BattleExtrasConfig.isHideBattleTilesEnabled()) {
         if (!BattleExtrasConfig.useCustomTopBattleTiles()) {
            boolean renderAttachments = BattleExtrasConfig.isClassicTopTileAttachmentsEnabled();
            float opacity = BattleOpacityHelper.getBattleOpacity();
            if (!(opacity <= 0.01F)) {
               if (isCompact) {
                  renderCompactOverlay(gui, activeBattlePokemon, left, rank, opacity, renderAttachments);
               } else {
                  int screenWidth = gui.method_51421();
                  int x = left ? 12 : screenWidth - 12 - 140;
                  int nativeTileY = 10 + rank * 40;
                  int y = nativeTileY + -13;
                  class_2960 texture = left ? PLAYER_TEXTURE : OPPONENT_TEXTURE;
                  int portraitSize = 28;
                  int portraitInset = 5;
                  int portraitX = left ? x + portraitInset : x + 140 - portraitInset - portraitSize;
                  int portraitY = y + 7;
                  if (renderAttachments) {
                     RenderSystem.enableBlend();
                     RenderSystem.defaultBlendFunc();
                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
                     gui.method_25290(texture, x, y, 0.0F, 0.0F, 140, 53, 140, 53);
                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  }

                  renderMegaBadgeIfNeeded(gui, activeBattlePokemon, left, rank, false, portraitX, portraitY, portraitSize, opacity);
                  renderBottomRow(gui, activeBattlePokemon, left, x, nativeTileY, opacity);
               }
            }
         }
      }
   }

   public static void renderSpeedRange(class_332 gui, ActiveClientBattlePokemon activeBattlePokemon, boolean left, int rank, boolean isCompact, float opacity) {
      if (activeBattlePokemon != null) {
         ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
         ClientBattlePokemon battlePokemon = activeBattlePokemon.getBattlePokemon();
         class_310 mc = class_310.method_1551();
         if (battle != null && battlePokemon != null && mc != null && !(opacity <= 0.01F)) {
            BattleTileSpeedRangeRenderer.renderNativeTile(
               gui, mc, battle, activeBattlePokemon, battlePokemon, rank, left, mc.method_22683().method_4486(), opacity, isCompact
            );
         }
      }
   }

   private static void renderCompactOverlay(
      class_332 gui, ActiveClientBattlePokemon activeBattlePokemon, boolean left, int rank, float opacity, boolean renderAttachments
   ) {
      if (activeBattlePokemon != null) {
         ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
         class_310 mc = class_310.method_1551();
         if (battle != null && mc != null) {
            TilePlacement placement = CustomBattleTileRenderer.calculateTilePlacement(mc, battle, activeBattlePokemon, left, rank, true);
            if (placement != null) {
               int compactIndex = resolveCompactVisibleIndex(rank);
               int x = Math.round(placement.x());
               int y = placement.y() + -13 + resolveCompactOverlayYOffset(left, compactIndex);
               int portraitSize = 19;
               int portraitInset = 4;
               int portraitX = left ? x + portraitInset : x + 128 - portraitInset - portraitSize;
               int portraitY = y + 5;
               class_2960 texture = resolveCompactTexture(left, compactIndex);
               if (renderAttachments) {
                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
                  gui.method_25290(texture, x, y, 0.0F, 0.0F, 128, 41, 128, 41);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               }

               renderMegaBadgeIfNeeded(gui, activeBattlePokemon, left, rank, true, portraitX, portraitY, portraitSize, opacity);
            }
         }
      }
   }

   private static class_2960 resolveCompactTexture(boolean left, int compactIndex) {
      if (compactIndex <= 0) {
         return left ? PLAYER_MULTI_TEXTURE : OPPONENT_MULTI_TEXTURE;
      } else {
         return left ? PLAYER_MULTI_SECONDARY_TEXTURE : OPPONENT_MULTI_SECONDARY_TEXTURE;
      }
   }

   private static int resolveCompactOverlayYOffset(boolean left, int compactIndex) {
      if (left) {
         return switch (compactIndex) {
            case 1 -> 0;
            case 2 -> 0;
            default -> 0;
         };
      } else {
         return switch (compactIndex) {
            case 1 -> 0;
            case 2 -> 0;
            default -> 0;
         };
      }
   }

   private static int resolveCompactVisibleIndex(int rank) {
      return Math.max(0, Math.min(rank, 2));
   }

   private static void renderBottomRow(class_332 gui, ActiveClientBattlePokemon activeBattlePokemon, boolean left, int tileX, int tileY, float opacity) {
      if (activeBattlePokemon != null) {
         ClientBattlePokemon battlePokemon = activeBattlePokemon.getBattlePokemon();
         if (battlePokemon != null) {
            class_327 font = class_310.method_1551().field_1772;
            int portraitSize = 28;
            int portraitInset = 5;
            int portraitX = left ? tileX + portraitInset : tileX + 140 - portraitInset - portraitSize;
            int contentX = left ? portraitX + portraitSize + 6 : tileX + 6;
            int contentRight = left ? tileX + 140 - 6 : portraitX - 6;
            int bottomRowY = tileY + 40 - 14;
            String side = left ? "player" : "opponent";
            String speciesName = battlePokemon.getSpecies().getName();
            String pokemonUuid = battlePokemon.getUuid() != null ? battlePokemon.getUuid().toString() : null;
            List<ElementalType> types = TypeIconRenderer.getDisplayTypes(battlePokemon, speciesName, side, pokemonUuid);
            String teraTypeName = BattleMessageSubscriber.getTeraType(side, speciesName);
            int typeWidth = BattleExtrasConfig.isTypeIconsEnabled() ? CustomBattleTileRenderer.getNativeTileTypeIconRowWidth(types, teraTypeName) : 0;
            String statusText = null;
            PersistentStatus status = battlePokemon.getStatus();
            if (status != null) {
               statusText = status.getShowdownName();
            }

            if ((statusText == null || statusText.isBlank()) && left) {
               statusText = LocalBattlePartyTracker.getEffectivePlayerStatusId(activeBattlePokemon, battlePokemon);
            }

            int statusWidth = CustomBattleTileRenderer.getNativeStatusPillWidth(font, statusText, 0.85F);
            int gap = statusText != null && !statusText.isBlank() ? (left ? 1 : 3) : 3;
            int iconStartX;
            int statusX;
            if (left) {
               iconStartX = contentX;
               statusX = contentX + typeWidth + (typeWidth > 0 ? gap : 0);
            } else {
               iconStartX = contentRight - typeWidth;
               int renderedTypeStartX = iconStartX + 2;
               int preferredStatusX = renderedTypeStartX - (statusWidth > 0 ? statusWidth + 4 : 0);
               statusX = Math.max(contentX, preferredStatusX);
            }

            if (typeWidth > 0) {
               CustomBattleTileRenderer.renderNativeTileTypeIcons(
                  gui, types, left ? iconStartX - 2 + 2 : iconStartX + 2 + -2, bottomRowY + 2, opacity, teraTypeName
               );
            }

            int renderedStatusX = statusX + (left ? 0 : 0);
            int renderedStatusY = bottomRowY + 1 + 2;
            if (statusText != null && !statusText.isBlank() && renderedStatusX >= contentX && renderedStatusX + statusWidth <= contentRight) {
               CustomBattleTileRenderer.renderNativeStatusPill(gui, font, statusText, renderedStatusX, renderedStatusY, opacity, 0.85F);
            }
         }
      }
   }

   private static void renderMegaBadgeIfNeeded(
      class_332 gui,
      ActiveClientBattlePokemon activeBattlePokemon,
      boolean left,
      int rank,
      boolean isCompact,
      int portraitX,
      int portraitY,
      int portraitSize,
      float opacity
   ) {
      if (activeBattlePokemon != null) {
         ClientBattlePokemon battlePokemon = activeBattlePokemon.getBattlePokemon();
         if (battlePokemon != null) {
            String side = left ? "player" : "opponent";
            String speciesName = battlePokemon.getSpecies().getName();
            String displayName = battlePokemon.getDisplayName().getString();
            if (CustomBattleTileRenderer.isMegaEvolvedForTile(battlePokemon, side, speciesName, displayName)) {
               int adjustedPortraitX = portraitX;
               int adjustedPortraitY = portraitY;
               if (!isCompact) {
                  adjustedPortraitX = portraitX + (left ? -1 : 3);
                  adjustedPortraitY = portraitY + 35;
               } else if (isCompact && (rank == 0 || rank == 1)) {
                  adjustedPortraitX = portraitX + (left ? -2 : 2);
                  adjustedPortraitY = portraitY + 29;
               } else if (isCompact && rank == 2) {
                  adjustedPortraitX = portraitX + (left ? -2 : 2);
                  adjustedPortraitY = portraitY + 29;
               }

               CustomBattleTileRenderer.renderMegaPortraitBadge(gui, adjustedPortraitX, adjustedPortraitY, portraitSize, left, opacity);
            }
         }
      }
   }
}
