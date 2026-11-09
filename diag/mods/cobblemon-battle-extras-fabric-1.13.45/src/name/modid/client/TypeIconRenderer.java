package name.modid.client;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.cobblemon.mod.common.pokemon.status.PersistentStatus;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import name.modid.CobblemonBattleInfoClient;
import name.modid.client.CustomBattleTileRenderer.SpeedInfo;
import name.modid.client.CustomBattleTileRenderer.TilePlacement;
import name.modid.client.TypeIconRenderer.CachedDisplayTypes;
import name.modid.client.TypeIconRenderer.TypeTooltipData;
import name.modid.platform.Services;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

public class TypeIconRenderer {
   private static final int STELLAR_TOOLTIP_BORDER = -2510003;
   private static final int TYPELESS_TOOLTIP_BORDER = -7364427;
   private static final float COMPACT_STATUS_TEXT_SCALE = 0.68F;
   private static final float COMPACT_BATTLE_TILE_OVERLAY_Z_LAYER = 200.0F;
   private static final long DISPLAY_TYPES_CACHE_TTL_MS = 75L;
   private static final Map<String, CachedDisplayTypes> DISPLAY_TYPES_CACHE = new ConcurrentHashMap<>();
   private static final class_2960 TYPE_SPRITE_SHEET = class_2960.method_60654("cobblemon:textures/gui/types.png");
   private static final String MEGA_SHOWDOWN_TERA_PATH = "mega_showdown:textures/gui/summary/tera_types/";
   private static final boolean MEGA_SHOWDOWN_LOADED = Services.PLATFORM.isModLoaded("mega_showdown");
   private static boolean RANDOMIZER_REFLECTION_CHECKED = false;
   private static boolean RANDOMIZER_REFLECTION_AVAILABLE = false;
   private static Method RANDOMIZER_HAS_OPPONENT_TYPES;
   private static Method RANDOMIZER_GET_OPPONENT_TYPES;
   private static Method RANDOMIZER_GET_WILD_TYPES;
   private static Method RANDOMIZER_IS_RUN_ACTIVE;
   private static Method RANDOMIZER_IS_RANDOM_TYPES;
   private static Method RANDOMIZER_GET_TYPES_FOR_POKEMON;
   private static Method RANDOMIZER_GET_TYPES_FOR_SPECIES;
   private static final String[] RANDOMIZER_TYPE_IDS = new String[]{
      "electric",
      "fighting",
      "psychic",
      "poison",
      "ground",
      "dragon",
      "normal",
      "flying",
      "ghost",
      "steel",
      "grass",
      "water",
      "fairy",
      "rock",
      "dark",
      "fire",
      "bug",
      "ice"
   };
   private static boolean ELEMENTAL_TYPES_REFLECTION_CHECKED = false;
   private static boolean ELEMENTAL_TYPES_REFLECTION_AVAILABLE = false;
   private static Method ELEMENTAL_TYPES_GET_METHOD;
   private static final class_2960 MEGA_ICON = class_2960.method_60654("cobblemon-battle-extras:textures/gui/mega_icon.png");
   private static final int MEGA_ICON_SIZE = 64;
   private static final int MEGA_ICON_DISPLAY_SIZE = 8;
   private static int MEGA_ICON_OFFSET_X_PLAYER = 14;
   private static int MEGA_ICON_OFFSET_Y_PLAYER = -15;
   private static int MEGA_ICON_OFFSET_X_OPPONENT = 30;
   private static int MEGA_ICON_OFFSET_Y_OPPONENT = -15;
   private static int MEGA_ICON_OFFSET_X_OPPONENT_SINGLE = 15;
   private static int MEGA_ICON_OFFSET_Y_OPPONENT_SINGLE = -15;
   private static final int ICON_WIDTH = 36;
   private static final int ICON_HEIGHT = 36;
   private static final int SPRITE_SHEET_WIDTH = 648;
   private static final int SPRITE_SHEET_HEIGHT = 36;
   private static final class_2960 TYPELESS_ICON = class_2960.method_60654("cobblemon-battle-extras:textures/gui/typeless.png");
   private static final int TYPELESS_ICON_SIZE = 36;
   private static final int TERA_ICON_SIZE = 32;
   private static final int DISPLAY_SIZE = 14;
   private static final int TERA_DISPLAY_SIZE = 18;
   private static final int COMPACT_ROW_DISPLAY_SIZE = 8;
   private static final int COMPACT_ROW_TERA_DISPLAY_SIZE = 10;
   private static final int COMPACT_ROW_ICON_SPACING = 1;
   private static final int COMPACT_NATIVE_CLASSIC_SECOND_SLOT_Y_OFFSET = 4;
   private static final int COMPACT_NATIVE_CLASSIC_SECOND_SLOT_STATUS_Y_ADJUST = -4;
   private static final int COMPACT_NATIVE_CLASSIC_THIRD_SLOT_Y_OFFSET = 8;
   private static final int COMPACT_NATIVE_CLASSIC_THIRD_SLOT_STATUS_Y_OFFSET = 0;
   private static final int ICON_SPACING = 1;

   public static void init() {
      if (!Services.PLATFORM.isModLoaded("cobblemon")) {
         CobblemonBattleInfoClient.debug("Cobblemon not found, type icons disabled", new Object[0]);
      } else {
         Services.EVENTS.registerHudRender((guiGraphics, tickDelta) -> {
            try {
               renderTypeIcons(guiGraphics);
            } catch (Throwable var3) {
            }
         });
         CobblemonBattleInfoClient.debug("Type icon renderer initialized", new Object[0]);
      }
   }

   private static void renderTypeIcons(class_332 gui) {
      if (BattleExtrasConfig.isTypeIconsEnabled()) {
         if (!BattleExtrasConfig.isHideBattleTilesEnabled() && !BattleExtrasConfig.useCustomTopBattleTiles()) {
            ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
            if (battle != null) {
               boolean isCompact = battle.getBattleFormat().getBattleType().getPokemonPerSide() > 1;
               if (BattleExtrasConfig.useCustomTopBattleTiles()) {
                  float opacity = BattleOpacityHelper.getBattleOpacity();
                  class_310 mc = class_310.method_1551();
                  int screenWidth = mc.method_22683().method_4486();
                  int screenHeight = mc.method_22683().method_4502();
                  int mouseX = (int)(mc.field_1729.method_1603() * (double)screenWidth / (double)mc.method_22683().method_4480());
                  int mouseY = (int)(mc.field_1729.method_1604() * (double)screenHeight / (double)mc.method_22683().method_4507());
                  TypeTooltipData hoveredTypeTooltip = null;
                  int HORIZONTAL_INSET = 12;
                  int VERTICAL_INSET = 10;
                  int VERTICAL_SPACING = 40;
                  int COMPACT_VERTICAL_SPACING = 30;
                  ClientBattleSide playerSide = BattleSideHelper.getPlayerSide(battle);
                  ClientBattleSide opponentSide = BattleSideHelper.getOpponentSide(battle);
                  int playerSlot = 0;

                  for (ClientBattleActor actor : playerSide.getActors()) {
                     for (ActiveClientBattlePokemon activePokemon : actor.getActivePokemon()) {
                        ClientBattlePokemon battlePokemon = activePokemon.getBattlePokemon();
                        if (battlePokemon != null) {
                           String pokemonName = battlePokemon.getSpecies().getName();
                           String pokemonUUID = battlePokemon.getUuid().toString();
                           boolean isMegaEvolved = BattleMessageSubscriber.isMegaEvolved("player", pokemonName.toLowerCase());
                           if (!isMegaEvolved) {
                              try {
                                 Method getFormMethod = battlePokemon.getClass().getMethod("getForm");
                                 Object form = getFormMethod.invoke(battlePokemon);
                                 if (form != null) {
                                    Method getNameMethod = form.getClass().getMethod("getName");
                                    String formName = (String)getNameMethod.invoke(form);
                                    if (formName != null && formName.toLowerCase().contains("mega")) {
                                       isMegaEvolved = true;
                                    }
                                 }
                              } catch (Exception var39) {
                              }

                              if (!isMegaEvolved) {
                                 try {
                                    Method getPropsMethod = battlePokemon.getClass().getMethod("getProperties");
                                    Object props = getPropsMethod.invoke(battlePokemon);
                                    if (props != null) {
                                       Method getFormMethod = props.getClass().getMethod("getForm");
                                       Object formName = getFormMethod.invoke(props);
                                       if (formName != null) {
                                          String formStr = formName.toString();
                                          if (formStr.toLowerCase().contains("mega")) {
                                             isMegaEvolved = true;
                                          }
                                       }
                                    }
                                 } catch (Exception var38) {
                                 }
                              }

                              if (!isMegaEvolved) {
                                 try {
                                    Method getAspectsMethod = battlePokemon.getClass().getMethod("getAspects");
                                    Object aspects = getAspectsMethod.invoke(battlePokemon);
                                    if (aspects != null) {
                                       String aspectsStr = aspects.toString().toLowerCase();
                                       if (aspectsStr.contains("mega")) {
                                          isMegaEvolved = true;
                                       }
                                    }
                                 } catch (Exception var37) {
                                 }
                              }
                           }

                           List<ElementalType> displayTypes = getDisplayTypes(battlePokemon, pokemonName, "player", pokemonUUID);
                           int baseX;
                           int baseY;
                           if (isCompact) {
                              baseX = 0;
                              baseY = 0;
                           } else {
                              baseX = 12;
                              baseY = 10 + playerSlot * 40 + 40;
                           }

                           boolean isTerastallized = BattleMessageSubscriber.isTerastallized("player", pokemonName);
                           boolean bottomVisibleSlot = isCompact && isBottomVisibleCompactSlot(mc, battle, activePokemon, true, playerSlot);
                           if (displayTypes == null) {
                              TypeTooltipData hovered = isCompact
                                 ? renderCompactBattleTileStellarType(
                                    gui, mc, battle, activePokemon, true, playerSlot, bottomVisibleSlot, opacity, mouseX, mouseY
                                 )
                                 : renderStellarType(gui, baseX, baseY, opacity, mouseX, mouseY);
                              if (hoveredTypeTooltip == null) {
                                 hoveredTypeTooltip = hovered;
                              }

                              if (isMegaEvolved) {
                                 renderMegaIcon(gui, baseX, baseY, opacity, true, 1);
                              }
                           } else if (displayTypes.isEmpty() && isTypeless("player", pokemonName)) {
                              TypeTooltipData hoveredx = isCompact
                                 ? renderCompactBattleTileTypelessIcon(
                                    gui, mc, battle, activePokemon, true, playerSlot, bottomVisibleSlot, opacity, mouseX, mouseY
                                 )
                                 : renderTypelessIcon(gui, baseX, baseY, opacity, mouseX, mouseY);
                              if (hoveredTypeTooltip == null) {
                                 hoveredTypeTooltip = hoveredx;
                              }

                              if (isMegaEvolved) {
                                 renderMegaIcon(gui, baseX, baseY, opacity, true, 1);
                              }
                           } else {
                              if (isCompact) {
                                 TypeTooltipData hoveredxx = renderCompactBattleTileTypes(
                                    gui, mc, battle, activePokemon, true, playerSlot, bottomVisibleSlot, displayTypes, opacity, isTerastallized, mouseX, mouseY
                                 );
                                 if (hoveredTypeTooltip == null) {
                                    hoveredTypeTooltip = hoveredxx;
                                 }
                              } else {
                                 TypeTooltipData hoveredxx = renderTypesRow(gui, displayTypes, baseX, baseY, opacity, isTerastallized, mouseX, mouseY);
                                 if (hoveredTypeTooltip == null) {
                                    hoveredTypeTooltip = hoveredxx;
                                 }
                              }

                              if (isMegaEvolved) {
                                 renderMegaIcon(gui, baseX, baseY, opacity, true, displayTypes.size());
                              }
                           }

                           playerSlot++;
                        }
                     }
                  }

                  int totalOpponentPokemon = 0;

                  for (ClientBattleActor actor : opponentSide.getActors()) {
                     for (ActiveClientBattlePokemon activePokemonx : actor.getActivePokemon()) {
                        if (activePokemonx.getBattlePokemon() != null) {
                           totalOpponentPokemon++;
                        }
                     }
                  }

                  int opponentSlot = 0;

                  for (ClientBattleActor actor : opponentSide.getActors()) {
                     for (ActiveClientBattlePokemon activePokemonxx : actor.getActivePokemon()) {
                        ClientBattlePokemon battlePokemon = activePokemonxx.getBattlePokemon();
                        if (battlePokemon != null) {
                           int compactOpponentRank = isCompact ? Math.max(0, totalOpponentPokemon - 1 - opponentSlot) : opponentSlot;
                           String pokemonNamex = battlePokemon.getSpecies().getName();
                           String pokemonUUIDx = battlePokemon.getUuid().toString();
                           boolean isMegaEvolvedx = BattleMessageSubscriber.isMegaEvolved("opponent", pokemonNamex.toLowerCase());
                           if (!isMegaEvolvedx) {
                              try {
                                 Method getFormMethod = battlePokemon.getClass().getMethod("getForm");
                                 Object form = getFormMethod.invoke(battlePokemon);
                                 if (form != null) {
                                    Method getNameMethod = form.getClass().getMethod("getName");
                                    String formName = (String)getNameMethod.invoke(form);
                                    if (formName != null && formName.toLowerCase().contains("mega")) {
                                       isMegaEvolvedx = true;
                                    }
                                 }
                              } catch (Exception var36) {
                              }

                              if (!isMegaEvolvedx) {
                                 try {
                                    Method getPropsMethod = battlePokemon.getClass().getMethod("getProperties");
                                    Object props = getPropsMethod.invoke(battlePokemon);
                                    if (props != null) {
                                       Method getFormMethod = props.getClass().getMethod("getForm");
                                       Object formName = getFormMethod.invoke(props);
                                       if (formName != null) {
                                          String formStr = formName.toString();
                                          if (formStr.toLowerCase().contains("mega")) {
                                             isMegaEvolvedx = true;
                                          }
                                       }
                                    }
                                 } catch (Exception var34) {
                                 }
                              }

                              if (!isMegaEvolvedx) {
                                 try {
                                    Method getAspectsMethod = battlePokemon.getClass().getMethod("getAspects");
                                    Object aspects = getAspectsMethod.invoke(battlePokemon);
                                    if (aspects != null) {
                                       String aspectsStr = aspects.toString().toLowerCase();
                                       if (aspectsStr.contains("mega")) {
                                          isMegaEvolvedx = true;
                                       }
                                    }
                                 } catch (Exception var35) {
                                 }
                              }
                           }

                           List<ElementalType> displayTypesx = getDisplayTypes(battlePokemon, pokemonNamex, "opponent", pokemonUUIDx);
                           int baseXx;
                           int baseYx;
                           if (isCompact) {
                              baseXx = 0;
                              baseYx = 0;
                           } else {
                              baseXx = screenWidth - 15;
                              baseYx = 10 + opponentSlot * 40 + 40;
                           }

                           boolean isTerastallized = BattleMessageSubscriber.isTerastallized("opponent", pokemonNamex);
                           boolean bottomVisibleSlot = isCompact && isBottomVisibleCompactSlot(mc, battle, activePokemonxx, false, compactOpponentRank);
                           if (isMegaEvolvedx) {
                              CobblemonBattleInfoClient.debug("[TypeIconRenderer] OPPONENT Pokemon: {} - MEGA DETECTED!", new Object[]{pokemonNamex});
                           }

                           if (displayTypesx == null) {
                              TypeTooltipData hoveredxx = isCompact
                                 ? renderCompactBattleTileStellarType(
                                    gui, mc, battle, activePokemonxx, false, compactOpponentRank, bottomVisibleSlot, opacity, mouseX, mouseY
                                 )
                                 : renderStellarTypeReversed(gui, baseXx, baseYx, opacity, mouseX, mouseY);
                              if (hoveredTypeTooltip == null) {
                                 hoveredTypeTooltip = hoveredxx;
                              }

                              if (isMegaEvolvedx) {
                                 renderMegaIcon(gui, baseXx, baseYx, opacity, false, 1);
                              }
                           } else if (displayTypesx.isEmpty() && isTypeless("opponent", pokemonNamex)) {
                              TypeTooltipData hoveredxxx = isCompact
                                 ? renderCompactBattleTileTypelessIcon(
                                    gui, mc, battle, activePokemonxx, false, compactOpponentRank, bottomVisibleSlot, opacity, mouseX, mouseY
                                 )
                                 : renderTypelessIconReversed(gui, baseXx, baseYx, opacity, mouseX, mouseY);
                              if (hoveredTypeTooltip == null) {
                                 hoveredTypeTooltip = hoveredxxx;
                              }

                              if (isMegaEvolvedx) {
                                 renderMegaIcon(gui, baseXx, baseYx, opacity, false, 1);
                              }
                           } else {
                              if (isCompact) {
                                 TypeTooltipData hoveredxxxx = renderCompactBattleTileTypes(
                                    gui,
                                    mc,
                                    battle,
                                    activePokemonxx,
                                    false,
                                    compactOpponentRank,
                                    bottomVisibleSlot,
                                    displayTypesx,
                                    opacity,
                                    isTerastallized,
                                    mouseX,
                                    mouseY
                                 );
                                 if (hoveredTypeTooltip == null) {
                                    hoveredTypeTooltip = hoveredxxxx;
                                 }
                              } else {
                                 TypeTooltipData hoveredxxxx = renderTypesRowReversed(
                                    gui, displayTypesx, baseXx, baseYx, opacity, isTerastallized, mouseX, mouseY
                                 );
                                 if (hoveredTypeTooltip == null) {
                                    hoveredTypeTooltip = hoveredxxxx;
                                 }
                              }

                              if (isMegaEvolvedx) {
                                 renderMegaIcon(gui, baseXx, baseYx, opacity, false, displayTypesx.size());
                              }
                           }

                           opponentSlot++;
                        }
                     }
                  }

                  if (hoveredTypeTooltip != null && !hoveredTypeTooltip.text.getString().isBlank()) {
                     renderTypeTooltip(gui, mouseX, mouseY, hoveredTypeTooltip);
                  }
               }
            }
         }
      }
   }

   public static List<ElementalType> getDisplayTypes(Object battlePokemon, String pokemonName) {
      return getDisplayTypes(battlePokemon, pokemonName, null, null);
   }

   public static List<ElementalType> getDisplayTypes(Object battlePokemon, String pokemonName, String side) {
      return getDisplayTypes(battlePokemon, pokemonName, side, null);
   }

   public static List<ElementalType> getDisplayTypes(Object battlePokemon, String pokemonName, String side, String pokemonUUID) {
      return getDisplayTypesInternal(battlePokemon, pokemonName, side, pokemonUUID, new HashSet<>());
   }

   public static void renderCompactNativeTileOverlay(class_332 gui, ActiveClientBattlePokemon activePokemon, boolean isPlayerSide, int rank, float opacity) {
      if (BattleExtrasConfig.isTypeIconsEnabled()
         && !BattleExtrasConfig.isHideBattleTilesEnabled()
         && !BattleExtrasConfig.useCustomTopBattleTiles()
         && activePokemon != null
         && !(opacity <= 0.01F)) {
         ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
         ClientBattlePokemon battlePokemon = activePokemon.getBattlePokemon();
         class_310 mc = class_310.method_1551();
         if (battle != null && battlePokemon != null && mc != null) {
            int screenWidth = mc.method_22683().method_4486();
            int screenHeight = mc.method_22683().method_4502();
            int mouseX = (int)(mc.field_1729.method_1603() * (double)screenWidth / (double)mc.method_22683().method_4480());
            int mouseY = (int)(mc.field_1729.method_1604() * (double)screenHeight / (double)mc.method_22683().method_4507());
            String side = isPlayerSide ? "player" : "opponent";
            String pokemonName = battlePokemon.getSpecies().getName();
            String pokemonUUID = battlePokemon.getUuid() != null ? battlePokemon.getUuid().toString() : null;
            List<ElementalType> displayTypes = getDisplayTypes(battlePokemon, pokemonName, side, pokemonUUID);
            boolean isTerastallized = BattleMessageSubscriber.isTerastallized(side, pokemonName);
            boolean bottomVisibleSlot = rank == 2
               && battle.getBattleFormat() != null
               && battle.getBattleFormat().getBattleType() != null
               && battle.getBattleFormat().getBattleType().getPokemonPerSide() >= 3;
            TypeTooltipData hoveredTypeTooltip;
            if (displayTypes == null) {
               hoveredTypeTooltip = renderCompactBattleTileStellarType(
                  gui, mc, battle, activePokemon, isPlayerSide, rank, bottomVisibleSlot, opacity, mouseX, mouseY
               );
            } else if (displayTypes.isEmpty() && isTypeless(side, pokemonName)) {
               hoveredTypeTooltip = renderCompactBattleTileTypelessIcon(
                  gui, mc, battle, activePokemon, isPlayerSide, rank, bottomVisibleSlot, opacity, mouseX, mouseY
               );
            } else {
               hoveredTypeTooltip = renderCompactBattleTileTypes(
                  gui, mc, battle, activePokemon, isPlayerSide, rank, bottomVisibleSlot, displayTypes, opacity, isTerastallized, mouseX, mouseY
               );
            }

            if (hoveredTypeTooltip != null && !hoveredTypeTooltip.text.getString().isBlank()) {
               renderTypeTooltip(gui, mouseX, mouseY, hoveredTypeTooltip);
            }
         }
      }
   }

   private static List<ElementalType> getDisplayTypesInternal(
      Object battlePokemon, String pokemonName, String side, String pokemonUUID, Set<String> reflectVisitKeys
   ) {
      String cacheKey = buildDisplayTypesCacheKey(battlePokemon, pokemonName, side, pokemonUUID);
      long now = System.currentTimeMillis();
      if (cacheKey != null) {
         CachedDisplayTypes cached = DISPLAY_TYPES_CACHE.get(cacheKey);
         if (cached != null && cached.expiresAt >= now) {
            return cached.types;
         }
      }

      List<ElementalType> result = new ArrayList<>();
      String reflectVisitKey = side != null && pokemonName != null ? side + ":" + pokemonName.toLowerCase(Locale.ROOT) : null;
      String teraType = BattleMessageSubscriber.getTeraType(side, pokemonName);
      if (teraType != null && !teraType.isEmpty()) {
         if (teraType.equalsIgnoreCase("Stellar")) {
            return null;
         }

         ElementalType teraElementType = stringToElementalType(teraType);
         if (teraElementType != null) {
            result.add(teraElementType);
            return cacheDisplayTypes(cacheKey, now, result);
         }
      }

      if (BattleMessageSubscriber.isSoaked(side, pokemonName)) {
         ElementalType waterType = stringToElementalType("Water");
         if (waterType != null) {
            result.add(waterType);
            String addedType = BattleMessageSubscriber.getAddedType(side, pokemonName);
            if (addedType != null && !addedType.isEmpty()) {
               ElementalType addType = stringToElementalType(addedType);
               if (addType != null && !result.contains(addType)) {
                  result.add(addType);
               }
            }

            return cacheDisplayTypes(cacheKey, now, result);
         }
      }

      String transformedType = BattleMessageSubscriber.getTransformedType(side, pokemonName);
      if (transformedType != null && !transformedType.isEmpty()) {
         ElementalType transformType = stringToElementalType(transformedType);
         if (transformType != null) {
            result.add(transformType);
            CobblemonBattleInfoClient.debug(
               "[TypeIconRenderer] Found transformed type {} for Pokemon '{}' side '{}'", new Object[]{transformedType, pokemonName, side}
            );
            String addedType = BattleMessageSubscriber.getAddedType(side, pokemonName);
            if (addedType != null && !addedType.isEmpty()) {
               ElementalType addType = stringToElementalType(addedType);
               if (addType != null && !result.contains(addType)) {
                  result.add(addType);
               }
            }

            return cacheDisplayTypes(cacheKey, now, result);
         }
      }

      String reflectTarget = BattleMessageSubscriber.getReflectTypeTarget(side, pokemonName);
      if (reflectTarget != null && !reflectTarget.isEmpty() && reflectVisitKey != null && reflectVisitKeys.add(reflectVisitKey)) {
         List<ElementalType> targetTypes = getReflectTargetTypes(reflectTarget, reflectVisitKeys);
         reflectVisitKeys.remove(reflectVisitKey);
         if (targetTypes != null) {
            result.addAll(targetTypes);
            return cacheDisplayTypes(cacheKey, now, result);
         }
      }

      boolean isMegaEvolved = BattleMessageSubscriber.isMegaEvolved(side, pokemonName != null ? pokemonName.toLowerCase() : "");
      String megaFormName = null;
      if (!isMegaEvolved) {
         try {
            Method getForm = battlePokemon.getClass().getMethod("getForm");
            Object form = getForm.invoke(battlePokemon);
            if (form != null) {
               Method formNameMethod = form.getClass().getMethod("getName");
               String formName = (String)formNameMethod.invoke(form);
               if (formName != null && formName.toLowerCase().contains("mega")) {
                  isMegaEvolved = true;
                  megaFormName = formName;
               }
            }
         } catch (Throwable var37) {
         }

         if (!isMegaEvolved) {
            try {
               Method getProps = battlePokemon.getClass().getMethod("getProperties");
               Object props = getProps.invoke(battlePokemon);
               if (props != null) {
                  Method getFormMethod = props.getClass().getMethod("getForm");
                  Object formName = getFormMethod.invoke(props);
                  if (formName != null) {
                     String formStr = formName.toString();
                     if (formStr.toLowerCase().contains("mega")) {
                        isMegaEvolved = true;
                        megaFormName = formStr;
                     }
                  }
               }
            } catch (Throwable var36) {
            }
         }
      }

      if (isMegaEvolved) {
         List<ElementalType> entityFormTypes = getTypesFromPokemonEntity(pokemonUUID, pokemonName);
         if (entityFormTypes != null && !entityFormTypes.isEmpty()) {
            CobblemonBattleInfoClient.debug(
               "[TypeIconRenderer] Got {} mega types from Pokemon entity for {} (UUID: {})", new Object[]{entityFormTypes.size(), pokemonName, pokemonUUID}
            );
            if (BattleMessageSubscriber.isRoosted(side, pokemonName)) {
               for (ElementalType type : entityFormTypes) {
                  if (!type.getName().equalsIgnoreCase("flying")) {
                     result.add(type);
                  }
               }

               if (result.isEmpty()) {
                  ElementalType normalType = stringToElementalType("Normal");
                  if (normalType != null) {
                     result.add(normalType);
                  }
               }
            } else {
               result.addAll(entityFormTypes);
            }

            return cacheDisplayTypes(cacheKey, now, result);
         }

         try {
            Method getForm = battlePokemon.getClass().getMethod("getForm");
            Object form = getForm.invoke(battlePokemon);
            if (form != null) {
               Method getTypes = form.getClass().getMethod("getTypes");
               Object types = getTypes.invoke(form);
               if (types instanceof Iterable) {
                  for (Object typex : (Iterable)types) {
                     if (typex instanceof ElementalType) {
                        result.add((ElementalType)typex);
                     }
                  }
               }

               if (!result.isEmpty()) {
                  CobblemonBattleInfoClient.debug(
                     "[TypeIconRenderer] Got {} mega types from form for {} (form={})", new Object[]{result.size(), pokemonName, megaFormName}
                  );
                  if (BattleMessageSubscriber.isRoosted(side, pokemonName)) {
                     result.removeIf(typexx -> typexx.getName().equalsIgnoreCase("flying"));
                     if (result.isEmpty()) {
                        ElementalType normalType = stringToElementalType("Normal");
                        if (normalType != null) {
                           result.add(normalType);
                        }
                     }
                  }

                  return cacheDisplayTypes(cacheKey, now, result);
               }
            }
         } catch (Throwable var46) {
            CobblemonBattleInfoClient.debug("[TypeIconRenderer] Failed to get mega form types: {}", new Object[]{var46.getMessage()});
         }

         List<String> megaTypes = BattleMessageSubscriber.getMegaTypes(side, pokemonName);
         if (megaTypes != null && !megaTypes.isEmpty()) {
            for (String typeName : megaTypes) {
               ElementalType megaType = stringToElementalType(typeName);
               if (megaType != null) {
                  result.add(megaType);
               }
            }

            if (!result.isEmpty()) {
               if (BattleMessageSubscriber.isRoosted(side, pokemonName)) {
                  result.removeIf(typexx -> typexx.getName().equalsIgnoreCase("flying"));
                  if (result.isEmpty()) {
                     ElementalType normalType = stringToElementalType("Normal");
                     if (normalType != null) {
                        result.add(normalType);
                     }
                  }
               }

               return cacheDisplayTypes(cacheKey, now, result);
            }
         }

         try {
            Method getForm = battlePokemon.getClass().getMethod("getForm");
            Object form = getForm.invoke(battlePokemon);
            if (form != null) {
               Method getTypesx = form.getClass().getMethod("getTypes");
               Object typesx = getTypesx.invoke(form);
               if (typesx instanceof Iterable) {
                  for (Object typexx : (Iterable)typesx) {
                     if (typexx instanceof ElementalType) {
                        result.add((ElementalType)typexx);
                     }
                  }
               }

               if (!result.isEmpty()) {
                  if (BattleMessageSubscriber.isRoosted(side, pokemonName)) {
                     result.removeIf(typexxx -> typexxx.getName().equalsIgnoreCase("flying"));
                     if (result.isEmpty()) {
                        ElementalType normalType = stringToElementalType("Normal");
                        if (normalType != null) {
                           result.add(normalType);
                        }
                     }
                  }

                  return cacheDisplayTypes(cacheKey, now, result);
               }
            }
         } catch (Throwable var45) {
            CobblemonBattleInfoClient.debug("[TypeIconRenderer] Failed to get mega form types: {}", new Object[]{var45.getMessage()});
         }
      }

      String meloettaForm = BattleMessageSubscriber.getMeloettaForm(pokemonName);
      if (meloettaForm != null && pokemonName.toLowerCase().contains("meloetta")) {
         if (meloettaForm.equals("pirouette")) {
            ElementalType normalType = stringToElementalType("Normal");
            ElementalType fightingType = stringToElementalType("Fighting");
            if (normalType != null) {
               result.add(normalType);
            }

            if (fightingType != null) {
               result.add(fightingType);
            }

            return cacheDisplayTypes(cacheKey, now, result);
         }

         if (meloettaForm.equals("aria")) {
            ElementalType normalTypex = stringToElementalType("Normal");
            ElementalType psychicType = stringToElementalType("Psychic");
            if (normalTypex != null) {
               result.add(normalTypex);
            }

            if (psychicType != null) {
               result.add(psychicType);
            }

            return cacheDisplayTypes(cacheKey, now, result);
         }
      }

      List<ElementalType> baseTypes = new ArrayList<>();
      if (pokemonUUID != null && !pokemonUUID.isEmpty()) {
         try {
            appendRandomizedTypes(baseTypes, pokemonName, pokemonUUID, side);
            if (baseTypes.isEmpty()) {
               appendRandomizedTypesFromShowdownId(baseTypes, battlePokemon, pokemonName, pokemonUUID, side);
            }

            if (!baseTypes.isEmpty()) {
               CobblemonBattleInfoClient.debug(
                  "[TypeIconRenderer] Got {} randomized types for {} (UUID: {}, side: {})", new Object[]{baseTypes.size(), pokemonName, pokemonUUID, side}
               );
               if (BattleMessageSubscriber.isRoosted(side, pokemonName)) {
                  baseTypes.removeIf(typexxx -> typexxx.getName().equalsIgnoreCase("flying"));
                  if (baseTypes.isEmpty()) {
                     ElementalType normalTypexx = stringToElementalType("Normal");
                     if (normalTypexx != null) {
                        baseTypes.add(normalTypexx);
                     }
                  }
               }

               String addedType = BattleMessageSubscriber.getAddedType(side, pokemonName);
               if (addedType != null && !addedType.isEmpty()) {
                  ElementalType addType = stringToElementalType(addedType);
                  if (addType != null && !baseTypes.contains(addType)) {
                     baseTypes.add(addType);
                  }
               }

               result.addAll(baseTypes);
               return result;
            }
         } catch (Throwable var35) {
            CobblemonBattleInfoClient.debug("[TypeIconRenderer] Failed to check randomizer types: {}", new Object[]{var35.getMessage()});
         }
      }

      if (pokemonUUID != null && !pokemonUUID.isEmpty()) {
         List<ElementalType> entityTypes = getTypesFromPokemonEntity(pokemonUUID, pokemonName);
         if (entityTypes != null && !entityTypes.isEmpty()) {
            baseTypes.addAll(entityTypes);
            CobblemonBattleInfoClient.debug(
               "[TypeIconRenderer] Got {} types from Pokemon entity for {} (UUID: {})", new Object[]{baseTypes.size(), pokemonName, pokemonUUID}
            );
         }
      }

      if (baseTypes.isEmpty()) {
         try {
            Method getProperties = battlePokemon.getClass().getMethod("getProperties");
            Object properties = getProperties.invoke(battlePokemon);
            if (properties != null) {
               Method getFormMethod = properties.getClass().getMethod("getForm");
               Object formName = getFormMethod.invoke(properties);
               Method getSpeciesMethod = battlePokemon.getClass().getMethod("getSpecies");
               Object species = getSpeciesMethod.invoke(battlePokemon);
               if (formName != null && species != null) {
                  String formNameStr = formName.toString();
                  CobblemonBattleInfoClient.debug("[TypeIconRenderer] Found form '{}' for {}", new Object[]{formNameStr, pokemonName});

                  try {
                     Method getFormByName = species.getClass().getMethod("getForm", String.class);
                     Object form = getFormByName.invoke(species, formNameStr);
                     if (form != null) {
                        Method getTypesxx = form.getClass().getMethod("getTypes");
                        Object typesxx = getTypesxx.invoke(form);
                        if (typesxx instanceof Iterable) {
                           for (Object typexxx : (Iterable)typesxx) {
                              if (typexxx instanceof ElementalType) {
                                 baseTypes.add((ElementalType)typexxx);
                              }
                           }
                        }

                        CobblemonBattleInfoClient.debug(
                           "[TypeIconRenderer] Got {} types from species.getForm('{}') for {}", new Object[]{baseTypes.size(), formNameStr, pokemonName}
                        );
                     }
                  } catch (NoSuchMethodException var43) {
                     try {
                        Method getForms = species.getClass().getMethod("getForms");
                        Object forms = getForms.invoke(species);
                        if (forms instanceof Iterable) {
                           for (Object formx : (Iterable)forms) {
                              Method getName = formx.getClass().getMethod("getName");
                              String name = getName.invoke(formx).toString();
                              if (name.equalsIgnoreCase(formNameStr)) {
                                 Method getTypesxx = formx.getClass().getMethod("getTypes");
                                 Object typesxx = getTypesxx.invoke(formx);
                                 if (typesxx instanceof Iterable) {
                                    for (Object typexxxx : (Iterable)typesxx) {
                                       if (typexxxx instanceof ElementalType) {
                                          baseTypes.add((ElementalType)typexxxx);
                                       }
                                    }
                                 }

                                 CobblemonBattleInfoClient.debug(
                                    "[TypeIconRenderer] Got {} types from iterating forms for {}", new Object[]{baseTypes.size(), pokemonName}
                                 );
                                 break;
                              }
                           }
                        }
                     } catch (Throwable var42) {
                        CobblemonBattleInfoClient.debug("[TypeIconRenderer] Failed to iterate forms: {}", new Object[]{var42.getMessage()});
                     }
                  }
               }
            }
         } catch (Throwable var44) {
            CobblemonBattleInfoClient.debug("[TypeIconRenderer] Failed to get form from properties: {}", new Object[]{var44.getMessage()});
         }
      }

      if (baseTypes.isEmpty()) {
         Object pokemon = null;
         String[] pokemonGetterMethods = new String[]{"getEffectedPokemon", "effectedPokemon", "getPokemon", "pokemon", "getOriginalPokemon"};

         for (String methodName : pokemonGetterMethods) {
            try {
               Method method = battlePokemon.getClass().getMethod(methodName);
               pokemon = method.invoke(battlePokemon);
               if (pokemon != null) {
                  CobblemonBattleInfoClient.debug("[TypeIconRenderer] Got Pokemon via {} for {}", new Object[]{methodName, pokemonName});
                  break;
               }
            } catch (Throwable var41) {
            }
         }

         if (pokemon != null) {
            try {
               Method getForm = pokemon.getClass().getMethod("getForm");
               Object form = getForm.invoke(pokemon);
               if (form != null) {
                  Method getTypesxx = form.getClass().getMethod("getTypes");
                  Object typesxx = getTypesxx.invoke(form);
                  if (typesxx instanceof Iterable) {
                     for (Object typexxxxx : (Iterable)typesxx) {
                        if (typexxxxx instanceof ElementalType) {
                           baseTypes.add((ElementalType)typexxxxx);
                        }
                     }
                  }

                  CobblemonBattleInfoClient.debug(
                     "[TypeIconRenderer] Got {} types from Pokemon.getForm().getTypes() for {}", new Object[]{baseTypes.size(), pokemonName}
                  );
               }
            } catch (Throwable var40) {
               CobblemonBattleInfoClient.debug("[TypeIconRenderer] Failed to get form types from Pokemon: {}", new Object[]{var40.getMessage()});
            }
         }
      }

      if (baseTypes.isEmpty()) {
         try {
            Method getForm = battlePokemon.getClass().getMethod("getForm");
            Object form = getForm.invoke(battlePokemon);
            if (form != null) {
               Method getTypesxx = form.getClass().getMethod("getTypes");
               Object typesxx = getTypesxx.invoke(form);
               if (typesxx instanceof Iterable) {
                  for (Object typexxxxxx : (Iterable)typesxx) {
                     if (typexxxxxx instanceof ElementalType) {
                        baseTypes.add((ElementalType)typexxxxxx);
                     }
                  }
               }
            }

            CobblemonBattleInfoClient.debug("[TypeIconRenderer] Got {} types from battlePokemon.getForm() for {}", new Object[]{baseTypes.size(), pokemonName});
         } catch (Throwable var39) {
            CobblemonBattleInfoClient.debug("[TypeIconRenderer] Failed to get form types from battlePokemon: {}", new Object[]{var39.getMessage()});
         }
      }

      if (baseTypes.isEmpty()) {
         try {
            Method getSpecies = battlePokemon.getClass().getMethod("getSpecies");
            Object species = getSpecies.invoke(battlePokemon);
            if (species != null) {
               Method getTypesxx = species.getClass().getMethod("getTypes");
               Object typesxx = getTypesxx.invoke(species);
               if (typesxx instanceof Iterable) {
                  for (Object typexxxxxxx : (Iterable)typesxx) {
                     if (typexxxxxxx instanceof ElementalType) {
                        baseTypes.add((ElementalType)typexxxxxxx);
                     }
                  }
               }
            }

            CobblemonBattleInfoClient.debug("[TypeIconRenderer] Got {} types from species for {}", new Object[]{baseTypes.size(), pokemonName});
         } catch (Throwable var38) {
            CobblemonBattleInfoClient.debug("[TypeIconRenderer] Failed to get species types for {}: {}", new Object[]{pokemonName, var38.getMessage()});
         }
      }

      if (BattleMessageSubscriber.isRoosted(side, pokemonName)) {
         for (ElementalType typexxxxxxxx : baseTypes) {
            if (!typexxxxxxxx.getName().equalsIgnoreCase("flying")) {
               result.add(typexxxxxxxx);
            }
         }

         if (result.isEmpty()) {
            ElementalType normalTypexx = stringToElementalType("Normal");
            if (normalTypexx != null) {
               result.add(normalTypexx);
            }
         }

         String addedType = BattleMessageSubscriber.getAddedType(side, pokemonName);
         if (addedType != null && !addedType.isEmpty()) {
            ElementalType addType = stringToElementalType(addedType);
            if (addType != null && !result.contains(addType)) {
               result.add(addType);
            }
         }

         return result;
      } else if (!BattleMessageSubscriber.hasBurnedUp(side, pokemonName)) {
         if (BattleMessageSubscriber.hasDoubleShocked(side, pokemonName)) {
            for (ElementalType typexxxxxxxxx : baseTypes) {
               if (!typexxxxxxxxx.getName().equalsIgnoreCase("electric")) {
                  result.add(typexxxxxxxxx);
               }
            }

            String addedType = BattleMessageSubscriber.getAddedType(side, pokemonName);
            if (addedType != null && !addedType.isEmpty()) {
               ElementalType addType = stringToElementalType(addedType);
               if (addType != null && !result.contains(addType)) {
                  result.add(addType);
               }
            }

            return result;
         } else {
            String addedType = BattleMessageSubscriber.getAddedType(side, pokemonName);
            if (addedType != null && !addedType.isEmpty()) {
               CobblemonBattleInfoClient.debug(
                  "[TypeIconRenderer] Adding {} type to {} - baseTypes size: {}, baseTypes: {}",
                  new Object[]{addedType, pokemonName, baseTypes.size(), baseTypes}
               );
               result.addAll(baseTypes);
               ElementalType addType = stringToElementalType(addedType);
               if (addType != null && !result.contains(addType)) {
                  result.add(addType);
               }

               CobblemonBattleInfoClient.debug("[TypeIconRenderer] Final types for {} with added type: {}", new Object[]{pokemonName, result});
               return cacheDisplayTypes(cacheKey, now, result);
            } else {
               return cacheDisplayTypes(cacheKey, now, baseTypes);
            }
         }
      } else {
         String burnUpTransformedType = BattleMessageSubscriber.getTransformedType(side, pokemonName);
         if (burnUpTransformedType != null && !burnUpTransformedType.isEmpty()) {
            ElementalType newType = stringToElementalType(burnUpTransformedType);
            if (newType != null) {
               result.add(newType);
            }
         } else {
            for (ElementalType typexxxxxxxxxx : baseTypes) {
               if (!typexxxxxxxxxx.getName().equalsIgnoreCase("fire")) {
                  result.add(typexxxxxxxxxx);
               }
            }
         }

         String addedType = BattleMessageSubscriber.getAddedType(side, pokemonName);
         if (addedType != null && !addedType.isEmpty()) {
            ElementalType addType = stringToElementalType(addedType);
            if (addType != null && !result.contains(addType)) {
               result.add(addType);
            }
         }

         return result;
      }
   }

   private static List<ElementalType> cacheDisplayTypes(String cacheKey, long now, List<ElementalType> types) {
      List<ElementalType> immutableTypes = List.copyOf(types);
      if (cacheKey != null) {
         DISPLAY_TYPES_CACHE.put(cacheKey, new CachedDisplayTypes(now + 75L, immutableTypes));
      }

      return immutableTypes;
   }

   private static String buildDisplayTypesCacheKey(Object battlePokemon, String pokemonName, String side, String pokemonUUID) {
      StringBuilder key = new StringBuilder();
      key.append(side != null ? side : "?");
      key.append('|');
      key.append(pokemonUUID != null && !pokemonUUID.isBlank() ? pokemonUUID : (pokemonName != null ? pokemonName : "unknown"));
      key.append('|');
      key.append(battlePokemon != null ? battlePokemon.getClass().getName() : "no-battle-pokemon");
      return key.toString();
   }

   private static List<ElementalType> getReflectTargetTypes(String targetStorageKey, Set<String> reflectVisitKeys) {
      if (targetStorageKey != null && !targetStorageKey.isBlank()) {
         ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
         if (battle == null) {
            return null;
         } else {
            String targetSide = extractSideFromStorageKey(targetStorageKey);
            String targetName = extractPokemonFromStorageKey(targetStorageKey);
            if (targetName != null && !targetName.isBlank()) {
               Object targetBattlePokemon = findBattlePokemon(battle, targetSide, targetName);
               if (targetBattlePokemon == null) {
                  CobblemonBattleInfoClient.debug("[TypeIconRenderer] Could not resolve Reflect Type target '{}'", new Object[]{targetStorageKey});
                  return null;
               } else {
                  String targetUuid = extractBattlePokemonUuid(targetBattlePokemon);
                  return getDisplayTypesInternal(targetBattlePokemon, targetName, targetSide, targetUuid, reflectVisitKeys);
               }
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static String extractSideFromStorageKey(String storageKey) {
      if (storageKey == null) {
         return null;
      } else if (storageKey.startsWith("player_")) {
         return "player";
      } else if (storageKey.startsWith("opponent_")) {
         return "opponent";
      } else {
         return storageKey.startsWith("unknown_") ? "unknown" : null;
      }
   }

   private static String extractPokemonFromStorageKey(String storageKey) {
      if (storageKey != null && !storageKey.isBlank()) {
         String side = extractSideFromStorageKey(storageKey);
         return side == null ? storageKey : storageKey.substring(side.length() + 1);
      } else {
         return null;
      }
   }

   private static Object findBattlePokemon(ClientBattle battle, String side, String pokemonName) {
      if (battle != null && pokemonName != null && !pokemonName.isBlank()) {
         Object match = findBattlePokemonOnSide(
            "player".equals(side) ? BattleSideHelper.getPlayerSide(battle) : BattleSideHelper.getOpponentSide(battle), pokemonName
         );
         if (match != null) {
            return match;
         } else if (side != null && !side.isBlank()) {
            return findBattlePokemonOnSide(
               "player".equals(side) ? BattleSideHelper.getOpponentSide(battle) : BattleSideHelper.getPlayerSide(battle), pokemonName
            );
         } else {
            match = findBattlePokemonOnSide(BattleSideHelper.getPlayerSide(battle), pokemonName);
            return match != null ? match : findBattlePokemonOnSide(BattleSideHelper.getOpponentSide(battle), pokemonName);
         }
      } else {
         return null;
      }
   }

   private static Object findBattlePokemonOnSide(Object battleSide, String pokemonName) {
      if (battleSide != null && pokemonName != null && !pokemonName.isBlank()) {
         String normalizedSearch = pokemonName.toLowerCase(Locale.ROOT);

         try {
            for (ClientBattleActor actor : ((ClientBattleSide)battleSide).getActors()) {
               for (ActiveClientBattlePokemon activePokemon : actor.getActivePokemon()) {
                  ClientBattlePokemon battlePokemon = activePokemon.getBattlePokemon();
                  if (battlePokemon != null && matchesBattlePokemonName(battlePokemon, normalizedSearch)) {
                     return battlePokemon;
                  }
               }
            }
         } catch (Throwable var8) {
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean matchesBattlePokemonName(Object battlePokemon, String normalizedSearch) {
      try {
         String speciesName = ((ClientBattlePokemon)battlePokemon).getSpecies().getName();
         if (speciesName != null && speciesName.toLowerCase(Locale.ROOT).equals(normalizedSearch)) {
            return true;
         }
      } catch (Throwable var4) {
      }

      try {
         String displayName = ((ClientBattlePokemon)battlePokemon).getDisplayName().getString();
         if (displayName != null && displayName.toLowerCase(Locale.ROOT).contains(normalizedSearch)) {
            return true;
         }
      } catch (Throwable var3) {
      }

      return false;
   }

   private static String extractBattlePokemonUuid(Object battlePokemon) {
      try {
         Object uuid = battlePokemon.getClass().getMethod("getUuid").invoke(battlePokemon);
         return uuid != null ? uuid.toString() : null;
      } catch (Throwable var2) {
         return null;
      }
   }

   private static void appendRandomizedTypes(List<ElementalType> baseTypes, String pokemonName, String pokemonUUID, String side) throws Exception {
      ensureRandomizerReflection();
      if (RANDOMIZER_REFLECTION_AVAILABLE) {
         if ("opponent".equals(side)) {
            Boolean hasOpponentTypesStored = (Boolean)RANDOMIZER_HAS_OPPONENT_TYPES.invoke(null);
            if (Boolean.TRUE.equals(hasOpponentTypesStored)) {
               String[] opponentTypes = (String[])RANDOMIZER_GET_OPPONENT_TYPES.invoke(null, pokemonUUID);
               if (opponentTypes != null && opponentTypes.length > 0) {
                  CobblemonBattleInfoClient.debug("[TypeIconRenderer] Found PvP opponent types for {} (UUID: {})", new Object[]{pokemonName, pokemonUUID});
                  appendTypeNames(baseTypes, opponentTypes);
               }
            }

            if (baseTypes.isEmpty()) {
               String[] wildTypes = (String[])RANDOMIZER_GET_WILD_TYPES.invoke(null, pokemonUUID);
               if (wildTypes != null && wildTypes.length > 0) {
                  CobblemonBattleInfoClient.debug("[TypeIconRenderer] Found wild types for {} (UUID: {})", new Object[]{pokemonName, pokemonUUID});
                  appendTypeNames(baseTypes, wildTypes);
               }
            }
         } else if ("player".equals(side)) {
            Boolean isRunActive = (Boolean)RANDOMIZER_IS_RUN_ACTIVE.invoke(null);
            Boolean isRandomTypes = (Boolean)RANDOMIZER_IS_RANDOM_TYPES.invoke(null);
            if (Boolean.TRUE.equals(isRunActive) && Boolean.TRUE.equals(isRandomTypes)) {
               String[] playerTypes = (String[])RANDOMIZER_GET_TYPES_FOR_POKEMON.invoke(null, pokemonName, pokemonUUID);
               if ((playerTypes == null || playerTypes.length == 0) && RANDOMIZER_GET_TYPES_FOR_SPECIES != null) {
                  playerTypes = (String[])RANDOMIZER_GET_TYPES_FOR_SPECIES.invoke(null, pokemonName);
               }

               if (playerTypes != null && playerTypes.length > 0) {
                  CobblemonBattleInfoClient.debug(
                     "[TypeIconRenderer] Found player's randomized types for {} (UUID: {}): {}",
                     new Object[]{pokemonName, pokemonUUID, Arrays.toString((Object[])playerTypes)}
                  );
                  appendTypeNames(baseTypes, playerTypes);
               }
            }
         }
      }
   }

   private static void appendTypeNames(List<ElementalType> out, String[] names) {
      for (String typeName : names) {
         ElementalType type = stringToElementalType(typeName);
         if (type != null) {
            out.add(type);
         }
      }
   }

   private static void appendRandomizedTypesFromShowdownId(
      List<ElementalType> baseTypes, Object battlePokemon, String pokemonName, String pokemonUUID, String side
   ) {
      if (battlePokemon != null) {
         for (String showdownId : collectShowdownIdCandidates(battlePokemon)) {
            String[] randomizedTypes = parseRandomizerTypeSuffix(showdownId);
            if (randomizedTypes != null && randomizedTypes.length != 0) {
               appendTypeNames(baseTypes, randomizedTypes);
               if (!baseTypes.isEmpty()) {
                  CobblemonBattleInfoClient.debug(
                     "[TypeIconRenderer] Parsed randomized showdown id '{}' for {} (UUID: {}, side: {}) -> {}",
                     new Object[]{showdownId, pokemonName, pokemonUUID, side, Arrays.toString((Object[])randomizedTypes)}
                  );
                  return;
               }
            }
         }
      }
   }

   private static LinkedHashSet<String> collectShowdownIdCandidates(Object battlePokemon) {
      LinkedHashSet<String> showdownIds = new LinkedHashSet<>();
      tryAddShowdownIdFromGetter(showdownIds, battlePokemon, "getSpecies");
      tryAddShowdownIdFromGetter(showdownIds, battlePokemon, "getForm");

      try {
         Method getProperties = battlePokemon.getClass().getMethod("getProperties");
         Object properties = getProperties.invoke(battlePokemon);
         if (properties != null) {
            tryAddShowdownIdFromGetter(showdownIds, properties, "getSpecies");
            tryAddShowdownIdFromGetter(showdownIds, properties, "getForm");
         }
      } catch (Throwable var4) {
      }

      return showdownIds;
   }

   private static void tryAddShowdownIdFromGetter(Set<String> showdownIds, Object source, String getterName) {
      if (source != null) {
         try {
            Method getter = source.getClass().getMethod(getterName);
            Object target = getter.invoke(source);
            String showdownId = extractNormalizedShowdownId(target);
            if (showdownId != null && !showdownId.isEmpty()) {
               showdownIds.add(showdownId);
            }
         } catch (Throwable var6) {
         }
      }
   }

   private static String extractNormalizedShowdownId(Object target) {
      if (target == null) {
         return null;
      } else {
         try {
            Method showdownIdMethod = target.getClass().getMethod("showdownId");
            Object showdownId = showdownIdMethod.invoke(target);
            if (showdownId != null) {
               return normalizeShowdownId(showdownId.toString());
            }
         } catch (Throwable var3) {
         }

         return normalizeShowdownId(target.toString());
      }
   }

   private static String normalizeShowdownId(String rawId) {
      if (rawId != null && !rawId.isBlank()) {
         String normalized = rawId.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
         return normalized.isEmpty() ? null : normalized;
      } else {
         return null;
      }
   }

   private static String[] parseRandomizerTypeSuffix(String showdownId) {
      if (showdownId != null && !showdownId.isBlank()) {
         int randIndex = showdownId.lastIndexOf("rand");
         if (randIndex >= 0 && randIndex + 4 < showdownId.length()) {
            String suffix = showdownId.substring(randIndex + 4);
            if (suffix.isBlank()) {
               return null;
            } else {
               for (String firstType : RANDOMIZER_TYPE_IDS) {
                  if (suffix.startsWith(firstType)) {
                     String remainder = suffix.substring(firstType.length());
                     if (remainder.isEmpty()) {
                        return new String[]{firstType};
                     }

                     for (String secondType : RANDOMIZER_TYPE_IDS) {
                        if (remainder.equals(secondType)) {
                           return new String[]{firstType, secondType};
                        }
                     }
                  }
               }

               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static void ensureRandomizerReflection() {
      if (!RANDOMIZER_REFLECTION_CHECKED) {
         RANDOMIZER_REFLECTION_CHECKED = true;

         try {
            Class<?> randomizerClientClass = Class.forName("cobblemon.randomizer.client.RandomizerClient");
            RANDOMIZER_HAS_OPPONENT_TYPES = randomizerClientClass.getMethod("hasOpponentPokemonTypes");
            RANDOMIZER_GET_OPPONENT_TYPES = randomizerClientClass.getMethod("getOpponentPokemonTypes", String.class);
            RANDOMIZER_GET_WILD_TYPES = randomizerClientClass.getMethod("getWildPokemonTypes", String.class);
            RANDOMIZER_IS_RUN_ACTIVE = randomizerClientClass.getMethod("isRunActive");
            RANDOMIZER_IS_RANDOM_TYPES = randomizerClientClass.getMethod("isRandomTypes");
            RANDOMIZER_GET_TYPES_FOR_POKEMON = randomizerClientClass.getMethod("getTypesForPokemon", String.class, String.class);

            try {
               RANDOMIZER_GET_TYPES_FOR_SPECIES = randomizerClientClass.getMethod("getTypesForSpecies", String.class);
            } catch (NoSuchMethodException var2) {
               RANDOMIZER_GET_TYPES_FOR_SPECIES = null;
            }

            RANDOMIZER_REFLECTION_AVAILABLE = true;
         } catch (Throwable var3) {
            RANDOMIZER_REFLECTION_AVAILABLE = false;
         }
      }
   }

   private static void ensureElementalTypesReflection() {
      if (!ELEMENTAL_TYPES_REFLECTION_CHECKED) {
         ELEMENTAL_TYPES_REFLECTION_CHECKED = true;

         try {
            Class<?> elementalTypesClass = Class.forName("com.cobblemon.mod.common.api.types.ElementalTypes");
            ELEMENTAL_TYPES_GET_METHOD = elementalTypesClass.getMethod("get", String.class);
            ELEMENTAL_TYPES_REFLECTION_AVAILABLE = true;
         } catch (Throwable var1) {
            ELEMENTAL_TYPES_REFLECTION_AVAILABLE = false;
         }
      }
   }

   public static ElementalType stringToElementalType(String typeName) {
      if (typeName != null && !typeName.isEmpty()) {
         try {
            ensureElementalTypesReflection();
            if (!ELEMENTAL_TYPES_REFLECTION_AVAILABLE || ELEMENTAL_TYPES_GET_METHOD == null) {
               return null;
            }

            Object result = ELEMENTAL_TYPES_GET_METHOD.invoke(null, typeName.toLowerCase(Locale.ROOT));
            if (result instanceof ElementalType) {
               return (ElementalType)result;
            }
         } catch (Throwable var2) {
            CobblemonBattleInfoClient.debug("Could not resolve type: {}", new Object[]{typeName});
         }

         return null;
      } else {
         return null;
      }
   }

   private static List<ElementalType> getTypesFromPokemonEntity(String pokemonUUID, String pokemonName) {
      if (pokemonUUID != null && !pokemonUUID.isEmpty()) {
         try {
            class_310 mc = class_310.method_1551();
            if (mc.field_1687 == null) {
               return null;
            }

            UUID targetUUID = UUID.fromString(pokemonUUID);

            for (class_1297 entity : mc.field_1687.method_18112()) {
               if (entity != null && entity.getClass().getName().contains("PokemonEntity")) {
                  try {
                     Method getPokemonMethod = entity.getClass().getMethod("getPokemon");
                     Object pokemonData = getPokemonMethod.invoke(entity);
                     if (pokemonData != null) {
                        Method getUuidMethod = pokemonData.getClass().getMethod("getUuid");
                        UUID entityPokemonUUID = (UUID)getUuidMethod.invoke(pokemonData);
                        if (entityPokemonUUID != null && entityPokemonUUID.equals(targetUUID)) {
                           List<ElementalType> result = new ArrayList<>();
                           Method getPrimaryTypeMethod = pokemonData.getClass().getMethod("getPrimaryType");
                           Object primaryType = getPrimaryTypeMethod.invoke(pokemonData);
                           if (primaryType instanceof ElementalType) {
                              result.add((ElementalType)primaryType);
                           }

                           Method getSecondaryTypeMethod = pokemonData.getClass().getMethod("getSecondaryType");
                           Object secondaryType = getSecondaryTypeMethod.invoke(pokemonData);
                           if (secondaryType instanceof ElementalType) {
                              result.add((ElementalType)secondaryType);
                           }

                           if (!result.isEmpty()) {
                              CobblemonBattleInfoClient.debug(
                                 "[TypeIconRenderer] Found Pokemon entity {} with UUID {}, got {} types from Pokemon object (may be randomized)",
                                 new Object[]{pokemonName, pokemonUUID, result.size()}
                              );
                              return result;
                           }
                        }
                     }
                  } catch (Throwable var15) {
                  }
               }
            }
         } catch (Throwable var16) {
            CobblemonBattleInfoClient.debug("[TypeIconRenderer] Failed to find Pokemon entity for {}: {}", new Object[]{pokemonName, var16.getMessage()});
         }

         return null;
      } else {
         return null;
      }
   }

   private static TypeTooltipData renderTypesRow(
      class_332 gui, List<ElementalType> types, int x, int y, float opacity, boolean isTerastallized, int mouseX, int mouseY
   ) {
      int offsetX = 0;
      TypeTooltipData hoveredType = null;
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
      int iconSize = isTerastallized ? 18 : 14;

      for (ElementalType type : types) {
         int iconX = x + offsetX;
         if (isTerastallized && MEGA_SHOWDOWN_LOADED) {
            String typeName = type.getName().toLowerCase();
            class_2960 teraIcon = class_2960.method_60654("mega_showdown:textures/gui/summary/tera_types/" + typeName + ".png");
            gui.method_25293(teraIcon, iconX, y, iconSize, iconSize, 0.0F, 0.0F, 32, 32, 32, 32);
         } else {
            int textureX = type.getTextureXMultiplier() * 36;
            gui.method_25293(TYPE_SPRITE_SHEET, iconX, y, iconSize, iconSize, (float)textureX, 0.0F, 36, 36, 648, 36);
         }

         if (hoveredType == null && isMouseOver(mouseX, mouseY, iconX, y, iconSize, iconSize)) {
            hoveredType = getTypeTooltipData(type);
         }

         offsetX += iconSize + 1;
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      return hoveredType;
   }

   private static int getCompactBattleTileTypeRowWidth(List<ElementalType> types, boolean isTerastallized) {
      if (isTerastallized && MEGA_SHOWDOWN_LOADED) {
         return 10;
      } else {
         return types != null && !types.isEmpty() ? types.size() * 8 + (types.size() - 1) * 1 : 0;
      }
   }

   private static int[] getCompactBattleTileTypeAnchor(
      class_310 mc, ClientBattle battle, ActiveClientBattlePokemon activePokemon, boolean isPlayerSide, int rank, boolean bottomVisibleSlot, int typeWidth
   ) {
      TilePlacement placement = CustomBattleTileRenderer.calculateTilePlacement(mc, battle, activePokemon, isPlayerSide, rank, true);
      int tileX = Math.round(placement.x());
      int tileY = placement.y();
      int tileWidth = placement.width();
      int tileHeight = placement.height();
      int portraitSize = 19;
      int portraitInset = 4;
      int portraitX = isPlayerSide ? tileX + portraitInset : tileX + tileWidth - portraitInset - portraitSize;
      int contentX = isPlayerSide ? portraitX + portraitSize + 6 : tileX + 6;
      int contentRight = isPlayerSide ? tileX + tileWidth - 6 : portraitX - 6;
      int bottomRowY = tileY + tileHeight - 14 + getCompactBattleTileTypeRowYOffset(isPlayerSide, rank, bottomVisibleSlot);
      int startX = isPlayerSide ? contentX - 2 : contentRight - typeWidth + 2;
      return new int[]{startX, bottomRowY};
   }

   private static int getCompactBattleTileTypeRowYOffset(boolean isPlayerSide, int rank, boolean bottomVisibleSlot) {
      int secondSlotOffset = rank == 1 ? 4 : 0;
      int thirdSlotOffset = bottomVisibleSlot ? 8 : 0;
      if (rank == 0) {
         return 4 + secondSlotOffset + thirdSlotOffset;
      } else {
         return rank == 2 ? -4 + secondSlotOffset + thirdSlotOffset : secondSlotOffset + thirdSlotOffset;
      }
   }

   private static int getCompactBattleTileStatusYOffset(boolean isPlayerSide, int rank, boolean bottomVisibleSlot) {
      int secondSlotOffset = rank == 1 ? 4 : 0;
      int secondSlotStatusAdjust = rank == 1 ? -4 : 0;
      int thirdSlotOffset = bottomVisibleSlot ? 0 : 0;
      if (isPlayerSide && rank == 0) {
         return -1 + secondSlotOffset + secondSlotStatusAdjust + thirdSlotOffset;
      } else if (isPlayerSide && rank == 1) {
         return -1 + secondSlotOffset + secondSlotStatusAdjust + thirdSlotOffset;
      } else if (!isPlayerSide && rank == 0) {
         return -1 + secondSlotOffset + secondSlotStatusAdjust + thirdSlotOffset;
      } else if (!isPlayerSide && rank == 1) {
         return -1 + secondSlotOffset + secondSlotStatusAdjust + thirdSlotOffset;
      } else {
         return rank == 2 ? -1 + secondSlotOffset + secondSlotStatusAdjust + thirdSlotOffset : secondSlotOffset + secondSlotStatusAdjust + thirdSlotOffset;
      }
   }

   private static boolean isBottomVisibleCompactSlot(class_310 mc, ClientBattle battle, ActiveClientBattlePokemon activePokemon, boolean isPlayerSide, int rank) {
      if (mc != null && battle != null && activePokemon != null) {
         ClientBattleSide side = isPlayerSide ? BattleSideHelper.getPlayerSide(battle) : BattleSideHelper.getOpponentSide(battle);
         List<ActiveClientBattlePokemon> activeMons = getOrderedActiveMons(side);
         if (activeMons.isEmpty()) {
            return false;
         } else {
            TilePlacement currentPlacement = CustomBattleTileRenderer.calculateTilePlacement(mc, battle, activePokemon, isPlayerSide, rank, true);
            if (currentPlacement == null) {
               return false;
            } else {
               int maxY = Integer.MIN_VALUE;
               int totalMons = activeMons.size();

               for (int index = 0; index < totalMons; index++) {
                  ActiveClientBattlePokemon other = activeMons.get(index);
                  if (other != null && other.getBattlePokemon() != null) {
                     int visibleRank = isPlayerSide ? index : totalMons - index - 1;
                     TilePlacement otherPlacement = CustomBattleTileRenderer.calculateTilePlacement(mc, battle, other, isPlayerSide, visibleRank, true);
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

   private static TypeTooltipData renderCompactBattleTileTypes(
      class_332 gui,
      class_310 mc,
      ClientBattle battle,
      ActiveClientBattlePokemon activePokemon,
      boolean isPlayerSide,
      int rank,
      boolean bottomVisibleSlot,
      List<ElementalType> types,
      float opacity,
      boolean isTerastallized,
      int mouseX,
      int mouseY
   ) {
      int typeWidth = getCompactBattleTileTypeRowWidth(types, isTerastallized);
      int[] anchor = getCompactBattleTileTypeAnchor(mc, battle, activePokemon, isPlayerSide, rank, bottomVisibleSlot, typeWidth);
      renderCompactBattleTileStatusPill(gui, mc, battle, activePokemon, isPlayerSide, rank, bottomVisibleSlot, typeWidth, anchor[0], anchor[1], opacity);
      return renderCompactTypesRow(gui, types, anchor[0], anchor[1], opacity, isTerastallized, mouseX, mouseY);
   }

   private static TypeTooltipData renderCompactBattleTileStellarType(
      class_332 gui,
      class_310 mc,
      ClientBattle battle,
      ActiveClientBattlePokemon activePokemon,
      boolean isPlayerSide,
      int rank,
      boolean bottomVisibleSlot,
      float opacity,
      int mouseX,
      int mouseY
   ) {
      int[] anchor = getCompactBattleTileTypeAnchor(mc, battle, activePokemon, isPlayerSide, rank, bottomVisibleSlot, 10);
      renderCompactBattleTileStatusPill(gui, mc, battle, activePokemon, isPlayerSide, rank, bottomVisibleSlot, 10, anchor[0], anchor[1], opacity);
      return renderCompactStellarType(gui, anchor[0], anchor[1], opacity, mouseX, mouseY);
   }

   private static TypeTooltipData renderCompactBattleTileTypelessIcon(
      class_332 gui,
      class_310 mc,
      ClientBattle battle,
      ActiveClientBattlePokemon activePokemon,
      boolean isPlayerSide,
      int rank,
      boolean bottomVisibleSlot,
      float opacity,
      int mouseX,
      int mouseY
   ) {
      int[] anchor = getCompactBattleTileTypeAnchor(mc, battle, activePokemon, isPlayerSide, rank, bottomVisibleSlot, 8);
      renderCompactBattleTileStatusPill(gui, mc, battle, activePokemon, isPlayerSide, rank, bottomVisibleSlot, 8, anchor[0], anchor[1], opacity);
      return renderCompactTypelessIcon(gui, anchor[0], anchor[1], opacity, mouseX, mouseY);
   }

   private static TypeTooltipData renderCompactTypesRow(
      class_332 gui, List<ElementalType> types, int x, int y, float opacity, boolean isTerastallized, int mouseX, int mouseY
   ) {
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
      pushCompactBattleTileOverlayLayer(gui);
      TypeTooltipData hoveredType = null;
      int iconSize = isTerastallized && MEGA_SHOWDOWN_LOADED ? 10 : 8;
      int offsetX = 0;

      for (ElementalType type : types) {
         if (type != null) {
            if (isTerastallized && MEGA_SHOWDOWN_LOADED) {
               String typeName = type.getName().toLowerCase();
               class_2960 teraIcon = class_2960.method_60654("mega_showdown:textures/gui/summary/tera_types/" + typeName + ".png");
               gui.method_25293(teraIcon, x + offsetX, y - 1, iconSize, iconSize, 0.0F, 0.0F, 32, 32, 32, 32);
            } else {
               int textureX = type.getTextureXMultiplier() * 36;
               gui.method_25293(TYPE_SPRITE_SHEET, x + offsetX, y, iconSize, iconSize, (float)textureX, 0.0F, 36, 36, 648, 36);
            }

            if (hoveredType == null && isMouseOver(mouseX, mouseY, x + offsetX, y, iconSize, iconSize)) {
               hoveredType = getTypeTooltipData(type);
            }

            offsetX += iconSize + 1;
            if (isTerastallized && MEGA_SHOWDOWN_LOADED) {
               break;
            }
         }
      }

      popCompactBattleTileOverlayLayer(gui);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      return hoveredType;
   }

   private static void renderCompactBattleTileStatusPill(
      class_332 gui,
      class_310 mc,
      ClientBattle battle,
      ActiveClientBattlePokemon activePokemon,
      boolean isPlayerSide,
      int rank,
      boolean bottomVisibleSlot,
      int typeWidth,
      int iconStartX,
      int bottomRowY,
      float opacity
   ) {
      if (!BattleExtrasConfig.useCustomTopBattleTiles()) {
         ClientBattlePokemon battlePokemon = activePokemon.getBattlePokemon();
         if (battlePokemon != null) {
            String statusText = null;
            PersistentStatus status = battlePokemon.getStatus();
            if (status != null) {
               statusText = status.getShowdownName();
            }

            if ((statusText == null || statusText.isBlank()) && isPlayerSide) {
               statusText = LocalBattlePartyTracker.getEffectivePlayerStatusId(activePokemon, battlePokemon);
            }

            if (statusText != null && !statusText.isBlank()) {
               TilePlacement placement = CustomBattleTileRenderer.calculateTilePlacement(mc, battle, activePokemon, isPlayerSide, rank, true);
               int tileX = Math.round(placement.x());
               int tileWidth = placement.width();
               int portraitSize = 19;
               int portraitInset = 4;
               int portraitX = isPlayerSide ? tileX + portraitInset : tileX + tileWidth - portraitInset - portraitSize;
               int contentX = isPlayerSide ? portraitX + portraitSize + 6 : tileX + 6;
               int contentRight = isPlayerSide ? tileX + tileWidth - 6 : portraitX - 6;
               class_327 font = mc.field_1772;
               int statusWidth = CustomBattleTileRenderer.getNativeStatusPillWidthCompact(font, statusText, 0.68F);
               SpeedInfo speedInfo = BattleTileSpeedRangeRenderer.resolveSpeedInfo(activePokemon, battlePokemon, isPlayerSide ? "player" : "opponent");
               int speedX = BattleTileSpeedRangeRenderer.getCompactSpeedLabelX(mc, battle, activePokemon, isPlayerSide, rank, mc.method_22683().method_4486());
               int speedWidth = speedInfo == null ? 0 : BattleTileSpeedRangeRenderer.getScaledTextWidth(font, "SPE " + speedInfo.text(), 0.68F);
               int statusX;
               if (isPlayerSide) {
                  statusX = iconStartX + typeWidth + (typeWidth > 0 ? 1 : 0);
                  int bottomRowLimit = speedInfo != null ? speedX - 2 : contentRight;
                  if (statusX < contentX || statusX + statusWidth > bottomRowLimit) {
                     return;
                  }
               } else {
                  int minimumStatusX = speedInfo != null ? speedX + speedWidth + 3 : contentX;
                  int preferredStatusX = iconStartX - (statusWidth > 0 ? statusWidth + 4 : 0);
                  statusX = Math.max(minimumStatusX, preferredStatusX);
                  if (statusX < contentX || statusX + statusWidth > contentRight) {
                     return;
                  }
               }

               pushCompactBattleTileOverlayLayer(gui);
               CustomBattleTileRenderer.renderNativeStatusPillCompact(
                  gui, font, statusText, statusX, bottomRowY + 1 + getCompactBattleTileStatusYOffset(isPlayerSide, rank, bottomVisibleSlot), opacity, 0.68F
               );
               popCompactBattleTileOverlayLayer(gui);
            }
         }
      }
   }

   private static TypeTooltipData renderCompactStellarType(class_332 gui, int x, int y, float opacity, int mouseX, int mouseY) {
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
      pushCompactBattleTileOverlayLayer(gui);
      class_2960 stellarIcon = class_2960.method_60654("mega_showdown:textures/gui/summary/tera_types/stellar.png");
      gui.method_25293(stellarIcon, x, y - 1, 10, 10, 0.0F, 0.0F, 32, 32, 32, 32);
      popCompactBattleTileOverlayLayer(gui);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      return isMouseOver(mouseX, mouseY, x, y - 1, 10, 10) ? new TypeTooltipData(class_2561.method_43470("Stellar"), -2510003) : null;
   }

   private static TypeTooltipData renderCompactTypelessIcon(class_332 gui, int x, int y, float opacity, int mouseX, int mouseY) {
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
      pushCompactBattleTileOverlayLayer(gui);
      gui.method_25293(TYPELESS_ICON, x, y, 8, 8, 0.0F, 0.0F, 36, 36, 36, 36);
      popCompactBattleTileOverlayLayer(gui);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      return isMouseOver(mouseX, mouseY, x, y, 8, 8) ? new TypeTooltipData(class_2561.method_43470("Typeless"), -7364427) : null;
   }

   private static void pushCompactBattleTileOverlayLayer(class_332 gui) {
      gui.method_51448().method_22903();
      gui.method_51448().method_46416(0.0F, 0.0F, 200.0F);
   }

   private static void popCompactBattleTileOverlayLayer(class_332 gui) {
      gui.method_51448().method_22909();
   }

   private static TypeTooltipData renderTypesColumn(
      class_332 gui, List<ElementalType> types, int x, int y, float opacity, boolean isTerastallized, int mouseX, int mouseY
   ) {
      int offsetY = 0;
      TypeTooltipData hoveredType = null;
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
      int iconSize = 10;

      for (ElementalType type : types) {
         int iconY = y + offsetY;
         if (isTerastallized && MEGA_SHOWDOWN_LOADED) {
            String typeName = type.getName().toLowerCase();
            class_2960 teraIcon = class_2960.method_60654("mega_showdown:textures/gui/summary/tera_types/" + typeName + ".png");
            gui.method_25293(teraIcon, x, iconY, iconSize, iconSize, 0.0F, 0.0F, 32, 32, 32, 32);
         } else {
            int textureX = type.getTextureXMultiplier() * 36;
            gui.method_25293(TYPE_SPRITE_SHEET, x, iconY, iconSize, iconSize, (float)textureX, 0.0F, 36, 36, 648, 36);
         }

         if (hoveredType == null && isMouseOver(mouseX, mouseY, x, iconY, iconSize, iconSize)) {
            hoveredType = getTypeTooltipData(type);
         }

         offsetY += iconSize + 1;
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      return hoveredType;
   }

   private static TypeTooltipData renderTypesRowReversed(
      class_332 gui, List<ElementalType> types, int x, int y, float opacity, boolean isTerastallized, int mouseX, int mouseY
   ) {
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
      TypeTooltipData hoveredType = null;
      int iconSize = isTerastallized ? 18 : 14;
      int offsetX = 0;

      for (int i = types.size() - 1; i >= 0; i--) {
         ElementalType type = types.get(i);
         int iconX = x - iconSize - offsetX;
         if (isTerastallized && MEGA_SHOWDOWN_LOADED) {
            String typeName = type.getName().toLowerCase();
            class_2960 teraIcon = class_2960.method_60654("mega_showdown:textures/gui/summary/tera_types/" + typeName + ".png");
            gui.method_25293(teraIcon, iconX, y, iconSize, iconSize, 0.0F, 0.0F, 32, 32, 32, 32);
         } else {
            int textureX = type.getTextureXMultiplier() * 36;
            gui.method_25293(TYPE_SPRITE_SHEET, iconX, y, iconSize, iconSize, (float)textureX, 0.0F, 36, 36, 648, 36);
         }

         if (hoveredType == null && isMouseOver(mouseX, mouseY, iconX, y, iconSize, iconSize)) {
            hoveredType = getTypeTooltipData(type);
         }

         offsetX += iconSize + 1;
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      return hoveredType;
   }

   private static TypeTooltipData renderStellarType(class_332 gui, int x, int y, float opacity, int mouseX, int mouseY) {
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
      class_2960 stellarIcon = class_2960.method_60654("mega_showdown:textures/gui/summary/tera_types/stellar.png");
      gui.method_25293(stellarIcon, x, y, 18, 18, 0.0F, 0.0F, 32, 32, 32, 32);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      return isMouseOver(mouseX, mouseY, x, y, 18, 18) ? new TypeTooltipData(class_2561.method_43470("Stellar"), -2510003) : null;
   }

   private static TypeTooltipData renderStellarTypeReversed(class_332 gui, int x, int y, float opacity, int mouseX, int mouseY) {
      return renderStellarType(gui, x - 18, y, opacity, mouseX, mouseY);
   }

   private static boolean isTypeless(String side, String pokemonName) {
      return BattleMessageSubscriber.hasBurnedUp(side, pokemonName) || BattleMessageSubscriber.hasDoubleShocked(side, pokemonName);
   }

   private static TypeTooltipData renderTypelessIcon(class_332 gui, int x, int y, float opacity, int mouseX, int mouseY) {
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
      gui.method_25293(TYPELESS_ICON, x, y, 14, 14, 0.0F, 0.0F, 36, 36, 36, 36);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      return isMouseOver(mouseX, mouseY, x, y, 14, 14) ? new TypeTooltipData(class_2561.method_43470("Typeless"), -7364427) : null;
   }

   private static TypeTooltipData renderTypelessIconReversed(class_332 gui, int x, int y, float opacity, int mouseX, int mouseY) {
      int iconX = x - 14;
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
      gui.method_25293(TYPELESS_ICON, iconX, y, 14, 14, 0.0F, 0.0F, 36, 36, 36, 36);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      return isMouseOver(mouseX, mouseY, iconX, y, 14, 14) ? new TypeTooltipData(class_2561.method_43470("Typeless"), -7364427) : null;
   }

   private static boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
      return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
   }

   private static TypeTooltipData getTypeTooltipData(ElementalType type) {
      if (type == null) {
         return null;
      } else {
         class_2561 tooltipText = null;

         try {
            class_2561 displayName = type.getDisplayName();
            if (displayName != null) {
               String localized = displayName.getString();
               if (localized != null && !localized.isBlank()) {
                  tooltipText = displayName;
               }
            }
         } catch (Throwable var4) {
         }

         if (tooltipText == null) {
            String fallback = type.getName();
            if (fallback == null || fallback.isBlank()) {
               return null;
            }

            fallback = fallback.substring(0, 1).toUpperCase(Locale.ROOT) + fallback.substring(1).toLowerCase(Locale.ROOT);
            tooltipText = class_2561.method_43470(fallback);
         }

         int borderColor = normalizeTooltipBorderColor(type.getHue());
         return new TypeTooltipData(tooltipText, borderColor);
      }
   }

   private static int normalizeTooltipBorderColor(int color) {
      return (color & 0xFF000000) == 0 ? 0xFF000000 | color & 16777215 : color;
   }

   private static void renderTypeTooltip(class_332 gui, int mouseX, int mouseY, TypeTooltipData tooltipData) {
      gui.method_51448().method_22903();
      gui.method_51448().method_46416(0.0F, 0.0F, 5000.0F);
      CustomTooltipRenderer.renderCompactCenteredTooltip(gui, tooltipData.text, mouseX, mouseY, tooltipData.borderColor);
      gui.method_51448().method_22909();
   }

   private static void renderMegaIcon(class_332 gui, int x, int y, float opacity, boolean isPlayerSide, int typeCount) {
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
      int typeIconsWidth = typeCount * 14 + (typeCount - 1) * 1;
      int iconY = y + 3;
      int iconX;
      if (isPlayerSide) {
         iconX = x - 8 - 2;
         if (typeCount == 1) {
            iconX += 14;
            iconY -= 15;
         } else if (typeCount == 2) {
            iconX += MEGA_ICON_OFFSET_X_PLAYER;
            iconY += MEGA_ICON_OFFSET_Y_PLAYER;
         }
      } else {
         iconX = x - typeIconsWidth - 8 - 2;
         if (typeCount == 1) {
            iconX += MEGA_ICON_OFFSET_X_OPPONENT_SINGLE;
            iconY += MEGA_ICON_OFFSET_Y_OPPONENT_SINGLE;
         } else if (typeCount == 2) {
            iconX += MEGA_ICON_OFFSET_X_OPPONENT;
            iconY += MEGA_ICON_OFFSET_Y_OPPONENT;
         }
      }

      gui.method_25293(MEGA_ICON, iconX, iconY, 8, 8, 0.0F, 0.0F, 64, 64, 64, 64);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }
}
