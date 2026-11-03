package water.bucket.test;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import water.bucket.config.ModConfig;
import water.bucket.stats.StatsManager;
import water.bucket.util.HotbarUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * Plays the mod in a real singleplayer world: the panic key is pressed through the
 * normal keyboard handling, the world is changed on the integrated server, and
 * every result is checked on both sides.
 *
 * <ol>
 *   <li>Fire: on fire, looking down, water bucket in slot 4, sword held. One press
 *       must place the water, pick it back up and give the sword back, on the client
 *       and on the server.</li>
 *   <li>Refill: a water source in front, an empty bucket in slot 3 and a water bucket
 *       in slot 4. The press must use the empty bucket, not the full one.</li>
 *   <li>Cobweb: standing in a cobweb with a water bucket, the press places water.</li>
 *   <li>Client commands, the HUD and the settings: with Cloth Config the Mod Menu
 *       button and the settings key open the config screen, without it neither opens
 *       a screen.</li>
 *   <li>Bucket status shown by the HUD: water bucket in the hotbar, only an empty one
 *       there, a bucket only in the main inventory, and no bucket.</li>
 * </ol>
 */
public final class WaterBucketClientGameTest implements FabricClientGameTest {

	private static final Logger LOGGER = LoggerFactory.getLogger("waterbucket-test");

	private static final int SWORD_SLOT = 0;
	private static final int EMPTY_SLOT = 3;
	private static final int WATER_SLOT = 4;
	/** A main-inventory slot, outside the hotbar. */
	private static final int INVENTORY_SLOT = 20;

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getClientLevel().waitForChunksRender();
			TestServerContext server = world.getServer();
			server.runCommand("gamemode survival @a");
			context.waitFor(client -> client.player != null && client.player.onGround(), 200);

			BlockPos feet = server.computeOnServer(s -> player(s).blockPosition());
			LOGGER.info("[gametest] player feet at {}", feet);

			fireMode(context, server, feet);
			refillMode(context, server, feet);
			cobwebMode(context, server, feet);
			commandsHudAndScreens(context);
			bucketStatus(context, server);
		}
	}

	private static void fireMode(ClientGameTestContext context, TestServerContext server, BlockPos feet) {
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			p.getInventory().clearContent();
			p.getInventory().setItem(SWORD_SLOT, new ItemStack(Items.DIAMOND_SWORD));
			p.getInventory().setItem(WATER_SLOT, new ItemStack(Items.WATER_BUCKET));
		});
		context.waitFor(client -> clientItem(client, WATER_SLOT) == Items.WATER_BUCKET, 100);
		context.runOnClient(client -> {
			client.player.getInventory().setSelectedSlot(SWORD_SLOT);
			client.player.setXRot(90f);
		});
		server.runOnServer(s -> player(s).setRemainingFireTicks(200));
		context.waitFor(client -> client.player.isOnFire(), 100);

		pressPanicKey(context);
		context.waitTicks(10);

		int slot = context.computeOnClient(client -> client.player.getInventory().getSelectedSlot());
		check(slot == SWORD_SLOT, "fire: the sword slot must be selected again on the client, got " + slot);
		check(context.computeOnClient(client -> clientItem(client, WATER_SLOT)) == Items.WATER_BUCKET,
				"fire: the water must be picked back up into slot " + WATER_SLOT);
		check(server.computeOnServer(s -> player(s).getInventory().getSelectedSlot()) == SWORD_SLOT,
				"fire: the server must see the sword slot selected again");
		check(server.computeOnServer(s -> player(s).level().getBlockState(feet).isAir()),
				"fire: no water may be left at the feet");
		check(server.computeOnServer(s -> player(s).getRemainingFireTicks() <= 0),
				"fire: the player must not be burning any more");
		int fires = context.computeOnClient(client -> StatsManager.getData().fireExtinguishes);
		check(fires == 1, "fire: one fire extinguish must be counted, got " + fires);
		LOGGER.info("[gametest] fire mode passed");
	}

	private static void refillMode(ClientGameTestContext context, TestServerContext server, BlockPos feet) {
		BlockPos hole = feet.below().east(2);
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			p.setRemainingFireTicks(0);
			p.level().setBlockAndUpdate(hole, Blocks.WATER.defaultBlockState());
			p.getInventory().setItem(EMPTY_SLOT, new ItemStack(Items.BUCKET));
		});
		context.waitFor(client -> clientItem(client, EMPTY_SLOT) == Items.BUCKET, 100);
		context.waitFor(client -> !client.player.isOnFire(), 100);
		context.getInput().lookAt(hole);
		context.waitTicks(2);

		pressPanicKey(context);
		context.waitTicks(5);

		int slot = context.computeOnClient(client -> client.player.getInventory().getSelectedSlot());
		check(slot == SWORD_SLOT, "refill: the sword slot must be selected again, got " + slot);
		check(context.computeOnClient(client -> clientItem(client, EMPTY_SLOT)) == Items.WATER_BUCKET,
				"refill: the empty bucket in slot " + EMPTY_SLOT + " must be filled");
		check(context.computeOnClient(client -> clientItem(client, WATER_SLOT)) == Items.WATER_BUCKET,
				"refill: the full bucket in slot " + WATER_SLOT + " must be left alone");
		check(server.computeOnServer(s -> player(s).level().getBlockState(hole).isAir()),
				"refill: the water source must be gone");
		int pickups = context.computeOnClient(client -> StatsManager.getData().waterPickups);
		check(pickups == 1, "refill: one pickup must be counted, got " + pickups);
		LOGGER.info("[gametest] refill mode passed");
	}

	private static void cobwebMode(ClientGameTestContext context, TestServerContext server, BlockPos feet) {
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			p.getInventory().setItem(EMPTY_SLOT, ItemStack.EMPTY);
			p.level().setBlockAndUpdate(feet, Blocks.COBWEB.defaultBlockState());
		});
		context.waitFor(client -> clientItem(client, EMPTY_SLOT) == Items.AIR, 100);
		context.waitFor(client -> client.level.getBlockState(feet).is(Blocks.COBWEB), 100);
		context.runOnClient(client -> client.player.setXRot(90f));
		context.waitTicks(2);

		pressPanicKey(context);
		context.waitTicks(5);

		int slot = context.computeOnClient(client -> client.player.getInventory().getSelectedSlot());
		check(slot == SWORD_SLOT, "cobweb: the sword slot must be selected again, got " + slot);
		check(context.computeOnClient(client -> clientItem(client, WATER_SLOT)) == Items.BUCKET,
				"cobweb: the water bucket must have been emptied");
		int webs = context.computeOnClient(client -> StatsManager.getData().webPlacements);
		check(webs == 1, "cobweb: one web placement must be counted, got " + webs);

		context.waitTicks(20);
		boolean webGone = server.computeOnServer(s -> !player(s).level().getBlockState(feet).is(Blocks.COBWEB));
		LOGGER.info("[gametest] cobweb mode passed (cobweb washed away after 20 ticks: {})", webGone);

		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			p.level().setBlockAndUpdate(feet, Blocks.AIR.defaultBlockState());
			p.level().setBlockAndUpdate(feet.above(), Blocks.AIR.defaultBlockState());
		});
		context.waitTicks(5);
	}

	private static void commandsHudAndScreens(ClientGameTestContext context) {
		for (String command : new String[] {"waterbucket", "waterbucket help", "waterbucket bucket", "waterbucket stats",
				"waterbucket config", "waterbucket config hudpos top_right"}) {
			int result = runClientCommand(context, command);
			check(result == 1, "/" + command + " must succeed, returned " + result);
		}
		check(ModConfig.INSTANCE.hudPosition == ModConfig.HudPosition.TOP_RIGHT, "hudpos top_right must be applied");
		int activations = context.computeOnClient(client -> StatsManager.getData().totalActivations);
		check(activations == 3, "three activations must be counted, got " + activations);

		context.runOnClient(client -> client.player.setXRot(20f));
		context.waitTicks(5);
		LOGGER.info("[gametest] HUD screenshot: {}", context.takeScreenshot("waterbucket-hud-top-right"));

		ModMenuApi modMenu = FabricLoader.getInstance().getEntrypointContainers("modmenu", ModMenuApi.class).stream()
				.filter(container -> container.getProvider().getMetadata().getId().equals("waterbucket"))
				.findFirst()
				.orElseThrow(() -> new AssertionError("waterbucket must register a Mod Menu entrypoint"))
				.getEntrypoint();

		// Cloth Config is optional: the settings screen exists only when it is installed.
		boolean cloth = FabricLoader.getInstance().isModLoaded("cloth-config");
		if (cloth) {
			context.setScreen(() -> modMenu.getModConfigScreenFactory().create(null));
			context.waitTicks(5);
			String screen = currentScreen(context);
			check(screen != null, "the config screen must open through Mod Menu");
			LOGGER.info("[gametest] config screen {} screenshot: {}", screen, context.takeScreenshot("waterbucket-config-screen"));
			context.setScreen(() -> null);
			context.waitTicks(2);

			pressKey(context, "key.waterbucket.config");
			context.waitTicks(5);
			String keyScreen = currentScreen(context);
			check(keyScreen != null, "the settings key must open the config screen");
			context.setScreen(() -> null);
			context.waitTicks(2);
			LOGGER.info("[gametest] with Cloth Config: Mod Menu and the settings key opened {}", keyScreen);
		} else {
			boolean noFactoryScreen = context.computeOnClient(client -> modMenu.getModConfigScreenFactory().create(null) == null);
			check(noFactoryScreen, "without Cloth Config the Mod Menu factory must give no screen");

			pressKey(context, "key.waterbucket.config");
			context.waitTicks(5);
			String keyScreen = currentScreen(context);
			check(keyScreen == null, "without Cloth Config the settings key must not open a screen, got " + keyScreen);
			LOGGER.info("[gametest] without Cloth Config: no settings screen from Mod Menu or the settings key, screenshot: {}",
					context.takeScreenshot("waterbucket-settings-key-no-cloth"));
		}

		int reset = runClientCommand(context, "waterbucket config hudpos reset");
		check(reset == 1 && ModConfig.INSTANCE.hudPosition == ModConfig.HudPosition.TOP_LEFT, "hudpos reset must work");
		LOGGER.info("[gametest] commands, HUD and config screen passed");
	}

	private static void bucketStatus(ClientGameTestContext context, TestServerContext server) {
		expectBucketStatus(context, server, Map.of(WATER_SLOT, Items.WATER_BUCKET, EMPTY_SLOT, Items.BUCKET),
				HotbarUtils.BucketStatus.READY);
		// An empty bucket can't put out fire, even with a water bucket elsewhere in the inventory.
		expectBucketStatus(context, server, Map.of(EMPTY_SLOT, Items.BUCKET, INVENTORY_SLOT, Items.WATER_BUCKET),
				HotbarUtils.BucketStatus.EMPTY);
		LOGGER.info("[gametest] HUD with only an empty bucket screenshot: {}", context.takeScreenshot("waterbucket-hud-empty"));
		expectBucketStatus(context, server, Map.of(INVENTORY_SLOT, Items.WATER_BUCKET), HotbarUtils.BucketStatus.INVENTORY);
		expectBucketStatus(context, server, Map.of(INVENTORY_SLOT, Items.BUCKET), HotbarUtils.BucketStatus.INVENTORY);
		expectBucketStatus(context, server, Map.of(), HotbarUtils.BucketStatus.NONE);
		LOGGER.info("[gametest] bucket status passed");
	}

	/** Gives the player exactly these items (slot to item) on the server, then checks the status on the client. */
	private static void expectBucketStatus(ClientGameTestContext context, TestServerContext server,
			Map<Integer, Item> items, HotbarUtils.BucketStatus expected) {
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			p.getInventory().clearContent();
			items.forEach((slot, item) -> p.getInventory().setItem(slot, new ItemStack(item)));
		});
		context.waitFor(client -> {
			for (int i = 0; i < client.player.getInventory().getContainerSize(); i++) {
				if (clientItem(client, i) != items.getOrDefault(i, Items.AIR)) return false;
			}
			return true;
		}, 100);
		context.waitTicks(2);

		HotbarUtils.BucketStatus status = context.computeOnClient(client -> HotbarUtils.status(client.player));
		check(status == expected, "bucket status with " + items + ": expected " + expected + ", got " + status);
		int result = runClientCommand(context, "waterbucket bucket");
		check(result == 1, "/waterbucket bucket must succeed with status " + expected + ", returned " + result);
		LOGGER.info("[gametest] bucket status {} ok with {}", expected, items);
	}

	/** Runs a client command the way the chat box does, on the client thread. */
	private static int runClientCommand(ClientGameTestContext context, String command) {
		return context.computeOnClient(client -> {
			try {
				return ClientCommands.getActiveDispatcher().execute(command,
						(FabricClientCommandSource) client.getConnection().getSuggestionsProvider());
			} catch (CommandSyntaxException e) {
				throw new AssertionError("/" + command + " was rejected: " + e.getMessage(), e);
			}
		});
	}

	private static void pressPanicKey(ClientGameTestContext context) {
		pressKey(context, "key.waterbucket.activate");
	}

	/** Presses the key bound to this key mapping name through the normal keyboard handling. */
	private static void pressKey(ClientGameTestContext context, String name) {
		context.getInput().pressKey(options -> Arrays.stream(options.keyMappings)
				.filter(mapping -> mapping.getName().equals(name))
				.findFirst()
				.orElseThrow(() -> new AssertionError("the key mapping " + name + " must be registered")));
	}

	/** Class name of the open screen, or null when none is open. */
	private static String currentScreen(ClientGameTestContext context) {
		return context.computeOnClient(client -> client.screen == null ? null : client.screen.getClass().getName());
	}

	private static ServerPlayer player(MinecraftServer server) {
		return server.getPlayerList().getPlayers().get(0);
	}

	private static Item clientItem(Minecraft client, int slot) {
		return client.player.getInventory().getItem(slot).getItem();
	}

	private static void check(boolean condition, String message) {
		if (!condition) throw new AssertionError(message);
	}
}
