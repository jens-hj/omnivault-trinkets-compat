package dev.jensh.omnivaulttrinkets;

import com.nightbeam.omnivault.backpack.BackpackItem;
import com.nightbeam.omnivault.backpack.upgrade.BackpackUpgrades;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import java.util.function.Consumer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class OmniVaultTrinkets implements ModInitializer {
	/** Runs a worn backpack's upgrades (magnet, feeding, restock, compacting) like OmniVault does for the chest slot. */
	private static final TrinketCallback BACKPACK = new TrinketCallback() {
		@Override
		public void tick(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			if (entity instanceof Player player && entity.level() instanceof ServerLevel level) {
				BackpackUpgrades.tickCarried(stack, level, player);
			}
		}
	};

	@Override
	public void onInitialize() {
		forEachBackpack(item -> TrinketCallback.setCallback(item, BACKPACK));
	}

	/** Every OmniVault backpack item, whether it is registered before or after this mod initializes. */
	public static void forEachBackpack(Consumer<Item> action) {
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
