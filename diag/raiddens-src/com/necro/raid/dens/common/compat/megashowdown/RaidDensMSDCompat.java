package com.necro.raid.dens.common.compat.megashowdown;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.AspectPropertyType;
import com.cobblemon.mod.common.pokemon.properties.StringProperty;
import com.github.yajatkaul.mega_showdown.block.MegaShowdownBlocks;
import com.github.yajatkaul.mega_showdown.item.MegaShowdownItems;
import com.github.yajatkaul.mega_showdown.utils.GlowHandler;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.raid.RaidType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2248;

public abstract class RaidDensMSDCompat {
   public static void setupTera(Pokemon pokemon) {
      StringProperty property = AspectPropertyType.INSTANCE.fromString("msd:tera_" + pokemon.getTeraType().showdownId());
      if (!property.matches(pokemon)) {
         property.apply(pokemon);
         applyEffects(pokemon, "mega_showdown:tera_init_" + pokemon.getTeraType().showdownId().toLowerCase(), false);
         pokemon.getPersistentData().method_10556("is_tera", true);
      }
   }

   public static void setupDmax(PokemonEntity pokemonEntity, Pokemon pokemon) {
      StringProperty property = AspectPropertyType.INSTANCE.fromString("msd:dmax");
      if (!property.matches(pokemon)) {
         property.apply(pokemon);
         applyEffects(pokemon, "mega_showdown:dynamax", pokemon.getGmaxFactor());
         pokemon.getPersistentData().method_10556("is_max", true);

         try {
            GlowHandler.applyDynamaxGlow(pokemonEntity);
         } catch (NullPointerException var4) {
         }
      }
   }

   public static class_1799 getTeraShard(RaidType raidType) {
      return switch (raidType) {
         case FIGHTING -> ((class_1792)MegaShowdownItems.FIGHTING_TERA_SHARD.get()).method_7854();
         case FLYING -> ((class_1792)MegaShowdownItems.FLYING_TERA_SHARD.get()).method_7854();
         case POISON -> ((class_1792)MegaShowdownItems.POISON_TERA_SHARD.get()).method_7854();
         case GROUND -> ((class_1792)MegaShowdownItems.GROUND_TERA_SHARD.get()).method_7854();
         case ROCK -> ((class_1792)MegaShowdownItems.ROCK_TERA_SHARD.get()).method_7854();
         case BUG -> ((class_1792)MegaShowdownItems.BUG_TERA_SHARD.get()).method_7854();
         case GHOST -> ((class_1792)MegaShowdownItems.GHOST_TERA_SHARD.get()).method_7854();
         case STEEL -> ((class_1792)MegaShowdownItems.STEEL_TERA_SHARD.get()).method_7854();
         case FIRE -> ((class_1792)MegaShowdownItems.FIRE_TERA_SHARD.get()).method_7854();
         case WATER -> ((class_1792)MegaShowdownItems.WATER_TERA_SHARD.get()).method_7854();
         case GRASS -> ((class_1792)MegaShowdownItems.GRASS_TERA_SHARD.get()).method_7854();
         case ELECTRIC -> ((class_1792)MegaShowdownItems.ELECTRIC_TERA_SHARD.get()).method_7854();
         case PSYCHIC -> ((class_1792)MegaShowdownItems.PSYCHIC_TERA_SHARD.get()).method_7854();
         case ICE -> ((class_1792)MegaShowdownItems.ICE_TERA_SHARD.get()).method_7854();
         case DRAGON -> ((class_1792)MegaShowdownItems.DRAGON_TERA_SHARD.get()).method_7854();
         case DARK -> ((class_1792)MegaShowdownItems.DARK_TERA_SHARD.get()).method_7854();
         case FAIRY -> ((class_1792)MegaShowdownItems.FAIRY_TERA_SHARD.get()).method_7854();
         case STELLAR -> ((class_1792)MegaShowdownItems.STELLAR_TERA_SHARD.get()).method_7854();
         default -> ((class_1792)MegaShowdownItems.NORMAL_TERA_SHARD.get()).method_7854();
      };
   }

   public static class_1799 getMaxMushroom() {
      return ((class_2248)MegaShowdownBlocks.MAX_MUSHROOM.get()).method_8389().method_7854();
   }

   private static void applyEffects(Pokemon pokemon, String effectId, boolean isGmax) {
      try {
         Class<?> effect = getEffectClass();
         Method getEffect = effect.getMethod("getEffect", String.class);
         Object effectInstance = getEffect.invoke(null, effectId);
         runApplyEffects(effect, effectInstance, pokemon, isGmax);
      } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException var6) {
         CobblemonRaidDens.LOGGER.error("Error applying MSD Effect:", var6);
      }
   }

   private static Class<?> getEffectClass() throws ClassNotFoundException {
      try {
         return Class.forName("com.github.yajatkaul.mega_showdown.api.codec.Effect");
      } catch (ClassNotFoundException var1) {
         return Class.forName("com.github.yajatkaul.mega_showdown.codec.Effect");
      }
   }

   private static void runApplyEffects(Class<?> clazz, Object instance, Pokemon pokemon, boolean isGmax) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      try {
         Method method = clazz.getMethod("applyEffects", Pokemon.class, List.class, Optional.class, PokemonEntity.class);
         method.invoke(instance, pokemon, isGmax ? List.of("dynamax_form=gmax") : List.of(), Optional.empty(), null);
      } catch (NoSuchMethodException var6) {
         Method methodx = clazz.getMethod("applyEffects", Pokemon.class, List.class, PokemonEntity.class);
         methodx.invoke(instance, pokemon, isGmax ? List.of("dynamax_form=gmax") : List.of(), null);
      }
   }
}
