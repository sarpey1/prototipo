package com.github.yajatkaul.mega_showdown.event;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.drop.ItemDropEntry;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent;
import com.cobblemon.mod.common.api.events.battles.instruction.FormeChangeEvent;
import com.cobblemon.mod.common.api.events.battles.instruction.MegaEvolutionEvent;
import com.cobblemon.mod.common.api.events.battles.instruction.TerastallizationEvent;
import com.cobblemon.mod.common.api.events.battles.instruction.ZMoveUsedEvent;
import com.cobblemon.mod.common.api.events.drops.LootDroppedEvent;
import com.cobblemon.mod.common.api.events.pokeball.ThrownPokeballHitEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonSentEvent.Post;
import com.cobblemon.mod.common.api.events.pokemon.RidePokemonEvent.Pre;
import com.cobblemon.mod.common.api.events.pokemon.healing.PokemonHealedEvent;
import com.cobblemon.mod.common.api.item.HealingSource.Force;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.types.tera.TeraTypes;
import com.cobblemon.mod.common.battles.dispatch.UntilDispatch;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.AspectPropertyType;
import com.cobblemon.mod.common.pokemon.properties.UnaspectPropertyType;
import com.github.yajatkaul.mega_showdown.MegaShowdown;
import com.github.yajatkaul.mega_showdown.advancement.AdvancementHelper;
import com.github.yajatkaul.mega_showdown.api.codec.BattleFormChange;
import com.github.yajatkaul.mega_showdown.api.codec.Effect;
import com.github.yajatkaul.mega_showdown.api.codec.HeldItemFormChange;
import com.github.yajatkaul.mega_showdown.api.codec.ZCrystal;
import com.github.yajatkaul.mega_showdown.api.event.DynamaxEndCallback;
import com.github.yajatkaul.mega_showdown.api.event.DynamaxStartCallback;
import com.github.yajatkaul.mega_showdown.api.event.UltraBurstCallback;
import com.github.yajatkaul.mega_showdown.config.MegaShowdownConfig;
import com.github.yajatkaul.mega_showdown.datapack.MegaShowdownDatapackRegister;
import com.github.yajatkaul.mega_showdown.gimmick.MaxGimmick;
import com.github.yajatkaul.mega_showdown.gimmick.MegaGimmick;
import com.github.yajatkaul.mega_showdown.gimmick.UltraGimmick;
import com.github.yajatkaul.mega_showdown.item.MegaShowdownItems;
import com.github.yajatkaul.mega_showdown.item.custom.form_change.FormChangeHeldItem;
import com.github.yajatkaul.mega_showdown.sound.MegaShowdownSounds;
import com.github.yajatkaul.mega_showdown.tag.MegaShowdownTags.Items;
import com.github.yajatkaul.mega_showdown.utils.AccessoriesUtils;
import com.github.yajatkaul.mega_showdown.utils.AspectUtils;
import com.github.yajatkaul.mega_showdown.utils.GlowHandler;
import com.github.yajatkaul.mega_showdown.utils.PlayerUtils;
import com.github.yajatkaul.mega_showdown.utils.RegistryLocator;
import com.github.yajatkaul.mega_showdown.utils.TeraHelper;
import com.github.yajatkaul.mega_showdown.utils.AspectUtils.EffectPair;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import kotlin.Unit;
import net.minecraft.class_1542;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_3222;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_7923;

public class CobbleEvents {
   public static void register() {
      CobblemonEvents.HELD_ITEM_PRE.subscribe(Priority.NORMAL, CobbleEvents::heldItemChange);
      CobblemonEvents.MEGA_EVOLUTION.subscribe(Priority.NORMAL, CobbleEvents::megaEvolution);
      CobblemonEvents.BATTLE_STARTED_PRE.subscribe(Priority.NORMAL, CobbleEvents::hookBattlePre);
      CobblemonEvents.BATTLE_STARTED_POST.subscribe(Priority.NORMAL, CobbleEvents::hookBattleStarted);
      CobblemonEvents.TERASTALLIZATION.subscribe(Priority.NORMAL, CobbleEvents::terrastallizationUsed);
      CobblemonEvents.POKEMON_HEALED.subscribe(Priority.NORMAL, CobbleEvents::healedPokemons);
      CobblemonEvents.ZPOWER_USED.subscribe(Priority.NORMAL, CobbleEvents::zMovesUsed);
      CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.NORMAL, CobbleEvents::devolveFainted);
      CobblemonEvents.POKEMON_SENT_POST.subscribe(Priority.NORMAL, CobbleEvents::pokemonSent);
      CobblemonEvents.THROWN_POKEBALL_HIT.subscribe(Priority.NORMAL, CobbleEvents::pokeballHit);
      CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, CobbleEvents::fixTera);
      CobblemonEvents.LOOT_DROPPED.subscribe(Priority.NORMAL, CobbleEvents::dropShardPokemon);
      CobblemonEvents.FORME_CHANGE.subscribe(Priority.NORMAL, CobbleEvents::formChanged);
      CobblemonEvents.RIDE_EVENT_PRE.subscribe(Priority.NORMAL, CobbleEvents::ridePre);
      DynamaxStartCallback.EVENT.register(CobbleEvents::dynamaxStarted);
      DynamaxEndCallback.EVENT.register(CobbleEvents::dynamaxEnded);
      UltraBurstCallback.EVENT.register(CobbleEvents::ultraBurst);
   }

   private static void ridePre(Pre event) {
      if (event.getPokemon().getPokemon().getPersistentData().method_10577("form_changing")) {
         event.cancel();
      }
   }

   private static void formChanged(FormeChangeEvent formeChangeEvent) {
      Pokemon pokemon = formeChangeEvent.getPokemon().getEffectedPokemon();
      if (pokemon.getSpecies().getName().equals("Greninja") && formeChangeEvent.getFormeName().equals("ash")) {
         AdvancementHelper.grantAdvancement(pokemon.getOwnerPlayer(), "bond/ash_greninja");
      } else if (pokemon.getSpecies().getName().equals("Greninja") && pokemon.getAspects().contains("bond")) {
         AdvancementHelper.grantAdvancement(pokemon.getOwnerPlayer(), "bond/ash_battle_bond");
      }

      BattlePokemon battlePokemon = formeChangeEvent.getPokemon();
      String formId = formeChangeEvent.getFormeName();
      if (MegaShowdownConfig.debugMode) {
         MegaShowdown.LOGGER.info(formId);
      }

      for (BattleFormChange battleFormChange : MegaShowdownDatapackRegister.BATTLE_FORM_CHANGE_REGISTRY) {
         if (formeChangeEvent.getFormeName().equals(battleFormChange.showdownFormChangeId())
            && battleFormChange.pokemons().contains(pokemon.getSpecies().getName())
            && battleFormChange.aspects().validate_apply(pokemon)) {
            Effect.getEffect(battleFormChange.effect())
               .applyEffectsBattle(
                  pokemon,
                  battleFormChange.aspects().aspectApply().aspects(),
                  battleFormChange.aspects().aspectApply().pokemonProperties(),
                  null,
                  battlePokemon
               );
            AspectUtils.appendRevertDataPokemon(
               Effect.getEffect(battleFormChange.effect()),
               battleFormChange.aspects().aspectRevert().aspects(),
               battleFormChange.aspects().aspectRevert().pokemonProperties(),
               pokemon,
               "battle_end_revert"
            );
            return;
         }
      }
   }

   private static void dropShardPokemon(LootDroppedEvent event) {
      if (MegaShowdownConfig.teralization) {
         if (event.getEntity() instanceof PokemonEntity pokemonEntity) {
            Pokemon pokemon = pokemonEntity.getPokemon();
            class_1792 correspondingTeraShard = TeraHelper.getTeraShardForType(pokemon.getPrimaryType());
            ItemDropEntry teraShardDropEntry = new ItemDropEntry();
            teraShardDropEntry.setItem(class_7923.field_41178.method_10221(correspondingTeraShard));
            Random random = new Random();
            boolean otherSuccess = MegaShowdownConfig.teraShardDropRate > 0.0 && random.nextDouble() < MegaShowdownConfig.teraShardDropRate / 100.0;
            boolean stellarSuccess = MegaShowdownConfig.stellarShardDropRate > 0.0 && random.nextDouble() < MegaShowdownConfig.stellarShardDropRate / 100.0;
            if (otherSuccess) {
               class_1799 stack = new class_1799(correspondingTeraShard);
               class_1542 itemEntity = new class_1542(
                  pokemonEntity.method_37908(), pokemonEntity.method_23317(), pokemonEntity.method_23318(), pokemonEntity.method_23321(), stack
               );
               pokemonEntity.method_37908().method_8649(itemEntity);
            } else if (stellarSuccess) {
               teraShardDropEntry.setItem(class_7923.field_41178.method_10221((class_1792)MegaShowdownItems.STELLAR_TERA_SHARD.get()));
               event.getDrops().add(teraShardDropEntry);
            }
         }
      }
   }

   private static void fixTera(PokemonCapturedEvent event) {
      Pokemon pokemon = event.getPokemon();
      if (pokemon.getSpecies().getName().equals("Ogerpon")) {
         if (pokemon.getAspects().contains("teal-mask")) {
            pokemon.setTeraType(TeraTypes.getGRASS());
         } else if (pokemon.getAspects().contains("wellspring-mask")) {
            pokemon.setTeraType(TeraTypes.getWATER());
         } else if (pokemon.getAspects().contains("hearthflame-mask")) {
            pokemon.setTeraType(TeraTypes.getFIRE());
         } else if (pokemon.getAspects().contains("cornerstone-mask")) {
            pokemon.setTeraType(TeraTypes.getROCK());
         } else {
            pokemon.setTeraType(TeraTypes.forElementalType(pokemon.getPrimaryType()));
         }
      } else if (pokemon.getSpecies().getName().equals("Terapagos")) {
         pokemon.setTeraType(TeraTypes.getSTELLAR());
      }
   }

   private static void pokeballHit(ThrownPokeballHitEvent event) {
      if (event.getPokemon().getAspects().contains("core-percent")) {
         event.cancel();
      }
   }

   private static void pokemonSent(Post event) {
      PokemonEntity pokemon = event.getPokemonEntity();
      if (pokemon.getPokemon().getPersistentData().method_10577("is_tera") && MegaShowdownConfig.legacyTeraEffect) {
         GlowHandler.applyTeraGlow(pokemon, "msd:tera_" + pokemon.getPokemon().getTeraType().showdownId());
      }
   }

   private static void devolveFainted(BattleFaintedEvent event) {
      Pokemon pokemon = event.getKilled().getEffectedPokemon();
      if (pokemon.getPersistentData().method_10545("battle_end_revert")) {
         for (EffectPair effectPair : AspectUtils.getRevertDataPokemon(pokemon, "battle_end_revert")) {
            effectPair.effect().revertEffects(pokemon, effectPair.aspects(), effectPair.pokemonProperties(), null);
         }

         pokemon.getPersistentData().method_10551("battle_end_revert");
      }

      if (pokemon.getPersistentData().method_10577("is_tera")) {
         pokemon.getAspects().stream().filter(a -> a.startsWith("msd:tera_")).forEach(name -> UnaspectPropertyType.INSTANCE.fromString(name).apply(pokemon));
         if (pokemon.getEntity() != null && MegaShowdownConfig.legacyTeraEffect) {
            pokemon.getEntity().method_5834(false);
         }

         pokemon.getPersistentData().method_10551("is_tera");
      }

      if (pokemon.getPersistentData().method_10577("is_max")) {
         if (pokemon.getAspects().contains("gmax")) {
            Effect.getEffect("mega_showdown:dynamax").revertEffectsBattle(pokemon, List.of("dynamax_form=none"), Optional.empty(), null, event.getKilled());
         } else {
            UnaspectPropertyType.INSTANCE.fromString("msd:dmax").apply(pokemon);
            Effect.getEffect("mega_showdown:dynamax").revertEffectsBattle(pokemon, List.of(), Optional.empty(), null, event.getKilled());
         }

         MaxGimmick.startGradualScalingDown(pokemon);
         pokemon.getPersistentData().method_10556("is_max", false);
      }
   }

   private static void ultraBurst(PokemonBattle battle, BattlePokemon battlePokemon) {
      Pokemon pokemon = battlePokemon.getEntity().getPokemon();
      UltraGimmick.ultraBurstInBattle(pokemon, battlePokemon);
   }

   private static void dynamaxEnded(PokemonBattle battle, BattlePokemon battlePokemon) {
      Pokemon pokemon = battlePokemon.getEffectedPokemon();
      MaxGimmick.startGradualScalingDown(pokemon);
      if (battlePokemon.getEntity().getPokemon().getAspects().contains("gmax")) {
         Effect.getEffect("mega_showdown:dynamax").revertEffectsBattle(pokemon, List.of("dynamax_form=none"), Optional.empty(), null, battlePokemon);
      } else {
         UnaspectPropertyType.INSTANCE.fromString("msd:dmax").apply(pokemon);
         Effect.getEffect("mega_showdown:dynamax").revertEffectsBattle(pokemon, List.of(), Optional.empty(), null, battlePokemon);
      }
   }

   private static void dynamaxStarted(PokemonBattle battle, BattlePokemon battlePokemon, Boolean gmax) {
      Pokemon pokemon = battlePokemon.getEffectedPokemon();
      AdvancementHelper.grantAdvancement(pokemon.getOwnerPlayer(), "dynamax/dynamax");
      if (gmax) {
         AdvancementHelper.grantAdvancement(pokemon.getOwnerPlayer(), "dynamax/gigantamax");
         Effect.getEffect("mega_showdown:dynamax").applyEffectsBattle(pokemon, List.of("dynamax_form=gmax"), Optional.empty(), null, battlePokemon);
         AspectUtils.appendRevertDataPokemon(Effect.empty(), List.of("dynamax_form=none"), Optional.empty(), pokemon, "battle_end_revert");
         battle.dispatchToFront(() -> new UntilDispatch(() -> true));
      } else {
         AspectPropertyType.INSTANCE.fromString("msd:dmax").apply(pokemon);
         Effect.getEffect("mega_showdown:dynamax").applyEffectsBattle(pokemon, List.of(), Optional.empty(), null, battlePokemon);
      }

      pokemon.getPersistentData().method_10556("is_max", true);
      PokemonEntity pokemonEntity = pokemon.getEntity();
      if (pokemonEntity != null) {
         MaxGimmick.startGradualScaling(pokemon, MegaShowdownConfig.dynamaxScaleFactor);
         class_2338 entityPos = pokemon.getEntity().method_23312();
         pokemonEntity.method_37908()
            .method_43128(
               null,
               (double)entityPos.method_10263(),
               (double)entityPos.method_10264(),
               (double)entityPos.method_10260(),
               (class_3414)MegaShowdownSounds.DYNAMAX.get(),
               class_3419.field_15248,
               0.2F,
               0.8F
            );
      }
   }

   private static void zMovesUsed(ZMoveUsedEvent event) {
      PokemonEntity pokemonEntity = event.getPokemon().getEffectedPokemon().getEntity();
      Pokemon pokemon = pokemonEntity.getPokemon();
      AdvancementHelper.grantAdvancement(pokemon.getOwnerPlayer(), "z/z_moves");
      if (pokemon.getSpecies().getName().equals("Pikachu") && pokemon.getAspects().contains("partner-cap")) {
         AdvancementHelper.grantAdvancement(pokemon.getOwnerPlayer(), "bond/ash_pikachu");
      }

      GlowHandler.applyZGlow(pokemonEntity, (ZCrystal)RegistryLocator.getComponent(ZCrystal.class, pokemon.heldItem()));
      Effect.getEffect("mega_showdown:z_move").applyEffectsBattle(pokemon, List.of(), Optional.empty(), null, event.getPokemon());
   }

   private static void healedPokemons(PokemonHealedEvent pokemonHealedEvent) {
      class_3222 player = pokemonHealedEvent.getPokemon().getOwnerPlayer();
      if (player != null && pokemonHealedEvent.getSource() == Force.INSTANCE) {
         class_1799 teraOrb = AccessoriesUtils.findFirstItemWithTag(player, Items.TERA_ORB);
         if (teraOrb != class_1799.field_8037) {
            teraOrb.method_7974(0);
         }
      }
   }

   private static void terrastallizationUsed(TerastallizationEvent event) {
      PokemonEntity pokemonEntity = event.getPokemon().getEffectedPokemon().getEntity();
      Pokemon pokemon = pokemonEntity.getPokemon();
      Effect.getEffect("mega_showdown:tera_init_" + pokemon.getTeraType().showdownId().toLowerCase(Locale.ROOT))
         .applyEffectsBattle(pokemon, List.of(), Optional.empty(), null, event.getPokemon());
      pokemonEntity.after(1.5F, () -> {
         AspectPropertyType.INSTANCE.fromString("msd:tera_" + pokemon.getTeraType().showdownId()).apply(pokemon);
         return Unit.INSTANCE;
      });
      AdvancementHelper.grantAdvancement(pokemon.getOwnerPlayer(), "tera/terastallized");
      AspectPropertyType.INSTANCE.fromString("play_tera").apply(pokemon);
      pokemonEntity.after(3.0F, () -> {
         UnaspectPropertyType.INSTANCE.fromString("play_tera").apply(pokemon);
         return Unit.INSTANCE;
      });
      pokemon.getPersistentData().method_10556("is_tera", true);
      if (MegaShowdownConfig.legacyTeraEffect) {
         GlowHandler.applyTeraGlow(pokemonEntity, "msd:tera_" + pokemon.getTeraType().showdownId());
      }

      class_3222 player = pokemon.getOwnerPlayer();
      if (!PlayerUtils.hasPokemon(player, "Terapagos")) {
         class_1799 teraOrb = AccessoriesUtils.findFirstItemWithTag(player, Items.TERA_ORB);
         if (teraOrb != class_1799.field_8037) {
            teraOrb.method_7974(teraOrb.method_7919() + 10);
         }
      }

      event.getBattle().dispatchWaitingToFront(3.5F, () -> Unit.INSTANCE);
   }

   private static void hookBattlePre(com.cobblemon.mod.common.api.events.battles.BattleStartedEvent.Pre event) {
      AtomicBoolean cancelled = new AtomicBoolean(false);
      event.getBattle().getActivePokemon().forEach(pkmn -> {
         if (pkmn.getBattlePokemon().getEffectedPokemon().getAspects().contains("core-percent")) {
            event.cancel();
            cancelled.set(true);
         }
      });
      if (!cancelled.get()) {
         event.getBattle().getPlayers().forEach(serverPlayer -> {
            PlayerPartyStore playerPartyStore = Cobblemon.INSTANCE.getStorage().getParty(serverPlayer);
            AspectUtils.revertPokemonsIfRequiredBattleStart(playerPartyStore);
         });
      }
   }

   private static void hookBattleStarted(com.cobblemon.mod.common.api.events.battles.BattleStartedEvent.Post event) {
      if (MegaShowdownConfig.debugMode) {
         event.getBattle().setMute(false);
      }

      event.getBattle().getOnEndHandlers().add(battle -> {
         battle.getPlayers().forEach(AspectUtils::revertPokemonsIfRequiredBattleEnd);
         return Unit.INSTANCE;
      });
   }

   private static void megaEvolution(MegaEvolutionEvent event) {
      AdvancementHelper.grantAdvancement(event.getPokemon().getOriginalPokemon().getOwnerPlayer(), "mega/mega_evolve");
      Pokemon pokemon = event.getPokemon().getEntity().getPokemon();
      MegaGimmick.megaEvolveInBattle(pokemon, event.getPokemon());
   }

   private static void heldItemChange(com.cobblemon.mod.common.api.events.pokemon.HeldItemEvent.Pre event) {
      Pokemon pokemon = event.getPokemon();
      class_1799 itemReceiving = event.getReceiving();
      class_1799 itemReturning = event.getReturning();
      if (itemReturning.method_7909() != event.getReceiving().method_7909()) {
         if (itemReturning.method_7909() instanceof FormChangeHeldItem formChangeItem) {
            formChangeItem.revert(pokemon);
         }

         if (itemReceiving.method_7909() instanceof FormChangeHeldItem formChangeItem) {
            formChangeItem.apply(pokemon);
         }

         HeldItemFormChange heldItemFormChangeReturning = (HeldItemFormChange)RegistryLocator.getComponent(HeldItemFormChange.class, itemReturning);
         if (heldItemFormChangeReturning != null) {
            heldItemFormChangeReturning.revert(pokemon);
         }

         HeldItemFormChange heldItemFormChangeReceiving = (HeldItemFormChange)RegistryLocator.getComponent(HeldItemFormChange.class, itemReceiving);
         if (heldItemFormChangeReceiving != null) {
            heldItemFormChangeReceiving.apply(pokemon);
         }

         MegaGimmick megaGimmick = (MegaGimmick)RegistryLocator.getComponent(MegaGimmick.class, itemReturning);
         if (megaGimmick != null && megaGimmick.pokemons().contains(pokemon.getSpecies().getName()) && MegaGimmick.isMega(pokemon)) {
            MegaGimmick.megaToggle(pokemon);
         }

         if (pokemon.getSpecies().getName().equals("Necrozma") && pokemon.getAspects().contains("ultra-fusion")) {
            ZCrystal zCrystal = (ZCrystal)RegistryLocator.getComponent(ZCrystal.class, itemReturning);
            if (zCrystal != null && zCrystal.showdown_item_id().equals("ultranecroziumz")) {
               UltraGimmick.ultraBurstToggle(pokemon);
            }
         }
      }
   }
}
