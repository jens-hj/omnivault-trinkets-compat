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
import dev.jensh.omnivaulttrinkets.neoforge.CuriosCompat;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketsApi;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.LoadingErrorScreen;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Drives a real NeoForge client: makes a world, wears a backpack in each installed accessory mod's back
 * slot, checks OmniVault sees it (carried, key, Pickup, ticking, menu) and screenshots it. Writes the
 * result to the file in {@code omnivault_trinkets_compat.test_result}, then quits.
 */
final class NeoClientTest {
	/** Runs {@code action} once {@code ready} holds, then waits {@code delay} client ticks. */
	private record Step(int delay, BooleanSupplier ready, Runnable action) {
	}

	/** One accessory mod's back slot: puts a stack in it (empty clears it). */
	private record Slot(String name, Function<ServerPlayer, Consumer<ItemStack>> setter) {
	}

	private final Queue<Step> steps = new ArrayDeque<>();
	private final List<String> failures = new ArrayList<>();
	private boolean started, seeded, done;
	private volatile int equippedAt;
	private int delay, ticks;
	private Minecraft mc;

	static void install() {
		NeoClientTest test = new NeoClientTest();
		NeoForge.EVENT_BUS.addListener(test::tick);
	}

	private void tick(ClientTickEvent.Post event) {
		if (done) {
			return;
		}
		mc = Minecraft.getInstance();
		try {
			if (++ticks > 3600) {
				throw new AssertionError("NeoForge client test timed out");
			}
			if (!started) {
				if (mc.gui.screen() instanceof LoadingErrorScreen) {
					throw new AssertionError("NeoForge loading warnings or errors; see the client log");
				}
				if (!(mc.gui.screen() instanceof TitleScreen)) {
					return;
				}
				started = true;
				mc.createWorldOpenFlows().createFreshLevel("back-slot-test-" + System.currentTimeMillis(),
						new LevelSettings("Back slot test", GameType.SURVIVAL, LevelSettings.DifficultySettings.DEFAULT, true, WorldDataConfiguration.DEFAULT),
						new WorldOptions(0, false, false), WorldPresets::createTestWorldDimensions, new TitleScreen());
				return;
			}
			if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) {
				return;
			}
			if (!seeded) {
				seeded = true;
				scenario();
				delay = 40;
			}
			if (delay-- > 0) {
				return;
			}
			Step step = steps.peek();
			if (step != null && step.ready.getAsBoolean()) {
				steps.poll();
				step.action.run();
				delay = step.delay;
			}
		} catch (Throwable error) {
			error.printStackTrace();
			failures.add(error.toString());
			finish();
		}
	}

	private void scenario() {
		mc.gui.setScreen(null);
		for (String command : new String[]{"gamerule spawn_mobs false", "gamerule advance_time false", "gamerule advance_weather false",
				"gamerule show_advancement_messages false", "time set 1000", "weather clear"}) {
			command(command);
		}
		List<Slot> slots = new ArrayList<>();
		if (ModList.get().isLoaded(CuriosCompat.MOD_ID)) {
			slots.add(new Slot("curios", player -> stack -> CuriosApi.getCuriosInventory(player)
					.flatMap(h -> h.getStacksHandler(CuriosCompat.SLOT))
					.orElseThrow(() -> new AssertionError("player has no curios back slot"))
					.getStacks().setStackInSlot(0, stack)));
		}
		if (ModList.get().isLoaded(TrinketsCompat.MOD_ID)) {
			slots.add(new Slot("trinkets", player -> stack -> {
				TrinketInventory back = TrinketsApi.getAttachment(player).getInventory(TrinketsCompat.SLOT);
				if (back == null) {
					throw new AssertionError("player has no trinkets chest/back slot");
				}
				back.setItem(0, stack);
			}));
		}
		if (slots.isEmpty()) {
			failures.add("neither Curios nor Trinkets is installed");
		}
		add(10, () -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK));
		for (Slot slot : slots) {
			add(30, () -> server(s -> {
				ServerPlayer player = player(s);
				for (Slot other : slots) {
					other.setter.apply(player).accept(ItemStack.EMPTY);
				}
				player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
				ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("omnivault:iron_backpack")));
				BackpackData data = Backpacks.data(s, stack);
				data.upgrades().setItem(0, new ItemStack(BackpackContent.upgrade(UpgradeType.PICKUP)));
				data.items().setItem(0, new ItemStack(Items.COBBLESTONE, 3));
				slot.setter.apply(player).accept(stack);
				equippedAt = player.tickCount;
			}));
			// The integrated server can lag the client after world load; wait for real player ticks.
			addWhen(() -> serverTicksSinceEquip() >= 25, () -> {
				server(s -> check(slot.name, player(s)));
				screenshot("neoforge_" + slot.name + "_back");
			});
		}
		add(5, this::finish);
	}

	/** OmniVault sees the worn backpack: it ticked, is carried, opens with the key and takes pickups. */
	private void check(String name, ServerPlayer player) {
		// With several accessory mods installed their back slots are numbered one after another.
		List<Integer> worn = BackSlot.backpackSlots(player);
		if (worn.size() != 1) {
			failures.add(name + ": expected one backpack in a back slot, found slots " + worn);
			return;
		}
		int virtual = worn.getFirst();
		ItemStack stack = BackSlot.get(player, virtual);
		BackpackSummary summary = stack.get(BackpackContent.SUMMARY.get());
		if (summary == null || summary.used() < 1) {
			failures.add(name + ": worn backpack did not tick, summary " + summary);
		}
		List<Integer> carried = Backpacks.carriedSlots(player);
		if (!carried.contains(virtual)) {
			failures.add(name + ": back slot backpack is not carried: " + carried);
		}
		int keySlot = Backpacks.slotForKey(player);
		if (keySlot != virtual) {
			failures.add(name + ": backpack key would open slot " + keySlot);
		}
		ItemStack rest = BackpackUpgrades.absorb(player, new ItemStack(Items.DIRT, 5));
		if (!rest.isEmpty()) {
			failures.add(name + ": pickup upgrade left " + rest);
		}
		Backpacks.open(player, new BackpackSource.Held(keySlot));
		if (!(player.containerMenu instanceof BackpackMenu menu) || !menu.stillValid(player)) {
			failures.add(name + ": backpack key did not open the back slot backpack: " + player.containerMenu);
		}
		player.closeContainer();
		System.out.println("[omnivault_trinkets_compat test] checked " + name + " back slot");
	}

	private void finish() {
		done = true;
		boolean passed = failures.isEmpty();
		String message = passed ? "NeoForge back slot tests passed" : String.join("; ", failures);
		try {
			Files.writeString(Path.of(System.getProperty("omnivault_trinkets_compat.test_result")),
					"{\"passed\":" + passed + ",\"message\":\"" + message.replace("\\", "/").replace("\"", "'") + "\"}\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("[omnivault_trinkets_compat test] " + message);
		mc.stop();
	}

	private void screenshot(String name) {
		Screenshot.takeScreenshot(mc.gameRenderer.mainRenderTarget(), image -> {
			try {
				Path directory = Path.of(System.getProperty("omnivault_trinkets_compat.screenshots"));
				Files.createDirectories(directory);
				image.writeToFile(directory.resolve(name + ".png"));
			} catch (Exception error) {
				throw new RuntimeException(error);
			} finally {
				image.close();
			}
		});
	}

	private void add(int delay, Runnable action) {
		steps.add(new Step(delay, () -> true, action));
	}

	private void addWhen(BooleanSupplier ready, Runnable action) {
		steps.add(new Step(0, ready, action));
	}

	private int serverTicksSinceEquip() {
		MinecraftServer server = mc.getSingleplayerServer();
		return CompletableFuture.supplyAsync(() -> player(server).tickCount - equippedAt, server).join();
	}

	private void server(Consumer<MinecraftServer> action) {
		MinecraftServer server = mc.getSingleplayerServer();
		CompletableFuture.runAsync(() -> action.accept(server), server).join();
	}

	private void command(String command) {
		server(s -> s.getCommands().performPrefixedCommand(s.createCommandSourceStack(), command));
	}

	private static ServerPlayer player(MinecraftServer server) {
		return server.getPlayerList().getPlayers().getFirst();
	}
}
