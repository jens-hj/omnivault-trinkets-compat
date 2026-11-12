package dev.jensh.omnivaulttrinkets.neoforge;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.jensh.omnivaulttrinkets.client.BackpackOnBack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/** Draws backpacks worn in the Curios back slot. */
public final class CuriosClientCompat {
	private static final ICurioRenderer RENDERER = new ICurioRenderer() {
		@Override
		public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(ItemStack stack, SlotContext slotContext,
				PoseStack pose, SubmitNodeCollector collector, int light, S state, RenderLayerParent<S, M> parent,
				EntityRendererProvider.Context context, float yRot, float xRot) {
			BackpackOnBack.submit(stack, slotContext.entity(), parent.getModel(), pose, collector, light, state);
		}
	};

	private CuriosClientCompat() {
	}

	public static void registerBackpack(Item item) {
		ICurioRenderer.register(item, () -> RENDERER);
	}
}
