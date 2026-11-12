package dev.jensh.omnivaulttrinkets.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nightbeam.omnivault.backpack.upgrade.BackpackUpgrades;
import dev.jensh.omnivaulttrinkets.BackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Pickup and Void upgrades look up carried backpacks by slot; resolve virtual back slots. */
@Mixin(value = BackpackUpgrades.class, remap = false)
public class BackpackUpgradesMixin {
	@WrapOperation(method = "absorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;"))
	private static ItemStack omnivaultTrinkets$backSlotItem(Inventory inventory, int slot, Operation<ItemStack> original) {
		return BackSlot.isVirtual(slot) ? BackSlot.get(inventory.player, slot) : original.call(inventory, slot);
	}
}
