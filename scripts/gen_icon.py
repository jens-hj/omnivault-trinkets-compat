"""Original pixel art. Each character is exactly one cell of a 16x16 grid."""
from pathlib import Path
from PIL import Image
from compat_symbol import compose

root = Path(__file__).resolve().parents[1]
ART = (
    '................',
    '................',
    '.....HH.........',
    '...HHDDHH.......',
    '..DLLLLLLD......',
    '..DLLLLLLD......',
    '..DDDYYDDD......',
    '..DMMYYMMD......',
    '..DMMMMMMD......',
    '..DMDDDDMD......',
    '..DMDLLDMD......',
    '..DMDDDDMD......',
    '...DDDDDD.......',
    '................',
    '................',
    '................',
)
COLORS = {'H': '#d5a576', 'D': '#755039', 'L': '#c9915b', 'M': '#ad774b', 'Y': '#ffe39b'}
image = compose(ART, COLORS)
for path, size in ((root / "common/src/main/resources/assets/omnivault_trinkets_compat/icon.png", 128), (root / "docs/icon.png", 512)):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.resize((size,size), Image.Resampling.NEAREST).save(path)
