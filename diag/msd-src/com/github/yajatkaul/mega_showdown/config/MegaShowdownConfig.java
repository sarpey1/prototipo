package com.github.yajatkaul.mega_showdown.config;

import com.github.yajatkaul.mega_showdown.MegaShowdown;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class MegaShowdownConfig {
   private static final String FILE_PATH = "./config/mega_showdown/config.json";
   public static boolean loaded = false;
   public static int teraShardRequired = 50;
   public static boolean outSideMega = true;
   public static boolean outSideUltraBurst = true;
   public static boolean multipleMegas = false;
   public static boolean msdPatchAutoUpdate = true;
   public static boolean mega = true;
   public static boolean zMoves = true;
   public static boolean teralization = true;
   public static boolean dynamax = true;
   public static int powerSpotRange = 20;
   public static boolean dynamaxAnywhere = false;
   public static float dynamaxScaleFactor = 4.0F;
   public static double teraShardDropRate = 10.0;
   public static double stellarShardDropRate = 1.0;
   public static boolean teraHats = true;
   public static boolean legacyTeraEffect = false;
   public static int likoPendentDuration = 72000;
   public static int minBondingRequired = 200;
   public static boolean showBattleHUD = true;
   public static boolean showStatChanges = true;
   public static boolean showMoveInspector = true;
   public static boolean debugMode = false;

   private static void save() {
      JsonObject json = getJsonObject();

      try {
         Files.createDirectories(Path.of("./config/mega_showdown"));

         try (FileWriter writer = new FileWriter("./config/mega_showdown/config.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(json));
         }
      } catch (IOException var6) {
         MegaShowdown.LOGGER.error("Failed to save MegaShowdown config:", var6);
      }
   }

   @NotNull
   private static JsonObject getJsonObject() {
      JsonObject json = new JsonObject();
      json.addProperty("teraShardRequired", teraShardRequired);
      json.addProperty("multipleMegas", multipleMegas);
      json.addProperty("msdPatchAutoUpdate", msdPatchAutoUpdate);
      json.addProperty("mega", mega);
      json.addProperty("zMoves", zMoves);
      json.addProperty("teralization", teralization);
      json.addProperty("dynamax", dynamax);
      json.addProperty("powerSpotRange", powerSpotRange);
      json.addProperty("dynamaxAnywhere", dynamaxAnywhere);
      json.addProperty("dynamaxScaleFactor", dynamaxScaleFactor);
      json.addProperty("teraShardDropRate", teraShardDropRate);
      json.addProperty("stellarShardDropRate", stellarShardDropRate);
      json.addProperty("likoPendentDuration", likoPendentDuration);
      json.addProperty("minBondingRequired", minBondingRequired);
      json.addProperty("outSideMega", outSideMega);
      json.addProperty("outSideUltraBurst", outSideUltraBurst);
      json.addProperty("teraHats", teraHats);
      json.addProperty("legacyTeraEffect", legacyTeraEffect);
      json.addProperty("showBattleHUD", showBattleHUD);
      json.addProperty("showStatChanges", showStatChanges);
      json.addProperty("showMoveTooltips", showMoveInspector);
      json.addProperty("debugMode", debugMode);
      return json;
   }

   public static void load() {
      File file = new File("./config/mega_showdown/config.json");
      if (!file.exists()) {
         MegaShowdown.LOGGER.info("MegaShowdown config not found, creating default.");
         save();
      } else {
         try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            JsonObject json = (JsonObject)gson.fromJson(reader, JsonObject.class);
            if (json.has("teraShardRequired")) {
               teraShardRequired = json.get("teraShardRequired").getAsInt();
            }

            if (json.has("multipleMegas")) {
               multipleMegas = json.get("multipleMegas").getAsBoolean();
            }

            if (json.has("msdPathAutoUpdate")) {
               msdPatchAutoUpdate = json.get("msdPathAutoUpdate").getAsBoolean();
            }

            if (json.has("mega")) {
               mega = json.get("mega").getAsBoolean();
            }

            if (json.has("zMoves")) {
               zMoves = json.get("zMoves").getAsBoolean();
            }

            if (json.has("teralization")) {
               teralization = json.get("teralization").getAsBoolean();
            }

            if (json.has("dynamax")) {
               dynamax = json.get("dynamax").getAsBoolean();
            }

            if (json.has("powerSpotRange")) {
               powerSpotRange = json.get("powerSpotRange").getAsInt();
            }

            if (json.has("dynamaxAnywhere")) {
               dynamaxAnywhere = json.get("dynamaxAnywhere").getAsBoolean();
            }

            if (json.has("dynamaxScaleFactor")) {
               dynamaxScaleFactor = json.get("dynamaxScaleFactor").getAsFloat();
            }

            if (json.has("teraShardDropRate")) {
               teraShardDropRate = json.get("teraShardDropRate").getAsDouble();
            }

            if (json.has("stellarShardDropRate")) {
               stellarShardDropRate = json.get("stellarShardDropRate").getAsDouble();
            }

            if (json.has("likoPendentDuration")) {
               likoPendentDuration = json.get("likoPendentDuration").getAsInt() * 20;
            }

            if (json.has("minBondingRequired")) {
               minBondingRequired = json.get("minBondingRequired").getAsInt();
            }

            if (json.has("outSideMega")) {
               outSideMega = json.get("outSideMega").getAsBoolean();
            }

            if (json.has("outSideUltraBurst")) {
               outSideUltraBurst = json.get("outSideUltraBurst").getAsBoolean();
            }

            if (json.has("teraHats")) {
               teraHats = json.get("teraHats").getAsBoolean();
            }

            if (json.has("legacyTeraEffect")) {
               legacyTeraEffect = json.get("legacyTeraEffect").getAsBoolean();
            }

            if (json.has("showBattleHUD")) {
               showBattleHUD = json.get("showBattleHUD").getAsBoolean();
            }

            if (json.has("showStatChanges")) {
               showStatChanges = json.get("showStatChanges").getAsBoolean();
            }

            if (json.has("showMoveTooltips")) {
               showMoveInspector = json.get("showMoveTooltips").getAsBoolean();
            }

            if (json.has("debugMode")) {
               debugMode = json.get("debugMode").getAsBoolean();
            }
         } catch (Exception var6) {
            MegaShowdown.LOGGER.error("Failed to load MegaShowdown config:", var6);
         }

         loaded = true;
      }
   }
}
