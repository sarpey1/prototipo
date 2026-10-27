#!/usr/bin/env bash
# Avvia un server dedicato con la mod, prova la scansione e lo spegne.
set -u
mkdir -p run
echo "eula=true" > run/eula.txt
printf 'online-mode=false\nlevel-seed=12345\n' > run/server.properties
mkfifo /tmp/console
( gradle runServer --args="nogui" --no-daemon < /tmp/console > server-test.log 2>&1 ) &
exec 3>/tmp/console

for i in $(seq 1 180); do
  if grep -q "Done (" server-test.log 2>/dev/null; then break; fi
  if grep -qE "BUILD FAILED|Exception in thread|crash" server-test.log 2>/dev/null; then break; fi
  sleep 5
done

echo "megaradar debugscan minecraft:village_plains" >&3
echo "megaradar debugscan minecraft:ruined_portal" >&3
echo "megaradar debugscan mega_showdown:megaroid" >&3
sleep 60
echo "stop" >&3
sleep 30
exec 3>&-
wait || true

cat server-test.log | tail -80
grep -q "Mega Radar inizializzato" server-test.log || { echo "MOD NON CARICATA"; exit 1; }
grep -q "Done (" server-test.log || { echo "SERVER NON AVVIATO"; exit 1; }
grep -q "debugscan minecraft:village_plains: [1-9]" server-test.log || { echo "SCANSIONE FALLITA"; exit 1; }
echo "SMOKE TEST OK"
