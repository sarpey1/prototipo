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

# ---- TUTTE le mod del pack: chi tocca i file del motore Showdown?
for J in allmods/*.jar; do
  hits=$(unzip -Z1 "$J" 2>/dev/null | grep -iE "showdown/(sim|data)/|battle-actions" | head -5)
  rm -rf y && mkdir y && (cd y && unzip -q -o "../$J" '*.class' 2>/dev/null)
  cls=$(grep -rlaE "battle-actions\.js|showdown/sim|runMegaEvo|copyInternalToExternal|attemptUnbundle|GraalShowdownUnbundler" y 2>/dev/null | head -5 | sed 's#^y/##')
  [ -n "$hits$cls" ] && { echo "== $(basename $J)"; echo "$hits"; echo "$cls"; }
done > diag/allmods-showdown-touchers.txt 2>&1
ls allmods | wc -l >> diag/allmods-showdown-touchers.txt
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
for pat in "Cobblemon-fabric" "mega_showdown" "architectury" "accessories-fabric" "accessories_compat_layer" "owo-lib" "fabric-api" "fabric-language-kotlin-1.13" "cobblemonraiddens" "geckolib" "cobblemon-additions"; do
  cp allmods/${pat}*.jar $D/mods/ 2>/dev/null
done
JAR=$(ls build/libs/*.jar | grep -v sources | head -1); cp "$JAR" $D/mods/ && cp "$JAR" diag/
ls $D/mods > diag/server-mods.txt
for run in 1 2; do
  (cd $D && timeout 240 java -Xmx3G -Dmixin.debug.export=true -jar server.jar nogui > ../diag/server-run$run.log 2>&1 <<< "stop") &
  pid=$!
  for i in $(seq 1 44); do grep -q "Done (" diag/server-run$run.log 2>/dev/null && break; sleep 5; done
  sleep 15
  { echo "== run $run"; ls -la --time-style=full-iso $D/showdown $D/showdown/sim 2>&1 | head -60;
    echo "-- marker:"; grep -c "megaradar:unlimited-megas" $D/showdown/sim/battle-actions.js;
    grep -n "runMegaEvo(pokemon)" -A14 $D/showdown/sim/battle-actions.js | head -40; } >> diag/showdown-state.txt 2>&1
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
