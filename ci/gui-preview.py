"""Anteprima statica della GUI del radar con texture e font originali di Minecraft (presi dal jar del client).
Riproduce le stesse coordinate di RadarMenu/RadarScreen. Uso: gui-preview.py <radar.png> <client.jar> <out.png>"""
import math, sys, zipfile, io
from PIL import Image

tex_path, jar_path, out_path = sys.argv[1:4]
jar = zipfile.ZipFile(jar_path)
def load(p): return Image.open(io.BytesIO(jar.read(p))).convert("RGBA")

# ---- font bitmap di Minecraft (ascii.png, glifi 8x8)
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
def draw_text(img, s, x, y, rgb, shadow=False):
    if shadow:
        r, g, b = rgb; draw_text(img, s, x + 1, y + 1, (r // 4, g // 4, b // 4))
    for c in s:
        if c == " ": x += 4; continue
        code = ch(c); gx, gy = (code % 16) * cell, (code // 16) * cell
        glyph = font.crop((gx, gy, gx + cell, gy + cell))
        tint = Image.new("RGBA", glyph.size, rgb + (255,)); tint.putalpha(glyph.getchannel("A"))
        img.alpha_composite(tint, (x, y)); x += widths[code] + 1

FUEL = ["coal", "copper_ingot", "iron_ingot", "gold_ingot", "redstone",
        "lapis_lazuli", "quartz", "amethyst_shard", "emerald", "diamond"]
items = {n: load(f"assets/minecraft/textures/item/{n}.png").resize((16, 16), Image.NEAREST) for n in FUEL}
radar_tex = Image.open(tex_path).convert("RGBA").crop((0, 0, 176, 232))

CX, CY, R = 88, 74, 48
def slot_pos(i):
    a = math.radians(-90 + 36 * i)
    return round(CX + R * math.cos(a)) - 8, round(CY + R * math.sin(a)) - 8

def fill(img, x0, y0, x1, y1, argb):
    a = (argb >> 24) & 255; rgb = ((argb >> 16) & 255, (argb >> 8) & 255, argb & 255)
    img.alpha_composite(Image.new("RGBA", (x1 - x0, y1 - y0), rgb + (a,)), (x0, y0))

def count(img, n, x, y):
    s = str(n); draw_text(img, s, x + 19 - 2 - text_w(s), y + 6 + 3, (255, 255, 255), shadow=True)

def panel(counts, ready):
    img = radar_tex.copy()
    draw_text(img, "Radar per Megaroidi", 8, 6, (64, 64, 64))
    draw_text(img, "Inventario", 8, 232 - 94, (64, 64, 64))
    for i, n in enumerate(FUEL):
        x, y = slot_pos(i)
        fill(img, x - 1, y - 1, x + 17, y + 17, 0xFF373737); fill(img, x, y, x + 17, y + 17, 0xFFFFFFFF); fill(img, x, y, x + 16, y + 16, 0xFF8B8B8B)
        img.alpha_composite(items[n], (x, y))
        if counts[i] == 0:
            count(img, 64, x, y)
            fill(img, x, y, x + 16, y + 16, 0x808B8B8B)       # stato vuoto: 50% di opacità
        elif counts[i] > 1:
            count(img, counts[i], x, y)
    # bottone
    bx, by, w, h = CX - 25, CY - 8, 50, 16
    f, hi, sh = (0xFF3E9B2A, 0xFF86DB63, 0xFF2A6B1C) if ready else (0xFFAAAAAA, 0xFFC8C8C8, 0xFF7E7E7E)
    fill(img, bx + 1, by, bx + w - 1, by + h, 0xFF1E1E1E); fill(img, bx, by + 1, bx + w, by + h - 1, 0xFF1E1E1E)
    fill(img, bx + 1, by + 1, bx + w - 1, by + h - 1, f)
    fill(img, bx + 1, by + 1, bx + w - 1, by + 2, hi); fill(img, bx + 1, by + h - 2, bx + w - 1, by + h - 1, sh)
    label = "Cerca"
    draw_text(img, label, bx + (w - text_w(label)) // 2, by + (h - 8) // 2,
              (255, 255, 255) if ready else (60, 60, 60), shadow=ready)
    return img

states = [panel([0] * 10, False),
          panel([64, 64, 64, 30, 0, 64, 0, 64, 0, 0], False),
          panel([64] * 10, True)]
S, GAP = 3, 24
out = Image.new("RGBA", (len(states) * (176 * S + GAP) + GAP, 232 * S + 2 * GAP), (32, 30, 36, 255))
for k, st in enumerate(states):
    out.alpha_composite(st.resize((176 * S, 232 * S), Image.NEAREST), (GAP + k * (176 * S + GAP), GAP))
out.save(out_path)
print("preview ok", out_path)
