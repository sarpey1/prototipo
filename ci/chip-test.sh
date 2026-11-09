#!/usr/bin/env bash
# Server con Cobblemon + LumyMon + mod della ricetta: la ricetta deve caricarsi senza errori.
set -u
D=chiptest; rm -rf $D; mkdir -p $D/mods
echo "eula=true" > $D/eula.txt; printf 'online-mode=false\nlevel-seed=12345\n' > $D/server.properties
curl -sSL -o $D/server.jar "https://meta.fabricmc.net/v2/versions/loader/1.21.1/0.17.3/1.0.3/server/jar"
for pat in "Cobblemon-fabric" "architectury" "fabric-api" "fabric-language-kotlin-1.13" "LumyMon" "geckolib" "owo-lib" "accessories-fabric" "accessories_compat_layer"; do cp allmods/${pat}*.jar $D/mods/ 2>/dev/null; done
cp ci/chip/*.jar $D/mods/
L=$(ls allmods/LumyMon*.jar | head -1); unzip -p "$L" fabric.mod.json > diag/lumymon-fabric.mod.json 2>/dev/null
unzip -Z1 "$L" | grep -iE "recipe.*locator|locator.*recipe" > diag/lumymon-locator-recipes.txt
for r in $(cat diag/lumymon-locator-recipes.txt); do echo "== $r"; unzip -p "$L" "$r"; done >> diag/lumymon-locator-recipes.txt
ls $D/mods > diag/chip-server-mods.txt
(cd $D && timeout 240 java -Xmx3G -jar server.jar nogui > ../diag/chip-server.log 2>&1 <<< "") &
pid=$!
for i in $(seq 1 44); do grep -qE "Done \(|Incompatible|crash" diag/chip-server.log 2>/dev/null && break; sleep 5; done
sleep 5; kill $pid 2>/dev/null; wait $pid 2>/dev/null
grep -nE "Done \(|lumychiprecipe|locator_chip|Parsing error|Incompatible|requires|Couldn't parse" diag/chip-server.log | head -30 > diag/chip-result.txt
