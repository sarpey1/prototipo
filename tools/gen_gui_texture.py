"""Genera textures/gui/radar.png: pannello stile vanilla 176x232 con il decagono e gli slot dell'inventario.
Le cornici degli slot del decagono sono disegnate in codice (RadarScreen), così compaiono solo quelle usate."""
import math, sys
from PIL import Image, ImageDraw

W, H = 176, 232
CX, CY, R_POLY = 88, 74, 51          # vertici del decagono sotto il centro degli slot (raggio slot = 48)
INV_Y = H - 82

BLACK = (0, 0, 0, 255)
PANEL = (198, 198, 198, 255)
HI = (255, 255, 255, 255)
SH = (85, 85, 85, 255)
DEC_FILL = (174, 174, 174, 255)
DEC_EDGE = (120, 120, 120, 255)
DEC_INNER = (186, 186, 186, 255)
SLOT_D, SLOT_L, SLOT_F = (55, 55, 55, 255), (255, 255, 255, 255), (139, 139, 139, 255)

img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

# pannello con angoli arrotondati e rilievo
d.rounded_rectangle([0, 0, W - 1, H - 1], radius=4, fill=BLACK)
d.rounded_rectangle([1, 1, W - 2, H - 2], radius=3, fill=PANEL)
d.rectangle([3, 1, W - 5, 2], fill=HI); d.rectangle([1, 3, 2, H - 5], fill=HI); d.point([(3, 3)], fill=HI)
d.rectangle([4, H - 3, W - 3, H - 2], fill=SH); d.rectangle([W - 3, 4, W - 2, H - 3], fill=SH); d.point([(W - 4, H - 4)], fill=SH)

def deca(r):
    return [(CX + r * math.cos(math.radians(-90 + 36 * i)), CY + r * math.sin(math.radians(-90 + 36 * i))) for i in range(10)]

# decagono: bordo scuro, corpo, leggera luce interna sul bordo
d.polygon(deca(R_POLY), fill=DEC_EDGE)
d.polygon(deca(R_POLY - 1.6), fill=DEC_INNER)
d.polygon(deca(R_POLY - 3.2), fill=DEC_FILL)

def slot(x, y):  # x,y = posizione dell'item; cornice 18x18
    d.rectangle([x - 1, y - 1, x + 16, y + 16], fill=SLOT_D)
    d.rectangle([x, y, x + 16, y + 16], fill=SLOT_L)
    d.rectangle([x, y, x + 15, y + 15], fill=SLOT_F)

for r in range(3):
    for c in range(9):
        slot(8 + c * 18, INV_Y + r * 18)
for c in range(9):
    slot(8 + c * 18, INV_Y + 58)

img.save(sys.argv[1])
