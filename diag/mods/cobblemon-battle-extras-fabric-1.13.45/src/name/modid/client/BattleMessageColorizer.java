package name.modid.client;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import name.modid.CobblemonBattleInfoClient;
import name.modid.client.BattleLogMoveTooltipHelper.MoveTooltipData;
import name.modid.client.BattleMessageColorizer.DamageSuffixMatch;
import name.modid.client.BattleMessageColorizer.MoveLineOverride;
import name.modid.client.battlelog.BattleParticipantResolver;
import name.modid.client.battlelog.BattleLogEntry.ActionSide;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2588;
import net.minecraft.class_5250;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BattleMessageColorizer {
   private static final Logger LOGGER = LoggerFactory.getLogger(BattleMessageColorizer.class);
   private static final Map<String, Integer> TYPE_COLORS = new HashMap<>();
   private static final Map<String, String> MOVE_TO_TYPE = new HashMap<>();
   private static final int COLOR_SANDSTORM = 12756557;
   private static final int COLOR_RAIN = 6852848;
   private static final int COLOR_SUN = 16306224;
   private static final int COLOR_HAIL = 10016984;
   private static final int COLOR_SNOW = 12116192;
   private static final int COLOR_PLAYER = 5635925;
   private static final int COLOR_OPPONENT = 16733525;
   private static final int COLOR_POISON = 10502304;
   private static final int COLOR_BURN = 15761456;
   private static final int COLOR_PARALYSIS = 16306224;
   private static final int COLOR_SLEEP = 7362712;
   private static final int COLOR_FREEZE = 10016984;
   private static final int COLOR_CONFUSION = 16275592;
   private static final int COLOR_STAT_UP = 5635925;
   private static final int COLOR_STAT_DOWN = 16733525;
   private static final int COLOR_SUPER_EFFECTIVE = 5635925;
   private static final int COLOR_NOT_EFFECTIVE = 11184810;
   private static final int COLOR_CRITICAL = 16755200;
   private static final int COLOR_MISS = 16733525;
   private static final int COLOR_DAMAGE_LOW = 5635925;
   private static final int COLOR_DAMAGE_MEDIUM = 16306224;
   private static final int COLOR_DAMAGE_HIGH = 16733525;
   private static final float LOW_DAMAGE_PERCENT_THRESHOLD = 25.0F;
   private static final float HIGH_DAMAGE_PERCENT_THRESHOLD = 50.0F;
   private static final Pattern MOVE_DAMAGE_SUFFIX_PATTERN = Pattern.compile("\\((\\d+(?:\\.\\d+)?)%\\)");
   private static final Pattern TRAILING_MOVE_DAMAGE_SUFFIX_PATTERN = Pattern.compile("^(.*?)(\\s*\\((\\d+(?:\\.\\d+)?)%\\))$");
   private static final int COLOR_MEGA_EVOLUTION = 11163135;
   private static final int COLOR_ABILITY = 16766720;
   private static String playerName = null;
   private static final Set<String> opponentPokemonNames = ConcurrentHashMap.newKeySet();
   private static final Map<String, String> textToMoveType = new HashMap<>();
   private static final Map<String, String> textToMoveId = new HashMap<>();
   private static final Map<String, String> textToEffectType = new HashMap<>();
   private static final Map<String, MoveLineOverride> textToMoveLineOverride = new HashMap<>();
   private static long lastMetadataClearTime = System.currentTimeMillis();
   private static final long METADATA_CLEAR_INTERVAL = 60000L;
   private static String opponentName = null;
   private static String activeWeatherType = null;
   private static Integer lastColor = null;
   private static long lastColorTime = 0L;
   private static final long COLOR_TIMEOUT_MS = 100L;

   public static void registerMoveMetadata(String localizedMoveName, String moveType) {
      registerMoveMetadata(localizedMoveName, moveType, null);
   }

   public static void registerMoveMetadata(String localizedMoveName, String moveType, String moveId) {
      if (localizedMoveName != null && moveType != null) {
         String key = localizedMoveName.trim().toLowerCase(Locale.ROOT);
         String value = moveType.trim().toLowerCase(Locale.ROOT);
         textToMoveType.put(key, value);
         String normalizedMoveId = normalizeMoveId(moveId);
         String compactKey = normalizeMoveId(localizedMoveName);
         if (compactKey != null && !compactKey.equals(key)) {
            textToMoveType.put(compactKey, value);
         }

         if (normalizedMoveId != null) {
            textToMoveId.put(key, normalizedMoveId);
            if (compactKey != null && !compactKey.equals(key)) {
               textToMoveId.put(compactKey, normalizedMoveId);
            }
         }

         CobblemonBattleInfoClient.debug(
            "[BattleMessageColorizer] Registered move metadata: '{}' -> type '{}' (id: '{}')", new Object[]{key, value, normalizedMoveId}
         );
         clearOldMetadataIfNeeded();
      }
   }

   public static void removeMoveMetadata(String localizedMoveName) {
      if (localizedMoveName != null) {
         String key = localizedMoveName.trim().toLowerCase(Locale.ROOT);
         String compactKey = normalizeMoveId(localizedMoveName);
         if (textToMoveType.remove(key) != null) {
            textToMoveId.remove(key);
            if (compactKey != null && !compactKey.equals(key)) {
               textToMoveType.remove(compactKey);
               textToMoveId.remove(compactKey);
            }

            CobblemonBattleInfoClient.debug("[BattleMessageColorizer] Removed move metadata for '{}'", new Object[]{key});
         }

         clearOldMetadataIfNeeded();
      }
   }

   public static void registerMoveLineOverride(String localizedLineText, String localizedMoveName, String moveType) {
      registerMoveLineOverride(localizedLineText, localizedMoveName, moveType, null);
   }

   public static void registerMoveLineOverride(String localizedLineText, String localizedMoveName, String moveType, String moveId) {
      String lineKey = normalizeTextLookupKey(localizedLineText);
      String moveKey = localizedMoveName == null ? null : localizedMoveName.trim();
      String typeKey = moveType == null ? null : moveType.trim().toLowerCase(Locale.ROOT);
      String moveIdKey = normalizeMoveId(moveId);
      if (lineKey != null && moveKey != null && !moveKey.isEmpty() && typeKey != null && !typeKey.isEmpty()) {
         textToMoveLineOverride.put(lineKey, new MoveLineOverride(moveKey, typeKey, moveIdKey));
         CobblemonBattleInfoClient.debug(
            "[BattleMessageColorizer] Registered move line override: '{}' -> '{}' ({}, id={})", new Object[]{localizedLineText, moveKey, typeKey, moveIdKey}
         );
         clearOldMetadataIfNeeded();
      }
   }

   public static void clearBattleScopedMetadata() {
      textToMoveType.clear();
      textToMoveId.clear();
      textToEffectType.clear();
      textToMoveLineOverride.clear();
      lastMetadataClearTime = System.currentTimeMillis();
   }

   public static void registerEffectMetadata(String localizedEffectName, String effectType) {
      if (localizedEffectName != null && effectType != null) {
         textToEffectType.put(localizedEffectName.trim().toLowerCase(Locale.ROOT), effectType.trim().toLowerCase(Locale.ROOT));
         clearOldMetadataIfNeeded();
      }
   }

   private static void clearOldMetadataIfNeeded() {
      long now = System.currentTimeMillis();
      if (now - lastMetadataClearTime > 1800000L) {
         try {
            Class<?> cobblemonClientClass = Class.forName("com.cobblemon.mod.common.client.CobblemonClient");
            Object instance = cobblemonClientClass.getField("INSTANCE").get(null);
            Method getBattle = instance.getClass().getMethod("getBattle");
            Object battle = getBattle.invoke(instance);
            if (battle == null) {
               CobblemonBattleInfoClient.debug("[BattleMessageColorizer] Clearing metadata (30min timeout, not in battle)", new Object[0]);
               textToMoveType.clear();
               textToMoveId.clear();
               textToEffectType.clear();
               textToMoveLineOverride.clear();
               lastMetadataClearTime = now;
            }
         } catch (Throwable var6) {
         }
      }
   }

   private static void initTypeColors() {
      TYPE_COLORS.put("normal", 16777215);
      TYPE_COLORS.put("fire", 16746496);
      TYPE_COLORS.put("water", 3381759);
      TYPE_COLORS.put("electric", 16776960);
      TYPE_COLORS.put("grass", 2263842);
      TYPE_COLORS.put("ice", 11393254);
      TYPE_COLORS.put("fighting", 12595240);
      TYPE_COLORS.put("poison", 10040012);
      TYPE_COLORS.put("ground", 13808780);
      TYPE_COLORS.put("flying", 15132410);
      TYPE_COLORS.put("psychic", 14524637);
      TYPE_COLORS.put("bug", 9498256);
      TYPE_COLORS.put("rock", 9139029);
      TYPE_COLORS.put("ghost", 9109643);
      TYPE_COLORS.put("dragon", 6970061);
      TYPE_COLORS.put("dark", 7362632);
      TYPE_COLORS.put("steel", 12632256);
      TYPE_COLORS.put("fairy", 16758489);
   }

   private static void initMoveTypes() {
      MOVE_TO_TYPE.put("flamethrower", "fire");
      MOVE_TO_TYPE.put("fire blast", "fire");
      MOVE_TO_TYPE.put("ember", "fire");
      MOVE_TO_TYPE.put("fire punch", "fire");
      MOVE_TO_TYPE.put("fire fang", "fire");
      MOVE_TO_TYPE.put("heat wave", "fire");
      MOVE_TO_TYPE.put("flare blitz", "fire");
      MOVE_TO_TYPE.put("overheat", "fire");
      MOVE_TO_TYPE.put("sacred fire", "fire");
      MOVE_TO_TYPE.put("inferno", "fire");
      MOVE_TO_TYPE.put("flame wheel", "fire");
      MOVE_TO_TYPE.put("flame burst", "fire");
      MOVE_TO_TYPE.put("will-o-wisp", "fire");
      MOVE_TO_TYPE.put("sunny day", "fire");
      MOVE_TO_TYPE.put("blaze kick", "fire");
      MOVE_TO_TYPE.put("lava plume", "fire");
      MOVE_TO_TYPE.put("magma storm", "fire");
      MOVE_TO_TYPE.put("flame charge", "fire");
      MOVE_TO_TYPE.put("incinerate", "fire");
      MOVE_TO_TYPE.put("mystical fire", "fire");
      MOVE_TO_TYPE.put("heat crash", "fire");
      MOVE_TO_TYPE.put("burn up", "fire");
      MOVE_TO_TYPE.put("pyro ball", "fire");
      MOVE_TO_TYPE.put("burning jealousy", "fire");
      MOVE_TO_TYPE.put("eruption", "fire");
      MOVE_TO_TYPE.put("blast burn", "fire");
      MOVE_TO_TYPE.put("blue flare", "fire");
      MOVE_TO_TYPE.put("fusion flare", "fire");
      MOVE_TO_TYPE.put("v-create", "fire");
      MOVE_TO_TYPE.put("surf", "water");
      MOVE_TO_TYPE.put("hydro pump", "water");
      MOVE_TO_TYPE.put("water gun", "water");
      MOVE_TO_TYPE.put("waterfall", "water");
      MOVE_TO_TYPE.put("aqua tail", "water");
      MOVE_TO_TYPE.put("scald", "water");
      MOVE_TO_TYPE.put("bubble beam", "water");
      MOVE_TO_TYPE.put("rain dance", "water");
      MOVE_TO_TYPE.put("aqua jet", "water");
      MOVE_TO_TYPE.put("hydro cannon", "water");
      MOVE_TO_TYPE.put("water pulse", "water");
      MOVE_TO_TYPE.put("muddy water", "water");
      MOVE_TO_TYPE.put("origin pulse", "water");
      MOVE_TO_TYPE.put("liquidation", "water");
      MOVE_TO_TYPE.put("solar beam", "grass");
      MOVE_TO_TYPE.put("leaf blade", "grass");
      MOVE_TO_TYPE.put("razor leaf", "grass");
      MOVE_TO_TYPE.put("energy ball", "grass");
      MOVE_TO_TYPE.put("giga drain", "grass");
      MOVE_TO_TYPE.put("mega drain", "grass");
      MOVE_TO_TYPE.put("seed bomb", "grass");
      MOVE_TO_TYPE.put("wood hammer", "grass");
      MOVE_TO_TYPE.put("power whip", "grass");
      MOVE_TO_TYPE.put("leaf storm", "grass");
      MOVE_TO_TYPE.put("synthesis", "grass");
      MOVE_TO_TYPE.put("grassy terrain", "grass");
      MOVE_TO_TYPE.put("spore", "grass");
      MOVE_TO_TYPE.put("sleep powder", "grass");
      MOVE_TO_TYPE.put("stun spore", "grass");
      MOVE_TO_TYPE.put("cotton spore", "grass");
      MOVE_TO_TYPE.put("cotton down", "grass");
      MOVE_TO_TYPE.put("leech seed", "grass");
      MOVE_TO_TYPE.put("leechseed", "grass");
      MOVE_TO_TYPE.put("ingrain", "grass");
      MOVE_TO_TYPE.put("thunderbolt", "electric");
      MOVE_TO_TYPE.put("thunder", "electric");
      MOVE_TO_TYPE.put("thunder wave", "electric");
      MOVE_TO_TYPE.put("volt tackle", "electric");
      MOVE_TO_TYPE.put("wild charge", "electric");
      MOVE_TO_TYPE.put("thunder punch", "electric");
      MOVE_TO_TYPE.put("thunder fang", "electric");
      MOVE_TO_TYPE.put("discharge", "electric");
      MOVE_TO_TYPE.put("electric terrain", "electric");
      MOVE_TO_TYPE.put("volt switch", "electric");
      MOVE_TO_TYPE.put("spark", "electric");
      MOVE_TO_TYPE.put("thunder shock", "electric");
      MOVE_TO_TYPE.put("zap cannon", "electric");
      MOVE_TO_TYPE.put("bolt strike", "electric");
      MOVE_TO_TYPE.put("ice beam", "ice");
      MOVE_TO_TYPE.put("blizzard", "ice");
      MOVE_TO_TYPE.put("ice punch", "ice");
      MOVE_TO_TYPE.put("ice fang", "ice");
      MOVE_TO_TYPE.put("aurora veil", "ice");
      MOVE_TO_TYPE.put("hail", "ice");
      MOVE_TO_TYPE.put("snowscape", "ice");
      MOVE_TO_TYPE.put("icicle crash", "ice");
      MOVE_TO_TYPE.put("avalanche", "ice");
      MOVE_TO_TYPE.put("freeze-dry", "ice");
      MOVE_TO_TYPE.put("ice shard", "ice");
      MOVE_TO_TYPE.put("aurora beam", "ice");
      MOVE_TO_TYPE.put("glaciate", "ice");
      MOVE_TO_TYPE.put("close combat", "fighting");
      MOVE_TO_TYPE.put("dynamic punch", "fighting");
      MOVE_TO_TYPE.put("focus blast", "fighting");
      MOVE_TO_TYPE.put("aura sphere", "fighting");
      MOVE_TO_TYPE.put("cross chop", "fighting");
      MOVE_TO_TYPE.put("high jump kick", "fighting");
      MOVE_TO_TYPE.put("brick break", "fighting");
      MOVE_TO_TYPE.put("mach punch", "fighting");
      MOVE_TO_TYPE.put("drain punch", "fighting");
      MOVE_TO_TYPE.put("hammer arm", "fighting");
      MOVE_TO_TYPE.put("superpower", "fighting");
      MOVE_TO_TYPE.put("seismic toss", "fighting");
      MOVE_TO_TYPE.put("karate chop", "fighting");
      MOVE_TO_TYPE.put("bulk up", "fighting");
      MOVE_TO_TYPE.put("sludge bomb", "poison");
      MOVE_TO_TYPE.put("sludge wave", "poison");
      MOVE_TO_TYPE.put("poison jab", "poison");
      MOVE_TO_TYPE.put("toxic", "poison");
      MOVE_TO_TYPE.put("toxic spikes", "poison");
      MOVE_TO_TYPE.put("poison fang", "poison");
      MOVE_TO_TYPE.put("gunk shot", "poison");
      MOVE_TO_TYPE.put("cross poison", "poison");
      MOVE_TO_TYPE.put("venoshock", "poison");
      MOVE_TO_TYPE.put("acid", "poison");
      MOVE_TO_TYPE.put("acid spray", "poison");
      MOVE_TO_TYPE.put("poison gas", "poison");
      MOVE_TO_TYPE.put("baneful bunker", "poison");
      MOVE_TO_TYPE.put("earthquake", "ground");
      MOVE_TO_TYPE.put("earth power", "ground");
      MOVE_TO_TYPE.put("dig", "ground");
      MOVE_TO_TYPE.put("bulldoze", "ground");
      MOVE_TO_TYPE.put("drill run", "ground");
      MOVE_TO_TYPE.put("sand tomb", "ground");
      MOVE_TO_TYPE.put("high horsepower", "ground");
      MOVE_TO_TYPE.put("stomping tantrum", "ground");
      MOVE_TO_TYPE.put("precipice blades", "ground");
      MOVE_TO_TYPE.put("mud shot", "ground");
      MOVE_TO_TYPE.put("mud-slap", "ground");
      MOVE_TO_TYPE.put("bone rush", "ground");
      MOVE_TO_TYPE.put("bonemerang", "ground");
      MOVE_TO_TYPE.put("spikes", "ground");
      MOVE_TO_TYPE.put("fly", "flying");
      MOVE_TO_TYPE.put("aerial ace", "flying");
      MOVE_TO_TYPE.put("brave bird", "flying");
      MOVE_TO_TYPE.put("air slash", "flying");
      MOVE_TO_TYPE.put("hurricane", "flying");
      MOVE_TO_TYPE.put("sky attack", "flying");
      MOVE_TO_TYPE.put("acrobatics", "flying");
      MOVE_TO_TYPE.put("roost", "flying");
      MOVE_TO_TYPE.put("wing attack", "flying");
      MOVE_TO_TYPE.put("peck", "flying");
      MOVE_TO_TYPE.put("drill peck", "flying");
      MOVE_TO_TYPE.put("tailwind", "flying");
      MOVE_TO_TYPE.put("defog", "flying");
      MOVE_TO_TYPE.put("psychic", "psychic");
      MOVE_TO_TYPE.put("psyshock", "psychic");
      MOVE_TO_TYPE.put("zen headbutt", "psychic");
      MOVE_TO_TYPE.put("future sight", "psychic");
      MOVE_TO_TYPE.put("calm mind", "psychic");
      MOVE_TO_TYPE.put("hypnosis", "psychic");
      MOVE_TO_TYPE.put("dream eater", "psychic");
      MOVE_TO_TYPE.put("trick room", "psychic");
      MOVE_TO_TYPE.put("psychic terrain", "psychic");
      MOVE_TO_TYPE.put("confusion", "psychic");
      MOVE_TO_TYPE.put("psybeam", "psychic");
      MOVE_TO_TYPE.put("reflect", "psychic");
      MOVE_TO_TYPE.put("light screen", "psychic");
      MOVE_TO_TYPE.put("teleport", "psychic");
      MOVE_TO_TYPE.put("rest", "psychic");
      MOVE_TO_TYPE.put("trick", "psychic");
      MOVE_TO_TYPE.put("expanding force", "psychic");
      MOVE_TO_TYPE.put("agility", "psychic");
      MOVE_TO_TYPE.put("amnesia", "psychic");
      MOVE_TO_TYPE.put("barrier", "psychic");
      MOVE_TO_TYPE.put("meditate", "psychic");
      MOVE_TO_TYPE.put("psycho cut", "psychic");
      MOVE_TO_TYPE.put("stored power", "psychic");
      MOVE_TO_TYPE.put("extrasensory", "psychic");
      MOVE_TO_TYPE.put("luster purge", "psychic");
      MOVE_TO_TYPE.put("mist ball", "psychic");
      MOVE_TO_TYPE.put("bug buzz", "bug");
      MOVE_TO_TYPE.put("x-scissor", "bug");
      MOVE_TO_TYPE.put("megahorn", "bug");
      MOVE_TO_TYPE.put("u-turn", "bug");
      MOVE_TO_TYPE.put("signal beam", "bug");
      MOVE_TO_TYPE.put("pin missile", "bug");
      MOVE_TO_TYPE.put("first impression", "bug");
      MOVE_TO_TYPE.put("leech life", "bug");
      MOVE_TO_TYPE.put("sticky web", "bug");
      MOVE_TO_TYPE.put("string shot", "bug");
      MOVE_TO_TYPE.put("fury cutter", "bug");
      MOVE_TO_TYPE.put("quiver dance", "bug");
      MOVE_TO_TYPE.put("lunge", "bug");
      MOVE_TO_TYPE.put("stone edge", "rock");
      MOVE_TO_TYPE.put("rock slide", "rock");
      MOVE_TO_TYPE.put("rock tomb", "rock");
      MOVE_TO_TYPE.put("rock blast", "rock");
      MOVE_TO_TYPE.put("power gem", "rock");
      MOVE_TO_TYPE.put("head smash", "rock");
      MOVE_TO_TYPE.put("stealth rock", "rock");
      MOVE_TO_TYPE.put("sandstorm", "rock");
      MOVE_TO_TYPE.put("rock throw", "rock");
      MOVE_TO_TYPE.put("ancient power", "rock");
      MOVE_TO_TYPE.put("rock polish", "rock");
      MOVE_TO_TYPE.put("smack down", "rock");
      MOVE_TO_TYPE.put("diamond storm", "rock");
      MOVE_TO_TYPE.put("accelerock", "rock");
      MOVE_TO_TYPE.put("shadow ball", "ghost");
      MOVE_TO_TYPE.put("shadow claw", "ghost");
      MOVE_TO_TYPE.put("shadow punch", "ghost");
      MOVE_TO_TYPE.put("shadow sneak", "ghost");
      MOVE_TO_TYPE.put("hex", "ghost");
      MOVE_TO_TYPE.put("phantom force", "ghost");
      MOVE_TO_TYPE.put("destiny bond", "ghost");
      MOVE_TO_TYPE.put("curse", "ghost");
      MOVE_TO_TYPE.put("night shade", "ghost");
      MOVE_TO_TYPE.put("confuse ray", "ghost");
      MOVE_TO_TYPE.put("shadow force", "ghost");
      MOVE_TO_TYPE.put("spectral thief", "ghost");
      MOVE_TO_TYPE.put("poltergeist", "ghost");
      MOVE_TO_TYPE.put("dragon claw", "dragon");
      MOVE_TO_TYPE.put("dragon pulse", "dragon");
      MOVE_TO_TYPE.put("draco meteor", "dragon");
      MOVE_TO_TYPE.put("outrage", "dragon");
      MOVE_TO_TYPE.put("dragon dance", "dragon");
      MOVE_TO_TYPE.put("dragon tail", "dragon");
      MOVE_TO_TYPE.put("dragon rush", "dragon");
      MOVE_TO_TYPE.put("dragon breath", "dragon");
      MOVE_TO_TYPE.put("dragon rage", "dragon");
      MOVE_TO_TYPE.put("twister", "dragon");
      MOVE_TO_TYPE.put("roar of time", "dragon");
      MOVE_TO_TYPE.put("spacial rend", "dragon");
      MOVE_TO_TYPE.put("core enforcer", "dragon");
      MOVE_TO_TYPE.put("clanging scales", "dragon");
      MOVE_TO_TYPE.put("dragon energy", "dragon");
      MOVE_TO_TYPE.put("dark pulse", "dark");
      MOVE_TO_TYPE.put("crunch", "dark");
      MOVE_TO_TYPE.put("knock off", "dark");
      MOVE_TO_TYPE.put("sucker punch", "dark");
      MOVE_TO_TYPE.put("night slash", "dark");
      MOVE_TO_TYPE.put("foul play", "dark");
      MOVE_TO_TYPE.put("pursuit", "dark");
      MOVE_TO_TYPE.put("bite", "dark");
      MOVE_TO_TYPE.put("thief", "dark");
      MOVE_TO_TYPE.put("nasty plot", "dark");
      MOVE_TO_TYPE.put("taunt", "dark");
      MOVE_TO_TYPE.put("torment", "dark");
      MOVE_TO_TYPE.put("snarl", "dark");
      MOVE_TO_TYPE.put("throat chop", "dark");
      MOVE_TO_TYPE.put("wicked blow", "dark");
      MOVE_TO_TYPE.put("brutal swing", "dark");
      MOVE_TO_TYPE.put("darkest lariat", "dark");
      MOVE_TO_TYPE.put("hone claws", "dark");
      MOVE_TO_TYPE.put("power trip", "dark");
      MOVE_TO_TYPE.put("lash out", "dark");
      MOVE_TO_TYPE.put("assurance", "dark");
      MOVE_TO_TYPE.put("beat up", "dark");
      MOVE_TO_TYPE.put("feint attack", "dark");
      MOVE_TO_TYPE.put("memento", "dark");
      MOVE_TO_TYPE.put("payback", "dark");
      MOVE_TO_TYPE.put("embargo", "dark");
      MOVE_TO_TYPE.put("snatch", "dark");
      MOVE_TO_TYPE.put("false surrender", "dark");
      MOVE_TO_TYPE.put("jaw lock", "dark");
      MOVE_TO_TYPE.put("obstruct", "dark");
      MOVE_TO_TYPE.put("parting shot", "dark");
      MOVE_TO_TYPE.put("topsy-turvy", "dark");
      MOVE_TO_TYPE.put("hyperspace fury", "dark");
      MOVE_TO_TYPE.put("baddy bad", "dark");
      MOVE_TO_TYPE.put("fiery wrath", "dark");
      MOVE_TO_TYPE.put("kowtow cleave", "dark");
      MOVE_TO_TYPE.put("night daze", "dark");
      MOVE_TO_TYPE.put("iron head", "steel");
      MOVE_TO_TYPE.put("ironhead", "steel");
      MOVE_TO_TYPE.put("flash cannon", "steel");
      MOVE_TO_TYPE.put("flashcannon", "steel");
      MOVE_TO_TYPE.put("iron tail", "steel");
      MOVE_TO_TYPE.put("irontail", "steel");
      MOVE_TO_TYPE.put("meteor mash", "steel");
      MOVE_TO_TYPE.put("meteormash", "steel");
      MOVE_TO_TYPE.put("steel wing", "steel");
      MOVE_TO_TYPE.put("steelwing", "steel");
      MOVE_TO_TYPE.put("bullet punch", "steel");
      MOVE_TO_TYPE.put("bulletpunch", "steel");
      MOVE_TO_TYPE.put("gyro ball", "steel");
      MOVE_TO_TYPE.put("gyroball", "steel");
      MOVE_TO_TYPE.put("heavy slam", "steel");
      MOVE_TO_TYPE.put("heavyslam", "steel");
      MOVE_TO_TYPE.put("iron defense", "steel");
      MOVE_TO_TYPE.put("irondefense", "steel");
      MOVE_TO_TYPE.put("autotomize", "steel");
      MOVE_TO_TYPE.put("king's shield", "steel");
      MOVE_TO_TYPE.put("kingsshield", "steel");
      MOVE_TO_TYPE.put("smart strike", "steel");
      MOVE_TO_TYPE.put("smartstrike", "steel");
      MOVE_TO_TYPE.put("sunsteel strike", "steel");
      MOVE_TO_TYPE.put("sunsteelstrike", "steel");
      MOVE_TO_TYPE.put("behemoth blade", "steel");
      MOVE_TO_TYPE.put("behemothblade", "steel");
      MOVE_TO_TYPE.put("behemoth bash", "steel");
      MOVE_TO_TYPE.put("behemothbash", "steel");
      MOVE_TO_TYPE.put("metal sound", "steel");
      MOVE_TO_TYPE.put("metalsound", "steel");
      MOVE_TO_TYPE.put("moonblast", "fairy");
      MOVE_TO_TYPE.put("dazzling gleam", "fairy");
      MOVE_TO_TYPE.put("play rough", "fairy");
      MOVE_TO_TYPE.put("fairy wind", "fairy");
      MOVE_TO_TYPE.put("draining kiss", "fairy");
      MOVE_TO_TYPE.put("moonlight", "fairy");
      MOVE_TO_TYPE.put("misty terrain", "fairy");
      MOVE_TO_TYPE.put("charm", "fairy");
      MOVE_TO_TYPE.put("sweet kiss", "fairy");
      MOVE_TO_TYPE.put("disarming voice", "fairy");
      MOVE_TO_TYPE.put("geomancy", "fairy");
      MOVE_TO_TYPE.put("fleur cannon", "fairy");
      MOVE_TO_TYPE.put("spirit break", "fairy");
      MOVE_TO_TYPE.put("strange steam", "fairy");
      MOVE_TO_TYPE.put("tackle", "normal");
      MOVE_TO_TYPE.put("hyper beam", "normal");
      MOVE_TO_TYPE.put("giga impact", "normal");
      MOVE_TO_TYPE.put("body slam", "normal");
      MOVE_TO_TYPE.put("return", "normal");
      MOVE_TO_TYPE.put("facade", "normal");
      MOVE_TO_TYPE.put("extreme speed", "normal");
      MOVE_TO_TYPE.put("quick attack", "normal");
      MOVE_TO_TYPE.put("slash", "normal");
      MOVE_TO_TYPE.put("swift", "normal");
      MOVE_TO_TYPE.put("headbutt", "normal");
      MOVE_TO_TYPE.put("double-edge", "normal");
      MOVE_TO_TYPE.put("protect", "normal");
      MOVE_TO_TYPE.put("substitute", "normal");
      MOVE_TO_TYPE.put("swords dance", "normal");
      MOVE_TO_TYPE.put("work up", "normal");
      MOVE_TO_TYPE.put("growl", "normal");
      MOVE_TO_TYPE.put("leer", "normal");
      MOVE_TO_TYPE.put("screech", "normal");
      MOVE_TO_TYPE.put("roar", "normal");
      MOVE_TO_TYPE.put("recover", "normal");
      MOVE_TO_TYPE.put("soft-boiled", "normal");
      MOVE_TO_TYPE.put("wish", "normal");
      MOVE_TO_TYPE.put("baton pass", "normal");
      MOVE_TO_TYPE.put("rapid spin", "normal");
      MOVE_TO_TYPE.put("explosion", "normal");
      MOVE_TO_TYPE.put("self-destruct", "normal");
      MOVE_TO_TYPE.put("boomburst", "normal");
      MOVE_TO_TYPE.put("crush grip", "normal");
      MOVE_TO_TYPE.put("last resort", "normal");
      MOVE_TO_TYPE.put("fake out", "normal");
      MOVE_TO_TYPE.put("scary face", "normal");
      MOVE_TO_TYPE.put("glare", "normal");
      MOVE_TO_TYPE.put("sweet scent", "normal");
      MOVE_TO_TYPE.put("follow me", "normal");
      MOVE_TO_TYPE.put("helping hand", "normal");
      MOVE_TO_TYPE.put("perish song", "normal");
      MOVE_TO_TYPE.put("encore", "normal");
      MOVE_TO_TYPE.put("disable", "normal");
      MOVE_TO_TYPE.put("pain split", "normal");
      MOVE_TO_TYPE.put("rage", "normal");
      MOVE_TO_TYPE.put("fury attack", "normal");
      MOVE_TO_TYPE.put("double slap", "normal");
      MOVE_TO_TYPE.put("comet punch", "normal");
      MOVE_TO_TYPE.put("mega kick", "normal");
      MOVE_TO_TYPE.put("mega punch", "normal");
      MOVE_TO_TYPE.put("hyper voice", "normal");
      MOVE_TO_TYPE.put("uproar", "normal");
      MOVE_TO_TYPE.put("round", "normal");
      MOVE_TO_TYPE.put("echoed voice", "normal");
      MOVE_TO_TYPE.put("snore", "normal");
      MOVE_TO_TYPE.put("belly drum", "normal");
      MOVE_TO_TYPE.put("stockpile", "normal");
      MOVE_TO_TYPE.put("swallow", "normal");
      MOVE_TO_TYPE.put("spit up", "normal");
      MOVE_TO_TYPE.put("slack off", "normal");
      MOVE_TO_TYPE.put("yawn", "normal");
      MOVE_TO_TYPE.put("morning sun", "normal");
      MOVE_TO_TYPE.put("milk drink", "normal");
      MOVE_TO_TYPE.put("refresh", "normal");
      MOVE_TO_TYPE.put("aromatherapy", "normal");
      MOVE_TO_TYPE.put("heal bell", "normal");
      MOVE_TO_TYPE.put("entrainment", "normal");
      MOVE_TO_TYPE.put("simple beam", "normal");
      MOVE_TO_TYPE.put("copycat", "normal");
      MOVE_TO_TYPE.put("mimic", "normal");
      MOVE_TO_TYPE.put("sketch", "normal");
      MOVE_TO_TYPE.put("metronome", "normal");
      MOVE_TO_TYPE.put("transform", "normal");
      MOVE_TO_TYPE.put("ditto", "normal");
      MOVE_TO_TYPE.put("sleep talk", "normal");
      MOVE_TO_TYPE.put("focus energy", "normal");
      MOVE_TO_TYPE.put("endure", "normal");
      MOVE_TO_TYPE.put("bide", "normal");
      MOVE_TO_TYPE.put("after you", "normal");
      MOVE_TO_TYPE.put("recycle", "normal");
      MOVE_TO_TYPE.put("bestow", "normal");
      MOVE_TO_TYPE.put("natural gift", "normal");
      MOVE_TO_TYPE.put("acupressure", "normal");
      MOVE_TO_TYPE.put("odor sleuth", "normal");
      MOVE_TO_TYPE.put("foresight", "normal");
      MOVE_TO_TYPE.put("mind reader", "normal");
      MOVE_TO_TYPE.put("lock-on", "normal");
      MOVE_TO_TYPE.put("mean look", "normal");
      MOVE_TO_TYPE.put("block", "normal");
      MOVE_TO_TYPE.put("wrap", "normal");
      MOVE_TO_TYPE.put("bind", "normal");
      MOVE_TO_TYPE.put("smokescreen", "normal");
      MOVE_TO_TYPE.put("flash", "normal");
      MOVE_TO_TYPE.put("double team", "normal");
      MOVE_TO_TYPE.put("minimize", "normal");
      MOVE_TO_TYPE.put("defense curl", "normal");
      MOVE_TO_TYPE.put("harden", "normal");
      MOVE_TO_TYPE.put("shell smash", "normal");
      MOVE_TO_TYPE.put("skull bash", "normal");
      MOVE_TO_TYPE.put("thrash", "normal");
      MOVE_TO_TYPE.put("struggle", "normal");
      MOVE_TO_TYPE.put("tri attack", "normal");
      MOVE_TO_TYPE.put("retaliate", "normal");
      MOVE_TO_TYPE.put("chip away", "normal");
      MOVE_TO_TYPE.put("acrobatics", "flying");
      MOVE_TO_TYPE.put("covet", "normal");
      MOVE_TO_TYPE.put("entrainment", "normal");
      MOVE_TO_TYPE.put("weatherball", "normal");
      MOVE_TO_TYPE.put("judgment", "normal");
      MOVE_TO_TYPE.put("techno blast", "normal");
      MOVE_TO_TYPE.put("terrain pulse", "normal");
      MOVE_TO_TYPE.put("multi-attack", "normal");
      MOVE_TO_TYPE.put("double hit", "normal");
      MOVE_TO_TYPE.put("flail", "normal");
      MOVE_TO_TYPE.put("reversal", "fighting");
      MOVE_TO_TYPE.put("frustration", "normal");
      MOVE_TO_TYPE.put("secret power", "normal");
      MOVE_TO_TYPE.put("smelling salts", "normal");
      MOVE_TO_TYPE.put("crush claw", "normal");
      MOVE_TO_TYPE.put("false swipe", "normal");
      MOVE_TO_TYPE.put("giga drain", "grass");
      MOVE_TO_TYPE.put("endeavor", "normal");
      MOVE_TO_TYPE.put("echoed voice", "normal");
      MOVE_TO_TYPE.put("relic song", "normal");
      MOVE_TO_TYPE.put("tidy up", "normal");
      MOVE_TO_TYPE.put("detect", "fighting");
      MOVE_TO_TYPE.put("counter", "fighting");
      MOVE_TO_TYPE.put("mirror coat", "psychic");
      MOVE_TO_TYPE.put("bide", "normal");
      MOVE_TO_TYPE.put("rage powder", "bug");
      MOVE_TO_TYPE.put("after you", "normal");
      MOVE_TO_TYPE.put("quash", "dark");
      MOVE_TO_TYPE.put("ally switch", "psychic");
      MOVE_TO_TYPE.put("recycle", "normal");
      MOVE_TO_TYPE.put("bestow", "normal");
      MOVE_TO_TYPE.put("fling", "dark");
      MOVE_TO_TYPE.put("natural gift", "normal");
      MOVE_TO_TYPE.put("acupressure", "normal");
      MOVE_TO_TYPE.put("odor sleuth", "normal");
      MOVE_TO_TYPE.put("foresight", "normal");
      MOVE_TO_TYPE.put("miracle eye", "psychic");
      MOVE_TO_TYPE.put("mind reader", "normal");
      MOVE_TO_TYPE.put("lock-on", "normal");
      MOVE_TO_TYPE.put("mean look", "normal");
      MOVE_TO_TYPE.put("block", "normal");
      MOVE_TO_TYPE.put("spider web", "bug");
      MOVE_TO_TYPE.put("wrap", "normal");
      MOVE_TO_TYPE.put("bind", "normal");
      MOVE_TO_TYPE.put("clamp", "water");
      MOVE_TO_TYPE.put("fire spin", "fire");
      MOVE_TO_TYPE.put("whirlpool", "water");
      MOVE_TO_TYPE.put("sand attack", "ground");
      MOVE_TO_TYPE.put("smokescreen", "normal");
      MOVE_TO_TYPE.put("flash", "normal");
      MOVE_TO_TYPE.put("kinesis", "psychic");
      MOVE_TO_TYPE.put("double team", "normal");
      MOVE_TO_TYPE.put("minimize", "normal");
      MOVE_TO_TYPE.put("defense curl", "normal");
      MOVE_TO_TYPE.put("harden", "normal");
      MOVE_TO_TYPE.put("withdraw", "water");
      MOVE_TO_TYPE.put("acid armor", "poison");
      MOVE_TO_TYPE.put("cotton guard", "grass");
      MOVE_TO_TYPE.put("shell smash", "normal");
      MOVE_TO_TYPE.put("cosmic power", "psychic");
      MOVE_TO_TYPE.put("skull bash", "normal");
      MOVE_TO_TYPE.put("thrash", "normal");
      MOVE_TO_TYPE.put("petal dance", "grass");
      MOVE_TO_TYPE.put("struggle", "normal");
      MOVE_TO_TYPE.put("fireblast", "fire");
      MOVE_TO_TYPE.put("firepunch", "fire");
      MOVE_TO_TYPE.put("firefang", "fire");
      MOVE_TO_TYPE.put("heatwave", "fire");
      MOVE_TO_TYPE.put("flareblitz", "fire");
      MOVE_TO_TYPE.put("sacredfire", "fire");
      MOVE_TO_TYPE.put("flamewheel", "fire");
      MOVE_TO_TYPE.put("willowisp", "fire");
      MOVE_TO_TYPE.put("sunnyday", "fire");
      MOVE_TO_TYPE.put("blazekick", "fire");
      MOVE_TO_TYPE.put("firespin", "fire");
      MOVE_TO_TYPE.put("flameburst", "fire");
      MOVE_TO_TYPE.put("lavaplume", "fire");
      MOVE_TO_TYPE.put("magmastorm", "fire");
      MOVE_TO_TYPE.put("flamecharge", "fire");
      MOVE_TO_TYPE.put("mysticalfire", "fire");
      MOVE_TO_TYPE.put("heatcrash", "fire");
      MOVE_TO_TYPE.put("burnup", "fire");
      MOVE_TO_TYPE.put("pyroball", "fire");
      MOVE_TO_TYPE.put("burningjealousy", "fire");
      MOVE_TO_TYPE.put("blastburn", "fire");
      MOVE_TO_TYPE.put("blueflare", "fire");
      MOVE_TO_TYPE.put("fusionflare", "fire");
      MOVE_TO_TYPE.put("vcreate", "fire");
      MOVE_TO_TYPE.put("hydropump", "water");
      MOVE_TO_TYPE.put("watergun", "water");
      MOVE_TO_TYPE.put("aquatail", "water");
      MOVE_TO_TYPE.put("bubblebeam", "water");
      MOVE_TO_TYPE.put("raindance", "water");
      MOVE_TO_TYPE.put("aquajet", "water");
      MOVE_TO_TYPE.put("hydrocannon", "water");
      MOVE_TO_TYPE.put("waterpulse", "water");
      MOVE_TO_TYPE.put("muddywater", "water");
      MOVE_TO_TYPE.put("originpulse", "water");
      MOVE_TO_TYPE.put("solarbeam", "grass");
      MOVE_TO_TYPE.put("leafblade", "grass");
      MOVE_TO_TYPE.put("razorleaf", "grass");
      MOVE_TO_TYPE.put("energyball", "grass");
      MOVE_TO_TYPE.put("gigadrain", "grass");
      MOVE_TO_TYPE.put("megadrain", "grass");
      MOVE_TO_TYPE.put("seedbomb", "grass");
      MOVE_TO_TYPE.put("woodhammer", "grass");
      MOVE_TO_TYPE.put("powerwhip", "grass");
      MOVE_TO_TYPE.put("leafstorm", "grass");
      MOVE_TO_TYPE.put("grassyterrain", "grass");
      MOVE_TO_TYPE.put("sleeppowder", "grass");
      MOVE_TO_TYPE.put("stunspore", "grass");
      MOVE_TO_TYPE.put("cottonspore", "grass");
      MOVE_TO_TYPE.put("cottondown", "grass");
      MOVE_TO_TYPE.put("thunderwave", "electric");
      MOVE_TO_TYPE.put("volttackle", "electric");
      MOVE_TO_TYPE.put("wildcharge", "electric");
      MOVE_TO_TYPE.put("thunderpunch", "electric");
      MOVE_TO_TYPE.put("thunderfang", "electric");
      MOVE_TO_TYPE.put("electricterrain", "electric");
      MOVE_TO_TYPE.put("voltswitch", "electric");
      MOVE_TO_TYPE.put("thundershock", "electric");
      MOVE_TO_TYPE.put("zapcannon", "electric");
      MOVE_TO_TYPE.put("boltstrike", "electric");
      MOVE_TO_TYPE.put("icebeam", "ice");
      MOVE_TO_TYPE.put("icepunch", "ice");
      MOVE_TO_TYPE.put("icefang", "ice");
      MOVE_TO_TYPE.put("auroraveil", "ice");
      MOVE_TO_TYPE.put("iciclecrash", "ice");
      MOVE_TO_TYPE.put("freezedry", "ice");
      MOVE_TO_TYPE.put("iceshard", "ice");
      MOVE_TO_TYPE.put("aurorabeam", "ice");
      MOVE_TO_TYPE.put("closecombat", "fighting");
      MOVE_TO_TYPE.put("dynamicpunch", "fighting");
      MOVE_TO_TYPE.put("focusblast", "fighting");
      MOVE_TO_TYPE.put("aurasphere", "fighting");
      MOVE_TO_TYPE.put("crosschop", "fighting");
      MOVE_TO_TYPE.put("highjumpkick", "fighting");
      MOVE_TO_TYPE.put("brickbreak", "fighting");
      MOVE_TO_TYPE.put("machpunch", "fighting");
      MOVE_TO_TYPE.put("drainpunch", "fighting");
      MOVE_TO_TYPE.put("hammerarm", "fighting");
      MOVE_TO_TYPE.put("seismictoss", "fighting");
      MOVE_TO_TYPE.put("karatechop", "fighting");
      MOVE_TO_TYPE.put("bulkup", "fighting");
      MOVE_TO_TYPE.put("sludgebomb", "poison");
      MOVE_TO_TYPE.put("sludgewave", "poison");
      MOVE_TO_TYPE.put("poisonjab", "poison");
      MOVE_TO_TYPE.put("toxicspikes", "poison");
      MOVE_TO_TYPE.put("poisonfang", "poison");
      MOVE_TO_TYPE.put("gunkshot", "poison");
      MOVE_TO_TYPE.put("crosspoison", "poison");
      MOVE_TO_TYPE.put("acidspray", "poison");
      MOVE_TO_TYPE.put("poisongas", "poison");
      MOVE_TO_TYPE.put("banefulbunker", "poison");
      MOVE_TO_TYPE.put("earthpower", "ground");
      MOVE_TO_TYPE.put("drillrun", "ground");
      MOVE_TO_TYPE.put("sandtomb", "ground");
      MOVE_TO_TYPE.put("highhorsepower", "ground");
      MOVE_TO_TYPE.put("stompingtantrum", "ground");
      MOVE_TO_TYPE.put("precipiceblades", "ground");
      MOVE_TO_TYPE.put("mudshot", "ground");
      MOVE_TO_TYPE.put("mudslap", "ground");
      MOVE_TO_TYPE.put("bonerush", "ground");
      MOVE_TO_TYPE.put("sandattack", "ground");
      MOVE_TO_TYPE.put("aerialace", "flying");
      MOVE_TO_TYPE.put("bravebird", "flying");
      MOVE_TO_TYPE.put("airslash", "flying");
      MOVE_TO_TYPE.put("skyattack", "flying");
      MOVE_TO_TYPE.put("wingattack", "flying");
      MOVE_TO_TYPE.put("drillpeck", "flying");
      MOVE_TO_TYPE.put("zenheadbutt", "psychic");
      MOVE_TO_TYPE.put("futuresight", "psychic");
      MOVE_TO_TYPE.put("calmmind", "psychic");
      MOVE_TO_TYPE.put("dreameater", "psychic");
      MOVE_TO_TYPE.put("trickroom", "psychic");
      MOVE_TO_TYPE.put("psychicterrain", "psychic");
      MOVE_TO_TYPE.put("lightscreen", "psychic");
      MOVE_TO_TYPE.put("expandingforce", "psychic");
      MOVE_TO_TYPE.put("psychocut", "psychic");
      MOVE_TO_TYPE.put("storedpower", "psychic");
      MOVE_TO_TYPE.put("lusterpurge", "psychic");
      MOVE_TO_TYPE.put("mistball", "psychic");
      MOVE_TO_TYPE.put("mirrorcoat", "psychic");
      MOVE_TO_TYPE.put("allyswitch", "psychic");
      MOVE_TO_TYPE.put("miracleeye", "psychic");
      MOVE_TO_TYPE.put("mindreader", "normal");
      MOVE_TO_TYPE.put("cosmicpower", "psychic");
      MOVE_TO_TYPE.put("bugbuzz", "bug");
      MOVE_TO_TYPE.put("xscissor", "bug");
      MOVE_TO_TYPE.put("uturn", "bug");
      MOVE_TO_TYPE.put("signalbeam", "bug");
      MOVE_TO_TYPE.put("pinmissile", "bug");
      MOVE_TO_TYPE.put("firstimpression", "bug");
      MOVE_TO_TYPE.put("leechlife", "bug");
      MOVE_TO_TYPE.put("stickyweb", "bug");
      MOVE_TO_TYPE.put("stringshot", "bug");
      MOVE_TO_TYPE.put("furycutter", "bug");
      MOVE_TO_TYPE.put("quiverdance", "bug");
      MOVE_TO_TYPE.put("ragepowder", "bug");
      MOVE_TO_TYPE.put("spiderweb", "bug");
      MOVE_TO_TYPE.put("stoneedge", "rock");
      MOVE_TO_TYPE.put("rockslide", "rock");
      MOVE_TO_TYPE.put("rocktomb", "rock");
      MOVE_TO_TYPE.put("rockblast", "rock");
      MOVE_TO_TYPE.put("powergem", "rock");
      MOVE_TO_TYPE.put("headsmash", "rock");
      MOVE_TO_TYPE.put("stealthrock", "rock");
      MOVE_TO_TYPE.put("rockthrow", "rock");
      MOVE_TO_TYPE.put("ancientpower", "rock");
      MOVE_TO_TYPE.put("rockpolish", "rock");
      MOVE_TO_TYPE.put("smackdown", "rock");
      MOVE_TO_TYPE.put("diamondstorm", "rock");
      MOVE_TO_TYPE.put("shadowball", "ghost");
      MOVE_TO_TYPE.put("shadowclaw", "ghost");
      MOVE_TO_TYPE.put("shadowpunch", "ghost");
      MOVE_TO_TYPE.put("shadowsneak", "ghost");
      MOVE_TO_TYPE.put("phantomforce", "ghost");
      MOVE_TO_TYPE.put("destinybond", "ghost");
      MOVE_TO_TYPE.put("nightshade", "ghost");
      MOVE_TO_TYPE.put("confuseray", "ghost");
      MOVE_TO_TYPE.put("shadowforce", "ghost");
      MOVE_TO_TYPE.put("spectralthief", "ghost");
      MOVE_TO_TYPE.put("spite", "ghost");
      MOVE_TO_TYPE.put("dragonclaw", "dragon");
      MOVE_TO_TYPE.put("dragonpulse", "dragon");
      MOVE_TO_TYPE.put("dracometeor", "dragon");
      MOVE_TO_TYPE.put("dragondance", "dragon");
      MOVE_TO_TYPE.put("dragontail", "dragon");
      MOVE_TO_TYPE.put("dragonrush", "dragon");
      MOVE_TO_TYPE.put("dragonbreath", "dragon");
      MOVE_TO_TYPE.put("roaroftime", "dragon");
      MOVE_TO_TYPE.put("spacialrend", "dragon");
      MOVE_TO_TYPE.put("coreenforcer", "dragon");
      MOVE_TO_TYPE.put("clangingscales", "dragon");
      MOVE_TO_TYPE.put("dragonenergy", "dragon");
      MOVE_TO_TYPE.put("dragonrage", "dragon");
      MOVE_TO_TYPE.put("darkpulse", "dark");
      MOVE_TO_TYPE.put("knockoff", "dark");
      MOVE_TO_TYPE.put("suckerpunch", "dark");
      MOVE_TO_TYPE.put("nightslash", "dark");
      MOVE_TO_TYPE.put("foulplay", "dark");
      MOVE_TO_TYPE.put("nastyplot", "dark");
      MOVE_TO_TYPE.put("throatchop", "dark");
      MOVE_TO_TYPE.put("wickedblow", "dark");
      MOVE_TO_TYPE.put("brutalswing", "dark");
      MOVE_TO_TYPE.put("darkestlariat", "dark");
      MOVE_TO_TYPE.put("honeclaws", "dark");
      MOVE_TO_TYPE.put("powertrip", "dark");
      MOVE_TO_TYPE.put("lashout", "dark");
      MOVE_TO_TYPE.put("beatup", "dark");
      MOVE_TO_TYPE.put("feintattack", "dark");
      MOVE_TO_TYPE.put("falsesurrender", "dark");
      MOVE_TO_TYPE.put("jawlock", "dark");
      MOVE_TO_TYPE.put("partingshot", "dark");
      MOVE_TO_TYPE.put("topsyturvy", "dark");
      MOVE_TO_TYPE.put("hyperspacefury", "dark");
      MOVE_TO_TYPE.put("baddybad", "dark");
      MOVE_TO_TYPE.put("fierywrath", "dark");
      MOVE_TO_TYPE.put("kowtowcleave", "dark");
      MOVE_TO_TYPE.put("nightdaze", "dark");
      MOVE_TO_TYPE.put("dazzlinggleam", "fairy");
      MOVE_TO_TYPE.put("playrough", "fairy");
      MOVE_TO_TYPE.put("fairywind", "fairy");
      MOVE_TO_TYPE.put("drainingkiss", "fairy");
      MOVE_TO_TYPE.put("mistyterrain", "fairy");
      MOVE_TO_TYPE.put("sweetkiss", "fairy");
      MOVE_TO_TYPE.put("disarmingvoice", "fairy");
      MOVE_TO_TYPE.put("fleurcannon", "fairy");
      MOVE_TO_TYPE.put("spiritbreak", "fairy");
      MOVE_TO_TYPE.put("strangesteam", "fairy");
      MOVE_TO_TYPE.put("hyperbeam", "normal");
      MOVE_TO_TYPE.put("gigaimpact", "normal");
      MOVE_TO_TYPE.put("bodyslam", "normal");
      MOVE_TO_TYPE.put("extremespeed", "normal");
      MOVE_TO_TYPE.put("quickattack", "normal");
      MOVE_TO_TYPE.put("doubleedge", "normal");
      MOVE_TO_TYPE.put("swordsdance", "normal");
      MOVE_TO_TYPE.put("workup", "normal");
      MOVE_TO_TYPE.put("softboiled", "normal");
      MOVE_TO_TYPE.put("batonpass", "normal");
      MOVE_TO_TYPE.put("rapidspin", "normal");
      MOVE_TO_TYPE.put("selfdestruct", "normal");
      MOVE_TO_TYPE.put("crushgrip", "normal");
      MOVE_TO_TYPE.put("lastresort", "normal");
      MOVE_TO_TYPE.put("fakeout", "normal");
      MOVE_TO_TYPE.put("scaryface", "normal");
      MOVE_TO_TYPE.put("sweetscent", "normal");
      MOVE_TO_TYPE.put("followme", "normal");
      MOVE_TO_TYPE.put("helpinghand", "normal");
      MOVE_TO_TYPE.put("perishsong", "normal");
      MOVE_TO_TYPE.put("painsplit", "normal");
      MOVE_TO_TYPE.put("furyattack", "normal");
      MOVE_TO_TYPE.put("doubleslap", "normal");
      MOVE_TO_TYPE.put("cometpunch", "normal");
      MOVE_TO_TYPE.put("megakick", "normal");
      MOVE_TO_TYPE.put("megapunch", "normal");
      MOVE_TO_TYPE.put("hypervoice", "normal");
      MOVE_TO_TYPE.put("echoedvoice", "normal");
      MOVE_TO_TYPE.put("bellydrum", "normal");
      MOVE_TO_TYPE.put("spitup", "normal");
      MOVE_TO_TYPE.put("slackoff", "normal");
      MOVE_TO_TYPE.put("morningsun", "normal");
      MOVE_TO_TYPE.put("milkdrink", "normal");
      MOVE_TO_TYPE.put("healbell", "normal");
      MOVE_TO_TYPE.put("simplebeam", "normal");
      MOVE_TO_TYPE.put("sleeptalk", "normal");
      MOVE_TO_TYPE.put("focusenergy", "normal");
      MOVE_TO_TYPE.put("lockon", "normal");
      MOVE_TO_TYPE.put("meanlook", "normal");
      MOVE_TO_TYPE.put("doubleteam", "normal");
      MOVE_TO_TYPE.put("defensecurl", "normal");
      MOVE_TO_TYPE.put("acidarmor", "poison");
      MOVE_TO_TYPE.put("cottonguard", "grass");
      MOVE_TO_TYPE.put("shellsmash", "normal");
      MOVE_TO_TYPE.put("skullbash", "normal");
      MOVE_TO_TYPE.put("petaldance", "grass");
      MOVE_TO_TYPE.put("triattack", "normal");
      MOVE_TO_TYPE.put("chipaway", "normal");
      MOVE_TO_TYPE.put("weatherball", "normal");
      MOVE_TO_TYPE.put("technoblast", "normal");
      MOVE_TO_TYPE.put("terrainpulse", "normal");
      MOVE_TO_TYPE.put("multiattack", "normal");
      MOVE_TO_TYPE.put("doublehit", "normal");
      MOVE_TO_TYPE.put("secretpower", "normal");
      MOVE_TO_TYPE.put("smellingsalts", "normal");
      MOVE_TO_TYPE.put("crushclaw", "normal");
      MOVE_TO_TYPE.put("falseswipe", "normal");
      MOVE_TO_TYPE.put("relicsong", "normal");
      MOVE_TO_TYPE.put("tidyup", "normal");
   }

   public static void setPlayerName(String name) {
      playerName = name;
   }

   public static void setOpponentName(String name) {
      opponentName = name;
   }

   public static void registerOpponentPokemon(String pokemonName) {
      if (pokemonName != null && !pokemonName.isBlank()) {
         registerOpponentNameVariant(pokemonName);
         String idCandidate = pokemonName.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "");
         if (!idCandidate.isBlank()) {
            registerLocalizedOpponentSpecies(idCandidate);
            if (idCandidate.contains("_")) {
               registerLocalizedOpponentSpecies(idCandidate.replace("_", ""));
            }
         }
      }
   }

   private static void registerOpponentNameVariant(String value) {
      if (value != null) {
         String lowered = value.trim().toLowerCase(Locale.ROOT);
         if (!lowered.isBlank()) {
            opponentPokemonNames.add(lowered);
            String compact = lowered.replaceAll("[^\\p{L}\\p{N}]", "");
            if (!compact.isBlank()) {
               opponentPokemonNames.add(compact);
            }
         }
      }
   }

   private static void registerLocalizedOpponentSpecies(String speciesId) {
      if (speciesId != null && !speciesId.isBlank()) {
         String translationKey = "cobblemon.species." + speciesId + ".name";
         String localized = class_2561.method_43471(translationKey).getString();
         if (localized != null && !localized.isBlank() && !localized.equals(translationKey)) {
            registerOpponentNameVariant(localized);
         }
      }
   }

   public static void clearOpponentPokemon() {
      opponentPokemonNames.clear();
   }

   public static void registerWeatherMetadata(String weatherId) {
      if (weatherId != null) {
         activeWeatherType = weatherId.toLowerCase();
         CobblemonBattleInfoClient.debug("[BattleMessageColorizer] Registered weather: '{}'", new Object[]{weatherId});
      }
   }

   public static void registerAbilityMetadata(String localizedAbilityName, String abilityId) {
      if (localizedAbilityName != null && abilityId != null) {
         textToEffectType.put(localizedAbilityName.toLowerCase(), "ability:" + abilityId.toLowerCase());
         CobblemonBattleInfoClient.debug("[BattleMessageColorizer] Registered ability: '{}' -> '{}'", new Object[]{localizedAbilityName, abilityId});
      }
   }

   public static void registerPlayerSwitchAction() {
   }

   public static void registerStatusEffectMetadata(String localizedText, String statusType) {
      if (localizedText != null && statusType != null) {
         textToEffectType.put(localizedText.toLowerCase(), "status:" + statusType.toLowerCase());
         CobblemonBattleInfoClient.debug("[BattleMessageColorizer] Registered status effect: '{}' -> '{}'", new Object[]{localizedText, statusType});
      }
   }

   public static void registerEffectivenessMetadata(String localizedText, String effectivenessType) {
      if (localizedText != null && effectivenessType != null) {
         textToEffectType.put(localizedText.toLowerCase(), "effectiveness:" + effectivenessType.toLowerCase());
         CobblemonBattleInfoClient.debug("[BattleMessageColorizer] Registered effectiveness: '{}' -> '{}'", new Object[]{localizedText, effectivenessType});
      }
   }

   public static void registerMissMetadata(String localizedText) {
      if (localizedText != null) {
         textToEffectType.put(localizedText.toLowerCase(), "miss");
         CobblemonBattleInfoClient.debug("[BattleMessageColorizer] Registered miss message: '{}'", new Object[]{localizedText});
      }
   }

   public static void registerCriticalHitMetadata(String localizedText) {
      if (localizedText != null) {
         textToEffectType.put(localizedText.toLowerCase(), "critical");
         CobblemonBattleInfoClient.debug("[BattleMessageColorizer] Registered critical hit: '{}'", new Object[]{localizedText});
      }
   }

   public static Integer getColorForText(String text) {
      if (text != null && !text.isEmpty()) {
         String lower = text.toLowerCase();
         Integer color = null;
         String effectType = textToEffectType.get(lower);
         if (effectType != null) {
            if (effectType.startsWith("status:")) {
               String status = effectType.substring(7);
               switch (status) {
                  case "brn":
                  case "burn":
                     color = 15761456;
                     break;
                  case "psn":
                  case "tox":
                  case "poison":
                  case "badly poisoned":
                     color = 10502304;
                     break;
                  case "par":
                  case "paralysis":
                     color = 16306224;
                     break;
                  case "slp":
                  case "sleep":
                     color = 7362712;
                     break;
                  case "frz":
                  case "freeze":
                     color = 10016984;
                     break;
                  case "confusion":
                     color = 16275592;
               }
            } else if (effectType.startsWith("effectiveness:")) {
               String eff = effectType.substring(14);
               switch (eff) {
                  case "super":
                  case "supereffective":
                     color = 5635925;
                     break;
                  case "resisted":
                  case "notveryeffective":
                     color = 11184810;
                     break;
                  case "immune":
                     color = 11184810;
               }
            } else if ("miss".equals(effectType)) {
               color = 16733525;
            } else if ("critical".equals(effectType)) {
               color = 16755200;
            }
         }

         if (color != null) {
            return color;
         } else {
            if (lower.contains("super effective")) {
               color = 5635925;
            } else if (lower.contains("not very effective")) {
               color = 11184810;
            } else if (lower.contains("critical hit")) {
               color = 16755200;
            } else if (lower.contains("missed") || lower.contains("but it failed")) {
               color = 16733525;
            } else if (lower.contains("rose") || lower.contains("boosted") || lower.contains("raised")) {
               color = 5635925;
            } else if (lower.contains("fell") || lower.contains("lowered") || lower.contains("dropped") || lower.contains("harshly")) {
               color = 16733525;
            } else if (lower.contains("poisoned") || lower.contains("badly poisoned")) {
               color = 10502304;
            } else if (lower.contains("burned") || lower.contains("burn")) {
               color = 15761456;
            } else if (lower.contains("paralyzed") || lower.contains("paralysis")) {
               color = 16306224;
            } else if (lower.contains("fell asleep") || lower.contains("woke up")) {
               color = 7362712;
            } else if (lower.contains("frozen") || lower.contains("thawed")) {
               color = 10016984;
            } else if (lower.contains("confused") || lower.contains("confusion")) {
               color = 16275592;
            } else if (!lower.contains("sandstorm") && !lower.contains("sand stream")) {
               if ((lower.matches(".*\\brain\\b.*") || lower.contains("downpour") || lower.contains("drizzle"))
                  && !lower.contains("ingrain")
                  && !lower.contains("terrain")) {
                  color = 6852848;
               } else if (lower.contains("sunlight") || lower.contains("sunny") || lower.contains("harsh sunlight") || lower.contains("drought")) {
                  color = 16306224;
               } else if (lower.contains("hail")) {
                  color = 10016984;
               } else if ((lower.contains("snow") || lower.contains("snowing") || lower.contains("falling down")) && !lower.contains("abomasnow")) {
                  color = 12116192;
               } else if (lower.contains("warning activated") || lower.contains("started to snow")) {
                  color = 12116192;
               } else if (lower.contains("aurora veil") || lower.contains("reflect") || lower.contains("light screen")) {
                  color = TYPE_COLORS.get("ice");
               } else if (lower.contains("made your team stronger") || lower.contains("physical and special")) {
                  color = TYPE_COLORS.get("ice");
               } else if (lower.contains("used ")) {
                  color = getMoveColorFromText(text);
                  if (color == null) {
                     if (playerName != null && text.contains(playerName)) {
                        color = 5635925;
                     } else {
                        color = 16733525;
                     }
                  }
               } else if (!lower.contains("was hurt by") && !lower.contains("took damage") && !lower.contains("lost")) {
                  if (lower.startsWith("wild ")
                     || lower.startsWith("foe ")
                     || lower.startsWith("opposing ")
                     || lower.startsWith("bouffalant")
                     || lower.startsWith("the wild")
                     || lower.startsWith("the foe")) {
                     color = 16733525;
                  } else if (playerName != null && text.startsWith(playerName)) {
                     color = 5635925;
                  } else if (playerName != null && text.contains(playerName)) {
                     color = 5635925;
                  }
               } else if (playerName != null && text.contains(playerName)) {
                  color = 5635925;
               } else if (lower.startsWith("bouffalant") || lower.startsWith("wild") || lower.startsWith("foe") || lower.startsWith("opposing")) {
                  color = 16733525;
               }
            } else {
               color = 12756557;
            }

            if (color != null) {
               lastColor = color;
               lastColorTime = System.currentTimeMillis();
               return color;
            } else {
               long now = System.currentTimeMillis();
               if (lastColor != null && now - lastColorTime < 100L) {
                  boolean isNewSentence = text.length() > 30
                     && (lower.startsWith("it ") || lower.startsWith("the ") || lower.startsWith("wild ") || lower.startsWith("foe "));
                  if (!isNewSentence && text.length() < 40) {
                     lastColorTime = now;
                     return lastColor;
                  }
               }

               if (now - lastColorTime >= 100L) {
                  lastColor = null;
               }

               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static Integer getMoveColorFromText(String text) {
      String lower = text.toLowerCase();
      String[] usedKeywords = new String[]{"used ", "usado ", "usou ", "utilisé ", "utilise ", "setzt ", "usa ", "ha usado "};
      int usedIndex = -1;
      int keywordLength = 0;

      for (String keyword : usedKeywords) {
         int idx = lower.indexOf(keyword);
         if (idx != -1) {
            usedIndex = idx;
            keywordLength = keyword.length();
            break;
         }
      }

      if (usedIndex == -1) {
         return null;
      } else {
         int moveStart = usedIndex + keywordLength;
         int moveEnd = text.indexOf("!", moveStart);
         if (moveEnd == -1) {
            moveEnd = text.indexOf(" on ", moveStart);
         }

         if (moveEnd == -1) {
            moveEnd = text.indexOf(" en ", moveStart);
         }

         if (moveEnd == -1) {
            moveEnd = text.indexOf(" sur ", moveStart);
         }

         if (moveEnd == -1) {
            moveEnd = text.length();
         }

         String moveName = text.substring(moveStart, moveEnd).trim();
         String moveNameLower = moveName.toLowerCase(Locale.ROOT);
         CobblemonBattleInfoClient.debug("[Colorizer] getMoveColorFromText: extracted move name '{}' from text", new Object[]{moveNameLower});
         String moveType = resolveMoveType(moveNameLower);
         return moveType != null && TYPE_COLORS.containsKey(moveType) ? TYPE_COLORS.get(moveType) : null;
      }
   }

   public static Integer getMoveTypeColor(String moveName) {
      String type = resolveMoveType(moveName);
      return type != null ? TYPE_COLORS.get(type) : null;
   }

   public static Integer getTypeColorByName(String typeName) {
      return typeName != null && !typeName.isBlank() ? TYPE_COLORS.get(typeName.trim().toLowerCase(Locale.ROOT)) : null;
   }

   private static String resolveMoveType(String moveName) {
      String normalizedMoveName = normalizeMoveLookupKey(moveName);
      if (normalizedMoveName == null) {
         return null;
      } else {
         String moveType = textToMoveType.get(normalizedMoveName);
         if (moveType == null) {
            moveType = MOVE_TO_TYPE.get(normalizedMoveName);
         }

         if (moveType == null) {
            String noSpaces = normalizedMoveName.replace(" ", "");
            if (!noSpaces.equals(normalizedMoveName)) {
               moveType = textToMoveType.get(noSpaces);
               if (moveType == null) {
                  moveType = MOVE_TO_TYPE.get(noSpaces);
               }
            }
         }

         return moveType;
      }
   }

   private static String resolveMoveTypeByPrefix(String partialMoveName) {
      String normalizedPrefix = normalizeMoveLookupKey(partialMoveName);
      if (normalizedPrefix == null) {
         return null;
      } else {
         if (normalizedPrefix.endsWith("-")) {
            normalizedPrefix = normalizedPrefix.substring(0, normalizedPrefix.length() - 1);
         }

         if (normalizedPrefix.isEmpty()) {
            return null;
         } else {
            String moveType = findMoveTypeByPrefix(textToMoveType, normalizedPrefix);
            if (moveType == null) {
               moveType = findMoveTypeByPrefix(MOVE_TO_TYPE, normalizedPrefix);
            }

            if (moveType == null) {
               String noSpacesPrefix = normalizedPrefix.replace(" ", "");
               if (!noSpacesPrefix.equals(normalizedPrefix)) {
                  moveType = findMoveTypeByPrefix(textToMoveType, noSpacesPrefix);
                  if (moveType == null) {
                     moveType = findMoveTypeByPrefix(MOVE_TO_TYPE, noSpacesPrefix);
                  }
               }
            }

            return moveType;
         }
      }
   }

   private static String findMoveTypeByPrefix(Map<String, String> moveTypes, String prefix) {
      for (Entry<String, String> entry : moveTypes.entrySet()) {
         if (entry.getKey().startsWith(prefix)) {
            return entry.getValue();
         }
      }

      return null;
   }

   private static String normalizeMoveLookupKey(String moveName) {
      if (moveName == null) {
         return null;
      } else {
         String normalizedMoveName = moveName.trim().toLowerCase(Locale.ROOT);
         return normalizedMoveName.isEmpty() ? null : normalizedMoveName;
      }
   }

   private static String normalizeTextLookupKey(String text) {
      if (text == null) {
         return null;
      } else {
         String normalizedText = text.trim().toLowerCase(Locale.ROOT);
         return normalizedText.isEmpty() ? null : normalizedText;
      }
   }

   public static class_2561 colorizeLineByParts(String text) {
      if (text == null || text.isEmpty()) {
         return class_2561.method_43470(text);
      } else if (!BattleExtrasConfig.isBattleLogColoringEnabled()) {
         return text.startsWith("* ") && text.toLowerCase().contains(" form") ? null : enhanceLineWithoutBattleLogColoring(text);
      } else if (text.startsWith("* ") && text.toLowerCase().contains(" form")) {
         return null;
      } else {
         MoveLineOverride moveLineOverride = textToMoveLineOverride.get(normalizeTextLookupKey(text));
         if (moveLineOverride != null) {
            return colorizeTextWithMove(text, moveLineOverride.moveName, moveLineOverride.moveType, moveLineOverride.moveId);
         } else {
            String lower = text.toLowerCase();
            class_5250 result = class_2561.method_43473();
            if (lower.startsWith("it is now turn")) {
               return class_2561.method_43470(text).method_27696(class_2583.field_24360.method_36139(5636095));
            } else if (lower.startsWith("go! ")) {
               result.method_10852(class_2561.method_43470("Go! ").method_27696(class_2583.field_24360.method_36139(5635925)));
               String pokemonPart = text.substring(4);
               result.method_10852(class_2561.method_43470(pokemonPart));
               return result;
            } else {
               if (lower.startsWith("¡adelante,") || lower.startsWith("adelante,")) {
                  int commaIdx = text.indexOf(44);
                  if (commaIdx > 0) {
                     String actionPart = text.substring(0, commaIdx + 1);
                     String pokemonPart = text.substring(commaIdx + 1).trim();
                     result.method_10852(class_2561.method_43470(actionPart).method_27696(class_2583.field_24360.method_36139(5635925)));
                     result.method_10852(class_2561.method_43470(" " + pokemonPart));
                     return result;
                  }
               }

               if (lower.startsWith("vai ")) {
                  result.method_10852(class_2561.method_43470(text.substring(0, 4)).method_27696(class_2583.field_24360.method_36139(5635925)));
                  String pokemonPart = text.substring(4);
                  result.method_10852(class_2561.method_43470(pokemonPart));
                  return result;
               } else if (lower.startsWith("allez ")) {
                  result.method_10852(class_2561.method_43470(text.substring(0, 6)).method_27696(class_2583.field_24360.method_36139(5635925)));
                  String pokemonPart = text.substring(6);
                  result.method_10852(class_2561.method_43470(pokemonPart));
                  return result;
               } else if (lower.startsWith("los ")) {
                  result.method_10852(class_2561.method_43470(text.substring(0, 4)).method_27696(class_2583.field_24360.method_36139(5635925)));
                  String pokemonPart = text.substring(4);
                  result.method_10852(class_2561.method_43470(pokemonPart));
                  return result;
               } else if (!lower.startsWith("vai! ") && !lower.startsWith("go ")) {
                  String effectType = textToEffectType.get(lower);
                  if (effectType != null) {
                     if (effectType.startsWith("effectiveness:")) {
                        String eff = effectType.substring(14);
                        switch (eff) {
                           case "super":
                           case "supereffective":
                              return class_2561.method_43470(text).method_27696(class_2583.field_24360.method_36139(5635925));
                           case "resisted":
                           case "notveryeffective":
                              return class_2561.method_43470(text).method_27696(class_2583.field_24360.method_36139(11184810));
                        }
                     } else {
                        if ("miss".equals(effectType)) {
                           return class_2561.method_43470(text).method_27696(class_2583.field_24360.method_36139(16733525));
                        }

                        if ("critical".equals(effectType)) {
                           return class_2561.method_43470(text).method_27696(class_2583.field_24360.method_36139(16755200));
                        }

                        if (effectType.startsWith("status:")) {
                           String status = effectType.substring(7);
                           Integer statusColor = null;
                           switch (status) {
                              case "brn":
                              case "burn":
                                 statusColor = 15761456;
                                 break;
                              case "psn":
                              case "tox":
                              case "poison":
                              case "badly poisoned":
                                 statusColor = 10502304;
                                 break;
                              case "par":
                              case "paralysis":
                                 statusColor = 16306224;
                                 break;
                              case "slp":
                              case "sleep":
                                 statusColor = 7362712;
                                 break;
                              case "frz":
                              case "freeze":
                                 statusColor = 10016984;
                                 break;
                              case "confusion":
                                 statusColor = 16275592;
                           }

                           if (statusColor != null) {
                              return class_2561.method_43470(text).method_27696(class_2583.field_24360.method_36139(statusColor));
                           }
                        }
                     }
                  }

                  if (lower.contains("super effective")
                     || lower.contains("extremely effective")
                     || lower.contains("supereficaz")
                     || lower.contains("extremadamente eficaz")
                     || lower.contains("supereficiente")
                     || lower.contains("extremamente eficaz")
                     || lower.contains("très efficace")
                     || lower.contains("extrêmement efficace")
                     || lower.contains("sehr effektiv")
                     || lower.contains("superefficace")
                     || lower.contains("こうかはばつぐんだ")) {
                     return class_2561.method_43470(text).method_27696(class_2583.field_24360.method_36139(5635925));
                  } else if (lower.contains("not very effective")
                     || lower.contains("not effective")
                     || lower.contains("mostly ineffective")
                     || lower.contains("barely effective")
                     || lower.contains("no muy eficaz")
                     || lower.contains("ineficaz")
                     || lower.contains("extremadamente ineficaz")
                     || lower.contains("não muito eficaz")
                     || lower.contains("pouco eficaz")
                     || lower.contains("ineficiente")
                     || lower.contains("pas très efficace")
                     || lower.contains("peu efficace")
                     || lower.contains("nicht sehr effektiv")
                     || lower.contains("wenig effektiv")
                     || lower.contains("non molto efficace")
                     || lower.contains("poco efficace")
                     || lower.contains("こうかはいまひとつ")) {
                     return class_2561.method_43470(text).method_27696(class_2583.field_24360.method_36139(11184810));
                  } else if (lower.contains("critical hit")
                     || lower.contains("golpe crítico")
                     || lower.contains("golpe critico")
                     || lower.contains("coup critique")
                     || lower.contains("volltreffer")
                     || lower.contains("brutto colpo")
                     || lower.contains("きゅうしょ")) {
                     return class_2561.method_43470(text).method_27696(class_2583.field_24360.method_36139(16755200));
                  } else if (lower.contains("missed")
                     || lower.contains("but it failed")
                     || lower.contains("falló")
                     || lower.contains("ha fallado")
                     || lower.contains("errou")
                     || lower.contains("fracassou")
                     || lower.contains("raté")
                     || lower.contains("a échoué")
                     || lower.contains("verfehlt")
                     || lower.contains("fehlgeschlagen")
                     || lower.contains("fallito")
                     || lower.contains("mancato")
                     || lower.contains("はずれた")) {
                     return class_2561.method_43470(text).method_27696(class_2583.field_24360.method_36139(16733525));
                  } else if (!lower.contains("sandstorm") && !lower.contains("sand stream")) {
                     if (activeWeatherType != null
                        && (lower.contains("empezado") || lower.contains("started") || lower.contains("comenzado") || lower.contains("commencé"))) {
                        int weatherColor = getWeatherColor(activeWeatherType);
                        if (weatherColor != 16777215) {
                           return class_2561.method_43470(text).method_27696(class_2583.field_24360.method_36139(weatherColor));
                        }
                     }

                     if (lower.contains("activated") || lower.contains("activado") || lower.contains("activé")) {
                        for (String effectName : textToEffectType.keySet()) {
                           if (lower.contains(effectName)) {
                              String abilityEffectType = textToEffectType.get(effectName);
                              if (abilityEffectType != null && abilityEffectType.startsWith("ability:")) {
                                 CobblemonBattleInfoClient.debug("[Colorizer] Found registered ability '{}' in text: '{}'", new Object[]{effectName, text});
                                 return colorizeAbilityMessage(text, effectName);
                              }
                           }
                        }
                     }

                     for (String effectNamex : textToEffectType.keySet()) {
                        if (lower.contains(effectNamex)) {
                           String megaEffectType = textToEffectType.get(effectNamex);
                           if ("mega_evolved".equals(megaEffectType)) {
                              CobblemonBattleInfoClient.debug("[Colorizer] Found Mega Evolution message: '{}'", new Object[]{text});
                              return colorizeMegaEvolutionPhrase(text, effectNamex);
                           }
                        }
                     }

                     String[] megaPhrases = new String[]{
                        "mega evolved!",
                        "mega evolved",
                        "megaevolved!",
                        "megaevolved",
                        "mega evolución!",
                        "mega evolución",
                        "megaevolución!",
                        "megaevolución",
                        "méga-évolution!",
                        "méga-évolution",
                        "megaévolution!",
                        "megaévolution",
                        "mega evolution!",
                        "mega evolution",
                        "megaevolution!",
                        "megaevolution",
                        "mega evoluiu!",
                        "mega evoluiu",
                        "megaevoluiu!",
                        "megaevoluiu",
                        "mega si è evoluto!",
                        "mega si è evoluto",
                        "megasievoluto!",
                        "megasievoluto",
                        "mega entwickelt!",
                        "mega entwickelt",
                        "megaentwickelt!",
                        "megaentwickelt",
                        "メガ進化!",
                        "メガ進化",
                        "메가진화!",
                        "메가진화"
                     };

                     for (String phrase : megaPhrases) {
                        if (lower.contains(phrase.toLowerCase())) {
                           CobblemonBattleInfoClient.debug("[Colorizer] Found Mega Evolution message (fallback): '{}'", new Object[]{text});
                           return colorizeMegaEvolutionPhrase(text, phrase);
                        }
                     }

                     if (lower.contains("terastallized") || lower.contains("terastallize")) {
                        return colorizeTerastallizationMessage(text);
                     } else if (!lower.contains("is fast asleep") && !lower.contains("is asleep")) {
                        for (String moveName : textToMoveType.keySet()) {
                           Pattern pattern = Pattern.compile("\\b" + Pattern.quote(moveName) + "\\b", 2);
                           Matcher matcher = pattern.matcher(text);
                           if (matcher.find()) {
                              CobblemonBattleInfoClient.debug("[Colorizer] Found registered move '{}' in text: '{}'", new Object[]{moveName, text});
                              return colorizeTextWithMove(text, moveName, textToMoveType.get(moveName), resolveMoveIdForDisplayName(moveName));
                           }
                        }

                        if (lower.contains("used ")) {
                           return colorizeUsedMove(text);
                        } else if ((lower.matches(".*\\brain\\b.*") || lower.contains("drizzle"))
                           && !lower.contains("ingrain")
                           && !lower.contains("terrain")
                           && !lower.contains("entrainment")
                           && !lower.contains("dance")) {
                           return colorizeWithHighlight(text, new String[]{"rain", "Drizzle"}, 6852848);
                        } else if ((lower.contains("sun") || lower.contains("drought")) && !lower.contains("sunny day")) {
                           return colorizeWithHighlight(text, new String[]{"sunlight", "sunny", "Drought"}, 16306224);
                        } else if (lower.contains("hail")) {
                           return colorizeWithHighlight(text, new String[]{"hail", "hailing"}, 10016984);
                        } else if (lower.contains("snow") && !lower.contains("abomasnow")) {
                           return colorizeWithHighlight(text, new String[]{"snow", "snowing"}, 12116192);
                        } else {
                           class_2561 wrappedMoveResult = tryColorizeWrappedMoveLine(text);
                           if (wrappedMoveResult != null) {
                              return wrappedMoveResult;
                           } else if (lower.contains("rose") || lower.contains("raised") || lower.contains("boosted")) {
                              return colorizeStatChange(text, 5635925);
                           } else if (lower.contains("fell") || lower.contains("lowered") || lower.contains("dropped")) {
                              return colorizeStatChange(text, 16733525);
                           } else if (lower.contains("poisoned")
                              || lower.contains("envenenado")
                              || lower.contains("empoisonné")
                              || lower.contains("vergiftet")
                              || lower.contains("avvelenato")) {
                              return colorizeWithHighlight(
                                 text,
                                 new String[]{
                                    "poisoned",
                                    "badly poisoned",
                                    "envenenado",
                                    "gravemente envenenado",
                                    "empoisonné",
                                    "gravement empoisonné",
                                    "vergiftet",
                                    "schwer vergiftet",
                                    "avvelenato",
                                    "gravemente avvelenato"
                                 },
                                 10502304
                              );
                           } else if (lower.contains("burned")
                              || lower.contains("quemado")
                              || lower.contains("queimado")
                              || lower.contains("brûlé")
                              || lower.contains("verbrannt")
                              || lower.contains("scottato")
                              || lower.contains("やけど")) {
                              return colorizeWithHighlight(
                                 text,
                                 new String[]{
                                    "burned",
                                    "quemado",
                                    "quemado(a)",
                                    "quemada",
                                    "queimado",
                                    "queimado(a)",
                                    "brûlé",
                                    "brûlée",
                                    "verbrannt",
                                    "scottato",
                                    "scottata",
                                    "やけど"
                                 },
                                 15761456
                              );
                           } else if (lower.contains("paralyzed")
                              || lower.contains("paralizado")
                              || lower.contains("paralisado")
                              || lower.contains("paralysé")
                              || lower.contains("gelähmt")
                              || lower.contains("paralizzato")) {
                              return colorizeWithHighlight(
                                 text,
                                 new String[]{
                                    "paralyzed",
                                    "paralizado",
                                    "paralizado(a)",
                                    "paralizada",
                                    "paralisado",
                                    "paralisado(a)",
                                    "paralysé",
                                    "paralysée",
                                    "gelähmt",
                                    "paralizzato",
                                    "paralizzata"
                                 },
                                 16306224
                              );
                           } else if (lower.contains("fell asleep")
                              || lower.contains("asleep")
                              || lower.contains("dormido")
                              || lower.contains("adormecido")
                              || lower.contains("endormi")
                              || lower.contains("eingeschlafen")
                              || lower.contains("addormentato")) {
                              return colorizeWithHighlight(
                                 text,
                                 new String[]{
                                    "fell asleep",
                                    "asleep",
                                    "dormido",
                                    "dormido(a)",
                                    "dormida",
                                    "adormecido",
                                    "adormecido(a)",
                                    "endormi",
                                    "endormie",
                                    "eingeschlafen",
                                    "addormentato",
                                    "addormentata"
                                 },
                                 7362712
                              );
                           } else if (lower.contains("frozen")
                              || lower.contains("congelado")
                              || lower.contains("gelé")
                              || lower.contains("eingefroren")
                              || lower.contains("congelato")) {
                              return colorizeWithHighlight(
                                 text,
                                 new String[]{"frozen", "congelado", "congelado(a)", "congelada", "gelé", "gelée", "eingefroren", "congelato", "congelata"},
                                 10016984
                              );
                           } else if (lower.contains("confused")
                              || lower.contains("confundido")
                              || lower.contains("confuso")
                              || lower.contains("verwirrt")
                              || lower.contains("confus")) {
                              return colorizeWithHighlight(
                                 text,
                                 new String[]{
                                    "confused",
                                    "confundido",
                                    "confundido(a)",
                                    "confundida",
                                    "confuso",
                                    "confuso(a)",
                                    "confusa",
                                    "verwirrt",
                                    "confus",
                                    "confuse"
                                 },
                                 16275592
                              );
                           } else if (lower.contains("tightening") || lower.contains("focus")) {
                              CobblemonBattleInfoClient.debug("[Colorizer] Found tightening/focus message: '{}'", new Object[]{text});
                              return colorizePokemonNames(text);
                           } else if (lower.contains("absorbing") || lower.contains("light")) {
                              return colorizePokemonNames(text);
                           } else if (lower.contains("whipped up") || lower.contains("whirlwind")) {
                              return colorizePokemonNames(text);
                           } else if (lower.contains("lowered its head") || lower.contains("skull bash")) {
                              return colorizePokemonNames(text);
                           } else if (lower.contains("flew up high") || lower.contains("sky attack")) {
                              return colorizePokemonNames(text);
                           } else if (lower.contains("burrowed") || lower.contains("underground")) {
                              return colorizePokemonNames(text);
                           } else if (lower.contains("dove underwater") || lower.contains("underwater")) {
                              return colorizePokemonNames(text);
                           } else if (lower.contains("bounced") || lower.contains("sprang up")) {
                              return colorizePokemonNames(text);
                           } else if (lower.contains("vanished") || lower.contains("disappeared")) {
                              return colorizePokemonNames(text);
                           } else if (lower.contains("cloaked") || lower.contains("freezing air")) {
                              return colorizePokemonNames(text);
                           } else if (lower.contains("tucked") || lower.contains("shell")) {
                              return colorizePokemonNames(text);
                           } else if (lower.contains("beak") || lower.contains("heated up")) {
                              return colorizePokemonNames(text);
                           } else if (lower.contains("meteor") || lower.contains("smashing")) {
                              return colorizePokemonNames(text);
                           } else if (lower.contains("stealth rock") || lower.contains("polished stones") || lower.contains("pointed stones")) {
                              return colorizeWithHighlight(text, new String[]{"Stealth Rock", "polished stones", "pointed stones"}, TYPE_COLORS.get("rock"));
                           } else if (lower.contains("spikes") && !lower.contains("toxic")) {
                              return colorizeWithHighlight(text, new String[]{"Spikes", "spikes"}, TYPE_COLORS.get("steel"));
                           } else if (lower.contains("toxic spikes")) {
                              return colorizeWithHighlight(text, new String[]{"Toxic Spikes", "toxic spikes"}, 10502304);
                           } else if (lower.contains("sticky web")) {
                              return colorizeWithHighlight(text, new String[]{"Sticky Web", "sticky web"}, TYPE_COLORS.get("bug"));
                           } else if (lower.contains("aurora veil")) {
                              return colorizeWithHighlight(text, new String[]{"Aurora Veil"}, TYPE_COLORS.get("ice"));
                           } else if (lower.contains("light screen")) {
                              return colorizeWithHighlight(text, new String[]{"Light Screen"}, TYPE_COLORS.get("psychic"));
                           } else if (lower.contains("reflect")) {
                              return colorizeWithHighlight(text, new String[]{"Reflect"}, TYPE_COLORS.get("psychic"));
                           } else if (lower.contains("sapped by ")) {
                              return colorizeSappedByMessage(text);
                           } else if (lower.contains("was hurt") || lower.contains("took damage") || lower.contains("lost")) {
                              return colorizePokemonNames(text);
                           } else {
                              return !lower.contains("fainted") && !lower.contains("sent out") && !lower.contains("withdrew")
                                 ? colorizePokemonNames(text)
                                 : colorizePokemonNames(text);
                           }
                        }
                     } else {
                        return colorizePokemonNames(text);
                     }
                  } else {
                     return colorizeWithHighlight(text, new String[]{"sandstorm", "Sand Stream"}, 12756557);
                  }
               } else {
                  int endIdx = lower.startsWith("vai! ") ? 5 : 3;
                  result.method_10852(class_2561.method_43470(text.substring(0, endIdx)).method_27696(class_2583.field_24360.method_36139(5635925)));
                  String pokemonPart = text.substring(endIdx);
                  result.method_10852(class_2561.method_43470(pokemonPart));
                  return result;
               }
            }
         }
      }
   }

   private static class_2561 enhanceLineWithoutBattleLogColoring(String text) {
      if (text != null && !text.isEmpty()) {
         MoveLineOverride moveLineOverride = textToMoveLineOverride.get(normalizeTextLookupKey(text));
         if (moveLineOverride != null) {
            return enhanceTextWithMoveReference(text, moveLineOverride.moveName, moveLineOverride.moveType, moveLineOverride.moveId);
         } else {
            for (String moveName : textToMoveType.keySet()) {
               Pattern pattern = Pattern.compile("\\b" + Pattern.quote(moveName) + "\\b", 2);
               Matcher matcher = pattern.matcher(text);
               if (matcher.find()) {
                  return enhanceTextWithMoveReference(text, moveName, textToMoveType.get(moveName), resolveMoveIdForDisplayName(moveName));
               }
            }

            class_2561 usedMoveLine = enhanceUsedMoveLineWithoutBattleLogColoring(text);
            if (usedMoveLine != null) {
               return usedMoveLine;
            } else {
               class_5250 result = class_2561.method_43473();
               appendPlainDamageAwareText(result, text);
               return result;
            }
         }
      } else {
         return class_2561.method_43470(text);
      }
   }

   private static class_2561 colorizeTextWithMove(String text, String moveName, String moveType) {
      return colorizeTextWithMove(text, moveName, moveType, null);
   }

   private static class_2561 colorizeTextWithMove(String text, String moveName, String moveType, String moveId) {
      Integer moveColor = TYPE_COLORS.get(moveType.toLowerCase());
      if (moveColor == null) {
         return class_2561.method_43470(text);
      } else {
         class_5250 result = class_2561.method_43473();
         Pattern pattern = Pattern.compile("\\b" + Pattern.quote(moveName) + "\\b", 2);
         Matcher matcher = pattern.matcher(text);
         if (!matcher.find()) {
            return class_2561.method_43470(text);
         } else {
            int moveIdx = matcher.start();
            int moveEnd = matcher.end();
            int renderStart = moveIdx;
            int renderEnd = moveEnd;
            if (moveIdx > 0 && moveEnd < text.length() && text.charAt(moveIdx - 1) == '[' && text.charAt(moveEnd) == ']') {
               renderStart = moveIdx - 1;
               renderEnd = moveEnd + 1;
            }

            if (renderStart > 0) {
               String before = text.substring(0, renderStart);
               result.method_10852(colorizePokemonNames(before));
            }

            String moveText = text.substring(moveIdx, moveEnd);
            result.method_10852(
               createStyledMoveReference(class_2561.method_43470(moveText), moveType, firstNonBlank(moveId, resolveMoveIdForDisplayName(moveName)))
            );
            if (renderEnd < text.length()) {
               String after = text.substring(renderEnd);
               appendDamageAwareText(result, after);
            }

            return result;
         }
      }
   }

   private static class_2561 enhanceTextWithMoveReference(String text, String moveName, String moveType, String moveId) {
      if (text != null && !text.isEmpty()) {
         class_5250 result = class_2561.method_43473();
         Pattern pattern = Pattern.compile("\\b" + Pattern.quote(moveName) + "\\b", 2);
         Matcher matcher = pattern.matcher(text);
         if (!matcher.find()) {
            appendPlainDamageAwareText(result, text);
            return result;
         } else {
            int moveIdx = matcher.start();
            int moveEnd = matcher.end();
            int renderStart = moveIdx;
            int renderEnd = moveEnd;
            if (moveIdx > 0 && moveEnd < text.length() && text.charAt(moveIdx - 1) == '[' && text.charAt(moveEnd) == ']') {
               renderStart = moveIdx - 1;
               renderEnd = moveEnd + 1;
            }

            if (renderStart > 0) {
               appendPlainDamageAwareText(result, text.substring(0, renderStart));
            }

            String moveText = text.substring(moveIdx, moveEnd);
            String resolvedMoveId = firstNonBlank(moveId, resolveMoveIdForDisplayName(moveName));
            result.method_10852(createStyledMoveReference(class_2561.method_43470(moveText), moveType, resolvedMoveId));
            if (renderEnd < text.length()) {
               appendPlainDamageAwareText(result, text.substring(renderEnd));
            }

            return result;
         }
      } else {
         return class_2561.method_43470(text);
      }
   }

   private static class_2561 colorizeUsedMove(String text) {
      class_5250 result = class_2561.method_43473();
      String lower = text.toLowerCase();
      String[] usedKeywords = new String[]{"used ", "usado ", "usou ", "utilisé ", "utilise ", "setzt ", "usa ", "ha usado "};
      int usedIdx = -1;
      String foundKeyword = null;

      for (String keyword : usedKeywords) {
         int idx = lower.indexOf(keyword);
         if (idx != -1) {
            usedIdx = idx;
            foundKeyword = keyword;
            break;
         }
      }

      if (usedIdx == -1) {
         return class_2561.method_43470(text);
      } else {
         String userPart = text.substring(0, usedIdx);
         ActionSide userSide = BattleParticipantResolver.resolveSide(userPart);
         if (userSide == ActionSide.PLAYER) {
            result.method_10852(colorizePlayerActorSegment(userPart));
         } else if (userSide == ActionSide.OPPONENT) {
            result.method_10852(colorizeOpponentActorSegment(userPart));
         } else {
            result.method_10852(colorizePokemonNames(userPart));
         }

         result.method_10852(class_2561.method_43470(foundKeyword));
         String afterUsed = text.substring(usedIdx + foundKeyword.length());
         int onIdx = afterUsed.toLowerCase().indexOf(" on ");
         int exclaimIdx = afterUsed.indexOf("!");
         int moveEnd = afterUsed.length();
         if (onIdx > 0) {
            moveEnd = onIdx;
         } else if (exclaimIdx > 0) {
            moveEnd = exclaimIdx;
         }

         String moveSegment = afterUsed.substring(0, moveEnd).trim();
         DamageSuffixMatch damageSuffix = extractTrailingDamageSuffix(moveSegment);
         String moveName = unwrapMoveReferenceText(damageSuffix == null ? moveSegment : damageSuffix.baseText());
         String moveNameLower = moveName.toLowerCase(Locale.ROOT);
         String moveId = resolveMoveIdForDisplayName(moveName);
         String moveType = resolveMoveTypeForReference(moveName, moveId);
         CobblemonBattleInfoClient.debug(
            "[Colorizer] Move name extracted: '{}', lowercase: '{}', type found: {}", new Object[]{moveName, moveNameLower, moveType}
         );
         if (moveType == null && moveName.length() >= 2) {
            moveType = resolveMoveTypeByPrefix(moveNameLower);
         }

         if (moveType != null && TYPE_COLORS.containsKey(moveType)) {
            result.method_10852(createStyledMoveReference(class_2561.method_43470(moveName), moveType, moveId));
         } else {
            result.method_10852(class_2561.method_43470(moveName));
         }

         if (damageSuffix != null) {
            appendDamageSuffix(result, damageSuffix.suffixText(), damageSuffix.percent());
         }

         if (moveEnd < afterUsed.length()) {
            String rest = afterUsed.substring(moveEnd);
            if (rest.toLowerCase().startsWith(" on ")) {
               result.method_10852(class_2561.method_43470(" on "));
               String target = rest.substring(4);
               String targetClean = target.replace("!", "").trim();
               ActionSide targetSide = BattleParticipantResolver.resolveSide(targetClean);
               if (targetSide == ActionSide.PLAYER) {
                  result.method_10852(colorizePlayerActorSegment(targetClean));
               } else if (targetSide == ActionSide.OPPONENT) {
                  result.method_10852(colorizeOpponentActorSegment(targetClean));
               } else {
                  result.method_10852(colorizePokemonNames(targetClean, 0));
               }

               if (target.endsWith("!")) {
                  result.method_10852(class_2561.method_43470("!"));
               }
            } else {
               appendDamageAwareText(result, rest);
            }
         }

         return result;
      }
   }

   private static class_2561 enhanceUsedMoveLineWithoutBattleLogColoring(String text) {
      if (text != null && !text.isEmpty()) {
         String lower = text.toLowerCase(Locale.ROOT);
         String[] usedKeywords = new String[]{"used ", "usado ", "usou ", "utilisé ", "utilise ", "setzt ", "usa ", "ha usado "};
         int usedIdx = -1;
         String foundKeyword = null;

         for (String keyword : usedKeywords) {
            int idx = lower.indexOf(keyword);
            if (idx != -1) {
               usedIdx = idx;
               foundKeyword = keyword;
               break;
            }
         }

         if (usedIdx == -1) {
            return null;
         } else {
            String afterUsed = text.substring(usedIdx + foundKeyword.length());
            int onIdx = afterUsed.toLowerCase(Locale.ROOT).indexOf(" on ");
            int exclaimIdx = afterUsed.indexOf("!");
            int moveEnd = afterUsed.length();
            if (onIdx > 0) {
               moveEnd = onIdx;
            } else if (exclaimIdx > 0) {
               moveEnd = exclaimIdx;
            }

            String moveSegment = afterUsed.substring(0, moveEnd).trim();
            DamageSuffixMatch damageSuffix = extractTrailingDamageSuffix(moveSegment);
            String moveName = unwrapMoveReferenceText(damageSuffix == null ? moveSegment : damageSuffix.baseText());
            String moveId = resolveMoveIdForDisplayName(moveName);
            String moveType = resolveMoveTypeForReference(moveName, moveId);
            if (moveType == null && moveName.length() >= 2) {
               moveType = resolveMoveTypeByPrefix(moveName.toLowerCase(Locale.ROOT));
            }

            if (moveId == null && moveType == null) {
               return null;
            } else {
               class_5250 result = class_2561.method_43473();
               result.method_10852(class_2561.method_43470(text.substring(0, usedIdx + foundKeyword.length())));
               result.method_10852(createStyledMoveReference(class_2561.method_43470(moveName), moveType, moveId));
               if (damageSuffix != null) {
                  appendDamageSuffix(result, damageSuffix.suffixText(), damageSuffix.percent());
               }

               if (moveEnd < afterUsed.length()) {
                  appendPlainDamageAwareText(result, afterUsed.substring(moveEnd));
               }

               return result;
            }
         }
      } else {
         return null;
      }
   }

   private static void appendDamageAwareText(class_5250 result, String text) {
      if (result != null && text != null && !text.isEmpty()) {
         Matcher matcher = MOVE_DAMAGE_SUFFIX_PATTERN.matcher(text);

         int cursor;
         for (cursor = 0; matcher.find(); cursor = matcher.end()) {
            if (matcher.start() > cursor) {
               result.method_10852(colorizePokemonNames(text.substring(cursor, matcher.start())));
            }

            appendDamageSuffix(result, matcher.group(), parseDamagePercent(matcher.group(1)));
         }

         if (cursor < text.length()) {
            result.method_10852(colorizePokemonNames(text.substring(cursor)));
         }
      }
   }

   private static void appendPlainDamageAwareText(class_5250 result, String text) {
      if (result != null && text != null && !text.isEmpty()) {
         Matcher matcher = MOVE_DAMAGE_SUFFIX_PATTERN.matcher(text);

         int cursor;
         for (cursor = 0; matcher.find(); cursor = matcher.end()) {
            if (matcher.start() > cursor) {
               result.method_10852(class_2561.method_43470(text.substring(cursor, matcher.start())));
            }

            appendDamageSuffix(result, matcher.group(), parseDamagePercent(matcher.group(1)));
         }

         if (cursor < text.length()) {
            result.method_10852(class_2561.method_43470(text.substring(cursor)));
         }
      }
   }

   private static void appendDamageSuffix(class_5250 result, String suffixText, float percent) {
      if (result != null && suffixText != null && !suffixText.isEmpty()) {
         result.method_10852(class_2561.method_43470(suffixText).method_27696(class_2583.field_24360.method_36139(getDamagePercentColor(percent))));
      }
   }

   private static int getDamagePercentColor(float percent) {
      if (percent < 25.0F) {
         return 5635925;
      } else {
         return percent < 50.0F ? 16306224 : 16733525;
      }
   }

   private static DamageSuffixMatch extractTrailingDamageSuffix(String text) {
      if (text != null && !text.isBlank()) {
         Matcher matcher = TRAILING_MOVE_DAMAGE_SUFFIX_PATTERN.matcher(text.trim());
         return !matcher.matches() ? null : new DamageSuffixMatch(matcher.group(1).trim(), matcher.group(2), parseDamagePercent(matcher.group(3)));
      } else {
         return null;
      }
   }

   private static float parseDamagePercent(String value) {
      if (value != null && !value.isBlank()) {
         try {
            return Float.parseFloat(value);
         } catch (NumberFormatException var2) {
            return 0.0F;
         }
      } else {
         return 0.0F;
      }
   }

   private static String unwrapMoveReferenceText(String moveText) {
      if (moveText == null) {
         return "";
      } else {
         String trimmed = moveText.trim();
         return trimmed.length() >= 2 && trimmed.startsWith("[") && trimmed.endsWith("]") ? trimmed.substring(1, trimmed.length() - 1).trim() : trimmed;
      }
   }

   private static class_2561 colorizeSappedByMessage(String text) {
      class_5250 result = class_2561.method_43473();
      String lower = text.toLowerCase();
      int sappedIdx = lower.indexOf("sapped by ");
      if (sappedIdx == -1) {
         return class_2561.method_43470(text);
      } else {
         String beforeSapped = text.substring(0, sappedIdx);
         boolean isPlayerPokemon = false;
         if (playerName != null && (beforeSapped.contains(playerName + "'s ") || beforeSapped.contains("_" + playerName + "_'s "))) {
            isPlayerPokemon = true;
         }

         if (isPlayerPokemon) {
            result.method_10852(colorizePokemonNames(beforeSapped));
         } else {
            result.method_10852(class_2561.method_43470(beforeSapped).method_27696(class_2583.field_24360.method_36139(16733525)));
         }

         result.method_10852(class_2561.method_43470("sapped by "));
         String afterSapped = text.substring(sappedIdx + 10);
         String moveName = afterSapped.replace("!", "").trim();
         String moveId = resolveMoveIdForDisplayName(moveName);
         String moveType = resolveMoveTypeForReference(moveName, moveId);
         if (moveType != null) {
            if (TYPE_COLORS.containsKey(moveType)) {
               result.method_10852(createStyledMoveReference(class_2561.method_43470(moveName), moveType, moveId));
               if (afterSapped.endsWith("!")) {
                  result.method_10852(class_2561.method_43470("!"));
               }
            } else {
               result.method_10852(class_2561.method_43470(afterSapped));
            }
         } else {
            result.method_10852(class_2561.method_43470(afterSapped));
         }

         return result;
      }
   }

   private static class_2561 tryColorizeWrappedMoveLine(String text) {
      if (text != null && !text.isEmpty()) {
         String trimmed = text.trim();
         String lower = trimmed.toLowerCase();
         String movePart = lower.replace("!", "").replace(".", "").trim();
         int onIdx = movePart.indexOf(" on ");
         String moveNameToCheck = onIdx > 0 ? movePart.substring(0, onIdx) : movePart;
         String moveId = resolveMoveIdForDisplayName(moveNameToCheck);
         String moveType = resolveMoveTypeForReference(moveNameToCheck, moveId);
         if (moveType != null && TYPE_COLORS.containsKey(moveType)) {
            class_5250 result = class_2561.method_43473();
            String originalMoveName;
            if (onIdx > 0) {
               originalMoveName = trimmed.substring(0, trimmed.toLowerCase().indexOf(" on "));
            } else {
               originalMoveName = trimmed.replace("!", "").replace(".", "").trim();
            }

            result.method_10852(createStyledMoveReference(class_2561.method_43470(originalMoveName), moveType, moveId));
            if (onIdx > 0) {
               int originalOnIdx = trimmed.toLowerCase().indexOf(" on ");
               String rest = trimmed.substring(originalOnIdx);
               if (rest.toLowerCase().startsWith(" on ")) {
                  result.method_10852(class_2561.method_43470(" on "));
                  String target = rest.substring(4).replace("!", "").trim();
                  result.method_10852(class_2561.method_43470(target).method_27696(class_2583.field_24360.method_36139(16733525)));
                  if (trimmed.endsWith("!")) {
                     result.method_10852(class_2561.method_43470("!"));
                  }
               } else {
                  result.method_10852(class_2561.method_43470(rest));
               }
            } else if (trimmed.endsWith("!")) {
               result.method_10852(class_2561.method_43470("!"));
            }

            CobblemonBattleInfoClient.debug("[Colorizer] Wrapped move line colorized: '{}' as type '{}'", new Object[]{trimmed, moveType});
            return result;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static class_2561 colorizeStatChange(String text, int statColor) {
      class_5250 result = class_2561.method_43473();
      String[] statNames = new String[]{
         "Attack",
         "Defense",
         "Speed",
         "Sp. Atk",
         "Sp. Def",
         "Sp Attack",
         "Sp Defense",
         "Special Attack",
         "Special Defense",
         "Accuracy",
         "Evasion",
         "evasiveness"
      };
      if (playerName != null) {
         String checkPlayerWith = "_" + playerName + "_";
         String checkPlayerWithout = playerName;
         if (text.startsWith(checkPlayerWith)) {
            result.method_10852(class_2561.method_43470(checkPlayerWith).method_27696(class_2583.field_24360.method_36139(5635925)));
            String remaining = text.substring(checkPlayerWith.length());
            int statStart = -1;

            for (String statName : statNames) {
               int idx = remaining.indexOf(statName);
               if (idx >= 0 && (statStart == -1 || idx < statStart)) {
                  statStart = idx;
               }
            }

            if (statStart > 0) {
               result.method_10852(class_2561.method_43470(remaining.substring(0, statStart)));
               result.method_10852(class_2561.method_43470(remaining.substring(statStart)).method_27696(class_2583.field_24360.method_36139(statColor)));
            } else {
               result.method_10852(class_2561.method_43470(remaining));
            }

            return result;
         }

         if (text.startsWith(checkPlayerWithout)) {
            result.method_10852(class_2561.method_43470(checkPlayerWithout).method_27696(class_2583.field_24360.method_36139(5635925)));
            String remaining = text.substring(checkPlayerWithout.length());
            int statStart = -1;

            for (String statNamex : statNames) {
               int idx = remaining.indexOf(statNamex);
               if (idx >= 0 && (statStart == -1 || idx < statStart)) {
                  statStart = idx;
               }
            }

            if (statStart > 0) {
               result.method_10852(class_2561.method_43470(remaining.substring(0, statStart)));
               result.method_10852(class_2561.method_43470(remaining.substring(statStart)).method_27696(class_2583.field_24360.method_36139(statColor)));
            } else {
               result.method_10852(class_2561.method_43470(remaining));
            }

            return result;
         }
      }

      int possessiveIdx = text.indexOf("'s ");
      if (possessiveIdx > 0) {
         result.method_10852(class_2561.method_43470(text.substring(0, possessiveIdx + 3)).method_27696(class_2583.field_24360.method_36139(16733525)));
         result.method_10852(class_2561.method_43470(text.substring(possessiveIdx + 3)).method_27696(class_2583.field_24360.method_36139(statColor)));
      } else {
         result.method_10852(class_2561.method_43470(text).method_27696(class_2583.field_24360.method_36139(statColor)));
      }

      return result;
   }

   private static class_2561 colorizeWithHighlight(String text, String[] keywords, Integer highlightColor) {
      if (highlightColor == null) {
         return class_2561.method_43470(text);
      } else {
         class_5250 result = class_2561.method_43473();
         String remaining = text;

         while (!remaining.isEmpty()) {
            int bestIdx = -1;
            String bestKeyword = null;

            for (String keyword : keywords) {
               int idx = remaining.toLowerCase().indexOf(keyword.toLowerCase());
               if (idx >= 0 && (bestIdx < 0 || idx < bestIdx)) {
                  bestIdx = idx;
                  bestKeyword = keyword;
               }
            }

            if (bestIdx < 0 || bestKeyword == null) {
               result.method_10852(colorizePokemonNames(remaining));
               break;
            }

            if (bestIdx > 0) {
               String before = remaining.substring(0, bestIdx);
               result.method_10852(colorizePokemonNames(before));
            }

            String actual = remaining.substring(bestIdx, bestIdx + bestKeyword.length());
            result.method_10852(class_2561.method_43470(actual).method_27696(class_2583.field_24360.method_36139(highlightColor)));
            remaining = remaining.substring(bestIdx + bestKeyword.length());
         }

         return result;
      }
   }

   private static class_2561 colorizePokemonNames(String text) {
      return colorizePokemonNames(text, 0);
   }

   private static class_2561 colorizePokemonNames(String text, int depth) {
      if (text == null || text.isEmpty()) {
         return class_2561.method_43470("");
      } else if (depth > 50) {
         return class_2561.method_43470(text);
      } else {
         class_5250 result = class_2561.method_43473();
         int earliestMatchPos = text.length();
         String matchedText = null;
         int matchColor = 16777215;
         boolean foundMatch = false;

         for (String opponentPokemon : opponentPokemonNames) {
            int pokemonIdx = text.toLowerCase().indexOf(opponentPokemon);
            if (pokemonIdx >= 0 && pokemonIdx < earliestMatchPos) {
               String actualPokemonName = text.substring(pokemonIdx, pokemonIdx + opponentPokemon.length());
               earliestMatchPos = pokemonIdx;
               matchedText = actualPokemonName;
               matchColor = 16733525;
               foundMatch = true;
            }
         }

         if (playerName != null) {
            String[] namesToCheck = new String[]{"_" + playerName + "_", playerName};

            for (String nameVariant : namesToCheck) {
               int playerIdx = indexOfIgnoreCase(text, nameVariant);
               if (playerIdx >= 0 && playerIdx < earliestMatchPos) {
                  if (playerIdx >= 3) {
                     String before = text.substring(Math.max(0, playerIdx - 3), playerIdx);
                     if (before.equals("'s ") || before.equals("'s ")) {
                        continue;
                     }
                  }

                  earliestMatchPos = playerIdx;
                  matchedText = nameVariant;
                  matchColor = 5635925;
                  foundMatch = true;
               }
            }
         }

         if (opponentName != null && !opponentName.isEmpty()) {
            int opponentIdx = indexOfIgnoreCase(text, opponentName);
            if (opponentIdx >= 0 && opponentIdx < earliestMatchPos) {
               boolean isTrainerName = false;
               if (opponentIdx == 0) {
                  String after = text.substring(opponentIdx + opponentName.length()).toLowerCase();
                  if (after.startsWith(" used ")
                     || after.startsWith(" sent out ")
                     || after.startsWith(" sent ")
                     || after.startsWith(" withdrew ")
                     || after.startsWith(" called ")) {
                     isTrainerName = true;
                  }

                  if (after.startsWith("'s ") || after.startsWith("'s ")) {
                     isTrainerName = false;
                  }
               }

               String beforeContext = text.substring(0, opponentIdx).toLowerCase();
               if (beforeContext.contains("'s ") || beforeContext.contains("'s ")) {
                  isTrainerName = false;
               }

               if (beforeContext.contains("wild ")
                  || beforeContext.contains("foe ")
                  || beforeContext.contains("opposing ")
                  || beforeContext.contains("the wild ")
                  || beforeContext.contains("the foe ")
                  || beforeContext.contains("the opposing ")) {
                  isTrainerName = false;
               }

               if (isTrainerName) {
                  earliestMatchPos = opponentIdx;
                  matchedText = opponentName;
                  matchColor = 16733525;
                  foundMatch = true;
               }
            }
         }

         if (foundMatch) {
            if (earliestMatchPos > 0) {
               result.method_10852(class_2561.method_43470(text.substring(0, earliestMatchPos)));
            }

            result.method_10852(class_2561.method_43470(matchedText).method_27696(class_2583.field_24360.method_36139(matchColor)));
            int nextPos = earliestMatchPos + matchedText.length();
            if (nextPos < text.length()) {
               result.method_10852(colorizePokemonNames(text.substring(nextPos), depth + 1));
            }

            return result;
         } else {
            String leadingPlayerMatch = findLeadingTrackedPlayerMatch(text);
            if (leadingPlayerMatch != null) {
               int leadingWhitespace = 0;

               while (leadingWhitespace < text.length() && Character.isWhitespace(text.charAt(leadingWhitespace))) {
                  leadingWhitespace++;
               }

               if (leadingWhitespace > 0) {
                  result.method_10852(class_2561.method_43470(text.substring(0, leadingWhitespace)));
               }

               result.method_10852(class_2561.method_43470(leadingPlayerMatch).method_27696(class_2583.field_24360.method_36139(5635925)));
               int nextPos = leadingWhitespace + leadingPlayerMatch.length();
               if (nextPos < text.length()) {
                  result.method_10852(colorizePokemonNames(text.substring(nextPos), depth + 1));
               }

               return result;
            } else {
               String leadingOpponentMatch = findLeadingTrackedOpponentMatch(text);
               if (leadingOpponentMatch == null) {
                  return class_2561.method_43470(text);
               } else {
                  int leadingWhitespace = 0;

                  while (leadingWhitespace < text.length() && Character.isWhitespace(text.charAt(leadingWhitespace))) {
                     leadingWhitespace++;
                  }

                  if (leadingWhitespace > 0) {
                     result.method_10852(class_2561.method_43470(text.substring(0, leadingWhitespace)));
                  }

                  result.method_10852(class_2561.method_43470(leadingOpponentMatch).method_27696(class_2583.field_24360.method_36139(16733525)));
                  int nextPos = leadingWhitespace + leadingOpponentMatch.length();
                  if (nextPos < text.length()) {
                     result.method_10852(colorizePokemonNames(text.substring(nextPos), depth + 1));
                  }

                  return result;
               }
            }
         }
      }
   }

   private static String findLeadingTrackedPlayerMatch(String text) {
      if (text != null && !text.isBlank()) {
         int leadingWhitespace = 0;

         while (leadingWhitespace < text.length() && Character.isWhitespace(text.charAt(leadingWhitespace))) {
            leadingWhitespace++;
         }

         String trimmedLeading = text.substring(leadingWhitespace);
         if (trimmedLeading.isEmpty()) {
            return null;
         } else {
            String[] words = trimmedLeading.split("\\s+");
            int maxWords = Math.min(words.length, 6);

            for (int wordCount = maxWords; wordCount >= 1; wordCount--) {
               String candidate = String.join(" ", Arrays.copyOfRange(words, 0, wordCount));
               if (isTrackedPlayerCandidate(candidate)) {
                  return candidate;
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private static String findLeadingTrackedOpponentMatch(String text) {
      if (text != null && !text.isBlank()) {
         int leadingWhitespace = 0;

         while (leadingWhitespace < text.length() && Character.isWhitespace(text.charAt(leadingWhitespace))) {
            leadingWhitespace++;
         }

         String trimmedLeading = text.substring(leadingWhitespace);
         if (trimmedLeading.isEmpty()) {
            return null;
         } else {
            String[] words = trimmedLeading.split("\\s+");
            int maxWords = Math.min(words.length, 6);

            for (int wordCount = maxWords; wordCount >= 1; wordCount--) {
               String candidate = String.join(" ", Arrays.copyOfRange(words, 0, wordCount));
               if (isTrackedOpponentCandidate(candidate)) {
                  return candidate;
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private static boolean isTrackedPlayerCandidate(String rawCandidate) {
      if (rawCandidate != null && !rawCandidate.isBlank()) {
         ActionSide resolvedSide = resolveTrackedCandidateSide(rawCandidate);
         return resolvedSide == ActionSide.PLAYER;
      } else {
         return false;
      }
   }

   private static boolean isTrackedOpponentCandidate(String rawCandidate) {
      if (rawCandidate != null && !rawCandidate.isBlank()) {
         ActionSide resolvedSide = resolveTrackedCandidateSide(rawCandidate);
         if (resolvedSide == ActionSide.OPPONENT) {
            return true;
         } else if (resolvedSide == ActionSide.PLAYER) {
            return false;
         } else {
            String lowered = rawCandidate.trim().toLowerCase(Locale.ROOT);
            if (lowered.isBlank()) {
               return false;
            } else if (opponentPokemonNames.contains(lowered)) {
               return true;
            } else {
               String compact = lowered.replaceAll("[^\\p{L}\\p{N}]", "");
               return !compact.isBlank() && opponentPokemonNames.contains(compact)
                  ? true
                  : BattleMessageSubscriber.isOpponentPokemon(lowered) || !compact.isBlank() && BattleMessageSubscriber.isOpponentPokemon(compact);
            }
         }
      } else {
         return false;
      }
   }

   private static ActionSide resolveTrackedCandidateSide(String rawCandidate) {
      if (rawCandidate != null && !rawCandidate.isBlank()) {
         ActionSide directSide = BattleParticipantResolver.resolveSide(rawCandidate);
         if (directSide != ActionSide.NEUTRAL) {
            return directSide;
         } else {
            String separator = findOpponentOwnershipSeparator(rawCandidate);
            if (separator == null) {
               return ActionSide.NEUTRAL;
            } else {
               int separatorIndex = indexOfIgnoreCase(rawCandidate, separator);
               if (separatorIndex <= 0) {
                  return ActionSide.NEUTRAL;
               } else {
                  String ownerPart = rawCandidate.substring(0, separatorIndex).trim();
                  String pokemonPart = rawCandidate.substring(separatorIndex + separator.length()).trim();
                  ActionSide ownerSide = BattleParticipantResolver.resolveSide(ownerPart);
                  if (ownerSide != ActionSide.NEUTRAL) {
                     return ownerSide;
                  } else {
                     ActionSide pokemonSide = BattleParticipantResolver.resolveSide(pokemonPart);
                     if (pokemonSide != ActionSide.NEUTRAL) {
                        return pokemonSide;
                     } else if (matchesPlayerTrainerName(ownerPart)) {
                        return ActionSide.PLAYER;
                     } else if (matchesOpponentTrainerName(ownerPart)) {
                        return ActionSide.OPPONENT;
                     } else {
                        return isKnownOpponentPokemonCandidate(pokemonPart) ? ActionSide.OPPONENT : ActionSide.NEUTRAL;
                     }
                  }
               }
            }
         }
      } else {
         return ActionSide.NEUTRAL;
      }
   }

   private static boolean matchesPlayerTrainerName(String candidate) {
      if (candidate != null && !candidate.isBlank() && playerName != null) {
         String trimmedCandidate = candidate.trim();
         return trimmedCandidate.equalsIgnoreCase(playerName) || trimmedCandidate.equalsIgnoreCase("_" + playerName + "_");
      } else {
         return false;
      }
   }

   private static boolean matchesOpponentTrainerName(String candidate) {
      return candidate != null && !candidate.isBlank() && opponentName != null && !opponentName.isBlank()
         ? candidate.trim().equalsIgnoreCase(opponentName)
         : false;
   }

   private static boolean isKnownOpponentPokemonCandidate(String candidate) {
      if (candidate != null && !candidate.isBlank()) {
         String lowered = candidate.trim().toLowerCase(Locale.ROOT);
         if (opponentPokemonNames.contains(lowered)) {
            return true;
         } else {
            String compact = lowered.replaceAll("[^\\p{L}\\p{N}]", "");
            return !compact.isBlank() && opponentPokemonNames.contains(compact)
               || BattleMessageSubscriber.isOpponentPokemon(lowered)
               || !compact.isBlank() && BattleMessageSubscriber.isOpponentPokemon(compact);
         }
      } else {
         return false;
      }
   }

   private static class_2561 colorizePlayerActorSegment(String actorText) {
      if (actorText != null && !actorText.isEmpty()) {
         if (playerName != null && !playerName.isBlank()) {
            String[] variants = new String[]{"_" + playerName + "_", playerName};
            int matchIndex = -1;
            int matchLength = 0;

            for (String variant : variants) {
               if (variant != null && !variant.isBlank()) {
                  int candidateIndex = indexOfIgnoreCase(actorText, variant);
                  if (candidateIndex >= 0 && (matchIndex < 0 || candidateIndex < matchIndex)) {
                     matchIndex = candidateIndex;
                     matchLength = variant.length();
                  }
               }
            }

            if (matchIndex >= 0) {
               class_5250 result = class_2561.method_43473();
               if (matchIndex > 0) {
                  result.method_10852(class_2561.method_43470(actorText.substring(0, matchIndex)));
               }

               result.method_10852(
                  class_2561.method_43470(actorText.substring(matchIndex, matchIndex + matchLength)).method_27696(class_2583.field_24360.method_36139(5635925))
               );
               if (matchIndex + matchLength < actorText.length()) {
                  result.method_10852(class_2561.method_43470(actorText.substring(matchIndex + matchLength)));
               }

               return result;
            } else {
               return (class_2561)(BattleParticipantResolver.resolveSide(actorText) == ActionSide.PLAYER
                  ? class_2561.method_43470(actorText).method_27696(class_2583.field_24360.method_36139(5635925))
                  : colorizePokemonNames(actorText));
            }
         } else {
            return colorizePokemonNames(actorText);
         }
      } else {
         return class_2561.method_43470("");
      }
   }

   private static class_2561 colorizeOpponentActorSegment(String actorText) {
      if (actorText != null && !actorText.isEmpty()) {
         String separator = findOpponentOwnershipSeparator(actorText);
         if (separator == null) {
            return class_2561.method_43470(actorText).method_27696(class_2583.field_24360.method_36139(16733525));
         } else {
            int separatorIndex = actorText.toLowerCase(Locale.ROOT).indexOf(separator);
            if (separatorIndex <= 0) {
               return class_2561.method_43470(actorText).method_27696(class_2583.field_24360.method_36139(16733525));
            } else {
               class_5250 result = class_2561.method_43473();
               if ("'s ".equals(separator)) {
                  int pokemonStart = separatorIndex + separator.length();
                  if (pokemonStart >= actorText.length()) {
                     return class_2561.method_43470(actorText).method_27696(class_2583.field_24360.method_36139(16733525));
                  } else {
                     result.method_10852(class_2561.method_43470(actorText.substring(0, pokemonStart)));
                     result.method_10852(class_2561.method_43470(actorText.substring(pokemonStart)).method_27696(class_2583.field_24360.method_36139(16733525)));
                     return result;
                  }
               } else {
                  result.method_10852(
                     class_2561.method_43470(actorText.substring(0, separatorIndex)).method_27696(class_2583.field_24360.method_36139(16733525))
                  );
                  result.method_10852(class_2561.method_43470(actorText.substring(separatorIndex)));
                  return result;
               }
            }
         }
      } else {
         return class_2561.method_43470("");
      }
   }

   private static String findOpponentOwnershipSeparator(String actorText) {
      if (actorText != null && !actorText.isBlank()) {
         String lower = actorText.toLowerCase(Locale.ROOT);
         if (lower.contains("'s ")) {
            return "'s ";
         } else {
            for (String separator : new String[]{" de ", " von ", " di "}) {
               if (lower.contains(separator)) {
                  return separator;
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private static int indexOfIgnoreCase(String text, String needle) {
      return text != null && needle != null && !needle.isBlank() ? text.toLowerCase(Locale.ROOT).indexOf(needle.toLowerCase(Locale.ROOT)) : -1;
   }

   private static int getWeatherColor(String weatherId) {
      if (weatherId == null) {
         return 16777215;
      } else {
         String var1 = weatherId.toLowerCase();

         return switch (var1) {
            case "sandstorm", "sand" -> 12756557;
            case "rain", "raindance", "primordialsea" -> 6852848;
            case "sun", "sunnyday", "desolateland", "harsh" -> 16306224;
            case "hail" -> 10016984;
            case "snow", "snowscape" -> 12116192;
            default -> 16777215;
         };
      }
   }

   private static class_2561 colorizeAbilityMessage(String text, String abilityName) {
      return translateAndColorizeAbilityMessage(text);
   }

   private static class_2561 translateAndColorizeAbilityMessage(String messageText) {
      String[] words = messageText.split("\\s+");
      class_5250 result = class_2561.method_43473();
      boolean first = true;

      for (int i = 0; i < words.length; i++) {
         String word = words[i];
         if (!first) {
            result.method_27693(" ");
         }

         first = false;
         boolean translated = false;

         for (int wordCount = 3; wordCount >= 1 && !translated; wordCount--) {
            if (i + wordCount <= words.length) {
               StringBuilder abilityCandidate = new StringBuilder();

               for (int j = 0; j < wordCount; j++) {
                  if (j > 0) {
                     abilityCandidate.append(" ");
                  }

                  abilityCandidate.append(words[i + j].replaceAll("[^a-zA-Z\\s]", ""));
               }

               String abilityNameCheck = abilityCandidate.toString();
               if (isKnownAbility(abilityNameCheck)) {
                  String abilityKey = "cobblemon.ability." + abilityNameCheck.toLowerCase().replace(" ", "");
                  String lastWord = words[i + wordCount - 1];
                  String punctuation = lastWord.replaceAll("[a-zA-Z\\s]", "");
                  result.method_10852(class_2561.method_43471(abilityKey).method_27694(style -> style.method_36139(16766720)));
                  if (!punctuation.isEmpty()) {
                     result.method_27693(punctuation);
                  }

                  i += wordCount - 1;
                  translated = true;
               }
            }
         }

         if (!translated) {
            class_2561 colorized = colorizeWord(word);
            result.method_10852(colorized);
         }
      }

      return result;
   }

   private static class_2561 colorizeWord(String word) {
      if (playerName != null && word.toLowerCase().contains(playerName.toLowerCase())) {
         return class_2561.method_43470(word).method_27696(class_2583.field_24360.method_36139(5635925));
      } else {
         return opponentPokemonNames.contains(word.toLowerCase().replaceAll("[^a-z]", ""))
            ? class_2561.method_43470(word).method_27696(class_2583.field_24360.method_36139(16733525))
            : class_2561.method_43470(word);
      }
   }

   private static boolean isKnownAbility(String name) {
      if (name != null && !name.isEmpty()) {
         String[] knownAbilities = new String[]{
            "Intimidate",
            "Overgrow",
            "Blaze",
            "Torrent",
            "Swarm",
            "Keen Eye",
            "Tangled Feet",
            "Big Pecks",
            "Guts",
            "Hustle",
            "Inner Focus",
            "Shed Skin",
            "Marvel Scale",
            "Limber",
            "Imposter",
            "Chlorophyll",
            "Solar Power",
            "Drought",
            "Drizzle",
            "Sand Stream",
            "Snow Warning",
            "Pressure",
            "Volt Absorb",
            "Water Absorb",
            "Flash Fire",
            "Levitate",
            "Effect Spore",
            "Synchronize",
            "Clear Body",
            "Liquid Ooze",
            "Rain Dish",
            "Sand Veil",
            "Static",
            "Lightning Rod",
            "Compound Eyes",
            "Shield Dust",
            "Run Away",
            "Wonder Guard",
            "Sturdy",
            "Damp",
            "Arena Trap",
            "Vital Spirit",
            "White Smoke",
            "Pure Power",
            "Shell Armor",
            "Air Lock",
            "Adaptability",
            "Skill Link",
            "Hydration",
            "Poison Heal",
            "Rivalry",
            "Steadfast",
            "Snow Cloak",
            "Gluttony",
            "Anger Point",
            "Unburden",
            "Heatproof",
            "Simple",
            "Dry Skin",
            "Download",
            "Iron Fist",
            "Poison Touch",
            "Regenerator",
            "Big Pecks",
            "Sand Rush",
            "Wonder Skin",
            "Analytic",
            "Illusion",
            "Imposter",
            "Infiltrator",
            "Mummy",
            "Moxie",
            "Justified",
            "Rattled",
            "Magic Bounce",
            "Sap Sipper",
            "Prankster",
            "Sand Force",
            "Telepathy",
            "Moody",
            "Overcoat",
            "Poison Touch",
            "Regenerator",
            "Friend Guard",
            "Weak Armor",
            "Heavy Metal",
            "Light Metal",
            "Multiscale",
            "Toxic Boost",
            "Flare Boost",
            "Harvest",
            "Telepathy",
            "Moody",
            "Overcoat",
            "Poison Touch",
            "Desolate Land",
            "Primordial Sea",
            "Delta Stream",
            "Stamina",
            "Wimp Out",
            "Emergency Exit",
            "Water Compaction",
            "Merciless",
            "Shields Down",
            "Stakeout",
            "Water Bubble",
            "Steelworker",
            "Berserk",
            "Slush Rush",
            "Long Reach",
            "Liquid Voice",
            "Triage",
            "Galvanize",
            "Surge Surfer",
            "Schooling",
            "Disguise",
            "Battle Bond",
            "Power Construct",
            "Corrosion",
            "Comatose",
            "Queenly Majesty",
            "Innards Out",
            "Dancer",
            "Battery",
            "Fluffy",
            "Dazzling",
            "Soul Heart",
            "Tangling Hair",
            "Receiver",
            "Power of Alchemy",
            "Beast Boost",
            "RKS System",
            "Electric Surge",
            "Psychic Surge",
            "Misty Surge",
            "Grassy Surge",
            "Full Metal Body",
            "Shadow Shield",
            "Prism Armor",
            "Neuroforce",
            "Intrepid Sword",
            "Dauntless Shield",
            "Libero",
            "Ball Fetch",
            "Cotton Down",
            "Propeller Tail",
            "Mirror Armor",
            "Gulp Missile",
            "Stalwart",
            "Steam Engine",
            "Punk Rock",
            "Sand Spit",
            "Ice Scales",
            "Ripen",
            "Ice Face",
            "Power Spot",
            "Mimicry",
            "Screen Cleaner",
            "Steely Spirit",
            "Perish Body",
            "Wandering Spirit",
            "Gorilla Tactics",
            "Neutralizing Gas",
            "Pastel Veil",
            "Hunger Switch",
            "Quick Draw",
            "Unseen Fist",
            "Curious Medicine",
            "Transistor",
            "Dragon's Maw",
            "Chilling Neigh",
            "Grim Neigh",
            "As One",
            "Lingering Aroma",
            "Seed Sower",
            "Thermal Exchange",
            "Anger Shell",
            "Purifying Salt",
            "Well Baked Body",
            "Wind Rider",
            "Guard Dog",
            "Rocky Payload",
            "Wind Power",
            "Zero to Hero",
            "Commander",
            "Electromorphosis",
            "Protosynthesis",
            "Quark Drive",
            "Good as Gold",
            "Vessel of Ruin",
            "Sword of Ruin",
            "Tablets of Ruin",
            "Beads of Ruin",
            "Orichalcum Pulse",
            "Hadron Engine",
            "Opportunist",
            "Cud Chew",
            "Sharpness",
            "Supreme Overlord",
            "Costar",
            "Toxic Debris",
            "Armor Tail",
            "Earth Eater",
            "Mycelium Might",
            "Minds Eye",
            "Supersweet Syrup",
            "Hospitality",
            "Toxic Chain",
            "Embody Aspect",
            "Tera Shift",
            "Tera Shell",
            "Teraform Zero",
            "Poison Puppeteer"
         };
         String normalized = name.trim();

         for (String ability : knownAbilities) {
            if (ability.equalsIgnoreCase(normalized)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static class_2561 colorizeMegaEvolutionPhrase(String text, String megaPhrase) {
      class_5250 result = class_2561.method_43473();
      String lower = text.toLowerCase();
      String lowerPhrase = megaPhrase.toLowerCase();
      int megaIdx = lower.lastIndexOf(lowerPhrase);
      if (megaIdx == -1) {
         String[] fallbackPhrases = new String[]{"mega evolved!", "mega evolved", "mega evolución!", "mega evolución"};

         for (String phrase : fallbackPhrases) {
            megaIdx = lower.lastIndexOf(phrase);
            if (megaIdx != -1) {
               lowerPhrase = phrase;
               break;
            }
         }
      }

      if (megaIdx == -1) {
         return colorizePokemonNames(text);
      } else {
         if (megaIdx > 0) {
            String before = text.substring(0, megaIdx);
            result.method_10852(colorizePokemonNames(before));
         }

         String megaText = text.substring(megaIdx, megaIdx + lowerPhrase.length());
         result.method_10852(class_2561.method_43470(megaText).method_27696(class_2583.field_24360.method_36139(11163135)));
         if (megaIdx + lowerPhrase.length() < text.length()) {
            String after = text.substring(megaIdx + lowerPhrase.length());
            result.method_10852(class_2561.method_43470(after));
         }

         return result;
      }
   }

   public static class_2561 colorizeTypeComponent(class_2561 typeComponent) {
      if (typeComponent == null) {
         return class_2561.method_43473();
      } else {
         String typeId = extractTypeId(typeComponent);
         if (typeId != null && !typeId.isBlank()) {
            String rendered = typeComponent.getString();
            if (rendered == null || rendered.isBlank()) {
               rendered = prettifyTypeId(typeId);
            }

            if ("stellar".equals(typeId)) {
               return createRainbowText(rendered);
            } else {
               Integer typeColor = TYPE_COLORS.get(typeId);
               return typeColor == null ? typeComponent.method_27661() : typeComponent.method_27661().method_27694(style -> style.method_36139(typeColor));
            }
         } else {
            return typeComponent.method_27661();
         }
      }
   }

   private static class_2561 colorizeTerastallizationMessage(String text) {
      class_5250 result = class_2561.method_43473();
      String lower = text.toLowerCase();
      String[] types = new String[]{
         "normal",
         "fire",
         "water",
         "electric",
         "grass",
         "ice",
         "fighting",
         "poison",
         "ground",
         "flying",
         "psychic",
         "bug",
         "rock",
         "ghost",
         "dragon",
         "dark",
         "steel",
         "fairy",
         "stellar"
      };
      String foundType = null;
      int typeStartIdx = -1;

      for (String type : types) {
         int idx = lower.indexOf(type + "-type");
         if (idx == -1) {
            idx = lower.indexOf(type + " type");
         }

         if (idx == -1) {
            idx = lower.indexOf(type + "-");
         }

         if (idx != -1) {
            foundType = type;
            typeStartIdx = idx;
            break;
         }
      }

      if (foundType != null && typeStartIdx != -1) {
         Integer typeColor = TYPE_COLORS.get(foundType);
         if (typeColor == null) {
            if (foundType.equals("stellar")) {
               typeColor = 11163135;
            } else {
               typeColor = 16777215;
            }
         }

         String originalTypeName = text.substring(typeStartIdx, typeStartIdx + foundType.length());
         originalTypeName = originalTypeName.substring(0, 1).toUpperCase() + originalTypeName.substring(1).toLowerCase();
         if (typeStartIdx > 0) {
            String before = text.substring(0, typeStartIdx);
            result.method_10852(colorizePokemonNames(before));
         }

         if (foundType.equals("stellar")) {
            result.method_10852(createRainbowText(originalTypeName));
         } else {
            result.method_10852(class_2561.method_43470(originalTypeName).method_27696(class_2583.field_24360.method_36139(typeColor)));
         }

         int afterTypeIdx = typeStartIdx + foundType.length();
         if (afterTypeIdx < text.length()) {
            String after = text.substring(afterTypeIdx);
            result.method_10852(class_2561.method_43470(after));
         }

         return result;
      } else {
         return colorizePokemonNames(text);
      }
   }

   public static class_2561 createStyledMoveReference(class_2561 moveComponent) {
      if (moveComponent == null) {
         return class_2561.method_43473();
      } else {
         String moveText = moveComponent.getString();
         String moveId = extractMoveIdFromComponent(moveComponent);
         String moveType = resolveMoveTypeForReference(moveText, moveId);
         return createStyledMoveReference(moveComponent, moveType, moveId);
      }
   }

   public static class_2561 tryCreateStyledMoveReference(class_2561 moveComponent) {
      if (moveComponent == null) {
         return null;
      } else {
         String moveId = extractMoveIdFromComponent(moveComponent);
         String moveType = resolveMoveTypeForReference(moveComponent.getString(), moveId);
         if (moveId == null && moveType == null) {
            return null;
         } else {
            MoveTooltipData tooltipData = BattleLogMoveTooltipHelper.getTooltipData(moveId);
            return moveType == null && tooltipData == null ? null : createStyledMoveReference(moveComponent, moveType, moveId);
         }
      }
   }

   public static String resolveMoveIdForBattleLogReference(String moveText) {
      if (moveText != null && !moveText.isBlank()) {
         String trimmed = moveText.trim();
         if (trimmed.length() >= 2 && trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
         }

         return resolveMoveIdForDisplayName(trimmed);
      } else {
         return null;
      }
   }

   public static class_2561 createStyledMoveReference(class_2561 moveComponent, String moveType, String moveId) {
      return (class_2561)(moveComponent == null
         ? class_2561.method_43473()
         : BattleLogMoveTooltipHelper.createMoveReferenceComponent(
            moveComponent, firstNonBlank(moveId, resolveMoveIdForDisplayName(moveComponent.getString())), moveType
         ));
   }

   private static String extractTypeId(class_2561 typeComponent) {
      if (typeComponent == null) {
         return null;
      } else {
         if (typeComponent.method_10851() instanceof class_2588 tc) {
            String key = tc.method_11022();
            if (key != null && key.startsWith("cobblemon.type.")) {
               String[] parts = key.split("\\.");
               if (parts.length > 0) {
                  return parts[parts.length - 1].toLowerCase(Locale.ROOT);
               }
            }
         }

         String rendered = typeComponent.getString();
         if (rendered != null && !rendered.isBlank()) {
            String normalized = rendered.trim().toLowerCase(Locale.ROOT);
            return !"stellar".equals(normalized) && !TYPE_COLORS.containsKey(normalized) ? null : normalized;
         } else {
            return null;
         }
      }
   }

   private static String extractMoveIdFromComponent(class_2561 moveComponent) {
      if (moveComponent != null && moveComponent.method_10851() instanceof class_2588 tc) {
         String key = tc.method_11022();
         if (key != null && key.startsWith("cobblemon.move.")) {
            String[] parts = key.split("\\.");
            if (parts.length > 0) {
               return normalizeMoveId(parts[parts.length - 1]);
            }
         }
      }

      return resolveMoveIdForDisplayName(moveComponent == null ? null : moveComponent.getString());
   }

   private static String resolveMoveTypeForReference(String moveText, String moveId) {
      String moveType = resolveMoveType(moveText);
      if (moveType != null) {
         return moveType;
      } else {
         MoveTooltipData tooltipData = BattleLogMoveTooltipHelper.getTooltipData(moveId);
         return tooltipData != null && tooltipData.type() != null ? tooltipData.type().getName().toLowerCase(Locale.ROOT) : null;
      }
   }

   private static String resolveMoveIdForDisplayName(String moveName) {
      String normalizedName = normalizeTextLookupKey(moveName);
      if (normalizedName == null) {
         return null;
      } else {
         String direct = textToMoveId.get(normalizedName);
         if (direct != null) {
            return direct;
         } else {
            String compact = normalizeMoveId(normalizedName);
            if (compact == null) {
               return null;
            } else {
               String compactRegistered = textToMoveId.get(compact);
               if (compactRegistered != null) {
                  return compactRegistered;
               } else {
                  return compact.matches("[a-z0-9_]+") ? compact : null;
               }
            }
         }
      }
   }

   private static String normalizeMoveId(String moveId) {
      if (moveId == null) {
         return null;
      } else {
         String normalized = moveId.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "");
         return normalized.isEmpty() ? null : normalized;
      }
   }

   private static String firstNonBlank(String first, String second) {
      return first != null && !first.isBlank() ? first : second;
   }

   private static String prettifyTypeId(String typeId) {
      return typeId != null && !typeId.isBlank() ? Character.toUpperCase(typeId.charAt(0)) + typeId.substring(1).toLowerCase(Locale.ROOT) : "";
   }

   private static class_2561 createRainbowText(String text) {
      class_5250 result = class_2561.method_43473();
      int[] rainbowColors = new int[]{16733525, 16755200, 16777045, 5635925, 5636095, 5592575, 16733695};

      for (int i = 0; i < text.length(); i++) {
         int colorIndex = i % rainbowColors.length;
         result.method_10852(
            class_2561.method_43470(String.valueOf(text.charAt(i))).method_27696(class_2583.field_24360.method_36139(rainbowColors[colorIndex]))
         );
      }

      return result;
   }

   static {
      initTypeColors();
      initMoveTypes();
   }
}
