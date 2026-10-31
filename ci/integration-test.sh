#!/usr/bin/env bash
# Server Fabric "di produzione" con Cobblemon + Mega Showdown + Mega Radar (come in Cobbleverse).
set -u
MC=1.21.1
LOADER=$(grep '^loader_version' gradle.properties | cut -d= -f2)
FAPI=$(grep '^fabric_version' gradle.properties | cut -d= -f2)
D=itest
rm -rf $D && mkdir -p $D/mods
echo "eula=true" > $D/eula.txt
printf 'online-mode=false\nlevel-seed=12345\nspawn-protection=0\n' > $D/server.properties

curl -sSL -o $D/server.jar "https://meta.fabricmc.net/v2/versions/loader/$MC/$LOADER/1.0.3/server/jar"
curl -sSL -o $D/mods/fabric-api.jar "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/$FAPI/fabric-api-$FAPI.jar"
echo "Mod da Modrinth:"
python3 ci/modrinth.py $D/mods "cobblemon@1.7.3+1.21.1" "SszvX85I@${MSD_VERSION:-+1.7.3+}" || exit 1
for f in build/libs/*.jar; do case "$f" in *-sources.jar) ;; *) cp "$f" $D/mods/ ;; esac; done
ls $D/mods

cd $D
mkfifo console
( java -Xmx3G -jar server.jar nogui < console > ../integration-test.log 2>&1 ) &
exec 3>console
for i in $(seq 1 120); do
  grep -q "Done (" ../integration-test.log 2>/dev/null && break
  grep -qE "Exception in thread \"main\"|crash-reports|Incompatible mods found" ../integration-test.log 2>/dev/null && break
  sleep 5
done
echo "datapack list enabled" >&3
echo "megaradar debugscan mega_showdown:megaroid" >&3
echo "megaradar debugscan mega_showdown:mega_site" >&3
sleep 40
echo "stop" >&3
sleep 30
exec 3>&-
wait || true
cd ..

grep -E "megaradar|Mega Showdown|multipleMegas|showdown/scripts|Failed to load script|ERROR" integration-test.log | grep -v "^\s*at " | head -60
fail=0
grep -q "Done (" integration-test.log || { echo "SERVER NON AVVIATO"; fail=1; }
grep -q "Mega Showdown trovato" integration-test.log || { echo "MEGA SHOWDOWN NON RILEVATO"; fail=1; }
grep -q "multipleMegas di Mega Showdown impostato a true" integration-test.log || { echo "multipleMegas NON ATTIVATO"; fail=1; }
grep -q "megaradar:unlimited_megas" integration-test.log || { echo "DATAPACK NON ATTIVO"; fail=1; }
grep -q "Failed to load script" integration-test.log && { echo "SCRIPT NON CARICATO"; fail=1; }
[ $fail = 0 ] && echo "INTEGRATION TEST OK"
exit $fail
