"""Draws the mod icon: a backpack with a gold trinket clasp as 16x16 pixel art, scaled up.

Usage: gen_icon.py <out.png> [modrinth_out.png]
The first output is 128px on a transparent background; the optional second is a 512px
rounded dark tile for the Modrinth project page.
"""
import sys
from PIL import Image, ImageDraw

# . empty, o outline, d dark leather, m mid leather, l light leather, s strap, g gold, y highlight
ART = [
    "................",
    ".....oooooo.....",
    "....oddddddo....",
    "...ommmmmmmmo...",
    "..oslmmmmmmlso..",
    "..osmmmmmmmmso..",
    "..osddddddddso..",
    "..osmmmgymmmso..",
    "..osmmgyygmmso..",
    "..osmmmggmmmso..",
    "..osmmmmmmmmso..",
    "..osdmmmmmmdso..",
    "..osddmmmmddso..",
    "..oossssssssoo..",
    "...oooooooooo...",
    "................",
]
COLORS = {
    "o": (30, 20, 16, 255),
    "d": (102, 60, 34, 255),
    "m": (146, 92, 52, 255),
    "l": (190, 132, 82, 255),
    "s": (64, 40, 26, 255),
    "g": (214, 160, 40, 255),
    "y": (255, 232, 120, 255),
}


def pixels():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(ART):
        for x, c in enumerate(row):
            if c in COLORS:
                img.putpixel((x, y), COLORS[c])
    return img


def main():
    art = pixels()
    art.resize((128, 128), Image.NEAREST).save(sys.argv[1])
    if len(sys.argv) > 2:
        tile = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
        ImageDraw.Draw(tile).rounded_rectangle((0, 0, 511, 511), radius=96, fill=(28, 24, 44, 255))
        tile.alpha_composite(art.resize((448, 448), Image.NEAREST), (32, 32))
        tile.save(sys.argv[2])


if __name__ == "__main__":
    main()
