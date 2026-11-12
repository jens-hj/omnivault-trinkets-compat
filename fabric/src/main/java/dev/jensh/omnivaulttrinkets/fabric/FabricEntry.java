package dev.jensh.omnivaulttrinkets.fabric;

import com.nightbeam.omnivault.backpack.BackpackItem;
import dev.jensh.omnivaulttrinkets.compat.trinkets.TrinketsCompat;
import java.util.function.Consumer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class FabricEntry implements ModInitializer {
	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().isModLoaded(TrinketsCompat.MOD_ID)) {
			TrinketsCompat.init();
			forEachBackpack(TrinketsCompat::registerBackpack);
		}
	}

	/** Every OmniVault backpack item, whether it is registered before or after this mod initializes. */
	static void forEachBackpack(Consumer<Item> action) {
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof BackpackItem) {
				action.accept(item);
			}
		}
		RegistryEntryAddedCallback.event(BuiltInRegistries.ITEM).register((rawId, id, item) -> {
			if (item instanceof BackpackItem) {
				action.accept(item);
			}
		});
	}
}
