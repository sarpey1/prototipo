package com.gitlab.srcmc.rctapi.api.ai;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BattleSide;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.ShowdownMoveset.Gimmick;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.item.interactive.PotionType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.RCTApi;
import com.gitlab.srcmc.rctapi.api.ai.RCTBattleAI.1;
import com.gitlab.srcmc.rctapi.api.ai.config.RCTBattleAIConfig;
import com.gitlab.srcmc.rctapi.api.ai.config.RCTBattleAIConfig.Builder;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates;
import com.gitlab.srcmc.rctapi.api.ai.utils.Debug;
import com.gitlab.srcmc.rctapi.api.ai.utils.MoveType;
import com.gitlab.srcmc.rctapi.api.ai.utils.PokeMath;
import com.gitlab.srcmc.rctapi.api.ai.utils.ResponseBuilder;
import com.gitlab.srcmc.rctapi.api.ai.utils.TypeChart;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Custom;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Pokemon.Boost;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Pokemon.State;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Pokemon.Status;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Pokemon.Volatile;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates.ActorState;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates.BattleState;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates.PokemonState;
import com.gitlab.srcmc.rctapi.api.ai.utils.ResponseBuilder.Choice;
import com.gitlab.srcmc.rctapi.api.models.Gimmicks;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;
import com.gitlab.srcmc.rctapi.api.util.collection.WeightedCollection;
import java.util.List;
import java.util.Random;
import org.jetbrains.annotations.NotNull;

public class RCTBattleAI implements BattleAI {
   private double moveBias;
   private double statusMoveBias;
   private double switchBias;
   private double itemBias;
   private double maxSelectMargin;
   private Random rng = new Random();
   private static final int STATUS_WEIGHT = 8;
   private static final int BOOSTS_WEIGHT = 6;
   private static final int TYPING_WEIGHT = 48;
   private static final int HEALTH_WEIGHT = 6;
   private static final double STATUS_FACTOR = 0.5;
   private static final double BOOSTS_FACTOR = 0.5;
   private static final double DMAX_FACTOR = 0.25;
   private static final double MAX_TYPE_EFFECTIVENESS = 2.0;
   private static final double DEFAULT_SIG3 = 0.125;
   private static final double RATING_EXP = 1.25;

   public RCTBattleAI() {
      this(new Builder().build());
   }

   public RCTBattleAI(@NotNull RCTBattleAIConfig config) {
      this.moveBias = config.moveBias();
      this.statusMoveBias = config.statusMoveBias();
      this.switchBias = config.switchBias();
      this.itemBias = config.itemBias();
      this.maxSelectMargin = config.maxSelectMargin();
   }

   public ShowdownActionResponse choose(ActiveBattlePokemon pkmn, PokemonBattle battle, BattleSide aiSide, ShowdownMoveset moveset, boolean forceSwitch) {
      Debug.log(
         Debug.AI_CHOICES,
         () -> ModCommon.LOG
               .info(
                  String.format(
                     "[REQUEST for %s]: noCancel: %b, wait: %b, must: %b, fitForced: %b, sending: %d, responses: %d, active: %d, forceSwitch: [%s ]",
                     pkmn.getActor().getName().getString(),
                     pkmn.getActor().getRequest().getNoCancel(),
                     pkmn.getActor().getRequest().getWait(),
                     pkmn.getActor().getMustChoose(),
                     pkmn.getActor().canFitForcedAction(),
                     pkmn.getActor().getStillSendingOutCount(),
                     pkmn.getActor().getResponses() != null ? pkmn.getActor().getResponses().size() : 0,
                     pkmn.getActor().getRequest().getActive() != null ? pkmn.getActor().getRequest().getActive().size() : 0,
                     pkmn.getActor().getRequest().getForceSwitch() != null
                        ? pkmn.getActor().getRequest().getForceSwitch().stream().map(String::valueOf).reduce("", (a, b) -> a + " " + b)
                        : ""
                  )
               )
      );
      Debug.log(
         Debug.POKE_INFO,
         () -> {
            if (pkmn.hasPokemon()) {
               Pokemon ep = pkmn.getBattlePokemon().getEffectedPokemon();
               ModCommon.LOG
                  .info(
                     String.format(
                        "[POKE INFO, %s]:\n- species: %s\n- nickname: %s\n- gender: %s\n- ability: %s\n- nature: %s\n- moves: [%s]\n- EVs: [%s]\n- IVs: [%s]\n- heldItem: %s\n- aspects: [%s]\n- tera: %s\n- dynamax: %d\n- gmax: %b\n- shiny: %b",
                        pkmn.getActor().getName().getString(),
                        ep.getSpecies().resourceIdentifier.toString(),
                        String.valueOf(ep.getNickname()),
                        ep.getGender().name(),
                        ep.getAbility().getName(),
                        ep.getNature().getName().toString(),
                        ep.getMoveSet().getMoves().stream().map(m -> m.getName()).reduce((a, b) -> a + ", " + b).orElse(""),
                        Stats.getEntries().stream().map(s -> String.format("%s=%d", s.name(), ep.getEvs().get(s))).reduce((a, b) -> a + ", " + b).orElse(""),
                        Stats.getEntries().stream().map(s -> String.format("%s=%d", s.name(), ep.getIvs().get(s))).reduce((a, b) -> a + ", " + b).orElse(""),
                        String.valueOf(ep.getHeldItem$common()),
                        ep.getAspects().stream().reduce((a, b) -> a + ", " + b).orElse(""),
                        ep.getTeraType().getName(),
                        ep.getDmaxLevel(),
                        ep.getGmaxFactor(),
                        ep.getShiny()
                     )
                  );
            }
         }
      );
      Debug.log(
         Debug.BATTLE_CONTEXTS,
         () -> {
            if (pkmn.isAlive()) {
               BattleEffects.dump(pkmn.getBattlePokemon());
               pkmn.getActor()
                  .getSide()
                  .getOppositeSide()
                  .getActivePokemon()
                  .stream()
                  .filter(p -> p.isAlive())
                  .map(p -> p.getBattlePokemon())
                  .forEach(BattleEffects::dump);
            }
         }
      );
      BattleState battleState = BattleStates.get(pkmn.getBattle());
      ActorState actorState = battleState.getActorState(pkmn.getActor());
      if (!canAct(pkmn, moveset)) {
         return ResponseBuilder.pass(pkmn).response();
      } else {
         if (canUseGimmick(pkmn, moveset)
            && RCTApi.getInstances()
               .map(rct -> ((RCTApi)rct.getValue()).getTrainerRegistry().getByOT(pkmn.getBattlePokemon().getEffectedPokemon()))
               .filter(t -> t != null && t instanceof TrainerNPC)
               .findFirst()
               .orElse(null) instanceof TrainerNPC trainer) {
            PokemonState pkmnState = battleState.getPokemonState(pkmn.getBattlePokemon());
            Gimmicks gimmicks = trainer.getGimmicks().of(pkmn.getBattlePokemon().getOriginalPokemon());
            moveset.setCanDynamax(
               ModCommon.modLoader().isLoaded("mega_showdown")
                  && gimmicks.dynamax()
                  && !actorState.hasGimmick(Gimmick.DYNAMAX.getId())
                  && !pkmnState.has(Custom.MEGA)
                  && !pkmnState.has(Custom.TERA)
                  && !moveset.getCanUltraBurst()
                  && !moveset.getCanMegaEvo()
                  && moveset.getCanZMove() == null
            );
            moveset.setCanTerastallize(
               gimmicks.tera() != null
                     && !actorState.hasGimmick(Gimmick.TERASTALLIZATION.getId())
                     && !actorState.hasGimmick(Gimmick.MEGA_EVOLUTION.getId())
                     && !pkmnState.has(Custom.MEGA)
                     && !pkmnState.has(Custom.ZMOVE)
                     && !pkmnState.has(Custom.DYNAMAX)
                     && !moveset.getCanDynamax()
                     && !moveset.getCanUltraBurst()
                     && !moveset.getCanMegaEvo()
                     && moveset.getCanZMove() == null
                  ? gimmicks.tera()
                  : null
            );
            if (moveset.getCanDynamax()
               || moveset.getCanTerastallize() != null
               || moveset.getCanMegaEvo()
               || moveset.getCanZMove() != null
               || moveset.getCanUltraBurst()) {
               for (ShowdownMoveset other : actorState.getSelectedMoveSets(battle)) {
                  if (moveset.getCanTerastallize() != null && other.getCanTerastallize() != null) {
                     moveset.setCanTerastallize(null);
                  }

                  if (moveset.getCanDynamax() && other.getCanDynamax()) {
                     moveset.setCanDynamax(false);
                  }

                  if (moveset.getCanMegaEvo() && other.getCanMegaEvo()) {
                     moveset.setCanMegaEvo(false);
                  }

                  if ((moveset.getCanZMove() != null || moveset.getCanUltraBurst()) && (other.getCanZMove() != null || other.getCanUltraBurst())) {
                     moveset.setCanZMove(null);
                     moveset.setCanUltraBurst(false);
                  }
               }
            }

            if (!moveset.getCanDynamax()) {
               moveset.setMaxMoves(null);
            }
         }

         ResponseBuilder builder = ResponseBuilder.create(pkmn, moveset, forceSwitch).margin(this.rng.nextDouble(this.maxSelectMargin)).random(this.rng);
         if (canUseMove(pkmn, moveset)) {
            actorState.addSelectedMoveSet(battle, moveset);
            builder.suggestMoves(
               candidates -> candidates.filter(pair -> {
                        if (pair.second instanceof ActiveBattlePokemon targetPkmn && !targetPkmn.isAlive()) {
                           return false;
                        }

                        return true;
                     })
                     .map(
                        pair -> pair.second instanceof ActiveBattlePokemon targetPkmn
                              ? new Choice(
                                 String.format("MOVE %s -> %s", ((InBattleMove)pair.first).id, targetPkmn.getBattlePokemon().getName().getString()),
                                 pair,
                                 1.0 - this.evalMove(pkmn.getBattlePokemon(), targetPkmn.getBattlePokemon(), (InBattleMove)pair.first)
                              )
                              : new Choice(
                                 String.format("MOVE %s -> <multi/none>", ((InBattleMove)pair.first).id),
                                 pair,
                                 1.0 - this.evalMove(pkmn.getBattlePokemon(), null, (InBattleMove)pair.first)
                              )
                     )
            );
         }

         if (canUseItem(pkmn, moveset)) {
            builder.suggestItems(
               candidates -> candidates.map(
                     pair -> new Choice(
                           String.format("ITEM %s -> %s", ((BagItem)pair.first).getItemName(), ((BattlePokemon)pair.second).getName().getString()),
                           pair,
                           1.0 - this.evalItem((BagItem)pair.first, (BattlePokemon)pair.second)
                        )
                  )
            );
         }

         if ((forceSwitch || canSwitch(pkmn, moveset)) && (forceSwitch || !pkmn.hasPokemon() || !State.trapped(pkmn.getBattlePokemon()))) {
            builder.suggestSwitches(
               candidates -> candidates.map(
                     bp -> new Choice(
                           String.format("SWITCH %s -> %s", pkmn.isAlive() ? pkmn.getBattlePokemon().getName().getString() : "<dead>", bp.getName().getString()),
                           bp,
                           1.0 - this.evalSwitch(pkmn, bp)
                        )
                  )
            );
         }

         return builder.response();
      }
   }

   private static boolean canAct(ActiveBattlePokemon pkmn, ShowdownMoveset moveset) {
      return !pkmn.hasPokemon()
         || !pkmn.getBattlePokemon().getEffectedPokemon().getAbility().getName().equals("commander")
         || pkmn.getSidePokemon()
            .stream()
            .filter(p -> p != pkmn && p.hasPokemon())
            .noneMatch(p -> p.getBattlePokemon().getEffectedPokemon().getSpecies().showdownId().equals("dondozo"));
   }

   private static boolean canUseGimmick(ActiveBattlePokemon pkmn, ShowdownMoveset moveset) {
      return canUseMove(pkmn, moveset);
   }

   private static boolean canUseMove(ActiveBattlePokemon pkmn, ShowdownMoveset moveset) {
      return moveset != null && pkmn.hasPokemon();
   }

   private static boolean canUseItem(ActiveBattlePokemon pkmn, ShowdownMoveset moveset) {
      return pkmn.hasPokemon();
   }

   private static boolean canSwitch(ActiveBattlePokemon pkmn, ShowdownMoveset moveset) {
      return true;
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private double evalMove(BattlePokemon from, BattlePokemon to, InBattleMove move) {
      MoveType mt = MoveType.of(move);
      if (to == null) {
         List<ActiveBattlePokemon> all = mt != MoveType.HEAL && mt != MoveType.CURE && mt != MoveType.BUFF
            ? from.getActor().getSide().getOppositeSide().getActivePokemon()
            : from.getActor().getSide().getActivePokemon();
         return all.stream()
            .filter(ActiveBattlePokemon::hasPokemon)
            .map(pkmn -> this.evalMove(from, pkmn.getBattlePokemon(), move))
            .max(Double::compare)
            .orElse(0.0);
      } else {
         boolean ally = from.actor.getSide().equals(to.actor.getSide());

         double e = switch (1.$SwitchMap$com$gitlab$srcmc$rctapi$api$ai$utils$MoveType[mt.ordinal()]) {
            case 1 -> ally
            ? Math.max(this.rngSin() * this.maxSelectMargin, 1.0 - (double)to.getHealth() / (double)to.getMaxHealth()) * this.statusMoveBias
            : 0.0;
            case 2 -> ally ? (Status.any(to) ? this.rngSin() : this.rngSin() * this.maxSelectMargin) * this.statusMoveBias : 0.0;
            case 3 -> {
               double e = ally ? (1.0 - this.rngSin()) * Math.min(1.0, Math.max(0.0, this.rngSin() - Boost.avg(to) * 5.0)) * this.statusMoveBias : 0.0;
               double h = (double)from.getHealth() / (double)from.getMaxHealth();
               yield e * h * h;
            }
            case 4 -> {
               double e = !ally ? (1.0 - this.rngSin()) * Math.min(1.0, Math.max(0.0, this.rngSin() + Boost.avg(to) * 5.0)) * this.statusMoveBias : 0.0;
               double h = (double)to.getHealth() / (double)to.getMaxHealth();
               yield e * h * h;
            }
            case 5 -> {
               double e = ally
                  ? 0.0
                  : (!Status.any(to) && !Volatile.any(to) ? 1.0 - this.rngSin() : this.rngSin() * this.maxSelectMargin) * this.statusMoveBias;
               int used = BattleStates.get(from.getActor().getBattle()).getPokemonState(from).age(Custom.USED_STATUS);
               int targets = ally ? from.getActor().getActivePokemon().size() : to.getActor().getActivePokemon().size();
               double r = 1.0 - Math.min(1.0, (double)used / (double)targets) * this.rngSin();
               yield e * r;
            }
            case 6 -> {
               double dmg = (double)Math.min(to.getHealth(), PokeMath.damage(from, to, move)) / (double)to.getHealth();
               yield !ally ? Math.min(1.0, dmg + (1.0 - this.rngSin()) * this.maxSelectMargin) * this.moveBias : 0.0;
            }
            default -> this.rng.nextDouble();
         };
         Pokemon fromEP = BattleStates.getTransformationOrEffected(from);
         Pokemon toEp = BattleStates.getTransformationOrEffected(to);
         double health = (double)from.getHealth() / (double)from.getMaxHealth();
         MoveTemplate mvtmp = Moves.getByName(move.id);
         boolean faster = ally || fromEP.getSpeed() >= toEp.getSpeed() || mvtmp != null && mvtmp.getPriority() > 0;
         double speedFactor = faster ? 1.0 : this.rngRange(health, 1.0, true);
         return e == 0.0 ? -1.0 : MoveType.eval(from, to, move.id) * MoveType.eval(from, to, mt) * speedFactor * e;
      }
   }

   private double evalItem(BagItem item, BattlePokemon to) {
      if (item instanceof PotionType potion) {
         int amount = potion == PotionType.POTION
            ? Math.min(20, to.getMaxHealth())
            : (
               potion == PotionType.SUPER_POTION
                  ? Math.min(50, to.getMaxHealth())
                  : (
                     potion == PotionType.HYPER_POTION
                        ? Math.min(200, to.getMaxHealth())
                        : (potion == PotionType.MAX_POTION ? to.getMaxHealth() : (potion == PotionType.FULL_RESTORE ? to.getMaxHealth() : 0))
                  )
            );
         double turnFactor = to.isSentOut()
            ? this.gaussNorm(Math.min(1.0, (double)BattleStates.get(to.getActor().battle).getPokemonState(to).age(Custom.TURN) / 3.0))
            : 1.0;
         double estHeal = (double)(Math.min(to.getMaxHealth(), to.getHealth() + amount) - to.getHealth()) / (double)amount;
         return turnFactor
            * Math.min(
               1.0,
               (double)amount
                  / (double)to.getMaxHealth()
                  * (1.0 - (double)to.getHealth() / (double)to.getMaxHealth())
                  * Math.min(1.0, estHeal * (to.isSentOut() ? 1.0 : 0.75) * (Status.any(to) && potion.getCuresStatus() ? 1.25 : 1.0))
                  * this.itemBias
            );
      } else {
         return 0.0;
      }
   }

   private double evalSwitch(ActiveBattlePokemon from, BattlePokemon to) {
      WeightedCollection<Double> deltas = new WeightedCollection();
      double bsd = this.gaussNorm(from.hasPokemon() ? Math.max(0.0, Math.min(1.0, 0.5 - Boost.avg(from.getBattlePokemon()))) : 0.0, 0.125);
      deltas.add(bsd, 6);
      double sad = this.gaussNorm(!from.hasPokemon() || !Status.any(from.getBattlePokemon()) && !Volatile.any(from.getBattlePokemon()) ? 0.0 : 0.5, 0.125);
      deltas.add(sad, 8);
      Pokemon toEP = BattleStates.getTransformationOrEffected(to);
      int[] oppStatsAll = new int[]{0};
      int[] oppCount = new int[]{0};
      to.getActor().getSide().getOppositeSide().getActivePokemon().stream().filter(ActiveBattlePokemon::hasPokemon).forEach(pkmn -> {
         double effFromOpp = from.hasPokemon() ? Math.min(1.0, TypeChart.getAverageEffectiveness(from.getBattlePokemon(), pkmn.getBattlePokemon()) / 2.0) : 0.0;
         double effOppFrom = from.hasPokemon() ? Math.min(1.0, TypeChart.getAverageEffectiveness(pkmn.getBattlePokemon(), from.getBattlePokemon()) / 2.0) : 0.0;
         double effToOpp = Math.min(1.0, TypeChart.getAverageEffectiveness(to, pkmn.getBattlePokemon()) / 2.0);
         double effOppTo = Math.min(1.0, TypeChart.getAverageEffectiveness(pkmn.getBattlePokemon(), to) / 2.0);
         double effFrom = from.hasPokemon() ? (effFromOpp + (1.0 - effOppFrom)) / 2.0 : 0.0;
         double effTo = (effToOpp + (1.0 - effOppTo)) / 2.0;
         double efd = this.gaussClamp(this.sinPI_2(effTo - effFrom), 0.125, -1.0, 1.0);
         deltas.add(efd, 48);
         if (pkmn.hasPokemon()) {
            Pokemon oppEP = BattleStates.getTransformationOrEffected(pkmn.getBattlePokemon());
            oppStatsAll[0] += oppEP.getAttack() + oppEP.getDefence() + oppEP.getSpecialAttack() + oppEP.getSpecialDefence() + oppEP.getSpeed();
            oppCount[0]++;
         }
      });
      int toStatsAll = toEP.getAttack() + toEP.getDefence() + toEP.getSpecialAttack() + toEP.getSpecialDefence() + toEP.getSpeed();
      double toStatsAvg = (double)toStatsAll / 5.0;
      double oppStatsAvg = oppCount[0] > 0 ? (double)oppStatsAll[0] / ((double)oppCount[0] * 5.0) : 0.0;
      double statsFactor = oppStatsAvg > 0.0 ? toStatsAvg / oppStatsAvg : 1.0;
      double fhCur = (double)(from.hasPokemon() ? from.getBattlePokemon().getHealth() : 0);
      double fhMax = (double)(from.hasPokemon() ? from.getBattlePokemon().getMaxHealth() : 1);
      double fhRel = fhCur / fhMax;
      double thRel = (double)to.getHealth() / (double)to.getMaxHealth();
      deltas.add(this.gaussClamp(thRel - fhRel, 0.125, -1.0, 1.0), 6);
      double agef = from.hasPokemon()
            && from.getBattle().getTurn() != 0
            && BattleStates.get(from.getBattle()).getPokemonState(from.getBattlePokemon()).age(Custom.TURN) <= 1
         ? this.gaussNorm()
         : 1.0;
      double dmf = from.hasPokemon() && BattleStates.get(from.getBattle()).getPokemonState(from.getBattlePokemon()).has(Custom.DYNAMAX)
         ? this.rngRange(Math.pow(0.25, 2.0), 0.25)
         : 1.0;
      double delta = Math.max(0.0, deltas.getWeightedSum()) / (double)deltas.getTotalWeight();
      double deltaBiased = this.gaussClamp(delta, this.switchBias / 2.0, delta, 1.0);
      double rating = Math.min(1.0, deltaBiased * agef * dmf * statsFactor);
      return this.rngRange(Math.pow(rating, 1.25), rating);
   }

   private double sinPI_2(double value) {
      return org.joml.Math.sin(value * (Math.PI / 2));
   }

   private double rngSin() {
      return this.sinPI_2(this.rng.nextDouble() * (Math.PI / 2));
   }

   private double rngRange(double min, double max) {
      return this.rngRange(min, max, false);
   }

   private double rngRange(double min, double max, boolean optimistic) {
      double range = max - min;
      return min + (optimistic ? this.rngSin() * range : (1.0 - this.rngSin()) * range);
   }

   private double gaussNorm() {
      return this.gaussNorm(0.5);
   }

   private double gaussNorm(double mean) {
      return this.gaussNorm(mean, 1.0);
   }

   private double gaussNorm(double mean, double sig3) {
      return this.gaussClamp(mean, sig3, 0.0, 1.0);
   }

   private double gaussClamp(double mean, double sig3, double min, double max) {
      return Math.clamp(this.rng.nextGaussian(mean, sig3 / 3.0), min, max);
   }
}
