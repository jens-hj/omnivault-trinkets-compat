package dev.jensh.omnivaulttrinkets.mixin;

import com.nightbeam.omnivault.backpack.Backpacks;
import dev.jensh.omnivaulttrinkets.BackSlot;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Backpacks in the back slot count as carried, right after the chest slot: the backpack key opens them
 * and their Pickup and Void upgrades take picked-up items.
 */
@Mixin(value = Backpacks.class, remap = false)
public class BackpacksMixin {
	@Inject(method = "carriedSlots", at = @At("RETURN"))
	private static void omnivaultTrinkets$addBackSlot(Player player, CallbackInfoReturnable<List<Integer>> cir) {
		List<Integer> back = BackSlot.backpackSlots(player);
		if (back.isEmpty()) {
			return;
		}
		List<Integer> slots = cir.getReturnValue();
		boolean chest = !slots.isEmpty() && slots.getFirst() == Backpacks.CHEST_SLOT;
		slots.addAll(chest ? 1 : 0, back);
	}
}
