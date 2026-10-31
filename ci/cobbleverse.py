"""Legge il modpack Cobbleverse (versione richiesta) da Modrinth e scarica le jar ESATTE di Cobblemon,
Mega Showdown e delle loro dipendenze dichiarate nel pack. Scrive versions.txt."""
import json, os, sys, urllib.request, zipfile, io, urllib.parse
UA = {"User-Agent": "megaradar-ci (github actions)"}
def get(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers=UA)) as r: return r.read()
want, out = sys.argv[1], sys.argv[2]
os.makedirs(out, exist_ok=True)
vers = json.loads(get("https://api.modrinth.com/v2/project/cobbleverse/version"))
match = [v for v in vers if want in v["version_number"] and "fabric" in v.get("loaders", [])] or [v for v in vers if want in v["version_number"]]
lines = [f"Cobbleverse disponibili: {[v['version_number'] for v in vers[:15]]}"]
if not match:
    open("versions.txt", "w").write("\n".join(lines + ["NESSUNA VERSIONE " + want])); sys.exit(0)
v = match[0]
lines.append(f"Scelto: {v['version_number']} loaders={v.get('loaders')} game={v.get('game_versions')}")
f = next(x for x in v["files"] if x["filename"].endswith(".mrpack"))
pack = zipfile.ZipFile(io.BytesIO(get(f["url"])))
index = json.loads(pack.read("modrinth.index.json"))
lines.append(f"dependencies: {index.get('dependencies')}")
keys = ("cobblemon", "mega_showdown", "mega-showdown", "megashowdown", "architectury", "accessories", "owo", "kotlin", "fabric-api", "fabric_api")
for fe in index["files"]:
    name = fe["path"].split("/")[-1]
    if fe["path"].startswith("mods/"):
        lines.append("mod: " + name)
    if fe["path"].startswith("mods/") and any(k in name.lower() for k in keys):
        open(os.path.join(out, name), "wb").write(get(fe["downloads"][0]))
        lines.append("  -> scaricato " + name)
# override e config del pack che riguardano showdown / mega showdown
for n in pack.namelist():
    if any(k in n.lower() for k in ("mega_showdown", "showdown", "megaradar")):
        lines.append("override: " + n)
        if n.endswith((".json", ".json5", ".toml", ".js")) and pack.getinfo(n).file_size < 200000:
            os.makedirs("diag/pack", exist_ok=True)
            open("diag/pack/" + n.replace("/", "__"), "wb").write(pack.read(n))
open("versions.txt", "w").write("\n".join(lines) + "\n")
print("\n".join(lines[:5]))
