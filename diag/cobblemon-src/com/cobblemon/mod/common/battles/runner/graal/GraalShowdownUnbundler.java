package com.cobblemon.mod.common.battles.runner.graal

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.util.FileUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Path
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nGraalShowdownUnbundler.kt\nKotlin\n*S Kotlin\n*F\n+ 1 GraalShowdownUnbundler.kt\ncom/cobblemon/mod/common/battles/runner/graal/GraalShowdownUnbundler\n+ 2 GsonExtensions.kt\ncom/cobblemon/mod/common/util/GsonExtensionsKt\n*L\n1#1,99:1\n17#2:100\n17#2:101\n*S KotlinDebug\n*F\n+ 1 GraalShowdownUnbundler.kt\ncom/cobblemon/mod/common/battles/runner/graal/GraalShowdownUnbundler\n*L\n79#1:100\n89#1:101\n*E\n"])
class GraalShowdownUnbundler {
   private final val gson: Gson = new GsonBuilder().disableHtmlEscaping().create()


   public fun attemptUnbundle() {
      var showdownDir: File = new File("showdown");
      var metadata: GraalShowdownUnbundler.ShowdownMetadata = this.loadShowdownMetadata();
      if (!showdownDir.exists() || Cobblemon.INSTANCE.getConfig().getAutoUpdateShowdown()) {
         showdownDir.mkdirs();
         var showdownZip: File = new File(showdownDir, "showdown.zip");
         var showdownMetadataFile: File = new File(showdownDir, "showdown.json");
         var extract: Boolean = true;
         if (showdownMetadataFile.exists()) {
            var current: GraalShowdownUnbundler.ShowdownMetadata = this.readShowdownMetadata(showdownMetadataFile);
            var var10000: Double = metadata.getShowdownVersion();
            if (var10000 == current.getShowdownVersion()) {
               extract = false;
            } else {
               Cobblemon.LOGGER
                  .info("Updating showdown service to version " + metadata.getShowdownVersion() + ", from version " + current.getShowdownVersion() + "...");
               var backupDir: File = new File("showdown-backup");
               if (backupDir.exists() && backupDir.isDirectory()) {
                  FilesKt.deleteRecursively(backupDir);
               }

               FilesKt.copyTo$default(showdownDir, backupDir, false, 0, 6, null);
            }
         }

         if (extract) {
            var var8: FileUtils = FileUtils.INSTANCE;
            var var10002: Path = showdownZip.toPath();
            var8.copyInternalToExternal("/data/cobblemon/showdown.zip", var10002);
            var8 = FileUtils.INSTANCE;
            var10002 = showdownMetadataFile.toPath();
            var8.copyInternalToExternal("/data/cobblemon/showdown.json", var10002);
            var8 = FileUtils.INSTANCE;
            var var10001: Path = showdownZip.toPath();
            var10002 = showdownDir.toPath();
            var8.unzipFile(var10001, var10002);
            showdownZip.delete();
         }
      }
   }

   private fun loadShowdownMetadata(): com.cobblemon.mod.common.battles.runner.graal.GraalShowdownUnbundler.ShowdownMetadata? {
      try {
         var var10000: InputStream = this.getClass().getResourceAsStream("/data/cobblemon/showdown.json");
         var var6: Gson = this.gson;
         return var6.fromJson(new InputStreamReader(var10000), GraalShowdownUnbundler.ShowdownMetadata.class) as GraalShowdownUnbundler.ShowdownMetadata;
      } catch (Exception var5) {
         var5.printStackTrace();
         return null;
      }
   }

   private fun readShowdownMetadata(target: File): com.cobblemon.mod.common.battles.runner.graal.GraalShowdownUnbundler.ShowdownMetadata? {
      try {
         label21: {
            var var2: Closeable = new InputStreamReader(new FileInputStream(target));
            var exception: java.lang.Throwable = null;

            try {
               try {
                  var it: InputStreamReader = var2 as InputStreamReader;
                  var var10000: Gson = this.gson;
                  var var9: GraalShowdownUnbundler.ShowdownMetadata = var10000.fromJson(it, GraalShowdownUnbundler.ShowdownMetadata.class) as GraalShowdownUnbundler.ShowdownMetadata;
               } catch (java.lang.Throwable var10) {
                  exception = var10;
                  throw var10;
               }
            } catch (java.lang.Throwable var11) {
               CloseableKt.closeFinally(var2, exception);
            }

            CloseableKt.closeFinally(var2, null);
         }
      } catch (Exception var12) {
         var12.printStackTrace();
         return null;
      }
   }

   private data class ShowdownMetadata(showdownVersion: Double) {
      public final val showdownVersion: Double

      init {
         this.showdownVersion = showdownVersion;
      }

      public operator fun component1(): Double {
         return this.showdownVersion;
      }

      public fun copy(showdownVersion: Double): com.cobblemon.mod.common.battles.runner.graal.GraalShowdownUnbundler.ShowdownMetadata {
         return new GraalShowdownUnbundler.ShowdownMetadata(showdownVersion);
      }

      public override fun toString(): String {
         return "ShowdownMetadata(showdownVersion=" + this.showdownVersion + ")";
      }

      public override fun hashCode(): Int {
         return java.lang.Double.hashCode(this.showdownVersion);
      }

      public override operator fun equals(other: Any?): Boolean {
         if (this === other) {
            return true;
         } else if (other !is GraalShowdownUnbundler.ShowdownMetadata) {
            return false;
         } else {
            return java.lang.Double.compare(this.showdownVersion, (other as GraalShowdownUnbundler.ShowdownMetadata).showdownVersion) == 0;
         }
      }
   }
}
