package dev.jensh.omnivaulttrinkets;

import com.nightbeam.omnivault.backpack.Backpacks;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketsApi;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * OmniVault addresses carried backpacks by {@link net.minecraft.world.entity.player.Inventory} slot index.
 * Backpacks in the Trinkets back slot get virtual indices from {@link #BASE} up, past every real slot,
 * which the mixins resolve back to the trinket stack.
 */
public final class BackSlot {
	public static final String SLOT = "chest/back";
	/** Virtual slot index of the first back slot. Non-negative, since OmniVault treats -1 as "none". */
	public static final int BASE = 1000;

	private BackSlot() {
	}

	public static boolean isVirtual(int slot) {
		return slot >= BASE;
	}

	@Nullable
	private static TrinketInventory inventory(Player player) {
		return TrinketsApi.getAttachment(player).getInventory(SLOT);
	}

	/** The stack in the given virtual slot, or empty. */
	public static ItemStack get(Player player, int slot) {
		TrinketInventory inventory = inventory(player);
		int index = slot - BASE;
		return inventory != null && index >= 0 && index < inventory.getContainerSize() ? inventory.getItem(index) : ItemStack.EMPTY;
	}

	/** Virtual slots of the backpacks worn in the back slot. */
	public static List<Integer> backpackSlots(Player player) {
		List<Integer> slots = new ArrayList<>();
		TrinketInventory inventory = inventory(player);
		if (inventory != null) {
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				if (Backpacks.isBackpack(inventory.getItem(i))) {
					slots.add(BASE + i);
				}
			}
		}
		return slots;
	}
}
