"""Original compatibility icon, composed on one absolute pixel grid."""
from pathlib import Path
from PIL import Image, ImageDraw
from compat_symbol import BACKGROUND, draw_compat_symbol

root = Path(__file__).resolve().parents[1]
image = Image.new("RGB", (32,32), BACKGROUND)
d = ImageDraw.Draw(image)
# Backpack silhouette; the family links sit beside the lower attachment point.
d.rectangle((7,3,12,4), fill="#cf9e68")
d.rectangle((6,4,13,5), fill="#68482f")
d.rectangle((4,7,16,24), fill="#a96d42")
d.rectangle((5,5,15,24), fill="#a96d42")
d.rectangle((5,6,15,11), fill="#c38a56")
d.rectangle((4,11,16,12), fill="#6c472e")
d.rectangle((6,15,14,21), fill="#815334")
d.rectangle((7,15,13,16), fill="#d29a60")
d.rectangle((9,10,11,13), fill="#ffe39b")
d.rectangle((3,9,3,22), fill="#664632")
d.rectangle((17,9,17,20), fill="#664632")
d.rectangle((5,25,15,25), fill="#664632")

draw_compat_symbol(image)
for path, size in ((root / "common/src/main/resources/assets/omnivault_trinkets_compat/icon.png", 128), (root / "docs/icon.png", 512)):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.resize((size,size), Image.Resampling.NEAREST).save(path)
