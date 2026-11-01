package name.modid.client;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.battles.ai.StrongBattleAIKt;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.cobblemon.mod.common.net.messages.client.battle.BattleTransformPokemonPacket;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import name.modid.CobblemonBattleInfoClient;
import name.modid.client.BattleMessageSubscriber.1;
import name.modid.client.BattleMessageSubscriber.HiddenPowerPendingContext;
import name.modid.client.BattleMessageSubscriber.RevealedPokemonContext;
import name.modid.client.BattleMessageSubscriber.SwapBoostPendingContext;
import name.modid.client.battlelog.BattleLogManager;
import name.modid.client.battlelog.BattleParticipantResolver;
import name.modid.client.battlelog.BattleLogEntry.ActionSide;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2588;
import net.minecraft.class_310;
import net.minecraft.class_7417;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BattleMessageSubscriber {
   private static final Logger LOGGER = LoggerFactory.getLogger("BattleMessageSubscriber");
   private static final String PROP_DEBUG_KEYS = "battleextras.debugKeys";
   private static final String ENV_DEBUG_KEYS = "BATTLEEXTRAS_DEBUG_KEYS";
   private static final String PROP_DEBUG_KEYS_FILTER = "battleextras.debugKeysFilter";
   private static final String ENV_DEBUG_KEYS_FILTER = "BATTLEEXTRAS_DEBUG_KEYS_FILTER";
   private static String currentWeather = "";
   private static String currentTerrain = "";
   private static int weatherTurns = 0;
   private static int terrainTurns = 0;
   private static int terrainStartTurn = 0;
   private static boolean weatherJustSet = false;
   private static final Map<String, Map<String, Integer>> statStages = new ConcurrentHashMap<>();
   private static final Map<String, Map<String, Integer>> pendingBatonPassStats = new ConcurrentHashMap<>();
   private static final Map<String, Map<String, Integer>> screens = new ConcurrentHashMap<>();
   private static final Map<String, Map<String, Integer>> hazards = new ConcurrentHashMap<>();
   private static final Map<String, Set<String>> ghostCurseTargetsBySide = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> leechSeeded = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> ingrained = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> aquaRing = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> hasSubstitute = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> confused = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> identified = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> taunted = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> encored = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> saltCured = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> infested = new ConcurrentHashMap<>();
   private static int trickRoomTurns = 0;
   private static final Map<String, Integer> tailwind = new ConcurrentHashMap<>();
   private static final Map<String, String> terastallized = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> soaked = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> burnedUp = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> doubleShocked = new ConcurrentHashMap<>();
   private static String pendingBurnUpPokemon = null;
   private static String pendingDoubleShockPokemon = null;
   private static final Map<String, String> lastBurnUpDoubleShockSide = new ConcurrentHashMap<>();
   private static String pendingSoakPokemon = null;
   private static String pendingSoakTarget = null;
   private static String pendingTeraSide = null;
   private static final Map<String, Integer> roosted = new ConcurrentHashMap<>();
   private static final Map<String, Integer> magnetRise = new ConcurrentHashMap<>();
   private static String pendingMagnetRisePokemon = null;
   private static final Map<String, Integer> telekinesis = new ConcurrentHashMap<>();
   private static String pendingTelekinesisMessage = null;
   private static String pendingHealBlockPokemon = null;
   private static String pendingEmbargoPokemon = null;
   private static String pendingSafeguardSide = null;
   private static String pendingLuckyChantSide = null;
   private static String pendingMistSide = null;
   private static String pendingSendOutTrainer = null;
   private static String pendingTransformTypePokemon = null;
   private static String pendingTransformTypeName = null;
   private static final String[] ALL_TYPES = new String[]{
      "Normal",
      "Fire",
      "Water",
      "Electric",
      "Grass",
      "Ice",
      "Fighting",
      "Poison",
      "Ground",
      "Flying",
      "Psychic",
      "Bug",
      "Rock",
      "Ghost",
      "Dragon",
      "Dark",
      "Steel",
      "Fairy"
   };
   private static final String[] HIDDEN_POWER_TYPES = new String[]{
      "fighting", "flying", "poison", "ground", "rock", "bug", "ghost", "steel", "fire", "water", "grass", "electric", "psychic", "ice", "dragon", "dark"
   };
   private static final Map<String, String> transformedTypes = new ConcurrentHashMap<>();
   private static final Map<String, String> addedTypes = new ConcurrentHashMap<>();
   private static final Map<String, String> reflectTypes = new ConcurrentHashMap<>();
   private static final Map<String, Set<String>> hiddenPowerCandidates = new ConcurrentHashMap<>();
   private static final Map<String, String> hiddenPowerResolvedTypes = new ConcurrentHashMap<>();
   private static final Map<String, Set<String>> hiddenPowerObservedMoveLines = new ConcurrentHashMap<>();
   private static HiddenPowerPendingContext pendingHiddenPowerContext = null;
   private static SwapBoostPendingContext pendingSwapBoostContext = null;
   private static String pendingAddedType = null;
   private static String pendingReflectTypePokemon = null;
   private static String pendingReflectTypeTarget = null;
   private static final Map<String, Boolean> smackDownGrounded = new ConcurrentHashMap<>();
   private static final StringBuilder statChangeBuffer = new StringBuilder();
   private static long lastStatChangeMessageTime = 0L;
   private static final long STAT_CHANGE_BUFFER_TIMEOUT_MS = 100L;
   private static int gravityTurns = 0;
   private static int wonderRoomTurns = 0;
   private static int magicRoomTurns = 0;
   private static int perishSongTurns = 0;
   private static int perishSongStartTurn = 0;
   private static final Map<String, Integer> embargo = new ConcurrentHashMap<>();
   private static final Map<String, Integer> healBlock = new ConcurrentHashMap<>();
   private static boolean hasLightClay = false;
   private static final Map<String, List<String>> megaEvolved = new ConcurrentHashMap<>();
   private static final Map<String, String> meloettaForm = new ConcurrentHashMap<>();
   private static String pendingMeloettaFormPokemon = null;
   private static final Map<String, String> pokemonAbilities = new ConcurrentHashMap<>();
   private static final Map<String, String> activePrimalPokemon = new ConcurrentHashMap<>();
   private static final Map<String, Map<String, int[]>> pokemonMovePP = new ConcurrentHashMap<>();
   private static boolean hasWeatherRock = false;
   private static int weatherStartTurn = 0;
   private static boolean subscribed = false;
   private static Object lastBattle = null;
   private static int tickCounter = 0;
   private static int lastTurnNumber = 0;
   private static int currentTurnNumber = 0;
   private static int playerPokemonCount = 0;
   private static int opponentPokemonCount = 0;
   private static String lastPokemonName = null;
   private static String currentActivePlayerPokemon = null;
   private static String currentActiveOpponentPokemon = null;
   private static String pendingStatMessage = null;
   private static long lastMessageTime = 0L;
   private static final StringBuilder messageBuffer = new StringBuilder();
   private static long lastBufferMessageTime = 0L;
   private static final long MESSAGE_BUFFER_TIMEOUT = 100L;
   private static String localPlayerName = null;
   private static final Set<String> localPlayerPokemon = ConcurrentHashMap.newKeySet();
   private static final Set<String> opponentPokemon = ConcurrentHashMap.newKeySet();
   private static final Map<String, String> ownerTextToSide = new ConcurrentHashMap<>();
   private static final Map<String, String> heldItems = new ConcurrentHashMap<>();
   private static boolean translationKeyDebugLogging = false;
   private static String translationKeyDebugFilter = null;
   private static long lastPacketHookDebugMs = 0L;
   private static long lastPacketFilteredKeyDebugMs = 0L;
   private static String lastFilteredKey = null;
   private static long lastPacketKeyInfoMs = 0L;
   private static String lastInfoKey = null;
   private static Class<?> cachedCobblemonClientClass;
   private static Object cachedCobblemonClientInstance;
   private static Method cachedGetBattleMethod;
   private static boolean reflectionCacheInitialized;
   private static final String[] ACCESSOR_METHOD_NAMES;
   private static final String[] DEBUG_ACCESSOR_METHOD_NAMES;
   private static final Map<Class<?>, Map<String, Method>> methodCache;
   private static final Map<Class<?>, Set<String>> methodNotFoundCache;
   private static final ConcurrentHashMap<String, Long> recentAbilityPopups;
   private static final long ABILITY_POPUP_DEDUPE_WINDOW_MS = 1500L;
   private static volatile long lastAbilityPopupFromKeyMs;
   private static final long SUPPRESS_LEGACY_ABILITY_WINDOW_MS = 2000L;
   private static int waterSportTurns;
   private static int mudSportTurns;

   private static String normalizePokemonName(String pokemonName) {
      return pokemonName == null ? null : pokemonName.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
   }

   private static String makeSidedKey(String side, String pokemonName) {
      if (pokemonName == null) {
         return null;
      } else {
         String safeSide = side != null && !side.isEmpty() ? side.toLowerCase() : "unknown";
         return safeSide + "_" + pokemonName.toLowerCase(Locale.ROOT);
      }
   }

   private static String determineSideFromFullText(String pokemonFullText, String pokemonLower) {
      String side = determineSideFromBattleCandidates(pokemonFullText, pokemonLower);
      if (side != null) {
         return side;
      } else {
         side = null;
         String fullLower = pokemonFullText == null ? null : pokemonFullText.toLowerCase(Locale.ROOT);
         String normalizedSpecies = normalizePokemonName(pokemonLower);
         if (pokemonFullText != null) {
            side = ownerTextToSide.get(fullLower);
         }

         if (side == null && pokemonLower != null) {
            side = ownerTextToSide.get(pokemonLower.toLowerCase(Locale.ROOT));
         }

         if (side == null && normalizedSpecies != null) {
            side = ownerTextToSide.get(normalizedSpecies);
         }

         if (side == null && normalizedSpecies != null) {
            side = determineSideFromBattle(normalizedSpecies);
         }

         if (side == null && pokemonFullText != null) {
            String strippedName = stripOwnerPrefix(pokemonFullText);
            String normalizedStripped = normalizePokemonName(strippedName);
            if (side == null && normalizedStripped != null) {
               side = determineSideFromBattle(normalizedStripped);
            }

            if (side == null) {
               String ownerName = extractOwnerNameFromBattleText(pokemonFullText);
               if (ownerName != null) {
                  side = determineSideFromTrainerName(ownerName);
               }
            }

            if (side == null && !hasOwnerPrefix(pokemonFullText)) {
               side = "opponent";
            }
         }

         if (side == null && pokemonLower != null) {
            String lowerSpecies = pokemonLower.toLowerCase(Locale.ROOT);
            if (!localPlayerPokemon.contains(lowerSpecies) && (normalizedSpecies == null || !localPlayerPokemon.contains(normalizedSpecies))) {
               if (opponentPokemon.contains(lowerSpecies) || normalizedSpecies != null && opponentPokemon.contains(normalizedSpecies)) {
                  side = "opponent";
               }
            } else {
               side = "player";
            }
         }

         return side;
      }
   }

   private static boolean hasOwnerPrefix(String pokemonFullText) {
      return pokemonFullText == null
         ? false
         : pokemonFullText.contains("'s ") || pokemonFullText.contains(" de ") || pokemonFullText.contains(" von ") || pokemonFullText.contains(" di ");
   }

   private static String extractOwnerNameFromBattleText(String pokemonFullText) {
      if (pokemonFullText == null) {
         return null;
      } else {
         int apostropheIdx = pokemonFullText.indexOf("'s ");
         if (apostropheIdx > 0) {
            return pokemonFullText.substring(0, apostropheIdx);
         } else {
            int deIdx = pokemonFullText.indexOf(" de ");
            if (deIdx > 0) {
               return pokemonFullText.substring(deIdx + 4);
            } else {
               int vonIdx = pokemonFullText.indexOf(" von ");
               if (vonIdx > 0) {
                  return pokemonFullText.substring(vonIdx + 5);
               } else {
                  int diIdx = pokemonFullText.indexOf(" di ");
                  return diIdx > 0 ? pokemonFullText.substring(diIdx + 4) : null;
               }
            }
         }
      }
   }

   private static String determineSideFromTrainerName(String trainerName) {
      if (trainerName != null && !trainerName.isBlank()) {
         try {
            ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
            String resolvedSide = BattleSideHelper.resolveTrainerSide(battle, trainerName);
            if (resolvedSide != null && !resolvedSide.isBlank()) {
               return resolvedSide;
            }
         } catch (Throwable var3) {
         }

         return localPlayerName != null && trainerName.equalsIgnoreCase(localPlayerName) ? "player" : null;
      } else {
         return null;
      }
   }

   private static String extractNameFromSidedKey(String sidedKey) {
      if (sidedKey == null) {
         return null;
      } else {
         int idx = sidedKey.indexOf(95);
         return idx >= 0 ? sidedKey.substring(idx + 1) : sidedKey;
      }
   }

   private static String readStringToggle(String systemPropertyName, String envVarName, String defaultValue) {
      try {
         String prop = System.getProperty(systemPropertyName);
         if (prop != null && !prop.isBlank()) {
            return prop.trim();
         }
      } catch (Throwable var5) {
      }

      try {
         String env = System.getenv(envVarName);
         if (env != null && !env.isBlank()) {
            return env.trim();
         }
      } catch (Throwable var4) {
      }

      return defaultValue;
   }

   private static boolean shouldDebugLogKey(String key) {
      try {
         if (!translationKeyDebugLogging) {
            return false;
         } else if (key == null) {
            return false;
         } else if (translationKeyDebugFilter != null && !translationKeyDebugFilter.isBlank()) {
            String f = translationKeyDebugFilter.toLowerCase(Locale.ROOT);
            return f.equals("battle") ? key.toLowerCase(Locale.ROOT).contains("cobblemon.battle") : key.toLowerCase(Locale.ROOT).contains(f);
         } else {
            return true;
         }
      } catch (Throwable var2) {
         return false;
      }
   }

   private static boolean readBooleanToggle(String systemPropertyName, String envVarName, boolean defaultValue) {
      try {
         String prop = System.getProperty(systemPropertyName);
         if (prop != null && !prop.isBlank()) {
            return Boolean.parseBoolean(prop.trim());
         }
      } catch (Throwable var5) {
      }

      try {
         String env = System.getenv(envVarName);
         if (env != null && !env.isBlank()) {
            return Boolean.parseBoolean(env.trim());
         }
      } catch (Throwable var4) {
      }

      return defaultValue;
   }

   public static void onBattleTranslatableMessage(class_2588 tc) {
      try {
         if (tc == null) {
            return;
         }

         if (translationKeyDebugLogging) {
            long now = System.currentTimeMillis();
            if (now - lastPacketHookDebugMs > 2000L) {
               lastPacketHookDebugMs = now;

               try {
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] BattleMessageHandler hook is firing (TranslatableContents packets observed). If you still don't see TK(packet) lines, your debug filter is excluding them.",
                     new Object[0]
                  );
               } catch (Throwable var5) {
               }
            }
         }

         if (!shouldDebugLogKey(tc.method_11022())) {
            if (translationKeyDebugLogging) {
               try {
                  long now = System.currentTimeMillis();
                  if (now - lastPacketFilteredKeyDebugMs > 2000L && (lastFilteredKey == null || !lastFilteredKey.equals(tc.method_11022()))) {
                     lastPacketFilteredKeyDebugMs = now;
                     lastFilteredKey = tc.method_11022();
                     String f = translationKeyDebugFilter == null ? "" : translationKeyDebugFilter;
                     CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Debug filter '{}' excluded key: {}", new Object[]{f, tc.method_11022()});
                  }
               } catch (Throwable var6) {
               }
            }
         } else {
            try {
               long now = System.currentTimeMillis();
               if (now - lastPacketKeyInfoMs > 250L && (lastInfoKey == null || !lastInfoKey.equals(tc.method_11022()))) {
                  lastPacketKeyInfoMs = now;
                  lastInfoKey = tc.method_11022();
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] TKi(packet): {} args={}",
                     new Object[]{tc.method_11022(), tc.method_11023() != null ? tc.method_11023().length : 0}
                  );
               }
            } catch (Throwable var7) {
            }

            try {
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] TK(packet): {} args={}", new Object[]{tc.method_11022(), tc.method_11023() != null ? tc.method_11023().length : 0}
               );
            } catch (Throwable var4) {
            }
         }

         processTranslatableMessage(tc);
      } catch (Throwable var8) {
      }
   }

   public static void onBattleTransformPacket(BattleTransformPokemonPacket packet) {
      try {
         if (packet == null) {
            return;
         }

         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] Transform packet received - PNX: {} (NOT marking as mega - handled by message parsing)", new Object[]{packet.getPnx()}
         );
      } catch (Throwable var2) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error processing transform packet", new Object[]{var2});
      }
   }

   private static void initReflectionCache() {
      if (!reflectionCacheInitialized) {
         try {
            cachedCobblemonClientClass = Class.forName("com.cobblemon.mod.common.client.CobblemonClient");
            cachedCobblemonClientInstance = cachedCobblemonClientClass.getField("INSTANCE").get(null);
            cachedGetBattleMethod = cachedCobblemonClientInstance.getClass().getMethod("getBattle");
            reflectionCacheInitialized = true;
         } catch (Exception var1) {
         }
      }
   }

   public static void tick() {
      tickCounter++;

      try {
         class_310 mc = class_310.method_1551();
         if (mc.field_1724 == null) {
            if (subscribed) {
               reset();
            }

            return;
         }

         if (localPlayerName == null && mc.field_1724.method_5477() != null) {
            localPlayerName = mc.field_1724.method_5477().getString();
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Local player name: {}", new Object[]{localPlayerName});
         }

         initReflectionCache();
         if (!reflectionCacheInitialized || cachedGetBattleMethod == null || cachedCobblemonClientInstance == null) {
            return;
         }

         Object battle = cachedGetBattleMethod.invoke(cachedCobblemonClientInstance);
         if (battle == null) {
            if (subscribed) {
               reset();
            }

            return;
         }

         if (battle != lastBattle) {
            if (lastBattle != null || subscribed) {
               reset();
            }

            lastBattle = battle;
            subscribed = false;
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] New battle detected!", new Object[0]);
            checkPlayerWeatherRock(battle);
            checkPlayerLightClay(battle);
            extractOpponentPokemonFromBattle(battle);
         }

         if (!subscribed) {
            subscribeToMessages(battle);
         }
      } catch (Exception var2) {
      }
   }

   private static void subscribeToMessages(Object battle) {
      try {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Subscribing via reflection", new Object[0]);
         Method getMessages = battle.getClass().getMethod("getMessages");
         Object messageQueue = getMessages.invoke(battle);
         Method subscribeMethod = null;

         for (Method m : messageQueue.getClass().getMethods()) {
            if (m.getName().equals("subscribe") && m.getParameterCount() == 1) {
               subscribeMethod = m;
               break;
            }
         }

         if (subscribeMethod == null) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] No subscribe method found", new Object[0]);
            return;
         }

         Class<?> function1Class = Class.forName("kotlin.jvm.functions.Function1");

         Object kotlinUnit;
         try {
            Class<?> unitClass = Class.forName("kotlin.Unit");
            kotlinUnit = unitClass.getField("INSTANCE").get(null);
         } catch (Exception var8) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Failed to cache Kotlin Unit", new Object[0]);
            return;
         }

         Object function1Proxy = Proxy.newProxyInstance(function1Class.getClassLoader(), new Class[]{function1Class}, (proxy, method, args) -> {
            if (method.getName().equals("invoke") && args != null && args.length == 1) {
               Object msg = args[0];
               processMessage(msg);
            }

            return kotlinUnit;
         });
         subscribeMethod.invoke(messageQueue, function1Proxy);
         subscribed = true;
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Subscribed successfully!", new Object[0]);
      } catch (Exception var9) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Subscribe error: {}", new Object[]{var9.getMessage(), var9});
      }
   }

   private static void processMessage(Object msg) {
      try {
         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] processMessage called with: {}", new Object[]{msg != null ? msg.getClass().getName() : "null"}
         );
         if (translationKeyDebugLogging) {
            try {
               for (String m : DEBUG_ACCESSOR_METHOD_NAMES) {
                  try {
                     Method method = getCachedMethod(msg.getClass(), m);
                     if (method != null
                        && method.invoke(msg) instanceof class_2561 c
                        && c.method_10851() instanceof class_2588 tc0
                        && shouldDebugLogKey(tc0.method_11022())) {
                        CobblemonBattleInfoClient.debug(
                           "[BattleMessageSubscriber] TK(direct:{}): {} args={}",
                           new Object[]{m, tc0.method_11022(), tc0.method_11023() != null ? tc0.method_11023().length : 0}
                        );
                     }
                  } catch (Throwable var11) {
                  }
               }
            } catch (Throwable var12) {
            }
         }

         List<class_2588> foundKeys = extractAllTranslatableContents(msg);
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Found {} translation keys in message", new Object[]{foundKeys.size()});

         for (class_2588 tc : foundKeys) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Processing key: {}", new Object[]{tc.method_11022()});
            if (shouldDebugLogKey(tc.method_11022())) {
               try {
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] TK: {} args={}", new Object[]{tc.method_11022(), tc.method_11023() != null ? tc.method_11023().length : 0}
                  );
               } catch (Throwable var10) {
               }
            }

            processTranslatableMessage(tc);
         }

         String text = extractTextFromFormattedCharSequence(msg);
         if (text != null && !text.isEmpty()) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Raw message: '{}'", new Object[]{text});
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBufferMessageTime > 100L && messageBuffer.length() > 0) {
               String completeMessage = messageBuffer.toString();
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Processing buffered: '{}'", new Object[]{completeMessage});
               BattleLogger.addBattleMessage(completeMessage);
               messageBuffer.setLength(0);
            }

            if (messageBuffer.length() > 0) {
               messageBuffer.append(" ");
            }

            messageBuffer.append(text.trim());
            lastBufferMessageTime = currentTime;
            String trimmed = text.trim();
            if (trimmed.endsWith("!") || trimmed.endsWith(".") || trimmed.endsWith("?")) {
               String completeMessage = messageBuffer.toString();
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Processing complete: '{}'", new Object[]{completeMessage});
               BattleLogger.addBattleMessage(completeMessage);
               messageBuffer.setLength(0);
            }
         }
      } catch (Exception var13) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Process error: {}", new Object[]{var13.getMessage(), var13});
      }
   }

   private static List<class_2588> extractAllTranslatableContents(Object msg) {
      List<class_2588> out = new ArrayList<>();
      Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());

      try {
         extractAllTranslatableContents0(msg, out, visited, 0);
      } catch (Throwable var4) {
      }

      return out;
   }

   private static void extractAllTranslatableContents0(Object obj, List<class_2588> out, Set<Object> visited, int depth) {
      if (obj != null) {
         if (depth <= 6) {
            if (!visited.contains(obj)) {
               visited.add(obj);
               if (obj instanceof class_2561 c) {
                  class_2588 tc = tryGetTranslatableContents(c);
                  if (tc != null) {
                     out.add(tc);
                  }

                  try {
                     if (c.method_10851() instanceof class_2588 inner) {
                        Object[] args = inner.method_11023();
                        if (args != null) {
                           for (Object a : args) {
                              extractAllTranslatableContents0(a, out, visited, depth + 1);
                           }
                        }
                     }
                  } catch (Throwable var13) {
                  }
               } else if (obj.getClass().isArray()) {
                  int len = Array.getLength(obj);

                  for (int i = 0; i < len; i++) {
                     Object a = Array.get(obj, i);
                     extractAllTranslatableContents0(a, out, visited, depth + 1);
                  }
               } else if (obj instanceof Iterable) {
                  for (Object a : (Iterable)obj) {
                     extractAllTranslatableContents0(a, out, visited, depth + 1);
                  }
               } else {
                  for (String m : ACCESSOR_METHOD_NAMES) {
                     try {
                        Method method = getCachedMethod(obj.getClass(), m);
                        if (method != null) {
                           Object r = method.invoke(obj);
                           extractAllTranslatableContents0(r, out, visited, depth + 1);
                        }
                     } catch (Throwable var12) {
                     }
                  }
               }
            }
         }
      }
   }

   private static Method getCachedMethod(Class<?> clazz, String methodName) {
      Set<String> notFound = methodNotFoundCache.get(clazz);
      if (notFound != null && notFound.contains(methodName)) {
         return null;
      } else {
         Map<String, Method> classMethods = methodCache.get(clazz);
         if (classMethods != null) {
            Method cached = classMethods.get(methodName);
            if (cached != null) {
               return cached;
            }
         }

         try {
            Method method = clazz.getMethod(methodName);
            methodCache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>()).put(methodName, method);
            return method;
         } catch (NoSuchMethodException var5) {
            methodNotFoundCache.computeIfAbsent(clazz, k -> ConcurrentHashMap.newKeySet()).add(methodName);
            return null;
         } catch (Throwable var6) {
            return null;
         }
      }
   }

   private static class_2588 tryGetTranslatableContents(class_2561 component) {
      try {
         if (component == null) {
            return null;
         }

         class_7417 var2 = component.method_10851();
         if (var2 instanceof class_2588) {
            return (class_2588)var2;
         }
      } catch (Throwable var3) {
      }

      return null;
   }

   private static void processTranslatableMessage(class_2588 msg) {
      try {
         String key = msg.method_11022();
         if (key == null) {
            return;
         }

         Object[] args = msg.method_11023();
         String[] parts = key.split("\\.");
         BattleLogger.onBattleTranslationKey(key, args);
         if (isTerastallizationKey(key)) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] TERA key hook: {} (args: {})", new Object[]{key, args == null ? 0 : args.length});
            tryApplyTerastallizationFromArgs(parts, args);
         }

         if (key.contains("switch")) {
            CobblemonBattleInfoClient.debug(
               "[processTranslatableMessage] SWITCH KEY: {} args={}", new Object[]{key, msg.method_11023() == null ? 0 : msg.method_11023().length}
            );
         }

         if (translationKeyDebugLogging) {
            try {
               if (key.contains("mega") || key.contains("forme") || key.contains("transform")) {
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] MEGA DEBUG - Key: {} args: {}", new Object[]{key, msg.method_11023() == null ? 0 : msg.method_11023().length}
                  );
               }

               if (key.contains("switch") || key.contains("sendout") || key.contains("sent")) {
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] SWITCH DEBUG - Key: {} args: {}", new Object[]{key, msg.method_11023() == null ? 0 : msg.method_11023().length}
                  );
                  if (msg.method_11023() != null) {
                     for (int i = 0; i < msg.method_11023().length; i++) {
                        CobblemonBattleInfoClient.debug("[BattleMessageSubscriber]   arg[{}]: {}", new Object[]{i, safeExtractPlainText(msg.method_11023()[i])});
                     }
                  }
               }
            } catch (Throwable var22) {
            }
         }

         if (parts.length < 3) {
            return;
         }

         if (!"cobblemon".equals(parts[0]) || !"battle".equals(parts[1])) {
            return;
         }

         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Battle TK: {} (args: {})", new Object[]{key, args == null ? 0 : args.length});
         if (key.contains("switch") || key.contains("withdraw") || key.contains("recall") || key.contains("return") || key.contains("sendout")) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] SWITCH KEY: {} (args: {})", new Object[]{key, args == null ? 0 : args.length});
            if (args != null) {
               for (int i = 0; i < args.length; i++) {
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber]   arg[{}]: {}", new Object[]{i, safeExtractPlainText(args[i])});
               }
            }
         }

         if (key.contains("focus") || key.contains("pump") || key.contains("critical") || key.contains("crit")) {
            CobblemonBattleInfoClient.debug(
               "[BattleMessageSubscriber] FOCUS/CRIT KEY DETECTED: {} (args: {})", new Object[]{key, args == null ? 0 : args.length}
            );
            if (args != null) {
               for (int i = 0; i < args.length; i++) {
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber]   arg[{}]: {}", new Object[]{i, safeExtractPlainText(args[i])});
               }
            }
         }

         if (key.contains("smack") || key.contains("ground") || key.contains("fell") || key.contains("gravity")) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] GROUNDING KEY DETECTED: {}", new Object[]{key});
            if (args != null) {
               for (int i = 0; i < args.length; i++) {
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber]   arg[{}]: {}", new Object[]{i, safeExtractPlainText(args[i])});
               }
            }
         }

         if (key.contains("burn") || key.contains("type") || key.contains("transform") || key.contains("shock")) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] TYPE/BURN DEBUG - Key: {} (args: {})", new Object[]{key, args == null ? 0 : args.length});
            if (args != null) {
               for (int i = 0; i < args.length; i++) {
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber]   arg[{}]: {}", new Object[]{i, safeExtractPlainText(args[i])});
               }
            }
         }

         if (key.contains("switch.self") && args != null && args.length >= 1) {
            String speciesId = safeExtractPokemonSpeciesIdLowerFromArgs(args, 0);
            String fullText = safeExtractPlainText(args[0]);
            String ownerFromSwitch = null;
            if (args[0] instanceof class_2561 c && c.method_10851() instanceof class_2588 tc) {
               CobblemonBattleInfoClient.debug(
                  "[SIDE_TRACK] switch.self translation key: '{}', args: {}", new Object[]{tc.method_11022(), tc.method_11023().length}
               );
               if (tc.method_11022().equals("cobblemon.battle.owned_pokemon") && tc.method_11023().length >= 1) {
                  ownerFromSwitch = safeExtractPlainText(tc.method_11023()[0]);
               }
            }

            if (speciesId != null) {
               String switchSide = ownerFromSwitch == null ? "player" : determineSideFromTrainerName(ownerFromSwitch);
               if (switchSide == null) {
                  switchSide = "player";
               }

               if (fullText != null && ownerFromSwitch != null) {
                  String lowerFullText = fullText.toLowerCase(Locale.ROOT);
                  ownerTextToSide.put(lowerFullText, switchSide);
                  CobblemonBattleInfoClient.debug(
                     "[SIDE_TRACK] Mapped {} from switch with owner: '{}'", new Object[]{switchSide.toUpperCase(Locale.ROOT), lowerFullText}
                  );
               } else {
                  CobblemonBattleInfoClient.debug("[SIDE_TRACK] Skipped mapping bare Pokemon name: '{}' (no owner info)", new Object[]{fullText});
               }

               if ("player".equals(switchSide)) {
                  localPlayerPokemon.add(speciesId.toLowerCase(Locale.ROOT));
                  opponentPokemon.remove(speciesId.toLowerCase(Locale.ROOT));
                  if (fullText != null) {
                     localPlayerPokemon.add(fullText.toLowerCase(Locale.ROOT));
                  }

                  CobblemonBattleInfoClient.debug("[SIDE_TRACK] Added to localPlayerPokemon: '{}', '{}'", new Object[]{speciesId, fullText});
                  currentActivePlayerPokemon = speciesId;
                  CobblemonBattleInfoClient.debug("[SIDE_TRACK] Player active Pokemon set to: '{}'", new Object[]{speciesId});
               } else {
                  opponentPokemon.add(speciesId.toLowerCase(Locale.ROOT));
                  if (fullText != null) {
                     opponentPokemon.add(fullText.toLowerCase(Locale.ROOT));
                  }

                  BattleMessageColorizer.registerOpponentPokemon(speciesId);
                  CobblemonBattleInfoClient.debug("[SIDE_TRACK] Added to opponentPokemon: '{}', '{}'", new Object[]{speciesId, fullText});
                  currentActiveOpponentPokemon = speciesId;
                  CobblemonBattleInfoClient.debug("[SIDE_TRACK] Opponent active Pokemon set to: '{}'", new Object[]{speciesId});
               }

               Map<String, Integer> batonStats = pendingBatonPassStats.remove(switchSide);
               if (batonStats != null && !batonStats.isEmpty()) {
                  String newStatKey = buildPrimaryStatStageStorageKey(switchSide, speciesId, speciesId, fullText);
                  Map<String, Integer> newStats = statStages.computeIfAbsent(newStatKey, k -> new ConcurrentHashMap<>());
                  newStats.putAll(batonStats);
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] Baton Pass: Transferred stats to {}'s {}: {}", new Object[]{switchSide, speciesId, batonStats}
                  );
               } else {
                  clearStatStagesForPokemon(switchSide, speciesId, fullText);
               }
            }
         } else if (key.contains("switch.other") && args != null && args.length >= 2) {
            String speciesIdx = safeExtractPokemonSpeciesIdLowerFromArgs(args, 1);
            String fullTextx = safeExtractPlainText(args[1]);
            String trainerName = safeExtractPlainText(args[0]);
            CobblemonBattleInfoClient.debug("[SIDE_TRACK] switch.other args[0]='{}', args[1]='{}'", new Object[]{trainerName, fullTextx});
            if (speciesIdx != null) {
               String switchSidex = determineSideFromTrainerName(trainerName);
               if (switchSidex == null) {
                  switchSidex = "opponent";
               }

               if (fullTextx != null) {
                  String lowerFullText = fullTextx.toLowerCase(Locale.ROOT);
                  ownerTextToSide.put(lowerFullText, switchSidex);
                  CobblemonBattleInfoClient.debug(
                     "[SIDE_TRACK] Mapped {} from switch (direct): '{}'", new Object[]{switchSidex.toUpperCase(Locale.ROOT), lowerFullText}
                  );
               }

               if ("player".equals(switchSidex)) {
                  localPlayerPokemon.add(speciesIdx.toLowerCase(Locale.ROOT));
                  opponentPokemon.remove(speciesIdx.toLowerCase(Locale.ROOT));
                  if (fullTextx != null) {
                     localPlayerPokemon.add(fullTextx.toLowerCase(Locale.ROOT));
                  }

                  CobblemonBattleInfoClient.debug("[SIDE_TRACK] Added to localPlayerPokemon: '{}', '{}'", new Object[]{speciesIdx, fullTextx});
                  currentActivePlayerPokemon = speciesIdx;
                  CobblemonBattleInfoClient.debug("[SIDE_TRACK] Player active Pokemon set to: '{}'", new Object[]{speciesIdx});
               } else {
                  opponentPokemon.add(speciesIdx.toLowerCase(Locale.ROOT));
                  if (fullTextx != null) {
                     opponentPokemon.add(fullTextx.toLowerCase(Locale.ROOT));
                  }

                  BattleMessageColorizer.registerOpponentPokemon(speciesIdx);
                  CobblemonBattleInfoClient.debug("[SIDE_TRACK] Added to opponentPokemon: '{}', '{}'", new Object[]{speciesIdx, fullTextx});
                  currentActiveOpponentPokemon = speciesIdx;
                  CobblemonBattleInfoClient.debug("[SIDE_TRACK] Opponent active Pokemon set to: '{}'", new Object[]{speciesIdx});
               }

               Map<String, Integer> batonStats = pendingBatonPassStats.remove(switchSidex);
               if (batonStats != null && !batonStats.isEmpty()) {
                  String newStatKey = buildPrimaryStatStageStorageKey(switchSidex, speciesIdx, speciesIdx, fullTextx);
                  Map<String, Integer> newStats = statStages.computeIfAbsent(newStatKey, k -> new ConcurrentHashMap<>());
                  newStats.putAll(batonStats);
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] Baton Pass: Transferred stats to {}'s {}: {}", new Object[]{switchSidex, speciesIdx, batonStats}
                  );
               } else {
                  clearStatStagesForPokemon(switchSidex, speciesIdx, fullTextx);
               }
            }
         }

         String var30 = parts[2];
         switch (var30) {
            case "switch_in":
            case "sendout":
            case "switch":
               tryApplySwitchInFromArgs(parts, args);
               if (key.contains("switch.self") && args != null && args.length >= 1) {
                  BattleMessageColorizer.registerPlayerSwitchAction();
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Registered player switch action", new Object[0]);
               }
               break;
            case "withdraw":
            case "recall":
            case "return":
               tryApplySwitchOutFromArgs(parts, args);
               break;
            case "dragged_out":
               tryApplyForcedSwitchFromArgs(parts, args, key);
               break;
            case "ability":
               tryApplyAbilityPopupFromArgs(parts, args);
               if (args != null && args.length >= 2) {
                  String localizedAbilityName = safeExtractPlainText(args[1]);
                  String abilityId = null;
                  if (args[1] instanceof class_2561 cxxx && cxxx.method_10851() instanceof class_2588 tcxxx) {
                     String abilityKey = tcxxx.method_11022();
                     if (abilityKey != null && abilityKey.startsWith("cobblemon.ability.")) {
                        String[] abilityParts = abilityKey.split("\\.");
                        if (abilityParts.length >= 3) {
                           abilityId = abilityParts[2];
                        }
                     }
                  }

                  if (abilityId == null && localizedAbilityName != null) {
                     abilityId = localizedAbilityName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
                     CobblemonBattleInfoClient.debug(
                        "[BattleMessageSubscriber] Derived ability ID from name: '{}' -> '{}'", new Object[]{localizedAbilityName, abilityId}
                     );
                  }

                  if (localizedAbilityName != null && abilityId != null) {
                     BattleMessageColorizer.registerAbilityMetadata(localizedAbilityName, abilityId);
                     CobblemonBattleInfoClient.debug(
                        "[BattleMessageSubscriber] Registered ability: '{}' (id: '{}')", new Object[]{localizedAbilityName, abilityId}
                     );
                  }

                  RevealedPokemonContext abilityPokemon = resolveRevealedPokemonContext(args, 0);
                  String pokemonFullxxx = abilityPokemon.fullText();
                  String pokemonSpeciesx = abilityPokemon.speciesId();
                  String abilitySide = abilityPokemon.side();
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] Recording ability - Pokemon: '{}', Side: '{}', AbilityId: '{}', Display: '{}'",
                     new Object[]{pokemonSpeciesx, abilitySide, abilityId, localizedAbilityName}
                  );
                  if ("opponent".equals(abilitySide) && pokemonSpeciesx != null && abilityId != null && localizedAbilityName != null) {
                     RevealedBattleInfo.recordAbility(abilitySide, pokemonSpeciesx, abilityId, localizedAbilityName);
                     CobblemonBattleInfoClient.debug(
                        "[BattleMessageSubscriber] SUCCESS - Recorded ability for opponent: {} = {}", new Object[]{pokemonSpeciesx, localizedAbilityName}
                     );
                  } else {
                     CobblemonBattleInfoClient.debug(
                        "[BattleMessageSubscriber] SKIPPED recording - side='{}', species='{}', abilityId='{}', display='{}'",
                        new Object[]{abilitySide, pokemonSpeciesx, abilityId, localizedAbilityName}
                     );
                  }
               }
               break;
            case "terastallize":
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] TERA KEY RECEIVED: {} args={}", new Object[]{String.join(".", parts), args != null ? args.length : 0}
               );
               if (args != null) {
                  for (int ix = 0; ix < args.length; ix++) {
                     Object arg = args[ix];
                     String argStr = "null";
                     if (arg instanceof class_2561 cxxx) {
                        argStr = "Component{getString='" + cxxx.getString() + "'";
                        if (cxxx.method_10851() instanceof class_2588 tcxxx) {
                           argStr = argStr + ", key='" + tcxxx.method_11022() + "'";
                        }

                        argStr = argStr + "}";
                     } else if (arg != null) {
                        argStr = arg.getClass().getSimpleName() + "{" + arg + "}";
                     }

                     CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] TERA args[{}] = {}", new Object[]{ix, argStr});
                  }
               }

               tryApplyTerastallizationFromArgs(parts, args);
               break;
            case "mega":
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] MEGA KEY RECEIVED: {} args={}", new Object[]{String.join(".", parts), args != null ? args.length : 0}
               );
               if (args != null) {
                  for (int ix = 0; ix < args.length; ix++) {
                     Object arg = args[ix];
                     String argStr = "null";
                     if (arg instanceof class_2561 cxxx) {
                        argStr = "Component{getString='" + cxxx.getString() + "'";
                        if (cxxx.method_10851() instanceof class_2588 tcxxx) {
                           argStr = argStr + ", key='" + tcxxx.method_11022() + "'";
                        }

                        argStr = argStr + "}";
                     } else if (arg != null) {
                        argStr = arg.getClass().getSimpleName() + "{" + arg + "}";
                     }

                     CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] MEGA args[{}] = {}", new Object[]{ix, argStr});
                  }
               }

               tryApplyMegaEvolutionFromArgs(parts, args);
               break;
            case "dynamax":
            case "gigantamax":
            case "gmax":
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] DYNAMAX KEY RECEIVED: {} args={}", new Object[]{String.join(".", parts), args != null ? args.length : 0}
               );
               tryApplyDynamaxFromArgs(parts, args);
               break;
            case "formechange":
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] FORMECHANGE KEY: {} args={}", new Object[]{String.join(".", parts), args != null ? args.length : 0}
               );
               String formeChangePokemon = null;
               if (args != null && args.length >= 1) {
                  formeChangePokemon = safeExtractPokemonSpeciesIdLowerFromArgs(args, 0);
                  if (formeChangePokemon == null && args.length >= 2) {
                     formeChangePokemon = safeExtractPokemonSpeciesIdLowerFromArgs(args, 1);
                  }
               }

               boolean isNonMegaFormChange = formeChangePokemon != null
                  && (
                     formeChangePokemon.contains("meloetta")
                        || formeChangePokemon.contains("darmanitan")
                        || formeChangePokemon.contains("aegislash")
                        || formeChangePokemon.contains("wishiwashi")
                        || formeChangePokemon.contains("minior")
                        || formeChangePokemon.contains("mimikyu")
                        || formeChangePokemon.contains("morpeko")
                        || formeChangePokemon.contains("eiscue")
                        || formeChangePokemon.contains("zygarde")
                        || formeChangePokemon.contains("castform")
                        || formeChangePokemon.contains("cherrim")
                  );
               if (isNonMegaFormChange) {
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] FORMECHANGE: Skipping non-mega form change for {}", new Object[]{formeChangePokemon}
                  );
                  if (formeChangePokemon.contains("meloetta")) {
                     String pokemonFullxx = safeExtractPlainText(args[0]);
                     String sidex = pokemonFullxx != null ? ownerTextToSide.get(pokemonFullxx.toLowerCase(Locale.ROOT)) : null;
                     if (sidex == null && pokemonFullxx != null) {
                        sidex = pokemonFullxx.contains("'s ") ? "player" : "opponent";
                     }

                     String storageKey = sidex != null ? makeSidedKey(sidex, "meloetta") : "meloetta";
                     String formArg = args.length >= 2 && args[1] != null ? args[1].toString().toLowerCase().trim() : "";
                     if (formArg.contains("pirouette")) {
                        meloettaForm.put(storageKey, "pirouette");
                        CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Meloetta changed to Pirouette form [key: {}]", new Object[]{storageKey});
                     } else if (formArg.contains("aria")) {
                        meloettaForm.put(storageKey, "aria");
                        CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Meloetta changed to Aria form [key: {}]", new Object[]{storageKey});
                     }
                  }
               } else {
                  if (args != null) {
                     for (int i = 0; i < args.length; i++) {
                        Object arg = args[i];
                        String argStr = "null";
                        if (arg instanceof class_2561 cxxx) {
                           argStr = "Component{getString='" + cxxx.getString() + "'";
                           if (cxxx.method_10851() instanceof class_2588 tcxxx) {
                              argStr = argStr + ", key='" + tcxxx.method_11022() + "'";
                           }

                           argStr = argStr + "}";
                        } else if (arg != null) {
                           argStr = arg.getClass().getSimpleName() + "{" + arg + "}";
                        }

                        CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] FORMECHANGE args[{}] = {}", new Object[]{i, argStr});
                     }
                  }

                  String megaForm = null;
                  if (args != null && args.length >= 2 && args[1] != null) {
                     String formArg = args[1].toString().toLowerCase().trim();
                     if (formArg.equals("x")) {
                        megaForm = "x";
                     } else if (formArg.equals("y")) {
                        megaForm = "y";
                     }
                  }

                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] FORMECHANGE detected form from args[1]: {}", new Object[]{megaForm});
                  tryApplyMegaEvolutionFromArgsWithForm(parts, args, megaForm);
               }
               break;
            case "fainted":
               if (args != null && args.length >= 1) {
                  String speciesIdxx = safeExtractPokemonSpeciesIdLowerFromArgs(args, 0);
                  String pokemonFullx = safeExtractPlainText(args[0]);
                  String side = null;
                  if (pokemonFullx != null) {
                     side = ownerTextToSide.get(pokemonFullx.toLowerCase(Locale.ROOT));
                     if (side == null && speciesIdxx != null) {
                        side = ownerTextToSide.get(speciesIdxx);
                     }
                  }

                  if (speciesIdxx != null && !speciesIdxx.isBlank()) {
                     String sidedKey = side != null ? makeSidedKey(side, speciesIdxx) : null;
                     boolean removed = false;
                     if (sidedKey != null) {
                        removed = megaEvolved.remove(sidedKey) != null;
                        statStages.remove(sidedKey);
                     }

                     if (!removed) {
                        megaEvolved.remove(speciesIdxx);
                     }

                     statStages.remove(speciesIdxx);
                     CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Fainted: Cleared status for '{}' (side: {})", new Object[]{speciesIdxx, side});
                  }
               }
               break;
            case "turn":
               int newTurn = 0;
               if (parts.length >= 4) {
                  try {
                     newTurn = Integer.parseInt(parts[3]);
                  } catch (NumberFormatException var21) {
                  }
               }

               if (newTurn <= 0) {
                  newTurn = Math.max(currentTurnNumber + 1, 1);
               }

               if (newTurn > lastTurnNumber) {
                  currentTurnNumber = newTurn;
                  TurnCounterRenderer.setTurn(newTurn);
                  lastTurnNumber = newTurn;
               }

               decrementScreenTurns("player");
               decrementScreenTurns("opponent");
               decrementHazardTurnsIfAny();
               decrementTailwindTurns("player");
               decrementTailwindTurns("opponent");
               decrementWeatherTurnsIfActive();
               decrementTerrainTurnsIfActive();
               decrementRoomTurnsIfActive();
               decrementPerishSongTurnsIfActive();
               decrementGravityTurns();
               decrementEmbargoTurns();
               decrementHealBlockTurns();
               break;
            case "weather":
               if (parts.length >= 5) {
                  String weatherId = parts[3];
                  String action = parts[4];
                  if ("start".equals(action)) {
                     setWeatherFromId(weatherId);
                     if (args != null && args.length > 0) {
                        BattleMessageColorizer.registerWeatherMetadata(weatherId);
                     }
                  } else if ("end".equals(action)) {
                     currentWeather = "";
                     weatherTurns = 0;
                     weatherStartTurn = 0;
                  } else if ("upkeep".equals(action) && weatherTurns == 0 && !hasWeatherRock) {
                     hasWeatherRock = true;
                     weatherTurns = 3;
                     if (currentWeather.isEmpty()) {
                        setWeatherFromId(weatherId);
                        weatherTurns = 3;
                     }
                  }
               }
               break;
            case "fieldstart":
               if (parts.length >= 4) {
                  String effect = parts[3];
                  if (effect.contains("terrain")) {
                     setTerrainFromId(effect);
                  } else {
                     setRoomFromId(effect);
                  }
               }
               break;
            case "fieldactivate":
               if (parts.length >= 4) {
                  activateFieldEffectFromId(parts[3]);
               }
               break;
            case "fieldend":
               if (parts.length >= 4) {
                  String effect = parts[3];
                  if (effect.contains("terrain")) {
                     currentTerrain = "";
                     terrainTurns = 0;
                     terrainStartTurn = 0;
                  } else {
                     clearRoomFromId(effect);
                  }
               }
               break;
            case "sidestart":
               if (parts.length >= 5) {
                  String side = determineSideFromPartsOrArgs(parts, args);
                  String effectId = parts[4];
                  if (!applySideEffectFromId(side, effectId)) {
                     applyHazardFromId(side, effectId);
                  }
               }
               break;
            case "sideend":
               if (parts.length >= 5) {
                  String side = determineSideFromPartsOrArgs(parts, args);
                  String effectId = parts[4];
                  if (!removeSideEffectFromId(side, effectId)) {
                     removeHazard(side, hazardDisplayName(effectId));
                  }
               }
               break;
            case "boost":
            case "unboost":
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] ========== STAT BOOST/UNBOOST ==========", new Object[0]);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Key: {}, isBoost: {}", new Object[]{key, "boost".equals(parts[2])});
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Parts: {}", new Object[]{Arrays.toString((Object[])parts)});
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Args count: {}", new Object[]{args == null ? 0 : args.length});
               if (args != null) {
                  for (int i = 0; i < args.length; i++) {
                     Object arg = args[i];
                     CobblemonBattleInfoClient.debug(
                        "[BattleMessageSubscriber]   arg[{}] type: {}", new Object[]{i, arg == null ? "null" : arg.getClass().getName()}
                     );
                     CobblemonBattleInfoClient.debug("[BattleMessageSubscriber]   arg[{}] value: {}", new Object[]{i, safeExtractPlainText(arg)});
                     if (arg instanceof class_2561) {
                        class_2561 cxxx = (class_2561)arg;
                        if (cxxx.method_10851() instanceof class_2588 tcxxx) {
                           CobblemonBattleInfoClient.debug("[BattleMessageSubscriber]   arg[{}] translation key: {}", new Object[]{i, tcxxx.method_11022()});
                        }
                     }
                  }
               }

               tryApplyStatBoost(parts, args, "boost".equals(parts[2]));
               break;
            case "setboost":
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] ========== SETBOOST ==========", new Object[0]);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Key: {}", new Object[]{key});
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Parts: {}", new Object[]{Arrays.toString((Object[])parts)});
               tryApplySetBoost(parts, args);
               break;
            case "clearallboost":
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] ========== CLEARALLBOOST (Haze) ==========", new Object[0]);
               statStages.clear();
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Cleared all stat stages", new Object[0]);
               break;
            case "clearboost":
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] ========== CLEARBOOST ==========", new Object[0]);
               if (args != null && args.length >= 1) {
                  String pokemonFull = safeExtractPlainText(args[0]);
                  String speciesIdx = safeExtractPokemonNameFromArgs(args, 0);
                  if (speciesIdx == null && pokemonFull != null) {
                     speciesIdx = stripOwnerPrefix(pokemonFull);
                  }

                  if (speciesIdx != null) {
                     String pokemon = stripOwnerPrefix(speciesIdx).toLowerCase(Locale.ROOT);
                     String side = determineSideFromFullText(pokemonFull, pokemon);
                     String statKey = side != null ? makeSidedKey(side, pokemon) : pokemon;
                     statStages.remove(statKey);
                     CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Cleared stat stages for {} (key: {})", new Object[]{pokemon, statKey});
                  }
               }
               break;
            case "copyboost":
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] ========== COPYBOOST (Psych Up) ==========", new Object[0]);
               if (args == null || args.length < 2) {
                  break;
               }

               String userFullx = safeExtractPlainText(args[0]);
               String targetFull = safeExtractPlainText(args[1]);
               String userSpeciesxx = safeExtractPokemonNameFromArgs(args, 0);
               String targetSpeciesx = safeExtractPokemonNameFromArgs(args, 1);
               if (userSpeciesxx == null && userFullx != null) {
                  userSpeciesxx = stripOwnerPrefix(userFullx);
               }

               if (targetSpeciesx == null && targetFull != null) {
                  targetSpeciesx = stripOwnerPrefix(targetFull);
               }

               if (userSpeciesxx == null || targetSpeciesx == null) {
                  break;
               }

               String userPokemon = stripOwnerPrefix(userSpeciesxx).toLowerCase(Locale.ROOT);
               String targetPokemon = stripOwnerPrefix(targetSpeciesx).toLowerCase(Locale.ROOT);
               String userSidex = determineSideFromFullText(userFullx, userPokemon);
               String targetSidex = determineSideFromFullText(targetFull, targetPokemon);
               String userKey = buildPrimaryStatStageStorageKey(userSidex, userPokemon, userSpeciesxx, userFullx);
               String targetKey = buildPrimaryStatStageStorageKey(targetSidex, targetPokemon, targetSpeciesx, targetFull);
               if (userKey != null && !userKey.isBlank() && targetKey != null && !targetKey.isBlank()) {
                  Map<String, Integer> targetStats = statStages.get(targetKey);
                  if (targetStats != null && !targetStats.isEmpty()) {
                     Map<String, Integer> userStats = statStages.computeIfAbsent(userKey, k -> new ConcurrentHashMap<>());
                     userStats.clear();
                     userStats.putAll(targetStats);
                     CobblemonBattleInfoClient.debug(
                        "[BattleMessageSubscriber] Psych Up: Copied stats from {} to {} (targetKey: {}, userKey: {})",
                        new Object[]{targetPokemon, userPokemon, targetKey, userKey}
                     );
                  } else {
                     CobblemonBattleInfoClient.debug(
                        "[BattleMessageSubscriber] Psych Up: No stats to copy from {} (targetKey: {})", new Object[]{targetPokemon, targetKey}
                     );
                  }
                  break;
               }

               return;
            case "swapboost":
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] ========== SWAPBOOST ==========", new Object[0]);
               tryApplySwapBoost(parts, args);
               break;
            case "invertboost":
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] ========== INVERTBOOST (Topsy-Turvy) ==========", new Object[0]);
               tryApplyInvertBoost(args);
               break;
            case "used_move":
            case "used_move_on":
               if (args != null && args.length >= 2) {
                  String localizedMoveName = safeExtractPlainText(args[1]);
                  String localizedMoveLine = class_2561.method_43469(key, args).getString();
                  RevealedPokemonContext moveUser = resolveRevealedPokemonContext(args, 0);
                  String userFull = moveUser.fullText();
                  String userSpeciesx = moveUser.speciesId();
                  String moveSide = moveUser.side();
                  String moveId = null;
                  String moveType = null;
                  if (args[1] instanceof class_2561 cxx && cxx.method_10851() instanceof class_2588 tcxx) {
                     String moveKey = tcxx.method_11022();
                     if (moveKey != null && moveKey.startsWith("cobblemon.move.")) {
                        String[] moveParts = moveKey.split("\\.");
                        if (moveParts.length >= 3) {
                           moveId = moveParts[2].toLowerCase(Locale.ROOT);
                           moveType = getMoveTypeFromId(moveId);
                        }
                     }
                  }

                  boolean hiddenPowerMove = isHiddenPowerMoveId(moveId);
                  String hiddenPowerAttackerKey = hiddenPowerMove ? buildHiddenPowerAttackerKey(moveSide, userFull, userSpeciesx) : null;
                  String inferredHiddenPowerType = hiddenPowerAttackerKey == null ? null : hiddenPowerResolvedTypes.get(hiddenPowerAttackerKey);
                  if (localizedMoveName != null && moveType != null) {
                     BattleMessageColorizer.registerMoveMetadata(localizedMoveName, moveType, moveId);
                     CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Registered move: '{}' -> '{}'", new Object[]{localizedMoveName, moveType});
                  } else if (localizedMoveName != null && hiddenPowerMove) {
                     BattleMessageColorizer.removeMoveMetadata(localizedMoveName);
                     if (inferredHiddenPowerType != null && localizedMoveLine != null && !localizedMoveLine.isBlank()) {
                        BattleMessageColorizer.registerMoveLineOverride(localizedMoveLine, localizedMoveName, inferredHiddenPowerType, moveId);
                        CobblemonBattleInfoClient.debug(
                           "[BattleMessageSubscriber] Applied inferred Hidden Power type '{}' to '{}'.",
                           new Object[]{inferredHiddenPowerType, localizedMoveLine}
                        );
                     } else {
                        CobblemonBattleInfoClient.debug(
                           "[BattleMessageSubscriber] Hidden Power move '{}' is still unresolved on the client; leaving it uncolored until effectiveness inference narrows the subtype.",
                           new Object[]{localizedMoveName}
                        );
                     }
                  } else {
                     CobblemonBattleInfoClient.debug(
                        "[BattleMessageSubscriber] Could not register move: '{}' (moveId: '{}', moveType: '{}')",
                        new Object[]{localizedMoveName, moveId, moveType}
                     );
                  }

                  if (hiddenPowerMove) {
                     trackHiddenPowerUsage(hiddenPowerAttackerKey, localizedMoveName, localizedMoveLine, moveId, args, moveSide);
                  } else {
                     pendingHiddenPowerContext = null;
                  }

                  if (moveId == null || !"heartswap".equals(moveId) && !"powerswap".equals(moveId) && !"guardswap".equals(moveId)) {
                     pendingSwapBoostContext = null;
                  } else {
                     trackSwapBoostUsage(moveId, args);
                  }

                  if (moveSide != null && !moveSide.isBlank() && isDynamaxMoveId(moveId)) {
                     GimmickUsageTracker.markGimmickUsedForSide(moveSide, "dynamax");
                     CobblemonBattleInfoClient.debug(
                        "[BattleMessageSubscriber] Dynamax usage fallback from move '{}' on side '{}'", new Object[]{moveId, moveSide}
                     );
                  }

                  if ("opponent".equals(moveSide) && userSpeciesx != null && moveId != null && localizedMoveName != null) {
                     RevealedBattleInfo.recordMove(moveSide, userSpeciesx, moveId, localizedMoveName, moveType);
                  }

                  if (moveId != null && "batonpass".equals(moveId.toLowerCase())) {
                     String batonSide = determineSideFromFullText(userFull, userSpeciesx);
                     if (batonSide != null && userSpeciesx != null) {
                        String statKey = buildPrimaryStatStageStorageKey(batonSide, userSpeciesx, userSpeciesx, userFull);
                        Map<String, Integer> currentStats = statStages.get(statKey);
                        if (currentStats != null && !currentStats.isEmpty()) {
                           pendingBatonPassStats.put(batonSide, new ConcurrentHashMap<>(currentStats));
                           CobblemonBattleInfoClient.debug(
                              "[BattleMessageSubscriber] Baton Pass: Saved stats for {} side: {}", new Object[]{batonSide, currentStats}
                           );
                        } else {
                           pendingBatonPassStats.put(batonSide, new ConcurrentHashMap<>());
                           CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Baton Pass: No stats to pass for {} side", new Object[]{batonSide});
                        }
                     }
                  }

                  if (moveId != null) {
                     String moveIdLower = moveId.toLowerCase();
                     if ("burnup".equals(moveIdLower) || "doubleshock".equals(moveIdLower)) {
                        String burnSide = determineSideFromFullText(userFull, userSpeciesx);
                        if (burnSide != null && userSpeciesx != null) {
                           String storageKey = makeSidedKey(burnSide, userSpeciesx);
                           if ("burnup".equals(moveIdLower)) {
                              burnedUp.put(storageKey, true);
                              CobblemonBattleInfoClient.debug(
                                 "[BattleMessageSubscriber] {} used Burn Up - lost Fire type! (key: {}, side: {})",
                                 new Object[]{userSpeciesx, storageKey, burnSide}
                              );
                           } else {
                              doubleShocked.put(storageKey, true);
                              CobblemonBattleInfoClient.debug(
                                 "[BattleMessageSubscriber] {} used Double Shock - lost Electric type! (key: {}, side: {})",
                                 new Object[]{userSpeciesx, storageKey, burnSide}
                              );
                           }

                           lastBurnUpDoubleShockSide.put(userSpeciesx, burnSide);
                        }
                     }
                  }

                  if (localizedMoveName != null && args.length >= 3) {
                     String moveLower = localizedMoveName.toLowerCase().replace(" ", "");
                     boolean isGroundingMove = false;
                     if (moveId != null) {
                        String moveIdLower = moveId.toLowerCase();
                        isGroundingMove = "smackdown".equals(moveIdLower) || "thousandarrows".equals(moveIdLower);
                     }

                     if (!isGroundingMove) {
                        isGroundingMove = "smackdown".equals(moveLower) || "thousandarrows".equals(moveLower);
                     }

                     if (isGroundingMove) {
                        String targetName = safeExtractPokemonNameFromArgs(args, 2);
                        if (targetName != null && !targetName.isBlank()) {
                           targetName = stripOwnerPrefix(targetName);
                           String lowerName = targetName.toLowerCase();
                           smackDownGrounded.put(lowerName, true);
                           magnetRise.remove(lowerName);
                           telekinesis.remove(lowerName);
                           CobblemonBattleInfoClient.debug(
                              "[BattleMessageSubscriber] {} was hit by {}! Now grounded. [from used_move_on]", new Object[]{targetName, localizedMoveName}
                           );
                        }
                     }
                  }
               }
               break;
            case "start":
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] START detected - Full key: {}, parts.length: {}, args.length: {}",
                  new Object[]{key, parts.length, args == null ? 0 : args.length}
               );
               if (parts.length >= 4) {
                  String effectId = parts[3];
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Effect ID: {}", new Object[]{effectId});
               }

               for (int i = 0; i < parts.length; i++) {
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber]   parts[{}]: {}", new Object[]{i, parts[i]});
               }

               if (parts.length >= 4 && args != null && args.length >= 1) {
                  String effectId = parts[3];
                  String normalizedEffectId = effectId.toLowerCase(Locale.ROOT);
                  if ("dynamax".equals(normalizedEffectId) || "gmax".equals(normalizedEffectId) || "gigantamax".equals(normalizedEffectId)) {
                     tryApplyDynamaxFromArgs(parts, args);
                  }

                  cobblemon_battle_extras$tryApplyAbilityPopupFromStartEffect(effectId, args);
                  String localizedMessage = class_2561.method_43469(key, args).getString();
                  String statusType = null;
                  String var134 = effectId.toLowerCase();
                  switch (var134) {
                     case "brn":
                        statusType = "burn";
                        break;
                     case "psn":
                        statusType = "poison";
                        break;
                     case "tox":
                        statusType = "badly poisoned";
                        break;
                     case "par":
                        statusType = "paralysis";
                        break;
                     case "slp":
                        statusType = "sleep";
                        break;
                     case "frz":
                        statusType = "freeze";
                        break;
                     case "confusion":
                        statusType = "confusion";
                  }

                  if (statusType != null && localizedMessage != null) {
                     BattleMessageColorizer.registerStatusEffectMetadata(localizedMessage, statusType);
                  }

                  if ("focusenergy".equals(effectId)) {
                     tryApplyFocusEnergy(args);
                  } else {
                     tryApplyVolatileStatusFromArgs(effectId, args);
                  }
               }
               break;
            case "end":
               if (parts.length >= 4 && args != null && args.length >= 1) {
                  String effectId = parts[3];
                  tryRemoveVolatileStatusFromArgs(effectId, args);
               }
               break;
            case "move":
               if (parts.length >= 4 && args != null && args.length >= 1) {
                  String moveName = parts[3].toLowerCase();
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Move message detected: {}", new Object[]{moveName});
                  if ("smackdown".equals(moveName) || "thousandarrows".equals(moveName)) {
                     String targetName = safeExtractPokemonNameFromArgs(args, args.length > 1 ? 1 : 0);
                     if (targetName != null && !targetName.isBlank()) {
                        targetName = stripOwnerPrefix(targetName);
                        String lowerName = targetName.toLowerCase();
                        smackDownGrounded.put(lowerName, true);
                        magnetRise.remove(lowerName);
                        telekinesis.remove(lowerName);
                        CobblemonBattleInfoClient.debug(
                           "[BattleMessageSubscriber] {} was hit by {}! Now grounded. [from move message]", new Object[]{targetName, moveName}
                        );
                     }
                  }
               }
               break;
            case "singleturn":
               if (parts.length >= 4 && args != null && args.length >= 1) {
                  String effectId = parts[3].toLowerCase();
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] Singleturn message detected: {} (turn: {})", new Object[]{effectId, lastTurnNumber}
                  );
                  if ("roost".equals(effectId)) {
                     String pokemonName = safeExtractPokemonNameFromArgs(args, 0);
                     if (pokemonName != null && !pokemonName.isBlank()) {
                        String pokemonFull = safeExtractPlainText(args[0]);
                        pokemonName = stripOwnerPrefix(pokemonName);
                        String lowerName = pokemonName.toLowerCase();
                        String side = pokemonFull != null ? ownerTextToSide.get(pokemonFull.toLowerCase(Locale.ROOT)) : null;
                        if (side == null && pokemonFull != null) {
                           if (pokemonFull.contains("'s ")) {
                              side = "player";
                           } else if (localPlayerPokemon.contains(lowerName)) {
                              side = "player";
                           } else {
                              side = "opponent";
                           }
                        }

                        String storageKey = side != null ? makeSidedKey(side, lowerName) : lowerName;
                        roosted.put(storageKey, lastTurnNumber);
                        CobblemonBattleInfoClient.debug(
                           "[BattleMessageSubscriber] {} used Roost (loses Flying type for turn {}) [from singleturn args] (key: {}, side: {})",
                           new Object[]{pokemonName, lastTurnNumber, storageKey, side}
                        );
                     }
                  }
               }
               break;
            case "activate":
               if (parts.length >= 4 && args != null && args.length >= 1) {
                  String effectId = parts[3].toLowerCase();
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Activate message detected: {}", new Object[]{effectId});
                  if ("smackdown".equals(effectId) || "gravity".equals(effectId)) {
                     String pokemonName = safeExtractPokemonNameFromArgs(args, 0);
                     if (pokemonName != null && !pokemonName.isBlank()) {
                        pokemonName = stripOwnerPrefix(pokemonName);
                        String lowerName = pokemonName.toLowerCase();
                        smackDownGrounded.put(lowerName, true);
                        magnetRise.remove(lowerName);
                        telekinesis.remove(lowerName);
                        CobblemonBattleInfoClient.debug(
                           "[BattleMessageSubscriber] {} grounded by {}! [from activate message]", new Object[]{pokemonName, effectId}
                        );
                     }
                  } else if ("infestation".equals(effectId)) {
                     tryApplyVolatileStatusFromArgs(effectId, args);
                  }
               }
               break;
            case "item":
            case "enditem":
               if (parts.length >= 4 && args != null && args.length >= 1) {
                  String itemIdx = parts[3].toLowerCase();
                  boolean isEndItem = "enditem".equals(parts[2]);
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] Item message detected: {} (parts[2]: {}, isEndItem: {})", new Object[]{itemIdx, parts[2], isEndItem}
                  );
                  RevealedPokemonContext itemPokemon = resolveRevealedPokemonContext(args, 0);
                  String pokemonSpecies = itemPokemon.speciesId();
                  String itemSide = itemPokemon.side();
                  String localizedItemName = null;
                  if (args.length >= 2) {
                     localizedItemName = safeExtractPlainText(args[1]);
                  }

                  if (localizedItemName == null || localizedItemName.isBlank()) {
                     localizedItemName = formatItemName(itemIdx);
                  }

                  if ("opponent".equals(itemSide) && pokemonSpecies != null && itemIdx != null && localizedItemName != null) {
                     RevealedBattleInfo.recordHeldItem(itemSide, pokemonSpecies, itemIdx, localizedItemName);
                  }

                  if (isEndItem && "opponent".equals(itemSide) && pokemonSpecies != null) {
                     RevealedBattleInfo.markHeldItemConsumed(itemSide, pokemonSpecies);
                  }

                  String normalizedItemId = itemIdx != null ? itemIdx.replace("_", "") : "";
                  if ("whiteherb".equals(normalizedItemId) && pokemonSpecies != null) {
                     String sidex = itemSide;
                     if (itemSide == null) {
                        sidex = determineSideFromBattle(pokemonSpecies);
                     }

                     String statKey = sidex != null ? makeSidedKey(sidex, pokemonSpecies) : pokemonSpecies;
                     clearNegativeStatStages(statKey, pokemonSpecies);
                  }
               }
               break;
            case "frisk":
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Frisk message detected", new Object[0]);
               if (args != null && args.length >= 3) {
                  RevealedPokemonContext friskUser = resolveRevealedPokemonContext(args, 0);
                  String userSpecies = friskUser.speciesId();
                  String userSide = friskUser.side();
                  RevealedPokemonContext friskTarget = resolveRevealedPokemonContext(args, 1);
                  String targetSpecies = friskTarget.speciesId();
                  String targetSide = friskTarget.side();
                  String itemName = safeExtractPlainText(args[2]);
                  String itemId = null;
                  if (args[2] instanceof class_2561 cx && cx.method_10851() instanceof class_2588 tcx) {
                     String itemKey = tcx.method_11022();
                     if (itemKey != null) {
                        String[] itemParts = itemKey.split("\\.");
                        if (itemParts.length >= 3) {
                           itemId = String.join("_", Arrays.copyOfRange(itemParts, 2, itemParts.length));
                           if (itemId.startsWith("held_item_")) {
                              itemId = itemId.substring("held_item_".length());
                           }
                        }
                     }
                  }

                  if (itemId == null && itemName != null) {
                     itemId = itemName.toLowerCase().replace(" ", "_");
                  }

                  if (userSide != null) {
                     String oppositeSide = "player".equals(userSide) ? "opponent" : "player";
                     if (targetSide == null || targetSide.equals(userSide)) {
                        targetSide = oppositeSide;
                     }
                  }

                  if ("opponent".equals(targetSide) && targetSpecies != null && itemId != null && itemName != null) {
                     RevealedBattleInfo.recordHeldItem(targetSide, targetSpecies, itemId, itemName);
                  }
               }
               break;
            case "supereffective":
            case "resisted":
            case "immune":
            case "miss":
            case "fail":
            case "damage":
            case "hitcount":
               String localizedMessagex = class_2561.method_43469(key, args).getString();
               String var63 = parts[2];
               switch (var63) {
                  case "supereffective":
                     BattleMessageColorizer.registerEffectivenessMetadata(localizedMessagex, "super");
                     break;
                  case "resisted":
                     BattleMessageColorizer.registerEffectivenessMetadata(localizedMessagex, "resisted");
                     break;
                  case "immune":
                     BattleMessageColorizer.registerEffectivenessMetadata(localizedMessagex, "immune");
                     break;
                  case "miss":
                  case "fail":
                     BattleMessageColorizer.registerMissMetadata(localizedMessagex);
               }

               if ("supereffective".equals(parts[2]) || "resisted".equals(parts[2])) {
                  tryResolvePendingHiddenPowerFromEffect(parts[2]);
               } else if ("immune".equals(parts[2])
                  || "miss".equals(parts[2])
                  || "fail".equals(parts[2])
                  || "damage".equals(parts[2])
                  || "hitcount".equals(parts[2])) {
                  pendingHiddenPowerContext = null;
               }
               break;
            default:
               if (key != null && key.contains("cobblemon.battle")) {
                  String localizedMessage = class_2561.method_43469(key, args).getString();
                  String lowerKey = key.toLowerCase();
                  String lowerMessage = localizedMessage.toLowerCase();
                  if (lowerKey.contains("dynamax") || lowerKey.contains("gigantamax") || lowerKey.contains("gmax")) {
                     tryApplyDynamaxFromArgs(parts, args);
                  }

                  if (lowerKey.contains("supereffective") || lowerKey.contains("super.effective")) {
                     BattleMessageColorizer.registerEffectivenessMetadata(localizedMessage, "super");
                  } else if (lowerKey.contains("resisted") || lowerKey.contains("not.very.effective") || lowerKey.contains("notveryeffective")) {
                     BattleMessageColorizer.registerEffectivenessMetadata(localizedMessage, "resisted");
                  } else if (lowerKey.contains("immune") || lowerKey.contains("no.effect") || lowerKey.contains("noeffect")) {
                     BattleMessageColorizer.registerEffectivenessMetadata(localizedMessage, "immune");
                  } else if (lowerKey.contains("miss") || lowerKey.contains("fail") || lowerKey.contains("avoid")) {
                     BattleMessageColorizer.registerMissMetadata(localizedMessage);
                  } else if (lowerKey.contains("critical") || lowerKey.contains("crit")) {
                     BattleMessageColorizer.registerCriticalHitMetadata(localizedMessage);
                  }

                  if (lowerKey.contains(".brn") || lowerMessage.contains("burn")) {
                     BattleMessageColorizer.registerStatusEffectMetadata(localizedMessage, "burn");
                  } else if (lowerKey.contains(".psn") || lowerKey.contains(".tox") || lowerMessage.contains("poison")) {
                     BattleMessageColorizer.registerStatusEffectMetadata(localizedMessage, "poison");
                  } else if (lowerKey.contains(".par") || lowerMessage.contains("paralyz")) {
                     BattleMessageColorizer.registerStatusEffectMetadata(localizedMessage, "paralysis");
                  } else if (lowerKey.contains(".slp") || lowerMessage.contains("asleep") || lowerMessage.contains("sleep")) {
                     BattleMessageColorizer.registerStatusEffectMetadata(localizedMessage, "sleep");
                  } else if (lowerKey.contains(".frz") || lowerMessage.contains("frozen") || lowerMessage.contains("freeze")) {
                     BattleMessageColorizer.registerStatusEffectMetadata(localizedMessage, "freeze");
                  }
               }
         }
      } catch (Throwable var23) {
      }
   }

   private static void tryApplyAbilityPopupFromArgs(String[] parts, Object[] args) {
      try {
         if (args == null || args.length < 2) {
            return;
         }

         class_2561 pokemonComponent = cobblemon_battle_extras$resolvePokemonComponent(args, 0);
         class_2561 abilityComponent = null;
         String abilityId = null;
         if (args[1] instanceof class_2561) {
            abilityComponent = (class_2561)args[1];
            if (abilityComponent.method_10851() instanceof class_2588 tc) {
               String key = tc.method_11022();
               if (key != null && key.startsWith("cobblemon.ability.")) {
                  String[] p = key.split("\\.");
                  if (p.length >= 3) {
                     abilityId = p[p.length - 1];
                  }
               }
            }
         } else if (args[1] instanceof String) {
            abilityId = cobblemon_battle_extras$normalizeIdentifier((String)args[1]);
         }

         String abilityFallbackName = safeExtractAbilityNameFromArgs(args, 1);
         if (abilityId == null || abilityId.isBlank()) {
            abilityId = cobblemon_battle_extras$normalizeIdentifier(abilityFallbackName);
         }

         if (abilityComponent == null || cobblemon_battle_extras$rendersAsRawTranslationKey(abilityComponent)) {
            abilityComponent = cobblemon_battle_extras$createAbilityComponent(abilityId, abilityFallbackName);
         }

         String pokemonName = safeExtractPokemonNameFromArgs(args, 0);
         if (pokemonName == null || pokemonName.isBlank()) {
            pokemonName = safeExtractPlainText(args[0]);
         }

         if (pokemonName == null || pokemonName.isBlank()) {
            pokemonName = "Pokemon";
         }

         if (abilityId == null || abilityId.isBlank()) {
            abilityId = "ability";
         }

         if (shouldShowAbilityPopup(pokemonName, abilityId)) {
            if (pokemonComponent != null && abilityComponent != null) {
               AbilityPopupRenderer.showAbilityPopup(pokemonComponent, abilityComponent);
            }

            markAbilityPopupShown(pokemonName, abilityId);
            markAbilityPopupShownFromKey();
         }
      } catch (Throwable var8) {
      }
   }

   private static void tryApplySwitchInFromArgs(String[] parts, Object[] args) {
      try {
         if (args == null || args.length < 2) {
            return;
         }

         String trainerName = safeExtractPlainText(args[0]);
         String pokemonName = safeExtractPokemonNameFromArgs(args, 1);
         if (pokemonName == null || pokemonName.isBlank()) {
            return;
         }

         String resolvedSide = determineSideFromTrainerName(trainerName);
         if (resolvedSide == null) {
            resolvedSide = determineSideFromBattleCandidates(trainerName, pokemonName);
         }

         if (resolvedSide == null) {
            resolvedSide = "opponent";
         }

         if ("player".equals(resolvedSide)) {
            localPlayerPokemon.add(pokemonName.toLowerCase());
            opponentPokemon.remove(pokemonName.toLowerCase());
            currentActivePlayerPokemon = pokemonName;
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Tracked player Pokemon (switch-in via TK): {} (now active)", new Object[]{pokemonName});
         } else if (!localPlayerPokemon.contains(pokemonName.toLowerCase())) {
            opponentPokemon.add(pokemonName.toLowerCase());
            BattleMessageColorizer.registerOpponentPokemon(pokemonName);
            currentActiveOpponentPokemon = pokemonName;
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Tracked opponent Pokemon (switch-in via TK): {} (now active)", new Object[]{pokemonName});
         }

         Map<String, Integer> batonStats = pendingBatonPassStats.remove(resolvedSide);
         if (batonStats != null && !batonStats.isEmpty()) {
            String newStatKey = buildPrimaryStatStageStorageKey(resolvedSide, pokemonName, pokemonName);
            Map<String, Integer> newStats = statStages.computeIfAbsent(newStatKey, k -> new ConcurrentHashMap<>());
            newStats.putAll(batonStats);
         } else {
            clearStatStagesForPokemon(resolvedSide, pokemonName, pokemonName);
         }
      } catch (Throwable var8) {
      }
   }

   private static void tryApplySwitchOutFromArgs(String[] parts, Object[] args) {
      try {
         if (args == null || args.length < 1) {
            return;
         }

         int pokemonArgIndex = args.length >= 2 ? 1 : 0;
         String pokemonSpecies = null;
         String pokemonFullText = null;
         if (args.length >= 2) {
            pokemonSpecies = safeExtractPokemonNameFromArgs(args, 1);
            pokemonFullText = safeExtractPlainText(args[1]);
         }

         if (pokemonSpecies == null || pokemonSpecies.isBlank()) {
            pokemonArgIndex = 0;
            pokemonSpecies = safeExtractPokemonNameFromArgs(args, 0);
            pokemonFullText = safeExtractPlainText(args[0]);
         }

         if (pokemonSpecies == null || pokemonSpecies.isBlank()) {
            return;
         }

         String side = null;
         if (parts != null && parts.length >= 4) {
            if ("self".equals(parts[3])) {
               side = "player";
            } else if ("other".equals(parts[3])) {
               side = "opponent";
            }
         }

         if (side == null && args.length >= 2) {
            side = determineSideFromTrainerName(safeExtractPlainText(args[0]));
         }

         if (side == null) {
            side = determineSideFromFullText(pokemonFullText, pokemonSpecies);
         }

         String pokemonName = stripOwnerPrefix(pokemonSpecies);
         clearStatStagesForPokemon(side, pokemonSpecies, pokemonFullText);
         clearVolatileStatus(pokemonName);
         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] Switch-out (TK): Cleared stat stages and volatile status for {} (species='{}', full='{}', side='{}', argIndex={})",
            new Object[]{pokemonName, pokemonSpecies, pokemonFullText, side, pokemonArgIndex}
         );
      } catch (Throwable var7) {
      }
   }

   private static void tryApplyForcedSwitchFromArgs(String[] parts, Object[] args, String key) {
      try {
         if (args == null || args.length < 1) {
            return;
         }

         String newPokemonName = safeExtractPokemonNameFromArgs(args, 0);
         if (newPokemonName == null || newPokemonName.isBlank()) {
            newPokemonName = safeExtractPlainText(args[0]);
         }

         if (newPokemonName != null) {
            newPokemonName = stripOwnerPrefix(newPokemonName);
         }

         String ownerNameFromKey = null;
         if (args[0] instanceof class_2561 c && c.method_10851() instanceof class_2588 tc) {
            String wrapperKey = tc.method_11022();
            if (wrapperKey != null
               && (wrapperKey.contains("owned_pokemon") || wrapperKey.contains("enemy_pokemon") || wrapperKey.contains("wild_pokemon"))
               && tc.method_11023().length >= 1) {
               ownerNameFromKey = safeExtractPlainText(tc.method_11023()[0]);
            }
         }

         String side = null;
         String previousActivePokemon = null;
         if (newPokemonName != null) {
            String lowerNewPokemon = newPokemonName.toLowerCase(Locale.ROOT);
            if (ownerNameFromKey != null) {
               side = determineSideFromTrainerName(ownerNameFromKey);
               if ("player".equals(side)) {
                  side = "player";
                  previousActivePokemon = currentActivePlayerPokemon;
                  currentActivePlayerPokemon = newPokemonName;
                  localPlayerPokemon.add(lowerNewPokemon);
               } else if ("opponent".equals(side)) {
                  side = "opponent";
                  previousActivePokemon = currentActiveOpponentPokemon;
                  currentActiveOpponentPokemon = newPokemonName;
                  opponentPokemon.add(lowerNewPokemon);
               }
            } else if (localPlayerPokemon.contains(lowerNewPokemon)) {
               side = "player";
               previousActivePokemon = currentActivePlayerPokemon;
               currentActivePlayerPokemon = newPokemonName;
            } else if (opponentPokemon.contains(lowerNewPokemon)) {
               side = "opponent";
               previousActivePokemon = currentActiveOpponentPokemon;
               currentActiveOpponentPokemon = newPokemonName;
            } else {
               String fullText = safeExtractPlainText(args[0]);
               if (fullText != null) {
                  String lowerFull = fullText.toLowerCase(Locale.ROOT);
                  String mappedSide = ownerTextToSide.get(lowerFull);
                  if (mappedSide != null) {
                     side = mappedSide;
                     if ("player".equals(mappedSide)) {
                        previousActivePokemon = currentActivePlayerPokemon;
                        currentActivePlayerPokemon = newPokemonName;
                        localPlayerPokemon.add(lowerNewPokemon);
                     } else {
                        previousActivePokemon = currentActiveOpponentPokemon;
                        currentActiveOpponentPokemon = newPokemonName;
                        opponentPokemon.add(lowerNewPokemon);
                     }
                  } else {
                     String ownerName = extractOwnerNameFromBattleText(fullText);
                     String inferredSide = determineSideFromTrainerName(ownerName);
                     if ("player".equals(inferredSide)) {
                        side = "player";
                        previousActivePokemon = currentActivePlayerPokemon;
                        currentActivePlayerPokemon = newPokemonName;
                        localPlayerPokemon.add(lowerNewPokemon);
                        ownerTextToSide.put(lowerFull, "player");
                     } else {
                        side = "opponent";
                        previousActivePokemon = currentActiveOpponentPokemon;
                        currentActiveOpponentPokemon = newPokemonName;
                        opponentPokemon.add(lowerNewPokemon);
                        ownerTextToSide.put(lowerFull, "opponent");
                     }
                  }
               }
            }
         }

         if (previousActivePokemon != null && !previousActivePokemon.equalsIgnoreCase(newPokemonName)) {
            clearStatStagesForPokemon(side, previousActivePokemon, previousActivePokemon);
            clearVolatileStatus(previousActivePokemon);
         }
      } catch (Throwable var13) {
         LOGGER.error("[BattleMessageSubscriber] Error in tryApplyForcedSwitchFromArgs: {}", var13.getMessage(), var13);
      }
   }

   private static boolean shouldShowAbilityPopup(String pokemonName, String abilityName) {
      try {
         String k = (pokemonName + "|" + abilityName).toLowerCase(Locale.ROOT);
         long now = System.currentTimeMillis();
         Long last = recentAbilityPopups.get(k);
         return last == null || now - last > 1500L;
      } catch (Throwable var6) {
         return true;
      }
   }

   private static void markAbilityPopupShown(String pokemonName, String abilityName) {
      try {
         String k = (pokemonName + "|" + abilityName).toLowerCase(Locale.ROOT);
         recentAbilityPopups.put(k, System.currentTimeMillis());
      } catch (Throwable var3) {
      }
   }

   private static void markAbilityPopupShownFromKey() {
      try {
         lastAbilityPopupFromKeyMs = System.currentTimeMillis();
      } catch (Throwable var1) {
      }
   }

   private static boolean shouldRunLegacyAbilityParsing() {
      try {
         long now = System.currentTimeMillis();
         return now - lastAbilityPopupFromKeyMs > 2000L;
      } catch (Throwable var2) {
         return true;
      }
   }

   private static String safeExtractAbilityNameFromArgs(Object[] args, int idx) {
      try {
         if (args != null && idx < args.length) {
            Object o = args[idx];
            if (o instanceof class_2561 c) {
               if (c.method_10851() instanceof class_2588 tc) {
                  String key = tc.method_11022();
                  if (key != null) {
                     String[] p = key.split("\\.");
                     if (p.length >= 3) {
                        String id = p[p.length - 1];
                        if (id != null && !id.isBlank()) {
                           return formatIdentifierToTitle(id);
                        }
                     }
                  }

                  Object[] a = tc.method_11023();
                  if (a != null && a.length > 0) {
                     String s = safeExtractPlainText(a[0]);
                     if (s != null && !s.isBlank()) {
                        return s;
                     }
                  }
               }

               try {
                  String s = c.getString();
                  if (s != null && !s.isBlank()) {
                     return s;
                  }
               } catch (Throwable var8) {
               }
            }

            String s = String.valueOf(o);
            return s != null && !s.isBlank() ? s : null;
         } else {
            return null;
         }
      } catch (Throwable var9) {
         return null;
      }
   }

   private static String safeExtractPlainText(Object o) {
      try {
         if (o == null) {
            return null;
         } else if (o instanceof class_2561 c) {
            try {
               String s = c.getString();
               return s != null && !s.isBlank() ? s : null;
            } catch (Throwable var3) {
               return null;
            }
         } else {
            String s = String.valueOf(o);
            return s != null && !s.isBlank() ? s : null;
         }
      } catch (Throwable var4) {
         return null;
      }
   }

   private static class_2561 cobblemon_battle_extras$resolvePokemonComponent(Object[] args, int idx) {
      try {
         if (args != null && idx < args.length) {
            Object value = args[idx];
            if (value instanceof class_2561) {
               return (class_2561)value;
            } else {
               String pokemonText = safeExtractPlainText(value);
               if (pokemonText == null || pokemonText.isBlank()) {
                  pokemonText = safeExtractPokemonNameFromArgs(args, idx);
               }

               return pokemonText != null && !pokemonText.isBlank() ? class_2561.method_43470(pokemonText) : null;
            }
         } else {
            return null;
         }
      } catch (Throwable var4) {
         return null;
      }
   }

   private static String cobblemon_battle_extras$normalizeIdentifier(String raw) {
      if (raw == null) {
         return null;
      } else {
         String normalized = raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
         return normalized.isBlank() ? null : normalized;
      }
   }

   private static class_2561 cobblemon_battle_extras$createAbilityComponent(String abilityId, String fallbackName) {
      try {
         String normalizedAbilityId = abilityId;
         if (abilityId == null || abilityId.isBlank()) {
            normalizedAbilityId = cobblemon_battle_extras$normalizeIdentifier(fallbackName);
         }

         if (normalizedAbilityId != null && !normalizedAbilityId.isBlank()) {
            String translationKey = "cobblemon.ability." + normalizedAbilityId;
            if (cobblemon_battle_extras$hasResolvedTranslation(translationKey)) {
               return class_2561.method_43471(translationKey);
            }
         }

         String displayName = fallbackName;
         if (fallbackName == null || fallbackName.isBlank()) {
            displayName = formatIdentifierToTitle(normalizedAbilityId);
         }

         return displayName != null && !displayName.isBlank() ? class_2561.method_43470(formatIdentifierToTitle(displayName)) : null;
      } catch (Throwable var4) {
         return null;
      }
   }

   private static boolean cobblemon_battle_extras$hasResolvedTranslation(String translationKey) {
      try {
         return translationKey != null && !translationKey.isBlank() ? !translationKey.equals(class_2561.method_43471(translationKey).getString()) : false;
      } catch (Throwable var2) {
         return false;
      }
   }

   private static boolean cobblemon_battle_extras$rendersAsRawTranslationKey(class_2561 component) {
      try {
         if (component != null && component.method_10851() instanceof class_2588 tc) {
            String translationKey = tc.method_11022();
            return translationKey != null && translationKey.equals(component.getString());
         } else {
            return false;
         }
      } catch (Throwable var3) {
         return false;
      }
   }

   private static void cobblemon_battle_extras$tryApplyAbilityPopupFromStartEffect(String effectId, Object[] args) {
      try {
         if (args == null || args.length < 1) {
            return;
         }

         String abilityId = cobblemon_battle_extras$normalizeIdentifier(effectId);
         if (abilityId == null || abilityId.isBlank()) {
            return;
         }

         String translationKey = "cobblemon.ability." + abilityId;
         if (!cobblemon_battle_extras$hasResolvedTranslation(translationKey)) {
            return;
         }

         class_2561 abilityComponent = cobblemon_battle_extras$createAbilityComponent(abilityId, effectId);
         if (abilityComponent == null) {
            return;
         }

         class_2561 pokemonComponent = cobblemon_battle_extras$resolvePokemonComponent(args, 0);
         if (pokemonComponent == null) {
            return;
         }

         String pokemonName = safeExtractPokemonNameFromArgs(args, 0);
         if (pokemonName == null || pokemonName.isBlank()) {
            pokemonName = safeExtractPlainText(args[0]);
         }

         if (pokemonName == null || pokemonName.isBlank()) {
            return;
         }

         if (shouldShowAbilityPopup(pokemonName, abilityId)) {
            AbilityPopupRenderer.showAbilityPopup(pokemonComponent, abilityComponent);
            markAbilityPopupShown(pokemonName, abilityId);
            markAbilityPopupShownFromKey();
         }
      } catch (Throwable var7) {
      }
   }

   private static String formatIdentifierToTitle(String id) {
      try {
         if (id == null) {
            return null;
         } else {
            String ignored = id.replace('_', ' ').replace('-', ' ').trim();
            if (ignored.isEmpty()) {
               return null;
            } else {
               String[] parts = ignored.split("\\s+");
               StringBuilder out = new StringBuilder();

               for (String p : parts) {
                  if (!p.isBlank()) {
                     if (out.length() > 0) {
                        out.append(' ');
                     }

                     out.append(Character.toUpperCase(p.charAt(0)));
                     if (p.length() > 1) {
                        out.append(p.substring(1));
                     }
                  }
               }

               return out.toString();
            }
         }
      } catch (Throwable var8) {
         return id;
      }
   }

   private static void tryApplyTerastallizationFromArgs(String[] parts, Object[] args) {
      try {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] tryApplyTerastallizationFromArgs called", new Object[0]);
         if (args == null || args.length == 0) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Tera: No args!", new Object[0]);
            return;
         }

         String speciesId = safeExtractPokemonSpeciesIdLowerFromArgs(args, 0);
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Tera: speciesId='{}'", new Object[]{speciesId});
         if (speciesId == null || speciesId.isBlank()) {
            String pokemonFull = safeExtractPlainText(args[0]);
            if (pokemonFull != null) {
               speciesId = normalizePokemonName(stripOwnerPrefix(pokemonFull));
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Tera: speciesId from stripOwnerPrefix='{}'", new Object[]{speciesId});
            }
         }

         if (speciesId == null || speciesId.isBlank()) {
            return;
         }

         String pokemonFull = safeExtractPlainText(args[0]);
         String canonicalSpecies = resolveCanonicalSpeciesFromBattle(pokemonFull != null ? pokemonFull : speciesId);
         if (canonicalSpecies == null || canonicalSpecies.isBlank()) {
            canonicalSpecies = resolveCanonicalSpeciesFromBattle(speciesId);
         }

         if (canonicalSpecies == null || canonicalSpecies.isBlank()) {
            canonicalSpecies = speciesId;
         }

         canonicalSpecies = stripBattleFormPrefixes(normalizePokemonName(canonicalSpecies));
         if (canonicalSpecies == null || canonicalSpecies.isBlank()) {
            return;
         }

         String side = null;
         if (pokemonFull != null && !pokemonFull.isBlank()) {
            side = ownerTextToSide.get(pokemonFull.toLowerCase(Locale.ROOT));
         }

         if (side == null && canonicalSpecies != null && !canonicalSpecies.isBlank()) {
            side = ownerTextToSide.get(canonicalSpecies);
         }

         if (side == null) {
            side = determineSideFromBattleCandidates(pokemonFull, canonicalSpecies);
         }

         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] Tera: speciesId='{}' canonical='{}' pokemonFull='{}' -> side='{}' (map keys: {})",
            new Object[]{speciesId, canonicalSpecies, pokemonFull, side, ownerTextToSide.keySet()}
         );
         if (side != null && !side.isBlank()) {
            GimmickUsageTracker.markGimmickUsedForSide(side, "tera");
         }

         String teraType = null;
         if (args.length >= 2) {
            teraType = safeExtractTypeNameFromArgs(args, 1);
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Tera: teraType from args[1]='{}'", new Object[]{teraType});
         }

         if ((teraType == null || teraType.isBlank()) && parts != null) {
            for (String p : parts) {
               if (p != null) {
                  String t = typeNameFromId(p);
                  if (t != null) {
                     teraType = t;
                     CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Tera: teraType from parts='{}'", new Object[]{t});
                     break;
                  }
               }
            }
         }

         if (teraType != null && !teraType.isBlank()) {
            String normalizedSpecies = stripBattleFormPrefixes(normalizePokemonName(speciesId));
            Set<String> storageKeys = new LinkedHashSet<>();
            storageKeys.add(canonicalSpecies);
            if (normalizedSpecies != null && !normalizedSpecies.isBlank()) {
               storageKeys.add(normalizedSpecies);
            }

            if (side != null && !side.isBlank()) {
               storageKeys.add(makeSidedKey(side, canonicalSpecies));
               if (normalizedSpecies != null && !normalizedSpecies.isBlank()) {
                  storageKeys.add(makeSidedKey(side, normalizedSpecies));
               }

               if (pokemonFull != null && !pokemonFull.isBlank()) {
                  ownerTextToSide.putIfAbsent(pokemonFull.toLowerCase(Locale.ROOT), side);
               }
            }

            for (String storageKey : storageKeys) {
               if (storageKey != null && !storageKey.isBlank()) {
                  terastallized.put(storageKey, teraType);
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Tera STORED: '{}' -> {}", new Object[]{storageKey, teraType});
               }
            }

            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Tera STORE COMPLETE: keys={} type={}", new Object[]{storageKeys, teraType});
         } else {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Tera: No teraType found!", new Object[0]);
         }
      } catch (Throwable var12) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Tera error", new Object[]{var12});
      }
   }

   private static void tryApplyDynamaxFromArgs(String[] parts, Object[] args) {
      try {
         if (args == null || args.length == 0) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Dynamax: no args", new Object[0]);
            return;
         }

         RevealedPokemonContext context = resolveRevealedPokemonContext(args, 0);
         if (args.length >= 2 && (context.side() == null || context.side().isBlank()) && (context.speciesId() == null || context.speciesId().isBlank())) {
            RevealedPokemonContext alternate = resolveRevealedPokemonContext(args, 1);
            if (alternate.side() != null && !alternate.side().isBlank() || alternate.speciesId() != null && !alternate.speciesId().isBlank()) {
               context = alternate;
            }
         }

         String speciesId = context.speciesId();
         String pokemonFull = context.fullText();
         if ((speciesId == null || speciesId.isBlank()) && pokemonFull != null && !pokemonFull.isBlank()) {
            speciesId = stripBattleFormPrefixes(normalizePokemonName(stripOwnerPrefix(pokemonFull)));
         }

         String side = context.side();
         if (pokemonFull != null && !pokemonFull.isBlank()) {
            side = ownerTextToSide.get(pokemonFull.toLowerCase(Locale.ROOT));
         }

         if (side == null && speciesId != null && !speciesId.isBlank()) {
            side = ownerTextToSide.get(speciesId);
         }

         if (side == null) {
            side = determineSideFromBattleCandidates(pokemonFull, speciesId);
         }

         String gimmickType = "dynamax";
         if (parts != null) {
            for (String part : parts) {
               if (part != null) {
                  String keyPart = part.toLowerCase(Locale.ROOT);
                  if (keyPart.contains("gigantamax") || keyPart.contains("gmax")) {
                     gimmickType = "gmax";
                     break;
                  }
               }
            }
         }

         if (side != null && !side.isBlank()) {
            GimmickUsageTracker.markGimmickUsedForSide(side, gimmickType);
            CobblemonBattleInfoClient.debug(
               "[BattleMessageSubscriber] Dynamax usage tracked: side='{}' species='{}' type='{}'", new Object[]{side, speciesId, gimmickType}
            );
         } else {
            CobblemonBattleInfoClient.debug(
               "[BattleMessageSubscriber] Dynamax usage side unresolved: species='{}' full='{}'", new Object[]{speciesId, pokemonFull}
            );
         }
      } catch (Throwable var12) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Dynamax tracking error", new Object[]{var12});
      }
   }

   private static boolean isDynamaxMoveId(String moveId) {
      if (moveId != null && !moveId.isBlank()) {
         String id = moveId.toLowerCase(Locale.ROOT);
         return "maximize".equals(id) ? false : id.startsWith("gmax") || id.startsWith("max");
      } else {
         return false;
      }
   }

   private static void tryApplyMegaEvolutionFromArgs(String[] parts, Object[] args) {
      try {
         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] tryApplyMegaEvolutionFromArgs called - key: {}, args: {}",
            new Object[]{String.join(".", parts), args == null ? 0 : args.length}
         );
         tryApplyMegaEvolutionFromArgsWithForm(parts, args, null);
      } catch (Throwable var3) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error in tryApplyMegaEvolutionFromArgs", new Object[]{var3});
      }
   }

   private static void tryApplyMegaEvolutionFromArgsWithForm(String[] parts, Object[] args, String megaForm) {
      try {
         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] tryApplyMegaEvolutionFromArgsWithForm called - key: {}, args: {}, form: {}",
            new Object[]{String.join(".", parts), args == null ? 0 : args.length, megaForm}
         );
         if (args == null || args.length == 0) {
            LOGGER.warn("[BattleMessageSubscriber] Mega: No args provided!");
            return;
         }

         int speciesIdx = args.length >= 2 && safeLooksLikeActorLabel(args[0]) ? 1 : 0;
         String idKeyLower = safeExtractPokemonSpeciesIdLowerFromArgs(args, speciesIdx);
         if (idKeyLower == null || idKeyLower.isBlank()) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega: Failed to extract Pokemon species ID from args", new Object[0]);
            return;
         }

         String pokemonFull = safeExtractPlainText(args[speciesIdx]);
         String side = null;
         if (pokemonFull != null) {
            side = ownerTextToSide.get(pokemonFull.toLowerCase(Locale.ROOT));
            if (side == null) {
               side = ownerTextToSide.get(idKeyLower);
            }
         }

         if (side == null) {
            side = determineSideFromBattleCandidates(pokemonFull, idKeyLower);
         }

         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] Mega: species='{}' pokemonFull='{}' -> side='{}'", new Object[]{idKeyLower, pokemonFull, side}
         );
         if (side != null && !side.isBlank()) {
            GimmickUsageTracker.markGimmickUsedForSide(side, "mega");
         }

         if (megaForm == null && args.length > 0) {
            Object[] storageKey = args;
            int megaTypes = args.length;

            for (int regError = 0; regError < megaTypes; regError++) {
               if (storageKey[regError] instanceof class_2561 c) {
                  String text = c.getString().toLowerCase();
                  if (text.contains("x form") || text.contains("mega x") || text.contains("-x")) {
                     megaForm = "x";
                     break;
                  }

                  if (text.contains("y form") || text.contains("mega y") || text.contains("-y")) {
                     megaForm = "y";
                     break;
                  }
               }
            }
         }

         String storageKey = side != null ? makeSidedKey(side, idKeyLower) : idKeyLower;
         if (megaEvolved.containsKey(storageKey)) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega: Types already set for '{}', skipping", new Object[]{storageKey});
            return;
         }

         List<String> megaTypes = getMegaFormTypes(idKeyLower, megaForm);
         if (megaTypes == null || megaTypes.isEmpty()) {
            megaTypes = Collections.emptyList();
         }

         megaEvolved.put(storageKey, megaTypes);
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega stored: '{}' (form={}) with types: {}", new Object[]{storageKey, megaForm, megaTypes});
         CritChanceCache.markForClearing();
         if (parts.length >= 3 && "mega".equals(parts[2])) {
            try {
               String fullKey = String.join(".", parts);
               class_2561 megaMessage = class_2561.method_43469(fullKey, args);
               String localizedText = megaMessage.getString();
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega Evolution full message: '{}'", new Object[]{localizedText});
               String lowerText = localizedText.toLowerCase();
               if (args.length > 0 && args[0] instanceof class_2561 pokemonComp) {
                  String pokemonText = pokemonComp.getString();
                  String messageWithoutPokemon = localizedText.replace(pokemonText, "").trim();
                  if (!messageWithoutPokemon.isEmpty()) {
                     BattleMessageColorizer.registerEffectMetadata(messageWithoutPokemon, "mega_evolved");
                     CobblemonBattleInfoClient.debug(
                        "[BattleMessageSubscriber] Registered Mega Evolution phrase for colorization: '{}'", new Object[]{messageWithoutPokemon}
                     );
                  }
               }

               BattleMessageColorizer.registerEffectMetadata("Mega Evolved!", "mega_evolved");
               BattleMessageColorizer.registerEffectMetadata("Mega Evolved", "mega_evolved");
               BattleMessageColorizer.registerEffectMetadata("mega evolved!", "mega_evolved");
               BattleMessageColorizer.registerEffectMetadata("mega evolved", "mega_evolved");
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Registered 'Mega Evolved!' variations for colorization", new Object[0]);
               if (lowerText.contains("mega")) {
                  String[] words = localizedText.split("\\s+");

                  for (int i = 0; i < words.length - 1; i++) {
                     if (words[i].toLowerCase().contains("mega")) {
                        String phrase = words[i] + " " + words[i + 1];
                        String cleanPhrase = phrase.replaceAll("[!?.,;]$", "");
                        BattleMessageColorizer.registerEffectMetadata(cleanPhrase, "mega_evolved");
                        BattleMessageColorizer.registerEffectMetadata(cleanPhrase + "!", "mega_evolved");
                        CobblemonBattleInfoClient.debug(
                           "[BattleMessageSubscriber] Registered Mega Evolution phrases: '{}' and '{}!'", new Object[]{cleanPhrase, cleanPhrase}
                        );
                     }
                  }
               }
            } catch (Throwable var17) {
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Failed to register Mega Evolution colorization metadata", new Object[]{var17});
            }
         }
      } catch (Throwable var18) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega extraction error", new Object[]{var18});
      }
   }

   private static boolean safeLooksLikeActorLabel(Object arg0) {
      try {
         if (arg0 == null) {
            return false;
         } else {
            String s;
            if (arg0 instanceof class_2561 c) {
               s = c.getString();
            } else {
               s = String.valueOf(arg0);
            }

            if (s == null) {
               return false;
            } else {
               String lower = s.toLowerCase(Locale.ROOT);
               return lower.contains("owned pokemon")
                  || lower.contains("opposing pokemon")
                  || lower.contains("wild pokemon")
                  || lower.contains("ally pokemon")
                  || lower.contains("enemy pokemon");
            }
         }
      } catch (Throwable var3) {
         return false;
      }
   }

   private static String safeExtractPokemonSpeciesIdLowerFromArgs(Object[] args, int idx) {
      String fallbackText = null;

      try {
         if (args == null || idx >= args.length) {
            CobblemonBattleInfoClient.debug(
               "[BattleMessageSubscriber] Mega Extract: Invalid args or index (args={}, idx={})", new Object[]{args == null ? "null" : args.length, idx}
            );
            return null;
         }

         Object o = args[idx];
         fallbackText = safeExtractPlainText(o);
         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] Mega Extract: args[{}] = {} (type: {})", new Object[]{idx, o, o == null ? "null" : o.getClass().getSimpleName()}
         );
         if (o instanceof class_2561 c) {
            CobblemonBattleInfoClient.debug(
               "[BattleMessageSubscriber] Mega Extract: Component string: '{}', contents type: {}",
               new Object[]{c.getString(), c.method_10851().getClass().getSimpleName()}
            );
            if (c.method_10851() instanceof class_2588 tc) {
               String key = tc.method_11022();
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega Extract: Translation key: '{}'", new Object[]{key});
               if (key != null && key.startsWith("cobblemon.species.")) {
                  String[] p = key.split("\\.");
                  if (p.length >= 3) {
                     String id = p[2];
                     if (id != null && !id.isBlank() && !id.equals("name")) {
                        String result = id.toLowerCase(Locale.ROOT);
                        CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega Extract: SUCCESS - Extracted species ID: '{}'", new Object[]{result});
                        return result;
                     }
                  }
               } else if (key != null && (key.contains("owned_pokemon") || key.contains("enemy_pokemon") || key.contains("wild_pokemon"))) {
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega Extract: Found wrapper key '{}', checking nested args", new Object[]{key});
                  Object[] nestedArgs = tc.method_11023();
                  if (nestedArgs != null) {
                     for (int i = 0; i < nestedArgs.length; i++) {
                        CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega Extract: Checking nested arg[{}]", new Object[]{i});
                        String nestedResult = safeExtractPokemonSpeciesIdLowerFromArgs(nestedArgs, i);
                        if (nestedResult != null) {
                           CobblemonBattleInfoClient.debug(
                              "[BattleMessageSubscriber] Mega Extract: SUCCESS from nested - Extracted species ID: '{}'", new Object[]{nestedResult}
                           );
                           return nestedResult;
                        }
                     }
                  }
               } else {
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] Mega Extract: Translation key doesn't start with 'cobblemon.species.': '{}'", new Object[]{key}
                  );
               }
            } else {
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega Extract: Component contents is not TranslatableContents", new Object[0]);
            }
         } else if (o != null) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega Extract: Argument is not a Component", new Object[0]);
         }
      } catch (Throwable var10) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega Extract: Exception", new Object[]{var10});
      }

      if (fallbackText != null && !fallbackText.isBlank()) {
         String canonicalFromBattle = resolveCanonicalSpeciesFromBattle(fallbackText);
         if (canonicalFromBattle != null && !canonicalFromBattle.isBlank()) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega Extract: SUCCESS from live battle match - '{}'", new Object[]{canonicalFromBattle});
            return canonicalFromBattle;
         }

         String normalized = normalizePokemonName(stripOwnerPrefix(fallbackText));
         String stripped = stripBattleFormPrefixes(normalized);
         if (stripped != null && !stripped.isBlank()) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega Extract: Fallback normalized - '{}'", new Object[]{stripped});
            return stripped;
         }
      }

      CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Mega Extract: FAILED to extract species ID", new Object[0]);
      return null;
   }

   private static String safeExtractPokemonSpeciesNameFromArgs(Object[] args, int idx) {
      try {
         if (args == null || idx >= args.length) {
            return null;
         }

         if (args[idx] instanceof class_2561 c && c.method_10851() instanceof class_2588 tc) {
            String key = tc.method_11022();
            if (key != null) {
               String[] p = key.split("\\.");
               if (p.length >= 3) {
                  String id = p[p.length - 1];
                  if (id != null && !id.isBlank()) {
                     return formatIdentifierToTitle(id);
                  }
               }
            }
         }
      } catch (Throwable var8) {
      }

      return null;
   }

   private static String safeExtractTypeNameFromArgs(Object[] args, int idx) {
      try {
         if (args != null && idx < args.length) {
            Object o = args[idx];
            if (o instanceof class_2561 c && c.method_10851() instanceof class_2588 tc) {
               String key = tc.method_11022();
               String fromKey = typeNameFromId(key);
               if (fromKey != null) {
                  return fromKey;
               }

               String[] p = key.split("\\.");

               for (String part : p) {
                  String fromPart = typeNameFromId(part);
                  if (fromPart != null) {
                     return fromPart;
                  }
               }

               Object[] nested = tc.method_11023();
               if (nested != null) {
                  for (int i = 0; i < nested.length; i++) {
                     String nestedType = safeExtractTypeNameFromArgs(nested, i);
                     if (nestedType != null) {
                        return nestedType;
                     }
                  }
               }

               String rendered = c.getString();
               String fromRendered = typeNameFromId(rendered);
               if (fromRendered != null) {
                  return fromRendered;
               }
            }

            return typeNameFromId(String.valueOf(o));
         } else {
            return null;
         }
      } catch (Throwable var13) {
         return null;
      }
   }

   private static String typeNameFromId(String id) {
      try {
         if (id == null) {
            return null;
         } else {
            String t = id.toLowerCase(Locale.ROOT);
            if (t.contains(".")) {
               String[] p = t.split("\\.");
               t = p[p.length - 1];
            }
            return switch (t) {
               case "stellar" -> "Stellar";
               case "normal" -> "Normal";
               case "fire" -> "Fire";
               case "water" -> "Water";
               case "electric" -> "Electric";
               case "grass" -> "Grass";
               case "ice" -> "Ice";
               case "fighting" -> "Fighting";
               case "poison" -> "Poison";
               case "ground" -> "Ground";
               case "flying" -> "Flying";
               case "psychic" -> "Psychic";
               case "bug" -> "Bug";
               case "rock" -> "Rock";
               case "ghost" -> "Ghost";
               case "dragon" -> "Dragon";
               case "dark" -> "Dark";
               case "steel" -> "Steel";
               case "fairy" -> "Fairy";
               default -> null;
            };
         }
      } catch (Throwable var4) {
         return null;
      }
   }

   private static boolean isSelfSide(Object[] args) {
      try {
         if (args == null || args.length == 0) {
            return true;
         }

         if (args[0] instanceof class_2561 c && c.method_10851() instanceof class_2588 tc) {
            return tc.method_11022().contains("ally") || tc.method_11022().contains("self");
         }
      } catch (Throwable var5) {
      }

      return true;
   }

   private static String determineSideFromPartsOrArgs(String[] parts, Object[] args) {
      try {
         label51:
         if (parts != null && parts.length >= 4) {
            String p = parts[3].toLowerCase(Locale.ROOT);
            if (!p.contains("opponent") && !p.contains("foe") && !p.contains("enemy")) {
               if (!p.contains("self") && !p.contains("ally") && !p.contains("player")) {
                  break label51;
               }

               return "player";
            }

            return "opponent";
         }
      } catch (Throwable var6) {
      }

      try {
         if (args != null && args.length > 0) {
            String token = safeExtractPlainText(args[0]);
            String species = safeExtractPokemonSpeciesIdLowerFromArgs(args, 0);
            String sideFromBattle = determineSideFromBattleCandidates(token, species);
            if (sideFromBattle != null) {
               return sideFromBattle;
            }
         }
      } catch (Throwable var5) {
      }

      return isSelfSide(args) ? "player" : "opponent";
   }

   private static boolean applySideEffectFromId(String side, String effectId) {
      try {
         if (side == null) {
            side = "player";
         }

         if (effectId == null) {
            return false;
         } else {
            String id = effectId.toLowerCase(Locale.ROOT);
            int screenDuration = hasLightClay ? 8 : 5;
            if (!id.contains("lightscreen") && !id.contains("light_screen")) {
               if ((id.contains("reflect") || id.contains("reflectscreen")) && !id.contains("reflecttype")) {
                  setScreen(side, "Reflect", screenDuration);
                  return true;
               } else if (id.contains("auroraveil") || id.contains("aurora_veil")) {
                  setScreen(side, "Aurora Veil", screenDuration);
                  return true;
               } else if (id.contains("mist") && !id.contains("misty")) {
                  setScreen(side, "Mist", 5);
                  return true;
               } else if (id.contains("safeguard")) {
                  setScreen(side, "Safeguard", 5);
                  return true;
               } else if (id.contains("luckychant") || id.contains("lucky_chant")) {
                  setScreen(side, "Lucky Chant", 5);
                  return true;
               } else if (id.contains("tailwind")) {
                  tailwind.put(side, 4);
                  return true;
               } else {
                  return false;
               }
            } else {
               setScreen(side, "Light Screen", screenDuration);
               return true;
            }
         }
      } catch (Throwable var4) {
         return false;
      }
   }

   private static boolean removeSideEffectFromId(String side, String effectId) {
      try {
         if (side == null) {
            side = "player";
         }

         if (effectId == null) {
            return false;
         } else {
            String id = effectId.toLowerCase(Locale.ROOT);
            if (!id.contains("lightscreen") && !id.contains("light_screen")) {
               if ((id.contains("reflect") || id.contains("reflectscreen")) && !id.contains("reflecttype")) {
                  removeScreen(side, "Reflect");
                  return true;
               } else if (id.contains("auroraveil") || id.contains("aurora_veil")) {
                  removeScreen(side, "Aurora Veil");
                  return true;
               } else if (id.contains("mist") && !id.contains("misty")) {
                  removeScreen(side, "Mist");
                  return true;
               } else if (id.contains("safeguard")) {
                  removeScreen(side, "Safeguard");
                  return true;
               } else if (id.contains("luckychant") || id.contains("lucky_chant")) {
                  removeScreen(side, "Lucky Chant");
                  return true;
               } else if (id.contains("tailwind")) {
                  tailwind.remove(side);
                  return true;
               } else {
                  return false;
               }
            } else {
               removeScreen(side, "Light Screen");
               return true;
            }
         }
      } catch (Throwable var3) {
         return false;
      }
   }

   private static void setWeatherFromId(String weatherId) {
      String id = weatherId == null ? "" : weatherId.toLowerCase(Locale.ROOT);
      String previousWeather = currentWeather;
      boolean isPrimalWeather = false;
      if (id.equals("primordialsea")) {
         currentWeather = "Heavy Rain";
         isPrimalWeather = true;
      } else if (id.equals("desolateland")) {
         currentWeather = "Harsh Sunlight";
         isPrimalWeather = true;
      } else if (id.equals("deltastream")) {
         currentWeather = "Strong Winds";
         isPrimalWeather = true;
      } else if (id.contains("rain")) {
         currentWeather = "Rain";
      } else if (id.contains("sun") || id.equals("sunnyday")) {
         currentWeather = "Sun";
      } else if (id.contains("sandstorm")) {
         currentWeather = "Sandstorm";
      } else if (id.contains("hail")) {
         currentWeather = "Hail";
      } else {
         if (!id.contains("snow")) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Unknown weather ID: '{}'", new Object[]{weatherId});
            return;
         }

         currentWeather = "Snow";
      }

      boolean weatherChanged = !currentWeather.equals(previousWeather);
      if (isPrimalWeather) {
         weatherTurns = -1;
         weatherStartTurn = 0;
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Primal weather set to '{}' (permanent, ID: '{}')", new Object[]{currentWeather, weatherId});
      } else {
         int weatherDuration = hasWeatherRock ? 8 : 5;
         if (weatherChanged) {
            weatherTurns = weatherDuration;
            weatherStartTurn = currentTurnNumber;
            weatherJustSet = true;
            CobblemonBattleInfoClient.debug(
               "[BattleMessageSubscriber] Weather CHANGED to '{}' with {} turns on turn {} (ID: '{}')",
               new Object[]{currentWeather, weatherTurns, currentTurnNumber, weatherId}
            );
         } else {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Weather '{}' refreshed (ID: '{}')", new Object[]{currentWeather, weatherId});
         }
      }
   }

   private static void setTerrainFromId(String terrainId) {
      if (terrainId != null) {
         String id = terrainId.toLowerCase(Locale.ROOT);
         if (id.contains("electric")) {
            currentTerrain = "Electric Terrain";
         } else if (id.contains("grassy")) {
            currentTerrain = "Grassy Terrain";
         } else if (id.contains("misty")) {
            currentTerrain = "Misty Terrain";
         } else if (id.contains("psychic")) {
            currentTerrain = "Psychic Terrain";
         }

         terrainTurns = currentTerrain.isEmpty() ? 0 : 5;
         terrainStartTurn = lastTurnNumber > 0 ? lastTurnNumber : 1;
      }
   }

   private static void setRoomFromId(String effectId) {
      if (effectId != null) {
         String id = effectId.toLowerCase(Locale.ROOT);
         if (id.contains("trickroom")) {
            trickRoomTurns = 5;
         } else if (id.contains("gravity")) {
            gravityTurns = 5;
         } else if (id.contains("wonderroom")) {
            wonderRoomTurns = 5;
         } else if (id.contains("magicroom")) {
            magicRoomTurns = 5;
         } else if (id.contains("watersport")) {
            waterSportTurns = 5;
         } else if (id.contains("mudsport")) {
            mudSportTurns = 5;
         }
      }
   }

   private static void clearRoomFromId(String effectId) {
      if (effectId != null) {
         String id = effectId.toLowerCase(Locale.ROOT);
         if (id.contains("trickroom")) {
            trickRoomTurns = 0;
         } else if (id.contains("gravity")) {
            gravityTurns = 0;
         } else if (id.contains("wonderroom")) {
            wonderRoomTurns = 0;
         } else if (id.contains("magicroom")) {
            magicRoomTurns = 0;
         } else if (id.contains("watersport")) {
            waterSportTurns = 0;
         } else if (id.contains("mudsport")) {
            mudSportTurns = 0;
         }
      }
   }

   private static void activateFieldEffectFromId(String effectId) {
      if (effectId != null) {
         String id = effectId.toLowerCase(Locale.ROOT);
         if (id.contains("perishsong")) {
            perishSongTurns = 3;
            perishSongStartTurn = currentTurnNumber > 0 ? currentTurnNumber : (lastTurnNumber > 0 ? lastTurnNumber : 1);
         }
      }
   }

   private static void applyHazardFromId(String side, String hazardId) {
      String hazard = hazardDisplayName(hazardId);
      if (!hazard.isEmpty()) {
         if (hazard.equals("Spikes")) {
            addHazardLayer(side, hazard, 3);
         } else if (hazard.equals("Toxic Spikes")) {
            addHazardLayer(side, hazard, 2);
         } else {
            setHazard(side, hazard, 1);
         }
      }
   }

   private static String hazardDisplayName(String hazardId) {
      if (hazardId == null) {
         return "";
      } else {
         String id = hazardId.toLowerCase(Locale.ROOT);
         if (id.contains("stealthrock")) {
            return "Stealth Rock";
         } else if (id.contains("toxicspikes")) {
            return "Toxic Spikes";
         } else if (id.contains("spikes")) {
            return "Spikes";
         } else if (id.contains("stickyweb")) {
            return "Sticky Web";
         } else {
            return id.contains("curse") ? "Curse" : "";
         }
      }
   }

   private static void tryApplyStatBoost(String[] parts, Object[] args, boolean isBoost) {
      try {
         if (parts.length < 4 || args == null || args.length < 2) {
            return;
         }

         String severity = parts[3];
         if ("cap".equals(severity)) {
            CobblemonBattleInfoClient.debug(
               "[STAT_EXTRACT] Skipping cap boost message '{}' because no stage change occurred.", new Object[]{Arrays.toString((Object[])parts)}
            );
            return;
         }

         String pokemonSpecies = safeExtractPokemonSpeciesIdLowerFromArgs(args, 0);
         if (pokemonSpecies == null || pokemonSpecies.isBlank()) {
            pokemonSpecies = safeExtractPokemonNameFromArgs(args, 0);
         }

         String pokemonFullText = safeExtractPlainText(args[0]);
         String stat = normalizeBattleStatId(safeExtractStatIdFromArgs(args, 1));
         String ownerNameFromKey = null;
         if (args.length >= 1 && args[0] instanceof class_2561 c && c.method_10851() instanceof class_2588 tc) {
            if (tc.method_11022().equals("cobblemon.battle.owned_pokemon") && tc.method_11023().length >= 1) {
               ownerNameFromKey = safeExtractPlainText(tc.method_11023()[0]);
               CobblemonBattleInfoClient.debug("[STAT_EXTRACT] Extracted owner from translation key: '{}'", new Object[]{ownerNameFromKey});
            }

            CobblemonBattleInfoClient.debug(
               "[STAT_EXTRACT] Pokemon translation key: '{}', args count: {}", new Object[]{tc.method_11022(), tc.method_11023().length}
            );

            for (int i = 0; i < tc.method_11023().length; i++) {
               CobblemonBattleInfoClient.debug("[STAT_EXTRACT]   Pokemon arg[{}]: {}", new Object[]{i, tc.method_11023()[i]});
            }
         }

         CobblemonBattleInfoClient.debug(
            "[STAT_EXTRACT] pokemonSpecies='{}', pokemonFullText='{}', stat='{}', severity='{}', ownerFromKey='{}'",
            new Object[]{pokemonSpecies, pokemonFullText, stat, severity, ownerNameFromKey}
         );
         if (args.length >= 2 && args[1] instanceof class_2561 c && c.method_10851() instanceof class_2588 tc) {
            CobblemonBattleInfoClient.debug("[STAT_EXTRACT] Stat translation key: '{}'", new Object[]{tc.method_11022()});
         }

         if (pokemonSpecies == null || stat == null) {
            CobblemonBattleInfoClient.debug("[STAT_EXTRACT] FAILED - pokemonSpecies or stat is null, skipping", new Object[0]);
            return;
         }

         String canonicalSpecies = resolveCanonicalBattleSpecies(pokemonSpecies, pokemonFullText);
         if (canonicalSpecies == null || canonicalSpecies.isBlank()) {
            CobblemonBattleInfoClient.debug(
               "[STAT_EXTRACT] FAILED - could not resolve canonical species for '{}' / '{}'", new Object[]{pokemonSpecies, pokemonFullText}
            );
            return;
         }

         String side = null;
         if (ownerNameFromKey != null) {
            side = determineSideFromTrainerName(ownerNameFromKey);
            if (side != null) {
               CobblemonBattleInfoClient.debug("[SIDE_DETECT_1] Matched {} via translation key owner: '{}'", new Object[]{side, ownerNameFromKey});
            }
         }

         if (side == null) {
            side = determineSideFromFullText(pokemonFullText, canonicalSpecies);
         }

         if (side == null && ownerNameFromKey == null && (pokemonFullText == null || !hasOwnerPrefix(pokemonFullText))) {
            side = "opponent";
         }

         if (side == null) {
            if (localPlayerPokemon.contains(canonicalSpecies)) {
               side = "player";
            } else if (opponentPokemon.contains(canonicalSpecies)) {
               side = "opponent";
            }
         }

         CobblemonBattleInfoClient.debug(
            "[SIDE_DETECT] pokemonFullText='{}', pokemonSpecies='{}', canonical='{}' -> side='{}' (map keys: {})",
            new Object[]{pokemonFullText, pokemonSpecies, canonicalSpecies, side, ownerTextToSide.keySet()}
         );
         String storageKey = buildPrimaryStatStageStorageKey(side, canonicalSpecies, pokemonSpecies, pokemonFullText);
         Integer delta = resolveStatStageDelta(severity);
         if (delta == null) {
            CobblemonBattleInfoClient.debug("[STAT_EXTRACT] Skipping unsupported boost severity '{}'", new Object[]{severity});
            return;
         }

         int current = 0;
         if (storageKey != null && !storageKey.isBlank()) {
            Map<String, Integer> stages = statStages.get(storageKey);
            if (stages != null && stages.containsKey(stat)) {
               current = stages.getOrDefault(stat, 0);
            }
         }

         int next = isBoost ? Math.min(6, current + delta) : Math.max(-6, current - delta);
         if (storageKey != null && !storageKey.isBlank()) {
            Map<String, Integer> stages = statStages.computeIfAbsent(storageKey, k -> new ConcurrentHashMap<>());
            stages.put(stat, next);
         }

         if (side != null && pokemonFullText != null) {
            String lowerFullText = pokemonFullText.toLowerCase(Locale.ROOT);
            if (!ownerTextToSide.containsKey(lowerFullText)) {
               ownerTextToSide.put(lowerFullText, side);
               CobblemonBattleInfoClient.debug("[SIDE_LEARN] Learned mapping: '{}' -> {}", new Object[]{lowerFullText, side});
            }

            ownerTextToSide.putIfAbsent(canonicalSpecies, side);
         }

         CobblemonBattleInfoClient.debug(
            "[STAT_APPLIED] {} {} {} -> {} (keys: {}, side: {})", new Object[]{canonicalSpecies, stat, current, next, storageKey, side}
         );
      } catch (Throwable var15) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error in tryApplyStatBoost", new Object[]{var15});
      }
   }

   private static void tryApplySetBoost(String[] parts, Object[] args) {
      try {
         if (parts.length < 4 || args == null || args.length < 1) {
            return;
         }

         String effect = parts[3];
         switch (effect) {
            case "bellydrum":
            case "angerpoint":
               String stat = "attack";
               int stage = 6;
               String pokemonSpecies = safeExtractPokemonSpeciesIdLowerFromArgs(args, 0);
               if (pokemonSpecies == null || pokemonSpecies.isBlank()) {
                  pokemonSpecies = safeExtractPokemonNameFromArgs(args, 0);
               }

               String pokemonFullText = safeExtractPlainText(args[0]);
               String ownerNameFromKey = null;
               if (args[0] instanceof class_2561 c
                  && c.method_10851() instanceof class_2588 tc
                  && tc.method_11022().equals("cobblemon.battle.owned_pokemon")
                  && tc.method_11023().length >= 1) {
                  ownerNameFromKey = safeExtractPlainText(tc.method_11023()[0]);
               }

               String canonicalSpecies = resolveCanonicalSpeciesFromBattle(pokemonFullText);
               if (canonicalSpecies == null || canonicalSpecies.isBlank()) {
                  canonicalSpecies = resolveCanonicalSpeciesFromBattle(pokemonSpecies);
               }

               if (canonicalSpecies == null || canonicalSpecies.isBlank()) {
                  String fallbackName = pokemonSpecies != null && !pokemonSpecies.isBlank() ? pokemonSpecies : pokemonFullText;
                  canonicalSpecies = normalizePokemonName(stripOwnerPrefix(fallbackName));
               }

               if (canonicalSpecies == null || canonicalSpecies.isBlank()) {
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] SetBoost '{}' could not resolve canonical species for '{}'", new Object[]{effect, pokemonFullText}
                  );
                  return;
               }

               String side = null;
               if (ownerNameFromKey != null) {
                  side = determineSideFromTrainerName(ownerNameFromKey);
               }

               if (side == null) {
                  side = determineSideFromFullText(pokemonFullText, canonicalSpecies);
               }

               String storageKey = buildPrimaryStatStageStorageKey(side, canonicalSpecies, pokemonSpecies, pokemonFullText);
               if (storageKey != null && !storageKey.isBlank()) {
                  Map<String, Integer> stages = statStages.computeIfAbsent(storageKey, k -> new ConcurrentHashMap<>());
                  stages.put(stat, stage);
               }

               if (side != null && pokemonFullText != null) {
                  ownerTextToSide.putIfAbsent(pokemonFullText.toLowerCase(Locale.ROOT), side);
                  ownerTextToSide.putIfAbsent(canonicalSpecies, side);
               }

               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] SetBoost '{}' applied {}={} for '{}' (species='{}', side='{}', keys={})",
                  new Object[]{effect, stat, stage, pokemonFullText, canonicalSpecies, side, storageKey}
               );
               break;
            default:
               return;
         }
      } catch (Throwable var12) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error in tryApplySetBoost", new Object[]{var12});
      }
   }

   private static void tryApplySwapBoost(String[] parts, Object[] args) {
      try {
         if (parts.length < 4 || args == null || args.length < 1) {
            return;
         }

         String effect = parts[3];

         Set<String> swappedStats = switch (effect) {
            case "generic", "heartswap" -> Set.of();
            case "guardswap" -> Set.of("defence", "specialdefence");
            case "powerswap" -> Set.of("attack", "specialattack");
            default -> null;
         };
         if (swappedStats == null) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] swapboost effect '{}' is not currently handled", new Object[]{effect});
            return;
         }

         String userSpecies = safeExtractPokemonSpeciesIdLowerFromArgs(args, 0);
         if (userSpecies == null || userSpecies.isBlank()) {
            userSpecies = safeExtractPokemonNameFromArgs(args, 0);
         }

         String userFull = safeExtractPlainText(args[0]);
         String targetSpecies = null;
         String targetFull = null;
         if (args.length >= 2) {
            targetSpecies = safeExtractPokemonSpeciesIdLowerFromArgs(args, 1);
            if (targetSpecies == null || targetSpecies.isBlank()) {
               targetSpecies = safeExtractPokemonNameFromArgs(args, 1);
            }

            targetFull = safeExtractPlainText(args[1]);
         }

         if ((targetSpecies == null || targetSpecies.isBlank()) && pendingSwapBoostContext != null && effect.equals(pendingSwapBoostContext.effectId)) {
            String pendingUserCanonical = resolveCanonicalBattleSpecies(pendingSwapBoostContext.userSpecies, pendingSwapBoostContext.userFull);
            String currentUserCanonical = resolveCanonicalBattleSpecies(userSpecies, userFull);
            if (pendingUserCanonical != null && pendingUserCanonical.equals(currentUserCanonical)) {
               targetSpecies = pendingSwapBoostContext.targetSpecies;
               targetFull = pendingSwapBoostContext.targetFull;
            }
         }

         String userCanonical = resolveCanonicalBattleSpecies(userSpecies, userFull);
         String targetCanonical = resolveCanonicalBattleSpecies(targetSpecies, targetFull);
         if (userCanonical == null || userCanonical.isBlank() || targetCanonical == null || targetCanonical.isBlank()) {
            return;
         }

         String userSide = determineSideFromFullText(userFull, userCanonical);
         String targetSide = determineSideFromFullText(targetFull, targetCanonical);
         Set<String> userKeys = buildStatStageStorageKeys(userSide, userCanonical, userSpecies, userFull);
         Set<String> targetKeys = buildStatStageStorageKeys(targetSide, targetCanonical, targetSpecies, targetFull);
         Map<String, Integer> userStages = new HashMap<>(getTrackedStagesSnapshot(userKeys));
         Map<String, Integer> targetStages = new HashMap<>(getTrackedStagesSnapshot(targetKeys));
         Set<String> statsToSwap = swappedStats.isEmpty() ? new LinkedHashSet<>() : new LinkedHashSet<>(swappedStats);
         if (statsToSwap.isEmpty()) {
            statsToSwap.addAll(userStages.keySet());
            statsToSwap.addAll(targetStages.keySet());
         }

         for (String stat : statsToSwap) {
            String normalizedStat = normalizeBattleStatId(stat);
            if (normalizedStat != null && !normalizedStat.isBlank()) {
               int userValue = userStages.getOrDefault(normalizedStat, 0);
               int targetValue = targetStages.getOrDefault(normalizedStat, 0);
               if (targetValue == 0) {
                  userStages.remove(normalizedStat);
               } else {
                  userStages.put(normalizedStat, targetValue);
               }

               if (userValue == 0) {
                  targetStages.remove(normalizedStat);
               } else {
                  targetStages.put(normalizedStat, userValue);
               }
            }
         }

         replaceTrackedStages(userKeys, userStages);
         replaceTrackedStages(targetKeys, targetStages);
         pendingSwapBoostContext = null;
         if (userSide != null && userFull != null) {
            ownerTextToSide.putIfAbsent(userFull.toLowerCase(Locale.ROOT), userSide);
            ownerTextToSide.putIfAbsent(userCanonical, userSide);
         }

         if (targetSide != null && targetFull != null) {
            ownerTextToSide.putIfAbsent(targetFull.toLowerCase(Locale.ROOT), targetSide);
            ownerTextToSide.putIfAbsent(targetCanonical, targetSide);
         }

         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] swapboost '{}' applied between '{}' and '{}' (userKeys={}, targetKeys={}, stats={})",
            new Object[]{effect, userCanonical, targetCanonical, userKeys, targetKeys, statsToSwap}
         );
      } catch (Throwable var22) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error in tryApplySwapBoost", new Object[]{var22});
      }
   }

   private static void trackSwapBoostUsage(String moveId, Object[] args) {
      try {
         if (args == null || args.length < 3) {
            pendingSwapBoostContext = null;
            return;
         }

         String userSpecies = safeExtractPokemonSpeciesIdLowerFromArgs(args, 0);
         if (userSpecies == null || userSpecies.isBlank()) {
            userSpecies = safeExtractPokemonNameFromArgs(args, 0);
         }

         String targetSpecies = safeExtractPokemonSpeciesIdLowerFromArgs(args, 2);
         if (targetSpecies == null || targetSpecies.isBlank()) {
            targetSpecies = safeExtractPokemonNameFromArgs(args, 2);
         }

         String userFull = safeExtractPlainText(args[0]);
         String targetFull = safeExtractPlainText(args[2]);
         if ((userSpecies == null || userSpecies.isBlank()) && (userFull == null || userFull.isBlank())) {
            pendingSwapBoostContext = null;
            return;
         }

         if ((targetSpecies == null || targetSpecies.isBlank()) && (targetFull == null || targetFull.isBlank())) {
            pendingSwapBoostContext = null;
            return;
         }

         pendingSwapBoostContext = new SwapBoostPendingContext(moveId.toLowerCase(Locale.ROOT), userFull, userSpecies, targetFull, targetSpecies);
      } catch (Throwable var6) {
         pendingSwapBoostContext = null;
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error tracking swapboost move usage", new Object[]{var6});
      }
   }

   private static void tryApplyFocusEnergy(Object[] args) {
      try {
         if (args == null || args.length < 1) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] tryApplyFocusEnergy: No args provided", new Object[0]);
            return;
         }

         String pokemonSpecies = safeExtractPokemonNameFromArgs(args, 0);
         String pokemonFullText = safeExtractPlainText(args[0]);
         if (pokemonSpecies == null || pokemonSpecies.isBlank()) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] tryApplyFocusEnergy: Failed to extract Pokemon name", new Object[0]);
            return;
         }

         String pokemonName = stripOwnerPrefix(pokemonSpecies);
         String lowerPokemon = pokemonName.toLowerCase(Locale.ROOT);
         String side = null;
         if (pokemonFullText != null) {
            side = ownerTextToSide.get(pokemonFullText.toLowerCase(Locale.ROOT));
         }

         if (side == null) {
            side = ownerTextToSide.get(lowerPokemon);
         }

         if (side == null && pokemonFullText != null) {
            if (pokemonFullText.contains("'s ") && localPlayerName != null) {
               int apostropheIdx = pokemonFullText.indexOf("'s ");
               if (apostropheIdx > 0) {
                  String ownerName = pokemonFullText.substring(0, apostropheIdx);
                  side = determineSideFromTrainerName(ownerName);
               }
            } else if (!pokemonFullText.contains("'s ")) {
               side = "opponent";
            }
         }

         if (side == null) {
            if (localPlayerPokemon.contains(lowerPokemon)) {
               side = "player";
            } else if (opponentPokemon.contains(lowerPokemon)) {
               side = "opponent";
            }
         }

         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Focus Energy detected side='{}' for pokemon='{}'", new Object[]{side, pokemonName});
         String statKey = buildPrimaryStatStageStorageKey(side, pokemonName, pokemonName, pokemonFullText);
         if (statKey == null || statKey.isBlank()) {
            return;
         }

         Map<String, Integer> stages = statStages.computeIfAbsent(statKey, k -> new ConcurrentHashMap<>());
         stages.put("crit", 2);
         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] Focus Energy APPLIED: {} (side: {}, key: {}) Crit = +2", new Object[]{pokemonName, side, statKey}
         );
      } catch (Throwable var8) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error applying Focus Energy", new Object[]{var8});
      }
   }

   private static void tryApplyInvertBoost(Object[] args) {
      try {
         if (args == null || args.length < 1) {
            return;
         }

         String targetFull = safeExtractPlainText(args[0]);
         String targetSpecies = safeExtractPokemonSpeciesIdLowerFromArgs(args, 0);
         if (targetSpecies == null || targetSpecies.isBlank()) {
            targetSpecies = safeExtractPokemonNameFromArgs(args, 0);
         }

         if ((targetSpecies == null || targetSpecies.isBlank()) && targetFull != null) {
            targetSpecies = stripOwnerPrefix(targetFull).toLowerCase(Locale.ROOT);
         }

         if (targetSpecies == null || targetSpecies.isBlank()) {
            return;
         }

         String targetPokemon = stripOwnerPrefix(targetSpecies).toLowerCase(Locale.ROOT);
         String targetSide = determineSideFromFullText(targetFull, targetPokemon);
         LinkedHashSet<String> candidateKeys = new LinkedHashSet<>();
         candidateKeys.add(buildPrimaryStatStageStorageKey(targetSide, targetPokemon, targetSpecies, targetFull));
         candidateKeys.add(targetPokemon);
         candidateKeys.add(makeSidedKey("player", targetPokemon));
         candidateKeys.add(makeSidedKey("opponent", targetPokemon));
         candidateKeys.removeIf(keyx -> keyx == null || keyx.isBlank());
         boolean inverted = false;

         for (String key : candidateKeys) {
            Map<String, Integer> stages = statStages.get(key);
            if (stages != null && !stages.isEmpty()) {
               boolean changedThisKey = false;

               for (Entry<String, Integer> entry : stages.entrySet()) {
                  String stat = entry.getKey();
                  Integer value = entry.getValue();
                  if (value != null && !"Crit".equalsIgnoreCase(stat)) {
                     int invertedValue = Math.max(-6, Math.min(6, -value));
                     if (invertedValue != value) {
                        entry.setValue(invertedValue);
                        changedThisKey = true;
                     }
                  }
               }

               if (changedThisKey) {
                  inverted = true;
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] Topsy-Turvy: Inverted stat stages for {} (key: {}) -> {}", new Object[]{targetPokemon, key, stages}
                  );
               }
            }
         }

         if (!inverted) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Topsy-Turvy: No tracked stat stages to invert for {}", new Object[]{targetPokemon});
         }
      } catch (Throwable var16) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error applying invert boost", new Object[]{var16});
      }
   }

   private static String safeExtractPokemonNameFromArgs(Object[] args, int idx) {
      try {
         if (args != null && idx < args.length) {
            Object o = args[idx];
            CobblemonBattleInfoClient.debug(
               "[BattleMessageSubscriber] safeExtractPokemonNameFromArgs: arg[{}] type={}, value={}",
               new Object[]{idx, o == null ? "null" : o.getClass().getSimpleName(), o}
            );
            if (!(o instanceof class_2561 c)) {
               String result = o == null ? null : o.toString();
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] safeExtractPokemonNameFromArgs: Non-Component result: '{}'", new Object[]{result});
               return result;
            } else {
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] safeExtractPokemonNameFromArgs: Component getString='{}', contentsType={}",
                  new Object[]{c.getString(), c.method_10851().getClass().getSimpleName()}
               );
               if (c.method_10851() instanceof class_2588 tc) {
                  String key = tc.method_11022();
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] safeExtractPokemonNameFromArgs: TranslatableContents key='{}'", new Object[]{key});
                  if (key != null && key.startsWith("cobblemon.species.")) {
                     String[] parts = key.split("\\.");
                     if (parts.length >= 3) {
                        return parts[2];
                     }
                  }

                  if (key != null && (key.contains("owned_pokemon") || key.contains("enemy_pokemon") || key.contains("wild_pokemon"))) {
                     CobblemonBattleInfoClient.debug(
                        "[BattleMessageSubscriber] safeExtractPokemonNameFromArgs: Found wrapper key '{}', checking nested args for species", new Object[]{key}
                     );
                     Object[] nestedArgs = tc.method_11023();
                     if (nestedArgs != null) {
                        for (int i = 0; i < nestedArgs.length; i++) {
                           Object nestedArg = nestedArgs[i];
                           if (nestedArg instanceof class_2561) {
                              class_2561 nestedComp = (class_2561)nestedArg;
                              class_7417 nestedKey = nestedComp.method_10851();
                              if (nestedKey instanceof class_2588) {
                                 class_2588 nestedTc = (class_2588)nestedKey;
                                 String nestedKeyx = nestedTc.method_11022();
                                 if (nestedKeyx != null && nestedKeyx.startsWith("cobblemon.species.")) {
                                    String[] parts = nestedKeyx.split("\\.");
                                    if (parts.length >= 3) {
                                       return parts[2];
                                    }
                                 }
                              }
                           }
                        }

                        CobblemonBattleInfoClient.debug(
                           "[BattleMessageSubscriber] safeExtractPokemonNameFromArgs: No species key found in nested args", new Object[0]
                        );
                     }
                  }

                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] safeExtractPokemonNameFromArgs: No species key found, falling back to getString()", new Object[0]
                  );
               }

               String fallback = c.getString();
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] safeExtractPokemonNameFromArgs: FALLBACK to localized string: '{}'", new Object[]{fallback}
               );
               return fallback;
            }
         } else {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] safeExtractPokemonNameFromArgs: args null or idx out of bounds", new Object[0]);
            return null;
         }
      } catch (Throwable var14) {
         LOGGER.error("[BattleMessageSubscriber] safeExtractPokemonNameFromArgs: Exception", var14);
         return null;
      }
   }

   private static String safeExtractMoveIdFromArgs(Object[] args, int idx) {
      try {
         if (args != null && idx < args.length) {
            Object o = args[idx];
            if (o instanceof class_2561 c && c.method_10851() instanceof class_2588 tc) {
               String[] p = tc.method_11022().split("\\.");
               return p.length >= 3 ? p[2] : tc.method_11022();
            }

            return o == null ? null : o.toString();
         } else {
            return null;
         }
      } catch (Throwable var6) {
         return null;
      }
   }

   private static String safeExtractStatIdFromArgs(Object[] args, int idx) {
      try {
         if (args != null && idx < args.length) {
            Object o = args[idx];
            if (o instanceof class_2561 c && c.method_10851() instanceof class_2588 tc) {
               String[] p = tc.method_11022().split("\\.");
               String statId = p.length >= 3 ? p[2] : tc.method_11022();
               if (statId != null) {
                  statId = normalizeBattleStatId(statId);
               }

               return statId;
            }

            return o == null ? null : normalizeBattleStatId(o.toString());
         } else {
            return null;
         }
      } catch (Throwable var7) {
         return null;
      }
   }

   private static String safeExtractTypeIdFromArgs(Object[] args, int idx) {
      try {
         if (args != null && idx < args.length) {
            Object o = args[idx];
            if (o instanceof class_2561 c && c.method_10851() instanceof class_2588 tc) {
               String[] p = tc.method_11022().split("\\.");
               return p.length >= 3 ? p[2] : tc.method_11022();
            }

            return o == null ? null : o.toString();
         } else {
            return null;
         }
      } catch (Throwable var6) {
         return null;
      }
   }

   private static String getMoveTypeFromId(String moveId) {
      try {
         if (moveId == null || moveId.isBlank()) {
            return null;
         }

         String normalizedMoveId = moveId.toLowerCase(Locale.ROOT);
         String hiddenPowerVariantType = getHiddenPowerTypeFromMoveId(normalizedMoveId);
         if (hiddenPowerVariantType != null) {
            return hiddenPowerVariantType;
         }

         if (isHiddenPowerMoveId(normalizedMoveId)) {
            CobblemonBattleInfoClient.debug(
               "[BattleMessageSubscriber] Skipping base-type lookup for '{}' because Hidden Power needs runtime Pokemon data for its exact subtype.",
               new Object[]{normalizedMoveId}
            );
            return null;
         }

         Class<?> movesClass = Class.forName("com.cobblemon.mod.common.api.moves.Moves");
         Object movesInstance = movesClass.getField("INSTANCE").get(null);
         Method getByNameMethod = movesInstance.getClass().getMethod("getByName", String.class);
         Object move = getByNameMethod.invoke(movesInstance, normalizedMoveId);
         if (move != null) {
            Object elementalType = null;

            try {
               Method getElementalTypeMethod = move.getClass().getMethod("getElementalType");
               elementalType = getElementalTypeMethod.invoke(move);
            } catch (NoSuchMethodException var13) {
               try {
                  Method getTypeMethod = move.getClass().getMethod("getType");
                  elementalType = getTypeMethod.invoke(move);
               } catch (NoSuchMethodException var12) {
                  try {
                     Field typeField = move.getClass().getField("type");
                     elementalType = typeField.get(move);
                  } catch (NoSuchFieldException var11) {
                  }
               }
            }

            if (elementalType != null) {
               Method getNameMethod = elementalType.getClass().getMethod("getName");
               return (String)getNameMethod.invoke(elementalType);
            }
         }
      } catch (Throwable var14) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Could not get move type for '{}': {}", new Object[]{moveId, var14.getMessage()});
      }

      return null;
   }

   private static boolean isHiddenPowerMoveId(String moveId) {
      return moveId == null ? false : moveId.toLowerCase(Locale.ROOT).startsWith("hiddenpower");
   }

   private static String getHiddenPowerTypeFromMoveId(String moveId) {
      if (!isHiddenPowerMoveId(moveId)) {
         return null;
      } else {
         String suffix = moveId.toLowerCase(Locale.ROOT).substring("hiddenpower".length()).replace("_", "").replace("-", "");
         if (suffix.isEmpty()) {
            return null;
         } else {
            return switch (suffix) {
               case "normal", "fire", "water", "electric", "grass", "ice", "fighting", "poison", "ground", "flying", "psychic", "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy" -> suffix;
               default -> null;
            };
         }
      }
   }

   private static void trackHiddenPowerUsage(
      String attackerKey, String localizedMoveName, String localizedMoveLine, String moveId, Object[] args, String moveSide
   ) {
      if (attackerKey != null && !attackerKey.isBlank() && localizedMoveName != null && !localizedMoveName.isBlank()) {
         if (localizedMoveLine != null && !localizedMoveLine.isBlank()) {
            hiddenPowerObservedMoveLines.computeIfAbsent(attackerKey, key -> ConcurrentHashMap.newKeySet()).add(localizedMoveLine);
         }

         String targetFull = args != null && args.length > 2 ? safeExtractPlainText(args[2]) : null;
         String targetSpecies = args != null && args.length > 2 ? safeExtractPokemonSpeciesIdLowerFromArgs(args, 2) : null;
         if ((targetSpecies == null || targetSpecies.isBlank()) && args != null && args.length > 2) {
            targetSpecies = safeExtractPokemonNameFromArgs(args, 2);
         }

         if ((targetSpecies == null || targetSpecies.isBlank()) && targetFull != null) {
            targetSpecies = stripOwnerPrefix(targetFull);
         }

         if (targetSpecies != null && !targetSpecies.isBlank()) {
            targetSpecies = normalizePokemonName(stripOwnerPrefix(targetSpecies));
         }

         String targetSide = determineSideFromFullText(targetFull, targetSpecies);
         if (targetSide == null && targetSpecies != null) {
            targetSide = determineSideFromBattle(targetSpecies);
         }

         if (targetSide == null && moveSide != null && !moveSide.isBlank()) {
            targetSide = "player".equals(moveSide) ? "opponent" : "player";
         }

         pendingHiddenPowerContext = new HiddenPowerPendingContext(
            attackerKey, localizedMoveName, localizedMoveLine, moveId, targetFull, targetSpecies, targetSide
         );
         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] Tracking Hidden Power usage for attackerKey='{}', target='{}', targetSide='{}'.",
            new Object[]{attackerKey, targetSpecies, targetSide}
         );
      } else {
         pendingHiddenPowerContext = null;
      }
   }

   private static void tryResolvePendingHiddenPowerFromEffect(String effectKind) {
      HiddenPowerPendingContext context = pendingHiddenPowerContext;
      pendingHiddenPowerContext = null;
      if (context != null && effectKind != null && !effectKind.isBlank()) {
         List<String> defenderTypes = getDisplayedTypeNamesForBattlePokemon(context.targetSide, context.targetFull, context.targetSpecies);
         if (defenderTypes.isEmpty()) {
            CobblemonBattleInfoClient.debug(
               "[BattleMessageSubscriber] Could not resolve Hidden Power defender types for attackerKey='{}'.", new Object[]{context.attackerKey}
            );
         } else {
            Set<String> effectCandidates = inferHiddenPowerCandidates(defenderTypes, effectKind);
            if (effectCandidates.isEmpty()) {
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] No Hidden Power candidates matched effect '{}' against defender types {}.",
                  new Object[]{effectKind, defenderTypes}
               );
            } else {
               Set<String> previousCandidates = hiddenPowerCandidates.get(context.attackerKey);
               Set<String> narrowedCandidates = new LinkedHashSet<>();
               if (previousCandidates != null && !previousCandidates.isEmpty()) {
                  narrowedCandidates.addAll(previousCandidates);
                  narrowedCandidates.retainAll(effectCandidates);
               } else {
                  narrowedCandidates.addAll(effectCandidates);
               }

               if (narrowedCandidates.isEmpty()) {
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] Hidden Power inference conflict for attackerKey='{}'; keeping previous candidates {} instead of empty intersection with {}.",
                     new Object[]{context.attackerKey, previousCandidates, effectCandidates}
                  );
                  if (previousCandidates == null || previousCandidates.isEmpty()) {
                     return;
                  }

                  narrowedCandidates.addAll(previousCandidates);
               }

               hiddenPowerCandidates.put(context.attackerKey, narrowedCandidates);
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] Hidden Power candidates for attackerKey='{}' narrowed to {} from effect '{}' against defender types {}.",
                  new Object[]{context.attackerKey, narrowedCandidates, effectKind, defenderTypes}
               );
               if (narrowedCandidates.size() == 1) {
                  String resolvedType = narrowedCandidates.iterator().next();
                  hiddenPowerResolvedTypes.put(context.attackerKey, resolvedType);
                  Set<String> observedMoveLines = hiddenPowerObservedMoveLines.get(context.attackerKey);
                  if (observedMoveLines != null && !observedMoveLines.isEmpty()) {
                     for (String observedMoveLine : observedMoveLines) {
                        if (observedMoveLine != null && !observedMoveLine.isBlank()) {
                           BattleMessageColorizer.registerMoveLineOverride(observedMoveLine, context.localizedMoveName, resolvedType, context.moveId);
                           BattleLogManager.refreshMoveEntriesByText(observedMoveLine);
                        }
                     }

                     CobblemonBattleInfoClient.debug(
                        "[BattleMessageSubscriber] Hidden Power resolved for attackerKey='{}' as '{}'.", new Object[]{context.attackerKey, resolvedType}
                     );
                  }
               }
            }
         }
      }
   }

   private static String buildHiddenPowerAttackerKey(String side, String pokemonFullText, String pokemonSpecies) {
      ClientBattlePokemon battlePokemon = findActiveBattlePokemon(side, pokemonFullText, pokemonSpecies);
      if (battlePokemon != null && battlePokemon.getUuid() != null) {
         String prefix = side != null && !side.isBlank() ? side.toLowerCase(Locale.ROOT) : "unknown";
         return prefix + "_" + battlePokemon.getUuid();
      } else {
         String effectiveSide = side;
         if (side == null || side.isBlank()) {
            effectiveSide = determineSideFromBattleCandidates(pokemonFullText, pokemonSpecies);
         }

         String canonicalSpecies = resolveCanonicalSpeciesFromBattle(pokemonFullText);
         if (canonicalSpecies == null || canonicalSpecies.isBlank()) {
            canonicalSpecies = resolveCanonicalSpeciesFromBattle(pokemonSpecies);
         }

         if (canonicalSpecies == null || canonicalSpecies.isBlank()) {
            String fallbackName = pokemonSpecies != null && !pokemonSpecies.isBlank() ? pokemonSpecies : pokemonFullText;
            canonicalSpecies = normalizePokemonName(stripOwnerPrefix(fallbackName));
         }

         if (canonicalSpecies != null && !canonicalSpecies.isBlank()) {
            return effectiveSide != null && !effectiveSide.isBlank() ? makeSidedKey(effectiveSide, canonicalSpecies) : canonicalSpecies;
         } else {
            return null;
         }
      }
   }

   private static Set<String> inferHiddenPowerCandidates(List<String> defenderTypes, String effectKind) {
      Set<String> candidates = new LinkedHashSet<>();
      if (defenderTypes != null && !defenderTypes.isEmpty() && effectKind != null && !effectKind.isBlank()) {
         boolean superEffective = "supereffective".equals(effectKind);
         boolean resisted = "resisted".equals(effectKind);
         if (!superEffective && !resisted) {
            return candidates;
         } else {
            for (String candidateType : HIDDEN_POWER_TYPES) {
               double multiplier = getTypeEffectivenessMultiplier(candidateType, defenderTypes);
               if (superEffective && multiplier > 1.0) {
                  candidates.add(candidateType);
               } else if (resisted && multiplier > 0.0 && multiplier < 1.0) {
                  candidates.add(candidateType);
               }
            }

            return candidates;
         }
      } else {
         return candidates;
      }
   }

   private static double getTypeEffectivenessMultiplier(String attackType, List<String> defenderTypes) {
      ElementalType attackerElementalType = TypeIconRenderer.stringToElementalType(attackType);
      if (attackerElementalType != null && defenderTypes != null) {
         double multiplier = 1.0;

         for (String defenderTypeName : defenderTypes) {
            ElementalType defenderElementalType = TypeIconRenderer.stringToElementalType(defenderTypeName);
            if (defenderElementalType != null) {
               multiplier *= StrongBattleAIKt.getDamageMultiplier(attackerElementalType, defenderElementalType);
            }
         }

         return multiplier;
      } else {
         return 1.0;
      }
   }

   private static List<String> getDisplayedTypeNamesForBattlePokemon(String side, String pokemonFullText, String pokemonSpecies) {
      ClientBattlePokemon battlePokemon = findActiveBattlePokemon(side, pokemonFullText, pokemonSpecies);
      if (battlePokemon == null) {
         return Collections.emptyList();
      } else {
         String effectiveSide = side;
         if (side == null || side.isBlank()) {
            effectiveSide = determineSideFromBattleCandidates(pokemonFullText, pokemonSpecies);
         }

         if (effectiveSide == null || effectiveSide.isBlank()) {
            effectiveSide = battlePokemon.getActor() != null
                  && battlePokemon.getActor().getSide() != null
                  && battlePokemon.getActor().getSide().equals(BattleSideHelper.getPlayerSide(CobblemonClient.INSTANCE.getBattle()))
               ? "player"
               : "opponent";
         }

         String canonicalSpecies = normalizePokemonName(battlePokemon.getSpecies().getName());
         List<ElementalType> displayedTypes = TypeIconRenderer.getDisplayTypes(
            battlePokemon, canonicalSpecies, effectiveSide, battlePokemon.getUuid().toString()
         );
         if (displayedTypes != null && !displayedTypes.isEmpty()) {
            List<String> result = new ArrayList<>();

            for (ElementalType displayedType : displayedTypes) {
               if (displayedType != null && displayedType.getName() != null) {
                  result.add(displayedType.getName().toLowerCase(Locale.ROOT));
               }
            }

            return result;
         } else {
            return Collections.emptyList();
         }
      }
   }

   private static ClientBattlePokemon findActiveBattlePokemon(String preferredSide, String pokemonFullText, String pokemonSpecies) {
      try {
         ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
         if (battle == null) {
            return null;
         }

         Set<String> searchCandidates = new LinkedHashSet<>();
         searchCandidates.addAll(buildBattleSearchCandidates(pokemonFullText));
         searchCandidates.addAll(buildBattleSearchCandidates(pokemonSpecies));
         if (searchCandidates.isEmpty()) {
            return null;
         }

         ClientBattleSide playerSide = BattleSideHelper.getPlayerSide(battle);
         ClientBattleSide opponentSide = BattleSideHelper.getOpponentSide(battle);
         if ("player".equals(preferredSide)) {
            ClientBattlePokemon playerMatch = findActiveBattlePokemonOnSide(playerSide, searchCandidates);
            if (playerMatch != null) {
               return playerMatch;
            }
         } else if ("opponent".equals(preferredSide)) {
            ClientBattlePokemon opponentMatch = findActiveBattlePokemonOnSide(opponentSide, searchCandidates);
            if (opponentMatch != null) {
               return opponentMatch;
            }
         }

         ClientBattlePokemon playerMatch = findActiveBattlePokemonOnSide(playerSide, searchCandidates);
         ClientBattlePokemon opponentMatch = findActiveBattlePokemonOnSide(opponentSide, searchCandidates);
         if (playerMatch != null && opponentMatch == null) {
            return playerMatch;
         }

         if (opponentMatch != null && playerMatch == null) {
            return opponentMatch;
         }

         if (playerMatch != null && opponentMatch != null && Objects.equals(playerMatch.getUuid(), opponentMatch.getUuid())) {
            return playerMatch;
         }
      } catch (Throwable var9) {
         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] Failed to resolve active battle Pokemon for '{}' / '{}': {}",
            new Object[]{pokemonFullText, pokemonSpecies, var9.getMessage()}
         );
      }

      return null;
   }

   private static ClientBattlePokemon findActiveBattlePokemonOnSide(ClientBattleSide side, Set<String> searchCandidates) {
      if (side != null && searchCandidates != null && !searchCandidates.isEmpty()) {
         for (ClientBattleActor actor : side.getActors()) {
            for (ActiveClientBattlePokemon activePokemon : actor.getActivePokemon()) {
               if (activePokemon != null) {
                  ClientBattlePokemon battlePokemon = activePokemon.getBattlePokemon();
                  if (battlePokemon != null) {
                     for (String searchCandidate : searchCandidates) {
                        if (searchCandidate != null && !searchCandidate.isBlank() && matchesBattlePokemonSearch(battlePokemon, searchCandidate)) {
                           return battlePokemon;
                        }
                     }
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean matchesBattlePokemonSearch(ClientBattlePokemon battlePokemon, String normalizedSearch) {
      if (battlePokemon != null && normalizedSearch != null && !normalizedSearch.isBlank()) {
         String canonicalSpecies = normalizePokemonName(battlePokemon.getSpecies().getName());
         String normalizedDisplayName = normalizePokemonName(battlePokemon.getDisplayName().getString());
         String strippedSearch = stripBattleFormPrefixes(normalizedSearch);
         String strippedDisplay = stripBattleFormPrefixes(normalizedDisplayName);
         String strippedSpecies = stripBattleFormPrefixes(canonicalSpecies);
         if (!normalizedSearch.equals(normalizedDisplayName)
            && !normalizedSearch.equals(canonicalSpecies)
            && !strippedSearch.equals(normalizedDisplayName)
            && !strippedSearch.equals(canonicalSpecies)
            && !normalizedSearch.equals(strippedDisplay)
            && !normalizedSearch.equals(strippedSpecies)
            && !strippedSearch.equals(strippedDisplay)
            && !strippedSearch.equals(strippedSpecies)) {
            return normalizedDisplayName == null
                  || !normalizedDisplayName.contains(normalizedSearch)
                     && !normalizedSearch.contains(normalizedDisplayName)
                     && !normalizedDisplayName.contains(strippedSearch)
                     && !strippedSearch.contains(normalizedDisplayName)
               ? canonicalSpecies != null
                  && (
                     canonicalSpecies.contains(normalizedSearch)
                        || normalizedSearch.contains(canonicalSpecies)
                        || canonicalSpecies.contains(strippedSearch)
                        || strippedSearch.contains(canonicalSpecies)
                  )
               : true;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private static void tryApplyVolatileStatusFromArgs(String effectId, Object[] args) {
      try {
         String pokemonFull = safeExtractPlainText(args[0]);
         String pokemonName = safeExtractPokemonNameFromArgs(args, 0);
         if (pokemonName == null || pokemonName.isBlank()) {
            return;
         }

         pokemonName = stripOwnerPrefix(pokemonName);
         String lowerName = pokemonName.toLowerCase();
         String side = pokemonFull != null ? ownerTextToSide.get(pokemonFull.toLowerCase(Locale.ROOT)) : null;
         if (side == null && pokemonFull != null) {
            boolean hasOwnerPrefix = false;
            if (localPlayerName != null && !localPlayerName.isBlank()) {
               String fullLower = pokemonFull.toLowerCase(Locale.ROOT);
               String nameLower = localPlayerName.toLowerCase(Locale.ROOT);
               hasOwnerPrefix = fullLower.contains(nameLower);
            }

            if (hasOwnerPrefix) {
               side = "player";
            } else if (localPlayerPokemon.contains(lowerName)) {
               side = "player";
            } else {
               side = "opponent";
            }
         }

         String storageKey = side != null ? makeSidedKey(side, lowerName) : lowerName;
         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] Processing effect '{}' for pokemon '{}' (key: {}, side: {})",
            new Object[]{effectId.toLowerCase(), pokemonName, storageKey, side}
         );
         String var22 = effectId.toLowerCase();
         switch (var22) {
            case "leechseed":
               leechSeeded.put(storageKey, true);
               break;
            case "ingrain":
               ingrained.put(storageKey, true);
               break;
            case "aquaring":
               aquaRing.put(storageKey, true);
               break;
            case "substitute":
               hasSubstitute.put(storageKey, true);
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] {} created a Substitute! [key: {}, side: {}]", new Object[]{pokemonName, storageKey, side}
               );
               break;
            case "embargo":
               embargo.put(storageKey, 5);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} is under Embargo! (5 turns) [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "healblock":
               healBlock.put(storageKey, 5);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} is under Heal Block! (5 turns) [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "magnetrise":
               magnetRise.put(storageKey, 5);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} used Magnet Rise! (5 turns) [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "telekinesis":
               telekinesis.put(storageKey, 3);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} is under Telekinesis! (3 turns) [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "smackdown":
               smackDownGrounded.put(storageKey, true);
               magnetRise.remove(storageKey);
               telekinesis.remove(storageKey);
               magnetRise.remove(lowerName);
               telekinesis.remove(lowerName);
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] {} was hit by Smack Down! Now grounded. [key: {}]", new Object[]{pokemonName, storageKey}
               );
               break;
            case "typechange":
               if (args.length >= 2) {
                  String typeId = safeExtractTypeIdFromArgs(args, 1);
                  if (typeId == null || typeId.isBlank()) {
                     typeId = safeExtractPlainText(args[1]);
                  }

                  if (typeId != null && !typeId.isBlank()) {
                     if (typeId.contains("???")) {
                        String effectiveSide = side;
                        String trackedSide = lastBurnUpDoubleShockSide.get(lowerName);
                        if (trackedSide != null) {
                           effectiveSide = trackedSide;
                           CobblemonBattleInfoClient.debug(
                              "[BattleMessageSubscriber] Using tracked side '{}' for {} Burn Up/Double Shock (was: {})",
                              new Object[]{trackedSide, lowerName, side}
                           );
                           lastBurnUpDoubleShockSide.remove(lowerName);
                        }

                        String effectiveKey = makeSidedKey(effectiveSide, lowerName);
                        burnedUp.put(effectiveKey, true);
                        if (typeId.contains("/")) {
                           String[] typeParts = typeId.split("/");

                           for (String part : typeParts) {
                              if (!"???".equals(part.trim()) && !part.trim().isEmpty()) {
                                 String remainingType = part.trim();
                                 remainingType = remainingType.substring(0, 1).toUpperCase() + remainingType.substring(1).toLowerCase();
                                 transformedTypes.put(effectiveKey, remainingType);
                                 break;
                              }
                           }
                        }
                     } else {
                        String normalizedType = typeId.substring(0, 1).toUpperCase() + typeId.substring(1).toLowerCase();
                        reflectTypes.remove(storageKey);
                        transformedTypes.put(storageKey, normalizedType);
                        CobblemonBattleInfoClient.debug(
                           "[BattleMessageSubscriber] {} transformed into {} type! [from typechange args] (key: {}, side: {})",
                           new Object[]{pokemonName, normalizedType, storageKey, side}
                        );
                        if ("water".equalsIgnoreCase(typeId)) {
                           soaked.put(storageKey, true);
                           CobblemonBattleInfoClient.debug(
                              "[BattleMessageSubscriber] {} was soaked! [from args] (key: {})", new Object[]{pokemonName, storageKey}
                           );
                        }
                     }
                  }
               }
               break;
            case "reflecttype":
               if (args.length >= 2) {
                  String targetFull = safeExtractPlainText(args[1]);
                  String targetName = safeExtractPokemonNameFromArgs(args, 1);
                  if (targetName == null || targetName.isBlank()) {
                     targetName = safeExtractPokemonSpeciesIdLowerFromArgs(args, 1);
                  }

                  if ((targetName == null || targetName.isBlank()) && targetFull != null && !targetFull.isBlank()) {
                     targetName = stripOwnerPrefix(targetFull);
                  }

                  if (targetName != null && !targetName.isBlank()) {
                     String targetLower = stripOwnerPrefix(targetName).toLowerCase(Locale.ROOT);
                     String targetSide = determineSideFromFullText(targetFull, targetLower);
                     String targetStorageKey = targetSide != null ? makeSidedKey(targetSide, targetLower) : targetLower;
                     reflectTypes.put(storageKey, targetStorageKey);
                     transformedTypes.remove(storageKey);
                     soaked.remove(storageKey);
                     burnedUp.remove(storageKey);
                     addedTypes.remove(storageKey);
                     CobblemonBattleInfoClient.debug(
                        "[BattleMessageSubscriber] {} copied {}'s type via Reflect Type [sourceKey: {}, targetKey: {}]",
                        new Object[]{pokemonName, targetName, storageKey, targetStorageKey}
                     );
                  }
               }
               break;
            case "typeadd":
               if (args.length >= 2) {
                  String typeId = safeExtractTypeIdFromArgs(args, 1);
                  if (typeId != null && !typeId.isBlank()) {
                     String normalizedType = typeId.substring(0, 1).toUpperCase() + typeId.substring(1).toLowerCase();
                     addedTypes.put(storageKey, normalizedType);
                     CobblemonBattleInfoClient.debug(
                        "[BattleMessageSubscriber] {} had {} type added! [from typeadd args] (key: {})", new Object[]{pokemonName, normalizedType, storageKey}
                     );
                  }
               }
               break;
            case "confusion":
               confused.put(storageKey, true);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} is now confused! [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "taunt":
               taunted.put(storageKey, true);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} is taunted! [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "encore":
               encored.put(storageKey, true);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} is under Encore! [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "saltcure":
               saltCured.put(storageKey, true);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} is afflicted by Salt Cure! [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "infestation":
               infested.put(storageKey, true);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} is trapped by Infestation! [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "curse":
               String curseTargetFull = args.length >= 2 ? safeExtractPlainText(args[1]) : null;
               String curseTargetName = args.length >= 2 ? safeExtractPokemonNameFromArgs(args, 1) : null;
               if ((curseTargetName == null || curseTargetName.isBlank()) && curseTargetFull != null && !curseTargetFull.isBlank()) {
                  curseTargetName = stripOwnerPrefix(curseTargetFull);
               }

               String curseTargetLower = curseTargetName == null ? null : stripOwnerPrefix(curseTargetName).toLowerCase(Locale.ROOT);
               String curseTargetSide = determineSideFromFullText(curseTargetFull, curseTargetLower);
               String curseTargetStorageKey = curseTargetLower != null && !curseTargetLower.isBlank()
                  ? (curseTargetSide != null ? makeSidedKey(curseTargetSide, curseTargetLower) : curseTargetLower)
                  : storageKey;
               applyGhostCurseForPokemon(curseTargetSide, curseTargetStorageKey);
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] {} put a Ghost Curse on {} [target key: {}, target side: {}]",
                  new Object[]{pokemonName, curseTargetName, curseTargetStorageKey, curseTargetSide}
               );
               break;
            case "foresight":
            case "miracleeye":
               identified.put(storageKey, true);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} was identified! [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "burnup":
               burnedUp.put(storageKey, true);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} used Burn Up and lost Fire type! [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "doubleshock":
               doubleShocked.put(storageKey, true);
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] {} used Double Shock and lost Electric type! [key: {}]", new Object[]{pokemonName, storageKey}
               );
         }
      } catch (Throwable var19) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error applying volatile status from args", new Object[]{var19});
      }
   }

   private static void tryRemoveVolatileStatusFromArgs(String effectId, Object[] args) {
      try {
         String pokemonFull = safeExtractPlainText(args[0]);
         String pokemonName = safeExtractPokemonNameFromArgs(args, 0);
         if (pokemonName == null || pokemonName.isBlank()) {
            return;
         }

         pokemonName = stripOwnerPrefix(pokemonName);
         String lowerName = pokemonName.toLowerCase();
         String side = pokemonFull != null ? ownerTextToSide.get(pokemonFull.toLowerCase(Locale.ROOT)) : null;
         if (side == null && pokemonFull != null) {
            if (pokemonFull.contains("'s ")) {
               side = "player";
            } else {
               side = "opponent";
            }
         }

         String storageKey = side != null ? makeSidedKey(side, lowerName) : lowerName;
         String var7 = effectId.toLowerCase();
         switch (var7) {
            case "leechseed":
               leechSeeded.remove(storageKey);
               leechSeeded.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} leech seed removed [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "ingrain":
               ingrained.remove(storageKey);
               ingrained.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} ingrain ended [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "aquaring":
               aquaRing.remove(storageKey);
               aquaRing.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} Aqua Ring ended [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "substitute":
               hasSubstitute.remove(storageKey);
               hasSubstitute.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} Substitute faded! [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "embargo":
               embargo.remove(storageKey);
               embargo.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} Embargo ended [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "healblock":
               healBlock.remove(storageKey);
               healBlock.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} Heal Block ended [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "magnetrise":
               magnetRise.remove(storageKey);
               magnetRise.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} Magnet Rise ended [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "telekinesis":
               telekinesis.remove(storageKey);
               telekinesis.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} Telekinesis ended [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "smackdown":
               smackDownGrounded.remove(storageKey);
               smackDownGrounded.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} is no longer grounded [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "typechange":
               reflectTypes.remove(storageKey);
               reflectTypes.remove(lowerName);
               transformedTypes.remove(storageKey);
               transformedTypes.remove(lowerName);
               soaked.remove(storageKey);
               soaked.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} type change ended [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "typeadd":
               addedTypes.remove(storageKey);
               addedTypes.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} added type removed [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "confusion":
               confused.remove(storageKey);
               confused.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} is no longer confused [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "taunt":
               taunted.remove(storageKey);
               taunted.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} is no longer taunted [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "encore":
               encored.remove(storageKey);
               encored.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} Encore ended [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "saltcure":
               saltCured.remove(storageKey);
               saltCured.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} Salt Cure ended [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "infestation":
               infested.remove(storageKey);
               infested.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} Infestation ended [key: {}]", new Object[]{pokemonName, storageKey});
               break;
            case "curse":
               removeGhostCurseForPokemon(storageKey, lowerName);
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] {} Ghost Curse ended [key: {}, side: {}]", new Object[]{pokemonName, storageKey, side}
               );
               break;
            case "foresight":
            case "miracleeye":
               identified.remove(storageKey);
               identified.remove(lowerName);
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] {} is no longer identified [from args]", new Object[]{pokemonName});
         }
      } catch (Throwable var9) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error removing volatile status from args", new Object[]{var9});
      }
   }

   private static void decrementHazardTurnsIfAny() {
   }

   private static void decrementWeatherTurnsIfActive() {
      try {
         if (currentWeather == null || currentWeather.isEmpty()) {
            return;
         }

         if (weatherTurns < 0) {
            return;
         }

         if (weatherJustSet) {
            weatherJustSet = false;
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Skipping weather decrement - just set this turn", new Object[0]);
            return;
         }

         if (weatherTurns > 0) {
            weatherTurns--;
         }
      } catch (Throwable var1) {
      }
   }

   private static void decrementTerrainTurnsIfActive() {
      try {
         if (currentTerrain == null || currentTerrain.isEmpty()) {
            return;
         }

         if (terrainStartTurn == currentTurnNumber) {
            return;
         }

         if (terrainTurns > 0) {
            terrainTurns--;
         }

         if (terrainTurns <= 0) {
            currentTerrain = "";
            terrainTurns = 0;
            terrainStartTurn = 0;
         }
      } catch (Throwable var1) {
      }
   }

   private static void decrementRoomTurnsIfActive() {
      try {
         if (trickRoomTurns > 0) {
            trickRoomTurns--;
         }

         if (trickRoomTurns < 0) {
            trickRoomTurns = 0;
         }

         if (wonderRoomTurns > 0) {
            wonderRoomTurns--;
         }

         if (wonderRoomTurns < 0) {
            wonderRoomTurns = 0;
         }

         if (magicRoomTurns > 0) {
            magicRoomTurns--;
         }

         if (magicRoomTurns < 0) {
            magicRoomTurns = 0;
         }
      } catch (Throwable var1) {
      }
   }

   private static void decrementPerishSongTurnsIfActive() {
      try {
         if (perishSongTurns <= 0) {
            return;
         }

         if (perishSongStartTurn == currentTurnNumber) {
            return;
         }

         perishSongTurns--;
         if (perishSongTurns <= 0) {
            perishSongTurns = 0;
            perishSongStartTurn = 0;
         }
      } catch (Throwable var1) {
      }
   }

   private static String extractTextFromFormattedCharSequence(Object formattedCharSeq) {
      try {
         StringBuilder sb = new StringBuilder();
         Method acceptMethod = null;
         Class<?> sinkType = null;

         for (Class<?> iface : formattedCharSeq.getClass().getInterfaces()) {
            for (Method m : iface.getMethods()) {
               if (m.getName().equals("accept") && m.getParameterCount() == 1) {
                  acceptMethod = m;
                  sinkType = m.getParameterTypes()[0];
                  break;
               }
            }

            if (acceptMethod != null) {
               break;
            }
         }

         if (acceptMethod != null && sinkType != null) {
            Object sinkProxy = Proxy.newProxyInstance(sinkType.getClassLoader(), new Class[]{sinkType}, (proxy, method, args) -> {
               if (method.getName().equals("accept")) {
                  if (args != null && args.length >= 3) {
                     int codePoint = ((Number)args[2]).intValue();
                     sb.appendCodePoint(codePoint);
                  }

                  return true;
               } else {
                  return null;
               }
            });
            acceptMethod.invoke(formattedCharSeq, sinkProxy);
            return sb.toString();
         } else {
            return null;
         }
      } catch (Exception var12) {
         return null;
      }
   }

   private static void decrementScreenTurns(String side) {
      Map<String, Integer> sideScreens = screens.get(side);
      if (sideScreens != null) {
         List<String> toExtend = new ArrayList<>();

         for (Entry<String, Integer> entry : sideScreens.entrySet()) {
            int currentTurns = entry.getValue();
            if (currentTurns > 0) {
               int newTurns = currentTurns - 1;
               entry.setValue(newTurns);
               if (newTurns == 0 && !hasLightClay) {
                  toExtend.add(entry.getKey());
               }
            }
         }

         sideScreens.entrySet().removeIf(e -> e.getValue() < 0);
      }
   }

   private static void detectLightClayAndExtendScreens() {
      for (String side : new String[]{"player", "opponent"}) {
         Map<String, Integer> sideScreens = screens.get(side);
         if (sideScreens != null) {
            for (Entry<String, Integer> entry : sideScreens.entrySet()) {
               if (entry.getValue() == 0 && !hasLightClay) {
                  hasLightClay = true;
                  entry.setValue(3);
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] Screen continued past 5 turns - Light Clay detected! Adding 3 more turns for {}", new Object[]{entry.getKey()}
                  );
               }
            }
         }
      }
   }

   private static void decrementTailwindTurns(String side) {
      if (tailwind.containsKey(side)) {
         int current = tailwind.get(side);
         if (current > 1) {
            tailwind.put(side, current - 1);
         } else {
            tailwind.remove(side);
         }
      }
   }

   public static int getWaterSportTurns() {
      return waterSportTurns;
   }

   private static void decrementWaterSportTurns() {
      if (waterSportTurns > 0) {
         waterSportTurns--;
      }
   }

   public static int getMudSportTurns() {
      return mudSportTurns;
   }

   private static void decrementMudSportTurns() {
      if (mudSportTurns > 0) {
         mudSportTurns--;
      }
   }

   public static int getMagnetRiseTurns(String pokemonName) {
      return pokemonName == null ? 0 : magnetRise.getOrDefault(pokemonName.toLowerCase(), 0);
   }

   public static int getMagnetRiseTurns(String side, String pokemonName) {
      if (pokemonName == null) {
         return 0;
      } else {
         if (side != null && !side.isEmpty()) {
            String sidedKey = makeSidedKey(side, pokemonName);
            int turns = magnetRise.getOrDefault(sidedKey, 0);
            if (turns > 0) {
               return turns;
            }
         }

         return magnetRise.getOrDefault(pokemonName.toLowerCase(), 0);
      }
   }

   public static boolean hasUngroundedEffect(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         return magnetRise.getOrDefault(lower, 0) > 0 || telekinesis.getOrDefault(lower, 0) > 0;
      }
   }

   public static boolean hasUngroundedEffect(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         if (side != null && !side.isEmpty()) {
            String sidedKey = makeSidedKey(side, pokemonName);
            if (magnetRise.getOrDefault(sidedKey, 0) > 0) {
               return true;
            }

            if (telekinesis.getOrDefault(sidedKey, 0) > 0) {
               return true;
            }
         }

         String lower = pokemonName.toLowerCase();
         return magnetRise.getOrDefault(lower, 0) > 0 || telekinesis.getOrDefault(lower, 0) > 0;
      }
   }

   public static int getUngroundedTurns(String pokemonName) {
      if (pokemonName == null) {
         return 0;
      } else {
         String lower = pokemonName.toLowerCase();
         int magnetTurns = magnetRise.getOrDefault(lower, 0);
         int teleTurns = telekinesis.getOrDefault(lower, 0);
         return Math.max(magnetTurns, teleTurns);
      }
   }

   public static boolean isMagnetRiseActive(String pokemonName) {
      return pokemonName == null ? false : magnetRise.getOrDefault(pokemonName.toLowerCase(), 0) > 0;
   }

   public static boolean isMagnetRiseActive(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         if (side != null && !side.isEmpty()) {
            String sidedKey = makeSidedKey(side, pokemonName);
            if (magnetRise.getOrDefault(sidedKey, 0) > 0) {
               return true;
            }
         }

         return magnetRise.getOrDefault(pokemonName.toLowerCase(), 0) > 0;
      }
   }

   public static boolean isTelekinesisActive(String pokemonName) {
      return pokemonName == null ? false : telekinesis.getOrDefault(pokemonName.toLowerCase(), 0) > 0;
   }

   public static boolean isTelekinesisActive(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         if (side != null && !side.isEmpty()) {
            String sidedKey = makeSidedKey(side, pokemonName);
            if (telekinesis.getOrDefault(sidedKey, 0) > 0) {
               return true;
            }
         }

         return telekinesis.getOrDefault(pokemonName.toLowerCase(), 0) > 0;
      }
   }

   public static int getTelekinesisturns(String side, String pokemonName) {
      if (pokemonName == null) {
         return 0;
      } else {
         if (side != null && !side.isEmpty()) {
            String sidedKey = makeSidedKey(side, pokemonName);
            int turns = telekinesis.getOrDefault(sidedKey, 0);
            if (turns > 0) {
               return turns;
            }
         }

         return telekinesis.getOrDefault(pokemonName.toLowerCase(), 0);
      }
   }

   private static void decrementMagnetRiseTurns() {
      magnetRise.replaceAll((k, v) -> v > 0 ? v - 1 : 0);
      magnetRise.entrySet().removeIf(e -> e.getValue() <= 0);
   }

   private static void decrementTelekinesiseTurns() {
      telekinesis.replaceAll((k, v) -> v > 0 ? v - 1 : 0);
      telekinesis.entrySet().removeIf(e -> e.getValue() <= 0);
   }

   public static boolean isGrounded(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         return gravityTurns > 0 || ingrained.getOrDefault(lower, false) || smackDownGrounded.getOrDefault(lower, false);
      }
   }

   public static int getGravityTurns() {
      return gravityTurns;
   }

   public static int getCurrentTurn() {
      return currentTurnNumber;
   }

   public static boolean isSmackDownGrounded(String pokemonName) {
      return pokemonName == null ? false : smackDownGrounded.getOrDefault(pokemonName.toLowerCase(), false);
   }

   public static boolean isSmackDownGrounded(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         if (side != null && !side.isEmpty()) {
            String sidedKey = makeSidedKey(side, pokemonName);
            Boolean val = smackDownGrounded.get(sidedKey);
            if (val != null) {
               return val;
            }
         }

         return smackDownGrounded.getOrDefault(pokemonName.toLowerCase(), false);
      }
   }

   public static int getUngroundedTurns(String side, String pokemonName) {
      if (pokemonName == null) {
         return 0;
      } else {
         if (side != null && !side.isEmpty()) {
            String sidedKey = makeSidedKey(side, pokemonName);
            int magnetTurns = magnetRise.getOrDefault(sidedKey, 0);
            int teleTurns = telekinesis.getOrDefault(sidedKey, 0);
            if (magnetTurns > 0 || teleTurns > 0) {
               return Math.max(magnetTurns, teleTurns);
            }
         }

         String lower = pokemonName.toLowerCase();
         int magnetTurns = magnetRise.getOrDefault(lower, 0);
         int teleTurns = telekinesis.getOrDefault(lower, 0);
         return Math.max(magnetTurns, teleTurns);
      }
   }

   private static void decrementGravityTurns() {
      if (gravityTurns > 0) {
         gravityTurns--;
         if (gravityTurns == 0) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Gravity wore off!", new Object[0]);
         }
      }
   }

   private static void decrementEmbargoTurns() {
      embargo.replaceAll((k, v) -> v > 0 ? v - 1 : 0);
      embargo.entrySet().removeIf(e -> e.getValue() <= 0);
   }

   private static void decrementHealBlockTurns() {
      healBlock.replaceAll((k, v) -> v > 0 ? v - 1 : 0);
      healBlock.entrySet().removeIf(e -> e.getValue() <= 0);
   }

   private static void setScreen(String side, String screen, int turns) {
      screens.computeIfAbsent(side, k -> new ConcurrentHashMap<>()).put(screen, turns);
   }

   private static void removeScreen(String side, String screen) {
      Map<String, Integer> sideScreens = screens.get(side);
      if (sideScreens != null) {
         sideScreens.remove(screen);
      }
   }

   private static void setHazard(String side, String hazard, int layers) {
      hazards.computeIfAbsent(side, k -> new ConcurrentHashMap<>()).put(hazard, layers);
   }

   private static void applyGhostCurseForPokemon(String side, String storageKey) {
      if (storageKey != null && !storageKey.isBlank()) {
         String resolvedSide = side;
         if (side == null || side.isBlank()) {
            resolvedSide = storageKey.startsWith("player_") ? "player" : (storageKey.startsWith("opponent_") ? "opponent" : "opponent");
         }

         Set<String> cursedTargets = ghostCurseTargetsBySide.computeIfAbsent(resolvedSide, k -> ConcurrentHashMap.newKeySet());
         cursedTargets.add(storageKey.toLowerCase(Locale.ROOT));
         setHazard(resolvedSide, "Curse", 1);
      }
   }

   private static void removeGhostCurseForPokemon(String storageKey, String lowerName) {
      if (storageKey != null && !storageKey.isBlank() || lowerName != null && !lowerName.isBlank()) {
         String normalizedStorageKey = storageKey == null ? null : storageKey.toLowerCase(Locale.ROOT);
         String normalizedLowerName = lowerName == null ? null : lowerName.toLowerCase(Locale.ROOT);

         for (Entry<String, Set<String>> entry : ghostCurseTargetsBySide.entrySet()) {
            String entrySide = entry.getKey();
            Set<String> cursedTargets = entry.getValue();
            if (cursedTargets != null && !cursedTargets.isEmpty()) {
               if (normalizedStorageKey != null) {
                  cursedTargets.remove(normalizedStorageKey);
               }

               if (normalizedLowerName != null) {
                  cursedTargets.remove(normalizedLowerName);
                  cursedTargets.removeIf(key -> key.endsWith("_" + normalizedLowerName));
               }

               if (cursedTargets.isEmpty()) {
                  removeHazard(entrySide, "Curse");
               } else {
                  setHazard(entrySide, "Curse", 1);
               }
            } else {
               removeHazard(entrySide, "Curse");
            }
         }
      }
   }

   private static void addHazardLayer(String side, String hazard, int maxLayers) {
      Map<String, Integer> sideHazards = hazards.computeIfAbsent(side, k -> new ConcurrentHashMap<>());
      int current = sideHazards.getOrDefault(hazard, 0);
      sideHazards.put(hazard, Math.min(current + 1, maxLayers));
   }

   private static void removeHazard(String side, String hazard) {
      Map<String, Integer> sideHazards = hazards.get(side);
      if (sideHazards != null) {
         sideHazards.remove(hazard);
      }
   }

   private static void clearAllHazards(String side) {
      Map<String, Integer> sideHazards = hazards.get(side);
      if (sideHazards != null) {
         sideHazards.clear();
      }
   }

   private static void clearStatStagesForPokemon(String pokemonName) {
      clearStatStagesForPokemon(null, pokemonName, pokemonName);
   }

   private static void clearStatStagesForPokemon(String side, String pokemonSpecies, String pokemonFullText) {
      if (pokemonSpecies != null && !pokemonSpecies.isBlank() || pokemonFullText != null && !pokemonFullText.isBlank()) {
         String canonicalSpecies = resolveCanonicalBattleSpecies(pokemonSpecies, pokemonFullText);
         Set<String> variants = new LinkedHashSet<>();
         variants.addAll(buildStatStageLookupVariants(side, pokemonSpecies));
         variants.addAll(buildStatStageLookupVariants(side, pokemonFullText));
         if (canonicalSpecies != null && !canonicalSpecies.isBlank()) {
            variants.add(canonicalSpecies);
            String strippedCanonical = stripBattleFormPrefixes(canonicalSpecies);
            if (strippedCanonical != null && !strippedCanonical.isBlank()) {
               variants.add(strippedCanonical);
            }
         }

         variants.removeIf(variant -> variant == null || variant.isBlank());
         Set<String> exactKeys = new LinkedHashSet<>(variants);
         if (canonicalSpecies != null && !canonicalSpecies.isBlank()) {
            exactKeys.addAll(buildStatStageStorageKeys(side, canonicalSpecies, pokemonSpecies, pokemonFullText));
         }

         if (side != null && !side.isBlank()) {
            for (String variant : variants) {
               exactKeys.add(makeSidedKey(side, variant));
            }
         } else {
            for (String variant : variants) {
               exactKeys.add(makeSidedKey("player", variant));
               exactKeys.add(makeSidedKey("opponent", variant));
            }
         }

         exactKeys.removeIf(key -> key == null || key.isBlank());

         for (String exactKey : exactKeys) {
            statStages.remove(exactKey);
         }

         statStages.entrySet().removeIf(entry -> {
            String key = entry.getKey();
            if (key != null && !key.isBlank()) {
               String keyLower = key.toLowerCase(Locale.ROOT);
               String entrySide = null;
               String namePart = keyLower;
               if (keyLower.startsWith("player_") || keyLower.startsWith("opponent_")) {
                  int separator = keyLower.indexOf(95);
                  entrySide = keyLower.substring(0, separator);
                  namePart = keyLower.substring(separator + 1);
               }

               if (side != null && !side.isBlank() && entrySide != null && !entrySide.equalsIgnoreCase(side)) {
                  return false;
               } else {
                  for (String variant : variants) {
                     String variantLower = variant.toLowerCase(Locale.ROOT);
                     if (keyLower.equals(variantLower) || namePart.equals(variantLower) || namePart.contains(variantLower) || variantLower.contains(namePart)) {
                        return true;
                     }
                  }

                  return false;
               }
            } else {
               return false;
            }
         });
         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] Cleared stat stages for species='{}', full='{}', side='{}' using variants={} exactKeys={}. Remaining={}",
            new Object[]{pokemonSpecies, pokemonFullText, side, variants, exactKeys, statStages}
         );
      }
   }

   private static boolean clearNegativeStatStages(String statKey, String pokemonName) {
      if (statKey == null) {
         return false;
      } else {
         Map<String, Integer> stages = statStages.get(statKey);
         if (stages != null && !stages.isEmpty()) {
            boolean removed = stages.entrySet().removeIf(entry -> entry.getValue() != null && entry.getValue() < 0);
            if (stages.isEmpty()) {
               statStages.remove(statKey);
            }

            return removed;
         } else {
            return false;
         }
      }
   }

   private static void reset() {
      currentWeather = "";
      currentTerrain = "";
      weatherTurns = 0;
      terrainTurns = 0;
      trickRoomTurns = 0;
      perishSongTurns = 0;
      weatherStartTurn = 0;
      terrainStartTurn = 0;
      perishSongStartTurn = 0;
      hasWeatherRock = false;
      waterSportTurns = 0;
      mudSportTurns = 0;
      gravityTurns = 0;
      statStages.clear();
      pendingBatonPassStats.clear();
      screens.clear();
      hazards.clear();
      ghostCurseTargetsBySide.clear();
      leechSeeded.clear();
      ingrained.clear();
      aquaRing.clear();
      hasSubstitute.clear();
      confused.clear();
      identified.clear();
      taunted.clear();
      encored.clear();
      saltCured.clear();
      infested.clear();
      tailwind.clear();
      terastallized.clear();
      soaked.clear();
      burnedUp.clear();
      roosted.clear();
      megaEvolved.clear();
      magnetRise.clear();
      telekinesis.clear();
      smackDownGrounded.clear();
      pokemonMovePP.clear();
      localPlayerPokemon.clear();
      opponentPokemon.clear();
      ownerTextToSide.clear();
      transformedTypes.clear();
      heldItems.clear();
      doubleShocked.clear();
      lastBurnUpDoubleShockSide.clear();
      hiddenPowerCandidates.clear();
      hiddenPowerResolvedTypes.clear();
      hiddenPowerObservedMoveLines.clear();
      wonderRoomTurns = 0;
      magicRoomTurns = 0;
      hasLightClay = false;
      embargo.clear();
      healBlock.clear();
      subscribed = false;
      lastBattle = null;
      lastTurnNumber = 0;
      currentTurnNumber = 0;
      lastPokemonName = null;
      currentActivePlayerPokemon = null;
      currentActiveOpponentPokemon = null;
      pendingStatMessage = null;
      pendingSoakPokemon = null;
      pendingBurnUpPokemon = null;
      pendingDoubleShockPokemon = null;
      pendingMagnetRisePokemon = null;
      pendingTelekinesisMessage = null;
      pendingHealBlockPokemon = null;
      pendingEmbargoPokemon = null;
      pendingSafeguardSide = null;
      pendingLuckyChantSide = null;
      pendingMistSide = null;
      pendingSendOutTrainer = null;
      pendingTransformTypePokemon = null;
      pendingTransformTypeName = null;
      pendingAddedType = null;
      pendingReflectTypePokemon = null;
      pendingReflectTypeTarget = null;
      pendingMeloettaFormPokemon = null;
      pendingHiddenPowerContext = null;
      pendingSwapBoostContext = null;
      addedTypes.clear();
      reflectTypes.clear();
      meloettaForm.clear();
      messageBuffer.setLength(0);
      lastBufferMessageTime = 0L;
      TurnCounterRenderer.reset();
      BattleMessageColorizer.clearOpponentPokemon();
      BattleMessageColorizer.clearBattleScopedMetadata();
   }

   private static boolean sideContainsPokemon(ClientBattleSide side, String normalizedSearch) {
      return findCanonicalSpeciesOnSide(side, normalizedSearch) != null;
   }

   private static String findCanonicalSpeciesOnSide(ClientBattleSide side, String normalizedSearch) {
      if (side != null && normalizedSearch != null && !normalizedSearch.isBlank()) {
         for (ClientBattleActor actor : side.getActors()) {
            for (ActiveClientBattlePokemon activePokemon : actor.getActivePokemon()) {
               if (activePokemon != null) {
                  ClientBattlePokemon battlePokemon = activePokemon.getBattlePokemon();
                  if (battlePokemon != null) {
                     String canonicalSpecies = normalizePokemonName(battlePokemon.getSpecies().getName());
                     String normalizedDisplayName = normalizePokemonName(battlePokemon.getDisplayName().getString());
                     String strippedSearch = stripBattleFormPrefixes(normalizedSearch);
                     String strippedDisplay = stripBattleFormPrefixes(normalizedDisplayName);
                     String strippedSpecies = stripBattleFormPrefixes(canonicalSpecies);
                     if (!normalizedSearch.equals(normalizedDisplayName)
                        && !normalizedSearch.equals(canonicalSpecies)
                        && !strippedSearch.equals(normalizedDisplayName)
                        && !strippedSearch.equals(canonicalSpecies)
                        && !normalizedSearch.equals(strippedDisplay)
                        && !normalizedSearch.equals(strippedSpecies)
                        && !strippedSearch.equals(strippedDisplay)
                        && !strippedSearch.equals(strippedSpecies)) {
                        if (normalizedDisplayName == null
                           || !normalizedDisplayName.contains(normalizedSearch)
                              && !normalizedSearch.contains(normalizedDisplayName)
                              && !normalizedDisplayName.contains(strippedSearch)
                              && !strippedSearch.contains(normalizedDisplayName)) {
                           if (canonicalSpecies == null
                              || !canonicalSpecies.contains(normalizedSearch)
                                 && !normalizedSearch.contains(canonicalSpecies)
                                 && !canonicalSpecies.contains(strippedSearch)
                                 && !strippedSearch.contains(canonicalSpecies)) {
                              continue;
                           }

                           return canonicalSpecies;
                        }

                        return canonicalSpecies;
                     }

                     return canonicalSpecies;
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static Set<String> buildBattleSearchCandidates(String pokemonName) {
      Set<String> candidates = new LinkedHashSet<>();
      if (pokemonName != null && !pokemonName.isBlank()) {
         String normalized = normalizePokemonName(pokemonName);
         if (normalized != null && !normalized.isBlank()) {
            candidates.add(normalized);
            String stripped = stripBattleFormPrefixes(normalized);
            if (!stripped.isBlank()) {
               candidates.add(stripped);
            }
         }

         String strippedOwner = stripOwnerPrefix(pokemonName);
         String normalizedOwner = normalizePokemonName(strippedOwner);
         if (normalizedOwner != null && !normalizedOwner.isBlank()) {
            candidates.add(normalizedOwner);
            String stripped = stripBattleFormPrefixes(normalizedOwner);
            if (!stripped.isBlank()) {
               candidates.add(stripped);
            }
         }

         return candidates;
      } else {
         return candidates;
      }
   }

   private static String resolveCanonicalBattleSpecies(String pokemonSpecies, String pokemonFullText) {
      String canonicalSpecies = resolveCanonicalSpeciesFromBattle(pokemonFullText);
      if (canonicalSpecies == null || canonicalSpecies.isBlank()) {
         canonicalSpecies = resolveCanonicalSpeciesFromBattle(pokemonSpecies);
      }

      if (canonicalSpecies == null || canonicalSpecies.isBlank()) {
         String fallbackName = pokemonSpecies != null && !pokemonSpecies.isBlank() ? pokemonSpecies : pokemonFullText;
         canonicalSpecies = normalizePokemonName(stripOwnerPrefix(fallbackName));
      }

      canonicalSpecies = stripBattleFormPrefixes(canonicalSpecies);
      return canonicalSpecies != null && !canonicalSpecies.isBlank() ? canonicalSpecies : null;
   }

   private static String buildPrimaryStatStageStorageKey(String side, String canonicalSpecies, String... pokemonReferences) {
      String resolvedCanonical = canonicalSpecies;
      if ((canonicalSpecies == null || canonicalSpecies.isBlank()) && pokemonReferences != null) {
         for (String pokemonReference : pokemonReferences) {
            resolvedCanonical = resolveCanonicalBattleSpecies(pokemonReference, pokemonReference);
            if (resolvedCanonical != null && !resolvedCanonical.isBlank()) {
               break;
            }
         }
      }

      if ((resolvedCanonical == null || resolvedCanonical.isBlank()) && pokemonReferences != null) {
         for (String pokemonReferencex : pokemonReferences) {
            for (String candidate : buildBattleSearchCandidates(pokemonReferencex)) {
               if (candidate != null && !candidate.isBlank()) {
                  resolvedCanonical = candidate;
                  break;
               }
            }

            if (resolvedCanonical != null && !resolvedCanonical.isBlank()) {
               break;
            }
         }
      }

      if (resolvedCanonical != null && !resolvedCanonical.isBlank()) {
         resolvedCanonical = stripBattleFormPrefixes(resolvedCanonical);
         if (resolvedCanonical != null && !resolvedCanonical.isBlank()) {
            return side != null && !side.isBlank() ? makeSidedKey(side, resolvedCanonical) : resolvedCanonical;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Set<String> buildStatStageStorageKeys(String side, String canonicalSpecies, String... pokemonReferences) {
      Set<String> storageKeys = new LinkedHashSet<>();
      String primaryKey = buildPrimaryStatStageStorageKey(side, canonicalSpecies, pokemonReferences);
      if (primaryKey != null && !primaryKey.isBlank()) {
         storageKeys.add(primaryKey);
      }

      return storageKeys;
   }

   private static Map<String, Integer> getTrackedStagesSnapshot(Set<String> storageKeys) {
      if (storageKeys != null && !storageKeys.isEmpty()) {
         for (String storageKey : storageKeys) {
            if (storageKey != null && !storageKey.isBlank()) {
               Map<String, Integer> stages = statStages.get(storageKey);
               if (stages != null && !stages.isEmpty()) {
                  return new HashMap<>(stages);
               }
            }
         }

         return Collections.emptyMap();
      } else {
         return Collections.emptyMap();
      }
   }

   private static void replaceTrackedStages(Set<String> storageKeys, Map<String, Integer> newStages) {
      if (storageKeys != null && !storageKeys.isEmpty()) {
         Map<String, Integer> sanitizedStages = new HashMap<>();
         if (newStages != null) {
            for (Entry<String, Integer> entry : newStages.entrySet()) {
               String stat = normalizeBattleStatId(entry.getKey());
               Integer value = entry.getValue();
               if (stat != null && !stat.isBlank() && value != null && value != 0) {
                  sanitizedStages.put(stat, Math.max(-6, Math.min(6, value)));
               }
            }
         }

         for (String storageKey : storageKeys) {
            if (storageKey != null && !storageKey.isBlank()) {
               if (sanitizedStages.isEmpty()) {
                  statStages.remove(storageKey);
               } else {
                  Map<String, Integer> stages = statStages.computeIfAbsent(storageKey, k -> new ConcurrentHashMap<>());
                  stages.clear();
                  stages.putAll(sanitizedStages);
               }
            }
         }
      }
   }

   private static Integer resolveStatStageDelta(String severity) {
      return switch (severity) {
         case "1", "slight" -> 1;
         case "2", "sharp" -> 2;
         case "3", "drastic", "severe" -> 3;
         case "max" -> 6;
         default -> null;
      };
   }

   private static String normalizeBattleStatId(String statId) {
      if (statId == null) {
         return null;
      } else {
         String normalized = statId.toLowerCase(Locale.ROOT).trim();
         if (normalized.isBlank()) {
            return null;
         } else {
            if (normalized.startsWith("cobblemon.stat.")) {
               String[] parts = normalized.split("\\.");
               if (parts.length >= 3) {
                  normalized = parts[2];
               }
            }

            normalized = normalized.replace("_", "").replace(" ", "").replace("-", "").replace(".", "");

            return switch (normalized) {
               case "atk", "attack" -> "attack";
               case "def", "defense", "defence" -> "defence";
               case "spa", "spatk", "spattack", "specialattack", "specialatk" -> "specialattack";
               case "spd", "spdef", "spdefense", "spdefence", "specialdefense", "specialdefence" -> "specialdefence";
               case "spe", "speed" -> "speed";
               case "acc", "accuracy" -> "accuracy";
               case "eva", "evasion" -> "evasion";
               case "crit", "critical", "criticalhit", "criticalhits" -> "crit";
               default -> normalized;
            };
         }
      }
   }

   private static RevealedPokemonContext resolveRevealedPokemonContext(Object[] args, int idx) {
      if (args != null && idx < args.length) {
         Object rawPokemon = args[idx];
         String fullText = safeExtractPlainText(rawPokemon);
         String speciesId = safeExtractPokemonSpeciesIdLowerFromArgs(args, idx);
         if ((speciesId == null || speciesId.isBlank()) && fullText != null) {
            speciesId = normalizePokemonName(stripOwnerPrefix(fullText));
         }

         String side = actionSideToRevealSide(BattleParticipantResolver.resolveSide(rawPokemon));
         if (side == null) {
            side = determineSideFromFullText(fullText, speciesId);
         }

         if (side == null && speciesId != null) {
            side = determineSideFromBattle(speciesId);
         }

         if (side == null && fullText != null) {
            side = determineSideFromBattle(fullText);
         }

         String canonicalSpecies = null;
         if (side != null) {
            canonicalSpecies = resolveCanonicalSpeciesOnSide(side, speciesId != null ? speciesId : fullText);
            if ((canonicalSpecies == null || canonicalSpecies.isBlank()) && fullText != null) {
               canonicalSpecies = resolveCanonicalSpeciesOnSide(side, fullText);
            }
         }

         if ((canonicalSpecies == null || canonicalSpecies.isBlank()) && speciesId != null) {
            canonicalSpecies = resolveCanonicalSpeciesFromBattle(speciesId);
         }

         if ((canonicalSpecies == null || canonicalSpecies.isBlank()) && fullText != null) {
            canonicalSpecies = resolveCanonicalSpeciesFromBattle(fullText);
         }

         if (canonicalSpecies != null && !canonicalSpecies.isBlank()) {
            speciesId = canonicalSpecies;
         }

         if (speciesId != null && !speciesId.isBlank()) {
            speciesId = stripBattleFormPrefixes(normalizePokemonName(speciesId));
         }

         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] Reveal context idx={} full='{}' side='{}' species='{}'", new Object[]{idx, fullText, side, speciesId}
         );
         return new RevealedPokemonContext(side, speciesId, fullText);
      } else {
         return new RevealedPokemonContext(null, null, null);
      }
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private static String actionSideToRevealSide(ActionSide actionSide) {
      if (actionSide == null) {
         return null;
      } else {
         return switch (1.$SwitchMap$name$modid$client$battlelog$BattleLogEntry$ActionSide[actionSide.ordinal()]) {
            case 1 -> "player";
            case 2 -> "opponent";
            case 3 -> null;
            default -> throw new MatchException(null, null);
         };
      }
   }

   private static String stripBattleFormPrefixes(String normalizedName) {
      if (normalizedName != null && !normalizedName.isBlank()) {
         String candidate = normalizedName;
         String[] prefixes = new String[]{"gigantamax", "gmax", "dynamax", "dmax"};

         for (String prefix : prefixes) {
            if (candidate.startsWith(prefix) && candidate.length() > prefix.length()) {
               return candidate.substring(prefix.length());
            }
         }

         return candidate;
      } else {
         return "";
      }
   }

   private static String determineSideFromBattleCandidates(String pokemonFullText, String pokemonLower) {
      Set<String> candidates = new LinkedHashSet<>();
      candidates.addAll(buildBattleSearchCandidates(pokemonFullText));
      candidates.addAll(buildBattleSearchCandidates(pokemonLower));

      for (String candidate : candidates) {
         String side = determineSideFromBattle(candidate);
         if (side != null) {
            return side;
         }
      }

      return null;
   }

   private static String resolveCanonicalSpeciesFromBattle(String pokemonName) {
      if (pokemonName != null && !pokemonName.isBlank()) {
         try {
            ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
            if (battle == null) {
               return null;
            }

            for (String candidate : buildBattleSearchCandidates(pokemonName)) {
               ClientBattleSide playerSide = BattleSideHelper.getPlayerSide(battle);
               String playerMatch = findCanonicalSpeciesOnSide(playerSide, candidate);
               ClientBattleSide opponentSide = BattleSideHelper.getOpponentSide(battle);
               String opponentMatch = findCanonicalSpeciesOnSide(opponentSide, candidate);
               if (playerMatch != null && opponentMatch == null) {
                  return playerMatch;
               }

               if (opponentMatch != null && playerMatch == null) {
                  return opponentMatch;
               }

               if (playerMatch != null && opponentMatch != null && playerMatch.equals(opponentMatch)) {
                  return playerMatch;
               }
            }
         } catch (Throwable var8) {
            CobblemonBattleInfoClient.debug("[SIDE_FROM_BATTLE] Error resolving species from battle for {}: {}", new Object[]{pokemonName, var8.getMessage()});
         }

         return null;
      } else {
         return null;
      }
   }

   private static String resolveCanonicalSpeciesOnSide(String side, String pokemonName) {
      if (side != null && !side.isBlank() && pokemonName != null && !pokemonName.isBlank()) {
         try {
            ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
            if (battle == null) {
               return null;
            }

            String var4 = side.toLowerCase(Locale.ROOT);

            ClientBattleSide targetSide = switch (var4) {
               case "player" -> BattleSideHelper.getPlayerSide(battle);
               case "opponent" -> BattleSideHelper.getOpponentSide(battle);
               default -> null;
            };
            if (targetSide == null) {
               return null;
            }

            for (String candidate : buildBattleSearchCandidates(pokemonName)) {
               String match = findCanonicalSpeciesOnSide(targetSide, candidate);
               if (match != null && !match.isBlank()) {
                  return match;
               }
            }
         } catch (Throwable var7) {
            CobblemonBattleInfoClient.debug(
               "[SIDE_FROM_BATTLE] Error resolving species on side {} for {}: {}", new Object[]{side, pokemonName, var7.getMessage()}
            );
         }

         return null;
      } else {
         return null;
      }
   }

   private static Set<String> buildStatStageLookupVariants(String side, String pokemonName) {
      Set<String> variants = new LinkedHashSet<>();
      if (pokemonName != null && !pokemonName.isBlank()) {
         String lower = pokemonName.toLowerCase(Locale.ROOT).trim();
         if (!lower.isBlank()) {
            variants.add(lower);
         }

         variants.addAll(buildBattleSearchCandidates(pokemonName));
         String canonicalOnSide = resolveCanonicalSpeciesOnSide(side, pokemonName);
         if (canonicalOnSide != null && !canonicalOnSide.isBlank()) {
            variants.add(canonicalOnSide);
            String strippedCanonical = stripBattleFormPrefixes(canonicalOnSide);
            if (!strippedCanonical.isBlank()) {
               variants.add(strippedCanonical);
            }
         }

         String canonicalFromBattle = resolveCanonicalSpeciesFromBattle(pokemonName);
         if (canonicalFromBattle != null && !canonicalFromBattle.isBlank()) {
            variants.add(canonicalFromBattle);
            String strippedCanonical = stripBattleFormPrefixes(canonicalFromBattle);
            if (!strippedCanonical.isBlank()) {
               variants.add(strippedCanonical);
            }
         }

         return variants;
      } else {
         return variants;
      }
   }

   private static String determineSideFromBattle(String pokemonName) {
      if (pokemonName != null && !pokemonName.isEmpty()) {
         try {
            ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
            if (battle == null) {
               return null;
            }

            for (String normalizedSearch : buildBattleSearchCandidates(pokemonName)) {
               ClientBattleSide playerSide = BattleSideHelper.getPlayerSide(battle);
               boolean inPlayer = sideContainsPokemon(playerSide, normalizedSearch);
               ClientBattleSide opponentSide = BattleSideHelper.getOpponentSide(battle);
               boolean inOpponent = sideContainsPokemon(opponentSide, normalizedSearch);
               if (inPlayer && !inOpponent) {
                  return "player";
               }

               if (inOpponent && !inPlayer) {
                  return "opponent";
               }
            }
         } catch (Throwable var8) {
            CobblemonBattleInfoClient.debug("[SIDE_FROM_BATTLE] Error checking battle for {}: {}", new Object[]{pokemonName, var8.getMessage()});
         }

         return null;
      } else {
         return null;
      }
   }

   private static String stripOwnerPrefix(String name) {
      if (name == null) {
         return null;
      } else {
         int apostropheIdx = name.lastIndexOf("'s ");
         if (apostropheIdx > 0) {
            return name.substring(apostropheIdx + 3).trim();
         } else {
            int deIdx = name.indexOf(" de ");
            if (deIdx > 0) {
               return name.substring(0, deIdx).trim();
            } else {
               int vonIdx = name.indexOf(" von ");
               if (vonIdx > 0) {
                  return name.substring(0, vonIdx).trim();
               } else {
                  int diIdx = name.indexOf(" di ");
                  return diIdx > 0 ? name.substring(0, diIdx).trim() : name.trim();
               }
            }
         }
      }
   }

   private static String formatItemName(String itemId) {
      if (itemId != null && !itemId.isBlank()) {
         String[] words = itemId.replace("_", " ").split(" ");
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
         return itemId;
      }
   }

   public static boolean isSoaked(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         return soaked.containsKey(sidedKey);
      } else {
         return false;
      }
   }

   public static boolean isSoaked(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (soaked.containsKey(lower)) {
            return true;
         } else {
            return soaked.containsKey(makeSidedKey("player", lower)) ? true : soaked.containsKey(makeSidedKey("opponent", lower));
         }
      }
   }

   public static boolean hasDoubleShocked(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         return doubleShocked.containsKey(sidedKey);
      } else {
         return false;
      }
   }

   public static boolean hasDoubleShocked(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (doubleShocked.containsKey(lower)) {
            return true;
         } else {
            return doubleShocked.containsKey(makeSidedKey("player", lower)) ? true : doubleShocked.containsKey(makeSidedKey("opponent", lower));
         }
      }
   }

   public static String getMeloettaForm(String pokemonName) {
      if (pokemonName == null) {
         return null;
      } else {
         String form = meloettaForm.get(pokemonName.toLowerCase());
         return form != null ? form : meloettaForm.get("meloetta");
      }
   }

   public static String getMeloettaForm(String side, String pokemonName) {
      if (pokemonName == null) {
         return null;
      } else {
         if (side != null && !side.isEmpty()) {
            String sidedKey = makeSidedKey(side, pokemonName);
            String form = meloettaForm.get(sidedKey);
            if (form != null) {
               return form;
            }
         }

         return getMeloettaForm(pokemonName);
      }
   }

   private static void setTransformedType(String pokemonName, String type) {
      if (pokemonName != null && type != null) {
         String key = pokemonName.toLowerCase();
         soaked.remove(key);
         burnedUp.remove(key);
         transformedTypes.put(key, type);
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Set {} transformed type to {}", new Object[]{pokemonName, type});
      }
   }

   public static String getTransformedType(String side, String pokemonName) {
      if (pokemonName == null) {
         return null;
      } else {
         String lower = pokemonName.toLowerCase();
         String normalized = normalizePokemonName(pokemonName);
         if (side != null && !side.isEmpty()) {
            String sidedKey = makeSidedKey(side, lower);
            String result = transformedTypes.get(sidedKey);
            if (result != null) {
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] getTransformedType found '{}' for key '{}'", new Object[]{result, sidedKey});
               return result;
            }

            if (!lower.equals(normalized)) {
               String normalizedKey = makeSidedKey(side, normalized);
               result = transformedTypes.get(normalizedKey);
               if (result != null) {
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] getTransformedType found '{}' for normalized key '{}'", new Object[]{result, normalizedKey}
                  );
                  return result;
               }
            }
         }

         return null;
      }
   }

   public static String getTransformedType(String pokemonName) {
      if (pokemonName == null) {
         return null;
      } else {
         String lower = pokemonName.toLowerCase();
         String normalized = normalizePokemonName(pokemonName);
         String result = transformedTypes.get(lower);
         if (result != null) {
            return result;
         } else {
            if (!lower.equals(normalized)) {
               result = transformedTypes.get(normalized);
               if (result != null) {
                  return result;
               }
            }

            for (String name : new String[]{lower, normalized}) {
               result = transformedTypes.get(makeSidedKey("player", name));
               if (result != null) {
                  return result;
               }

               result = transformedTypes.get(makeSidedKey("opponent", name));
               if (result != null) {
                  return result;
               }
            }

            if (!transformedTypes.isEmpty()) {
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] getTransformedType lookup for '{}' (normalized: '{}') failed. Available keys: {}",
                  new Object[]{lower, normalized, transformedTypes.keySet()}
               );
            }

            return null;
         }
      }
   }

   public static String getAddedType(String side, String pokemonName) {
      if (pokemonName == null) {
         return null;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         return addedTypes.get(sidedKey);
      } else {
         return null;
      }
   }

   public static String getAddedType(String pokemonName) {
      if (pokemonName == null) {
         return null;
      } else {
         String lower = pokemonName.toLowerCase();
         String result = addedTypes.get(lower);
         if (result != null) {
            return result;
         } else {
            result = addedTypes.get(makeSidedKey("player", lower));
            return result != null ? result : addedTypes.get(makeSidedKey("opponent", lower));
         }
      }
   }

   public static String getReflectTypeTarget(String pokemonName) {
      return pokemonName == null ? null : reflectTypes.get(pokemonName.toLowerCase(Locale.ROOT));
   }

   public static String getReflectTypeTarget(String side, String pokemonName) {
      if (pokemonName == null) {
         return null;
      } else {
         if (side != null && !side.isBlank()) {
            String sidedKey = makeSidedKey(side, pokemonName);
            String target = reflectTypes.get(sidedKey);
            if (target != null) {
               return target;
            }
         }

         return reflectTypes.get(pokemonName.toLowerCase(Locale.ROOT));
      }
   }

   public static boolean hasBurnedUp(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         return burnedUp.containsKey(sidedKey);
      } else {
         return false;
      }
   }

   public static boolean hasBurnedUp(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (burnedUp.containsKey(lower)) {
            return true;
         } else if (burnedUp.containsKey(makeSidedKey("player", lower))) {
            return true;
         } else if (burnedUp.containsKey(makeSidedKey("opponent", lower))) {
            return true;
         } else {
            if (!burnedUp.isEmpty()) {
               CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] hasBurnedUp('{}') = false, burnedUp keys: {}", new Object[]{lower, burnedUp.keySet()});
            }

            return false;
         }
      }
   }

   public static boolean isRoosted(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         Integer roostTurn = roosted.get(sidedKey);
         return roostTurn != null && roostTurn == lastTurnNumber;
      } else {
         return false;
      }
   }

   public static boolean isRoosted(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         Integer roostTurn = roosted.get(lower);
         if (roostTurn != null && roostTurn == lastTurnNumber) {
            return true;
         } else {
            roostTurn = roosted.get(makeSidedKey("player", lower));
            if (roostTurn != null && roostTurn == lastTurnNumber) {
               return true;
            } else {
               roostTurn = roosted.get(makeSidedKey("opponent", lower));
               return roostTurn != null && roostTurn == lastTurnNumber;
            }
         }
      }
   }

   private static List<String> getMegaFormTypes(String pokemonName, String megaForm) {
      String lower = pokemonName.toLowerCase();
      if (lower.contains("charizard") && "x".equals(megaForm)) {
         return Arrays.asList("Fire", "Dragon");
      } else if (lower.contains("charizard") && "y".equals(megaForm)) {
         return Arrays.asList("Fire", "Flying");
      } else if (lower.contains("charizard")) {
         return null;
      } else if (lower.contains("mewtwo") && "x".equals(megaForm)) {
         return Arrays.asList("Psychic", "Fighting");
      } else if (lower.contains("mewtwo") && "y".equals(megaForm)) {
         return Arrays.asList("Psychic");
      } else if (lower.contains("ampharos")) {
         return Arrays.asList("Electric", "Dragon");
      } else if (lower.contains("aggron")) {
         return Arrays.asList("Steel");
      } else if (lower.contains("altaria")) {
         return Arrays.asList("Dragon", "Fairy");
      } else if (lower.contains("lopunny")) {
         return Arrays.asList("Normal", "Fighting");
      } else if (lower.contains("gyarados")) {
         return Arrays.asList("Water", "Dark");
      } else if (lower.contains("sceptile")) {
         return Arrays.asList("Grass", "Dragon");
      } else if (lower.contains("pinsir")) {
         return Arrays.asList("Bug", "Flying");
      } else {
         return lower.contains("audino") ? Arrays.asList("Normal", "Fairy") : null;
      }
   }

   public static List<String> getMegaTypes(String pokemonName) {
      return pokemonName == null ? null : megaEvolved.get(pokemonName.toLowerCase());
   }

   public static List<String> getMegaTypes(String side, String pokemonName) {
      if (pokemonName == null) {
         return null;
      } else {
         String nameLower = pokemonName.toLowerCase(Locale.ROOT);
         if (side != null && !side.isEmpty()) {
            String sidedKey = side.toLowerCase(Locale.ROOT) + "_" + nameLower;
            return megaEvolved.get(sidedKey);
         } else {
            return null;
         }
      }
   }

   public static boolean isMegaEvolved(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String queryKey = pokemonName.toLowerCase();
         boolean result = megaEvolved.containsKey(queryKey);
         if (!result) {
            result = megaEvolved.containsKey("player_" + queryKey) || megaEvolved.containsKey("opponent_" + queryKey);
         }

         return result;
      }
   }

   public static boolean isMegaEvolved(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String nameLower = pokemonName.toLowerCase(Locale.ROOT);
         if (side != null && !side.isEmpty()) {
            String sidedKey = side.toLowerCase(Locale.ROOT) + "_" + nameLower;
            return megaEvolved.containsKey(sidedKey);
         } else {
            return false;
         }
      }
   }

   private static String extractPokemonNameFromMessage(String text, String action) {
      String lower = text.toLowerCase();
      int actionIdx = lower.indexOf(action);
      if (actionIdx <= 0) {
         CobblemonBattleInfoClient.debug("[extractPokemonNameFromMessage] Action '{}' not found in text: '{}'", new Object[]{action, text});
         return null;
      } else {
         String beforeAction = text.substring(0, actionIdx).trim();
         CobblemonBattleInfoClient.debug("[extractPokemonNameFromMessage] Before action: '{}'", new Object[]{beforeAction});
         if (beforeAction.startsWith("The opposing ")) {
            beforeAction = beforeAction.substring("The opposing ".length());
         } else if (beforeAction.startsWith("Wild ")) {
            beforeAction = beforeAction.substring("Wild ".length());
         }

         if (beforeAction.contains("'s ")) {
            int lastApostrophe = beforeAction.lastIndexOf("'s ");
            beforeAction = beforeAction.substring(lastApostrophe + 3);
         }

         String result = beforeAction.trim();
         CobblemonBattleInfoClient.debug("[extractPokemonNameFromMessage] Extracted name: '{}'", new Object[]{result});
         return result;
      }
   }

   private static void checkPlayerWeatherRock(Object battle) {
      try {
         Method getSide1 = battle.getClass().getMethod("getSide1");
         Method getSide2 = battle.getClass().getMethod("getSide2");
         Object side1 = getSide1.invoke(battle);
         Object side2 = getSide2.invoke(battle);
         Method getRequest = battle.getClass().getMethod("getFirstUnansweredRequest");
         Object request = getRequest.invoke(battle);
         Object playerSide = side1;
         if (request != null) {
            try {
               Method getActivePokemon = request.getClass().getMethod("getActivePokemon");
               Object activePokemon = getActivePokemon.invoke(request);
               if (activePokemon != null) {
                  Method getActor = activePokemon.getClass().getMethod("getActor");
                  Object requestActor = getActor.invoke(activePokemon);
                  Method getActors = side1.getClass().getMethod("getActors");
                  Object actors1 = getActors.invoke(side1);
                  if (actors1 instanceof Iterable) {
                     boolean found = false;
                     Iterator pokemon = ((Iterable)actors1).iterator();

                     while (true) {
                        if (pokemon.hasNext()) {
                           Object actor = pokemon.next();
                           if (actor != requestActor) {
                              continue;
                           }

                           found = true;
                        }

                        playerSide = found ? side1 : side2;
                        break;
                     }
                  }
               }
            } catch (Exception var18) {
            }
         }

         Method getActors = playerSide.getClass().getMethod("getActors");
         Object actors = getActors.invoke(playerSide);
         if (actors instanceof Iterable) {
            for (Object actor : (Iterable)actors) {
               try {
                  Method getPokemon = actor.getClass().getMethod("getPokemon");
                  Object pokemonList = getPokemon.invoke(actor);
                  if (pokemonList instanceof Iterable) {
                     for (Object pokemon : (Iterable)pokemonList) {
                        if (checkPokemonHeldItemForRock(pokemon)) {
                           hasWeatherRock = true;
                           CobblemonBattleInfoClient.debug(
                              "[BattleMessageSubscriber] Weather rock detected on player's team! Weather will last 8 turns.", new Object[0]
                           );
                           return;
                        }
                     }
                  }
               } catch (Exception var17) {
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error checking actor Pokemon: {}", new Object[]{var17.getMessage()});
               }
            }
         }

         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] No weather rock found on player's team.", new Object[0]);
      } catch (Exception var19) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error checking for weather rock: {}", new Object[]{var19.getMessage()});
      }
   }

   private static boolean checkPokemonHeldItemForRock(Object pokemon) {
      try {
         for (String methodName : new String[]{"getHeldItem", "heldItem", "getItem", "item"}) {
            try {
               Method method = pokemon.getClass().getMethod(methodName);
               Object heldItem = method.invoke(pokemon);
               if (heldItem != null && isWeatherRock(heldItem)) {
                  return true;
               }
            } catch (NoSuchMethodException var11) {
            }
         }

         for (String fieldName : new String[]{"heldItem", "item", "heldItemStack"}) {
            try {
               Field field = pokemon.getClass().getDeclaredField(fieldName);
               field.setAccessible(true);
               Object heldItem = field.get(pokemon);
               if (heldItem != null && isWeatherRock(heldItem)) {
                  return true;
               }
            } catch (NoSuchFieldException var10) {
            }
         }

         try {
            Method getEffected = pokemon.getClass().getMethod("getEffectedPokemon");
            Object effectedPokemon = getEffected.invoke(pokemon);
            if (effectedPokemon != null) {
               for (String methodName : new String[]{"getHeldItem", "heldItem"}) {
                  try {
                     Method method = effectedPokemon.getClass().getMethod(methodName);
                     Object heldItem = method.invoke(effectedPokemon);
                     if (heldItem != null && isWeatherRock(heldItem)) {
                        return true;
                     }
                  } catch (NoSuchMethodException var9) {
                  }
               }
            }
         } catch (NoSuchMethodException var12) {
         }
      } catch (Exception var13) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error checking held item: {}", new Object[]{var13.getMessage()});
      }

      return false;
   }

   private static boolean isWeatherRock(Object item) {
      try {
         String itemString = item.toString().toLowerCase();
         if (itemString.contains("heat_rock")
            || itemString.contains("heatrock")
            || itemString.contains("damp_rock")
            || itemString.contains("damprock")
            || itemString.contains("icy_rock")
            || itemString.contains("icyrock")
            || itemString.contains("smooth_rock")
            || itemString.contains("smoothrock")) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Found weather rock: {}", new Object[]{itemString});
            return true;
         }

         for (String methodName : new String[]{"getId", "getRegistryName", "getName", "name"}) {
            try {
               Method method = item.getClass().getMethod(methodName);
               Object result = method.invoke(item);
               if (result != null) {
                  String resultStr = result.toString().toLowerCase();
                  if (resultStr.contains("heat_rock") || resultStr.contains("damp_rock") || resultStr.contains("icy_rock") || resultStr.contains("smooth_rock")
                     )
                   {
                     CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Found weather rock via getId: {}", new Object[]{resultStr});
                     return true;
                  }
               }
            } catch (NoSuchMethodException var9) {
            }
         }
      } catch (Exception var10) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error checking if item is weather rock: {}", new Object[]{var10.getMessage()});
      }

      return false;
   }

   public static void setHasWeatherRock(boolean hasRock) {
      hasWeatherRock = hasRock;
   }

   private static void checkPlayerLightClay(Object battle) {
      try {
         Method getSide1 = battle.getClass().getMethod("getSide1");
         Method getSide2 = battle.getClass().getMethod("getSide2");
         Object side1 = getSide1.invoke(battle);
         Object side2 = getSide2.invoke(battle);
         Method getRequest = battle.getClass().getMethod("getFirstUnansweredRequest");
         Object request = getRequest.invoke(battle);
         Object playerSide = side1;
         if (request != null) {
            try {
               Method getActivePokemon = request.getClass().getMethod("getActivePokemon");
               Object activePokemon = getActivePokemon.invoke(request);
               if (activePokemon != null) {
                  Method getActor = activePokemon.getClass().getMethod("getActor");
                  Object requestActor = getActor.invoke(activePokemon);
                  Method getActors = side1.getClass().getMethod("getActors");
                  Object actors1 = getActors.invoke(side1);
                  if (actors1 instanceof Iterable) {
                     boolean found = false;
                     Iterator pokemon = ((Iterable)actors1).iterator();

                     while (true) {
                        if (pokemon.hasNext()) {
                           Object actor = pokemon.next();
                           if (actor != requestActor) {
                              continue;
                           }

                           found = true;
                        }

                        playerSide = found ? side1 : side2;
                        break;
                     }
                  }
               }
            } catch (Exception var18) {
            }
         }

         Method getActors = playerSide.getClass().getMethod("getActors");
         Object actors = getActors.invoke(playerSide);
         if (actors instanceof Iterable) {
            for (Object actor : (Iterable)actors) {
               try {
                  Method getPokemon = actor.getClass().getMethod("getPokemon");
                  Object pokemonList = getPokemon.invoke(actor);
                  if (pokemonList instanceof Iterable) {
                     for (Object pokemon : (Iterable)pokemonList) {
                        if (checkPokemonHeldItemForLightClay(pokemon)) {
                           hasLightClay = true;
                           CobblemonBattleInfoClient.debug(
                              "[BattleMessageSubscriber] Light Clay detected on player's team! Screens will last 8 turns.", new Object[0]
                           );
                           return;
                        }
                     }
                  }
               } catch (Exception var17) {
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error checking actor Pokemon for Light Clay: {}", new Object[]{var17.getMessage()});
               }
            }
         }

         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] No Light Clay found on player's team.", new Object[0]);
      } catch (Exception var19) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error checking for Light Clay: {}", new Object[]{var19.getMessage()});
      }
   }

   private static boolean checkPokemonHeldItemForLightClay(Object pokemon) {
      try {
         for (String methodName : new String[]{"getHeldItem", "heldItem", "getItem", "item"}) {
            try {
               Method method = pokemon.getClass().getMethod(methodName);
               Object heldItem = method.invoke(pokemon);
               if (heldItem != null && isLightClay(heldItem)) {
                  return true;
               }
            } catch (NoSuchMethodException var11) {
            }
         }

         for (String fieldName : new String[]{"heldItem", "item", "heldItemStack"}) {
            try {
               Field field = pokemon.getClass().getDeclaredField(fieldName);
               field.setAccessible(true);
               Object heldItem = field.get(pokemon);
               if (heldItem != null && isLightClay(heldItem)) {
                  return true;
               }
            } catch (NoSuchFieldException var10) {
            }
         }

         try {
            Method getEffected = pokemon.getClass().getMethod("getEffectedPokemon");
            Object effectedPokemon = getEffected.invoke(pokemon);
            if (effectedPokemon != null) {
               for (String methodName : new String[]{"getHeldItem", "heldItem"}) {
                  try {
                     Method method = effectedPokemon.getClass().getMethod(methodName);
                     Object heldItem = method.invoke(effectedPokemon);
                     if (heldItem != null && isLightClay(heldItem)) {
                        return true;
                     }
                  } catch (NoSuchMethodException var9) {
                  }
               }
            }
         } catch (NoSuchMethodException var12) {
         }
      } catch (Exception var13) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error checking held item for Light Clay: {}", new Object[]{var13.getMessage()});
      }

      return false;
   }

   private static boolean isLightClay(Object item) {
      try {
         String itemString = item.toString().toLowerCase();
         if (itemString.contains("light_clay") || itemString.contains("lightclay")) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Found Light Clay: {}", new Object[]{itemString});
            return true;
         }

         for (String methodName : new String[]{"getId", "getRegistryName", "getName", "name"}) {
            try {
               Method method = item.getClass().getMethod(methodName);
               Object result = method.invoke(item);
               if (result != null) {
                  String resultStr = result.toString().toLowerCase();
                  if (resultStr.contains("light_clay") || resultStr.contains("lightclay")) {
                     CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Found Light Clay via getId: {}", new Object[]{resultStr});
                     return true;
                  }
               }
            } catch (NoSuchMethodException var9) {
            }
         }
      } catch (Exception var10) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error checking if item is Light Clay: {}", new Object[]{var10.getMessage()});
      }

      return false;
   }

   public static void setHasLightClay(boolean hasClay) {
      hasLightClay = hasClay;
   }

   private static void extractOpponentPokemonFromBattle(Object battle) {
      try {
         Method getSide1 = battle.getClass().getMethod("getSide1");
         Method getSide2 = battle.getClass().getMethod("getSide2");
         Object side1 = getSide1.invoke(battle);
         Object side2 = getSide2.invoke(battle);
         Method getRequest = battle.getClass().getMethod("getFirstUnansweredRequest");
         Object request = getRequest.invoke(battle);
         Object opponentSide = side2;
         if (request != null) {
            try {
               Method getActivePokemon = request.getClass().getMethod("getActivePokemon");
               Object activePokemon = getActivePokemon.invoke(request);
               if (activePokemon != null) {
                  Method getActor = activePokemon.getClass().getMethod("getActor");
                  Object requestActor = getActor.invoke(activePokemon);
                  Method getActors = side1.getClass().getMethod("getActors");
                  Object actors1 = getActors.invoke(side1);
                  if (actors1 instanceof Iterable) {
                     boolean foundOnSide1 = false;
                     Iterator activePokemonx = ((Iterable)actors1).iterator();

                     while (true) {
                        if (activePokemonx.hasNext()) {
                           Object actor = activePokemonx.next();
                           if (actor != requestActor) {
                              continue;
                           }

                           foundOnSide1 = true;
                        }

                        if (foundOnSide1) {
                           opponentSide = side2;
                        } else {
                           opponentSide = side1;
                        }
                        break;
                     }
                  }
               }
            } catch (Exception var24) {
            }
         }

         Method getActors = opponentSide.getClass().getMethod("getActors");
         Object actors = getActors.invoke(opponentSide);
         if (actors instanceof Iterable) {
            for (Object actor : (Iterable)actors) {
               try {
                  Method getActivePokemon = actor.getClass().getMethod("getActivePokemon");
                  Object activePokemonList = getActivePokemon.invoke(actor);
                  if (activePokemonList instanceof Iterable) {
                     for (Object activePokemon : (Iterable)activePokemonList) {
                        Method getBattlePokemon = activePokemon.getClass().getMethod("getBattlePokemon");
                        Object battlePokemon = getBattlePokemon.invoke(activePokemon);
                        if (battlePokemon != null) {
                           Method getSpecies = battlePokemon.getClass().getMethod("getSpecies");
                           Object species = getSpecies.invoke(battlePokemon);
                           Method getName = species.getClass().getMethod("getName");
                           String pokemonName = (String)getName.invoke(species);
                           if (pokemonName != null && !pokemonName.isEmpty()) {
                              opponentPokemon.add(pokemonName.toLowerCase());
                              BattleMessageColorizer.registerOpponentPokemon(pokemonName);
                              CobblemonBattleInfoClient.debug(
                                 "[BattleMessageSubscriber] Registered opponent Pokemon at battle start: {}", new Object[]{pokemonName}
                              );
                           }
                        }
                     }
                  }
               } catch (Exception var23) {
                  CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error extracting opponent Pokemon: {}", new Object[]{var23.getMessage()});
               }
            }
         }
      } catch (Exception var25) {
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Error extracting opponent Pokemon from battle: {}", new Object[]{var25.getMessage()});
      }
   }

   public static String getCurrentWeather() {
      return currentWeather;
   }

   public static String getCurrentTerrain() {
      return currentTerrain;
   }

   public static int getLastTurnNumber() {
      return lastTurnNumber;
   }

   public static int getWeatherTurns() {
      return weatherTurns;
   }

   public static int getTerrainTurns() {
      return terrainTurns;
   }

   public static int getPerishSongTurns() {
      return perishSongTurns;
   }

   public static int getTrickRoomTurns() {
      return trickRoomTurns;
   }

   public static int getWonderRoomTurns() {
      return wonderRoomTurns;
   }

   public static int getMagicRoomTurns() {
      return magicRoomTurns;
   }

   public static int getEmbargoTurns(String pokemonName) {
      if (pokemonName == null) {
         return 0;
      } else {
         int turns = embargo.getOrDefault(pokemonName.toLowerCase(), 0);
         if (turns > 0) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] getEmbargoTurns('{}') = {}", new Object[]{pokemonName, turns});
         }

         return turns;
      }
   }

   public static int getEmbargoTurns(String side, String pokemonName) {
      if (pokemonName == null) {
         return 0;
      } else {
         if (side != null && !side.isEmpty()) {
            String sidedKey = makeSidedKey(side, pokemonName);
            Integer val = embargo.get(sidedKey);
            if (val != null && val > 0) {
               return val;
            }
         }

         return embargo.getOrDefault(pokemonName.toLowerCase(), 0);
      }
   }

   public static int getHealBlockTurns(String pokemonName) {
      if (pokemonName == null) {
         return 0;
      } else {
         int turns = healBlock.getOrDefault(pokemonName.toLowerCase(), 0);
         if (turns > 0) {
            CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] getHealBlockTurns('{}') = {}", new Object[]{pokemonName, turns});
         }

         return turns;
      }
   }

   public static int getHealBlockTurns(String side, String pokemonName) {
      if (pokemonName == null) {
         return 0;
      } else {
         if (side != null && !side.isEmpty()) {
            String sidedKey = makeSidedKey(side, pokemonName);
            Integer val = healBlock.get(sidedKey);
            if (val != null && val > 0) {
               return val;
            }
         }

         return healBlock.getOrDefault(pokemonName.toLowerCase(), 0);
      }
   }

   public static Map<String, Integer> getStatStagesForPokemon(String side, String name) {
      if (name == null) {
         return Collections.emptyMap();
      } else {
         String primaryKey = buildPrimaryStatStageStorageKey(side, resolveCanonicalBattleSpecies(name, name), name);
         if (primaryKey != null && !primaryKey.isBlank()) {
            Map<String, Integer> primaryResult = statStages.get(primaryKey);
            if (primaryResult != null && !primaryResult.isEmpty()) {
               CobblemonBattleInfoClient.debug(
                  "[STAT_RETRIEVE] Found canonical stats for {} (side: {}, key: {}): {}", new Object[]{name, side, primaryKey, primaryResult}
               );
               return primaryResult;
            }
         }

         Set<String> variants = buildStatStageLookupVariants(side, name);
         if (side != null && !side.isEmpty()) {
            for (String variant : variants) {
               String sidedKey = makeSidedKey(side, variant);
               Map<String, Integer> result = statStages.get(sidedKey);
               if (result != null && !result.isEmpty()) {
                  CobblemonBattleInfoClient.debug(
                     "[STAT_RETRIEVE] Found stats for {} (side: {}, key: {}, variant: {}): {}", new Object[]{name, side, sidedKey, variant, result}
                  );
                  return result;
               }
            }

            CobblemonBattleInfoClient.debug("[STAT_RETRIEVE] No sided stats found for {} (side: {}, variants: {})", new Object[]{name, side, variants});
         }

         for (String variantx : variants) {
            Map<String, Integer> result = statStages.get(variantx);
            if (result != null && !result.isEmpty()) {
               CobblemonBattleInfoClient.debug("[STAT_RETRIEVE] Found stats for {} (variant match: {}): {}", new Object[]{name, variantx, result});
               return result;
            }
         }

         for (Entry<String, Map<String, Integer>> entry : statStages.entrySet()) {
            for (String variantxx : variants) {
               if (entry.getKey().equalsIgnoreCase(variantxx)) {
                  CobblemonBattleInfoClient.debug(
                     "[STAT_RETRIEVE] Found stats for {} (case-insensitive match, key: {}, variant: {}): {}",
                     new Object[]{name, entry.getKey(), variantxx, entry.getValue()}
                  );
                  return entry.getValue();
               }
            }
         }

         CobblemonBattleInfoClient.debug(
            "[STAT_RETRIEVE] No stats found for {} (side: {}, variants: {}). All keys: {}", new Object[]{name, side, variants, statStages.keySet()}
         );
         return Collections.emptyMap();
      }
   }

   public static int getStatStage(String side, String name, String stat) {
      Map<String, Integer> stages = getStatStagesForPokemon(side, name);
      if (stages.isEmpty()) {
         return 0;
      } else {
         String normalizedStat = normalizeBattleStatId(stat);
         return normalizedStat != null && !normalizedStat.isBlank() ? stages.getOrDefault(normalizedStat, 0) : 0;
      }
   }

   public static Map<String, Integer> getStatStagesForPokemon(String name) {
      if (name == null) {
         return Collections.emptyMap();
      } else {
         String canonicalName = resolveCanonicalBattleSpecies(name, name);
         String playerPrimaryKey = buildPrimaryStatStageStorageKey("player", canonicalName, name);
         String opponentPrimaryKey = buildPrimaryStatStageStorageKey("opponent", canonicalName, name);
         if (playerPrimaryKey != null && !playerPrimaryKey.isBlank()) {
            Map<String, Integer> playerPrimary = statStages.get(playerPrimaryKey);
            if (playerPrimary != null && !playerPrimary.isEmpty()) {
               return playerPrimary;
            }
         }

         if (opponentPrimaryKey != null && !opponentPrimaryKey.isBlank()) {
            Map<String, Integer> opponentPrimary = statStages.get(opponentPrimaryKey);
            if (opponentPrimary != null && !opponentPrimary.isEmpty()) {
               return opponentPrimary;
            }
         }

         Set<String> variants = buildStatStageLookupVariants(null, name);

         for (String variant : variants) {
            Map<String, Integer> playerResult = statStages.get(makeSidedKey("player", variant));
            Map<String, Integer> opponentResult = statStages.get(makeSidedKey("opponent", variant));
            if (playerResult == null || playerResult.isEmpty() || opponentResult != null && !opponentResult.isEmpty()) {
               if (opponentResult == null || opponentResult.isEmpty() || playerResult != null && !playerResult.isEmpty()) {
                  continue;
               }

               return opponentResult;
            }

            return playerResult;
         }

         for (String variant : variants) {
            Map<String, Integer> result = statStages.get(variant);
            if (result != null && !result.isEmpty()) {
               return result;
            }
         }

         for (Entry<String, Map<String, Integer>> entry : statStages.entrySet()) {
            for (String variantx : variants) {
               if (entry.getKey().equalsIgnoreCase(variantx)) {
                  return entry.getValue();
               }
            }
         }

         return Collections.emptyMap();
      }
   }

   public static Map<String, Map<String, Integer>> getAllStatStages() {
      return statStages;
   }

   public static Map<String, Integer> getScreensForSide(String side) {
      return screens.getOrDefault(side, Collections.emptyMap());
   }

   public static Map<String, Integer> getHazardsForSide(String side) {
      return hazards.getOrDefault(side, Collections.emptyMap());
   }

   public static int getTailwindTurns(String side) {
      return tailwind.getOrDefault(side, 0);
   }

   public static String getTeraType(String side, String pokemonName) {
      if (pokemonName == null) {
         return null;
      } else {
         String lower = pokemonName.toLowerCase(Locale.ROOT);
         String normalized = normalizePokemonName(pokemonName);
         String stripped = stripBattleFormPrefixes(normalized);
         Set<String> variants = new LinkedHashSet<>();
         variants.add(lower);
         if (normalized != null && !normalized.isBlank()) {
            variants.add(normalized);
         }

         if (stripped != null && !stripped.isBlank()) {
            variants.add(stripped);
         }

         if (side != null && !side.isEmpty()) {
            for (String variant : variants) {
               String sidedKey = makeSidedKey(side, variant);
               String result = terastallized.get(sidedKey);
               if (result != null) {
                  return result;
               }
            }
         }

         for (String variantx : variants) {
            String result = terastallized.get(variantx);
            if (result != null) {
               return result;
            }
         }

         for (Entry<String, String> entry : terastallized.entrySet()) {
            String key = entry.getKey();
            if (!key.contains("_") || !key.startsWith("player_") && !key.startsWith("opponent_")) {
               for (String variantxx : variants) {
                  if (variantxx != null && !variantxx.isBlank() && (key.contains(variantxx) || variantxx.contains(key))) {
                     return entry.getValue();
                  }
               }
            }
         }

         return null;
      }
   }

   public static String getTeraType(String pokemonName) {
      if (pokemonName == null) {
         return null;
      } else {
         String lower = pokemonName.toLowerCase(Locale.ROOT);
         String normalized = normalizePokemonName(pokemonName);
         String stripped = stripBattleFormPrefixes(normalized);
         Set<String> variants = new LinkedHashSet<>();
         variants.add(lower);
         if (normalized != null && !normalized.isBlank()) {
            variants.add(normalized);
         }

         if (stripped != null && !stripped.isBlank()) {
            variants.add(stripped);
         }

         for (String variant : variants) {
            String teraType = terastallized.get(variant);
            if (teraType != null) {
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] Found Tera type '{}' for '{}' (direct match: '{}')", new Object[]{teraType, pokemonName, variant}
               );
               return teraType;
            }
         }

         for (Entry<String, String> entry : terastallized.entrySet()) {
            for (String variantx : variants) {
               if (variantx != null && !variantx.isBlank() && (entry.getKey().contains(variantx) || variantx.contains(entry.getKey()))) {
                  CobblemonBattleInfoClient.debug(
                     "[BattleMessageSubscriber] Found Tera type '{}' for '{}' (partial match: {}, variant: {})",
                     new Object[]{entry.getValue(), pokemonName, entry.getKey(), variantx}
                  );
                  return entry.getValue();
               }
            }
         }

         return null;
      }
   }

   private static boolean isTerastallizationKey(String key) {
      if (key != null && !key.isBlank()) {
         String lower = key.toLowerCase(Locale.ROOT);
         return lower.contains("terastalliz") || lower.contains("terastaliz") || lower.contains(".terastall") || lower.contains(".terastal");
      } else {
         return false;
      }
   }

   public static boolean isTerastallized(String pokemonName) {
      return getTeraType(pokemonName) != null;
   }

   public static boolean isTerastallized(String side, String pokemonName) {
      return getTeraType(side, pokemonName) != null;
   }

   public static boolean isIngrained(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         Boolean val = ingrained.get(sidedKey);
         return val != null && val;
      } else {
         return false;
      }
   }

   public static boolean isIngrained(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (ingrained.containsKey(lower) && ingrained.get(lower)) {
            return true;
         } else {
            return ingrained.containsKey(makeSidedKey("player", lower)) && ingrained.get(makeSidedKey("player", lower))
               ? true
               : ingrained.containsKey(makeSidedKey("opponent", lower)) && ingrained.get(makeSidedKey("opponent", lower));
         }
      }
   }

   public static boolean hasAquaRing(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         Boolean val = aquaRing.get(sidedKey);
         return val != null && val;
      } else {
         return false;
      }
   }

   public static boolean hasAquaRing(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (aquaRing.containsKey(lower) && aquaRing.get(lower)) {
            return true;
         } else {
            return aquaRing.containsKey(makeSidedKey("player", lower)) && aquaRing.get(makeSidedKey("player", lower))
               ? true
               : aquaRing.containsKey(makeSidedKey("opponent", lower)) && aquaRing.get(makeSidedKey("opponent", lower));
         }
      }
   }

   public static boolean hasEmbargo(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         Integer val = embargo.get(sidedKey);
         return val != null && val > 0;
      } else {
         return false;
      }
   }

   public static boolean hasEmbargo(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (embargo.containsKey(lower) && embargo.get(lower) > 0) {
            return true;
         } else {
            return embargo.containsKey(makeSidedKey("player", lower)) && embargo.get(makeSidedKey("player", lower)) > 0
               ? true
               : embargo.containsKey(makeSidedKey("opponent", lower)) && embargo.get(makeSidedKey("opponent", lower)) > 0;
         }
      }
   }

   public static boolean hasHealBlock(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         Integer val = healBlock.get(sidedKey);
         return val != null && val > 0;
      } else {
         return false;
      }
   }

   public static boolean hasHealBlock(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (healBlock.containsKey(lower) && healBlock.get(lower) > 0) {
            return true;
         } else {
            return healBlock.containsKey(makeSidedKey("player", lower)) && healBlock.get(makeSidedKey("player", lower)) > 0
               ? true
               : healBlock.containsKey(makeSidedKey("opponent", lower)) && healBlock.get(makeSidedKey("opponent", lower)) > 0;
         }
      }
   }

   public static boolean isLeechSeeded(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         Boolean val = leechSeeded.get(sidedKey);
         return val != null && val;
      } else {
         return false;
      }
   }

   public static boolean isLeechSeeded(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (leechSeeded.containsKey(lower) && leechSeeded.get(lower)) {
            return true;
         } else {
            return leechSeeded.containsKey(makeSidedKey("player", lower)) && leechSeeded.get(makeSidedKey("player", lower))
               ? true
               : leechSeeded.containsKey(makeSidedKey("opponent", lower)) && leechSeeded.get(makeSidedKey("opponent", lower));
         }
      }
   }

   public static boolean hasSubstitute(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         Boolean val = hasSubstitute.get(sidedKey);
         return val != null && val;
      } else {
         return false;
      }
   }

   public static boolean hasSubstitute(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (hasSubstitute.containsKey(lower) && hasSubstitute.get(lower)) {
            return true;
         } else {
            return hasSubstitute.containsKey(makeSidedKey("player", lower)) && hasSubstitute.get(makeSidedKey("player", lower))
               ? true
               : hasSubstitute.containsKey(makeSidedKey("opponent", lower)) && hasSubstitute.get(makeSidedKey("opponent", lower));
         }
      }
   }

   public static boolean isConfused(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         Boolean val = confused.get(sidedKey);
         return val != null && val;
      } else {
         return false;
      }
   }

   public static boolean isConfused(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (confused.containsKey(lower) && confused.get(lower)) {
            return true;
         } else {
            return confused.containsKey(makeSidedKey("player", lower)) && confused.get(makeSidedKey("player", lower))
               ? true
               : confused.containsKey(makeSidedKey("opponent", lower)) && confused.get(makeSidedKey("opponent", lower));
         }
      }
   }

   public static boolean isIdentified(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         Boolean val = identified.get(sidedKey);
         return val != null && val;
      } else {
         return false;
      }
   }

   public static boolean isIdentified(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (identified.containsKey(lower) && identified.get(lower)) {
            return true;
         } else {
            return identified.containsKey(makeSidedKey("player", lower)) && identified.get(makeSidedKey("player", lower))
               ? true
               : identified.containsKey(makeSidedKey("opponent", lower)) && identified.get(makeSidedKey("opponent", lower));
         }
      }
   }

   public static boolean hasTaunt(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         Boolean val = taunted.get(sidedKey);
         return val != null && val;
      } else {
         return false;
      }
   }

   public static boolean hasTaunt(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (taunted.containsKey(lower) && taunted.get(lower)) {
            return true;
         } else {
            return taunted.containsKey(makeSidedKey("player", lower)) && taunted.get(makeSidedKey("player", lower))
               ? true
               : taunted.containsKey(makeSidedKey("opponent", lower)) && taunted.get(makeSidedKey("opponent", lower));
         }
      }
   }

   public static boolean hasEncore(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         Boolean val = encored.get(sidedKey);
         return val != null && val;
      } else {
         return false;
      }
   }

   public static boolean hasEncore(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (encored.containsKey(lower) && encored.get(lower)) {
            return true;
         } else {
            return encored.containsKey(makeSidedKey("player", lower)) && encored.get(makeSidedKey("player", lower))
               ? true
               : encored.containsKey(makeSidedKey("opponent", lower)) && encored.get(makeSidedKey("opponent", lower));
         }
      }
   }

   public static boolean isSaltCured(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         Boolean val = saltCured.get(sidedKey);
         return val != null && val;
      } else {
         return false;
      }
   }

   public static boolean isSaltCured(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (saltCured.containsKey(lower) && saltCured.get(lower)) {
            return true;
         } else {
            return saltCured.containsKey(makeSidedKey("player", lower)) && saltCured.get(makeSidedKey("player", lower))
               ? true
               : saltCured.containsKey(makeSidedKey("opponent", lower)) && saltCured.get(makeSidedKey("opponent", lower));
         }
      }
   }

   public static boolean hasInfestation(String side, String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else if (side != null && !side.isEmpty()) {
         String sidedKey = makeSidedKey(side, pokemonName);
         Boolean val = infested.get(sidedKey);
         return val != null && val;
      } else {
         return false;
      }
   }

   public static boolean hasInfestation(String pokemonName) {
      if (pokemonName == null) {
         return false;
      } else {
         String lower = pokemonName.toLowerCase();
         if (infested.containsKey(lower) && infested.get(lower)) {
            return true;
         } else {
            return infested.containsKey(makeSidedKey("player", lower)) && infested.get(makeSidedKey("player", lower))
               ? true
               : infested.containsKey(makeSidedKey("opponent", lower)) && infested.get(makeSidedKey("opponent", lower));
         }
      }
   }

   public static boolean isLocalPlayerPokemon(String pokemonName) {
      return pokemonName == null ? false : localPlayerPokemon.contains(pokemonName.toLowerCase());
   }

   public static boolean isOpponentPokemon(String pokemonName) {
      return pokemonName == null ? false : opponentPokemon.contains(pokemonName.toLowerCase());
   }

   public static List<String> getTrackedOpponentPokemon() {
      return new ArrayList<>(opponentPokemon);
   }

   public static void clearVolatileStatus(String pokemonName) {
      if (pokemonName != null) {
         String lower = pokemonName.toLowerCase();
         leechSeeded.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         ingrained.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         aquaRing.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         hasSubstitute.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         confused.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         identified.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         taunted.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         encored.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         saltCured.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         infested.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         soaked.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         burnedUp.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         magnetRise.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         telekinesis.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         smackDownGrounded.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         transformedTypes.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         reflectTypes.entrySet().removeIf(e -> e.getKey().toLowerCase(Locale.ROOT).equals(lower) || e.getKey().toLowerCase(Locale.ROOT).endsWith("_" + lower));
         addedTypes.entrySet().removeIf(e -> e.getKey().toLowerCase().equals(lower));
         removeGhostCurseForPokemon(null, lower);
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Cleared volatile status for {}", new Object[]{pokemonName});
      }
   }

   public static void initializeMovePP(String pokemonName, String moveName, int currentPP, int maxPP) {
      String lowerPokemon = pokemonName.toLowerCase();
      String lowerMove = moveName.toLowerCase();
      Map<String, int[]> movePP = pokemonMovePP.computeIfAbsent(lowerPokemon, k -> new ConcurrentHashMap<>());
      if (!movePP.containsKey(lowerMove)) {
         movePP.put(lowerMove, new int[]{currentPP, maxPP});
         CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Initialized PP for {}'s {}: {}/{}", new Object[]{pokemonName, moveName, currentPP, maxPP});
      }
   }

   public static int[] getMovePP(String pokemonName, String moveName) {
      if (pokemonName != null && moveName != null) {
         String lowerPokemon = pokemonName.toLowerCase();
         String lowerMove = moveName.toLowerCase();
         Map<String, int[]> movePP = pokemonMovePP.get(lowerPokemon);
         return movePP == null ? null : movePP.get(lowerMove);
      } else {
         return null;
      }
   }

   public static boolean hasPPDataFor(String pokemonName) {
      return pokemonName == null ? false : pokemonMovePP.containsKey(pokemonName.toLowerCase());
   }

   public static int[] getMovePPForAnyPokemon(String moveName) {
      if (moveName == null) {
         return null;
      } else {
         String lowerMove = moveName.toLowerCase();

         for (Map<String, int[]> moveMap : pokemonMovePP.values()) {
            if (moveMap.containsKey(lowerMove)) {
               return moveMap.get(lowerMove);
            }
         }

         return null;
      }
   }

   public static void captureHeldItemIfNeeded(String pokemonName) {
      if (pokemonName != null) {
         String lowerName = pokemonName.toLowerCase();
         if (!heldItems.containsKey(lowerName)) {
            try {
               class_310 mc = class_310.method_1551();
               if (mc.field_1724 == null || mc.field_1687 == null) {
                  return;
               }

               Class<?> cobblemonClass = Class.forName("com.cobblemon.mod.common.Cobblemon");
               Field instanceField = cobblemonClass.getField("INSTANCE");
               Object cobblemonInstance = instanceField.get(null);
               if (cobblemonInstance == null) {
                  return;
               }

               Method getStorage = cobblemonInstance.getClass().getMethod("getStorage");
               Object storageInstance = getStorage.invoke(cobblemonInstance);
               if (storageInstance == null) {
                  return;
               }

               Method getPartyMethod = null;
               Class<?> secondParamClass = null;

               for (Method method : storageInstance.getClass().getMethods()) {
                  if (method.getName().equals("getParty") && method.getParameterCount() == 2) {
                     Class<?>[] params = method.getParameterTypes();
                     if (params[0] == UUID.class) {
                        getPartyMethod = method;
                        secondParamClass = params[1];
                        break;
                     }
                  }
               }

               if (getPartyMethod == null || secondParamClass == null) {
                  return;
               }

               Object secondParam = null;
               if (secondParamClass.getName().contains("class_5455") || secondParamClass.getName().contains("RegistryAccess")) {
                  secondParam = mc.field_1687.method_30349();
               }

               if (secondParam == null) {
                  return;
               }

               Object party = getPartyMethod.invoke(storageInstance, mc.field_1724.method_5667(), secondParam);
               if (party == null) {
                  return;
               }

               for (Object partyPokemon : (Iterable)party) {
                  if (partyPokemon instanceof Pokemon pkm) {
                     String speciesName = pkm.getSpecies().getName().toLowerCase();
                     if (speciesName.equals(lowerName)) {
                        class_1799 heldItem = pkm.heldItem();
                        if (heldItem != null && !heldItem.method_7960()) {
                           String itemId = heldItem.method_7909().toString();
                           heldItems.put(lowerName, itemId);
                           CobblemonBattleInfoClient.debug("[BattleMessageSubscriber] Captured held item for {}: {}", new Object[]{pokemonName, itemId});
                        } else {
                           heldItems.put(lowerName, "");
                        }
                        break;
                     }
                  }
               }
            } catch (Exception var18) {
               CobblemonBattleInfoClient.debug(
                  "[BattleMessageSubscriber] Could not capture held item for {}: {}", new Object[]{pokemonName, var18.getMessage()}
               );
            }
         }
      }
   }

   public static String getHeldItem(String pokemonName) {
      if (pokemonName == null) {
         return null;
      } else {
         captureHeldItemIfNeeded(pokemonName);
         String item = heldItems.get(pokemonName.toLowerCase());
         return item != null && !item.isEmpty() ? item : null;
      }
   }

   public static String getHeldItem(String side, String pokemonName) {
      if (pokemonName == null) {
         return null;
      } else {
         if (side != null && !side.isEmpty()) {
            String sidedKey = makeSidedKey(side, pokemonName);
            String item = heldItems.get(sidedKey);
            if (item != null && !item.isEmpty()) {
               return item;
            }
         }

         return getHeldItem(pokemonName);
      }
   }

   static {
      translationKeyDebugLogging = readBooleanToggle("battleextras.debugKeys", "BATTLEEXTRAS_DEBUG_KEYS", translationKeyDebugLogging);
      translationKeyDebugFilter = readStringToggle("battleextras.debugKeysFilter", "BATTLEEXTRAS_DEBUG_KEYS_FILTER", null);
      if (translationKeyDebugLogging) {
         CobblemonBattleInfoClient.debug(
            "[BattleMessageSubscriber] Translation-key debug logging enabled ({} / {}).", new Object[]{"battleextras.debugKeys", "BATTLEEXTRAS_DEBUG_KEYS"}
         );
      }

      cachedCobblemonClientClass = null;
      cachedCobblemonClientInstance = null;
      cachedGetBattleMethod = null;
      reflectionCacheInitialized = false;
      ACCESSOR_METHOD_NAMES = new String[]{"getComponent", "component", "getText", "getMessage", "getContents", "contents", "getArgs", "args"};
      DEBUG_ACCESSOR_METHOD_NAMES = new String[]{"getMessage", "getComponent", "message", "component", "getText", "text"};
      methodCache = new ConcurrentHashMap<>();
      methodNotFoundCache = new ConcurrentHashMap<>();
      recentAbilityPopups = new ConcurrentHashMap<>();
      lastAbilityPopupFromKeyMs = 0L;
      waterSportTurns = 0;
      mudSportTurns = 0;
   }
}
