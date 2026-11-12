package dev.jensh.omnivaulttrinkets.test;

import com.nightbeam.omnivault.backpack.BackpackContent;
import com.nightbeam.omnivault.backpack.BackpackData;
import com.nightbeam.omnivault.backpack.BackpackMenu;
import com.nightbeam.omnivault.backpack.BackpackSource;
import com.nightbeam.omnivault.backpack.BackpackSummary;
import com.nightbeam.omnivault.backpack.Backpacks;
import com.nightbeam.omnivault.backpack.upgrade.BackpackUpgrades;
import com.nightbeam.omnivault.backpack.upgrade.UpgradeType;
import dev.jensh.omnivaulttrinkets.BackSlot;
import dev.jensh.omnivaulttrinkets.compat.trinkets.TrinketsCompat;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.impl.client.render.ClientTrinketsManager;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** A backpack in the Trinkets back slot is carried, opens, runs its upgrades and renders. */
@SuppressWarnings("UnstableApiUsage")
public class BackSlotClientGameTest implements FabricClientGameTest {
	private static final String[] TIERS = {"leather", "copper", "iron", "gold", "diamond", "netherite"};

	@Override
	public void runTest(ClientGameTestContext context) {
		List<String> failures = new ArrayList<>();

		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			sp.getConnection().waitForChunksRender();
			for (String cmd : new String[]{"time set noon", "weather clear", "gamerule spawn_mobs false"}) {
				sp.getServer().runCommand(cmd);
			}

			context.runOnClient(mc -> {
				for (String tier : TIERS) {
					Item item = backpack(tier);
					if (ClientTrinketsManager.INSTANCE.getResolved(new ItemStack(item)) == null) {
						failures.add(tier + " backpack has no trinket renderer");
					}
				}
			});

			// Every tier fits the back slot.
			sp.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				TrinketInventory back = TrinketsApi.getAttachment(player).getInventory(TrinketsCompat.SLOT);
				if (back == null) {
					failures.add("player has no chest/back trinket slot");
					return;
				}
				for (String tier : TIERS) {
					ItemStack stack = new ItemStack(backpack(tier));
					if (!back.getSlotAccess(0).slotType().validatorCheck(stack, back.getSlotAccess(0), player)) {
						failures.add(tier + " backpack is not allowed in chest/back");
					}
				}
			});

			// Wear an iron backpack with a Pickup upgrade in the back slot.
			sp.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				TrinketInventory back = TrinketsApi.getAttachment(player).getInventory(TrinketsCompat.SLOT);
				if (back == null) {
					return;
				}
				ItemStack stack = new ItemStack(backpack("iron"));
				BackpackData data = Backpacks.data(server, stack);
				data.upgrades().setItem(0, new ItemStack(BackpackContent.upgrade(UpgradeType.PICKUP)));
				data.items().setItem(0, new ItemStack(Items.COBBLESTONE, 3));
				back.setItem(0, stack);

				List<Integer> carried = Backpacks.carriedSlots(player);
				if (!carried.contains(BackSlot.BASE)) {
					failures.add("back slot backpack is not carried: " + carried);
				}
				if (Backpacks.slotForKey(player) != BackSlot.BASE) {
					failures.add("backpack key would open slot " + Backpacks.slotForKey(player));
				}

				ItemStack rest = BackpackUpgrades.absorb(player, new ItemStack(Items.DIRT, 5));
				if (!rest.isEmpty()) {
					failures.add("pickup upgrade left " + rest);
				}

				Backpacks.open(player, new BackpackSource.Held(Backpacks.slotForKey(player)));
				if (!(player.containerMenu instanceof BackpackMenu menu) || !menu.stillValid(player)) {
					failures.add("backpack key did not open the back slot backpack: " + player.containerMenu);
				}
				player.closeContainer();
			});

			// The worn backpack ticks, which refreshes its tooltip summary.
			context.waitTicks(25);
			sp.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
				TrinketInventory back = TrinketsApi.getAttachment(player).getInventory(TrinketsCompat.SLOT);
				if (back == null) {
					return;
				}
				BackpackSummary summary = back.getItem(0).get(BackpackContent.SUMMARY.get());
				if (summary == null || summary.used() < 2) {
					failures.add("back slot backpack did not tick, summary " + summary);
				}
			});

			context.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK));
			context.waitTicks(20);
			context.takeScreenshot("back_slot_iron");

			sp.getServer().runOnServer(server -> server.getPlayerList().getPlayers().getFirst()
					.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE)));
			context.waitTicks(10);
			context.takeScreenshot("back_slot_iron_chestplate");
			context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
		}

		if (!failures.isEmpty()) {
			throw new AssertionError("Failed checks:\n - " + String.join("\n - ", failures));
		}
	}

	private static Item backpack(String tier) {
		return BuiltInRegistries.ITEM.getValue(Identifier.parse("omnivault:" + tier + "_backpack"));
	}
}
