package dev.jensh.omnivaulttrinkets.mixin;

import com.nightbeam.omnivault.backpack.BackpackSource;
import dev.jensh.omnivaulttrinkets.BackSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** An open backpack whose source is a virtual back slot reads its stack from Trinkets. */
@Mixin(value = BackpackSource.Held.class, remap = false)
public class BackpackSourceHeldMixin {
	@Shadow
	@Final
	private int slot;

	@Inject(method = "stack", at = @At("HEAD"), cancellable = true)
	private void omnivaultTrinkets$backSlotStack(Player player, CallbackInfoReturnable<ItemStack> cir) {
		if (BackSlot.isVirtual(slot)) {
			cir.setReturnValue(BackSlot.get(player, slot));
		}
	}
}
