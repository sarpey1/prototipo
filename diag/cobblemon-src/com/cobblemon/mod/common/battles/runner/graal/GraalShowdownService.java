package com.cobblemon.mod.common.battles.runner.graal

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.runner.ShowdownService
import com.cobblemon.mod.relocations.graalvm.polyglot.Context
import com.cobblemon.mod.relocations.graalvm.polyglot.HostAccess
import com.cobblemon.mod.relocations.graalvm.polyglot.PolyglotAccess
import com.cobblemon.mod.relocations.graalvm.polyglot.Value
import com.cobblemon.mod.relocations.graalvm.polyglot.HostAccess.Export
import com.cobblemon.mod.relocations.graalvm.polyglot.io.FileSystem
import com.google.gson.Gson
import com.google.gson.JsonArray
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.AccessMode
import java.nio.file.DirectoryStream
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.DirectoryStream.Filter
import java.nio.file.attribute.FileAttribute
import java.util.Arrays
import java.util.UUID
import java.util.Map.Entry
import kotlin.jvm.internal.Intrinsics

class GraalShowdownService : ShowdownService {
   public final lateinit var context: Context
      private set
   public final lateinit var sendBattleMessageFunction: Value
      private set
   public final val unbundler: GraalShowdownUnbundler = new GraalShowdownUnbundler()
   public final val gson: Gson = new Gson()


   fun getContext(): Context {
      if (this.context != null) {
         return this.context;
      } else {
         Intrinsics.throwUninitializedPropertyAccessException("context");
         return null;
      }
   }

   fun setContext(`<set-?>`: Context) {
      this.context = `<set-?>`;
   }

   fun getSendBattleMessageFunction(): Value {
      if (this.sendBattleMessageFunction != null) {
         return this.sendBattleMessageFunction;
      } else {
         Intrinsics.throwUninitializedPropertyAccessException("sendBattleMessageFunction");
         return null;
      }
   }

   fun setSendBattleMessageFunction(`<set-?>`: Value) {
      this.sendBattleMessageFunction = `<set-?>`;
   }

   public open fun openConnection() {
      this.unbundler.attemptUnbundle();
      this.createContext();
      this.boot();
   }

   private fun createContext() {
      var var10001: Context = Context.newBuilder(new java.lang.String[]{"js"})
         .allowIO(true)
         .fileSystem(new FileSystem(Paths.get("./showdown")) {
            private final FileSystem default;

            {
               this.$wd = `$wd`;
               this.default = FileSystem.newDefaultFileSystem();
            }

            public final FileSystem getDefault() {
               return this.default;
            }

            public Path parsePath(URI uri) {
               return this.default.parsePath(uri);
            }

            public Path parsePath(java.lang.String path) {
               return this.default.parsePath(path);
            }

            public void createDirectory(Path dir, FileAttribute<?>... attrs) {
               this.default.createDirectory(dir, Arrays.copyOf(attrs, attrs.length));
            }

            public void delete(Path path) {
               this.default.delete(path);
            }

            public SeekableByteChannel newByteChannel(Path path, java.util.Set<? extends OpenOption> options, FileAttribute<?>... attrs) {
               return this.default.newByteChannel(path, options, Arrays.copyOf(attrs, attrs.length));
            }

            public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) {
               return this.default.newDirectoryStream(dir, filter);
            }

            public Path toAbsolutePath(Path path) {
               return this.default.toAbsolutePath(path);
            }

            public Path toRealPath(Path path, LinkOption... linkOptions) {
               return this.default.toRealPath(path, Arrays.copyOf(linkOptions, linkOptions.length));
            }

            public java.util.Map<java.lang.String, Object> readAttributes(Path path, java.lang.String attributes, LinkOption... options) {
               return this.default.readAttributes(path, attributes, Arrays.copyOf(options, options.length));
            }

            public void checkAccess(Path path, java.util.Set<? extends AccessMode> modes, LinkOption... linkOptions) {
               if (!path.toRealPath(LinkOption.NOFOLLOW_LINKS).startsWith(this.$wd.toRealPath(LinkOption.NOFOLLOW_LINKS))) {
                  Cobblemon.LOGGER.error("Hacked JS files in datapacks or some weird file system setup that Hiroku failed to anticipate.");
                  throw new IOException("Someone has put hacked JS files into datapacks because file access is being attempted outside of controlled folders.");
               }
            }
         })
         .allowExperimentalOptions(true)
         .allowPolyglotAccess(PolyglotAccess.ALL)
         .allowHostAccess(
            HostAccess.newBuilder(HostAccess.EXPLICIT).allowIterableAccess(true).allowArrayAccess(true).allowListAccess(true).allowMapAccess(true).build()
         )
         .allowCreateThread(true)
         .logHandler(GraalLogger.INSTANCE)
         .option("engine.WarnInterpreterOnly", "false")
         .option("js.commonjs-require", "true")
         .option("js.commonjs-require-cwd", "showdown")
         .option("js.commonjs-core-modules-replacements", "buffer:buffer/,crypto:crypto-browserify,path:path-browserify")
         .allowHostClassLoading(true)
         .allowNativeAccess(true)
         .allowCreateProcess(true)
         .build();
      this.setContext(var10001);
      this.getContext().eval("js", "globalThis.process = {\n    cwd: function() {\n        return '';\n    }\n}");
   }

   public open fun closeConnection() {
      this.getContext().close();
   }

   private fun boot() {
      this.getContext().eval("js", FilesKt.readText$default(new File("showdown/index.js"), null, 1, null));
      var var10001: Value = this.getContext().getBindings("js").getMember("sendBattleMessage");
      this.setSendBattleMessageFunction(var10001);
   }

   public open fun startBattle(battle: PokemonBattle, messages: Array<String>) {
      this.getContext().getBindings("js").getMember("startBattle").execute(new Object[]{this, battle.getBattleId().toString(), messages});
   }

   public open fun endBattle(battle: PokemonBattle) {
      this.getContext().getBindings("js").getMember("endBattle").execute(new Object[]{battle.getBattleId().toString()});
   }

   public open fun send(battleId: UUID, messages: Array<String>) {
      this.sendToShowdown(battleId, messages);
   }

   public open fun getRegistryData(type: String): JsonArray {
      var var10000: Any = this.gson.fromJson(this.getContext().getBindings("js").getMember("getData").execute(new Object[]{type}).asString(), JsonArray.class);
      return var10000 as JsonArray;
   }

   public open fun sendRegistryData(data: Map<String, String>, type: String) {
      this.getContext()
         .getBindings("js")
         .getMember("receiveData")
         .execute(
            new Object[]{
               CollectionsKt.joinToString$default(data.entrySet(), null, "{", "}", 0, null, GraalShowdownService::sendRegistryData$lambda$0, 25, null), type
            }
         );
   }

   public open fun sendRegistryEntry(data: String, type: String) {
      this.getContext().getBindings("js").getMember("receiveData").execute(new Object[]{new Regex("[\r\n]+").replace(data, " "), type});
   }

   public open fun resetRegistryData(type: String) {
      this.getContext().getBindings("js").getMember("resetData").execute(new Object[]{type});
   }

   public open fun resetAllRegistries() {
      this.getContext().getBindings("js").getMember("resetAll").execute(new Object[0]);
   }

   public open fun indicateSpeciesInitialized() {
      this.getContext().getBindings("js").getMember("afterSpeciesInit").execute(new Object[0]);
   }

   private fun sendToShowdown(battleId: UUID, messages: Array<String>) {
      this.getSendBattleMessageFunction().execute(new Object[]{battleId.toString(), messages});
   }

   @Export
   public fun sendFromShowdown(battleId: String, message: String) {
      var var10000: ShowdownInterpreter = ShowdownInterpreter.INSTANCE;
      var var10001: UUID = UUID.fromString(battleId);
      var10000.interpretMessage(var10001, message);
   }

   @Export
   public fun log(message: String) {
      Cobblemon.LOGGER.info(message);
   }

   @JvmStatic
   fun `sendRegistryData$lambda$0`(var0: Entry): java.lang.CharSequence {
      return "\"" + var0.getKey() as java.lang.String + "\": " + new Regex("[\r\n]+").replace(var0.getValue() as java.lang.String, " ");
   }
}
