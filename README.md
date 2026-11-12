# OmniVault Trinkets Compat

Fabric mod for Minecraft 26.3 that lets [OmniVault](https://modrinth.com/mod/omnivault) backpacks be worn
in the [Trinkets Updated](https://modrinth.com/mod/trinkets-updated) back slot (`chest/back`), freeing the
chestplate slot for armour.

A backpack in the back slot behaves like one worn in the chest slot:
- it renders on the player's back (pushed out a pixel over a chestplate),
- the open-backpack key (B) opens it (a chest-slot backpack still takes priority),
- Pickup and Void upgrades take picked-up items,
- Magnet, Feeding, Restock and Compacting upgrades keep ticking.

## How it works
- Data: enables `chest/back` for players and adds `#omnivault:backpacks` to `#trinkets:chest/back`.
- A `TrinketCallback` per backpack item runs OmniVault's `BackpackUpgrades.tickCarried`.
- OmniVault addresses carried backpacks by `Inventory` slot index. Back slots get virtual indices from
  1000 up: `Backpacks.carriedSlots` returns them after the chest slot, and `BackpackSource.Held.stack` /
  `BackpackUpgrades.absorb` resolve them to the trinket stack (mixins).
- A `TrinketRenderer` draws the backpack's block model like OmniVault's `BackpackLayer`.

Needed on the server (slot, key, upgrades) and the client (rendering).

## Development
```
nix develop
./gradlew runClientGameTest   # wears a backpack in the back slot, checks key/pickup/tick, screenshots it
./gradlew build
python3 scripts/gen_icon.py src/main/resources/assets/omnivault_trinkets_compat/icon.png
```

## License
MIT
