"""Genera le texture animate dei due radar (strisce verticali di frame 16x16 + .mcmeta) e l'icona della mod.
Il fascio ruota in senso orario lasciando una scia; il bersaglio (blip) si accende quando il fascio ci passa sopra.

Uso: gen_item_textures.py <cartella assets/megaradar>
"""
import math, os, sys, json
from PIL import Image

FRAMES = 20          # un giro completo
FRAMETIME = 2        # tick per frame -> 2 secondi a giro

SCREEN = (10, 40, 24)
RING = (30, 110, 60)
FRAME = (40, 44, 52)
CENTER = (120, 255, 160)
TRAIL = [(20, (90, 230, 140)), (50, (50, 165, 95)), (90, (28, 92, 54))]

TYPES = {
    "megaroid_radar": dict(blip=(230, 120, 255), bezel=(150, 70, 200), bezel_hi=(196, 120, 240)),
    "mega_city_radar": dict(blip=(110, 240, 255), bezel=(40, 150, 190), bezel_hi=(110, 214, 245)),
}
BLIP_PIXELS = [(10, 4), (11, 4), (10, 5), (11, 5)]
C = 7.5

def mix(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))

def frame(spec, lead):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    p = im.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - C, y - C)
            if d > 7.5:
                continue
            if d > 6.4:  # ghiera, con riflesso in alto a sinistra
                p[x, y] = (spec["bezel_hi"] if (x + y) < 11 else spec["bezel"]) + (255,)
            elif d > 5.6:
                p[x, y] = FRAME + (255,)
            else:
                col = RING if abs(d - 2.9) < 0.5 else SCREEN
                ang = math.degrees(math.atan2(y - C, x - C)) % 360
                diff = (lead - ang) % 360
                for limit, c in TRAIL:
                    if diff < limit:
                        col = c
                        break
                p[x, y] = col + (255,)
    for (x, y) in [(7, 7), (8, 7)]:
        p[x, y] = CENTER + (255,)
    # blip: luminoso subito dopo il passaggio del fascio, poi si affievolisce
    bang = math.degrees(math.atan2(4.5 - C, 10.5 - C)) % 360
    since = (lead - bang) % 360
    k = 1.0 if since < 30 else max(0.3, 1 - (since - 30) / 200)
    for (x, y) in BLIP_PIXELS:
        p[x, y] = mix(SCREEN, spec["blip"], k) + (255,)
    return im

out = sys.argv[1]
item_dir = os.path.join(out, "textures", "item")
os.makedirs(item_dir, exist_ok=True)
firsts = {}
for name, spec in TYPES.items():
    frames = [frame(spec, f * 360 / FRAMES) for f in range(FRAMES)]
    strip = Image.new("RGBA", (16, 16 * FRAMES))
    for i, fr in enumerate(frames):
        strip.paste(fr, (0, 16 * i))
    strip.save(os.path.join(item_dir, f"{name}.png"))
    with open(os.path.join(item_dir, f"{name}.png.mcmeta"), "w") as f:
        json.dump({"animation": {"frametime": FRAMETIME, "interpolate": False}}, f, indent=2)
    # per l'icona: frame con il blip appena acceso
    firsts[name] = frames[int(FRAMES * 0.9)]
    if len(sys.argv) > 2:  # anteprima GIF opzionale
        big = [fr.resize((128, 128), Image.NEAREST) for fr in frames]
        bg = [Image.alpha_composite(Image.new("RGBA", (128, 128), (40, 40, 46, 255)), b) for b in big]
        bg[0].save(os.path.join(sys.argv[2], f"{name}.gif"), save_all=True, append_images=bg[1:],
                   duration=100, loop=0, disposal=2)

icon = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
icon.paste(firsts["megaroid_radar"].resize((64, 64), Image.NEAREST), (0, 32))
icon.paste(firsts["mega_city_radar"].resize((64, 64), Image.NEAREST), (64, 32))
icon.save(os.path.join(out, "icon.png"))
print("ok")
