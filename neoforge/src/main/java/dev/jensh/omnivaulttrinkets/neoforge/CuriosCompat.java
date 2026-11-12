package dev.jensh.omnivaulttrinkets.neoforge;

import dev.jensh.omnivaulttrinkets.BackSlot;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/** Curios API (NeoForge): the {@code back} slot. Only touched when Curios is installed. */
public final class CuriosCompat {
	public static final String MOD_ID = "curios";
	public static final String SLOT = "back";

	private static final ICurioItem BACKPACK = new ICurioItem() {
		@Override
		public void curioTick(SlotContext slotContext, ItemStack stack) {
			BackSlot.tick(stack, slotContext.entity());
		}
	};

	private CuriosCompat() {
	}

	public static void init() {
		BackSlot.register(player -> CuriosApi.getCuriosInventory(player)
				.flatMap(handler -> handler.getStacksHandler(SLOT))
				.map(handler -> {
					IDynamicStackHandler stacks = handler.getStacks();
					List<ItemStack> list = new ArrayList<>(stacks.getSlots());
					for (int i = 0; i < stacks.getSlots(); i++) {
						list.add(stacks.getStackInSlot(i));
					}
					return list;
				})
				.orElse(List.of()));
	}

	public static void registerBackpack(Item item) {
		CuriosApi.registerCurio(item, BACKPACK);
	}
}
