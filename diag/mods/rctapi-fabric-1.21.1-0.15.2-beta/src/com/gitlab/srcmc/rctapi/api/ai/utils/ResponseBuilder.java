package com.gitlab.srcmc.rctapi.api.ai.utils;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BagItemActionResponse;
import com.cobblemon.mod.common.battles.DefaultActionResponse;
import com.cobblemon.mod.common.battles.ForcePassActionResponse;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.MoveActionResponse;
import com.cobblemon.mod.common.battles.PassActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionRequest;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.SwitchActionResponse;
import com.cobblemon.mod.common.battles.Targetable;
import com.cobblemon.mod.common.battles.ShowdownMoveset.Gimmick;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Custom;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates.ActorState;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates.BattleState;
import com.gitlab.srcmc.rctapi.api.ai.utils.ResponseBuilder.Choice;
import com.gitlab.srcmc.rctapi.api.ai.utils.ResponseBuilder.Pair;
import com.gitlab.srcmc.rctapi.api.battle.BattleManager.TrainerEntityBattleActor;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.class_1802;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class ResponseBuilder {
   private Supplier<Stream<BattlePokemon>> switchCandidates = Stream::empty;
   private Supplier<Stream<Pair<BagItem, BattlePokemon>>> itemCandidates = Stream::empty;
   private Supplier<Stream<Pair<InBattleMove, Targetable>>> moveCandidates = Stream::empty;
   private List<Choice<ShowdownActionResponse>> choices = new ArrayList<>();
   private Random rng = new Random(0L);
   private ActiveBattlePokemon pkmn;
   private ShowdownMoveset moveset;
   private boolean forceSwitch;
   private boolean forceMove;
   private boolean mustChoose;
   private double margin;
   private ShowdownActionResponse response;
   private static final int MAX_CHOICE_RNG = 16;

   public static ResponseBuilder create(ActiveBattlePokemon pkmn, ShowdownMoveset moveset, boolean forceSwitch) {
      ResponseBuilder builder = new ResponseBuilder();
      ShowdownActionRequest request = pkmn.getActor().getRequest();
      builder.mustChoose = pkmn.getActor().getMustChoose();
      builder.forceSwitch = forceSwitch;
      builder.forceMove = false;
      builder.moveset = moveset;
      builder.pkmn = pkmn;
      if (!request.getWait() && (forceSwitch || !request.getForceSwitch().contains(true))) {
         if (!builder.forceSwitch && pkmn.hasPokemon()) {
            selectMoves(pkmn, builder);
            if (!builder.forceMove) {
               selectItemUsages(pkmn, builder);
            }
         }

         if (!builder.forceMove) {
            selectSwitches(pkmn, builder);
         }
      }

      return builder;
   }

   public static ResponseBuilder pass(ActiveBattlePokemon pkmn) {
      ResponseBuilder builder = new ResponseBuilder();
      builder.pkmn = pkmn;
      builder.response = PassActionResponse.INSTANCE;
      return builder;
   }

   private static void selectMoves(ActiveBattlePokemon pkmn, ResponseBuilder builder) {
      if (builder.moveset != null && builder.moveset.moves.stream().findFirst().isPresent()) {
         Stream<InBattleMove> stream;
         if (builder.moveset.moves.stream().anyMatch(InBattleMove::mustBeUsed)) {
            stream = builder.moveset.moves.stream().filter(InBattleMove::mustBeUsed);
            builder.forceMove = true;
         } else {
            stream = builder.moveset.moves.stream().filter(InBattleMove::canBeUsed);
         }

         builder.moveCandidates = () -> stream.flatMap(
               mv -> mv.getTargets(pkmn) != null && !mv.getTargets(pkmn).isEmpty()
                     ? mv.getTargets(pkmn).stream().map(t -> new Pair(mv, t))
                     : Stream.of(new Pair(mv, (Targetable)null))
            );
      }
   }

   private static void selectSwitches(ActiveBattlePokemon pkmn, ResponseBuilder builder) {
      builder.switchCandidates = () -> pkmn.getActor().getPokemonList().stream().filter(p -> p.canBeSentOut());
   }

   private static void selectItemUsages(ActiveBattlePokemon pkmn, ResponseBuilder builder) {
      if (pkmn.getActor().canFitForcedAction() && pkmn.getActor() instanceof TrainerEntityBattleActor actor) {
         builder.itemCandidates = () -> actor.getBag()
               .getItems()
               .stream()
               .flatMap(
                  bi -> pkmn.getActor()
                        .getPokemonList()
                        .stream()
                        .filter(
                           p -> bi.canUse(
                                 class_7923.field_41178.method_17966(class_2960.method_60654(bi.getItemName())).orElse(class_1802.field_8162).method_7854(),
                                 pkmn.getBattle(),
                                 p
                              )
                        )
                        .map(p -> new Pair(bi, p))
               );
      }
   }

   public ResponseBuilder suggestSwitches(Function<Stream<BattlePokemon>, Stream<Choice<BattlePokemon>>> consumer) {
      consumer.apply(this.switchCandidates.get()).forEach(choice -> {
         SwitchActionResponse response = new SwitchActionResponse(((BattlePokemon)choice.value).getUuid());
         this.choices.add(new Choice(choice.name, response, choice.weight, () -> {
            ((BattlePokemon)choice.value).setWillBeSwitchedIn(true);
            if (this.pkmn.hasPokemon()) {
               this.pkmn.getBattlePokemon().setWillBeSwitchedIn(false);
            }
         }));
      });
      return this;
   }

   public ResponseBuilder suggestItems(Function<Stream<Pair<BagItem, BattlePokemon>>, Stream<Choice<Pair<BagItem, BattlePokemon>>>> consumer) {
      if (this.pkmn.getActor() instanceof TrainerEntityBattleActor actor) {
         consumer.apply(this.itemCandidates.get()).forEach(choice -> {
            ForcePassActionResponse response = new ForcePassActionResponse();
            this.choices.add(new Choice(choice.name, response, choice.weight, () -> {
               BagItem item = (BagItem)((Pair)choice.value).first;
               BattlePokemon pkmn = (BattlePokemon)((Pair)choice.value).second;
               this.pkmn.getActor().forceChoose(new BagItemActionResponse(actor.getBag().use(item), pkmn, pkmn.getUuid().toString()));
            }, true));
         });
      }

      return this;
   }

   public ResponseBuilder suggestMoves(Function<Stream<Pair<InBattleMove, Targetable>>, Stream<Choice<Pair<InBattleMove, Targetable>>>> consumer) {
      Gimmick gimmick = this.moveset != null && this.moveset.getGimmicks().size() > 0
         ? (Gimmick)this.moveset.getGimmicks().get(this.rng.nextInt(this.moveset.getGimmicks().size()))
         : null;
      Debug.log(Debug.AI_CHOICES, () -> {
         ModCommon.LOG.info("[GIMMICKS of " + (this.pkmn.hasPokemon() ? this.pkmn.getBattlePokemon().getName().getString() : "<dead>") + "]:");
         if (this.moveset != null) {
            this.moveset.getGimmicks().forEach(g -> ModCommon.LOG.info(" - " + g.getId() + ", " + g.name()));
         }

         if (gimmick != null) {
            ModCommon.LOG.info("ACTIVATED: " + gimmick.getId());
         }
      });
      BattleState battleState = BattleStates.get(this.pkmn.getBattle());
      ActorState actorSt = battleState.getActorState(this.pkmn.getActor());
      consumer.apply(this.moveCandidates.get())
         .forEach(
            choice -> {
               InBattleMove move = (InBattleMove)((Pair)choice.value).first;
               MoveTemplate moveInstance = Moves.getByName(move.id);
               Targetable target = (Targetable)((Pair)choice.value).second;
               boolean dynamaxed = this.pkmn.hasPokemon()
                  && BattleStates.get(this.pkmn.getBattle()).getPokemonState(this.pkmn.getBattlePokemon()).has(Custom.DYNAMAX);
               String gimmickId = dynamaxed
                  ? Gimmick.DYNAMAX.getId()
                  : (moveInstance == null || gimmick == null || Gimmick.Z_POWER.equals(gimmick) && move.getGimmickMove() == null ? null : gimmick.getId());
               boolean max = Gimmick.DYNAMAX.getId().equals(gimmickId);
               boolean status = moveInstance == null ? false : Moves.getByName(move.id).getDamageCategory().equals(DamageCategories.INSTANCE.getSTATUS());
               MoveActionResponse response = new MoveActionResponse(move.id, (!max || !status) && target != null ? target.getPNX() : null, gimmickId);
               this.choices.add(new Choice(choice.name, response, choice.weight, () -> {
                  if (this.pkmn.hasPokemon() && gimmickId != null) {
                     if (Gimmick.DYNAMAX.getId().equals(gimmickId)) {
                        if (!dynamaxed) {
                           battleState.getPokemonState(this.pkmn.getBattlePokemon()).add(Custom.DYNAMAX);
                        }
                     } else if (Gimmick.MEGA_EVOLUTION.getId().equals(gimmickId)) {
                        battleState.getPokemonState(this.pkmn.getBattlePokemon()).add(Custom.MEGA);
                     } else if (Gimmick.TERASTALLIZATION.getId().equals(gimmickId)) {
                        battleState.getPokemonState(this.pkmn.getBattlePokemon()).add(Custom.TERA);
                     } else if (Gimmick.Z_POWER.getId().equals(gimmickId) || Gimmick.ULTRA_BURST.getId().equals(gimmickId)) {
                        battleState.getPokemonState(this.pkmn.getBattlePokemon()).add(Custom.ZMOVE);
                     }

                     actorSt.addGimmick(gimmickId);
                  }
               }));
            }
         );
      return this;
   }

   public ShowdownActionResponse response(Consumer<ShowdownActionResponse> consumer) {
      Debug.log(
         Debug.AI_CHOICES,
         "[CHOICES OF %s]:%s",
         new Object[]{
            this.pkmn.isAlive() ? this.pkmn.getBattlePokemon().getName().getString() : "<dead>",
            ", forceMove: " + this.forceMove + ", forceSwitch: " + this.forceSwitch + ", mustChoose: " + this.mustChoose
         }
      );
      Debug.log(
         Debug.AI_CHOICES,
         () -> this.choices
               .stream()
               .sorted()
               .forEach(
                  c -> ModCommon.LOG
                        .info(
                           String.format(
                              " - %s: %.4f, forced: %b, valid: %b",
                              c.name,
                              c.weight,
                              c.forced,
                              ((ShowdownActionResponse)c.value).isValid(this.pkmn, this.moveset, this.forceSwitch)
                           )
                        )
               )
      );
      List<Choice<ShowdownActionResponse>> choices = this.choices
         .stream()
         .filter(c -> c.forced || ((ShowdownActionResponse)c.value).isValid(this.pkmn, this.moveset, this.forceSwitch))
         .toList();
      ShowdownActionResponse response = (ShowdownActionResponse)(this.response != null
         ? this.response
         : (
            choices.isEmpty()
               ? (this.forceMove && this.mustChoose && this.pkmn.hasPokemon() ? new DefaultActionResponse() : PassActionResponse.INSTANCE)
               : (ShowdownActionResponse)getRandom(takeWithMargin(choices.stream().sorted(), this.margin), this.rng, this.margin)
                  .orElse(new Choice("DEFAULT", new DefaultActionResponse(), 0.0))
                  .pick()
                  .value
         ));
      consumer.accept(response);
      return response;
   }

   public ShowdownActionResponse response() {
      return this.response(r -> Debug.log(Debug.AI_CHOICES, "=> RESPONSE: " + r.toString() + ", " + r.toShowdownString(this.pkmn, this.moveset), new Object[0]));
   }

   public ResponseBuilder margin(double margin) {
      this.margin = margin;
      return this;
   }

   public ResponseBuilder random(Random rng) {
      this.rng = rng;
      return this;
   }

   private static <T> Stream<Choice<T>> takeWithMargin(Stream<Choice<T>> in, double margin) {
      double[] w = new double[]{Double.NEGATIVE_INFINITY};
      return in.takeWhile(choice -> {
         if (w[0] == Double.NEGATIVE_INFINITY) {
            w[0] = choice.weight;
         } else if (choice.weight - w[0] > margin) {
            return false;
         }

         return true;
      });
   }

   private static <T> Optional<Choice<T>> getRandom(Stream<Choice<T>> stream, Random rng, double margin) {
      Iterator<Choice<T>> it = stream.iterator();
      Choice<T> c;
      if (it.hasNext()) {
         Choice<T> next = it.next();
         double start = next.weight;
         double w = 0.0;
         int i = 1;
         c = next;

         while (it.hasNext()) {
            next = it.next();
            w = margin > 0.0 ? (next.weight - start) / margin : 1.0;
            if (rng.nextInt(++i + (int)(w * 16.0)) == 0) {
               c = next;
            }
         }
      } else {
         c = null;
      }

      return Optional.ofNullable(c);
   }
}
