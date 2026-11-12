package dev.jensh.omnivaulttrinkets.compat.trinkets;

import dev.jensh.omnivaulttrinkets.BackSlot;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Trinkets Updated (Fabric and NeoForge): the {@code chest/back} slot. Only touched when Trinkets is installed. */
public final class TrinketsCompat {
	public static final String MOD_ID = "trinkets_updated";
	public static final String SLOT = "chest/back";

	private static final TrinketCallback BACKPACK = new TrinketCallback() {
		@Override
		public void tick(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			BackSlot.tick(stack, entity);
		}
	};

	private TrinketsCompat() {
	}

	public static void init() {
		BackSlot.register(player -> {
			TrinketInventory inventory = TrinketsApi.getAttachment(player).getInventory(SLOT);
			if (inventory == null) {
				return List.of();
			}
			List<ItemStack> stacks = new ArrayList<>(inventory.getContainerSize());
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				stacks.add(inventory.getItem(i));
			}
			return stacks;
		});
	}

	public static void registerBackpack(Item item) {
		TrinketCallback.setCallback(item, BACKPACK);
	}
}
