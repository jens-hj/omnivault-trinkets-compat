package dev.jensh.omnivaulttrinkets.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nightbeam.omnivault.backpack.BackpackItem;
import com.nightbeam.omnivault.backpack.BackpackTier;
import com.nightbeam.omnivault.backpack.Backpacks;
import com.nightbeam.omnivault.backpack.client.BackpackClient;
import com.nightbeam.omnivault.client.ClientPlatform;
import java.util.List;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * Draws a backpack worn in a back slot the way OmniVault draws one worn in the chest slot: its block
 * model, on the back of the torso. Shared by every accessory mod's renderer hook.
 */
public final class BackpackOnBack {
	// Same placement as OmniVault's BackpackLayer.
	private static final float MODEL_TOP = 13.0f / 16;
	private static final float MODEL_BACK = 13.0f / 16;
	private static final float SCALE = 0.85f;
	/** Pushed out a pixel over a chestplate so it doesn't sink into the armour. */
	private static final float ARMOR_OFFSET = 1.0f / 16;

	private BackpackOnBack() {
	}

	public static void submit(ItemStack stack, LivingEntity entity, Object model, PoseStack pose, SubmitNodeCollector collector, int light,
			LivingEntityRenderState state) {
		if (!(stack.getItem() instanceof BackpackItem item) || !(model instanceof HumanoidModel<?> humanoid) || state.isInvisible) {
			return;
		}
		ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
		if (Backpacks.isBackpack(chest)) {
			// OmniVault already draws the chest-slot backpack in this spot.
			return;
		}
		List<BlockStateModelPart> parts = List.of(ClientPlatform.INSTANCE.getStandaloneModel(BackpackClient.model(item.tier())));
		int tint = item.tier() == BackpackTier.LEATHER ? DyedItemColor.getOrDefault(stack, DyedItemColor.LEATHER_COLOR) : -1;
		int[] tints = {0xFF000000 | tint};

		pose.pushPose();
		humanoid.body.translateAndRotate(pose);
		pose.translate(0, 1.0f / 16, 2.0f / 16 + (chest.isEmpty() ? 0 : ARMOR_OFFSET));
		pose.rotateDegrees(Axis.XP, 180);
		pose.scale(SCALE, SCALE, SCALE);
		pose.translate(-0.5f, -MODEL_TOP, -MODEL_BACK);
		collector.submitBlockModel(pose, stack.hasFoil() ? Sheets.cutoutBlockItemGlintSheet() : Sheets.cutoutBlockItemSheet(), parts, tints, light,
				OverlayTexture.NO_OVERLAY, state.outlineColor);
		pose.popPose();
	}
}
