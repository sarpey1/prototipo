package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.interpreter.BasicContext
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.interpreter.Effect
import com.cobblemon.mod.common.api.battles.interpreter.InvalidInstructionException
import com.cobblemon.mod.common.api.battles.interpreter.MissingContext
import com.cobblemon.mod.common.api.battles.interpreter.Effect.Type
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.text.TextKt
import com.cobblemon.mod.common.battles.dispatch.InstructionSet
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.battles.interpreter.ContextManager
import com.cobblemon.mod.common.battles.interpreter.ContextManager.Companion
import com.cobblemon.mod.common.battles.interpreter.instructions.AbilityInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.ActivateInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.BagItemInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.BlockInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.BoostInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.CantInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.CenterInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.ClearAllBoostInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.ClearBoostInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.ClearNegativeBoostInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.CopyBoostInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.CritInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.CureStatusInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.DamageInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.DragInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.EndAbilityInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.EndInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.EndItemInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.ErrorInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.FailInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.FaintInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.FieldActivateInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.FieldEndInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.FieldStartInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.FormeChangeInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.HealInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.HitCountInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.IgnoredInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.ImmuneInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.InitializeInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.InvertBoostInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.ItemInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.MegaInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.MissInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.MoveInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.NothingInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.PpUpdateInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.PrepareInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.RechargeInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.ReplaceInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.RequestInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.ResistedInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.SetBoostInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.SetHpInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.SideEndInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.SideStartInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.SingleMoveInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.SingleTurnInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.StartInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.StatusInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.SuperEffectiveInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.SwapBoostInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.SwapInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.SwapSideConditionsInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.SwitchInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.TerastallizeInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.TransformInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.TurnInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.UnknownInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.UpkeepInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.WeatherInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.WinInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.ZBrokenInstruction
import com.cobblemon.mod.common.battles.interpreter.instructions.ZPowerInstruction
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.util.DistributionUtilsKt
import com.cobblemon.mod.common.util.LocalizationUtilsKt
import java.util.ArrayList
import java.util.Arrays
import java.util.LinkedHashMap
import java.util.UUID
import kotlin.jvm.functions.Function4
import kotlin.jvm.functions.Function6
import kotlin.jvm.internal.SourceDebugExtension
import net.minecraft.class_2561
import net.minecraft.class_5250

@SourceDebugExtension(["SMAP\nShowdownInterpreter.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ShowdownInterpreter.kt\ncom/cobblemon/mod/common/battles/ShowdownInterpreter\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 _Arrays.kt\nkotlin/collections/ArraysKt___ArraysKt\n+ 4 ArraysJVM.kt\nkotlin/collections/ArraysKt__ArraysJVMKt\n*L\n1#1,334:1\n1869#2,2:335\n1563#2:340\n1634#2,3:341\n1869#2,2:346\n3919#3:337\n4434#3,2:338\n37#4,2:344\n*S KotlinDebug\n*F\n+ 1 ShowdownInterpreter.kt\ncom/cobblemon/mod/common/battles/ShowdownInterpreter\n*L\n178#1:335,2\n247#1:340\n247#1:341,3\n61#1:346,2\n246#1:337\n246#1:338,2\n249#1:344,2\n*E\n"])
object ShowdownInterpreter private constructor() {
   @JvmStatic
   public ShowdownInterpreter INSTANCE = new ShowdownInterpreter();

   public final val lastCauser: MutableMap<UUID, BattleMessage> = (new LinkedHashMap()) as java.util.Map
   private final val updateInstructionParser: MutableMap<
      String,
      (PokemonBattle, InstructionSet, BattleMessage, Iterator<BattleMessage>) -> InterpreterInstruction
   > = (new LinkedHashMap()) as java.util.Map
   private final val splitInstructionParser: MutableMap<
      String,
      (PokemonBattle, BattleActor, InstructionSet, BattleMessage, BattleMessage, Iterator<BattleMessage>) -> InterpreterInstruction
   > = (new LinkedHashMap()) as java.util.Map
   private final val sideInstructionParser: MutableMap<String, (PokemonBattle, BattleActor, InstructionSet, BattleMessage) -> InterpreterInstruction> =
      (new LinkedHashMap()) as java.util.Map


   public fun interpretMessage(battleId: UUID, message: String) {
      if (!StringsKt.startsWith$default(message, "{\"winner\":\"", false, 2, null)) {
         var battle: PokemonBattle = BattleRegistry.getBattle(battleId);
         if (battle == null) {
            Cobblemon.LOGGER.info("No battle could be found with the id: " + battleId);
         } else {
            DistributionUtilsKt.runOnServer(ShowdownInterpreter::interpretMessage$lambda$0);
         }
      }
   }

   public fun interpret(battle: PokemonBattle, rawMessage: String) {
      PokemonBattle.log$default(battle, null, 1, null);
      battle.log(rawMessage);
      PokemonBattle.log$default(battle, null, 1, null);
      var instructionSet: InstructionSet = new InstructionSet();
      var battleMessages: java.util.List = new ArrayList();

      try {
         var lines: java.util.List = CollectionsKt.toMutableList(StringsKt.split$default(rawMessage, new java.lang.String[]{"\n"}, false, 0, 6, null));
         if (lines.get(0) == "update") {
            lines.remove(0);

            var var15: java.lang.Iterable;
            for (Object element$iv : var15) {
               battleMessages.add(new BattleMessage(var22 as java.lang.String));
            }

            var var16: java.util.Iterator = battleMessages.iterator();

            while (iterator.hasNext()) {
               var var30: InterpreterInstruction;
               label40: {
                  var var18: BattleMessage = var16.next() as BattleMessage;
                  var var29: Function4 = updateInstructionParser.get(StringsKt.replace$default(var18.getId(), "|", "", false, 4, null));
                  if (var29 != null) {
                     var30 = var29.invoke(battle, instructionSet, var18, var16) as InterpreterInstruction;
                     if (var30 != null) {
                        break label40;
                     }
                  }

                  var30 = (new UnknownInstruction(var18)) as InterpreterInstruction;
               }

               instructionSet.getInstructions().add(var30);
            }
         } else if (lines.get(0) == "sideupdate") {
            var var14: java.lang.String = lines.get(1) as java.lang.String;
            var targetActor: BattleActor = battle.getActor(var14);
            var message: BattleMessage = new BattleMessage(lines.get(2) as java.lang.String);
            var var21: java.lang.String = StringsKt.replace$default(message.getId(), "|", "", false, 4, null);
            if (targetActor == null) {
               battle.log("No actor could be found with the showdown id: " + var14);
               return;
            }

            var var28: InterpreterInstruction;
            label58: {
               var var27: Function4 = sideInstructionParser.get(var21);
               if (var27 != null) {
                  var28 = var27.invoke(battle, targetActor, instructionSet, message) as InterpreterInstruction;
                  if (var28 != null) {
                     break label58;
                  }
               }

               var28 = (new UnknownInstruction(message)) as InterpreterInstruction;
            }

            instructionSet.getInstructions().add(var28);
         }

         instructionSet.execute(battle);
      } catch (InvalidInstructionException var12) {
         var var10000: java.lang.String = var12.getMessage();
         if (var10000 != null) {
            battle.broadcastChatMessage(TextKt.red(var10000) as class_2561);
            Cobblemon.LOGGER.error(var10000);
         }
      } catch (Exception var13) {
         battle.broadcastChatMessage(TextKt.red("A fatal error occurred. Please report to developers.") as class_2561);
         Cobblemon.LOGGER.error("Caught exception interpreting battle instructions.", var13);
      }
   }

   public fun broadcastOptionalAbility(battle: PokemonBattle, effect: Effect?, pokemon: BattlePokemon) {
      if (effect != null && effect.getType() === Type.ABILITY) {
         this.broadcastAbility(battle, effect, pokemon);
      }
   }

   public fun broadcastAbility(battle: PokemonBattle, effect: Effect, pokemon: BattlePokemon) {
      battle.dispatchWaiting(0.5F, ShowdownInterpreter::broadcastAbility$lambda$0);
   }

   public fun getContextFromFaint(pokemon: BattlePokemon, battle: PokemonBattle): BattleContext {
      var var10000: BattleMessage = battle.getMinorBattleActions().get(pokemon.getUuid()) as BattleMessage;
      if (var10000 == null) {
         var10000 = lastCauser.get(battle.getBattleId());
         if (var10000 == null) {
            return (new MissingContext(null, 0, null, null, 15, null)) as BattleContext;
         }
      }

      var side: BattleSide = pokemon.getActor().getSide();
      var var5: java.lang.String = var10000.getId();
      switch (var5.hashCode()) {
         case -1387046880:
            if (var5.equals("-activate")) {
               var var62: Effect = var10000.effectAt(1);
               return if (var62 != null)
                  (
                     new BasicContext(
                        var62.getId(),
                        battle.getTurn(),
                        com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type.FAINT,
                        var10000.battlePokemon(0, battle)
                     )
                  ) as BattleContext
                  else
                  (new MissingContext(null, 0, null, null, 15, null)) as BattleContext;
            }

            return (new MissingContext(null, 0, null, null, 15, null)) as BattleContext;
         case -56166948:
            if (!var5.equals("-damage")) {
               return (new MissingContext(null, 0, null, null, 15, null)) as BattleContext;
            }
            break;
         case 3357649:
            if (!var5.equals("move")) {
               return (new MissingContext(null, 0, null, null, 15, null)) as BattleContext;
            }
            break;
         case 1398069333:
            if (var5.equals("-start")) {
               var var48: Effect = var10000.effectAt(1);
               if (var48 != null) {
                  var var50: BattleContext = ContextManager.Companion
                     .scoop(
                        if (StringsKt.contains$default(var48.getId(), "perish", false, 2, null)) "perishsong" else var48.getId(),
                        new java.util.Collection[]{
                           pokemon.getContextManager().get(com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type.VOLATILE)
                        }
                     );
                  if (var50 != null) {
                     return var50;
                  }
               }

               return (new MissingContext(null, 0, null, null, 15, null)) as BattleContext;
            }

            return (new MissingContext(null, 0, null, null, 15, null)) as BattleContext;
         default:
            return (new MissingContext(null, 0, null, null, 15, null)) as BattleContext;
      }

      var var51: Effect = var10000.effect("of");
      var var63: BattleContext;
      if (var51 != null) {
         label89: {
            var var52: Effect = BattleMessage.effect$default(var10000, null, 1, null);
            if (var52 != null) {
               var53 = var52.getId();
               if (var53 != null) {
                  break label89;
               }
            }

            var53 = var51.getId();
         }

         var var54: java.lang.String = var10000.optionalArgument("of");
         var var29: java.lang.String = StringsKt.substringBefore$default(var54, ':', null, 2, null);
         var var55: java.lang.String = var10000.optionalArgument("of");
         var63 = (
            new BasicContext(
               var53,
               battle.getTurn(),
               com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type.FAINT,
               battle.getBattlePokemon(var29, StringsKt.trim(StringsKt.substringAfter$default(var55, ':', null, 2, null)).toString())
            )
         ) as BattleContext;
      } else {
         var var56: Effect = BattleMessage.effect$default(var10000, null, 1, null);
         if (var56 == null) {
            var63 = null;
         } else {
            var var32: Array<Any> = com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type.values();
            var `$i$f$toTypedArray`: java.util.Collection = new ArrayList();

            for (Object element$iv$iv : $this$filter$iv) {
               if (`item$iv$iv`.getDamaging()) {
                  `$i$f$toTypedArray`.add(`item$iv$iv`);
               }
            }

            var var36: java.lang.Iterable = `$i$f$toTypedArray` as java.util.List;
            var `destination$iv$ivx`: java.util.Collection = new ArrayList(CollectionsKt.collectionSizeOrDefault(`$i$f$toTypedArray` as java.util.List, 10));

            for (Object item$iv$iv : $this$map$iv) {
               var it: com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type = var46 as com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type;
               var var57: java.util.Collection = pokemon.getContextManager().get(var46 as com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type);
               if (var57 == null) {
                  var57 = side.getContextManager().get(it);
                  if (var57 == null) {
                     var57 = battle.getContextManager().get(it);
                  }
               }

               `destination$iv$ivx`.add(var57);
            }

            var32 = `destination$iv$ivx` as java.util.List;
            var var58: Companion = ContextManager.Companion;
            var var10001: java.lang.String = var56.getId();
            var var37: Array<java.util.Collection> = (var32 as java.util.Collection).toArray(new java.util.Collection[0]);
            var63 = var58.scoop(var10001, Arrays.copyOf(var37, var37.length));
         }

         if (var63 == null) {
            var10000 = lastCauser.get(battle.getBattleId());
            var var61: BasicContext;
            if (var10000 != null) {
               var var60: Effect = var10000.effectAt(1);
               var61 = new BasicContext(
                  var60.getId(), battle.getTurn(), com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type.FAINT, var10000.battlePokemon(0, battle)
               );
            } else {
               var61 = null;
            }

            var63 = if (var61 != null) var61 as BattleContext else (new MissingContext(null, 0, null, null, 15, null)) as BattleContext;
         }
      }

      return var63;
   }

   public fun getContextFromAction(message: BattleMessage, type: com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type, battle: PokemonBattle): BattleContext {
      var var10000: Pair = BattleMessage.actorAndActivePokemonFromOptional$default(message, battle, null, 2, null);
      var var16: BattleContext;
      if (var10000 != null) {
         label67: {
            var var13: Effect = message.effectAt(1);
            if (var13 != null) {
               var14 = var13.getId();
               if (var14 != null) {
                  break label67;
               }
            }

            var var15: Effect = message.effectAt(0);
            if (var15 == null) {
               return (new MissingContext(null, 0, null, null, 15, null)) as BattleContext;
            }

            var14 = var15.getId();
         }

         var16 = (new BasicContext(var14, battle.getTurn(), type, (var10000.getSecond() as ActiveBattlePokemon).getBattlePokemon())) as BattleContext;
      } else if (message.actorAndActivePokemon(0, battle) != null) {
         var var17: Effect = message.effectAt(1);
         if (var17 != null) {
            var var18: java.lang.String = var17.getId();
            if (var18 != null) {
               var var19: BattleMessage = lastCauser.get(battle.getBattleId());
               if (var19 != null) {
                  var var20: BattlePokemon = var19.battlePokemon(0, battle);
                  if (var20 != null) {
                     return (new BasicContext(var18, battle.getTurn(), type, var20)) as BattleContext;
                  }
               }

               return (new MissingContext(null, 0, null, null, 15, null)) as BattleContext;
            }
         }

         var16 = (new MissingContext(null, 0, null, null, 15, null)) as BattleContext;
      } else {
         var var21: BattleMessage = lastCauser.get(battle.getBattleId());
         if (var21 == null) {
            var16 = null;
         } else {
            label71: {
               label69: {
                  var var22: Effect = message.effectAt(1);
                  if (var22 != null) {
                     var23 = var22.getId();
                     if (var23 != null) {
                        break label69;
                     }
                  }

                  var var24: Effect = message.effectAt(0);
                  if (var24 == null) {
                     var16 = (new MissingContext(null, 0, null, null, 15, null)) as BattleContext;
                     break label71;
                  }

                  var23 = var24.getId();
               }

               var16 = (new BasicContext(var23, battle.getTurn(), type, var21.battlePokemon(0, battle))) as BattleContext;
            }
         }

         if (var16 == null) {
            var16 = (new MissingContext(null, 0, null, null, 15, null)) as BattleContext;
         }
      }

      return var16;
   }

   @JvmStatic
   public fun registerUpdateInstructionParser(
      id: String,
      callback: (PokemonBattle, InstructionSet, BattleMessage, Iterator<BattleMessage>) -> InterpreterInstruction
   ) {
      updateInstructionParser.put(id, callback);
   }

   @JvmStatic
   public fun registerSplitInstructionParser(
      id: String,
      callback: (PokemonBattle, BattleActor, InstructionSet, BattleMessage, BattleMessage, Iterator<BattleMessage>) -> InterpreterInstruction
   ) {
      splitInstructionParser.put(id, callback);
   }

   @JvmStatic
   public fun registerSideInstructionParser(id: String, callback: (PokemonBattle, BattleActor, InstructionSet, BattleMessage) -> InterpreterInstruction) {
      sideInstructionParser.put(id, callback);
   }

   @JvmStatic
   fun `_init_$lambda$0`(battle: PokemonBattle, instructionSet: InstructionSet, message: BattleMessage, messages: java.util.Iterator): InterpreterInstruction {
      var privateMessage: BattleMessage = messages.next() as BattleMessage;
      var publicMessage: BattleMessage = messages.next() as BattleMessage;
      var var10001: java.lang.String = message.argumentAt(0);
      var var10000: BattleActor = battle.getActor(var10001);
      var var10: Function6 = splitInstructionParser.get(
         StringsKt.split$default(publicMessage.getRawMessage(), new java.lang.String[]{"|"}, false, 0, 6, null).get(1) as java.lang.String
      );
      if (var10 != null) {
         var var11: InterpreterInstruction = var10.invoke(battle, var10000, instructionSet, publicMessage, privateMessage, messages) as InterpreterInstruction;
         if (var11 != null) {
            return var11;
         }
      }

      return (new IgnoredInstruction()) as InterpreterInstruction;
   }

   @JvmStatic
   fun `lambda$1$0`(var0: PokemonBattle, var1: InstructionSet, var2: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new IgnoredInstruction()) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$2`(var0: PokemonBattle, instructionSet: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new AbilityInstruction(instructionSet, message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$3`(var0: PokemonBattle, instructionSet: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new ActivateInstruction(instructionSet, message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$4`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new BagItemInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$5`(battle: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new BoostInstruction(battle, message, true)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$6`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new BlockInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$7`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new CantInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$8`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new ClearAllBoostInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$9`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new ClearNegativeBoostInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$10`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new ClearBoostInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$11`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new CopyBoostInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$12`(var0: PokemonBattle, instructionSet: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new CritInstruction(message, instructionSet)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$13`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new CureStatusInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$14`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new FormeChangeInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$15`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new EndAbilityInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$16`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new EndInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$17`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new EndItemInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$18`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new FailInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$19`(battle: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new FaintInstruction(battle, message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$20`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new FieldActivateInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$21`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new FieldEndInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$22`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new FieldStartInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$23`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new FormeChangeInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$24`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new HitCountInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$25`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new ImmuneInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$26`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new InvertBoostInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$27`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new ItemInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$28`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new MegaInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$29`(battle: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new MissInstruction(battle, message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$30`(var0: PokemonBattle, instructionSet: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new MoveInstruction(instructionSet, message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$31`(var0: PokemonBattle, var1: InstructionSet, var2: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new NothingInstruction()) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$32`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new PpUpdateInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$33`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new PrepareInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$34`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new RechargeInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$35`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new ReplaceInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$36`(var0: PokemonBattle, instructionSet: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new ResistedInstruction(message, instructionSet)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$37`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new SetBoostInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$38`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new SideEndInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$39`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new SideStartInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$40`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new SingleMoveInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$41`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new SingleTurnInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$42`(var0: PokemonBattle, instructionSet: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new InitializeInstruction(instructionSet, message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$43`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new StartInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$44`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new StatusInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$45`(var0: PokemonBattle, instructionSet: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new SuperEffectiveInstruction(message, instructionSet)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$46`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new SwapBoostInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$47`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new SwapSideConditionsInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$48`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new TerastallizeInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$49`(battle: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new TransformInstruction(battle, message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$50`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new TurnInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$51`(battle: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new BoostInstruction(battle, message, false)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$52`(var0: PokemonBattle, var1: InstructionSet, var2: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new UpkeepInstruction()) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$53`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new WeatherInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$54`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new WinInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$55`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new ZBrokenInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$56`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new ZPowerInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$57`(var0: PokemonBattle, instructionSet: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new SwapInstruction(message, instructionSet)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$58`(var0: PokemonBattle, var1: InstructionSet, message: BattleMessage, var3: java.util.Iterator): InterpreterInstruction {
      return (new CenterInstruction(message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$59`(var0: PokemonBattle, targetActor: BattleActor, var2: InstructionSet, message: BattleMessage): InterpreterInstruction {
      return (new ErrorInstruction(targetActor, message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$60`(var0: PokemonBattle, targetActor: BattleActor, var2: InstructionSet, message: BattleMessage): InterpreterInstruction {
      return (new RequestInstruction(targetActor, message)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$61`(
      var0: PokemonBattle,
      targetActor: BattleActor,
      instructionSet: InstructionSet,
      publicMessage: BattleMessage,
      privateMessage: BattleMessage,
      var5: java.util.Iterator
   ): InterpreterInstruction {
      return (new DamageInstruction(instructionSet, targetActor, publicMessage, privateMessage)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$62`(
      var0: PokemonBattle,
      targetActor: BattleActor,
      instructionSet: InstructionSet,
      publicMessage: BattleMessage,
      privateMessage: BattleMessage,
      var5: java.util.Iterator
   ): InterpreterInstruction {
      return (new DragInstruction(instructionSet, targetActor, publicMessage, privateMessage)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$63`(
      var0: PokemonBattle,
      targetActor: BattleActor,
      var2: InstructionSet,
      publicMessage: BattleMessage,
      privateMessage: BattleMessage,
      var5: java.util.Iterator
   ): InterpreterInstruction {
      return (new HealInstruction(targetActor, publicMessage, privateMessage)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$64`(
      var0: PokemonBattle,
      targetActor: BattleActor,
      var2: InstructionSet,
      publicMessage: BattleMessage,
      privateMessage: BattleMessage,
      var5: java.util.Iterator
   ): InterpreterInstruction {
      return (new SetHpInstruction(targetActor, publicMessage, privateMessage)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `_init_$lambda$65`(
      var0: PokemonBattle,
      targetActor: BattleActor,
      instructionSet: InstructionSet,
      publicMessage: BattleMessage,
      privateMessage: BattleMessage,
      var5: java.util.Iterator
   ): InterpreterInstruction {
      return (new SwitchInstruction(instructionSet, targetActor, publicMessage, privateMessage)) as InterpreterInstruction;
   }

   @JvmStatic
   fun `interpretMessage$lambda$0`(`$battle`: PokemonBattle, `$message`: java.lang.String): Unit {
      `$battle`.getShowdownMessages().add(`$message`);
      INSTANCE.interpret(`$battle`, `$message`);
      return Unit.INSTANCE;
   }

   @JvmStatic
   fun `broadcastAbility$lambda$0`(`$pokemon`: BattlePokemon, `$effect`: Effect, `$battle`: PokemonBattle): Unit {
      var var10000: class_5250 = LocalizationUtilsKt.battleLang("ability.generic", new Object[]{`$pokemon`.getName(), `$effect`.getTypelessData()});
      `$battle`.broadcastChatMessage(TextKt.yellow(var10000) as class_2561);
      return Unit.INSTANCE;
   }

   @JvmStatic
   fun {
      updateInstructionParser.put("split", ShowdownInterpreter::_init_$lambda$0);

      var var6: java.lang.Iterable;
      for (Object element$iv : var6) {
         updateInstructionParser.put(`element$iv` as java.lang.String, ShowdownInterpreter::lambda$1$0);
      }

      updateInstructionParser.put("-ability", ShowdownInterpreter::_init_$lambda$2);
      updateInstructionParser.put("-activate", ShowdownInterpreter::_init_$lambda$3);
      updateInstructionParser.put("bagitem", ShowdownInterpreter::_init_$lambda$4);
      updateInstructionParser.put("-boost", ShowdownInterpreter::_init_$lambda$5);
      updateInstructionParser.put("-block", ShowdownInterpreter::_init_$lambda$6);
      updateInstructionParser.put("cant", ShowdownInterpreter::_init_$lambda$7);
      updateInstructionParser.put("-clearallboost", ShowdownInterpreter::_init_$lambda$8);
      updateInstructionParser.put("-clearnegativeboost", ShowdownInterpreter::_init_$lambda$9);
      updateInstructionParser.put("-clearboost", ShowdownInterpreter::_init_$lambda$10);
      updateInstructionParser.put("-copyboost", ShowdownInterpreter::_init_$lambda$11);
      updateInstructionParser.put("-crit", ShowdownInterpreter::_init_$lambda$12);
      updateInstructionParser.put("-curestatus", ShowdownInterpreter::_init_$lambda$13);
      updateInstructionParser.put("detailschange", ShowdownInterpreter::_init_$lambda$14);
      updateInstructionParser.put("-endability", ShowdownInterpreter::_init_$lambda$15);
      updateInstructionParser.put("-end", ShowdownInterpreter::_init_$lambda$16);
      updateInstructionParser.put("-enditem", ShowdownInterpreter::_init_$lambda$17);
      updateInstructionParser.put("-fail", ShowdownInterpreter::_init_$lambda$18);
      updateInstructionParser.put("faint", ShowdownInterpreter::_init_$lambda$19);
      updateInstructionParser.put("-fieldactivate", ShowdownInterpreter::_init_$lambda$20);
      updateInstructionParser.put("-fieldend", ShowdownInterpreter::_init_$lambda$21);
      updateInstructionParser.put("-fieldstart", ShowdownInterpreter::_init_$lambda$22);
      updateInstructionParser.put("-formechange", ShowdownInterpreter::_init_$lambda$23);
      updateInstructionParser.put("-hitcount", ShowdownInterpreter::_init_$lambda$24);
      updateInstructionParser.put("-immune", ShowdownInterpreter::_init_$lambda$25);
      updateInstructionParser.put("-invertboost", ShowdownInterpreter::_init_$lambda$26);
      updateInstructionParser.put("-item", ShowdownInterpreter::_init_$lambda$27);
      updateInstructionParser.put("-mega", ShowdownInterpreter::_init_$lambda$28);
      updateInstructionParser.put("-miss", ShowdownInterpreter::_init_$lambda$29);
      updateInstructionParser.put("move", ShowdownInterpreter::_init_$lambda$30);
      updateInstructionParser.put("-nothing", ShowdownInterpreter::_init_$lambda$31);
      updateInstructionParser.put("pp_update", ShowdownInterpreter::_init_$lambda$32);
      updateInstructionParser.put("-prepare", ShowdownInterpreter::_init_$lambda$33);
      updateInstructionParser.put("-mustrecharge", ShowdownInterpreter::_init_$lambda$34);
      updateInstructionParser.put("replace", ShowdownInterpreter::_init_$lambda$35);
      updateInstructionParser.put("-resisted", ShowdownInterpreter::_init_$lambda$36);
      updateInstructionParser.put("-setboost", ShowdownInterpreter::_init_$lambda$37);
      updateInstructionParser.put("-sideend", ShowdownInterpreter::_init_$lambda$38);
      updateInstructionParser.put("-sidestart", ShowdownInterpreter::_init_$lambda$39);
      updateInstructionParser.put("-singlemove", ShowdownInterpreter::_init_$lambda$40);
      updateInstructionParser.put("-singleturn", ShowdownInterpreter::_init_$lambda$41);
      updateInstructionParser.put("start", ShowdownInterpreter::_init_$lambda$42);
      updateInstructionParser.put("-start", ShowdownInterpreter::_init_$lambda$43);
      updateInstructionParser.put("-status", ShowdownInterpreter::_init_$lambda$44);
      updateInstructionParser.put("-supereffective", ShowdownInterpreter::_init_$lambda$45);
      updateInstructionParser.put("-swapboost", ShowdownInterpreter::_init_$lambda$46);
      updateInstructionParser.put("-swapsideconditions", ShowdownInterpreter::_init_$lambda$47);
      updateInstructionParser.put("-terastallize", ShowdownInterpreter::_init_$lambda$48);
      updateInstructionParser.put("-transform", ShowdownInterpreter::_init_$lambda$49);
      updateInstructionParser.put("turn", ShowdownInterpreter::_init_$lambda$50);
      updateInstructionParser.put("-unboost", ShowdownInterpreter::_init_$lambda$51);
      updateInstructionParser.put("upkeep", ShowdownInterpreter::_init_$lambda$52);
      updateInstructionParser.put("-weather", ShowdownInterpreter::_init_$lambda$53);
      updateInstructionParser.put("win", ShowdownInterpreter::_init_$lambda$54);
      updateInstructionParser.put("-zbroken", ShowdownInterpreter::_init_$lambda$55);
      updateInstructionParser.put("-zpower", ShowdownInterpreter::_init_$lambda$56);
      updateInstructionParser.put("swap", ShowdownInterpreter::_init_$lambda$57);
      updateInstructionParser.put("-center", ShowdownInterpreter::_init_$lambda$58);
      sideInstructionParser.put("error", ShowdownInterpreter::_init_$lambda$59);
      sideInstructionParser.put("request", ShowdownInterpreter::_init_$lambda$60);
      splitInstructionParser.put("-damage", ShowdownInterpreter::_init_$lambda$61);
      splitInstructionParser.put("drag", ShowdownInterpreter::_init_$lambda$62);
      splitInstructionParser.put("-heal", ShowdownInterpreter::_init_$lambda$63);
      splitInstructionParser.put("-sethp", ShowdownInterpreter::_init_$lambda$64);
      splitInstructionParser.put("switch", ShowdownInterpreter::_init_$lambda$65);
   }
}
