package dev.jensh.omnivaulttrinkets.fabric;

import dev.jensh.omnivaulttrinkets.compat.trinkets.TrinketsClientCompat;
import dev.jensh.omnivaulttrinkets.compat.trinkets.TrinketsCompat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class FabricClientEntry implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().isModLoaded(TrinketsCompat.MOD_ID)) {
			FabricEntry.forEachBackpack(TrinketsClientCompat::registerBackpack);
		}
	}
}
