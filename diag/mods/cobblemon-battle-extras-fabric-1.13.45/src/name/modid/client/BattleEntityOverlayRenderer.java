package name.modid.client;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import name.modid.CobblemonBattleInfoClient;
import name.modid.client.BattleEntityOverlayRenderer.BaseSpeedCacheData;
import name.modid.client.BattleEntityOverlayRenderer.DisplayTypesCacheData;
import name.modid.client.BattleEntityOverlayRenderer.MatchedBattlePokemon;
import name.modid.platform.Services;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2561;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_757;
import net.minecraft.class_7833;
import net.minecraft.class_898;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_327.class_6415;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BattleEntityOverlayRenderer {
   private static final Logger LOGGER = LoggerFactory.getLogger("BattleEntityOverlayRenderer");
   private static final class_2960 BATTLE_INFO_BASE = class_2960.method_60654("cobblemon:textures/gui/battle/battle_info_base.png");
   private static final class_2960 BATTLE_INFO_BASE_FLIPPED = class_2960.method_60654("cobblemon:textures/gui/battle/battle_info_base_flipped.png");
   private static final class_2960 ALTERNATIVE_BATTLE_INFO_BASE = class_2960.method_60654(
      "cobblemon-battle-extras:textures/gui/battle_info_base_alternative.png"
   );
   private static final class_2960 ALTERNATIVE_BATTLE_INFO_BASE_FLIPPED = class_2960.method_60654(
      "cobblemon-battle-extras:textures/gui/battle_info_base_alternative_flipped.png"
   );
   private static final class_2960 SIMPLIFIED_BATTLE_INFO_BASE = class_2960.method_60654("cobblemon-battle-extras:textures/gui/battle_info_healthbar.png");
   private static final class_2960 SIMPLIFIED_BATTLE_INFO_BASE_FLIPPED = class_2960.method_60654(
      "cobblemon-battle-extras:textures/gui/battle_info_healthbar_flipped.png"
   );
   private static final class_2960 TYPE_SPRITE_SHEET = class_2960.method_60654("cobblemon:textures/gui/types.png");
   private static final String MEGA_SHOWDOWN_TERA_PATH = "mega_showdown:textures/gui/summary/tera_types/";
   private static final int TERA_ICON_SIZE = 32;
   private static final boolean MEGA_SHOWDOWN_LOADED = Services.PLATFORM.isModLoaded("mega_showdown");
   private static final class_2960 MEGA_ICON = class_2960.method_60654("cobblemon-battle-extras:textures/gui/mega_icon.png");
   private static final int MEGA_ICON_SIZE = 64;
   private static final int MEGA_ICON_DISPLAY_SIZE = 6;
   private static float MEGA_ICON_OFFSET_X_PLAYER_OVERLAY = -27.0F;
   private static float MEGA_ICON_OFFSET_Y_PLAYER_OVERLAY = -16.0F;
   private static float MEGA_ICON_OFFSET_X_OPPONENT_OVERLAY = 27.0F;
   private static float MEGA_ICON_OFFSET_Y_OPPONENT_OVERLAY = -16.0F;
   private static float MEGA_ICON_OFFSET_X_PLAYER_OVERLAY_SINGLE = -12.0F;
   private static float MEGA_ICON_OFFSET_Y_PLAYER_OVERLAY_SINGLE = -17.0F;
   private static float MEGA_ICON_OFFSET_X_OPPONENT_OVERLAY_SINGLE = 98.0F;
   private static float MEGA_ICON_OFFSET_Y_OPPONENT_OVERLAY_SINGLE = -17.0F;
   private static float MEGA_ICON_OFFSET_X_PLAYER_ALT = -32.0F;
   private static float MEGA_ICON_OFFSET_Y_PLAYER_ALT = -21.0F;
   private static float MEGA_ICON_OFFSET_X_OPPONENT_ALT = -100.0F;
   private static float MEGA_ICON_OFFSET_Y_OPPONENT_ALT = -21.0F;
   private static float MEGA_ICON_OFFSET_X_OPPONENT_ALT_SINGLE = -114.0F;
   private static float MEGA_ICON_OFFSET_Y_OPPONENT_ALT_SINGLE = -21.0F;
   private static final int TEXTURE_WIDTH = 140;
   private static final int TEXTURE_HEIGHT = 40;
   private static final int TYPE_ICON_SIZE = 36;
   private static final int TYPE_SHEET_WIDTH = 648;
   private static final int TYPE_DISPLAY_SIZE = 12;
   private static final float SCALE = 0.012F;
   private static final float HEIGHT_ABOVE_BASE = 0.5F;
   private static final float HEIGHT_SCALE_MULTIPLIER = 0.15F;
   private static final int LIGHT = 15728880;
   private static final long OVERLAY_BATTLE_CACHE_TTL_MS = 120L;
   private static final long BASE_SPEED_CACHE_TTL_MS = 5000L;
   private static final long DISPLAY_TYPES_CACHE_TTL_MS = 120L;
   private static final Map<String, BattleEntityOverlayRenderer.OverlayBattleCacheData> overlayBattleCache = new ConcurrentHashMap<>();
   private static final Map<String, BaseSpeedCacheData> baseSpeedCache = new ConcurrentHashMap<>();
   private static final Map<String, class_2960> teraIconCache = new ConcurrentHashMap<>();
   private static final Map<String, DisplayTypesCacheData> displayTypesCache = new ConcurrentHashMap<>();
   private static final Map<Class<?>, Method> battlePokemonGenderMethodCache = new ConcurrentHashMap<>();
   private static final Map<Class<?>, Method> battlePokemonStatusMethodCache = new ConcurrentHashMap<>();
   private static final Map<Class<?>, Method> statusShowdownNameMethodCache = new ConcurrentHashMap<>();
   private static final Map<String, Method> zeroArgMethodCache = new ConcurrentHashMap<>();
   private static final Set<String> missingZeroArgMethods = ConcurrentHashMap.newKeySet();
   private static final float HP_BAR_X_START_PLAYER = 38.0F;
   private static final float HP_BAR_X_START_OPPONENT = 3.0F;
   private static final float HP_BAR_MAX_WIDTH_PLAYER = 99.0F;
   private static final float HP_BAR_MAX_WIDTH_OPPONENT = 99.0F;
   private static final float HP_BAR_Y = 24.0F;
   private static final float HP_BAR_HEIGHT = 4.0F;
   private static final float ALT_HP_BAR_X_START_PLAYER = 2.0F;
   private static final float ALT_HP_BAR_X_START_OPPONENT = 6.0F;
   private static final float ALT_HP_BAR_MAX_WIDTH_PLAYER = 132.0F;
   private static final float ALT_HP_BAR_MAX_WIDTH_OPPONENT = 132.0F;
   private static final float SIMPLIFIED_HP_BAR_X_START_PLAYER = 2.0F;
   private static final float SIMPLIFIED_HP_BAR_X_START_OPPONENT = 6.0F;
   private static final float SIMPLIFIED_HP_BAR_MAX_WIDTH_PLAYER = 132.0F;
   private static final float SIMPLIFIED_HP_BAR_MAX_WIDTH_OPPONENT = 132.0F;
   private static final float SIMPLIFIED_HP_BAR_Y = 24.0F;
   private static final float SIMPLIFIED_HP_BAR_HEIGHT = 4.0F;
   private static final float PORTRAIT_X = 3.0F;
   private static final float PORTRAIT_Y = 3.0F;
   private static final float PORTRAIT_SIZE = 32.0F;
   private static final float ALT_NAME_X_PLAYER = 6.0F;
   private static final float ALT_NAME_X_OPPONENT = 10.0F;
   private static final float ALT_NAME_Y = 12.0F;
   private static final float ALT_LEVEL_RIGHT_PADDING_PLAYER = 10.0F;
   private static final float ALT_LEVEL_RIGHT_PADDING_OPPONENT = 6.0F;
   private static final float ALT_GENDER_SPACING = 2.0F;
   private static final float ALT_TYPE_ICON_PADDING_X = 3.0F;
   private static final float ALT_TYPE_ICON_Y = -10.0F;

   public static void renderBattleOverlay(
      class_1297 entity, class_4587 poseStack, class_4597 buffers, class_4184 camera, float partialTicks, double x, double y, double z
   ) {
      if (entity != null && entity.getClass().getName().contains("PokemonEntity")) {
         if (BattleExtrasConfig.isBattleNameplatesEnabled()) {
            ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
            if (battle != null) {
               try {
                  Method getBattleIdMethod = getZeroArgMethodCached(entity.getClass(), "getBattleId");
                  if (getBattleIdMethod == null) {
                     return;
                  }

                  UUID pokemonBattleId = (UUID)getBattleIdMethod.invoke(entity);
                  UUID battleId = battle.getBattleId();
                  if (battleId == null || pokemonBattleId == null || !battleId.equals(pokemonBattleId)) {
                     return;
                  }

                  class_1297 cameraEntity = camera.method_19331();
                  if (cameraEntity == null) {
                     return;
                  }

                  double dx = entity.method_23317() - cameraEntity.method_23317();
                  double dy = entity.method_23318() - cameraEntity.method_23318();
                  double dz = entity.method_23321() - cameraEntity.method_23321();
                  double distSq = dx * dx + dy * dy + dz * dz;
                  if (distSq > 4096.0) {
                     return;
                  }

                  Method getPokemonMethod = getZeroArgMethodCached(entity.getClass(), "getPokemon");
                  if (getPokemonMethod == null) {
                     return;
                  }

                  Object pokemonData = getPokemonMethod.invoke(entity);
                  if (pokemonData == null) {
                     return;
                  }

                  Method getSpeciesMethod = getZeroArgMethodCached(pokemonData.getClass(), "getSpecies");
                  if (getSpeciesMethod == null) {
                     return;
                  }

                  Object species = getSpeciesMethod.invoke(pokemonData);
                  if (species == null) {
                     return;
                  }

                  Method getNameMethod = getZeroArgMethodCached(species.getClass(), "getName");
                  if (getNameMethod == null) {
                     return;
                  }

                  String pokemonName = (String)getNameMethod.invoke(species);
                  String displayName = pokemonName;

                  try {
                     Method getTranslatedNameMethod = getZeroArgMethodCached(species.getClass(), "getTranslatedName");
                     Object nameComponent = getTranslatedNameMethod != null ? getTranslatedNameMethod.invoke(species) : null;
                     if (nameComponent != null) {
                        Method getStringMethod = getZeroArgMethodCached(nameComponent.getClass(), "getString");
                        if (getStringMethod != null) {
                           displayName = getStringMethod.invoke(nameComponent).toString();
                        }
                     }
                  } catch (Exception var55) {
                  }

                  Method getLevelMethod = getZeroArgMethodCached(pokemonData.getClass(), "getLevel");
                  if (getLevelMethod == null) {
                     return;
                  }

                  int level = (Integer)getLevelMethod.invoke(pokemonData);
                  class_310 mc = class_310.method_1551();
                  UUID pokemonUUID = null;

                  try {
                     Method getUuidMethod = getZeroArgMethodCached(pokemonData.getClass(), "getUuid");
                     if (getUuidMethod != null) {
                        pokemonUUID = (UUID)getUuidMethod.invoke(pokemonData);
                     }
                  } catch (Exception var54) {
                  }

                  MatchedBattlePokemon matchedBattlePokemon = findMatchedBattlePokemon(battle, pokemonUUID, pokemonName);
                  String currentFormNameLower = getCurrentFormNameLower(pokemonData);
                  BattleEntityOverlayRenderer.OverlayBattleCacheData overlayData = getOverlayBattleDataCached(
                     battle, battleId, pokemonUUID, pokemonName, currentFormNameLower
                  );
                  boolean isPlayerPokemon = overlayData.playerPokemon;
                  String side = isPlayerPokemon ? "player" : "opponent";
                  DisplayTypesCacheData displayTypeData = getDisplayTypesCached(battleId, pokemonUUID, pokemonName, side, pokemonData, species);
                  List<ElementalType> types = displayTypeData.types;
                  boolean isTerastallized = displayTypeData.terastallized;
                  float healthPercent = overlayData.healthPercent;
                  String gender = overlayData.gender;
                  String status = overlayData.status;
                  boolean isMegaEvolved = overlayData.megaEvolved;
                  int baseSpeed = getBaseSpeedCached(pokemonData, pokemonUUID, pokemonName, currentFormNameLower);
                  Integer exactPlayerSpeed = isPlayerPokemon ? resolveExactPlayerSpeed(pokemonData, matchedBattlePokemon, pokemonUUID) : null;
                  float entityHeight = (float)entity.method_5829().method_17940();
                  float heightOffset = 0.5F + entityHeight * 0.15F;
                  poseStack.method_22903();
                  poseStack.method_22904(x, y + (double)entityHeight + (double)heightOffset, z);
                  poseStack.method_22907(camera.method_23767());
                  poseStack.method_22907(class_7833.field_40716.rotationDegrees(180.0F));
                  poseStack.method_22905(-0.012F, -0.012F, 0.012F);
                  renderBattleInfoPlate(poseStack, isPlayerPokemon);
                  int nameplateStyle = BattleExtrasConfig.getNameplateStyle();
                  boolean useAltPositioning = nameplateStyle != 0;
                  if (nameplateStyle == 0) {
                     renderPortrait(poseStack, buffers, entity, isPlayerPokemon, partialTicks, camera);
                  }

                  renderHPBar(poseStack, healthPercent, isPlayerPokemon, nameplateStyle);
                  class_4598 textBuffer = mc.method_22940().method_23000();
                  renderNameAndLevel(poseStack, mc.field_1772, displayName, level, isPlayerPokemon, gender, nameplateStyle, textBuffer);
                  renderSpeedRange(
                     poseStack,
                     mc.field_1772,
                     baseSpeed,
                     level,
                     isPlayerPokemon,
                     pokemonName,
                     displayName,
                     exactPlayerSpeed,
                     matchedBattlePokemon,
                     nameplateStyle,
                     textBuffer
                  );
                  if (status != null) {
                     renderStatus(poseStack, mc.field_1772, status, isPlayerPokemon, nameplateStyle, textBuffer);
                  }

                  textBuffer.method_22993();
                  renderTypeIcons(poseStack, types, isPlayerPokemon, isTerastallized, nameplateStyle, matchedBattlePokemon);
                  if (isMegaEvolved && types != null && !types.isEmpty()) {
                     renderMegaIcon(poseStack, isPlayerPokemon, types.size(), useAltPositioning);
                  }

                  poseStack.method_22909();
               } catch (Exception var56) {
               }
            }
         }
      }
   }

   private static void renderBattleInfoPlate(class_4587 poseStack, boolean isPlayer) {
      RenderSystem.enableDepthTest();
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA, class_4535.ONE, class_4534.ONE_MINUS_SRC_ALPHA);
      float alpha = BattleExtrasConfig.getBattleNameplateOpacity();
      float brightness = BattleExtrasConfig.getBattleNameplateBrightness();
      RenderSystem.setShaderColor(brightness, brightness, brightness, alpha);
      int nameplateStyle = BattleExtrasConfig.getNameplateStyle();
      class_2960 plateTexture;
      if (nameplateStyle == 2) {
         plateTexture = isPlayer ? SIMPLIFIED_BATTLE_INFO_BASE : SIMPLIFIED_BATTLE_INFO_BASE_FLIPPED;
      } else if (nameplateStyle == 1) {
         plateTexture = isPlayer ? ALTERNATIVE_BATTLE_INFO_BASE : ALTERNATIVE_BATTLE_INFO_BASE_FLIPPED;
      } else {
         plateTexture = isPlayer ? BATTLE_INFO_BASE : BATTLE_INFO_BASE_FLIPPED;
      }

      RenderSystem.setShaderTexture(0, plateTexture);
      RenderSystem.setShader(class_757::method_34542);
      Matrix4f matrix = poseStack.method_23760().method_23761();
      float halfWidth = 70.0F;
      float halfHeight = 20.0F;
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1585);
      buffer.method_22918(matrix, -halfWidth, -halfHeight, 0.0F).method_22913(0.0F, 0.0F);
      buffer.method_22918(matrix, -halfWidth, halfHeight, 0.0F).method_22913(0.0F, 1.0F);
      buffer.method_22918(matrix, halfWidth, halfHeight, 0.0F).method_22913(1.0F, 1.0F);
      buffer.method_22918(matrix, halfWidth, -halfHeight, 0.0F).method_22913(1.0F, 0.0F);
      class_286.method_43433(buffer.method_60800());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
   }

   private static void renderPortrait(class_4587 poseStack, class_4597 buffers, class_1297 entity, boolean isPlayer, float partialTicks, class_4184 camera) {
      class_310 mc = class_310.method_1551();
      float portCenterX;
      if (isPlayer) {
         portCenterX = -51.0F;
      } else {
         portCenterX = 51.0F;
      }

      float portCenterY = -1.0F;
      float entityHeight = (float)entity.method_5829().method_17940();
      float entityWidth = (float)entity.method_5829().method_17939();
      float maxDimension = Math.max(entityHeight, entityWidth);
      float scale = 16.8F / maxDimension;
      float savedYRot = entity.method_36454();
      float savedYRotO = entity.field_5982;
      float savedXRot = entity.method_36455();
      float savedXRotO = entity.field_6004;
      float savedYHeadRot = 0.0F;
      float savedYHeadRotO = 0.0F;
      float savedYBodyRot = 0.0F;
      float savedYBodyRotO = 0.0F;
      if (entity instanceof class_1309 living) {
         savedYHeadRot = living.field_6241;
         savedYHeadRotO = living.field_6259;
         savedYBodyRot = living.field_6283;
         savedYBodyRotO = living.field_6220;
      }

      entity.method_36456(0.0F);
      entity.field_5982 = 0.0F;
      entity.method_36457(0.0F);
      entity.field_6004 = 0.0F;
      if (entity instanceof class_1309 living) {
         living.field_6241 = 0.0F;
         living.field_6259 = 0.0F;
         living.field_6283 = 0.0F;
         living.field_6220 = 0.0F;
      }

      poseStack.method_22903();
      poseStack.method_46416(portCenterX, portCenterY + 9.6F, 0.01F);
      poseStack.method_22907(class_7833.field_40714.rotationDegrees(180.0F));
      poseStack.method_22905(scale, scale, scale);

      try {
         class_898 dispatcher = mc.method_1561();
         dispatcher.method_3948(false);
         dispatcher.method_3954(entity, 0.0, 0.0, 0.0, 180.0F, 0.0F, poseStack, buffers, 15728880);
         dispatcher.method_3948(true);
      } catch (Exception var22) {
      }

      poseStack.method_22909();
      entity.method_36456(savedYRot);
      entity.field_5982 = savedYRotO;
      entity.method_36457(savedXRot);
      entity.field_6004 = savedXRotO;
      if (entity instanceof class_1309 living) {
         living.field_6241 = savedYHeadRot;
         living.field_6259 = savedYHeadRotO;
         living.field_6283 = savedYBodyRot;
         living.field_6220 = savedYBodyRotO;
      }
   }

   private static void renderHPBar(class_4587 poseStack, float healthPercent, boolean isPlayer, int nameplateStyle) {
      healthPercent = Math.max(0.0F, Math.min(1.0F, healthPercent));
      RenderSystem.setShader(class_757::method_34540);
      Matrix4f matrix = poseStack.method_23760().method_23761();
      float hpBarHeight = 4.0F;
      float xStartPlayer;
      float xStartOpponent;
      float maxWidthPlayer;
      float maxWidthOpponent;
      float hpBarY;
      if (nameplateStyle == 2) {
         xStartPlayer = 2.0F;
         xStartOpponent = 6.0F;
         maxWidthPlayer = 132.0F;
         maxWidthOpponent = 132.0F;
         hpBarY = 24.0F;
         hpBarHeight = 4.0F;
      } else if (nameplateStyle == 1) {
         xStartPlayer = 2.0F;
         xStartOpponent = 6.0F;
         maxWidthPlayer = 132.0F;
         maxWidthOpponent = 132.0F;
         hpBarY = 24.0F;
      } else {
         xStartPlayer = 38.0F;
         xStartOpponent = 3.0F;
         maxWidthPlayer = 99.0F;
         maxWidthOpponent = 99.0F;
         hpBarY = 24.0F;
      }

      float barLeft;
      float barRight;
      float barTop;
      if (isPlayer) {
         barLeft = -70.0F + xStartPlayer;
         barTop = -20.0F + hpBarY - 2.0F;
         barRight = barLeft + maxWidthPlayer * healthPercent;
      } else {
         barRight = -70.0F + xStartOpponent + maxWidthOpponent;
         barTop = -20.0F + hpBarY - 2.0F;
         barLeft = barRight - maxWidthOpponent * healthPercent;
      }

      int r;
      int g;
      int b;
      if (healthPercent > 0.5F) {
         r = 100;
         g = 220;
         b = 100;
      } else if (healthPercent > 0.2F) {
         r = 255;
         g = 200;
         b = 50;
      } else {
         r = 230;
         g = 70;
         b = 70;
      }

      float brightness = BattleExtrasConfig.getBattleNameplateBrightness();
      r = (int)Math.min(255.0F, (float)r * brightness);
      g = (int)Math.min(255.0F, (float)g * brightness);
      b = (int)Math.min(255.0F, (float)b * brightness);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(class_757::method_34540);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableDepthTest();
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);
      buffer.method_22918(matrix, barLeft, barTop, 0.01F).method_1336(r, g, b, 255);
      buffer.method_22918(matrix, barLeft, barTop + hpBarHeight, 0.01F).method_1336(r, g, b, 255);
      buffer.method_22918(matrix, barRight, barTop + hpBarHeight, 0.01F).method_1336(r, g, b, 255);
      buffer.method_22918(matrix, barRight, barTop, 0.01F).method_1336(r, g, b, 255);
      class_286.method_43433(buffer.method_60800());
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
   }

   private static void renderNameAndLevel(
      class_4587 poseStack, class_327 font, String name, int level, boolean isPlayer, String gender, int nameplateStyle, class_4598 textBuffer
   ) {
      float textZOffset = 0.1F;
      boolean simplifiedStyle = nameplateStyle == 2;
      float nameX;
      float levelX;
      float genderX;
      float nameY;
      if (nameplateStyle != 2 && nameplateStyle != 1) {
         nameY = -8.0F;
         if (isPlayer) {
            nameX = -30.0F;
            int nameWidth = font.method_1727(name);
            genderX = nameX + (float)nameWidth * 0.8F + 2.0F;
            String levelText = "Lv." + level;
            int levelWidth = font.method_1727(levelText);
            levelX = 60.0F - (float)levelWidth * 0.7F;
         } else {
            nameX = -60.0F;
            int nameWidth = font.method_1727(name);
            genderX = nameX + (float)nameWidth * 0.8F + 2.0F;
            String levelText = "Lv." + level;
            int levelWidth = font.method_1727(levelText);
            levelX = 25.0F - (float)levelWidth * 0.7F;
         }
      } else {
         nameY = -8.0F;
         if (isPlayer) {
            nameX = -64.0F;
            int nameWidth = font.method_1727(name);
            genderX = nameX + (float)nameWidth * 0.8F + 2.0F;
            String levelText = "Lv." + level;
            int levelWidth = font.method_1727(levelText);
            levelX = 60.0F - (float)levelWidth * 0.7F;
         } else {
            nameX = -60.0F;
            int nameWidth = font.method_1727(name);
            genderX = nameX + (float)nameWidth * 0.8F + 2.0F;
            String levelText = "Lv." + level;
            int levelWidth = font.method_1727(levelText);
            levelX = 64.0F - (float)levelWidth * 0.7F;
         }
      }

      drawNameplateText(poseStack, font, name, nameX, nameY, textZOffset, 0.8F, -1, simplifiedStyle, textBuffer);
      if (gender != null && !gender.isEmpty()) {
         String genderSymbol;
         int genderColor;
         if (gender.equalsIgnoreCase("MALE")) {
            genderSymbol = "♂";
            genderColor = -9924368;
         } else if (gender.equalsIgnoreCase("FEMALE")) {
            genderSymbol = "♀";
            genderColor = -1140308;
         } else {
            genderSymbol = null;
            genderColor = 0;
         }

         if (genderSymbol != null) {
            drawNameplateText(poseStack, font, genderSymbol, genderX, nameY, textZOffset, 0.8F, genderColor, simplifiedStyle, textBuffer);
         }
      }

      String levelText = "Lv." + level;
      drawNameplateText(poseStack, font, levelText, levelX, nameY, textZOffset, 0.7F, -11141291, simplifiedStyle, textBuffer);
   }

   private static void drawNameplateText(
      class_4587 poseStack, class_327 font, String text, float x, float y, float z, float scale, int textColor, boolean outlined, class_4598 textBuffer
   ) {
      if (text != null && !text.isEmpty()) {
         if (outlined) {
            drawOutlinedNameplateText(poseStack, font, text, x, y, z, scale, textColor, textBuffer);
         } else {
            drawNameplateTextLayer(poseStack, font, text, x, y, z, scale, textColor, false, textBuffer);
         }
      }
   }

   private static void drawOutlinedNameplateText(
      class_4587 poseStack, class_327 font, String text, float x, float y, float z, float scale, int textColor, class_4598 textBuffer
   ) {
      poseStack.method_22903();
      poseStack.method_46416(x, y, z);
      poseStack.method_22905(scale, scale, scale);
      font.method_37296(
         class_2561.method_43470(text).method_30937(), 0.0F, 0.0F, textColor, -15724528, poseStack.method_23760().method_23761(), textBuffer, 15728880
      );
      poseStack.method_22909();
   }

   private static void drawNameplateTextLayer(
      class_4587 poseStack, class_327 font, String text, float x, float y, float z, float scale, int textColor, boolean shadow, class_4598 textBuffer
   ) {
      poseStack.method_22903();
      poseStack.method_46416(x, y, z);
      poseStack.method_22905(scale, scale, scale);
      font.method_27521(text, 0.0F, 0.0F, textColor, shadow, poseStack.method_23760().method_23761(), textBuffer, class_6415.field_33994, 0, 15728880);
      poseStack.method_22909();
   }

   private static void renderTypeIcons(
      class_4587 poseStack, List<ElementalType> types, boolean isPlayer, boolean isTerastallized, int nameplateStyle, MatchedBattlePokemon matchedBattlePokemon
   ) {
      if (types != null && !types.isEmpty()) {
         RenderSystem.enableDepthTest();
         RenderSystem.setShader(class_757::method_34542);
         RenderSystem.enableBlend();
         float brightness = BattleExtrasConfig.getBattleNameplateBrightness();
         RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0F);
         Matrix4f matrix = poseStack.method_23760().method_23761();
         int iconDisplaySize = 12;
         int iconSpacing = 2;
         int activePokemonCount = 1;
         if (matchedBattlePokemon != null && matchedBattlePokemon.activePokemon != null && matchedBattlePokemon.activePokemon.getActor() != null) {
            activePokemonCount = Math.max(1, matchedBattlePokemon.activePokemon.getActor().getActivePokemon().size());
         }

         boolean compactMultiLayout = activePokemonCount > 1;
         if (compactMultiLayout) {
            iconDisplaySize = 10;
            iconSpacing = 1;
         }

         float totalWidth = (float)(types.size() * iconDisplaySize + (types.size() - 1) * iconSpacing);
         float startX;
         float y;
         if (compactMultiLayout) {
            float hpBarHeight = 4.0F;
            float xStartPlayer;
            float xStartOpponent;
            float maxWidthPlayer;
            float maxWidthOpponent;
            float hpBarY;
            if (nameplateStyle == 2) {
               xStartPlayer = 2.0F;
               xStartOpponent = 6.0F;
               maxWidthPlayer = 132.0F;
               maxWidthOpponent = 132.0F;
               hpBarY = 24.0F;
               hpBarHeight = 4.0F;
            } else if (nameplateStyle == 1) {
               xStartPlayer = 2.0F;
               xStartOpponent = 6.0F;
               maxWidthPlayer = 132.0F;
               maxWidthOpponent = 132.0F;
               hpBarY = 24.0F;
            } else {
               xStartPlayer = 38.0F;
               xStartOpponent = 3.0F;
               maxWidthPlayer = 99.0F;
               maxWidthOpponent = 99.0F;
               hpBarY = 24.0F;
            }

            float barLeft = -70.0F + (isPlayer ? xStartPlayer : xStartOpponent);
            float barRight = barLeft + (isPlayer ? maxWidthPlayer : maxWidthOpponent);
            float barTop = -20.0F + hpBarY - 2.0F;
            float barBottom = barTop + hpBarHeight;
            y = barBottom + 2.0F;
            if (isPlayer) {
               startX = barLeft + 2.0F;
            } else {
               startX = barRight - totalWidth - 2.0F;
            }
         } else if (nameplateStyle != 2 && nameplateStyle != 1) {
            float paddingX = 3.0F;
            y = 23.0F;
            if (isPlayer) {
               startX = -70.0F + paddingX;
            } else {
               startX = 70.0F - totalWidth - paddingX;
            }
         } else {
            float paddingX = 3.0F;
            y = 10.0F;
            if (isPlayer) {
               startX = -70.0F + paddingX;
            } else {
               startX = 70.0F - totalWidth - paddingX;
            }
         }

         if (isTerastallized && MEGA_SHOWDOWN_LOADED) {
            float offsetX = 0.0F;

            for (ElementalType type : types) {
               float iconX = startX + offsetX;
               class_2960 teraIcon = getTeraIcon(type.getName());
               RenderSystem.setShaderTexture(0, teraIcon);
               class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1585);
               buffer.method_22918(matrix, iconX, y, 0.001F).method_22913(0.0F, 0.0F);
               buffer.method_22918(matrix, iconX, y + (float)iconDisplaySize, 0.001F).method_22913(0.0F, 1.0F);
               buffer.method_22918(matrix, iconX + (float)iconDisplaySize, y + (float)iconDisplaySize, 0.001F).method_22913(1.0F, 1.0F);
               buffer.method_22918(matrix, iconX + (float)iconDisplaySize, y, 0.001F).method_22913(1.0F, 0.0F);
               class_286.method_43433(buffer.method_60800());
               offsetX += (float)(iconDisplaySize + iconSpacing);
            }
         } else {
            RenderSystem.setShaderTexture(0, TYPE_SPRITE_SHEET);
            class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1585);
            float offsetX = 0.0F;

            for (ElementalType type : types) {
               float iconX = startX + offsetX;
               int textureX = type.getTextureXMultiplier() * 36;
               float u0 = (float)textureX / 648.0F;
               float u1 = (float)(textureX + 36) / 648.0F;
               buffer.method_22918(matrix, iconX, y, 0.001F).method_22913(u0, 0.0F);
               buffer.method_22918(matrix, iconX, y + (float)iconDisplaySize, 0.001F).method_22913(u0, 1.0F);
               buffer.method_22918(matrix, iconX + (float)iconDisplaySize, y + (float)iconDisplaySize, 0.001F).method_22913(u1, 1.0F);
               buffer.method_22918(matrix, iconX + (float)iconDisplaySize, y, 0.001F).method_22913(u1, 0.0F);
               offsetX += (float)(iconDisplaySize + iconSpacing);
            }

            class_286.method_43433(buffer.method_60800());
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private static List<ElementalType> getTypesFromSpecies(Object species) {
      List<ElementalType> types = new ArrayList<>();

      try {
         Method getTypesMethod = getZeroArgMethodCached(species.getClass(), "getTypes");
         if (getTypesMethod == null) {
            return types;
         }

         for (ElementalType type : (Iterable)getTypesMethod.invoke(species)) {
            types.add(type);
         }
      } catch (Exception var6) {
      }

      return types;
   }

   private static List<ElementalType> getTypesFromPokemon(Object pokemonData) {
      List<ElementalType> types = new ArrayList<>();

      try {
         Method getFormMethod = getZeroArgMethodCached(pokemonData.getClass(), "getForm");
         if (getFormMethod == null) {
            return types;
         }

         Object form = getFormMethod.invoke(pokemonData);
         if (form != null) {
            Method getTypesMethod = getZeroArgMethodCached(form.getClass(), "getTypes");
            if (getTypesMethod == null) {
               return types;
            }

            Object typesObj = getTypesMethod.invoke(form);
            if (typesObj instanceof Iterable) {
               for (Object type : (Iterable)typesObj) {
                  if (type instanceof ElementalType) {
                     types.add((ElementalType)type);
                  }
               }
            }

            if (!types.isEmpty()) {
               CobblemonBattleInfoClient.debug("[BattleEntityOverlayRenderer] Got {} types from Pokemon.getForm().getTypes()", new Object[]{types.size()});
            }
         }
      } catch (Exception var8) {
         CobblemonBattleInfoClient.debug("[BattleEntityOverlayRenderer] Failed to get form types from Pokemon: {}", new Object[]{var8.getMessage()});
      }

      return types;
   }

   private static String buildDisplayTypesCacheKey(UUID battleId, UUID pokemonUUID, String pokemonName, String side) {
      String base = buildOverlayCacheKey(battleId, pokemonUUID, pokemonName);
      return base + "|side:" + side;
   }

   private static DisplayTypesCacheData getDisplayTypesCached(
      UUID battleId, UUID pokemonUUID, String pokemonName, String side, Object pokemonData, Object species
   ) {
      String cacheKey = buildDisplayTypesCacheKey(battleId, pokemonUUID, pokemonName, side);
      long now = System.currentTimeMillis();
      DisplayTypesCacheData cached = displayTypesCache.get(cacheKey);
      if (cached != null && now <= cached.expiresAt) {
         return cached;
      } else {
         boolean terastallized = BattleMessageSubscriber.isTerastallized(pokemonName);
         List<ElementalType> types = TypeIconRenderer.getDisplayTypes(pokemonData, pokemonName, side, pokemonUUID != null ? pokemonUUID.toString() : null);
         if (types == null || types.isEmpty()) {
            types = getTypesFromPokemon(pokemonData);
         }

         if (types == null || types.isEmpty()) {
            types = getTypesFromSpecies(species);
         }

         List<ElementalType> safeTypes = types == null ? List.of() : List.copyOf(types);
         DisplayTypesCacheData data = new DisplayTypesCacheData(now + 120L, safeTypes, terastallized);
         displayTypesCache.put(cacheKey, data);
         if (displayTypesCache.size() > 256) {
            displayTypesCache.entrySet().removeIf(entry -> now > entry.getValue().expiresAt);
            if (displayTypesCache.size() > 384) {
               displayTypesCache.clear();
            }
         }

         return data;
      }
   }

   private static Method getZeroArgMethodCached(Class<?> clazz, String methodName) {
      String key = clazz.getName() + "#" + methodName;
      Method method = zeroArgMethodCache.get(key);
      if (method != null) {
         return method;
      } else if (missingZeroArgMethods.contains(key)) {
         return null;
      } else {
         try {
            Method discovered = clazz.getMethod(methodName);
            zeroArgMethodCache.put(key, discovered);
            return discovered;
         } catch (Exception var5) {
            missingZeroArgMethods.add(key);
            return null;
         }
      }
   }

   private static String buildOverlayCacheKey(UUID battleId, UUID pokemonUUID, String pokemonName) {
      String battleKey = battleId != null ? battleId.toString() : "no-battle";
      return pokemonUUID != null ? battleKey + "|uuid:" + pokemonUUID : battleKey + "|name:" + (pokemonName != null ? pokemonName.toLowerCase() : "unknown");
   }

   private static BattleEntityOverlayRenderer.OverlayBattleCacheData getOverlayBattleDataCached(
      ClientBattle battle, UUID battleId, UUID pokemonUUID, String pokemonName, String currentFormNameLower
   ) {
      String cacheKey = buildOverlayCacheKey(battleId, pokemonUUID, pokemonName);
      long now = System.currentTimeMillis();
      BattleEntityOverlayRenderer.OverlayBattleCacheData cached = overlayBattleCache.get(cacheKey);
      if (cached != null && now <= cached.expiresAt) {
         return cached;
      } else {
         MatchedBattlePokemon matched = findMatchedBattlePokemon(battle, pokemonUUID, pokemonName);
         float healthPercent = 1.0F;
         String gender = null;
         String status = null;
         boolean isPlayerPokemon;
         if (matched != null && matched.battlePokemon != null) {
            isPlayerPokemon = matched.playerSide;
            healthPercent = computeHealthPercent(matched.battlePokemon, matched.playerSide);
            gender = getGenderFromBattlePokemon(matched.battlePokemon);
            status = getStatusFromBattlePokemon(matched.battlePokemon);
         } else {
            isPlayerPokemon = isPlayerSidePokemon(battle, pokemonName);
         }

         String lowerPokemonName = pokemonName != null ? pokemonName.toLowerCase() : null;
         boolean megaEvolved = BattleMessageSubscriber.isMegaEvolved(lowerPokemonName);
         if (!megaEvolved && currentFormNameLower != null && currentFormNameLower.contains("mega")) {
            megaEvolved = true;
         }

         BattleEntityOverlayRenderer.OverlayBattleCacheData data = new BattleEntityOverlayRenderer.OverlayBattleCacheData(
            now + 120L, isPlayerPokemon, healthPercent, gender, status, megaEvolved
         );
         overlayBattleCache.put(cacheKey, data);
         if (overlayBattleCache.size() > 256) {
            overlayBattleCache.entrySet().removeIf(entry -> now > entry.getValue().expiresAt);
            if (overlayBattleCache.size() > 384) {
               overlayBattleCache.clear();
            }
         }

         return data;
      }
   }

   private static MatchedBattlePokemon findMatchedBattlePokemon(ClientBattle battle, UUID pokemonUUID, String pokemonName) {
      ClientBattleSide playerSide = BattleSideHelper.getPlayerSide(battle);
      ClientBattleSide opponentSide = BattleSideHelper.getOpponentSide(battle);
      String loweredName = pokemonName != null ? pokemonName.toLowerCase() : null;

      for (ClientBattleActor actor : playerSide.getActors()) {
         for (ActiveClientBattlePokemon active : actor.getActivePokemon()) {
            ClientBattlePokemon bp = active.getBattlePokemon();
            if (bp != null) {
               UUID battlePokemonUUID = bp.getUuid();
               if (pokemonUUID != null && battlePokemonUUID != null && battlePokemonUUID.equals(pokemonUUID)) {
                  return new MatchedBattlePokemon(bp, active, true);
               }
            }
         }
      }

      for (ClientBattleActor actor : opponentSide.getActors()) {
         for (ActiveClientBattlePokemon activex : actor.getActivePokemon()) {
            ClientBattlePokemon bp = activex.getBattlePokemon();
            if (bp != null) {
               UUID battlePokemonUUID = bp.getUuid();
               if (pokemonUUID != null && battlePokemonUUID != null && battlePokemonUUID.equals(pokemonUUID)) {
                  return new MatchedBattlePokemon(bp, activex, false);
               }
            }
         }
      }

      if (loweredName == null) {
         return null;
      } else {
         for (ClientBattleActor actor : playerSide.getActors()) {
            for (ActiveClientBattlePokemon activexx : actor.getActivePokemon()) {
               ClientBattlePokemon bp = activexx.getBattlePokemon();
               if (bp != null && bp.getSpecies().getName().equalsIgnoreCase(loweredName)) {
                  return new MatchedBattlePokemon(bp, activexx, true);
               }
            }
         }

         for (ClientBattleActor actor : opponentSide.getActors()) {
            for (ActiveClientBattlePokemon activexxx : actor.getActivePokemon()) {
               ClientBattlePokemon bp = activexxx.getBattlePokemon();
               if (bp != null && bp.getSpecies().getName().equalsIgnoreCase(loweredName)) {
                  return new MatchedBattlePokemon(bp, activexxx, false);
               }
            }
         }

         return null;
      }
   }

   private static float computeHealthPercent(ClientBattlePokemon bp, boolean isPlayerPokemon) {
      float hpValue = bp.getHpValue();
      float maxHp = bp.getMaxHp();
      if (!isPlayerPokemon && hpValue <= 1.0F && maxHp > 1.0F) {
         return hpValue;
      } else {
         return maxHp > 0.0F ? hpValue / maxHp : 1.0F;
      }
   }

   private static String getGenderFromBattlePokemon(ClientBattlePokemon bp) {
      if (bp == null) {
         return null;
      } else {
         try {
            Method method = battlePokemonGenderMethodCache.computeIfAbsent(bp.getClass(), clazz -> {
               try {
                  return clazz.getMethod("getGender");
               } catch (Exception var2x) {
                  return null;
               }
            });
            if (method == null) {
               return null;
            } else {
               Object genderObj = method.invoke(bp);
               return genderObj != null ? genderObj.toString() : null;
            }
         } catch (Exception var3) {
            return null;
         }
      }
   }

   private static String getStatusFromBattlePokemon(ClientBattlePokemon bp) {
      if (bp == null) {
         return null;
      } else {
         try {
            Method getStatus = battlePokemonStatusMethodCache.computeIfAbsent(bp.getClass(), clazz -> {
               try {
                  return clazz.getMethod("getStatus");
               } catch (Exception var2x) {
                  return null;
               }
            });
            if (getStatus == null) {
               return null;
            } else {
               Object statusObj = getStatus.invoke(bp);
               if (statusObj == null) {
                  return null;
               } else {
                  Method getShowdownName = statusShowdownNameMethodCache.computeIfAbsent(statusObj.getClass(), clazz -> {
                     try {
                        return clazz.getMethod("getShowdownName");
                     } catch (Exception var2x) {
                        return null;
                     }
                  });
                  if (getShowdownName == null) {
                     return null;
                  } else {
                     Object showdownName = getShowdownName.invoke(statusObj);
                     return showdownName != null ? showdownName.toString() : null;
                  }
               }
            }
         } catch (Exception var5) {
            return null;
         }
      }
   }

   private static String getCurrentFormNameLower(Object pokemonData) {
      if (pokemonData == null) {
         return null;
      } else {
         try {
            Method getFormMethod = getZeroArgMethodCached(pokemonData.getClass(), "getForm");
            if (getFormMethod == null) {
               return null;
            } else {
               Object form = getFormMethod.invoke(pokemonData);
               if (form == null) {
                  return null;
               } else {
                  Method formNameMethod = getZeroArgMethodCached(form.getClass(), "getName");
                  if (formNameMethod == null) {
                     return null;
                  } else {
                     Object formName = formNameMethod.invoke(form);
                     return formName != null ? formName.toString().toLowerCase() : null;
                  }
               }
            }
         } catch (Exception var5) {
            return null;
         }
      }
   }

   private static int getBaseSpeedCached(Object pokemonData, UUID pokemonUUID, String pokemonName, String formNameLower) {
      if (pokemonData == null) {
         return 0;
      } else {
         String key = pokemonUUID != null ? "uuid:" + pokemonUUID : "name:" + (pokemonName != null ? pokemonName.toLowerCase() : "unknown");
         long now = System.currentTimeMillis();
         BaseSpeedCacheData cached = baseSpeedCache.get(key);
         if (cached == null
            || now > cached.expiresAt
            || (cached.formNameLower != null || formNameLower != null) && (cached.formNameLower == null || !cached.formNameLower.equals(formNameLower))) {
            int baseSpeed = extractBaseSpeedFromPokemonData(pokemonData);
            baseSpeedCache.put(key, new BaseSpeedCacheData(now + 5000L, formNameLower, baseSpeed));
            if (baseSpeedCache.size() > 256) {
               baseSpeedCache.entrySet().removeIf(entry -> now > entry.getValue().expiresAt);
               if (baseSpeedCache.size() > 384) {
                  baseSpeedCache.clear();
               }
            }

            return baseSpeed;
         } else {
            return cached.baseSpeed;
         }
      }
   }

   private static int extractBaseSpeedFromPokemonData(Object pokemonData) {
      int baseSpeed = 0;

      try {
         Method getFormMethod = getZeroArgMethodCached(pokemonData.getClass(), "getForm");
         if (getFormMethod != null) {
            Object form = getFormMethod.invoke(pokemonData);
            if (form != null) {
               Method getBaseStats = getZeroArgMethodCached(form.getClass(), "getBaseStats");
               if (getBaseStats != null) {
                  baseSpeed = extractBaseSpeedFromStats(getBaseStats.invoke(form));
               }
            }
         }
      } catch (Exception var6) {
      }

      if (baseSpeed > 0) {
         return baseSpeed;
      } else {
         try {
            Method getSpeciesMethod = getZeroArgMethodCached(pokemonData.getClass(), "getSpecies");
            if (getSpeciesMethod == null) {
               return 0;
            }

            Object species = getSpeciesMethod.invoke(pokemonData);
            if (species != null) {
               Method getBaseStats = getZeroArgMethodCached(species.getClass(), "getBaseStats");
               if (getBaseStats == null) {
                  return 0;
               }

               return extractBaseSpeedFromStats(getBaseStats.invoke(species));
            }
         } catch (Exception var5) {
         }

         return 0;
      }
   }

   private static int extractBaseSpeedFromStats(Object statsObj) {
      if (!(statsObj instanceof Map<?, ?> statsMap)) {
         return 0;
      } else {
         for (Entry<?, ?> entry : statsMap.entrySet()) {
            if (entry.getValue() instanceof Number speedValue) {
               String statName = String.valueOf(entry.getKey()).toLowerCase();
               if (statName.contains("speed") || statName.equals("spe")) {
                  return speedValue.intValue();
               }
            }
         }

         return 0;
      }
   }

   private static class_2960 getTeraIcon(String typeName) {
      String key = typeName == null ? "normal" : typeName.toLowerCase();
      return teraIconCache.computeIfAbsent(key, k -> class_2960.method_60654("mega_showdown:textures/gui/summary/tera_types/" + k + ".png"));
   }

   private static float getHealthPercentFromBattle(ClientBattle battle, String pokemonName, boolean isPlayerPokemon) {
      ClientBattleSide playerSide = BattleSideHelper.getPlayerSide(battle);
      ClientBattleSide opponentSide = BattleSideHelper.getOpponentSide(battle);
      ClientBattleSide targetSide = isPlayerPokemon ? playerSide : opponentSide;

      for (ClientBattleActor actor : targetSide.getActors()) {
         for (ActiveClientBattlePokemon active : actor.getActivePokemon()) {
            ClientBattlePokemon bp = active.getBattlePokemon();
            if (bp != null && bp.getSpecies().getName().equalsIgnoreCase(pokemonName)) {
               float hpValue = bp.getHpValue();
               float maxHp = bp.getMaxHp();
               float ratio;
               if (!isPlayerPokemon && hpValue <= 1.0F && maxHp > 1.0F) {
                  ratio = hpValue;
               } else if (maxHp > 0.0F) {
                  ratio = hpValue / maxHp;
               } else {
                  ratio = 1.0F;
               }

               return ratio;
            }
         }
      }

      return 1.0F;
   }

   private static String getGenderFromBattle(ClientBattle battle, String pokemonName, boolean isPlayerPokemon) {
      ClientBattleSide playerSide = BattleSideHelper.getPlayerSide(battle);
      ClientBattleSide opponentSide = BattleSideHelper.getOpponentSide(battle);
      ClientBattleSide targetSide = isPlayerPokemon ? playerSide : opponentSide;

      for (ClientBattleActor actor : targetSide.getActors()) {
         for (ActiveClientBattlePokemon active : actor.getActivePokemon()) {
            ClientBattlePokemon bp = active.getBattlePokemon();
            if (bp != null && bp.getSpecies().getName().equalsIgnoreCase(pokemonName)) {
               try {
                  Method getGenderMethod = bp.getClass().getMethod("getGender");
                  Object genderObj = getGenderMethod.invoke(bp);
                  if (genderObj != null) {
                     return genderObj.toString();
                  }
               } catch (Exception var13) {
               }
            }
         }
      }

      return null;
   }

   private static String getStatusFromBattle(ClientBattle battle, String pokemonName, boolean isPlayerPokemon) {
      ClientBattleSide playerSide = BattleSideHelper.getPlayerSide(battle);
      ClientBattleSide opponentSide = BattleSideHelper.getOpponentSide(battle);
      ClientBattleSide targetSide = isPlayerPokemon ? playerSide : opponentSide;

      for (ClientBattleActor actor : targetSide.getActors()) {
         for (ActiveClientBattlePokemon active : actor.getActivePokemon()) {
            ClientBattlePokemon bp = active.getBattlePokemon();
            if (bp != null && bp.getSpecies().getName().equalsIgnoreCase(pokemonName)) {
               try {
                  Method getStatusMethod = bp.getClass().getMethod("getStatus");
                  Object statusObj = getStatusMethod.invoke(bp);
                  if (statusObj != null) {
                     Method getShowdownNameMethod = statusObj.getClass().getMethod("getShowdownName");
                     Object showdownName = getShowdownNameMethod.invoke(statusObj);
                     if (showdownName != null) {
                        return showdownName.toString();
                     }
                  }
               } catch (Exception var15) {
               }
            }
         }
      }

      return null;
   }

   private static boolean isPlayerPokemonByPokemonUUID(ClientBattle battle, UUID pokemonUUID, String pokemonName) {
      if (pokemonUUID == null) {
         return isPlayerSidePokemon(battle, pokemonName);
      } else {
         ClientBattleSide playerSide = BattleSideHelper.getPlayerSide(battle);
         ClientBattleSide opponentSide = BattleSideHelper.getOpponentSide(battle);

         for (ClientBattleActor actor : playerSide.getActors()) {
            for (ActiveClientBattlePokemon active : actor.getActivePokemon()) {
               ClientBattlePokemon bp = active.getBattlePokemon();
               if (bp != null) {
                  UUID battlePokemonUUID = bp.getUuid();
                  if (battlePokemonUUID != null && battlePokemonUUID.equals(pokemonUUID)) {
                     return true;
                  }
               }
            }
         }

         for (ClientBattleActor actor : opponentSide.getActors()) {
            for (ActiveClientBattlePokemon activex : actor.getActivePokemon()) {
               ClientBattlePokemon bp = activex.getBattlePokemon();
               if (bp != null) {
                  UUID battlePokemonUUID = bp.getUuid();
                  if (battlePokemonUUID != null && battlePokemonUUID.equals(pokemonUUID)) {
                     return false;
                  }
               }
            }
         }

         return isPlayerSidePokemon(battle, pokemonName);
      }
   }

   private static boolean isPlayerSidePokemon(ClientBattle battle, String pokemonName) {
      ClientBattleSide playerSide = BattleSideHelper.getPlayerSide(battle);

      for (ClientBattleActor actor : playerSide.getActors()) {
         for (ActiveClientBattlePokemon active : actor.getActivePokemon()) {
            ClientBattlePokemon bp = active.getBattlePokemon();
            if (bp != null && bp.getSpecies().getName().equalsIgnoreCase(pokemonName)) {
               CobblemonBattleInfoClient.debug("[BattleEntityOverlay] {} found on PLAYER side", new Object[]{pokemonName});
               return true;
            }
         }
      }

      CobblemonBattleInfoClient.debug("[BattleEntityOverlay] {} NOT found on player side -> must be OPPONENT", new Object[]{pokemonName});
      return false;
   }

   private static void renderMegaIcon(class_4587 poseStack, boolean isPlayer, int typeCount, boolean useAlternativeStyle) {
      RenderSystem.enableDepthTest();
      RenderSystem.setShaderTexture(0, MEGA_ICON);
      RenderSystem.setShader(class_757::method_34542);
      RenderSystem.enableBlend();
      float brightness = BattleExtrasConfig.getBattleNameplateBrightness();
      RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0F);
      Matrix4f matrix = poseStack.method_23760().method_23761();
      float typeIconsWidth = (float)(typeCount * 12 + (typeCount - 1) * 2);
      float paddingX = useAlternativeStyle ? 3.0F : 3.0F;
      float baseY = 20.0F + (useAlternativeStyle ? -10.0F : 3.0F);
      float iconY = baseY + 3.0F;
      float iconX;
      if (isPlayer) {
         float typeStartX = -70.0F + paddingX;
         iconX = typeStartX + typeIconsWidth + 2.0F;
      } else {
         float typeEndX = 70.0F - paddingX - typeIconsWidth;
         iconX = typeEndX - 6.0F - 2.0F;
      }

      if (typeCount == 1) {
         if (useAlternativeStyle) {
            if (isPlayer) {
               iconX -= 18.0F;
               iconY -= 21.0F;
            } else {
               iconX += MEGA_ICON_OFFSET_X_OPPONENT_ALT_SINGLE;
               iconY += MEGA_ICON_OFFSET_Y_OPPONENT_ALT_SINGLE;
            }
         } else {
            iconX += isPlayer ? MEGA_ICON_OFFSET_X_PLAYER_OVERLAY_SINGLE : MEGA_ICON_OFFSET_X_OPPONENT_OVERLAY_SINGLE;
            iconY += isPlayer ? MEGA_ICON_OFFSET_Y_PLAYER_OVERLAY_SINGLE : MEGA_ICON_OFFSET_Y_OPPONENT_OVERLAY_SINGLE;
         }
      } else if (typeCount == 2) {
         if (useAlternativeStyle) {
            iconX += isPlayer ? MEGA_ICON_OFFSET_X_PLAYER_ALT : MEGA_ICON_OFFSET_X_OPPONENT_ALT;
            iconY += isPlayer ? MEGA_ICON_OFFSET_Y_PLAYER_ALT : MEGA_ICON_OFFSET_Y_OPPONENT_ALT;
         } else {
            iconX += isPlayer ? MEGA_ICON_OFFSET_X_PLAYER_OVERLAY : MEGA_ICON_OFFSET_X_OPPONENT_OVERLAY;
            iconY += isPlayer ? MEGA_ICON_OFFSET_Y_PLAYER_OVERLAY : MEGA_ICON_OFFSET_Y_OPPONENT_OVERLAY;
         }
      }

      float iconZ = -0.1F;
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1585);
      buffer.method_22918(matrix, iconX, iconY, iconZ).method_22913(0.0F, 0.0F);
      buffer.method_22918(matrix, iconX, iconY + 6.0F, iconZ).method_22913(0.0F, 1.0F);
      buffer.method_22918(matrix, iconX + 6.0F, iconY + 6.0F, iconZ).method_22913(1.0F, 1.0F);
      buffer.method_22918(matrix, iconX + 6.0F, iconY, iconZ).method_22913(1.0F, 0.0F);
      class_286.method_43433(buffer.method_60800());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private static void renderSpeedRange(
      class_4587 poseStack,
      class_327 font,
      int baseSpeed,
      int level,
      boolean isPlayer,
      String pokemonName,
      String displayName,
      Integer exactPlayerSpeed,
      MatchedBattlePokemon matchedBattlePokemon,
      int nameplateStyle,
      class_4598 textBuffer
   ) {
      if (BattleExtrasConfig.isSpeedRangeEnabled()) {
         if (BattleExtrasConfig.isHideBattleTilesEnabled()) {
            try {
               String side = isPlayer ? "player" : "opponent";
               int speedStage = resolveTrackedStatStage(
                  side,
                  "speed",
                  pokemonName,
                  displayName,
                  getBattlePokemonDisplayName(matchedBattlePokemon != null ? matchedBattlePokemon.battlePokemon : null),
                  getBattlePokemonSpeciesName(matchedBattlePokemon != null ? matchedBattlePokemon.battlePokemon : null)
               );
               boolean hasTailwind = BattleMessageSubscriber.getTailwindTurns(side) > 0;
               if ((exactPlayerSpeed == null || exactPlayerSpeed <= 0) && baseSpeed <= 0) {
                  return;
               }

               String speedText;
               if (exactPlayerSpeed != null && exactPlayerSpeed > 0) {
                  int displayedSpeed = applySpeedStageModifier(exactPlayerSpeed, speedStage);
                  if (hasTailwind) {
                     displayedSpeed *= 2;
                  }

                  speedText = "Spe: " + displayedSpeed;
               } else {
                  int minSpeed = (int)Math.floor((double)((2 * baseSpeed + 0 + 0) * level / 100 + 5) * 0.9);
                  int maxSpeed = (int)Math.floor((double)((2 * baseSpeed + 31 + 63) * level / 100 + 5) * 1.1);
                  minSpeed = applySpeedStageModifier(minSpeed, speedStage);
                  maxSpeed = applySpeedStageModifier(maxSpeed, speedStage);
                  if (hasTailwind) {
                     minSpeed *= 2;
                     maxSpeed *= 2;
                  }

                  speedText = "Spe: " + minSpeed + "-" + maxSpeed;
               }

               int color;
               if (speedStage > 0) {
                  color = -11141291;
               } else if (speedStage < 0) {
                  color = -43691;
               } else {
                  color = -5579265;
               }

               float textZOffset = 0.1F;
               float speedY = 14.0F;
               float textScale = 0.85F;
               int textWidth = font.method_1727(speedText);
               float speedX;
               if (isPlayer) {
                  speedX = (float)(-textWidth) * 0.4F + 35.0F;
               } else {
                  speedX = (float)(-textWidth) * 0.4F - 40.0F;
               }

               poseStack.method_22903();
               poseStack.method_46416(speedX, speedY, textZOffset);
               poseStack.method_22905(textScale, textScale, textScale);
               font.method_27521(speedText, 0.0F, 0.0F, color, false, poseStack.method_23760().method_23761(), textBuffer, class_6415.field_33994, 0, 15728880);
               poseStack.method_22909();
            } catch (Throwable var21) {
            }
         }
      }
   }

   private static int applySpeedStageModifier(int speed, int speedStage) {
      if (speedStage > 0) {
         return (int)((double)(speed * (2 + speedStage)) / 2.0);
      } else {
         return speedStage < 0 ? (int)((double)speed * 2.0 / (double)(2 - speedStage)) : speed;
      }
   }

   private static Integer resolveExactPlayerSpeed(Object pokemonData, MatchedBattlePokemon matchedBattlePokemon, UUID pokemonUUID) {
      if (shouldUseRangeForForcedLevelClone(matchedBattlePokemon)) {
         return null;
      } else {
         Integer battleStateSpeed = getBattleStatePlayerSpeed(matchedBattlePokemon);
         if (battleStateSpeed != null && battleStateSpeed > 0) {
            return battleStateSpeed;
         } else {
            Integer battleLevelPartySpeed = getBattleLevelSpeedFromLocalParty(matchedBattlePokemon);
            if (battleLevelPartySpeed != null && battleLevelPartySpeed > 0) {
               return battleLevelPartySpeed;
            } else if (matchedBattlePokemon != null && matchedBattlePokemon.battlePokemon != null) {
               return null;
            } else {
               Integer directSpeed = getExactSpeedFromPokemonData(
                  pokemonData, matchedBattlePokemon != null && matchedBattlePokemon.battlePokemon != null ? matchedBattlePokemon.battlePokemon.getLevel() : 0
               );
               if (directSpeed != null && directSpeed > 0) {
                  return directSpeed;
               } else {
                  return matchedBattlePokemon != null && matchedBattlePokemon.battlePokemon != null ? null : getActualPlayerSpeedFromParty(pokemonUUID);
               }
            }
         }
      }
   }

   private static boolean shouldUseRangeForForcedLevelClone(MatchedBattlePokemon matchedBattlePokemon) {
      if (matchedBattlePokemon != null && matchedBattlePokemon.battlePokemon != null) {
         try {
            Pokemon localPartyPokemon = LocalBattlePartyTracker.findLocalPartyPokemon(matchedBattlePokemon.battlePokemon);
            if (localPartyPokemon == null) {
               return false;
            } else {
               int battleLevel = matchedBattlePokemon.battlePokemon.getLevel();
               int truePartyLevel = localPartyPokemon.getLevel();
               return battleLevel > 0 && truePartyLevel > 0 && battleLevel != truePartyLevel;
            }
         } catch (Throwable var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static Integer getBattleStatePlayerSpeed(MatchedBattlePokemon matchedBattlePokemon) {
      if (matchedBattlePokemon != null && matchedBattlePokemon.battlePokemon != null) {
         int battleLevel = 0;

         try {
            battleLevel = matchedBattlePokemon.battlePokemon.getLevel();
         } catch (Throwable var13) {
         }

         Integer actorPokemonSpeed = getExactSpeedFromActorPokemon(matchedBattlePokemon);
         if (actorPokemonSpeed != null && actorPokemonSpeed > 0) {
            return actorPokemonSpeed;
         } else {
            Object createdBattlePokemon = createPokemonFromBattleProperties(matchedBattlePokemon.battlePokemon);
            Integer createdSpeed = getExactSpeedFromPokemonData(createdBattlePokemon, battleLevel);
            if (createdSpeed != null && createdSpeed > 0) {
               return createdSpeed;
            } else {
               for (String methodName : new String[]{"getEffectedPokemon", "effectedPokemon", "getPokemon", "pokemon", "getOriginalPokemon"}) {
                  try {
                     Method method = matchedBattlePokemon.battlePokemon.getClass().getMethod(methodName);
                     Object battleBackedPokemon = method.invoke(matchedBattlePokemon.battlePokemon);
                     Integer speed = getExactSpeedFromPokemonData(battleBackedPokemon, battleLevel);
                     if (speed != null && speed > 0) {
                        return speed;
                     }
                  } catch (Throwable var12) {
                  }
               }

               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static Integer getBattleLevelSpeedFromLocalParty(MatchedBattlePokemon matchedBattlePokemon) {
      if (matchedBattlePokemon != null && matchedBattlePokemon.battlePokemon != null) {
         try {
            Pokemon localPartyPokemon = LocalBattlePartyTracker.findLocalPartyPokemon(matchedBattlePokemon.battlePokemon);
            if (localPartyPokemon == null) {
               return null;
            } else {
               int battleLevel = matchedBattlePokemon.battlePokemon.getLevel();
               return battleLevel <= 0 ? null : computeStatAtLevel(localPartyPokemon, Stats.SPEED, battleLevel);
            }
         } catch (Throwable var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Integer getExactSpeedFromActorPokemon(MatchedBattlePokemon matchedBattlePokemon) {
      if (matchedBattlePokemon != null && matchedBattlePokemon.activePokemon != null) {
         try {
            ClientBattleActor actor = matchedBattlePokemon.activePokemon.getActor();
            if (actor != null && actor.getPokemon() != null && !actor.getPokemon().isEmpty()) {
               UUID battlePokemonUuid = matchedBattlePokemon.battlePokemon != null ? matchedBattlePokemon.battlePokemon.getUuid() : null;
               if (battlePokemonUuid != null) {
                  for (Pokemon actorPokemon : actor.getPokemon()) {
                     if (actorPokemon != null && battlePokemonUuid.equals(actorPokemon.getUuid())) {
                        return actorPokemon.getStat(Stats.SPEED);
                     }
                  }
               }

               String battleSpeciesName = getBattlePokemonSpeciesName(matchedBattlePokemon.battlePokemon);
               if (battleSpeciesName != null && !battleSpeciesName.isBlank()) {
                  for (Pokemon actorPokemonx : actor.getPokemon()) {
                     if (actorPokemonx != null
                        && actorPokemonx.getSpecies() != null
                        && battleSpeciesName.equalsIgnoreCase(actorPokemonx.getSpecies().getName())) {
                        return actorPokemonx.getStat(Stats.SPEED);
                     }
                  }
               }

               return null;
            } else {
               return null;
            }
         } catch (Throwable var6) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Object createPokemonFromBattleProperties(ClientBattlePokemon battlePokemon) {
      if (battlePokemon == null) {
         return null;
      } else {
         try {
            Method getPropertiesMethod = battlePokemon.getClass().getMethod("getProperties");
            Object properties = getPropertiesMethod.invoke(battlePokemon);
            if (properties == null) {
               return null;
            } else {
               Method createMethod = properties.getClass().getMethod("create");
               return createMethod.invoke(properties);
            }
         } catch (Throwable var4) {
            return null;
         }
      }
   }

   private static Integer getExactSpeedFromPokemonData(Object pokemonData, int battleLevel) {
      if (pokemonData == null) {
         return null;
      } else {
         if (pokemonData instanceof Pokemon pokemon) {
            int level = battleLevel > 0 ? battleLevel : pokemon.getLevel();
            if (level > 0) {
               return computeStatAtLevel(pokemon, Stats.SPEED, level);
            }
         }

         try {
            Method getStatMethod = pokemonData.getClass().getMethod("getStat", Stat.class);
            if (getStatMethod.invoke(pokemonData, Stats.SPEED) instanceof Number number && number.intValue() > 0) {
               return number.intValue();
            }
         } catch (Throwable var5) {
         }

         return null;
      }
   }

   private static int computeStatAtLevel(Pokemon pokemon, Stats stat, int level) {
      try {
         Integer baseVal = (Integer)pokemon.getForm().getBaseStats().get(stat);
         int base = baseVal != null ? baseVal : 1;
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

   private static Integer getActualPlayerSpeedFromParty(UUID pokemonUUID) {
      if (pokemonUUID == null) {
         return null;
      } else {
         class_310 mc = class_310.method_1551();
         if (mc.field_1724 == null) {
            return null;
         } else {
            try {
               Object storage = CobblemonClient.INSTANCE.getStorage();
               Field partyField = storage.getClass().getDeclaredField("party");
               partyField.setAccessible(true);
               Object clientParty = partyField.get(storage);
               Object cachedPartyPokemon = clientParty.getClass().getMethod("findByUUID", UUID.class).invoke(clientParty, pokemonUUID);
               if (cachedPartyPokemon != null) {
                  return ((Pokemon)cachedPartyPokemon).getStat(Stats.SPEED);
               }
            } catch (Throwable var6) {
            }

            if (mc.field_1687 == null) {
               return null;
            } else {
               try {
                  for (Pokemon partyPokemon : Cobblemon.INSTANCE.getStorage().getParty(mc.field_1724.method_5667(), mc.field_1687.method_30349())) {
                     if (partyPokemon != null && pokemonUUID.equals(partyPokemon.getUuid())) {
                        return partyPokemon.getStat(Stats.SPEED);
                     }
                  }
               } catch (Throwable var7) {
               }

               return null;
            }
         }
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

   private static String getBattlePokemonDisplayName(ClientBattlePokemon battlePokemon) {
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

   private static void renderStatus(class_4587 poseStack, class_327 font, String status, boolean isPlayer, int nameplateStyle, class_4598 textBuffer) {
      if (status != null && !status.isEmpty()) {
         int textColor = -1;
         String displayText = status.toUpperCase();
         String textZOffset = status.toLowerCase();
         int bgColor;
         switch (textZOffset) {
            case "slp":
               bgColor = -7303008;
               break;
            case "brn":
               bgColor = -2068432;
               break;
            case "psn":
            case "tox":
               bgColor = -6274912;
               if (status.equalsIgnoreCase("tox")) {
                  displayText = "TOX";
               }
               break;
            case "par":
               bgColor = -3100640;
               break;
            case "frz":
               bgColor = -9385776;
               break;
            default:
               bgColor = -8355712;
         }

         float textZOffset = 0.1F;
         float statusY = 11.0F;
         int textWidth = font.method_1727(displayText);
         float bgPadding = 2.0F;
         float bgWidth = (float)textWidth * 0.7F + bgPadding * 2.0F;
         float bgHeight = 8.0F;
         float typeIconsWidth = 26.0F;
         float statusX;
         if (isPlayer) {
            statusX = -67.0F + typeIconsWidth + 10.0F;
         } else {
            statusX = 67.0F - typeIconsWidth - bgWidth - 10.0F;
         }

         poseStack.method_22903();
         poseStack.method_46416(statusX, statusY, textZOffset - 0.01F);
         RenderSystem.enableBlend();
         RenderSystem.setShader(class_757::method_34540);
         Matrix4f matrix = poseStack.method_23760().method_23761();
         class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);
         float r = (float)(bgColor >> 16 & 0xFF) / 255.0F;
         float g = (float)(bgColor >> 8 & 0xFF) / 255.0F;
         float b = (float)(bgColor & 0xFF) / 255.0F;
         float a = (float)(bgColor >> 24 & 0xFF) / 255.0F;
         buffer.method_22918(matrix, 0.0F, 0.0F, 0.0F).method_22915(r, g, b, a);
         buffer.method_22918(matrix, 0.0F, bgHeight, 0.0F).method_22915(r, g, b, a);
         buffer.method_22918(matrix, bgWidth, bgHeight, 0.0F).method_22915(r, g, b, a);
         buffer.method_22918(matrix, bgWidth, 0.0F, 0.0F).method_22915(r, g, b, a);
         class_286.method_43433(buffer.method_60800());
         poseStack.method_22909();
         poseStack.method_22903();
         poseStack.method_46416(statusX + bgPadding, statusY + 1.0F, textZOffset);
         poseStack.method_22905(0.7F, 0.7F, 0.7F);
         font.method_27521(displayText, 0.0F, 0.0F, textColor, false, poseStack.method_23760().method_23761(), textBuffer, class_6415.field_33994, 0, 15728880);
         poseStack.method_22909();
      }
   }

   private static final class OverlayBattleCacheData {
      private final long expiresAt;
      private final boolean playerPokemon;
      private final float healthPercent;
      private final String gender;
      private final String status;
      private final boolean megaEvolved;

      private OverlayBattleCacheData(long expiresAt, boolean playerPokemon, float healthPercent, String gender, String status, boolean megaEvolved) {
         this.expiresAt = expiresAt;
         this.playerPokemon = playerPokemon;
         this.healthPercent = healthPercent;
         this.gender = gender;
         this.status = status;
         this.megaEvolved = megaEvolved;
      }
   }
}
