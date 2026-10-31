package com.github.yajatkaul.mega_showdown.gimmick;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.player.GeneralPlayerData;
import com.cobblemon.mod.common.battles.ShowdownMoveset.Gimmick;
import com.github.yajatkaul.mega_showdown.config.MegaShowdownConfig;
import com.github.yajatkaul.mega_showdown.gimmick.GimmickTurnCheck.1;
import com.github.yajatkaul.mega_showdown.tag.MegaShowdownTags.Blocks;
import com.github.yajatkaul.mega_showdown.tag.MegaShowdownTags.Items;
import com.github.yajatkaul.mega_showdown.utils.AccessoriesUtils;
import com.github.yajatkaul.mega_showdown.utils.PlayerUtils;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_3222;

public class GimmickTurnCheck {
   public static void check(class_3222 player) {
      GeneralPlayerData data = Cobblemon.INSTANCE.getPlayerDataManager().getGenericData(player);
      if (!PlayerUtils.isBlockNearby(player, Blocks.POWER_SPOT, MegaShowdownConfig.powerSpotRange) && !MegaShowdownConfig.dynamaxAnywhere) {
         data.getKeyItems().remove(class_2960.method_60655("cobblemon", "dynamax_band"));
      } else if (hasGimmick(Gimmick.DYNAMAX, player)) {
         data.getKeyItems().add(class_2960.method_60655("cobblemon", "dynamax_band"));
      } else {
         data.getKeyItems().remove(class_2960.method_60655("cobblemon", "dynamax_band"));
      }

      if (hasGimmick(Gimmick.TERASTALLIZATION, player)) {
         data.getKeyItems().add(class_2960.method_60655("cobblemon", "tera_orb"));
      } else {
         data.getKeyItems().remove(class_2960.method_60655("cobblemon", "tera_orb"));
      }

      if (hasGimmick(Gimmick.MEGA_EVOLUTION, player)) {
         data.getKeyItems().add(class_2960.method_60655("cobblemon", "key_stone"));
      } else {
         data.getKeyItems().remove(class_2960.method_60655("cobblemon", "key_stone"));
      }

      if (hasGimmick(Gimmick.Z_POWER, player)) {
         data.getKeyItems().add(class_2960.method_60655("cobblemon", "z_ring"));
      } else {
         data.getKeyItems().remove(class_2960.method_60655("cobblemon", "z_ring"));
      }
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static boolean hasGimmick(Gimmick gimmick, class_1309 user) {
      boolean hasOmniRing = AccessoriesUtils.checkTagInAccessories(user, Items.OMNI_RING);
      switch (1.$SwitchMap$com$cobblemon$mod$common$battles$ShowdownMoveset$Gimmick[gimmick.ordinal()]) {
         case 1:
            if (!MegaShowdownConfig.dynamax) {
               return false;
            } else {
               if (hasOmniRing) {
                  return true;
               }

               return AccessoriesUtils.checkTagInAccessories(user, Items.DYNAMAX_BAND);
            }
         case 2:
            if (!MegaShowdownConfig.teralization) {
               return false;
            } else if (hasOmniRing) {
               return true;
            } else {
               class_1799 teraOrb = AccessoriesUtils.findFirstItemWithTag(user, Items.TERA_ORB);
               if (user instanceof class_3222 player) {
                  boolean hasTerapagos = PlayerUtils.hasPokemon(player, "Terapagos");
                  if (teraOrb.method_7960()) {
                     return false;
                  }

                  if (hasTerapagos) {
                     teraOrb.method_7974(0);
                  }

                  return teraOrb.method_7919() < 100;
               }

               return teraOrb != class_1799.field_8037;
            }
         case 3:
            if (!MegaShowdownConfig.zMoves) {
               return false;
            } else {
               if (hasOmniRing) {
                  return true;
               }

               return AccessoriesUtils.checkTagInAccessories(user, Items.Z_RING);
            }
         case 4:
            if (!MegaShowdownConfig.mega) {
               return false;
            } else {
               boolean hasMegaAccess = AccessoriesUtils.checkTagInAccessories(user, Items.MEGA_BRACELET);
               if (user instanceof class_3222 player) {
                  return (hasMegaAccess || hasOmniRing) && !MegaGimmick.hasMega(player);
               }

               return hasMegaAccess || hasOmniRing;
            }
         default:
            return false;
      }
   }
}
