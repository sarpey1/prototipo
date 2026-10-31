package name.modid.mixin.client;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.api.moves.categories.DamageCategory;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.cobblemon.mod.common.client.battle.SingleActionRequest;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection.MoveTile;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import name.modid.CobblemonBattleInfoClient;
import name.modid.client.BattleExtrasConfig;
import name.modid.client.BattleMessageSubscriber;
import name.modid.client.BattlePokemonSpeedResolver;
import name.modid.client.BattleSideHelper;
import name.modid.client.CritChanceCache;
import name.modid.client.CustomBattleController;
import name.modid.client.LocalBattlePartyTracker;
import name.modid.client.MoveDamagePreviewCalculator;
import name.modid.client.MoveTooltipOverlayState;
import name.modid.client.PokemonNameHelper;
import name.modid.client.TypeChart;
import name.modid.client.TypeIconRenderer;
import name.modid.client.MoveDamagePreviewCalculator.AttackContext;
import name.modid.client.MoveDamagePreviewCalculator.DamagePreview;
import name.modid.client.MoveDamagePreviewCalculator.DefenceContext;
import net.minecraft.class_124;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_5250;
import net.minecraft.class_7923;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MoveTile.class})
public abstract class MoveTileMixin {
   private static final Logger LOGGER = LoggerFactory.getLogger(MoveTileMixin.class);
   @Shadow(
      remap = false
   )
   public MoveTemplate moveTemplate;
   private static String cachedOpponentId = null;
   private static List<ElementalType> cachedFormTypes = null;
   private static long lastCacheTime = 0L;
   private static String cachedPlayerPokemonId = null;
   private static String cachedPlayerAbility = null;
   private static long lastAbilityCacheTime = 0L;
   private static boolean cachedPlayerIsMega = false;
   private static MoveTemplate cachedMoveTooltipTemplate = null;
   private static int cachedMoveTooltipCurrentPp = Integer.MIN_VALUE;
   private static int cachedMoveTooltipMaxPp = Integer.MIN_VALUE;
   private static int cachedMoveTooltipTurnNumber = Integer.MIN_VALUE;
   private static String cachedMoveTooltipWeatherKey = "";
   private static int cachedMoveTooltipOpponentHpKey = Integer.MIN_VALUE;
   private static String cachedMoveTooltipOpponentStateKey = "";
   private static List<class_2561> cachedMoveTooltipLines = null;
   private static int cachedMoveTooltipTypeColor = 16777215;
   private static ElementalType cachedMoveTooltipMoveType = null;
   private static DamageCategory cachedMoveTooltipMoveCategory = null;
   private static Field inBattleMoveField = null;
   private static boolean inBattleMoveFieldResolved = false;

   @Shadow(
      remap = false
   )
   public abstract boolean isHovered(double var1, double var3);

   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   public void hideNativeMoveTile(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (BattleExtrasConfig.useCustomMoveTiles() && CustomBattleController.isMovesSelectionActive()) {
         this.renderMoveTooltipIfNeeded(context, mouseX, mouseY);
         ci.cancel();
      }
   }

   @Inject(
      method = {"render"},
      at = {@At("TAIL")},
      remap = false
   )
   public void onRender(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      this.renderMoveTooltipIfNeeded(context, mouseX, mouseY);
   }

   @Inject(
      method = {"render"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/cobblemon/mod/common/client/gui/TypeIcon;render(Lnet/minecraft/client/gui/GuiGraphics;)V"
      )},
      remap = false
   )
   public void renderVanillaTooltipBeforeTypeIcon(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (!BattleExtrasConfig.useCustomMoveTiles() && BattleExtrasConfig.isVanillaStyleMoveTooltipsEnabled()) {
         this.renderMoveTooltipIfNeeded(context, mouseX, mouseY);
      }
   }

   private void renderMoveTooltipIfNeeded(class_332 context, int mouseX, int mouseY) {
      if (BattleExtrasConfig.isMoveTooltipsEnabled()) {
         boolean isMouseHovered = this.isHovered((double)mouseX, (double)mouseY);
         boolean isControllerSelected = CustomBattleController.isTileControllerSelected(this);
         if (isMouseHovered) {
            if (this.moveTemplate != null) {
               int[] tileBounds = BattleExtrasConfig.isVanillaStyleMoveTooltipsEnabled() ? CustomBattleController.getTilePosition(this) : null;
               this.renderTooltipAtPosition(context, mouseX, mouseY, -1, tileBounds);
            }
         } else {
            if (isControllerSelected && CustomBattleController.isMovesSelectionActive() && !CustomBattleController.isMouseHoveringAnyMoveTile()) {
               if (this.moveTemplate == null) {
                  return;
               }

               int[] tilePos = CustomBattleController.getSelectedTilePosition();
               int tileIndex = CustomBattleController.getSelectedTileIndex();
               if (tilePos != null && tileIndex >= 0) {
                  int tileY = tilePos[1];
                  int tileHeight = tilePos[3];
                  int tooltipX = 8;
                  int anchorBottomY;
                  if (tileIndex <= 1) {
                     anchorBottomY = tileY - 8;
                  } else {
                     anchorBottomY = tileY - tileHeight - 8 - 8;
                  }

                  this.renderTooltipAtPosition(context, tooltipX, 0, anchorBottomY, BattleExtrasConfig.isVanillaStyleMoveTooltipsEnabled() ? tilePos : null);
               }
            }
         }
      }
   }

   private void renderTooltipAtPosition(class_332 context, int tooltipX, int tooltipY, int anchorBottomY, int[] tileBounds) {
      try {
         InBattleMove inBattleMove = this.getInBattleMoveForTile();
         int cachePp = inBattleMove != null ? inBattleMove.getPp() : -1;
         int cacheMaxPp = inBattleMove != null ? inBattleMove.getMaxpp() : -1;
         String weatherKey = BattleMessageSubscriber.getCurrentWeather();
         if (weatherKey == null) {
            weatherKey = "";
         }

         String terrainKey = BattleMessageSubscriber.getCurrentTerrain();
         if (terrainKey == null) {
            terrainKey = "";
         }

         weatherKey = weatherKey + "|" + terrainKey + "|wr=" + BattleMessageSubscriber.getWonderRoomTurns();
         int turnNumber = BattleMessageSubscriber.getLastTurnNumber();
         int opponentHpKey = 0;
         String opponentStateKey = "";

         try {
            ClientBattle battleForHp = CobblemonClient.INSTANCE.getBattle();
            if (battleForHp != null) {
               ClientBattleSide opponentSideForHp = BattleSideHelper.getOpponentSide(battleForHp);
               StringBuilder opponentStateBuilder = new StringBuilder();

               for (ClientBattleActor actor : opponentSideForHp.getActors()) {
                  for (ActiveClientBattlePokemon active : actor.getActivePokemon()) {
                     ClientBattlePokemon bp = active.getBattlePokemon();
                     if (bp != null) {
                        opponentHpKey += (int)(bp.getHpValue() * 1000.0F);
                        opponentStateBuilder.append(bp.getUuid())
                           .append('|')
                           .append(bp.getSpecies().getName())
                           .append('|')
                           .append(bp.getLevel())
                           .append('|')
                           .append(bp.isHpFlat())
                           .append('|')
                           .append(bp.getHpValue())
                           .append('|')
                           .append(bp.getMaxHp())
                           .append(';');
                     }
                  }
               }

               opponentStateKey = opponentStateBuilder.toString();
            }
         } catch (Throwable var74) {
         }

         if (isCachedMoveTooltipValid(this.moveTemplate, cachePp, cacheMaxPp, turnNumber, weatherKey, opponentHpKey, opponentStateKey)) {
            MoveTooltipOverlayState.queue(
               cachedMoveTooltipLines,
               cachedMoveTooltipTypeColor,
               cachedMoveTooltipMoveType,
               cachedMoveTooltipMoveCategory,
               tooltipX,
               tooltipY,
               anchorBottomY,
               tileBounds
            );
            return;
         }

         List<class_2561> tooltip = new ArrayList<>();
         int typeColor = this.moveTemplate.getElementalType().getHue();

         String moveName;
         try {
            Object displayNameComponent = this.moveTemplate.getClass().getMethod("getDisplayName").invoke(this.moveTemplate);
            if (displayNameComponent != null) {
               moveName = displayNameComponent.getClass().getMethod("getString").invoke(displayNameComponent).toString();
            } else {
               moveName = this.formatMoveName(this.moveTemplate.getName());
            }
         } catch (Exception var70) {
            moveName = this.formatMoveName(this.moveTemplate.getName());
         }

         tooltip.add(class_2561.method_43470(moveName).method_27694(style -> style.method_36139(typeColor).method_10982(true)));

         String typeName;
         try {
            Object typeDisplayName = this.moveTemplate.getElementalType().getClass().getMethod("getDisplayName").invoke(this.moveTemplate.getElementalType());
            typeName = typeDisplayName.getClass().getMethod("getString").invoke(typeDisplayName).toString();
         } catch (Exception var69) {
            typeName = this.capitalize(this.moveTemplate.getElementalType().getName());
         }

         String category;
         try {
            Object categoryDisplayName = this.moveTemplate
               .getDamageCategory()
               .getClass()
               .getMethod("getDisplayName")
               .invoke(this.moveTemplate.getDamageCategory());
            category = categoryDisplayName.getClass().getMethod("getString").invoke(categoryDisplayName).toString();
         } catch (Exception var68) {
            category = this.capitalize(this.moveTemplate.getDamageCategory().getName());
         }

         tooltip.add(class_2561.method_43470(typeName + " | " + category).method_27692(class_124.field_1080));

         try {
            String moveId = this.moveTemplate.getName().toLowerCase();
            String translationKey = "cobblemon.move." + moveId + ".desc";
            class_2561 descComponent = class_2561.method_43471(translationKey);
            String description = descComponent.getString();
            if (description != null && !description.isEmpty() && !description.equals(translationKey)) {
               tooltip.add(class_2561.method_43473());

               for (String line : this.wrapText(description, 35)) {
                  tooltip.add(class_2561.method_43470(line).method_27695(new class_124[]{class_124.field_1068, class_124.field_1056}));
               }
            }
         } catch (Exception var73) {
         }

         boolean isStatus = this.moveTemplate.getDamageCategory() == DamageCategories.INSTANCE.getSTATUS();
         double power = this.moveTemplate.getPower();
         double accuracy = this.moveTemplate.getAccuracy();
         ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
         ActiveClientBattlePokemon activePlayerPokemon = this.getPlayerActiveBattlePokemon(battle);
         ClientBattlePokemon playerPokemon = activePlayerPokemon != null ? activePlayerPokemon.getBattlePokemon() : null;
         String playerPokemonName = null;
         float stabMultiplier = 1.0F;
         double critChance = 6.25;
         double adjustedAccuracy = accuracy;
         if (playerPokemon != null) {
            try {
               playerPokemonName = this.getPlayerPokemonName(playerPokemon);
               CobblemonBattleInfoClient.debug("[MoveTileMixin] About to call getCritChancePercentage for: {}", new Object[]{playerPokemonName});
               stabMultiplier = this.calculateStabMultiplier(this.moveTemplate, playerPokemon, playerPokemonName);
               critChance = this.getCritChancePercentage(playerPokemonName, playerPokemon, this.moveTemplate);
               adjustedAccuracy = this.calculateAdjustedAccuracy(accuracy, playerPokemonName, playerPokemon, this.moveTemplate);
               CobblemonBattleInfoClient.debug("[MoveTileMixin] getCritChancePercentage returned: {}%", new Object[]{critChance});
            } catch (Exception var67) {
               LOGGER.error("[MoveTileMixin] Exception in tooltip calculation", var67);
            }
         } else {
            CobblemonBattleInfoClient.debug("[MoveTileMixin] playerPokemon is null, using defaults", new Object[0]);
         }

         tooltip.add(class_2561.method_43473());
         if (power > 0.0) {
            int basePower = (int)power;
            boolean hasStab = stabMultiplier > 1.0F;
            if (hasStab) {
               int adjustedPower = (int)((float)basePower * stabMultiplier);
               tooltip.add(class_2561.method_43469("move.battleinfo.power.stab", new Object[]{basePower, adjustedPower}).method_27692(class_124.field_1061));
            } else {
               tooltip.add(class_2561.method_43469("move.battleinfo.power", new Object[]{basePower}).method_27692(class_124.field_1061));
            }
         } else if (!isStatus) {
            tooltip.add(class_2561.method_43471("move.battleinfo.power.none").method_27692(class_124.field_1063));
         }

         if (accuracy > 0.0) {
            int baseAccuracy = (int)accuracy;
            int finalAccuracy = (int)adjustedAccuracy;
            boolean hasAccuracyBoost = finalAccuracy != baseAccuracy;
            if (hasAccuracyBoost) {
               tooltip.add(
                  class_2561.method_43469("move.battleinfo.accuracy.boost", new Object[]{baseAccuracy, finalAccuracy}).method_27692(class_124.field_1078)
               );
            } else {
               tooltip.add(class_2561.method_43469("move.battleinfo.accuracy", new Object[]{baseAccuracy}).method_27692(class_124.field_1078));
            }
         } else {
            tooltip.add(class_2561.method_43471("move.battleinfo.accuracy.none").method_27692(class_124.field_1063));
         }

         int priority = this.moveTemplate.getPriority();
         tooltip.add(
            class_2561.method_43469("move.battleinfo.priority", new Object[]{this.formatPriorityValue(priority)})
               .method_27692(this.getPriorityFormatting(priority))
         );
         if (!isStatus && power > 0.0) {
            String critText;
            class_124 critColor;
            if (critChance <= 4.17) {
               critText = "4.17%";
               critColor = class_124.field_1080;
            } else if (critChance == 12.5) {
               critText = "12.5%";
               critColor = class_124.field_1054;
            } else if (critChance == 50.0) {
               critText = "50%";
               critColor = class_124.field_1065;
            } else if (critChance >= 100.0) {
               critText = "100%";
               critColor = class_124.field_1061;
            } else {
               critText = String.format("%.1f%%", critChance);
               critColor = class_124.field_1054;
            }

            tooltip.add(class_2561.method_43469("move.battleinfo.crit", new Object[]{critText}).method_27692(critColor));
         }

         if (inBattleMove != null) {
            int pp = inBattleMove.getPp();
            int maxPP = inBattleMove.getMaxpp();
            class_124 ppColor = pp == 0 ? class_124.field_1061 : (pp <= maxPP / 4 ? class_124.field_1065 : class_124.field_1060);
            tooltip.add(class_2561.method_43469("move.battleinfo.pp", new Object[]{pp, maxPP}).method_27692(ppColor));
         }

         try {
            double effectChance = 0.0;

            try {
               Method method = this.moveTemplate.getClass().getMethod("getEffectChances");
               Object result = method.invoke(this.moveTemplate);
               if (result != null && result.getClass().isArray()) {
                  int length = Array.getLength(result);
                  if (length > 0) {
                     Object firstChance = Array.get(result, 0);
                     if (firstChance instanceof Number) {
                        effectChance = ((Number)firstChance).doubleValue();
                     }
                  }
               }
            } catch (Exception var66) {
               CobblemonBattleInfoClient.debug(
                  "[MoveTileMixin] Failed to get effectChances for {}: {}", new Object[]{this.moveTemplate.getName(), var66.getMessage()}
               );
            }

            if (effectChance == 0.0) {
               try {
                  Field field = this.moveTemplate.getClass().getDeclaredField("effectChances");
                  field.setAccessible(true);
                  Object result = field.get(this.moveTemplate);
                  if (result != null && result.getClass().isArray()) {
                     int length = Array.getLength(result);
                     if (length > 0) {
                        Object firstChance = Array.get(result, 0);
                        if (firstChance instanceof Number) {
                           effectChance = ((Number)firstChance).doubleValue();
                        }
                     }
                  }
               } catch (Exception var65) {
                  CobblemonBattleInfoClient.debug(
                     "[MoveTileMixin] Failed to access effectChances field for {}: {}", new Object[]{this.moveTemplate.getName(), var65.getMessage()}
                  );
               }
            }

            if (effectChance > 0.0) {
               String effectText = this.getEffectDescriptionFromMove(this.moveTemplate);
               if (effectText != null && !effectText.isEmpty()) {
                  tooltip.add(
                     class_2561.method_43469("move.battleinfo.effect", new Object[]{(int)effectChance})
                        .method_10852(class_2561.method_43470(effectText))
                        .method_27692(class_124.field_1076)
                  );
               } else {
                  tooltip.add(class_2561.method_43469("move.battleinfo.effect", new Object[]{(int)effectChance}).method_27692(class_124.field_1076));
               }
            }
         } catch (Exception var72) {
            CobblemonBattleInfoClient.debug("[MoveTileMixin] Error extracting effect chance: {}", new Object[]{var72.getMessage()});
         }

         if (battle != null && !isStatus) {
            ClientBattleSide opponentSide = BattleSideHelper.getOpponentSide(battle);
            List<ClientBattleActor> actors = opponentSide.getActors();
            List<ClientBattlePokemon> targets = new ArrayList<>();

            for (ClientBattleActor actor : actors) {
               for (ActiveClientBattlePokemon activex : actor.getActivePokemon()) {
                  ClientBattlePokemon battlePokemon = activex.getBattlePokemon();
                  if (battlePokemon != null) {
                     targets.add(battlePokemon);
                  }
               }
            }

            if (targets.size() > 1) {
               Collections.reverse(targets);
            }

            if (!targets.isEmpty()) {
               tooltip.add(class_2561.method_43473());
               int targetNumber = 1;

               for (ClientBattlePokemon battlePokemon : targets) {
                  PokemonProperties properties = battlePokemon.getProperties();
                  String formName = properties.getForm();
                  Species species = battlePokemon.getSpecies();
                  String opponentName = species.getName();
                  String opponentDisplayName = opponentName;

                  try {
                     Method getTranslatedNameMethod = species.getClass().getMethod("getTranslatedName");
                     Object nameComponent = getTranslatedNameMethod.invoke(species);
                     if (nameComponent != null) {
                        Object localized = nameComponent.getClass().getMethod("getString").invoke(nameComponent);
                        if (localized != null && !localized.toString().isEmpty()) {
                           opponentDisplayName = localized.toString();
                        }
                     }
                  } catch (Throwable var64) {
                     try {
                        Method getDisplayName = species.getClass().getMethod("getDisplayName");
                        Object displayName = getDisplayName.invoke(species);
                        if (displayName != null) {
                           Object localized = displayName.getClass().getMethod("getString").invoke(displayName);
                           if (localized != null && !localized.toString().isEmpty()) {
                              opponentDisplayName = localized.toString();
                           }
                        }
                     } catch (Throwable var63) {
                     }
                  }

                  ElementalType type1 = null;
                  ElementalType type2 = null;
                  Object matchedFormObj = null;

                  try {
                     for (FormData form : species.getForms()) {
                        String availableFormName = form.getName();
                        boolean matches = false;
                        if (formName != null && !formName.isEmpty()) {
                           matches = availableFormName.equalsIgnoreCase(formName);
                           if (!matches) {
                              matches = availableFormName.equalsIgnoreCase(formName.replace("_", "-"));
                           }

                           if (!matches) {
                              matches = availableFormName.equalsIgnoreCase(formName.replace("-", "_"));
                           }

                           if (!matches) {
                              matches = availableFormName.replace(" ", "")
                                 .replace("-", "")
                                 .replace("_", "")
                                 .equalsIgnoreCase(formName.replace(" ", "").replace("-", "").replace("_", ""));
                           }
                        }

                        if (matches) {
                           matchedFormObj = form;
                           Iterable<ElementalType> types = form.getTypes();
                           ArrayList<ElementalType> typeList = new ArrayList<>();

                           for (ElementalType type : types) {
                              typeList.add(type);
                           }

                           type1 = typeList.size() > 0 ? typeList.get(0) : null;
                           type2 = typeList.size() > 1 ? typeList.get(1) : null;
                           CobblemonBattleInfoClient.debug(
                              "[MoveTileMixin] Matched form '{}' for {} with types: {}/{}",
                              new Object[]{availableFormName, opponentName, type1 != null ? type1.getName() : "null", type2 != null ? type2.getName() : "null"}
                           );
                           break;
                        }
                     }
                  } catch (Exception var71) {
                     CobblemonBattleInfoClient.debug("[MoveTileMixin] Error matching form: {}", new Object[]{var71.getMessage()});
                     Pokemon pokemon = properties.create();
                     type1 = pokemon.getPrimaryType();
                     type2 = pokemon.getSecondaryType();
                  }

                  if (type1 == null) {
                     Pokemon pokemon = properties.create();
                     type1 = pokemon.getPrimaryType();
                     type2 = pokemon.getSecondaryType();
                  }

                  String randType1Name = null;
                  String randType2Name = null;

                  try {
                     Class<?> randomizerClient = Class.forName("cobblemon.randomizer.client.RandomizerClient");
                     String pokemonUUID = battlePokemon.getUuid().toString();
                     String[] wildTypes = (String[])randomizerClient.getMethod("getWildPokemonTypes", String.class).invoke(null, pokemonUUID);
                     if (wildTypes != null && wildTypes.length > 0) {
                        randType1Name = wildTypes[0].toLowerCase();
                        randType2Name = wildTypes.length > 1 ? wildTypes[1].toLowerCase() : null;
                        CobblemonBattleInfoClient.debug(
                           "[MoveTileMixin] Found wild types for {}: {}/{}", new Object[]{opponentName, randType1Name, randType2Name}
                        );
                     }

                     if (randType1Name == null) {
                        Boolean hasOpponentTypes = (Boolean)randomizerClient.getMethod("hasOpponentPokemonTypes").invoke(null);
                        if (hasOpponentTypes != null && hasOpponentTypes) {
                           String[] opponentTypes = (String[])randomizerClient.getMethod("getOpponentPokemonTypes", String.class).invoke(null, pokemonUUID);
                           if (opponentTypes != null && opponentTypes.length > 0) {
                              randType1Name = opponentTypes[0].toLowerCase();
                              randType2Name = opponentTypes.length > 1 ? opponentTypes[1].toLowerCase() : null;
                              CobblemonBattleInfoClient.debug(
                                 "[MoveTileMixin] Found PvP opponent types for {}: {}/{}", new Object[]{opponentName, randType1Name, randType2Name}
                              );
                           }
                        }
                     }
                  } catch (ClassNotFoundException var61) {
                  } catch (Throwable var62) {
                  }

                  String moveType = this.moveTemplate.getElementalType().getName().toLowerCase();
                  String teraType = BattleMessageSubscriber.getTeraType("opponent", opponentName);
                  float effectiveness;
                  if (teraType != null && !teraType.isEmpty()) {
                     effectiveness = TypeChart.getEffectivenessAgainstTypes(moveType, teraType, null);
                  } else {
                     String transformedType = BattleMessageSubscriber.getTransformedType("opponent", opponentName);
                     if (transformedType != null && !transformedType.isEmpty()) {
                        effectiveness = TypeChart.getEffectivenessAgainstTypes(moveType, transformedType, null);
                     } else {
                        List<String> megaTypes = BattleMessageSubscriber.getMegaTypes("opponent", opponentName);
                        if (megaTypes != null && !megaTypes.isEmpty()) {
                           String megaType1 = megaTypes.get(0);
                           String megaType2 = megaTypes.size() > 1 ? megaTypes.get(1) : null;
                           effectiveness = TypeChart.getEffectivenessAgainstTypes(moveType, megaType1, megaType2);
                        } else if (randType1Name != null) {
                           effectiveness = TypeChart.getEffectivenessAgainstTypes(moveType, randType1Name, randType2Name);
                        } else {
                           String addedType = BattleMessageSubscriber.getAddedType("opponent", opponentName);
                           if (addedType != null && !addedType.isEmpty()) {
                              String baseType1 = type1 != null ? type1.getName().toLowerCase() : null;
                              String baseType2 = type2 != null ? type2.getName().toLowerCase() : null;
                              float mult1 = baseType1 != null ? TypeChart.getEffectivenessAgainstTypes(moveType, baseType1, null) : 1.0F;
                              float mult2 = baseType2 != null ? TypeChart.getEffectivenessAgainstTypes(moveType, baseType2, null) : 1.0F;
                              float mult3 = TypeChart.getEffectivenessAgainstTypes(moveType, addedType, null);
                              effectiveness = mult1 * mult2 * mult3;
                           } else {
                              effectiveness = TypeChart.getEffectiveness(this.moveTemplate, type1, type2);
                           }
                        }
                     }
                  }

                  boolean showTargetPrefix = targets.size() > 1;
                  String colorPrimaryType = null;
                  String colorTeraType = BattleMessageSubscriber.getTeraType("opponent", opponentName);
                  if (colorTeraType != null && !colorTeraType.isEmpty()) {
                     colorPrimaryType = colorTeraType;
                  } else {
                     String colorTransformedType = BattleMessageSubscriber.getTransformedType("opponent", opponentName);
                     if (colorTransformedType != null && !colorTransformedType.isEmpty()) {
                        colorPrimaryType = colorTransformedType;
                     } else {
                        List<String> colorMegaTypes = BattleMessageSubscriber.getMegaTypes("opponent", opponentName);
                        if (colorMegaTypes != null && !colorMegaTypes.isEmpty()) {
                           colorPrimaryType = colorMegaTypes.get(0);
                        } else if (randType1Name != null) {
                           colorPrimaryType = randType1Name;
                        } else if (type1 != null) {
                           colorPrimaryType = type1.getName();
                        }
                     }
                  }

                  ElementalType resolvedPrimaryTypeForColor = this.getElementalTypeFromName(colorPrimaryType);
                  if (resolvedPrimaryTypeForColor == null) {
                     resolvedPrimaryTypeForColor = this.resolveTargetNameColorType(battlePokemon, opponentName, type1, type2);
                  }

                  int targetPrefixColor = resolvedPrimaryTypeForColor != null ? resolvedPrimaryTypeForColor.getHue() : class_124.field_1080.method_532();
                  class_2561 targetPrefix = showTargetPrefix
                     ? class_2561.method_43470(opponentDisplayName + ": ").method_27694(style -> style.method_36139(targetPrefixColor))
                     : class_2561.method_43473();
                  class_5250 dmgRangeComp = null;
                  if (effectiveness != 0.0F && !isStatus && power > 0.0 && BattleExtrasConfig.isMoveDamageRangeEnabled()) {
                     dmgRangeComp = this.computeDamageRangeComponent(
                        activePlayerPokemon,
                        playerPokemon,
                        playerPokemonName,
                        species,
                        matchedFormObj,
                        opponentName,
                        this.moveTemplate,
                        effectiveness,
                        stabMultiplier,
                        battlePokemon
                     );
                  }

                  class_5250 effectComp;
                  if (effectiveness == 0.0F) {
                     effectComp = class_2561.method_43469("move.battleinfo.immune", new Object[]{"×0"}).method_27692(class_124.field_1063);
                  } else if (effectiveness <= 0.25F) {
                     effectComp = class_2561.method_43469("move.battleinfo.mostly_ineffective", new Object[]{"×0.25"}).method_27692(class_124.field_1063);
                  } else if (effectiveness < 1.0F) {
                     effectComp = class_2561.method_43469("move.battleinfo.not_effective", new Object[]{"×0.5"}).method_27692(class_124.field_1080);
                  } else if (effectiveness == 1.0F) {
                     effectComp = class_2561.method_43469("move.battleinfo.normal_effective", new Object[]{"×1"}).method_27692(class_124.field_1080);
                  } else if (effectiveness >= 4.0F) {
                     effectComp = class_2561.method_43469("move.battleinfo.extremely_effective", new Object[]{"×4"}).method_27692(class_124.field_1065);
                  } else {
                     effectComp = class_2561.method_43469("move.battleinfo.super_effective", new Object[]{"×2"}).method_27692(class_124.field_1060);
                  }

                  if (dmgRangeComp != null) {
                     effectComp.method_10852(dmgRangeComp);
                  }

                  tooltip.add(class_2561.method_43473().method_10852(targetPrefix).method_10852(effectComp));
                  targetNumber++;
               }
            }
         }

         if (!tooltip.isEmpty()) {
            ElementalType moveTypex = this.moveTemplate.getElementalType();
            DamageCategory moveCategory = this.moveTemplate.getDamageCategory();
            cacheMoveTooltipData(
               this.moveTemplate, cachePp, cacheMaxPp, turnNumber, weatherKey, opponentHpKey, opponentStateKey, tooltip, typeColor, moveTypex, moveCategory
            );
            MoveTooltipOverlayState.queue(tooltip, typeColor, moveTypex, moveCategory, tooltipX, tooltipY, anchorBottomY, tileBounds);
         }
      } catch (Throwable var75) {
      }
   }

   private InBattleMove getInBattleMoveForTile() {
      try {
         if (!inBattleMoveFieldResolved) {
            inBattleMoveFieldResolved = true;
            inBattleMoveField = MoveTile.class.getDeclaredField("move");
            inBattleMoveField.setAccessible(true);
         }

         if (inBattleMoveField == null) {
            return null;
         } else {
            Object value = inBattleMoveField.get(this);
            return value instanceof InBattleMove ? (InBattleMove)value : null;
         }
      } catch (Throwable var2) {
         return null;
      }
   }

   private static boolean isCachedMoveTooltipValid(
      MoveTemplate template, int currentPp, int maxPp, int turnNumber, String weatherKey, int opponentHpKey, String opponentStateKey
   ) {
      if (cachedMoveTooltipLines == null) {
         return false;
      } else if (cachedMoveTooltipTemplate != template) {
         return false;
      } else if (cachedMoveTooltipCurrentPp != currentPp || cachedMoveTooltipMaxPp != maxPp) {
         return false;
      } else if (cachedMoveTooltipTurnNumber != turnNumber) {
         return false;
      } else if (cachedMoveTooltipOpponentHpKey != opponentHpKey) {
         return false;
      } else {
         return !cachedMoveTooltipWeatherKey.equals(weatherKey) ? false : cachedMoveTooltipOpponentStateKey.equals(opponentStateKey);
      }
   }

   private static void cacheMoveTooltipData(
      MoveTemplate template,
      int currentPp,
      int maxPp,
      int turnNumber,
      String weatherKey,
      int opponentHpKey,
      String opponentStateKey,
      List<class_2561> tooltipLines,
      int typeColor,
      ElementalType moveType,
      DamageCategory moveCategory
   ) {
      cachedMoveTooltipTemplate = template;
      cachedMoveTooltipCurrentPp = currentPp;
      cachedMoveTooltipMaxPp = maxPp;
      cachedMoveTooltipTurnNumber = turnNumber;
      cachedMoveTooltipWeatherKey = weatherKey;
      cachedMoveTooltipOpponentHpKey = opponentHpKey;
      cachedMoveTooltipOpponentStateKey = opponentStateKey;
      cachedMoveTooltipLines = new ArrayList<>(tooltipLines);
      cachedMoveTooltipTypeColor = typeColor;
      cachedMoveTooltipMoveType = moveType;
      cachedMoveTooltipMoveCategory = moveCategory;
   }

   private String getEffectDescriptionFromMove(MoveTemplate move) {
      try {
         Method secondaryEffectMethod = move.getClass().getMethod("getSecondaryEffects");
         Object effects = secondaryEffectMethod.invoke(move);
         if (effects != null) {
            if (effects instanceof List<?> effectList) {
               if (!effectList.isEmpty()) {
                  Object firstEffect = effectList.get(0);
                  return this.extractEffectDescription(firstEffect);
               }
            } else if (effects.getClass().isArray()) {
               int length = Array.getLength(effects);
               if (length > 0) {
                  Object firstEffect = Array.get(effects, 0);
                  return this.extractEffectDescription(firstEffect);
               }
            }
         }
      } catch (Exception var7) {
         CobblemonBattleInfoClient.debug("[MoveTileMixin] Failed to get secondary effects for {}: {}", new Object[]{move.getName(), var7.getMessage()});
      }

      try {
         Method statChangesMethod = move.getClass().getMethod("getStatChanges");
         Object statChanges = statChangesMethod.invoke(move);
         if (statChanges != null) {
            if (statChanges instanceof List && !((List)statChanges).isEmpty()) {
               return " Stat ↑/↓";
            }

            if (statChanges.getClass().isArray() && Array.getLength(statChanges) > 0) {
               return " Stat ↑/↓";
            }
         }
      } catch (Exception var6) {
      }

      return "";
   }

   private String extractEffectDescription(Object effect) {
      try {
         Method statusMethod = effect.getClass().getMethod("getStatus");
         Object status = statusMethod.invoke(effect);
         label74:
         if (status != null) {
            String statusName = status.toString().toLowerCase();
            if (!statusName.contains("burn") && !statusName.contains("brn")) {
               if (!statusName.contains("paralyze") && !statusName.contains("par")) {
                  if (!statusName.contains("poison") && !statusName.contains("psn") && !statusName.contains("tox")) {
                     if (!statusName.contains("sleep") && !statusName.contains("slp")) {
                        if (!statusName.contains("freeze") && !statusName.contains("frz")) {
                           if (statusName.contains("confuse")) {
                              return " Confuse";
                           }
                           break label74;
                        }

                        return " Freeze";
                     }

                     return " Sleep";
                  }

                  return " Poison";
               }

               return " Paralyze";
            }

            return " Burn";
         }
      } catch (Exception var6) {
      }

      try {
         Method volatileMethod = effect.getClass().getMethod("getVolatileStatus");
         Object volatileStatus = volatileMethod.invoke(effect);
         if (volatileStatus != null) {
            String volatileName = volatileStatus.toString().toLowerCase();
            if (volatileName.contains("flinch")) {
               return " Flinch";
            }

            if (volatileName.contains("confuse")) {
               return " Confuse";
            }
         }
      } catch (Exception var5) {
      }

      return "";
   }

   private ElementalType getElementalTypeFromName(String typeName) {
      if (typeName == null) {
         return null;
      } else {
         String lowerName = this.normalizeTypeName(typeName);
         if (lowerName.isEmpty()) {
            return null;
         } else {
            switch (lowerName) {
               case "normal":
                  return ElementalTypes.INSTANCE.getNORMAL();
               case "fire":
                  return ElementalTypes.INSTANCE.getFIRE();
               case "water":
                  return ElementalTypes.INSTANCE.getWATER();
               case "electric":
                  return ElementalTypes.INSTANCE.getELECTRIC();
               case "grass":
                  return ElementalTypes.INSTANCE.getGRASS();
               case "ice":
                  return ElementalTypes.INSTANCE.getICE();
               case "fighting":
                  return ElementalTypes.INSTANCE.getFIGHTING();
               case "poison":
                  return ElementalTypes.INSTANCE.getPOISON();
               case "ground":
                  return ElementalTypes.INSTANCE.getGROUND();
               case "flying":
                  return ElementalTypes.INSTANCE.getFLYING();
               case "psychic":
                  return ElementalTypes.INSTANCE.getPSYCHIC();
               case "bug":
                  return ElementalTypes.INSTANCE.getBUG();
               case "rock":
                  return ElementalTypes.INSTANCE.getROCK();
               case "ghost":
                  return ElementalTypes.INSTANCE.getGHOST();
               case "dragon":
                  return ElementalTypes.INSTANCE.getDRAGON();
               case "dark":
                  return ElementalTypes.INSTANCE.getDARK();
               case "steel":
                  return ElementalTypes.INSTANCE.getSTEEL();
               case "fairy":
                  return ElementalTypes.INSTANCE.getFAIRY();
               default:
                  return null;
            }
         }
      }
   }

   private ElementalType resolveTargetNameColorType(ClientBattlePokemon battlePokemon, String opponentName, ElementalType baseType1, ElementalType baseType2) {
      try {
         String uuid = battlePokemon.getUuid() != null ? battlePokemon.getUuid().toString() : null;
         List<ElementalType> displayTypes = TypeIconRenderer.getDisplayTypes(battlePokemon, opponentName, "opponent", uuid);
         if (displayTypes != null && !displayTypes.isEmpty()) {
            ElementalType first = displayTypes.get(0);
            if (first != null) {
               return first;
            }
         }
      } catch (Throwable var8) {
      }

      if (baseType1 != null) {
         return baseType1;
      } else {
         return baseType2 != null ? baseType2 : null;
      }
   }

   private String normalizeTypeName(String rawTypeName) {
      if (rawTypeName == null) {
         return "";
      } else {
         String s = rawTypeName.trim().toLowerCase(Locale.ROOT);
         if (s.isEmpty()) {
            return s;
         } else {
            int colon = s.indexOf(58);
            if (colon >= 0 && colon + 1 < s.length()) {
               s = s.substring(colon + 1);
            }

            int space = s.indexOf(32);
            if (space > 0) {
               s = s.substring(0, space);
            }

            while (!s.isEmpty() && !Character.isLetter(s.charAt(s.length() - 1))) {
               s = s.substring(0, s.length() - 1);
            }

            return s;
         }
      }
   }

   private List<String> wrapText(String text, int maxCharsPerLine) {
      List<String> lines = new ArrayList<>();
      String[] words = text.split(" ");
      StringBuilder currentLine = new StringBuilder();

      for (String word : words) {
         if (currentLine.length() + word.length() + 1 > maxCharsPerLine && currentLine.length() > 0) {
            lines.add(currentLine.toString().trim());
            currentLine = new StringBuilder();
         }

         currentLine.append(word).append(" ");
      }

      if (currentLine.length() > 0) {
         lines.add(currentLine.toString().trim());
      }

      return lines;
   }

   private String formatMoveName(String name) {
      if (name != null && !name.isEmpty()) {
         if (name.contains(" ")) {
            return this.capitalizeWords(name);
         } else if (name.contains("_")) {
            return this.capitalizeWords(name.replace("_", " "));
         } else {
            String spaced = name.replaceAll("([a-z])([A-Z])", "$1 $2");
            if (spaced.contains(" ")) {
               return this.capitalizeWords(spaced);
            } else {
               String lower = name.toLowerCase();
               String[] completeMoveNames = new String[]{
                  "ingrain",
                  "agility",
                  "amnesia",
                  "attract",
                  "barrier",
                  "bide",
                  "blizzard",
                  "disable",
                  "embargo",
                  "encore",
                  "endure",
                  "entrainment",
                  "explosion",
                  "facade",
                  "fissure",
                  "flatter",
                  "foresight",
                  "frustration",
                  "growl",
                  "guillotine",
                  "harden",
                  "hypnosis",
                  "imprison",
                  "incinerate",
                  "inferno",
                  "magnitude",
                  "meditate",
                  "memento",
                  "metronome",
                  "mimic",
                  "minimize",
                  "moonlight",
                  "nightmare",
                  "outrage",
                  "overheat",
                  "payback",
                  "protect",
                  "psych",
                  "psychic",
                  "pursuit",
                  "quiver",
                  "rage",
                  "recover",
                  "recycle",
                  "reflect",
                  "refresh",
                  "rest",
                  "retaliate",
                  "return",
                  "revenge",
                  "roar",
                  "safeguard",
                  "screech",
                  "selfdestruct",
                  "sharpen",
                  "sketch",
                  "snore",
                  "splash",
                  "spore",
                  "stockpile",
                  "stomp",
                  "strength",
                  "struggle",
                  "substitute",
                  "swagger",
                  "swallow",
                  "switcheroo",
                  "synthesis",
                  "tackle",
                  "tailwind",
                  "taunt",
                  "teleport",
                  "thrash",
                  "tickle",
                  "torment",
                  "toxic",
                  "transform",
                  "uproar",
                  "venoshock",
                  "whirlpool",
                  "withdraw",
                  "yawn"
               };

               for (String completeName : completeMoveNames) {
                  if (lower.equals(completeName)) {
                     return this.capitalize(completeName);
                  }
               }

               String[] moveWords = new String[]{
                  "chip",
                  "away",
                  "leer",
                  "tackle",
                  "scratch",
                  "pound",
                  "slam",
                  "stomp",
                  "kick",
                  "punch",
                  "slap",
                  "strike",
                  "smash",
                  "crush",
                  "bash",
                  "whack",
                  "swipe",
                  "slash",
                  "cut",
                  "chop",
                  "hack",
                  "rend",
                  "tear",
                  "rip",
                  "bite",
                  "crunch",
                  "chomp",
                  "gnaw",
                  "fang",
                  "claw",
                  "horn",
                  "tusk",
                  "peck",
                  "drill",
                  "stab",
                  "pierce",
                  "thrust",
                  "lunge",
                  "charge",
                  "rush",
                  "dash",
                  "quick",
                  "swift",
                  "rapid",
                  "speed",
                  "agility",
                  "haste",
                  "throw",
                  "fling",
                  "toss",
                  "hurl",
                  "launch",
                  "shoot",
                  "fire",
                  "blast",
                  "gun",
                  "beam",
                  "ray",
                  "pulse",
                  "wave",
                  "burst",
                  "explosion",
                  "boom",
                  "bang",
                  "spin",
                  "roll",
                  "tumble",
                  "flip",
                  "twist",
                  "whirl",
                  "twirl",
                  "gyro",
                  "jump",
                  "leap",
                  "hop",
                  "bounce",
                  "spring",
                  "vault",
                  "soar",
                  "fly",
                  "dive",
                  "plunge",
                  "drop",
                  "fall",
                  "crash",
                  "land",
                  "wrap",
                  "bind",
                  "squeeze",
                  "constrict",
                  "coil",
                  "trap",
                  "snare",
                  "grab",
                  "hold",
                  "grip",
                  "seize",
                  "catch",
                  "snatch",
                  "steal",
                  "thief",
                  "block",
                  "guard",
                  "shield",
                  "protect",
                  "defend",
                  "barrier",
                  "wall",
                  "dodge",
                  "evade",
                  "avoid",
                  "escape",
                  "flee",
                  "run",
                  "retreat",
                  "vine",
                  "whip",
                  "razor",
                  "leaf",
                  "petal",
                  "seed",
                  "spore",
                  "powder",
                  "grass",
                  "plant",
                  "flower",
                  "bloom",
                  "blossom",
                  "garden",
                  "forest",
                  "jungle",
                  "water",
                  "aqua",
                  "hydro",
                  "surf",
                  "splash",
                  "bubble",
                  "foam",
                  "rain",
                  "drizzle",
                  "storm",
                  "flood",
                  "torrent",
                  "cascade",
                  "waterfall",
                  "flame",
                  "blaze",
                  "scorch",
                  "sear",
                  "ember",
                  "inferno",
                  "heat",
                  "warm",
                  "hot",
                  "lava",
                  "magma",
                  "volcano",
                  "eruption",
                  "ice",
                  "freeze",
                  "frost",
                  "cold",
                  "chill",
                  "snow",
                  "hail",
                  "blizzard",
                  "icicle",
                  "glacier",
                  "arctic",
                  "polar",
                  "winter",
                  "frozen",
                  "electric",
                  "thunder",
                  "lightning",
                  "bolt",
                  "shock",
                  "spark",
                  "zap",
                  "volt",
                  "static",
                  "magnetic",
                  "electro",
                  "plasma",
                  "current",
                  "ground",
                  "earth",
                  "rock",
                  "stone",
                  "boulder",
                  "pebble",
                  "sand",
                  "mud",
                  "dirt",
                  "soil",
                  "quake",
                  "tremor",
                  "fissure",
                  "earthquake",
                  "psychic",
                  "psych",
                  "mind",
                  "mental",
                  "brain",
                  "telekinesis",
                  "telepathy",
                  "confusion",
                  "hypnosis",
                  "dream",
                  "sleep",
                  "rest",
                  "wake",
                  "nightmare",
                  "ghost",
                  "shadow",
                  "shade",
                  "spirit",
                  "soul",
                  "haunt",
                  "curse",
                  "hex",
                  "dark",
                  "night",
                  "black",
                  "evil",
                  "wicked",
                  "sinister",
                  "foul",
                  "nasty",
                  "dragon",
                  "draconic",
                  "draco",
                  "rage",
                  "rampage",
                  "outrage",
                  "fury",
                  "fairy",
                  "charm",
                  "cute",
                  "sweet",
                  "lovely",
                  "beauty",
                  "grace",
                  "elegant",
                  "steel",
                  "iron",
                  "metal",
                  "chrome",
                  "silver",
                  "gold",
                  "bronze",
                  "copper",
                  "poison",
                  "toxic",
                  "venom",
                  "acid",
                  "sludge",
                  "smog",
                  "gas",
                  "fume",
                  "bug",
                  "insect",
                  "beetle",
                  "spider",
                  "web",
                  "string",
                  "silk",
                  "cocoon",
                  "flying",
                  "air",
                  "wind",
                  "gust",
                  "breeze",
                  "tornado",
                  "hurricane",
                  "cyclone",
                  "fighting",
                  "combat",
                  "battle",
                  "martial",
                  "karate",
                  "judo",
                  "normal",
                  "basic",
                  "simple",
                  "plain",
                  "regular",
                  "standard",
                  "common",
                  "attack",
                  "defense",
                  "special",
                  "accuracy",
                  "evasion",
                  "power",
                  "force",
                  "strength",
                  "might",
                  "energy",
                  "aura",
                  "chi",
                  "ki",
                  "boost",
                  "raise",
                  "increase",
                  "enhance",
                  "buff",
                  "up",
                  "rise",
                  "grow",
                  "lower",
                  "reduce",
                  "decrease",
                  "weaken",
                  "debuff",
                  "down",
                  "heal",
                  "recover",
                  "restore",
                  "cure",
                  "remedy",
                  "medicine",
                  "potion",
                  "damage",
                  "hurt",
                  "harm",
                  "pain",
                  "wound",
                  "injure",
                  "destroy",
                  "demolish",
                  "status",
                  "condition",
                  "effect",
                  "state",
                  "mode",
                  "form",
                  "change",
                  "paralyze",
                  "confuse",
                  "attract",
                  "super",
                  "hyper",
                  "ultra",
                  "mega",
                  "giga",
                  "tera",
                  "max",
                  "extreme",
                  "double",
                  "triple",
                  "multi",
                  "twin",
                  "dual",
                  "combo",
                  "chain",
                  "link",
                  "cross",
                  "plus",
                  "minus",
                  "zero",
                  "one",
                  "two",
                  "three",
                  "four",
                  "five",
                  "first",
                  "second",
                  "third",
                  "last",
                  "final",
                  "ultimate",
                  "supreme",
                  "solar",
                  "lunar",
                  "sun",
                  "moon",
                  "star",
                  "cosmic",
                  "space",
                  "galaxy",
                  "light",
                  "bright",
                  "shine",
                  "glow",
                  "flash",
                  "gleam",
                  "sparkle",
                  "glitter",
                  "head",
                  "body",
                  "tail",
                  "arm",
                  "leg",
                  "wing",
                  "fin",
                  "tentacle",
                  "eye",
                  "ear",
                  "nose",
                  "mouth",
                  "tongue",
                  "tooth",
                  "teeth",
                  "jaw",
                  "roar",
                  "growl",
                  "howl",
                  "screech",
                  "scream",
                  "cry",
                  "yell",
                  "shout",
                  "sing",
                  "song",
                  "melody",
                  "tune",
                  "music",
                  "sound",
                  "noise",
                  "echo",
                  "dance",
                  "sway",
                  "groove",
                  "rhythm",
                  "beat",
                  "tempo",
                  "move",
                  "motion",
                  "ball",
                  "bomb",
                  "canon",
                  "cannon",
                  "shot",
                  "arrow",
                  "spear",
                  "sword",
                  "axe",
                  "hammer",
                  "mallet",
                  "club",
                  "staff",
                  "wand",
                  "rod",
                  "stick",
                  "tail",
                  "wing",
                  "claw",
                  "fang",
                  "bite",
                  "sting",
                  "horn",
                  "tusk",
                  "flare",
                  "nova",
                  "comet",
                  "meteor",
                  "asteroid",
                  "orbit",
                  "void",
                  "scald",
                  "steam",
                  "mist",
                  "fog",
                  "haze",
                  "cloud",
                  "sky",
                  "heaven",
                  "hell",
                  "abyss",
                  "void",
                  "null",
                  "empty",
                  "full",
                  "half",
                  "part",
                  "whole",
                  "complete",
                  "total",
                  "partial",
                  "minor",
                  "major",
                  "critical",
                  "secret",
                  "hidden",
                  "stealth",
                  "sneak",
                  "ambush",
                  "surprise",
                  "trap",
                  "toxic",
                  "spit",
                  "spray",
                  "jet",
                  "stream",
                  "geyser",
                  "fountain",
                  "needle",
                  "spine",
                  "thorn",
                  "barb",
                  "spike",
                  "point",
                  "edge",
                  "blade",
                  "reversal",
                  "revenge",
                  "payback",
                  "return",
                  "retaliate",
                  "counter",
                  "bulk",
                  "mass",
                  "weight",
                  "heavy",
                  "light",
                  "feather",
                  "float",
                  "sink",
                  "anchor",
                  "lock",
                  "key",
                  "door",
                  "gate",
                  "portal",
                  "warp",
                  "teleport",
                  "psycho",
                  "mind",
                  "will",
                  "spirit",
                  "heart",
                  "core",
                  "center",
                  "focus",
                  "aurora",
                  "veil",
                  "screen",
                  "reflect",
                  "safeguard",
                  "wish",
                  "trick",
                  "room",
                  "tailwind",
                  "weather",
                  "terrain",
                  "zone",
                  "field",
                  "arena",
                  "court",
                  "sword",
                  "shield",
                  "armor",
                  "shell",
                  "protect",
                  "detect",
                  "endure",
                  "follow",
                  "pursuit",
                  "chase",
                  "hunt",
                  "track",
                  "search",
                  "find",
                  "seek",
                  "sucker",
                  "fake",
                  "feint",
                  "bait",
                  "lure",
                  "decoy",
                  "taunt",
                  "encore",
                  "disable",
                  "imprison",
                  "spite",
                  "grudge",
                  "destiny",
                  "bond",
                  "perish",
                  "assist",
                  "copy",
                  "mimic",
                  "sketch",
                  "metronome",
                  "transform",
                  "ditto",
                  "baton",
                  "pass",
                  "relay",
                  "switch",
                  "pivot",
                  "uturn",
                  "volt",
                  "close",
                  "mach",
                  "bullet",
                  "vacuum",
                  "extreme",
                  "priority",
                  "aqua",
                  "jet",
                  "accel",
                  "rock",
                  "detect",
                  "mat",
                  "block",
                  "wide",
                  "breaking",
                  "swipe",
                  "surging",
                  "strikes",
                  "make",
                  "it",
                  "rain",
                  "stomping",
                  "tantrum",
                  "plot",
                  "smack"
               };
               StringBuilder result = new StringBuilder();
               int i = 0;

               while (i < lower.length()) {
                  String bestMatch = null;
                  int bestLen = 0;

                  for (String word : moveWords) {
                     if (lower.startsWith(word, i) && word.length() > bestLen) {
                        bestMatch = word;
                        bestLen = word.length();
                     }
                  }

                  if (bestMatch != null) {
                     if (result.length() > 0) {
                        result.append(" ");
                     }

                     result.append(this.capitalize(bestMatch));
                     i += bestLen;
                  } else {
                     char c = lower.charAt(i);
                     if (result.length() == 0) {
                        result.append(Character.toUpperCase(c));
                     } else if (result.charAt(result.length() - 1) == ' ') {
                        result.append(Character.toUpperCase(c));
                     } else {
                        result.append(c);
                     }

                     i++;
                  }
               }

               return result.toString();
            }
         }
      } else {
         return name;
      }
   }

   private String capitalizeWords(String str) {
      String[] words = str.split(" ");
      StringBuilder result = new StringBuilder();

      for (int i = 0; i < words.length; i++) {
         if (i > 0) {
            result.append(" ");
         }

         result.append(this.capitalize(words[i]));
      }

      return result.toString();
   }

   private String capitalize(String str) {
      return str != null && !str.isEmpty() ? Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase() : str;
   }

   private List<ElementalType> getCurrentFormTypes(ClientBattlePokemon battlePokemon) {
      String pokemonId = null;
      String formName = null;
      String speciesName = null;

      try {
         Method getProperties = battlePokemon.getClass().getMethod("getProperties");
         Object properties = getProperties.invoke(battlePokemon);
         if (properties != null) {
            Method getFormMethod = properties.getClass().getMethod("getForm");
            formName = getFormMethod.invoke(properties).toString();
            Method getSpeciesMethod = battlePokemon.getClass().getMethod("getSpecies");
            Object species = getSpeciesMethod.invoke(battlePokemon);
            if (species != null) {
               speciesName = species.toString();
            }
         }

         pokemonId = speciesName + "_" + formName;
      } catch (Throwable var31) {
         pokemonId = "unknown_" + System.identityHashCode(battlePokemon);
      }

      long currentTime = System.currentTimeMillis();
      if (pokemonId.equals(cachedOpponentId) && cachedFormTypes != null && currentTime - lastCacheTime < 1000L) {
         return new ArrayList<>(cachedFormTypes);
      } else {
         List<ElementalType> types = new ArrayList<>();
         String methodUsed = "none";

         try {
            Method getEffectedPokemon = battlePokemon.getClass().getMethod("getEffectedPokemon");
            Object effectedPokemon = getEffectedPokemon.invoke(battlePokemon);
            if (effectedPokemon != null) {
               try {
                  Method getForm = effectedPokemon.getClass().getMethod("getForm");
                  Object form = getForm.invoke(effectedPokemon);
                  if (form != null) {
                     Method getTypes = form.getClass().getMethod("getTypes");
                     Object typesObj = getTypes.invoke(form);
                     if (typesObj instanceof Iterable) {
                        for (Object type : (Iterable)typesObj) {
                           if (type instanceof ElementalType) {
                              types.add((ElementalType)type);
                           }
                        }

                        if (!types.isEmpty()) {
                           methodUsed = "effectedPokemon.getForm()";
                        }
                     }
                  }
               } catch (Throwable var40) {
               }

               if (types.isEmpty()) {
                  try {
                     Method getPrimaryType = effectedPokemon.getClass().getMethod("getPrimaryType");
                     Method getSecondaryType = effectedPokemon.getClass().getMethod("getSecondaryType");
                     ElementalType type1 = (ElementalType)getPrimaryType.invoke(effectedPokemon);
                     ElementalType type2 = (ElementalType)getSecondaryType.invoke(effectedPokemon);
                     if (type1 != null) {
                        types.add(type1);
                     }

                     if (type2 != null) {
                        types.add(type2);
                     }

                     if (!types.isEmpty()) {
                        methodUsed = "effectedPokemon types";
                     }
                  } catch (Throwable var30) {
                  }
               }
            }
         } catch (Throwable var41) {
         }

         if (types.isEmpty()) {
            String[] pokemonGetterMethods = new String[]{"getPokemon", "pokemon", "getOriginalPokemon"};

            for (String methodName : pokemonGetterMethods) {
               try {
                  Method method = battlePokemon.getClass().getMethod(methodName);
                  Object pokemon = method.invoke(battlePokemon);
                  if (pokemon != null) {
                     try {
                        Method getForm = pokemon.getClass().getMethod("getForm");
                        Object form = getForm.invoke(pokemon);
                        if (form != null) {
                           Method getTypes = form.getClass().getMethod("getTypes");
                           Object typesObj = getTypes.invoke(form);
                           if (typesObj instanceof Iterable) {
                              for (Object typex : (Iterable)typesObj) {
                                 if (typex instanceof ElementalType) {
                                    types.add((ElementalType)typex);
                                 }
                              }

                              if (!types.isEmpty()) {
                                 methodUsed = methodName + ".getForm()";
                                 break;
                              }
                           }
                        }
                     } catch (Throwable var38) {
                     }
                  }
               } catch (Throwable var39) {
               }
            }
         }

         if (types.isEmpty()) {
            try {
               Method getForm = battlePokemon.getClass().getMethod("getForm");
               Object form = getForm.invoke(battlePokemon);
               if (form != null) {
                  Method getTypes = form.getClass().getMethod("getTypes");
                  Object typesObj = getTypes.invoke(form);
                  if (typesObj instanceof Iterable) {
                     for (Object typexx : (Iterable)typesObj) {
                        if (typexx instanceof ElementalType) {
                           types.add((ElementalType)typexx);
                        }
                     }

                     if (!types.isEmpty()) {
                        methodUsed = "battlePokemon.getForm()";
                     }
                  }
               }
            } catch (Throwable var37) {
            }
         }

         if (types.isEmpty()) {
            try {
               Method getProperties = battlePokemon.getClass().getMethod("getProperties");
               Object properties = getProperties.invoke(battlePokemon);
               if (properties != null) {
                  Method getFormMethod = properties.getClass().getMethod("getForm");
                  Object formNameObj = getFormMethod.invoke(properties);
                  Method getSpeciesMethod = battlePokemon.getClass().getMethod("getSpecies");
                  Object species = getSpeciesMethod.invoke(battlePokemon);
                  if (formNameObj != null && species != null) {
                     String formNameStr = formNameObj.toString();

                     try {
                        Method getFormByName = species.getClass().getMethod("getForm", String.class);
                        Object form = getFormByName.invoke(species, formNameStr);
                        if (form != null) {
                           Method getTypes = form.getClass().getMethod("getTypes");
                           Object typesObj = getTypes.invoke(form);
                           if (typesObj instanceof Iterable) {
                              for (Object typexxx : (Iterable)typesObj) {
                                 if (typexxx instanceof ElementalType) {
                                    types.add((ElementalType)typexxx);
                                 }
                              }

                              if (!types.isEmpty()) {
                                 methodUsed = "species.getForm(name)";
                              }
                           }
                        }
                     } catch (NoSuchMethodException var35) {
                        try {
                           Method getForms = species.getClass().getMethod("getForms");
                           Object forms = getForms.invoke(species);
                           if (forms instanceof Iterable) {
                              int formCount = 0;

                              for (Object formx : (Iterable)forms) {
                                 formCount++;
                                 Method getName = formx.getClass().getMethod("getName");
                                 String name = getName.invoke(formx).toString();
                                 String normalizedName = name.replaceAll("[''`´-]", "").toLowerCase();
                                 String normalizedFormName = formNameStr.replaceAll("[''`´-]", "").toLowerCase();
                                 if (normalizedName.equals(normalizedFormName)) {
                                    Method getTypes = formx.getClass().getMethod("getTypes");
                                    Object typesObj = getTypes.invoke(formx);
                                    if (typesObj instanceof Iterable) {
                                       for (Object typexxxx : (Iterable)typesObj) {
                                          if (typexxxx instanceof ElementalType) {
                                             types.add((ElementalType)typexxxx);
                                          }
                                       }

                                       if (!types.isEmpty()) {
                                          methodUsed = "species.getForms() iteration";
                                       }
                                    }
                                    break;
                                 }
                              }
                           }
                        } catch (Throwable var34) {
                        }
                     }
                  }
               }
            } catch (Throwable var36) {
            }
         }

         if (types.isEmpty()) {
            try {
               Pokemon pokemon = battlePokemon.getProperties().create();
               if (pokemon != null) {
                  try {
                     Method getForm = pokemon.getClass().getMethod("getForm");
                     Object form = getForm.invoke(pokemon);
                     if (form != null) {
                        Method getTypes = form.getClass().getMethod("getTypes");
                        Object typesObj = getTypes.invoke(form);
                        if (typesObj instanceof Iterable) {
                           for (Object typexxxxx : (Iterable)typesObj) {
                              if (typexxxxx instanceof ElementalType) {
                                 types.add((ElementalType)typexxxxx);
                              }
                           }

                           if (!types.isEmpty()) {
                              methodUsed = "create().getForm() fallback";
                           }
                        }
                     }
                  } catch (Throwable var32) {
                     ElementalType type1x = pokemon.getPrimaryType();
                     ElementalType type2x = pokemon.getSecondaryType();
                     if (type1x != null) {
                        types.add(type1x);
                     }

                     if (type2x != null) {
                        types.add(type2x);
                     }

                     if (!types.isEmpty()) {
                        methodUsed = "create() primary/secondary fallback";
                     }
                  }
               }
            } catch (Throwable var33) {
               LOGGER.error("[getCurrentFormTypes] All methods failed for Pokemon ID: {}", pokemonId, var33);
            }
         }

         if (types.isEmpty()) {
            methodUsed = "FAILED - no types found";
            LOGGER.warn("[getCurrentFormTypes] Failed to get types for Pokemon ID: {}", pokemonId);
         } else {
            String typeNames = types.stream().map(t -> t.getName()).reduce((a, b) -> a + "/" + b).orElse("none");
            CobblemonBattleInfoClient.debug("[getCurrentFormTypes] Pokemon ID: {}, Types: {}, Method: {}", new Object[]{pokemonId, typeNames, methodUsed});
         }

         cachedOpponentId = pokemonId;
         cachedFormTypes = new ArrayList<>(types);
         lastCacheTime = currentTime;
         return types;
      }
   }

   private String getPlayerPokemonName(ClientBattlePokemon battlePokemon) {
      if (battlePokemon == null) {
         return null;
      } else {
         try {
            if (battlePokemon.getDisplayName() != null) {
               String displayName = battlePokemon.getDisplayName().getString();
               if (displayName != null && !displayName.isBlank()) {
                  return displayName.toLowerCase();
               }
            }
         } catch (Exception var5) {
         }

         try {
            if (battlePokemon.getSpecies() != null) {
               String speciesName = battlePokemon.getSpecies().getName();
               if (speciesName != null && !speciesName.isBlank()) {
                  return speciesName.toLowerCase();
               }
            }
         } catch (Exception var4) {
         }

         try {
            Pokemon pokemon = battlePokemon.getProperties().create();
            return pokemon.getSpecies().getName().toLowerCase();
         } catch (Exception var3) {
            return null;
         }
      }
   }

   private ClientBattlePokemon getPlayerActivePokemon(ClientBattle battle) {
      ActiveClientBattlePokemon activePokemon = this.getPlayerActiveBattlePokemon(battle);
      return activePokemon != null ? activePokemon.getBattlePokemon() : null;
   }

   private ActiveClientBattlePokemon getPlayerActiveBattlePokemon(ClientBattle battle) {
      if (battle == null) {
         return null;
      } else {
         try {
            SingleActionRequest request = battle.getFirstUnansweredRequest();
            if (request != null && request.getActivePokemon() != null) {
               return request.getActivePokemon();
            }

            ClientBattleSide playerSide = BattleSideHelper.getPlayerSide(battle);
            if (playerSide == null) {
               return null;
            }

            List<ClientBattleActor> actors = playerSide.getActors();
            if (!actors.isEmpty()) {
               ClientBattleActor firstActor = actors.get(0);
               List<ActiveClientBattlePokemon> activePokemon = firstActor.getActivePokemon();
               if (!activePokemon.isEmpty()) {
                  return activePokemon.get(0);
               }
            }
         } catch (Exception var7) {
         }

         return null;
      }
   }

   private float calculateStabMultiplier(MoveTemplate move, ClientBattlePokemon playerPokemon, String pokemonName) {
      if (move != null && playerPokemon != null && pokemonName != null) {
         try {
            String moveType = move.getElementalType().getName().toLowerCase();
            String teraType = BattleMessageSubscriber.getTeraType("player", pokemonName);
            CobblemonBattleInfoClient.debug("[STAB] Checking STAB for {} using {} move (type: {})", new Object[]{pokemonName, move.getName(), moveType});
            String transformedType = BattleMessageSubscriber.getTransformedType("player", pokemonName);
            if (transformedType != null && !transformedType.isEmpty()) {
               boolean hasStab = transformedType.equalsIgnoreCase(moveType);
               CobblemonBattleInfoClient.debug(
                  "[STAB] {} has transformed type: {}, move type: {}, native STAB: {}", new Object[]{pokemonName, transformedType, moveType, hasStab}
               );
               return this.resolveTeraAwareStabMultiplier(hasStab, teraType, moveType);
            } else {
               List<String> megaTypes = BattleMessageSubscriber.getMegaTypes("player", pokemonName);
               if (megaTypes != null && !megaTypes.isEmpty()) {
                  boolean hasStab = false;

                  for (String megaType : megaTypes) {
                     if (megaType != null && megaType.equalsIgnoreCase(moveType)) {
                        hasStab = true;
                        break;
                     }
                  }

                  CobblemonBattleInfoClient.debug("[STAB] {} mega types {} native STAB for {}: {}", new Object[]{pokemonName, megaTypes, moveType, hasStab});
                  return this.resolveTeraAwareStabMultiplier(hasStab, teraType, moveType);
               } else {
                  if (teraType == null || teraType.isEmpty()) {
                     try {
                        String pokemonUUID = playerPokemon.getUuid().toString();
                        List<ElementalType> displayTypes = TypeIconRenderer.getDisplayTypes(playerPokemon, pokemonName, "player", pokemonUUID);
                        if (displayTypes != null && !displayTypes.isEmpty()) {
                           for (ElementalType type : displayTypes) {
                              if (type != null && type.getName().equalsIgnoreCase(moveType)) {
                                 CobblemonBattleInfoClient.debug(
                                    "[STAB] {} has type {} (from getDisplayTypes) matching move type {}, STAB: true",
                                    new Object[]{pokemonName, type.getName(), moveType}
                                 );
                                 return 1.5F;
                              }
                           }

                           CobblemonBattleInfoClient.debug(
                              "[STAB] {} has types {} but none match move type {}",
                              new Object[]{pokemonName, displayTypes.stream().map(t -> t.getName()).reduce((a, b) -> a + "/" + b).orElse("NONE"), moveType}
                           );
                        }
                     } catch (Throwable var12) {
                        CobblemonBattleInfoClient.debug("[STAB] Error using getDisplayTypes: {}", new Object[]{var12.getMessage()});
                     }
                  }

                  List<ElementalType> baseTypes = this.getCurrentFormTypes(playerPokemon);
                  boolean currentTypeStab = false;
                  CobblemonBattleInfoClient.debug(
                     "[STAB] {} base types from getCurrentFormTypes: {}",
                     new Object[]{
                        pokemonName, baseTypes.isEmpty() ? "EMPTY" : baseTypes.stream().map(t -> t.getName()).reduce((a, b) -> a + "/" + b).orElse("NONE")
                     }
                  );

                  for (ElementalType typex : baseTypes) {
                     if (typex != null && typex.getName().equalsIgnoreCase(moveType)) {
                        currentTypeStab = true;
                        break;
                     }
                  }

                  String addedType = BattleMessageSubscriber.getAddedType("player", pokemonName);
                  if (addedType != null && !addedType.isEmpty() && addedType.equalsIgnoreCase(moveType)) {
                     currentTypeStab = true;
                  }

                  float stabMultiplier = this.resolveTeraAwareStabMultiplier(currentTypeStab, teraType, moveType);
                  CobblemonBattleInfoClient.debug(
                     "[STAB] Final STAB for {} with move type {} is {} (nativeMatch={}, teraType={})",
                     new Object[]{pokemonName, moveType, stabMultiplier, currentTypeStab, teraType}
                  );
                  return stabMultiplier;
               }
            }
         } catch (Exception var13) {
            LOGGER.error("[STAB] Error calculating STAB for {}: {}", pokemonName, var13.getMessage());
            return 1.0F;
         }
      } else {
         return 1.0F;
      }
   }

   private float resolveTeraAwareStabMultiplier(boolean currentTypeMatch, String teraType, String moveType) {
      boolean teraTypeMatch = teraType != null && !teraType.isEmpty() && teraType.equalsIgnoreCase(moveType);
      if (currentTypeMatch && teraTypeMatch) {
         return 2.0F;
      } else {
         return !currentTypeMatch && !teraTypeMatch ? 1.0F : 1.5F;
      }
   }

   private String getCachedPlayerAbility(String pokemonName, Pokemon pokemon, ClientBattlePokemon battlePokemon) {
      if (pokemonName != null && pokemon != null) {
         long currentTime = System.currentTimeMillis();
         boolean currentIsMega = BattleMessageSubscriber.isMegaEvolved(pokemonName);
         if (CritChanceCache.shouldClearAndReset()) {
            CobblemonBattleInfoClient.debug("[MoveTileMixin] Clearing ability cache due to Mega Evolution flag", new Object[0]);
            clearPlayerAbilityCache();
         }

         if (cachedPlayerPokemonId != null
            && cachedPlayerPokemonId.equals(pokemonName)
            && cachedPlayerAbility != null
            && currentTime - lastAbilityCacheTime < 1000L
            && cachedPlayerIsMega == currentIsMega) {
            CobblemonBattleInfoClient.debug(
               "[MoveTileMixin] Using cached ability for {}: {} (isMega: {})", new Object[]{pokemonName, cachedPlayerAbility, currentIsMega}
            );
            return cachedPlayerAbility;
         } else {
            if (cachedPlayerPokemonId != null && cachedPlayerPokemonId.equals(pokemonName) && cachedPlayerIsMega != currentIsMega) {
               CobblemonBattleInfoClient.debug(
                  "[MoveTileMixin] Mega Evolution state changed for {}: {} -> {}, refreshing ability",
                  new Object[]{pokemonName, cachedPlayerIsMega, currentIsMega}
               );
            }

            String abilityName = null;

            try {
               Method getAbilityMethod = pokemon.getClass().getMethod("getAbility");
               Object ability = getAbilityMethod.invoke(pokemon);
               if (ability != null) {
                  try {
                     Method getNameMethod = ability.getClass().getMethod("getName");
                     abilityName = (String)getNameMethod.invoke(ability);
                  } catch (Exception var14) {
                  }
               }
            } catch (Exception var15) {
            }

            if (abilityName == null || "pressure".equals(abilityName.toLowerCase())) {
               try {
                  Method getAbilityMethod = battlePokemon.getClass().getMethod("getAbility");
                  Object ability = getAbilityMethod.invoke(battlePokemon);
                  if (ability != null) {
                     try {
                        Method getNameMethod = ability.getClass().getMethod("getName");
                        String battleAbilityName = (String)getNameMethod.invoke(ability);
                        if (battleAbilityName != null && !"pressure".equals(battleAbilityName.toLowerCase())) {
                           abilityName = battleAbilityName;
                        }
                     } catch (Exception var12) {
                     }
                  }
               } catch (Exception var13) {
               }
            }

            if ("absol".equals(pokemonName) && (abilityName == null || "pressure".equals(abilityName.toLowerCase()))) {
               abilityName = "superluck";
            }

            if ("absol".equals(pokemonName) && abilityName != null) {
               String lowerAbility = abilityName.toLowerCase();
               if ((!"superluck".equals(lowerAbility) || !"pressure".equals(cachedPlayerAbility))
                  && "pressure".equals(lowerAbility)
                  && "superluck".equals(cachedPlayerAbility)) {
                  return cachedPlayerAbility;
               }
            }

            cachedPlayerPokemonId = pokemonName;
            cachedPlayerAbility = abilityName;
            cachedPlayerIsMega = currentIsMega;
            lastAbilityCacheTime = currentTime;
            CobblemonBattleInfoClient.debug("[MoveTileMixin] Cached ability for {}: {} (isMega: {})", new Object[]{pokemonName, abilityName, currentIsMega});
            return abilityName;
         }
      } else {
         return null;
      }
   }

   private static void clearPlayerAbilityCache() {
      cachedPlayerPokemonId = null;
      cachedPlayerAbility = null;
      cachedPlayerIsMega = false;
      lastAbilityCacheTime = 0L;
   }

   private boolean isMegaEvolved(Pokemon pokemon) {
      if (pokemon == null) {
         return false;
      } else {
         try {
            Method getAspectsMethod = pokemon.getClass().getMethod("getAspects");
            Object aspectsSet = getAspectsMethod.invoke(pokemon);
            if (aspectsSet != null) {
               String aspectsString = aspectsSet.toString().toLowerCase();
               return aspectsString.contains("mega_evolution=mega")
                  || aspectsString.contains("mega_evolution=mega_x")
                  || aspectsString.contains("mega_evolution=mega_y");
            }
         } catch (Exception var6) {
            try {
               String speciesName = pokemon.getSpecies().getName().toLowerCase();
               return speciesName.contains("mega");
            } catch (Exception var5) {
            }
         }

         return false;
      }
   }

   private boolean checkPokemonHeldItemForCrit(Pokemon pokemon) {
      if (pokemon == null) {
         return false;
      } else {
         String[] methodNames = new String[]{"heldItem", "getHeldItem"};

         for (String methodName : methodNames) {
            try {
               Method method = pokemon.getClass().getMethod(methodName);
               Object heldItem = method.invoke(pokemon);
               if (heldItem != null && this.isCritItem(heldItem)) {
                  return true;
               }
            } catch (Exception var11) {
            }
         }

         String[] fieldNames = new String[]{"heldItemNoCopy", "heldItem"};

         for (String fieldName : fieldNames) {
            try {
               Field field = pokemon.getClass().getDeclaredField(fieldName);
               field.setAccessible(true);
               Object heldItem = field.get(pokemon);
               if (heldItem != null && this.isCritItem(heldItem)) {
                  return true;
               }
            } catch (Exception var10) {
            }
         }

         return false;
      }
   }

   private boolean isCritItem(Object item) {
      if (item == null) {
         return false;
      } else {
         try {
            String itemString = item.toString().toLowerCase();
            CobblemonBattleInfoClient.debug("[MoveTileMixin] Checking item: {}", new Object[]{itemString});
            if (itemString.contains("minecraft:air")) {
               return false;
            }

            if (itemString.contains("razor_claw") || itemString.contains("razorclaw") || itemString.contains("scope_lens") || itemString.contains("scopelens")) {
               CobblemonBattleInfoClient.debug("[MoveTileMixin] Found crit-boosting item: {}", new Object[]{itemString});
               return true;
            }

            try {
               Method getIdMethod = item.getClass().getMethod("getId");
               Object result = getIdMethod.invoke(item);
               if (result != null) {
                  String resultStr = result.toString().toLowerCase();
                  if (resultStr.contains("razor_claw") || resultStr.contains("scope_lens")) {
                     CobblemonBattleInfoClient.debug("[MoveTileMixin] Found crit-boosting item via getId: {}", new Object[]{resultStr});
                     return true;
                  }
               }
            } catch (Exception var6) {
            }
         } catch (Exception var7) {
            CobblemonBattleInfoClient.debug("[MoveTileMixin] Error checking if item is crit item: {}", new Object[]{var7.getMessage()});
         }

         return false;
      }
   }

   private boolean checkBattlePokemonHeldItemForCrit(ClientBattlePokemon battlePokemon) {
      if (battlePokemon == null) {
         return false;
      } else {
         String[] methodNames = new String[]{"getHeldItem", "heldItem"};

         for (String methodName : methodNames) {
            try {
               Method method = battlePokemon.getClass().getMethod(methodName);
               Object heldItem = method.invoke(battlePokemon);
               if (heldItem != null && this.isCritItem(heldItem)) {
                  CobblemonBattleInfoClient.debug("[MoveTileMixin] Found crit item via BattlePokemon.{}: {}", new Object[]{methodName, heldItem});
                  return true;
               }
            } catch (Exception var11) {
            }
         }

         String[] fieldNames = new String[]{"heldItem", "item"};

         for (String fieldName : fieldNames) {
            try {
               Field field = battlePokemon.getClass().getDeclaredField(fieldName);
               field.setAccessible(true);
               Object heldItem = field.get(battlePokemon);
               if (heldItem != null && this.isCritItem(heldItem)) {
                  CobblemonBattleInfoClient.debug("[MoveTileMixin] Found crit item via BattlePokemon field {}: {}", new Object[]{fieldName, heldItem});
                  return true;
               }
            } catch (Exception var10) {
            }
         }

         return false;
      }
   }

   private double getCritChancePercentage(String pokemonName, ClientBattlePokemon battlePokemon, MoveTemplate move) {
      if (pokemonName == null) {
         return 4.17;
      } else {
         int critStage = 0;
         CobblemonBattleInfoClient.debug("[MoveTileMixin] ========================================", new Object[0]);
         CobblemonBattleInfoClient.debug("[MoveTileMixin] Calculating crit chance for: {}", new Object[]{pokemonName});
         CobblemonBattleInfoClient.debug("[MoveTileMixin] Move: {}", new Object[]{move != null ? move.getName() : "null"});

         try {
            if (move != null) {
               try {
                  Method getCritRatioMethod = move.getClass().getMethod("getCritRatio");
                  Object critRatioObj = getCritRatioMethod.invoke(move);
                  if (critRatioObj != null) {
                     int critRatio = 0;
                     if (critRatioObj instanceof Integer) {
                        critRatio = (Integer)critRatioObj;
                     } else if (critRatioObj instanceof Double) {
                        critRatio = ((Double)critRatioObj).intValue();
                     }

                     CobblemonBattleInfoClient.debug("[MoveTileMixin] Move crit ratio: {}", new Object[]{critRatio});
                     if (critRatio > 1) {
                        int stageBonus = critRatio - 1;
                        critStage += stageBonus;
                        CobblemonBattleInfoClient.debug(
                           "[MoveTileMixin] ✓✓✓ High-crit move! Added {} stage(s) (critStage now: {})", new Object[]{stageBonus, critStage}
                        );
                     }
                  }
               } catch (NoSuchMethodException var11) {
                  CobblemonBattleInfoClient.debug("[MoveTileMixin] getCritRatio method not found, move likely has normal crit ratio", new Object[0]);
               } catch (Exception var12) {
                  CobblemonBattleInfoClient.debug("[MoveTileMixin] Error checking move crit ratio: {}", new Object[]{var12.getMessage()});
               }
            }

            Map<String, Integer> statStages = BattleMessageSubscriber.getStatStagesForPokemon("player", pokemonName);
            if (statStages != null && (statStages.containsKey("crit") || statStages.containsKey("Crit"))) {
               int statCritStage = statStages.containsKey("crit") ? statStages.get("crit") : statStages.get("Crit");
               critStage += statCritStage;
               CobblemonBattleInfoClient.debug(
                  "[MoveTileMixin] Added {} crit stage(s) from stat stages (critStage now: {})", new Object[]{statCritStage, critStage}
               );
            } else {
               CobblemonBattleInfoClient.debug("[MoveTileMixin] No base crit stages found", new Object[0]);
            }

            CobblemonBattleInfoClient.debug("[MoveTileMixin] Checking for held items...", new Object[0]);
            String heldItem = BattleMessageSubscriber.getHeldItem(pokemonName);
            if (heldItem != null) {
               CobblemonBattleInfoClient.debug("[MoveTileMixin] Held item: {}", new Object[]{heldItem});
               String lowerItem = heldItem.toLowerCase();
               if (lowerItem.contains("razor_claw") || lowerItem.contains("razorclaw") || lowerItem.contains("scope_lens") || lowerItem.contains("scopelens")) {
                  CobblemonBattleInfoClient.debug("[MoveTileMixin] ✓✓✓ Held item boosts crit! (critStage now: {})", new Object[]{++critStage});
               } else if (!lowerItem.contains("lucky_punch") && !lowerItem.contains("luckypunch")) {
                  if ((lowerItem.contains("leek") || lowerItem.contains("stick"))
                     && (
                        pokemonName.equalsIgnoreCase("farfetchd")
                           || pokemonName.equalsIgnoreCase("farfetch'd")
                           || pokemonName.equalsIgnoreCase("sirfetchd")
                           || pokemonName.equalsIgnoreCase("sirfetch'd")
                     )) {
                     critStage += 2;
                     CobblemonBattleInfoClient.debug(
                        "[MoveTileMixin] ✓✓✓ Leek detected for Farfetch'd/Sirfetch'd! (critStage now: {})", new Object[]{critStage}
                     );
                  }
               } else if (pokemonName.equalsIgnoreCase("chansey")) {
                  critStage += 2;
                  CobblemonBattleInfoClient.debug("[MoveTileMixin] ✓✓✓ Lucky Punch detected for Chansey! (critStage now: {})", new Object[]{critStage});
               }
            } else {
               CobblemonBattleInfoClient.debug("[MoveTileMixin] No held item", new Object[0]);
            }

            CobblemonBattleInfoClient.debug("[MoveTileMixin] Checking for ability bonuses...", new Object[0]);
            if (battlePokemon != null) {
               try {
                  Pokemon pokemon = battlePokemon.getProperties().create();
                  CobblemonBattleInfoClient.debug("[MoveTileMixin] Pokemon object: {}", new Object[]{pokemon});
                  if (pokemon != null) {
                     boolean isMega = BattleMessageSubscriber.isMegaEvolved(pokemonName);
                     CobblemonBattleInfoClient.debug("[MoveTileMixin] Is Mega Evolved: {}", new Object[]{isMega});
                     if (!isMega) {
                        String currentAbility = this.getCachedPlayerAbility(pokemonName, pokemon, battlePokemon);
                        CobblemonBattleInfoClient.debug("[MoveTileMixin] Current ability: {}", new Object[]{currentAbility});
                        if (currentAbility != null) {
                           String lowerAbilityName = currentAbility.toLowerCase();
                           if (!lowerAbilityName.contains("super_luck")
                              && !lowerAbilityName.contains("superluck")
                              && !lowerAbilityName.contains("super luck")
                              && !lowerAbilityName.equals("superluck")) {
                              CobblemonBattleInfoClient.debug("[MoveTileMixin] Ability does not boost crit: {}", new Object[]{currentAbility});
                           } else {
                              CobblemonBattleInfoClient.debug("[MoveTileMixin] ✓✓✓ Super Luck detected! (critStage now: {})", new Object[]{++critStage});
                           }
                        } else {
                           CobblemonBattleInfoClient.debug("[MoveTileMixin] No ability found (null)", new Object[0]);
                        }
                     } else {
                        CobblemonBattleInfoClient.debug("[MoveTileMixin] Skipping ability check because Pokemon is Mega Evolved", new Object[0]);
                     }
                  }
               } catch (Exception var13) {
                  CobblemonBattleInfoClient.debug("[MoveTileMixin] Ability check failed: {}", new Object[]{var13.getMessage()});
                  var13.printStackTrace();
               }
            }

            CobblemonBattleInfoClient.debug("[MoveTileMixin] Final crit stage: {}", new Object[]{critStage});
            double result;
            if (critStage <= 0) {
               result = 4.17;
            } else if (critStage == 1) {
               result = 12.5;
            } else if (critStage == 2) {
               result = 50.0;
            } else {
               result = 100.0;
            }

            CobblemonBattleInfoClient.debug("[MoveTileMixin] Final crit chance: {}%", new Object[]{result});
            CobblemonBattleInfoClient.debug("[MoveTileMixin] ========================================", new Object[0]);
            return result;
         } catch (Exception var14) {
            LOGGER.error("[MoveTileMixin] Critical error in getCritChancePercentage", var14);
            CobblemonBattleInfoClient.debug("[MoveTileMixin] Returning default 4.17%", new Object[0]);
            return 4.17;
         }
      }
   }

   private double calculateAdjustedAccuracy(double baseAccuracy, String pokemonName, ClientBattlePokemon battlePokemon, MoveTemplate move) {
      if (baseAccuracy <= 0.0) {
         return baseAccuracy;
      } else {
         double adjustedAccuracy = baseAccuracy;
         if (move != null) {
            String moveName = move.getName().toLowerCase();
            String currentWeather = BattleMessageSubscriber.getCurrentWeather().toLowerCase();
            if (moveName.equals("thunder") || moveName.equals("hurricane")) {
               if (currentWeather.contains("rain")) {
                  adjustedAccuracy = 100.0;
                  CobblemonBattleInfoClient.debug("[MoveTileMixin] {} in rain - 100% accuracy!", new Object[]{move.getName()});
                  return adjustedAccuracy;
               }

               if (currentWeather.contains("sun") || currentWeather.contains("harsh")) {
                  adjustedAccuracy = 50.0;
                  CobblemonBattleInfoClient.debug("[MoveTileMixin] {} in harsh sunlight - 50% accuracy!", new Object[]{move.getName()});
                  return adjustedAccuracy;
               }
            }

            if (moveName.equals("blizzard") && (currentWeather.contains("snow") || currentWeather.contains("hail"))) {
               adjustedAccuracy = 100.0;
               CobblemonBattleInfoClient.debug("[MoveTileMixin] Blizzard in snow/hail - 100% accuracy!", new Object[0]);
               return adjustedAccuracy;
            }
         }

         try {
            if (battlePokemon != null) {
               String abilityName = this.getAbilityNameForAccuracy(battlePokemon);
               if (abilityName != null) {
                  String lowerAbility = abilityName.toLowerCase().replace(" ", "").replace("_", "");
                  if (lowerAbility.equals("compoundeyes") || lowerAbility.equals("compoundlens")) {
                     adjustedAccuracy *= 1.3;
                     CobblemonBattleInfoClient.debug("[MoveTileMixin] Compound Eyes detected! Accuracy boosted by 30%", new Object[0]);
                  } else if (lowerAbility.equals("victorystar")) {
                     adjustedAccuracy *= 1.1;
                     CobblemonBattleInfoClient.debug("[MoveTileMixin] Victory Star detected! Accuracy boosted by 10%", new Object[0]);
                  } else if (lowerAbility.equals("hustle") && move != null && move.getDamageCategory() == DamageCategories.INSTANCE.getPHYSICAL()) {
                     adjustedAccuracy *= 0.8;
                     CobblemonBattleInfoClient.debug("[MoveTileMixin] Hustle detected! Physical move accuracy reduced by 20%", new Object[0]);
                  }
               }
            }

            Map<String, Integer> statStages = this.findStatsForPokemon(pokemonName);
            CobblemonBattleInfoClient.debug("[MoveTileMixin] calculateAdjustedAccuracy: pokemonName='{}', statStages={}", new Object[]{pokemonName, statStages});
            int accuracyStage = 0;
            if (statStages != null) {
               if (statStages.containsKey("accuracy")) {
                  accuracyStage = statStages.get("accuracy");
               } else if (statStages.containsKey("ACC")) {
                  accuracyStage = statStages.get("ACC");
               }
            }

            if (accuracyStage != 0) {
               double stageMultiplier = 1.0;
               if (accuracyStage > 0) {
                  stageMultiplier = (3.0 + (double)accuracyStage) / 3.0;
               } else if (accuracyStage < 0) {
                  stageMultiplier = 3.0 / (3.0 - (double)accuracyStage);
               }

               adjustedAccuracy *= stageMultiplier;
               CobblemonBattleInfoClient.debug("[MoveTileMixin] Accuracy stage: {}, multiplier: {}", new Object[]{accuracyStage, stageMultiplier});
            }

            String heldItem = BattleMessageSubscriber.getHeldItem(pokemonName);
            if (heldItem != null) {
               String lowerItem = heldItem.toLowerCase();
               if (lowerItem.contains("wide_lens") || lowerItem.contains("widelens")) {
                  adjustedAccuracy *= 1.0998535F;
                  CobblemonBattleInfoClient.debug("[MoveTileMixin] Wide Lens detected! Accuracy boosted by ~10%", new Object[0]);
               } else if (lowerItem.contains("zoom_lens") || lowerItem.contains("zoomlens")) {
                  adjustedAccuracy *= 1.1999512F;
                  CobblemonBattleInfoClient.debug("[MoveTileMixin] Zoom Lens detected! Accuracy boosted by ~20%", new Object[0]);
               }
            }

            if (adjustedAccuracy > 100.0) {
               adjustedAccuracy = 100.0;
            }

            CobblemonBattleInfoClient.debug("[MoveTileMixin] Base accuracy: {}%, Adjusted accuracy: {}%", new Object[]{baseAccuracy, adjustedAccuracy});
         } catch (Exception var12) {
            LOGGER.error("[MoveTileMixin] Error calculating adjusted accuracy", var12);
         }

         return adjustedAccuracy;
      }
   }

   private String formatPriorityValue(int priority) {
      return priority > 0 ? "+" + priority : Integer.toString(priority);
   }

   private class_124 getPriorityFormatting(int priority) {
      if (priority > 0) {
         return class_124.field_1065;
      } else {
         return priority < 0 ? class_124.field_1061 : class_124.field_1080;
      }
   }

   private String getAbilityNameForAccuracy(ClientBattlePokemon battlePokemon) {
      return null;
   }

   private String extractAbilityName(Object ability) {
      if (ability == null) {
         return null;
      } else {
         try {
            Method getDisplayName = ability.getClass().getMethod("getDisplayName");
            Object displayName = getDisplayName.invoke(ability);
            if (displayName != null) {
               try {
                  Method getString = displayName.getClass().getMethod("getString");
                  Object str = getString.invoke(displayName);
                  if (str != null && !str.toString().isEmpty()) {
                     return str.toString();
                  }
               } catch (Exception var13) {
                  String result = displayName.toString();
                  if (result != null && !result.isEmpty()) {
                     return result;
                  }
               }
            }
         } catch (Exception var14) {
         }

         try {
            Method getNameMethod = ability.getClass().getMethod("getName");
            Object name = getNameMethod.invoke(ability);
            if (name != null && !name.toString().isEmpty()) {
               return name.toString();
            }
         } catch (Exception var9) {
         }

         try {
            Method getTemplate = ability.getClass().getMethod("getTemplate");
            Object template = getTemplate.invoke(ability);
            if (template != null) {
               try {
                  Method getDisplayName = template.getClass().getMethod("getDisplayName");
                  Object displayName = getDisplayName.invoke(template);
                  if (displayName != null) {
                     try {
                        Method getStringx = displayName.getClass().getMethod("getString");
                        Object strx = getStringx.invoke(displayName);
                        if (strx != null && !strx.toString().isEmpty()) {
                           return strx.toString();
                        }
                     } catch (Exception var10) {
                        String result = displayName.toString();
                        if (result != null && !result.isEmpty()) {
                           return result;
                        }
                     }
                  }
               } catch (Exception var11) {
               }

               try {
                  Method getNameMethod = template.getClass().getMethod("getName");
                  Object name = getNameMethod.invoke(template);
                  if (name != null && !name.toString().isEmpty()) {
                     return name.toString();
                  }
               } catch (Exception var8) {
               }
            }
         } catch (Exception var12) {
         }

         String result = ability.toString();
         return result != null && !result.isEmpty() && !result.contains("@") ? result : null;
      }
   }

   private Map<String, Integer> findStatsForPokemon(String searchName) {
      if (searchName != null && !searchName.isEmpty()) {
         Map<String, Integer> stats = BattleMessageSubscriber.getStatStagesForPokemon("player", searchName);
         if (stats != null && !stats.isEmpty()) {
            return stats;
         } else {
            stats = BattleMessageSubscriber.getStatStagesForPokemon(searchName);
            if (stats != null && !stats.isEmpty()) {
               return stats;
            } else {
               Map<String, Map<String, Integer>> allStats = BattleMessageSubscriber.getAllStatStages();
               String searchLower = searchName.toLowerCase();

               for (Entry<String, Map<String, Integer>> entry : allStats.entrySet()) {
                  String keyLower = entry.getKey().toLowerCase();
                  if (keyLower.equals(searchLower)) {
                     return entry.getValue();
                  }
               }

               if (searchName.length() >= 3) {
                  for (Entry<String, Map<String, Integer>> entryx : allStats.entrySet()) {
                     String keyLower = entryx.getKey().toLowerCase();
                     if (entryx.getKey().length() >= 3 && (keyLower.contains(searchLower) || searchLower.contains(keyLower))) {
                        return entryx.getValue();
                     }
                  }
               }

               return Collections.emptyMap();
            }
         }
      } else {
         return Collections.emptyMap();
      }
   }

   private static Pokemon getPlayerPokemon(ActiveClientBattlePokemon activePokemon, ClientBattlePokemon battlePokemon) {
      if (activePokemon != null) {
         try {
            Pokemon actorPokemon = BattlePokemonSpeedResolver.findBattleActorPokemon(activePokemon);
            if (actorPokemon != null) {
               return actorPokemon;
            }
         } catch (Throwable var4) {
         }
      }

      if (battlePokemon == null) {
         return null;
      } else {
         try {
            return LocalBattlePartyTracker.findLocalPartyPokemon(battlePokemon);
         } catch (Throwable var3) {
            return null;
         }
      }
   }

   private static int calcNonHPStat(int base, int iv, int ev, double natureMult, int level) {
      if (level <= 0) {
         return 1;
      } else {
         int raw = (int)Math.floor((double)((2 * base + iv + ev / 4) * level) / 100.0) + 5;
         return Math.max(1, (int)Math.floor((double)raw * natureMult));
      }
   }

   private static int calcHPStat(int base, int iv, int ev, int level) {
      return level <= 0 ? 1 : Math.max(1, (int)Math.floor((double)((2 * base + iv + ev / 4) * level) / 100.0) + level + 10);
   }

   private static double statStageMult(int stage) {
      return stage >= 0 ? (2.0 + (double)stage) / 2.0 : 2.0 / (2.0 - (double)stage);
   }

   private static int calcBaseDamage(int level, int power, int atk, int def) {
      if (def > 0 && power > 0 && atk > 0) {
         double step1 = Math.floor(2.0 * (double)level / 5.0 + 2.0);
         double step2 = Math.floor(step1 * (double)power * (double)atk / (double)def);
         return Math.max(1, (int)Math.floor(step2 / 50.0 + 2.0));
      } else {
         return 0;
      }
   }

   private static int getBaseStatValue(Object formObj, Object speciesObj, Stat stat) {
      if (formObj != null) {
         try {
            Object bs = formObj.getClass().getMethod("getBaseStats").invoke(formObj);
            if (bs instanceof Map) {
               Object v = ((Map)bs).get(stat);
               if (v instanceof Number && ((Number)v).intValue() > 0) {
                  return ((Number)v).intValue();
               }
            }
         } catch (Throwable var6) {
         }
      }

      try {
         Object bs = speciesObj.getClass().getMethod("getBaseStats").invoke(speciesObj);
         if (bs instanceof Map) {
            Object v = ((Map)bs).get(stat);
            if (v instanceof Number) {
               return ((Number)v).intValue();
            }
         }
      } catch (Throwable var5) {
      }

      return 0;
   }

   private class_5250 computeDamageRangeComponent(
      ActiveClientBattlePokemon activePlayerPokemon,
      ClientBattlePokemon playerPokemon,
      String playerPokemonName,
      Object speciesObj,
      Object matchedFormObj,
      String opponentName,
      MoveTemplate move,
      float typeEffectiveness,
      float stabMultiplier,
      ClientBattlePokemon opponentPokemon
   ) {
      try {
         if (playerPokemon != null && speciesObj != null) {
            double rawPower = move.getPower();
            if (rawPower <= 0.0) {
               return null;
            } else {
               Pokemon actualPokemon = getPlayerPokemon(activePlayerPokemon, playerPokemon);
               if (actualPokemon == null) {
                  return null;
               } else {
                  int attackerLevel = playerPokemon.getLevel();
                  if (attackerLevel <= 0) {
                     attackerLevel = actualPokemon.getLevel();
                  }

                  if (attackerLevel <= 0) {
                     return null;
                  } else {
                     int opponentLevel = attackerLevel;
                     if (opponentPokemon != null) {
                        try {
                           int directLevel = opponentPokemon.getLevel();
                           if (directLevel > 0) {
                              opponentLevel = directLevel;
                           }
                        } catch (Throwable var28) {
                        }
                     }

                     if (opponentLevel <= 0) {
                        opponentLevel = 1;
                     }

                     int baseDef = getBaseStatValue(matchedFormObj, speciesObj, Stats.DEFENCE);
                     int baseSpecialDef = getBaseStatValue(matchedFormObj, speciesObj, Stats.SPECIAL_DEFENCE);
                     int baseHP = getBaseStatValue(matchedFormObj, speciesObj, Stats.HP);
                     if (baseDef > 0 && baseSpecialDef > 0 && baseHP > 0) {
                        int attackStage = resolveTrackedStatStage(
                           "player",
                           "attack",
                           playerPokemonName,
                           battlePokemonDisplayName(playerPokemon),
                           battlePokemonSpeciesName(playerPokemon),
                           pokemonDisplayName(actualPokemon),
                           pokemonSpeciesName(actualPokemon)
                        );
                        int specialAttackStage = resolveTrackedStatStage(
                           "player",
                           "special-attack",
                           playerPokemonName,
                           battlePokemonDisplayName(playerPokemon),
                           battlePokemonSpeciesName(playerPokemon),
                           pokemonDisplayName(actualPokemon),
                           pokemonSpeciesName(actualPokemon)
                        );
                        int defenceStage = resolveTrackedStatStage(
                           "opponent", "defense", opponentName, battlePokemonDisplayName(opponentPokemon), battlePokemonSpeciesName(opponentPokemon)
                        );
                        int specialDefenceStage = resolveTrackedStatStage(
                           "opponent", "special-defense", opponentName, battlePokemonDisplayName(opponentPokemon), battlePokemonSpeciesName(opponentPokemon)
                        );
                        Map<Stat, Integer> megaBaseStats = resolveMegaBaseStats(actualPokemon, "player", playerPokemonName);
                        int atkStat = computeStatAtLevel(actualPokemon, Stats.ATTACK, attackerLevel, megaBaseStats);
                        int spaStat = computeStatAtLevel(actualPokemon, Stats.SPECIAL_ATTACK, attackerLevel, megaBaseStats);
                        int defStat = computeStatAtLevel(actualPokemon, Stats.DEFENCE, attackerLevel, megaBaseStats);
                        DamagePreview preview = MoveDamagePreviewCalculator.calculatePreview(
                           new AttackContext(
                              attackerLevel,
                              atkStat,
                              spaStat,
                              attackStage,
                              specialAttackStage,
                              getPokemonStatusId(actualPokemon),
                              isPokemonGrounded("player", playerPokemonName),
                              getHeldItemPath(actualPokemon),
                              getAbilityName(actualPokemon),
                              getHpRatio(actualPokemon),
                              defStat,
                              resolveTrackedStatStage(
                                 "player",
                                 "defense",
                                 playerPokemonName,
                                 battlePokemonDisplayName(playerPokemon),
                                 battlePokemonSpeciesName(playerPokemon),
                                 pokemonDisplayName(actualPokemon),
                                 pokemonSpeciesName(actualPokemon)
                              )
                           ),
                           new DefenceContext(
                              opponentLevel,
                              baseHP,
                              baseDef,
                              baseSpecialDef,
                              opponentPokemon != null ? opponentPokemon.getMaxHp() : 0.0F,
                              opponentPokemon != null && opponentPokemon.isHpFlat(),
                              defenceStage,
                              specialDefenceStage,
                              getBattlePokemonHpRatio(opponentPokemon),
                              isPokemonGrounded("opponent", opponentName),
                              isRockType(opponentPokemon, opponentName)
                           ),
                           move,
                           typeEffectiveness,
                           stabMultiplier
                        );
                        return MoveDamagePreviewCalculator.buildInlineComponent(preview);
                     } else {
                        return null;
                     }
                  }
               }
            }
         } else {
            return null;
         }
      } catch (Throwable var29) {
         return null;
      }
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

   private static int computeStatAtLevel(Pokemon pokemon, Stats stat, int level, Map<Stat, Integer> baseStatsOverride) {
      try {
         int base;
         if (baseStatsOverride != null) {
            Integer overrideVal = baseStatsOverride.get(stat);
            base = overrideVal != null ? overrideVal : 1;
         } else {
            Integer baseVal = (Integer)pokemon.getForm().getBaseStats().get(stat);
            base = baseVal != null ? baseVal : 1;
         }

         int iv = pokemon.getIvs().getOrDefault(stat);
         int ev = pokemon.getEvs().getOrDefault(stat);
         int innerValue = (2 * base + iv + ev / 4) * level / 100 + 5;
         return pokemon.getEffectiveNature().modifyStat(stat, innerValue);
      } catch (Throwable var9) {
         try {
            return pokemon.getStat(stat);
         } catch (Throwable var8) {
            return 1;
         }
      }
   }

   private static Map<Stat, Integer> resolveMegaBaseStats(Pokemon pokemon, String side, String pokemonName) {
      try {
         if (!BattleMessageSubscriber.isMegaEvolved(side, pokemonName)) {
            return null;
         } else {
            Species species = pokemon.getSpecies();
            List<FormData> forms = species.getForms();
            ArrayList<FormData> megaForms = new ArrayList<>();

            for (FormData form : forms) {
               String showdownId = form.formOnlyShowdownId();
               if (showdownId.contains("mega")) {
                  megaForms.add(form);
               }
            }

            if (megaForms.isEmpty()) {
               return null;
            } else if (megaForms.size() == 1) {
               return megaForms.get(0).getBaseStats();
            } else {
               List<String> megaTypes = BattleMessageSubscriber.getMegaTypes(side, pokemonName);
               if (megaTypes != null && !megaTypes.isEmpty()) {
                  for (FormData formx : megaForms) {
                     String formType1 = formx.getPrimaryType().getName().toLowerCase(Locale.ROOT);
                     String formType2 = formx.getSecondaryType() != null ? formx.getSecondaryType().getName().toLowerCase(Locale.ROOT) : null;
                     boolean match = true;

                     for (String megaType : megaTypes) {
                        String mt = megaType.toLowerCase(Locale.ROOT);
                        if (!mt.equals(formType1) && !mt.equals(formType2)) {
                           match = false;
                           break;
                        }
                     }

                     if (match) {
                        return formx.getBaseStats();
                     }
                  }
               }

               return megaForms.get(0).getBaseStats();
            }
         }
      } catch (Throwable var15) {
         return null;
      }
   }

   private static String getAbilityName(Pokemon pokemon) {
      try {
         return pokemon.getAbility().getName().toLowerCase(Locale.ROOT);
      } catch (Throwable var2) {
         return "";
      }
   }

   private static String getPokemonStatusId(Pokemon pokemon) {
      if (pokemon == null) {
         return null;
      } else {
         try {
            PersistentStatusContainer statusContainer = pokemon.getStatus();
            return statusContainer != null && statusContainer.getStatus() != null ? statusContainer.getStatus().getShowdownName() : null;
         } catch (Throwable var2) {
            return null;
         }
      }
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

   private static boolean isRockType(ClientBattlePokemon battlePokemon, String opponentName) {
      if (battlePokemon != null && opponentName != null) {
         try {
            List<ElementalType> displayTypes = TypeIconRenderer.getDisplayTypes(battlePokemon, opponentName, "opponent", null);
            if (displayTypes == null) {
               return false;
            }

            for (ElementalType type : displayTypes) {
               if (type != null && "rock".equalsIgnoreCase(type.getName())) {
                  return true;
               }
            }
         } catch (Throwable var5) {
         }

         return false;
      } else {
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

   private static String battlePokemonDisplayName(ClientBattlePokemon battlePokemon) {
      if (battlePokemon == null) {
         return null;
      } else {
         try {
            return battlePokemon.getDisplayName() != null ? battlePokemon.getDisplayName().getString() : null;
         } catch (Throwable var2) {
            return null;
         }
      }
   }

   private static String battlePokemonSpeciesName(ClientBattlePokemon battlePokemon) {
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

   private static String pokemonDisplayName(Pokemon pokemon) {
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

   private static String pokemonSpeciesName(Pokemon pokemon) {
      if (pokemon == null) {
         return null;
      } else {
         try {
            return pokemon.getSpecies() != null ? pokemon.getSpecies().getName() : null;
         } catch (Throwable var2) {
            return null;
         }
      }
   }
}
