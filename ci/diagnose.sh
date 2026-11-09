#!/usr/bin/env bash
# Diagnosi Mega illimitate con le versioni ESATTE di Cobbleverse. Non fallisce mai: pubblica tutto in diag/.
set -u
CV=${COBBLEVERSE_VERSION:-1.7.42}
ALL_MODS=1 python3 ci/cobbleverse.py "$CV" cvmods > diag/cobbleverse.log 2>&1 || true
cp versions.txt diag/ 2>/dev/null || true
ls -la cvmods > diag/cvmods.txt 2>&1 || true

# ---- decompilazione delle classi che decidono il tasto Mega
curl -sSL -o vineflower.jar https://repo1.maven.org/maven2/org/vineflower/vineflower/1.10.1/vineflower-1.10.1.jar
COB=$(ls cvmods/*obblemon*fabric*.jar cvmods/*obblemon*.jar 2>/dev/null | grep -v -i mega | head -1)
MSD=$(ls cvmods/*mega*showdown*.jar 2>/dev/null | head -1)
echo "Cobblemon: $COB / MSD: $MSD" >> diag/versions.txt
decompile() {  # jar, regex, outdir
  rm -rf cls && mkdir cls
  (cd cls && unzip -q -o "../$1" $(unzip -Z1 "../$1" | grep -E "$2" | tr '\n' ' ') 2>/dev/null)
  mkdir -p "$3"
  java -jar vineflower.jar -dgs=1 cls "$3" > /dev/null 2>&1 || true
}
if [ -n "$COB" ]; then
  unzip -Z1 "$COB" | grep -iE "gimmick|ShowdownActionRequest|ShowdownMoveset|runner/graal|BattleMoveSelection|KeyItem|keyitem" > diag/cobblemon-classes.txt
  decompile "$COB" "(gimmick|Gimmick|ShowdownActionRequest|ShowdownMoveset|runner/graal/|BattleMoveSelection).*\.class$" diag/cobblemon-src
  unzip -Z1 "$COB" | grep -iE "showdown.*\.(zip|js)$|^showdown" | head -40 > diag/cobblemon-showdown-files.txt
fi
if [ -n "$MSD" ]; then
  decompile "$MSD" "(ShowdownPatcher|GraalShowdownUnbundlerMixin|GimmickTurnCheck|MegaGimmick|ShowdownActionRequestMixin|MegaShowdownConfig)\.class$" diag/msd-src
  unzip -p "$MSD" assets/mega_showdown/showdown/battle-actions.js | grep -n "runMegaEvo(pokemon)" -A25 > diag/msd-runMegaEvo.txt 2>&1
fi

# ---- cerca "chip localizzatore" nei file di lingua di tutte le mod
for J in allmods/*.jar; do
  for L in $(unzip -Z1 "$J" 2>/dev/null | grep -E "lang/(it_it|en_us)\.json$"); do
    unzip -p "$J" "$L" 2>/dev/null | grep -iE "localizzatore|locator chip|locator_chip|tracking chip" | sed "s#^#$(basename $J) $L: #"
  done
done > diag/chip-search.txt 2>&1
bash ci/chip-test.sh || true
# ---- TUTTE le mod del pack: chi tocca i file del motore Showdown?
for J in allmods/*.jar; do
  hits=$(unzip -Z1 "$J" 2>/dev/null | grep -iE "showdown/(sim|data)/|battle-actions" | head -5)
  rm -rf y && mkdir y && (cd y && unzip -q -o "../$J" '*.class' 2>/dev/null)
  cls=$(grep -rlaE "battle-actions\.js|showdown/sim|runMegaEvo|copyInternalToExternal|attemptUnbundle|GraalShowdownUnbundler" y 2>/dev/null | head -5 | sed 's#^y/##')
  [ -n "$hits$cls" ] && { echo "== $(basename $J)"; echo "$hits"; echo "$cls"; }
done > diag/allmods-showdown-touchers.txt 2>&1
ls allmods | wc -l >> diag/allmods-showdown-touchers.txt
# ---- chi tocca la Mega lato Java/Kotlin in TUTTE le mod (escluse le classi di Cobblemon stesso)
mkdir -p diag/mega-touchers
for J in allmods/*.jar; do
  rm -rf y && mkdir y && (cd y && unzip -q -o "../$J" '*.class' '*.mixins.json' '*mixins*.json' 2>/dev/null)
  m=$(grep -rlaE "canMegaEvo|blockGimmick|MEGA_EVOLUTION|setCanMegaEvo|getCanMegaEvo|key_stone" y 2>/dev/null | grep -v "^y/com/cobblemon/mod/common/" | sed 's#^y/##')
  if [ -n "$m" ]; then
    n=$(basename "$J" .jar); echo "== $n"; echo "$m"
    rm -rf cls && mkdir cls && for c in $m; do case "$c" in *.class) mkdir -p "cls/$(dirname $c)"; cp "y/$c" "cls/$c";; esac; done
    java -jar vineflower.jar -dgs=1 cls "diag/mega-touchers/$n" > /dev/null 2>&1 || true
  fi
done > diag/mega-touchers.txt 2>&1
# tutti i mixin di Mega Showdown 1.8.4 + deserializzazione delle richieste in Cobblemon
M=$(ls allmods/mega_showdown*.jar | head -1)
unzip -p "$M" $(unzip -Z1 "$M" | grep -E "mixins\.json$") > diag/msd-mixins.json 2>/dev/null
rm -rf cls && mkdir cls && (cd cls && unzip -q -o "../$M" $(unzip -Z1 "../$M" | grep -E "/mixin/.*\.class$" | tr '\n' ' '))
java -jar vineflower.jar -dgs=1 cls diag/msd-mixins-src > /dev/null 2>&1 || true
rm -rf cls && mkdir cls && (cd cls && unzip -q -o "../$COB" $(unzip -Z1 "../$COB" | grep -E "(ShowdownMovesetAdapter|ShowdownActionRequest|BattleMoveSelection|BattleActionSelection|PlayerBattleActor|BattleActor|ShowdownInterpreter|MegaEvolution|BattleMegaEvolution).*\.class$" | tr '\n' ' '))
java -jar vineflower.jar -dgs=1 cls diag/cobblemon-src2 > /dev/null 2>&1 || true
# ---- Raid Dens: decompila tutto ciò che riguarda showdown
RD=$(ls allmods/cobblemonraiddens*.jar | head -1)
if [ -n "$RD" ]; then
  rm -rf cls && mkdir cls && (cd cls && unzip -q -o "../$RD" $(unzip -Z1 "../$RD" | grep -iE "showdown.*\.class$|mixins/.*Graal.*\.class$|RaidBattle.*\.class$" | tr '\n' ' '))
  java -jar vineflower.jar -dgs=1 cls diag/raiddens-src > /dev/null 2>&1 || true
  unzip -Z1 "$RD" | grep -iE "showdown|\.js$|mixins\.json" > diag/raiddens-files.txt
  mkdir -p diag/raiddens-res && for r in $(unzip -Z1 "$RD" | grep -iE "\.js$|mixins\.json$"); do unzip -p "$RD" "$r" > "diag/raiddens-res/$(echo $r | tr / _)"; done
fi
# ---- mod di Cobbleverse che toccano le battaglie: cerca riferimenti a Mega / gimmick / showdown
for J in cvmods/rctapi*.jar cvmods/rctmod*.jar cvmods/zamega*.jar cvmods/cobblemon-battle-extras*.jar; do
  [ -f "$J" ] || continue
  n=$(basename "$J" .jar); mkdir -p "diag/mods/$n"
  unzip -Z1 "$J" > "diag/mods/$n/files.txt"
  unzip -Z1 "$J" | grep -iE "\.(js|json|mixins\.json)$" | grep -iE "showdown|mega|gimmick|battle|mixin" | head -80 > "diag/mods/$n/interesting-files.txt"
  rm -rf x && mkdir x && (cd x && unzip -q -o "../$J")
  grep -rlaiE "canMegaEvo|megaEvo|MEGA_EVOLUTION|blockGimmick|key_stone|keyItem|runMegaEvo|hasMegaEvolved|showdown/sim|battle-actions" x > "diag/mods/$n/matches.txt" 2>/dev/null
  # decompila le classi che corrispondono
  rm -rf cls && mkdir cls && for c in $(grep "\.class$" "diag/mods/$n/matches.txt"); do mkdir -p "cls/$(dirname ${c#x/})"; cp "$c" "cls/${c#x/}"; done
  java -jar vineflower.jar -dgs=1 cls "diag/mods/$n/src" > /dev/null 2>&1 || true
  for r in $(grep -E "\.(js|json)$" "diag/mods/$n/matches.txt"); do mkdir -p "diag/mods/$n/res"; cp "$r" "diag/mods/$n/res/$(echo ${r#x/} | tr / _)"; done
done
# ---- server reale con Cobblemon + Mega Showdown di Cobbleverse + Mega Radar, avviato DUE volte
D=itest; rm -rf $D; mkdir -p $D/mods
echo "eula=true" > $D/eula.txt
printf 'online-mode=false\nlevel-seed=12345\n' > $D/server.properties
curl -sSL -o $D/server.jar "https://meta.fabricmc.net/v2/versions/loader/1.21.1/0.17.3/1.0.3/server/jar"
# solo le mod necessarie, alle versioni di Cobbleverse
for pat in "Cobblemon-fabric" "mega_showdown" "architectury" "accessories-fabric" "accessories_compat_layer" "owo-lib" "fabric-api" "fabric-language-kotlin-1.13" "cobblemonraiddens" "geckolib" "cobblemon-additions" "zamega"; do
  cp allmods/${pat}*.jar $D/mods/ 2>/dev/null
done
JAR=$(ls build/libs/*.jar | grep -v sources | head -1); cp "$JAR" $D/mods/ && cp "$JAR" diag/
ls $D/mods > diag/server-mods.txt
for run in 1 2; do
  rm -f $D/console && mkfifo $D/console
  (cd $D && timeout 300 java -Xmx3G -jar server.jar nogui < console > ../diag/server-run$run.log 2>&1) &
  pid=$!
  exec 3>$D/console
  for i in $(seq 1 50); do grep -q "Done (" diag/server-run$run.log 2>/dev/null && break; sleep 5; done
  sleep 5
  mkdir -p $D/config && cp ci/probe-scenarios.js $D/config/megaradar_probe.js
  echo "megaradar megatest" >&3
  sleep 5
  echo "megaradar megaeval" >&3
  sleep 10
  echo "megaradar megastate" >&3
  sleep 5
  { echo "== run $run"; ls -la --time-style=full-iso $D/showdown/sim 2>&1 | grep battle-actions;
    echo "-- marker:"; grep -c "megaradar:unlimited-megas" $D/showdown/sim/battle-actions.js; } >> diag/showdown-state.txt 2>&1
  echo "stop" >&3
  exec 3>&-
  wait $pid || true
done
# classi trasformate dai mixin: ordine delle iniezioni in attemptUnbundle
U=$(find $D/.mixin.out -name "GraalShowdownUnbundler.class" 2>/dev/null | head -1)
[ -n "$U" ] && javap -c -p "$U" | grep -n "attemptUnbundle\|invokevirtual\|invokestatic\|invokespecial" | grep -iE "attemptUnbundle|handler|megaradar|mega_showdown|beforeShowdown|CallbackInfo|\$" > diag/unbundler-injections.txt 2>&1
find $D/.mixin.out -type f | head -50 > diag/mixin-out-files.txt 2>&1
grep -E "megaradar|Mega illimitate|Showdown|showdown|Mixin|mixin|fallback|All files" diag/server-run*.log | grep -v "^\s*at " > diag/server-relevant.txt
cp -r $D/showdown diag-showdown 2>/dev/null || true

# file del motore utili per leggere il formato delle squadre di Cobblemon
mkdir -p diag/engine
for f in sim/pokemon.js sim/teams.js sim/side.js sim/battle-stream.js index.js sim/cobblemon/cobblemon.js; do
  [ -f "diag-showdown/$f" ] && cp "diag-showdown/$f" "diag/engine/$(echo $f | tr / _)"
done
ls -R diag-showdown/sim/cobblemon diag-showdown/data/mods 2>/dev/null | head -40 > diag/engine/listing.txt
# ---- battaglia sul motore REALE (cartella showdown generata dal server)
if [ -d diag-showdown/sim ]; then
  node ci/real-engine-test.js diag-showdown > diag/real-engine-test.txt 2>&1 || true
  # stesso motore, ripristinato all'originale di Mega Showdown e poi modificato dal nostro patcher
  JAR=$(ls build/libs/*.jar | grep -v sources | head -1)
  cp -r diag-showdown engine-orig && unzip -p cvmods/mega_showdown*.jar assets/mega_showdown/showdown/battle-actions.js > engine-orig/sim/battle-actions.js
  node ci/real-engine-test.js engine-orig > diag/real-engine-orig.txt 2>&1 || true
  cp -r engine-orig engine-patched
  mkdir -p pt && (cd pt && unzip -q -o "../$JAR" 'com/megaradar/compat/*')
  printf 'public class P { public static void main(String[] a) throws Exception { System.out.println(com.megaradar.compat.ShowdownMegaPatch.apply(java.nio.file.Path.of(a[0]), true)); } }' > pt/P.java
  (cd pt && javac -cp . P.java 2>&1 | tail -2; java -cp . P ../engine-patched/sim/battle-actions.js) > diag/patcher-run.txt 2>&1
  node ci/real-engine-test.js engine-patched > diag/real-engine-patched.txt 2>&1 || true
fi
echo done
