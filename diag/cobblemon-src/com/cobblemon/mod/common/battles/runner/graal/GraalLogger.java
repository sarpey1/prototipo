package com.cobblemon.mod.common.battles.runner.graal

import com.cobblemon.mod.common.Cobblemon
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord

object GraalLogger private constructor() : Handler {
   @JvmStatic
   public GraalLogger INSTANCE = new GraalLogger();


   public override fun publish(record: LogRecord?) {
      if (record != null) {
         var var2: Level = record.getLevel();
         if (var2 == Level.INFO) {
            Cobblemon.LOGGER.info(record.getMessage());
         } else if (var2 == Level.WARNING) {
            Cobblemon.LOGGER.warn(record.getMessage());
         } else if (var2 == Level.SEVERE) {
            Cobblemon.LOGGER.error(record.getMessage());
         } else {
            Cobblemon.LOGGER.debug(record.getMessage());
         }
      }
   }

   public override fun flush() {
   }

   public override fun close() {
   }
}
