"""Compat family links: integrated into each composition on a 32px grid."""
from PIL import ImageDraw

BACKGROUND = "#142b32"

def draw_compat_symbol(image):
    if image.size != (32, 32):
        raise ValueError("Compose compatibility icons on the shared 32px grid")
    draw = ImageDraw.Draw(image)
    # The surrounding space is part of the icon composition, not a badge.
    draw.rectangle((18,23,25,28), outline="#82e3cf", width=2)
    draw.rectangle((23,20,29,25), outline="#ffe39b", width=2)
    draw.rectangle((23,23,24,24), fill="#f3faf2")
