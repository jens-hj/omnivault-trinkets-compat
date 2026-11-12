package dev.jensh.omnivaulttrinkets.test;

import com.nightbeam.omnivault.backpack.BackpackContent;
import com.nightbeam.omnivault.backpack.BackpackData;
import com.nightbeam.omnivault.backpack.BackpackItem;
import com.nightbeam.omnivault.backpack.Backpacks;
import com.nightbeam.omnivault.backpack.client.BackpackClient;
import com.nightbeam.omnivault.backpack.upgrade.UpgradeType;
import dev.jensh.omnivaulttrinkets.BackSlot;
import dev.jensh.omnivaulttrinkets.compat.trinkets.TrinketsCompat;
import eu.pb4.trinkets.api.TrinketsApi;
import java.nio.file.Path;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;

/** Modrinth gallery and page shots. Only runs with OVT_GALLERY set; writes to build/run/clientGameTest/gallery. */
@SuppressWarnings("UnstableApiUsage")
public class GalleryClientGameTest implements FabricClientGameTest {
	static final boolean ENABLED = System.getenv("OVT_GALLERY") != null;
	private static final Path OUT = Path.of("gallery");
	private static final String[] TIERS = {"leather", "copper", "iron", "gold", "diamond", "netherite"};

	@Override
	public void runTest(ClientGameTestContext context) {
		if (!ENABLED) {
			return;
		}
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			sp.getConnection().waitForChunksRender();
			for (String cmd : new String[]{"gamerule spawn_mobs false", "gamerule advance_time false", "gamerule advance_weather false",
					"gamerule show_advancement_messages false", "time set 1000", "weather clear", "gamemode survival @a"}) {
				sp.getServer().runCommand(cmd);
			}
			scene(sp);
			context.waitTicks(20);
			sp.getConnection().waitForChunksRender();
			context.runOnClient(mc -> {
				mc.options.fov().set(60);
				mc.options.bobView().set(false);
				mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
			});
			context.getInput().pressKey(options -> options.keyToggleGui);

			// Hero: a dyed leather backpack on the back over iron armour, in front of a vault wall.
			armour(sp, Items.IRON_CHESTPLATE);
			back(sp, "leather", 0x3D7BC8);
			pose(context, sp, 180, 8);
			shoot(context, "hero");

			// Every tier, over a chestplate.
			armour(sp, Items.DIAMOND_CHESTPLATE);
			pose(context, sp, 200, 18);
			for (String tier : TIERS) {
				back(sp, tier, -1);
				shoot(context, "tier_" + tier);
			}
			armour(sp, ItemStack.EMPTY.getItem());
			back(sp, "netherite", -1);
			shoot(context, "netherite_no_armour");

			// Inventory: the chest trinket group open, hovering the backpack in the back slot.
			back(sp, "diamond", -1);
			armour(sp, Items.DIAMOND_CHESTPLATE);
			fill(sp);
			context.getInput().pressKey(options -> options.keyToggleGui);
			context.runOnClient(mc -> {
				mc.options.setCameraType(CameraType.FIRST_PERSON);
				mc.gui.toastManager().clear();
				mc.gui.setScreen(new InventoryScreen(mc.player));
			});
			context.waitTicks(5);
			hover(context, false, 0);
			context.waitTicks(5);
			hover(context, false, -3);
			context.waitTicks(5);
			gui(context, "inventory_back_slot");
			hover(context, true, 0);
			context.waitTicks(5);
			gui(context, "inventory_back_slot_tooltip");
			context.runOnClient(mc -> mc.gui.setScreen(null));
			context.waitTicks(5);

			// The backpack key opens the back slot backpack.
			context.getInput().pressKey(BackpackClient.OPEN_KEY);
			context.waitTicks(10);
			gui(context, "backpack_key");
			context.runOnClient(mc -> mc.gui.setScreen(null));
		}
	}

	private static void scene(TestSingleplayerContext sp) {
		run(sp, "tp @a 0.5 -60 0.5 180 8");
		run(sp, "fill -6 -61 -9 6 -61 2 minecraft:stone_bricks");
		run(sp, "fill -5 -61 -8 5 -61 1 minecraft:polished_andesite");
		String[] vaults = {"copper", "iron", "gold", "diamond", "netherite", "omnium"};
		for (int i = 0; i < vaults.length; i++) {
			int x = i - 3;
			run(sp, "setblock %d -60 -6 omnivault:%s_vault[facing=south]", x, vaults[i]);
			run(sp, "setblock %d -59 -6 omnivault:drawer_2x2[facing=south]", x);
			run(sp, "setblock %d -58 -6 omnivault:drawer_1x2[facing=south]", x);
		}
		run(sp, "setblock 3 -60 -6 omnivault:nexus_core");
		run(sp, "setblock -4 -60 -5 omnivault:copper_backpack[facing=south]");
		run(sp, "setblock 4 -60 -4 minecraft:lantern");
		run(sp, "setblock -5 -60 -3 minecraft:barrel");
	}

	/** Turns the player, body included: the body otherwise lags behind the head after a teleport. */
	private static void pose(ClientGameTestContext context, TestSingleplayerContext sp, int yaw, int pitch) {
		run(sp, "tp @a 0.5 -60 0.5 %d %d", yaw, pitch);
		context.waitTicks(2);
		context.runOnClient(mc -> {
			mc.player.setYBodyRot(yaw);
			mc.player.yBodyRotO = yaw;
			mc.player.setYHeadRot(yaw);
			mc.player.yHeadRotO = yaw;
		});
	}

	private static void armour(TestSingleplayerContext sp, net.minecraft.world.item.Item item) {
		sp.getServer().runOnServer(server -> player(server).setItemSlot(EquipmentSlot.CHEST, new ItemStack(item)));
	}

	private static void back(TestSingleplayerContext sp, String tier, int dye) {
		sp.getServer().runOnServer(server -> {
			ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("omnivault:" + tier + "_backpack")));
			if (dye >= 0) {
				stack.set(DataComponents.DYED_COLOR, new DyedItemColor(dye));
			}
			TrinketsApi.getAttachment(player(server)).getInventory(TrinketsCompat.SLOT).setItem(0, stack);
		});
	}

	/** Puts some loot and two upgrades in the worn backpack so its screen and tooltip aren't empty. */
	private static void fill(TestSingleplayerContext sp) {
		sp.getServer().runOnServer(server -> {
			ServerPlayer player = player(server);
			ItemStack stack = BackSlot.get(player, BackSlot.BASE);
			BackpackData data = Backpacks.data(server, stack);
			data.upgrades().setItem(0, new ItemStack(BackpackContent.upgrade(UpgradeType.PICKUP)));
			data.upgrades().setItem(1, new ItemStack(BackpackContent.upgrade(UpgradeType.MAGNET)));
			ItemStack[] loot = {new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.IRON_INGOT, 37), new ItemStack(Items.REDSTONE, 52),
					new ItemStack(Items.OAK_LOG, 48), new ItemStack(Items.BREAD, 12), new ItemStack(Items.TORCH, 40),
					new ItemStack(Items.DIAMOND, 9), new ItemStack(Items.COAL, 30), new ItemStack(Items.AMETHYST_SHARD, 18),
					new ItemStack(Items.QUARTZ, 21), new ItemStack(Items.GOLD_INGOT, 14), new ItemStack(Items.ENDER_PEARL, 6)};
			for (int i = 0; i < loot.length; i++) {
				data.items().setItem(i, loot[i]);
			}
			Backpacks.refreshSummary(stack, data);
			player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_PICKAXE));
			player.getInventory().setItem(1, new ItemStack(Items.DIAMOND_SWORD));
			player.getInventory().setItem(2, new ItemStack(Items.TORCH, 32));
			player.getInventory().setItem(3, new ItemStack(Items.COOKED_BEEF, 16));
		});
	}

	/**
	 * Hovers the chestplate slot (opening the chest trinket group), or the backpack in the back slot. A non-zero
	 * {@code dx} aims that far into the slot instead of its centre; -3 is the group's border, which keeps the group
	 * open without a tooltip.
	 */
	private static void hover(ClientGameTestContext context, boolean backpack, int dx) {
		double[] pos = context.computeOnClient(mc -> {
			var screen = (AbstractContainerScreen<?>) mc.gui.screen();
			Slot target = screen.getMenu().slots.get(6); // chestplate slot
			if (backpack) {
				for (Slot slot : screen.getMenu().slots) {
					if (slot.getItem().getItem() instanceof BackpackItem && slot.container != mc.player.getInventory() && slot.isActive()) {
						target = slot;
					}
				}
			}
			int left = field(screen, "leftPos"), top = field(screen, "topPos");
			double scale = mc.getWindow().getGuiScale();
			return new double[]{(left + target.x + (dx != 0 ? dx : 8)) * scale, (top + target.y + 8) * scale};
		});
		context.getInput().setCursorPos(pos[0], pos[1]);
	}

	private static int field(Object screen, String name) {
		try {
			var f = AbstractContainerScreen.class.getDeclaredField(name);
			f.setAccessible(true);
			return f.getInt(screen);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private static void shoot(ClientGameTestContext context, String name) {
		context.runOnClient(mc -> mc.gui.toastManager().clear());
		context.waitTicks(10);
		context.takeScreenshot(TestScreenshotOptions.of(name).disableCounterPrefix().withSize(2560, 1440).withDestinationDir(OUT));
	}

	private static void gui(ClientGameTestContext context, String name) {
		context.runOnClient(mc -> mc.gui.toastManager().clear());
		context.takeScreenshot(TestScreenshotOptions.of(name).disableCounterPrefix().withDestinationDir(OUT));
	}

	private static ServerPlayer player(MinecraftServer server) {
		return server.getPlayerList().getPlayers().getFirst();
	}

	private static void run(TestSingleplayerContext sp, String fmt, Object... args) {
		sp.getServer().runCommand(String.format(Locale.ROOT, fmt, args));
	}
}
