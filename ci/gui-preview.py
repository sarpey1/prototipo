"""Anteprima della GUI del radar con texture e font originali di Minecraft (presi dal jar del client).
Riproduce coordinate e colori di RadarMenu / RadarScreen / SearchButton.
Uso: gui-preview.py <cartella textures/gui> <client.jar> <out.png> <out.gif>"""
import math, sys, zipfile, io
from PIL import Image

gui_dir, jar_path, out_png, out_gif = sys.argv[1:5]
jar = zipfile.ZipFile(jar_path)
def load(p): return Image.open(io.BytesIO(jar.read(p))).convert("RGBA")

# ---- font bitmap di Minecraft
font = load("assets/minecraft/textures/font/ascii.png")
cell = font.width // 16
widths = {}
for code in range(256):
    gx, gy = (code % 16) * cell, (code // 16) * cell
    w = 0
    for x in range(cell):
        if any(font.getpixel((gx + x, gy + y))[3] for y in range(cell)): w = x + 1
    widths[code] = w
def ch(c): return ord(c) if ord(c) < 256 else ord("?")
def text_w(s): return sum((4 if c == " " else widths[ch(c)] + 1) for c in s) - 1
def rgb(argb): return ((argb >> 16) & 255, (argb >> 8) & 255, argb & 255)
def draw_text(img, s, x, y, color, shadow=False):
    if shadow:
        r, g, b = color; draw_text(img, s, x + 1, y + 1, (r // 4, g // 4, b // 4))
    for c in s:
        if c == " ": x += 4; continue
        code = ch(c); gx, gy = (code % 16) * cell, (code // 16) * cell
        glyph = font.crop((gx, gy, gx + cell, gy + cell))
        tint = Image.new("RGBA", glyph.size, color + (255,)); tint.putalpha(glyph.getchannel("A"))
        img.alpha_composite(tint, (x, y)); x += widths[code] + 1

FUEL = ["coal", "copper_ingot", "iron_ingot", "gold_ingot", "redstone",
        "lapis_lazuli", "quartz", "amethyst_shard", "emerald", "diamond"]
items = {n: load(f"assets/minecraft/textures/item/{n}.png").resize((16, 16), Image.NEAREST) for n in FUEL}

THEMES = {
    "megaroid": dict(tex="radar_megaroid.png", light=0xFFE678FF, title="Radar per Megaroidi"),
    "mega_city": dict(tex="radar_mega_city.png", light=0xFF6EF0FF, title="Radar per Megasite"),
}
PHOSPHOR, PHOSPHOR_BRIGHT = 0x3CC86E, 0xFF5CFF8E
SCREEN_SLOT, SCREEN_SLOT_EDGE = 0xFF0F2318, 0xFF2A5A3C
LABEL, MUTED = 0xFF9AA3B2, 0xFF8A93A3
CX, CY, R = 88, 74, 48

def slot_pos(i):
    a = math.radians(-90 + 36 * i)
    return round(CX + R * math.cos(a)) - 8, round(CY + R * math.sin(a)) - 8

def fill(img, x0, y0, x1, y1, argb):
    a = (argb >> 24) & 255
    img.alpha_composite(Image.new("RGBA", (x1 - x0, y1 - y0), rgb(argb) + (a,)), (x0, y0))

def lerp(a, b, t):
    t = max(0, min(1, t))
    return 0xFF000000 | sum(int(((a >> s) & 255) + (((b >> s) & 255) - ((a >> s) & 255)) * t) << s for s in (16, 8, 0))

SWEEP = [(dx, dy, math.degrees(math.atan2(dy, dx))) for dy in range(-46, 47) for dx in range(-46, 47)
         if 2 <= math.hypot(dx, dy) <= 46]

def sweep_layer(sweep, ready):
    layer = Image.new("RGBA", (176, 232), (0, 0, 0, 0)); px = layer.load()
    base = 0xA0 if ready else 0x70
    for dx, dy, ang in SWEEP:
        diff = (sweep - ang) % 360
        if diff > 75: continue
        al = min(255, base + 0x40) if diff < 3 else int(base * (1 - diff / 75) ** 2)
        if al < 6: continue
        px[CX + dx, CY + dy] = rgb(PHOSPHOR) + (al,)
    return layer

def count(img, n, x, y):
    s = str(n); draw_text(img, s, x + 19 - 2 - text_w(s), y + 6 + 3, (255, 255, 255), shadow=True)

def panel(theme_key, counts, sweep, pulse=1.0):
    th = THEMES[theme_key]
    ready = all(c == 64 for c in counts)
    img = Image.open(f"{gui_dir}/{th['tex']}").convert("RGBA").crop((0, 0, 176, 232))
    img.alpha_composite(sweep_layer(sweep, ready))
    for i, n in enumerate(FUEL):
        x, y = slot_pos(i)
        if counts[i] == 64: border = PHOSPHOR_BRIGHT
        else: border = lerp(SCREEN_SLOT_EDGE, th["light"], 1 - ((sweep - (-90 + 36 * i)) % 360) / 100)
        fill(img, x - 1, y - 1, x + 17, y + 17, border); fill(img, x, y, x + 16, y + 16, SCREEN_SLOT)
        img.alpha_composite(items[n], (x, y))
        if counts[i] == 0:
            count(img, 64, x, y); fill(img, x, y, x + 16, y + 16, 0x80000000 | (SCREEN_SLOT & 0xFFFFFF))
        elif counts[i] > 1:
            count(img, counts[i], x, y)
    # bottone
    bx, by, w, h = CX - 25, CY - 8, 50, 16
    if ready:
        fill(img, bx - 2, by - 2, bx + w + 2, by + h + 2, (int(0x20 + 0x50 * pulse) << 24) | PHOSPHOR)
        fill(img, bx - 1, by - 1, bx + w + 1, by + h + 1, (int(0x60 + 0x80 * pulse) << 24) | PHOSPHOR)
        f, hi, sh = 0xFF2FA352, 0xFF6EE89A, 0xFF1C6A35
    else:
        f, hi, sh = 0xFF3A3F4A, 0xFF4E5461, 0xFF262A32
    fill(img, bx + 1, by, bx + w - 1, by + h, 0xFF0E1014); fill(img, bx, by + 1, bx + w, by + h - 1, 0xFF0E1014)
    fill(img, bx + 1, by + 1, bx + w - 1, by + h - 1, f)
    fill(img, bx + 1, by + 1, bx + w - 1, by + 2, hi); fill(img, bx + 1, by + h - 2, bx + w - 1, by + h - 1, sh)
    draw_text(img, "Cerca", bx + (w - text_w("Cerca")) // 2, by + 4, (255, 255, 255) if ready else rgb(MUTED), shadow=ready)
    # testi
    draw_text(img, th["title"], 8, 6, rgb(th["light"]), shadow=True)
    done = sum(1 for c in counts if c == 64)
    prog = f"{done}/10"
    draw_text(img, prog, 176 - 8 - text_w(prog), 6, rgb(PHOSPHOR_BRIGHT if done == 10 else MUTED), shadow=True)
    draw_text(img, "Inventario", 8, 232 - 94, rgb(LABEL))
    return img

PARTIAL = [64, 64, 64, 30, 0, 64, 0, 64, 0, 0]
states = [panel("megaroid", [0] * 10, 20), panel("megaroid", PARTIAL, 150), panel("mega_city", [64] * 10, 250)]
S, GAP = 3, 24
out = Image.new("RGBA", (len(states) * (176 * S + GAP) + GAP, 232 * S + 2 * GAP), (24, 22, 28, 255))
for k, st in enumerate(states):
    out.alpha_composite(st.resize((176 * S, 232 * S), Image.NEAREST), (GAP + k * (176 * S + GAP), GAP))
out.save(out_png)

# GIF: un giro di scansione, a sinistra radar in attesa, a destra radar pronto (gira al doppio della velocità)
frames = []
N = 24
for f in range(N):
    left = panel("megaroid", PARTIAL, -90 + 360 * f / N)
    right = panel("mega_city", [64] * 10, -90 + 720 * f / N, pulse=0.5 + 0.5 * math.sin(2 * math.pi * f / N * 3))
    fr = Image.new("RGBA", (2 * 176 * 2 + 3 * 12, 232 * 2 + 24), (24, 22, 28, 255))
    fr.alpha_composite(left.resize((352, 464), Image.NEAREST), (12, 12))
    fr.alpha_composite(right.resize((352, 464), Image.NEAREST), (24 + 352, 12))
    frames.append(fr.convert("RGB").quantize(colors=255, method=Image.Quantize.MEDIANCUT))
frames[0].save(out_gif, save_all=True, append_images=frames[1:], duration=133, loop=0)
print("preview ok")
