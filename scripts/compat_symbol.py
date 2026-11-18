"""Shared compatibility mark on a strict 16x16 pixel grid."""
from PIL import Image

BACKGROUND = "#142b32"
# Two joined links, explicitly drawn one cell at a time. No rasterized strokes.
LINKS = ("..GGGG", "..G..G", "CCWWGG", "C..C..", "CCCC..")
LINK_COLORS = {"G": "#ffe39b", "C": "#82e3cf", "W": "#f3faf2"}

def rgb(hex_color):
    return tuple(bytes.fromhex(hex_color.lstrip("#")))

def compose(rows, colors):
    if len(rows) != 16 or any(len(row) != 16 for row in rows):
        raise ValueError("Artwork must contain exactly 16 rows of 16 cells")
    image = Image.new("RGB", (16,16), BACKGROUND)
    for y,row in enumerate(rows):
        for x,pixel in enumerate(row):
            if pixel != ".": image.putpixel((x,y), rgb(colors[pixel]))
    for y,row in enumerate(LINKS, 10):
        for x,pixel in enumerate(row, 9):
            if pixel != ".":
                if rows[y][x] != ".": raise ValueError("Artwork overlaps the compatibility mark")
                image.putpixel((x,y), rgb(LINK_COLORS[pixel]))
    return image
