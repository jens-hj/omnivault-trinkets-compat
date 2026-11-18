"""Compat family symbol v1: two joined links, identical across all compat mods.

Palette and geometry live on an absolute 32x32 grid. Nearest-neighbour scaling
keeps the badge identical at 128px and 512px. Fix/patch mods do not use this mark.
Keep this self-contained file identical in the four compat repositories.
"""
from PIL import Image, ImageDraw


def with_compat_symbol(image):
    result = image.convert("RGBA")
    if result.width != result.height or result.width % 32:
        raise ValueError("Compatibility icons must be square multiples of 32px")
    badge = Image.new("RGBA", (32, 32))
    draw = ImageDraw.Draw(badge)
    draw.polygon([(18,18),(29,18),(31,20),(31,29),(29,31),(18,31),(16,29),(16,20)], fill="#10202b")
    draw.rectangle((18,23,25,28), outline="#82e3cf", width=2)
    draw.rectangle((23,20,29,25), outline="#ffe39b", width=2)
    draw.rectangle((23,23,24,24), fill="#f3faf2")
    result.alpha_composite(badge.resize(result.size, Image.Resampling.NEAREST))
    return result
