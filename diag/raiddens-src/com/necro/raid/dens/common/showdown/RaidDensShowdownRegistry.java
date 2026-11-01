package com.necro.raid.dens.common.showdown;

import com.cobblemon.mod.common.battles.ShowdownInterpreter;
import com.necro.raid.dens.common.showdown.instructions.CheerBoostInstruction;
import com.necro.raid.dens.common.showdown.instructions.CheerInstruction;
import com.necro.raid.dens.common.showdown.instructions.DoNothingInstruction;
import com.necro.raid.dens.common.showdown.instructions.PlayerJoinInstruction;
import com.necro.raid.dens.common.showdown.instructions.RaidBoostInstruction;
import com.necro.raid.dens.common.showdown.instructions.RaidDamageInstruction;
import com.necro.raid.dens.common.showdown.instructions.RaidEnergyInstruction;
import com.necro.raid.dens.common.showdown.instructions.RaidHealInstruction;
import com.necro.raid.dens.common.showdown.instructions.ResetBossInstruction;
import com.necro.raid.dens.common.showdown.instructions.ResetPlayerInstruction;
import com.necro.raid.dens.common.showdown.instructions.ShieldAddInstruction;
import com.necro.raid.dens.common.showdown.instructions.ShieldRemoveInstruction;

public class RaidDensShowdownRegistry {
   public static void registerInstructions() {
      ShowdownInterpreter.registerUpdateInstructionParser("cheer", (battle, instruction, message, messageIterator) -> new CheerInstruction(message));
      ShowdownInterpreter.registerUpdateInstructionParser(
         "cheerboost", (battle, instruction, message, messageIterator) -> new CheerBoostInstruction(battle, message)
      );
      ShowdownInterpreter.registerUpdateInstructionParser(
         "clearboss", (battle, instruction, message, messageIterator) -> new ResetBossInstruction(battle, message)
      );
      ShowdownInterpreter.registerUpdateInstructionParser(
         "clearplayer", (battle, instruction, message, messageIterator) -> new ResetPlayerInstruction(battle, message)
      );
      ShowdownInterpreter.registerUpdateInstructionParser(
         "raidenergy", (battle, instruction, message, messageIterator) -> new RaidEnergyInstruction(battle, message)
      );
      ShowdownInterpreter.registerUpdateInstructionParser(
         "playerjoin", (battle, instruction, message, messageIterator) -> new PlayerJoinInstruction(battle, message)
      );
      ShowdownInterpreter.registerUpdateInstructionParser(
         "shieldadd", (battle, instruction, message, messageIterator) -> new ShieldAddInstruction(battle, message)
      );
      ShowdownInterpreter.registerUpdateInstructionParser(
         "shieldremove", (battle, instruction, message, messageIterator) -> new ShieldRemoveInstruction(battle, message)
      );
      ShowdownInterpreter.registerUpdateInstructionParser("donothing", (battle, instruction, message, messageIterator) -> new DoNothingInstruction());
      ShowdownInterpreter.registerUpdateInstructionParser(
         "-raidboost", (battle, instruction, message, messageIterator) -> new RaidBoostInstruction(battle, message, true)
      );
      ShowdownInterpreter.registerUpdateInstructionParser(
         "-raidunboost", (battle, instruction, message, messageIterator) -> new RaidBoostInstruction(battle, message, false)
      );
      ShowdownInterpreter.registerSplitInstructionParser(
         "-raiddamage",
         (battle, actor, instruction, publicMessage, privateMessage, messageIterator) -> new RaidDamageInstruction(
               instruction, actor, publicMessage, privateMessage
            )
      );
      ShowdownInterpreter.registerSplitInstructionParser(
         "-raidheal",
         (battle, actor, instruction, publicMessage, privateMessage, messageIterator) -> new RaidHealInstruction(actor, publicMessage, privateMessage)
      );
   }
}
