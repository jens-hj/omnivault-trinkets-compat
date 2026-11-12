#!/usr/bin/env python3
"""Builds the Modrinth page art in docs/page/ from the gallery shots.

Shoot first:  OVT_GALLERY=1 ./gradlew runClientGameTest   (writes build/run/clientGameTest/gallery/)
Fonts are OFL (docs/page/fonts). The backpack icon is the same pixel art as the mod icon.
"""
import importlib.util
from pathlib import Path
from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageFont, ImageChops

root = Path(__file__).resolve().parents[1]
shots = root / "build" / "run" / "clientGameTest" / "gallery"
out = root / "docs" / "page"
fonts = out / "fonts"
TITLE = ImageFont.truetype(str(fonts / "PressStart2P-Regular.ttf"), 8)
HEAD = ImageFont.truetype(str(fonts / "Silkscreen-Bold.ttf"), 8)
INK = (24, 22, 30)
ACCENT = (90, 170, 230)
TIERS = ["leather", "copper", "iron", "gold", "diamond", "netherite"]

_spec = importlib.util.spec_from_file_location("gen_icon", root / "scripts" / "gen_icon.py")
gen_icon = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(gen_icon)


def up(img, factor):
    return img.resize((img.width * factor, img.height * factor), Image.NEAREST)


def text_mask(text, font, scale):
    box = font.getbbox(text)
    m = Image.new("L", (box[2] - box[0] + 2, box[3] - box[1] + 2), 0)
    ImageDraw.Draw(m).text((-box[0] + 1, -box[1] + 1), text, font=font, fill=255)
    return up(m, scale)


def label(text, font, scale, colour=(240, 236, 226), shadow=3):
    m = text_mask(text, font, scale)
    img = Image.new("RGBA", (m.width + shadow, m.height + shadow), (0, 0, 0, 0))
    img.paste(Image.new("RGBA", m.size, INK + (255,)), (shadow, shadow), m)
    img.paste(Image.new("RGBA", m.size, colour + (255,)), (0, 0), m)
    return img


def extruded(text, font, scale, depth, face_top, face_bottom, extrude, outline):
    """Blocky 3D lettering: gradient face, stepped extrusion, hard outline."""
    m = text_mask(text, font, scale)
    pad = depth + scale * 2
    w, h = m.width + pad * 2, m.height + pad * 2
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))

    def at(k):
        layer = Image.new("L", (w, h), 0)
        layer.paste(m, (pad + k, pad + k))
        return layer

    grown = Image.new("L", (w, h), 0)
    for k in range(depth + 1):
        grown = ImageChops.lighter(grown, at(k))
    img.paste(Image.new("RGBA", (w, h), outline), (0, 0), grown.filter(ImageFilter.MaxFilter(scale * 2 + 1)))
    for k in range(depth, 0, -1):
        shade = tuple(int(c * (0.7 + 0.3 * (1 - k / depth))) for c in extrude) + (255,)
        img.paste(Image.new("RGBA", (w, h), shade), (0, 0), at(k))
    grad = Image.new("RGBA", (w, h))
    gd = ImageDraw.Draw(grad)
    for yy in range(h):
        t = min(max((yy - pad) / max(m.height, 1), 0), 1)
        gd.line([(0, yy), (w, yy)], fill=tuple(int(face_top[c] + (face_bottom[c] - face_top[c]) * t) for c in range(3)) + (255,))
    img.paste(grad, (0, 0), at(0))
    return img.crop(img.getbbox())


def backpack_icon(factor):
    return up(gen_icon.pixels(), factor)


def banner():
    hero = Image.open(shots / "hero.png").convert("RGB")
    W, H = 1600, 640
    hero = hero.resize((W, round(hero.height * W / hero.width)), Image.LANCZOS)
    top = 40
    hero = hero.crop((0, top, W, top + H))
    base = Image.new("RGBA", (W, H))
    base.paste(hero)
    shade = Image.new("RGBA", (W, H))
    sd = ImageDraw.Draw(shade)
    for yy in range(H):
        t = max(0, 1 - yy / (H * 0.42))
        sd.line([(0, yy), (W, yy)], fill=(22, 20, 28, int(215 * t ** 1.2)))
    base = Image.alpha_composite(base, shade)
    title = extruded("OMNIVAULT TRINKETS", TITLE, 8, 10, (196, 232, 255), (70, 140, 214), (30, 60, 110), INK + (255,))
    if title.width > W - 80:
        title = title.resize((W - 80, round(title.height * (W - 80) / title.width)), Image.NEAREST)
    base.alpha_composite(title, ((W - title.width) // 2, 34))
    sub = label("YOUR BACKPACK  IN THE BACK SLOT", HEAD, 4, shadow=4)
    base.alpha_composite(sub, ((W - sub.width) // 2, 34 + title.height + 22))
    base.convert("RGB").save(out / "banner.jpg", quality=90)


def header(text):
    icon = backpack_icon(4)
    lab = label(text.upper(), HEAD, 5)
    w = icon.width + 20 + lab.width
    h = max(icon.height, lab.height)
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    img.alpha_composite(icon, (0, (h - icon.height) // 2))
    img.alpha_composite(lab, (icon.width + 20, (h - lab.height) // 2))
    img.save(out / f"header_{text.lower().replace(' ', '_')}.png")


def divider():
    W, H = 1200, 40
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for x in range(W):
        t = 1 - abs(x - W / 2) / (W / 2)
        d.line([(x, H // 2 - 1), (x, H // 2)], fill=ACCENT + (int(255 * min(1, t * 1.6)),))
    gem = backpack_icon(2)
    gap = Image.new("RGBA", (gem.width + 24, 4), (0, 0, 0, 0))
    img.paste(gap, ((W - gap.width) // 2, H // 2 - 2))
    img.alpha_composite(gem, ((W - gem.width) // 2, (H - gem.height) // 2))
    img.save(out / "divider.png")


def framed(img, caption=None):
    """Rounded dark frame in the accent colour, with an optional pixel caption."""
    pad = 10
    W, H = img.width + pad * 2, img.height + pad * 2 + (44 if caption else 0)
    base = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(base).rounded_rectangle((0, 0, W - 1, H - 1), 14, fill=(30, 30, 38, 255), outline=ACCENT + (255,), width=3)
    base.paste(img, (pad, pad))
    if caption:
        m = label(caption, HEAD, 3, shadow=0)
        base.alpha_composite(m, ((W - m.width) // 2, H - 34))
    return base


def gui(name, box, factor=2):
    img = Image.open(shots / f"{name}.png").convert("RGB").crop(box)
    framed(up(img, factor)).convert("RGB").save(out / f"{name}.png", optimize=True)


def tiers_gif():
    frames = []
    for tier in TIERS:
        img = Image.open(shots / f"tier_{tier}.png").convert("RGB").crop((960, 500, 1600, 1300))
        img = ImageEnhance.Contrast(img.resize((420, 525), Image.LANCZOS)).enhance(1.04)
        frames.append(framed(img, tier.upper()))
    bg = Image.new("RGBA", frames[0].size, (30, 30, 38, 255))
    flat = [Image.alpha_composite(bg, f).convert("RGB").quantize(colors=200, method=Image.Quantize.MEDIANCUT) for f in frames]
    flat[0].save(out / "tiers.gif", save_all=True, append_images=flat[1:], duration=1100, loop=0, optimize=True, disposal=2)


if __name__ == "__main__":
    out.mkdir(parents=True, exist_ok=True)
    banner()
    divider()
    for text in ("Wear it", "Open it", "Upgrades", "Requirements"):
        header(text)
    gui("inventory_back_slot", (248, 70, 606, 400))
    gui("inventory_back_slot_tooltip", (248, 70, 680, 400))
    gui("backpack_key", (122, 18, 738, 462))
    tiers_gif()
    print("Wrote", sorted(p.name for p in out.iterdir() if p.is_file()))
