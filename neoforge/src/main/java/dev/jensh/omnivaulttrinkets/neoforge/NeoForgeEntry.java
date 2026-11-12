package dev.jensh.omnivaulttrinkets.neoforge;

import com.nightbeam.omnivault.backpack.BackpackItem;
import dev.jensh.omnivaulttrinkets.compat.trinkets.TrinketsClientCompat;
import dev.jensh.omnivaulttrinkets.compat.trinkets.TrinketsCompat;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(NeoForgeEntry.MOD_ID)
public class NeoForgeEntry {
	public static final String MOD_ID = "omnivault_trinkets_compat";

	public NeoForgeEntry(IEventBus modBus, Dist dist) {
		boolean trinkets = ModList.get().isLoaded(TrinketsCompat.MOD_ID);
		boolean curios = ModList.get().isLoaded(CuriosCompat.MOD_ID);
		if (curios) {
			CuriosCompat.init();
		}
		if (trinkets) {
			TrinketsCompat.init();
		}
		modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(() -> {
			if (curios) {
				forEachBackpack(CuriosCompat::registerBackpack);
			}
			if (trinkets) {
				forEachBackpack(TrinketsCompat::registerBackpack);
			}
		}));
		if (dist.isClient()) {
			modBus.addListener(FMLClientSetupEvent.class, event -> event.enqueueWork(() -> {
				if (curios) {
					forEachBackpack(CuriosClientCompat::registerBackpack);
				}
				if (trinkets) {
					forEachBackpack(TrinketsClientCompat::registerBackpack);
				}
			}));
		}
	}

	private static void forEachBackpack(Consumer<Item> action) {
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof BackpackItem) {
				action.accept(item);
			}
		}
	}
}
