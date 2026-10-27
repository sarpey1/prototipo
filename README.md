# Mega Radar — Fabric 1.21.1 (Cobbleverse / Mega Showdown)

Due radar che localizzano le strutture di **Mega Showdown** tramite la stessa routine di `/locate`,
con un costo di carburante pesante e una freccia-bussola sull'HUD.

| Item | Trova | Struttura di default |
|---|---|---|
| Radar per Megaroidi (`megaradar:megaroid_radar`) | Meteorite sepolto con il Keystone Ore (Y −20 / −32) | `mega_showdown:megaroid` |
| Radar per Megasite (`megaradar:mega_city_radar`) | Sito sotterraneo con il Mega Crystal → Mega Stone | `mega_showdown:mega_site` |

## Struttura del progetto

```
megaradar/
├── build.gradle                 Loom 1.10, mappature Mojang, split client/common
├── gradle.properties            versioni MC / Loader / Fabric API
├── settings.gradle
├── gradle/wrapper/gradle-wrapper.properties
└── src/
    ├── main/                    CODICE COMUNE (server + client)
    │   ├── java/com/megaradar/
    │   │   ├── MegaRadar.java               entrypoint main
    │   │   ├── command/RadarCommand.java    /megaradar reload
    │   │   ├── config/RadarConfig.java      config/megaradar.json (Gson)
    │   │   ├── fuel/FuelManager.java        verifica e consumo carburante
    │   │   ├── item/RadarItem.java          logica di utilizzo (server-side)
    │   │   ├── network/
    │   │   │   ├── ModNetworking.java       registrazione payload + invio
    │   │   │   ├── RadarResultPayload.java  S2C esito scansione
    │   │   │   └── FuelSyncPayload.java     S2C costo carburante del server
    │   │   ├── radar/RadarType.java         enum dei due radar
    │   │   ├── radar/StructureScanner.java  tag + config → findNearestMapStructure
    │   │   └── registry/ModItems.java       registrazione item + tab creativo
    │   └── resources/
    │       ├── fabric.mod.json
    │       ├── assets/megaradar/            lang it/en, modelli, texture, icona
    │       └── data/megaradar/
    │           ├── recipe/                  ricette shaped
    │           └── tags/worldgen/structure/ megaroid_targets.json, mega_city_targets.json
    └── client/                  SOLO CLIENT
        └── java/com/megaradar/client/
            ├── MegaRadarClient.java         ricevitori payload + DISCONNECT
            └── RadarHud.java                freccia e distanza sull'HUD
```

## Flusso di rete

```
Client  ── clic destro (pacchetto vanilla "use item") ──▶  Server
                                                        controlla carburante
                                                        findNearestMapStructure(...)
                                                        consuma carburante
Client  ◀── RadarResultPayload (S2C) ───────────────────  posizione, dimensione, durata
Client  disegna la freccia sull'HUD
```

All'ingresso nel server arriva anche `FuelSyncPayload`, così il tooltip mostra il costo della config **del server**.
Non serve un payload C2S: l'uso dell'item passa già dal pacchetto vanilla e il server è autoritativo.

## Crafting (shaped)

**Radar per Megaroidi**
```
Netherite  Echo Shard   Netherite
Diamond B. Recovery C.  Diamond B.
Netherite  Amethyst B.  Netherite
```

**Radar per Megasite**
```
Emerald B.     Netherite    Emerald B.
Recovery C.    Nether Star  Heart of the Sea
Emerald B.     Netherite    Emerald B.
```

Le ricette sono in `data/megaradar/recipe/` e si possono sovrascrivere con un datapack o KubeJS.

## Carburante

Default: **uno stack (64) di ogni minerale per ogni scansione** — carbone, lingotti di rame, ferro e oro,
redstone, lapislazzuli, quarzo, frammenti di ametista, smeraldi, diamanti.
Viene consumato solo se la scansione trova qualcosa (configurabile). In creativa è gratis.

## Configurazione — `config/megaradar.json`

```json
{
  "searchRadiusChunks": 100,
  "cooldownTicks": 200,
  "consumeFuelOnlyIfFound": true,
  "skipKnownStructures": false,
  "hudDurationSeconds": 180,
  "fuelPerUse": { "minecraft:coal": 64, "minecraft:diamond": 64 },
  "megaroidExtraStructures": [],
  "megaCityExtraStructures": []
}
```

Dopo una modifica: `/megaradar reload` (OP 2), che risincronizza anche i client connessi.

### Cambiare le strutture bersaglio

Due modi, cumulabili:

1. **Tag (consigliato, via datapack)**: `data/megaradar/tags/worldgen/structure/megaroid_targets.json`
   e `mega_city_targets.json`. Le voci usano `"required": false`, quindi la mod si avvia anche senza Mega Showdown.
2. **Config**: aggiungi ID in `megaroidExtraStructures` / `megaCityExtraStructures`
   (es. una struttura di Cobbleverse o di un altro pack).

Per scoprire gli ID esatti delle strutture installate: scrivi `/locate structure ` in gioco e guarda l'autocompletamento.

## Build

Richiede **JDK 21**.

```bash
gradle wrapper          # solo la prima volta (crea gradlew e gradle-wrapper.jar)
./gradlew build         # jar in build/libs/megaradar-1.0.0.jar
./gradlew runClient     # test in dev
```

Oppure apri la cartella in IntelliJ IDEA (con il plugin Minecraft Development) e importa come progetto Gradle.

Se `fabric_version` in `gradle.properties` non viene risolta, prendi l'ultima build per 1.21.1 da
https://fabricmc.net/develop.

## Installazione in Cobbleverse

Copia `megaradar-1.0.0.jar` nella cartella `mods` **sia del client sia del server**.
