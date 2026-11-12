package dev.jensh.omnivaulttrinkets.client;

import dev.jensh.omnivaulttrinkets.OmniVaultTrinkets;
import eu.pb4.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.ClientModInitializer;

public class OmniVaultTrinketsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BackpackTrinketRenderer renderer = new BackpackTrinketRenderer();
		OmniVaultTrinkets.forEachBackpack(item -> TrinketRendererRegistry.registerRenderer(item, renderer));
	}
}
