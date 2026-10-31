#!/usr/bin/env bash
# Diagnosi Mega illimitate con le versioni ESATTE di Cobbleverse. Non fallisce mai: pubblica tutto in diag/.
set -u
CV=${COBBLEVERSE_VERSION:-1.7.42}
python3 ci/cobbleverse.py "$CV" cvmods > diag/cobbleverse.log 2>&1 || true
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

# ---- server reale con Cobblemon + Mega Showdown di Cobbleverse + Mega Radar, avviato DUE volte
D=itest; rm -rf $D; mkdir -p $D/mods
echo "eula=true" > $D/eula.txt
printf 'online-mode=false\nlevel-seed=12345\n' > $D/server.properties
curl -sSL -o $D/server.jar "https://meta.fabricmc.net/v2/versions/loader/1.21.1/0.17.3/1.0.3/server/jar"
# solo le mod necessarie, alle versioni di Cobbleverse
for pat in "Cobblemon-fabric" "mega_showdown" "architectury" "accessories-fabric" "accessories_compat_layer" "owo-lib" "fabric-api" "fabric-language-kotlin-1.13"; do
  cp cvmods/${pat}*.jar $D/mods/ 2>/dev/null
done
for f in build/libs/*.jar; do case "$f" in *-sources.jar) ;; *) cp "$f" $D/mods/ ;; esac; done
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
