# OmniVault Trinkets Compat

Fabric and NeoForge mod for Minecraft 26.3 that lets [OmniVault](https://modrinth.com/mod/omnivault)
backpacks be worn in an accessory mod's back slot, freeing the chestplate slot for armour.

One jar per loader, each with every slot mod it supports built in as an optional integration:

| Slot mod | Fabric | NeoForge | Slot |
|---|---|---|---|
| [Trinkets Updated](https://modrinth.com/mod/trinkets-updated) | yes | yes | `chest/back` |
| [Curios API](https://modrinth.com/mod/curios) | no 26.3 release | yes | `back` |
| [Accessories](https://modrinth.com/mod/accessories) | planned | planned | waiting for a 26.3 release |

A backpack in the back slot behaves like one worn in the chest slot:
- it renders on the player's back (pushed out a pixel over a chestplate),
- the open-backpack key (B) opens it (a chest-slot backpack still takes priority),
- Pickup and Void upgrades take picked-up items,
- Magnet, Feeding, Restock and Compacting upgrades keep ticking.

## How it works
- `common/`: loader-neutral code, compiled into both jars.
  - OmniVault addresses carried backpacks by `Inventory` slot index. Back slots get virtual indices from
    1000 up (`BackSlot`), one source per installed slot mod, in registration order. Mixins make
    `Backpacks.carriedSlots` return them after the chest slot, and `BackpackSource.Held.stack` /
    `BackpackUpgrades.absorb` resolve them to the accessory stack.
  - `BackpackOnBack` draws the backpack's block model like OmniVault's `BackpackLayer`.
  - `compat/trinkets`: Trinkets' API is the same on both loaders. Slot data in `data/trinkets/`.
- `fabric/`: Fabric entrypoints (Trinkets), Fabric client gametests.
- `neoforge/`: NeoForge entrypoint, Curios compat (`ICurioItem` tick, `ICurioRenderer`, data in
  `data/curios/`), and a client test driver.

Each integration is only touched when its mod is loaded. To add Accessories later: a `BackSlot.Source`,
a tick hook and a renderer per loader, plus its slot data.

Needed on the server (slot, key, upgrades) and the client (rendering).

## Development
```
nix develop
./gradlew build                     # both jars: build/libs (fabric), neoforge/build/libs
./gradlew runClientGameTest         # Fabric: back slot carry/key/pickup/tick/render, screenshots
./gradlew runNeoForgeClientTest     # NeoForge: same checks for Curios and Trinkets (-PnoCurios / -PnoTrinkets)
python3 scripts/gen_icon.py src/main/resources/assets/omnivault_trinkets_compat/icon.png

# Modrinth page art: gallery shots, then docs/page/ (banner, headers, tier GIF, GUI shots)
OVT_GALLERY=1 ./gradlew runClientGameTest
python3 scripts/gen_page_art.py
```

## License
MIT
