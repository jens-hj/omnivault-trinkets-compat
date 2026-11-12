package dev.jensh.omnivaulttrinkets;

import com.nightbeam.omnivault.backpack.Backpacks;
import com.nightbeam.omnivault.backpack.upgrade.BackpackUpgrades;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * OmniVault addresses carried backpacks by {@link net.minecraft.world.entity.player.Inventory} slot index.
 * Backpacks in an accessory mod's back slot get virtual indices from {@link #BASE} up, past every real
 * slot, which the mixins resolve back to the accessory stack.
 */
public final class BackSlot {
	/** Virtual slot index of the first back slot. Non-negative, since OmniVault treats -1 as "none". */
	public static final int BASE = 1000;

	/** The back slots one accessory mod gives a player, as the live stacks in them. */
	@FunctionalInterface
	public interface Source {
		List<ItemStack> stacks(Player player);
	}

	private static final List<Source> SOURCES = new CopyOnWriteArrayList<>();

	private BackSlot() {
	}

	/** Adds an accessory mod's back slots. Called once per installed mod at startup. */
	public static void register(Source source) {
		SOURCES.add(source);
	}

	public static boolean isVirtual(int slot) {
		return slot >= BASE;
	}

	/** Every back slot stack, from all accessory mods in registration order. */
	public static List<ItemStack> stacks(Player player) {
		List<ItemStack> stacks = new ArrayList<>();
		for (Source source : SOURCES) {
			stacks.addAll(source.stacks(player));
		}
		return stacks;
	}

	/** The stack in the given virtual slot, or empty. */
	public static ItemStack get(Player player, int slot) {
		List<ItemStack> stacks = stacks(player);
		int index = slot - BASE;
		return index >= 0 && index < stacks.size() ? stacks.get(index) : ItemStack.EMPTY;
	}

	/** Virtual slots of the backpacks worn in back slots. */
	public static List<Integer> backpackSlots(Player player) {
		List<Integer> slots = new ArrayList<>();
		List<ItemStack> stacks = stacks(player);
		for (int i = 0; i < stacks.size(); i++) {
			if (Backpacks.isBackpack(stacks.get(i))) {
				slots.add(BASE + i);
			}
		}
		return slots;
	}

	/** Runs a worn backpack's upgrades (magnet, feeding, restock, compacting) like OmniVault does for the chest slot. */
	public static void tick(ItemStack stack, LivingEntity entity) {
		if (entity instanceof Player player && entity.level() instanceof ServerLevel level) {
			BackpackUpgrades.tickCarried(stack, level, player);
		}
	}
}
