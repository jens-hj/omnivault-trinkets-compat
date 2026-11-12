package dev.jensh.omnivaulttrinkets.compat.trinkets;

import dev.jensh.omnivaulttrinkets.client.BackpackOnBack;
import eu.pb4.trinkets.api.client.TrinketRenderer;
import eu.pb4.trinkets.api.client.TrinketRendererRegistry;
import net.minecraft.world.item.Item;

/** Draws backpacks worn in the Trinkets back slot. */
public final class TrinketsClientCompat {
	private static final TrinketRenderer RENDERER = (stack, slot, model, pose, collector, light, state, yRot, xRot) ->
			BackpackOnBack.submit(stack, slot.inventory().getAttachment().getEntity(), model, pose, collector, light, state);

	private TrinketsClientCompat() {
	}

	public static void registerBackpack(Item item) {
		TrinketRendererRegistry.registerRenderer(item, RENDERER);
	}
}
