"""Genera le texture della GUI del radar, una per tipo, nello stile dell'item:
ghiera colorata (viola = Megaroidi, azzurra = Megasite), corpo in metallo scuro,
schermo a fosfori verdi a forma di decagono con anelli e mirino.
Le cornici degli slot sul decagono, il fascio di scansione e il bottone sono disegnati in codice.

Uso: gen_gui_texture.py <cartella textures/gui>
"""
import math, sys, os
from PIL import Image, ImageDraw

W, H = 176, 232
CX, CY = 88, 74
INV_Y = H - 82

BLACK = (0, 0, 0, 255)
BODY = (38, 42, 51, 255)          # metallo scuro (come la cornice dell'item)
BODY_HI = (58, 63, 74, 255)
BODY_LO = (24, 27, 33, 255)
WELL = (27, 30, 37, 255)          # vano inventario incassato
SCREEN = (10, 40, 24, 255)        # verde fosfori (come lo schermo dell'item)
GRID = (22, 78, 46, 255)
GRID_DIM = (16, 58, 34, 255)
SLOT_D, SLOT_L, SLOT_F = (14, 16, 20, 255), (74, 80, 92, 255), (46, 51, 61, 255)

THEMES = {
    "megaroid": dict(accent=(150, 70, 200), hi=(196, 120, 240), lo=(92, 38, 128)),
    "mega_city": dict(accent=(40, 150, 190), hi=(110, 214, 245), lo=(22, 92, 120)),
}

def rgba(c): return tuple(c) + (255,) if len(c) == 3 else c

def deca(r, cx=CX, cy=CY):
    return [(cx + r * math.cos(math.radians(-90 + 36 * i)), cy + r * math.sin(math.radians(-90 + 36 * i)))
            for i in range(10)]

def build(theme):
    acc, hi, lo = rgba(theme["accent"]), rgba(theme["hi"]), rgba(theme["lo"])
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # ghiera colorata con rilievo + contorno nero
    d.rounded_rectangle([0, 0, W - 1, H - 1], radius=7, fill=BLACK)
    d.rounded_rectangle([1, 1, W - 2, H - 2], radius=6, fill=lo)
    d.rounded_rectangle([1, 1, W - 3, H - 3], radius=6, fill=hi)
    d.rounded_rectangle([2, 2, W - 3, H - 3], radius=5, fill=acc)
    # corpo in metallo scuro
    d.rounded_rectangle([4, 4, W - 5, H - 5], radius=4, fill=BLACK)
    d.rounded_rectangle([5, 5, W - 6, H - 6], radius=3, fill=BODY)
    d.line([(8, 5), (W - 9, 5)], fill=BODY_HI)

    # schermo a decagono: anello metallico, filo colorato, vetro verde
    d.polygon(deca(56), fill=BLACK)
    d.polygon(deca(55), fill=BODY_HI)
    d.polygon(deca(53.5), fill=lo)
    d.polygon(deca(52.5), fill=acc)
    d.polygon(deca(51.5), fill=BLACK)
    d.polygon(deca(50.5), fill=SCREEN)
    # anelli concentrici e mirino
    for r in (17, 34):
        d.polygon(deca(r), outline=GRID)
    for k in range(-46, 47, 2):           # mirino tratteggiato
        d.point([(CX, CY + k), (CX + k, CY)], fill=GRID_DIM)
    # tacche sui raggi verso i vertici
    for i in range(10):
        a = math.radians(-90 + 36 * i + 18)
        for r in (40, 42, 44):
            d.point([(round(CX + r * math.cos(a)), round(CY + r * math.sin(a)))], fill=GRID_DIM)

    # vano inventario incassato
    top = INV_Y - 16
    d.rounded_rectangle([7, top, W - 8, H - 8], radius=2, fill=WELL)
    d.line([(8, top), (W - 9, top)], fill=BODY_LO)
    d.line([(8, H - 8), (W - 9, H - 8)], fill=BODY_HI)

    # viti decorative negli angoli in basso del corpo
    for sx in (11, W - 12):
        d.ellipse([sx - 1, top - 9, sx + 1, top - 7], fill=BODY_HI)
        d.point([(sx, top - 8)], fill=BODY_LO)

    def slot(x, y):
        d.rectangle([x - 1, y - 1, x + 16, y + 16], fill=SLOT_D)
        d.rectangle([x, y, x + 16, y + 16], fill=SLOT_L)
        d.rectangle([x, y, x + 15, y + 15], fill=SLOT_F)

    for r in range(3):
        for c in range(9):
            slot(8 + c * 18, INV_Y + r * 18)
    for c in range(9):
        slot(8 + c * 18, INV_Y + 58)
    return img

out = sys.argv[1]
os.makedirs(out, exist_ok=True)
for name, theme in THEMES.items():
    build(theme).save(os.path.join(out, f"radar_{name}.png"))
print("ok")
