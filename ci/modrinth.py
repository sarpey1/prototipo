"""Scarica da Modrinth una mod (versione esatta) e tutte le sue dipendenze obbligatorie per Fabric 1.21.1."""
import json, sys, os, urllib.request, urllib.parse

API = "https://api.modrinth.com/v2"
GAME, LOADER = "1.21.1", "fabric"
UA = {"User-Agent": "megaradar-ci (github actions)"}

def get(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers=UA)) as r:
        return json.load(r)

def versions(project):
    q = urllib.parse.urlencode({"loaders": json.dumps([LOADER]), "game_versions": json.dumps([GAME])})
    return get(f"{API}/project/{project}/version?{q}")

def pick(project, exact=None):
    vs = versions(project)
    if exact:  # corrispondenza parziale: "+1.7.3" = ultima versione per Cobblemon 1.7.3
        vs = [v for v in vs if exact in v["version_number"]]
    if not vs:
        raise SystemExit(f"nessuna versione per {project} {exact or ''}")
    return vs[0]

def download(version, out):
    f = next((f for f in version["files"] if f["primary"]), version["files"][0])
    path = os.path.join(out, f["filename"])
    if not os.path.exists(path):
        urllib.request.urlretrieve(f["url"], path)
    print(f"  {f['filename']}")

def resolve(version, out, seen, pins):
    for dep in version.get("dependencies", []):
        if dep["dependency_type"] != "required" or not dep.get("project_id"):
            continue
        pid = dep["project_id"]
        if pid in seen or pid == "P7dR8mSH":  # Fabric API la forniamo noi
            continue
        seen.add(pid)
        slug = get(f"{API}/project/{pid}")["slug"]
        dv = pick(pid, pins.get(slug))
        download(dv, out)
        resolve(dv, out, seen, pins)

if __name__ == "__main__":
    out = sys.argv[1]
    os.makedirs(out, exist_ok=True)
    pins = {}
    roots = []
    for arg in sys.argv[2:]:
        slug, _, ver = arg.partition("@")
        roots.append(slug)
        if ver:
            pins[slug] = ver
    seen = set()
    for slug in roots:
        v = pick(slug, pins.get(slug))
        seen.add(v["project_id"])
        download(v, out)
        resolve(v, out, seen, pins)
