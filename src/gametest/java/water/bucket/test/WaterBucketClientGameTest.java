package water.bucket.test;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import water.bucket.config.ModConfig;
import water.bucket.stats.StatsManager;

import java.util.Arrays;

/**
 * Plays the mod in a real singleplayer world: the panic key is pressed through the
 * normal keyboard handling, the world is changed on the integrated server, and
 * every result is checked on both sides. Same cases as the test on the 26.x
 * branches, written with the Yarn names this branch uses.
 *
 * <ol>
 *   <li>Fire: on fire, looking down, water bucket in slot 4, sword held. One press
 *       must place the water, pick it back up and give the sword back, on the client
 *       and on the server.</li>
 *   <li>Refill: a water source in front, an empty bucket in slot 3 and a water bucket
 *       in slot 4. The press must use the empty bucket, not the full one.</li>
 *   <li>Cobweb: standing in a cobweb with a water bucket, the press places water.</li>
 *   <li>Client commands, the HUD and the config screen opened through Mod Menu.</li>
 * </ol>
 */
public final class WaterBucketClientGameTest implements FabricClientGameTest {

	private static final Logger LOGGER = LoggerFactory.getLogger("waterbucket-test");

	private static final int SWORD_SLOT = 0;
	private static final int EMPTY_SLOT = 3;
	private static final int WATER_SLOT = 4;

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getClientWorld().waitForChunksRender();
			TestServerContext server = world.getServer();
			server.runCommand("gamemode survival @a");
			context.waitFor(client -> client.player != null && client.player.isOnGround(), 200);

			BlockPos feet = server.computeOnServer(s -> player(s).getBlockPos());
			LOGGER.info("[gametest] player feet at {}", feet);

			fireMode(context, server, feet);
			refillMode(context, server, feet);
			cobwebMode(context, server, feet);
			commandsHudAndScreens(context);
		}
	}

	private static void fireMode(ClientGameTestContext context, TestServerContext server, BlockPos feet) {
		server.runOnServer(s -> {
			ServerPlayerEntity p = player(s);
			p.getInventory().clear();
			p.getInventory().setStack(SWORD_SLOT, new ItemStack(Items.DIAMOND_SWORD));
			p.getInventory().setStack(WATER_SLOT, new ItemStack(Items.WATER_BUCKET));
		});
		context.waitFor(client -> clientItem(client, WATER_SLOT) == Items.WATER_BUCKET, 100);
		context.runOnClient(client -> {
			client.player.getInventory().setSelectedSlot(SWORD_SLOT);
			client.player.setPitch(90f);
		});
		server.runOnServer(s -> player(s).setFireTicks(200));
		context.waitFor(client -> client.player.isOnFire(), 100);

		pressPanicKey(context);
		context.waitTicks(10);

		int slot = context.computeOnClient(client -> client.player.getInventory().getSelectedSlot());
		check(slot == SWORD_SLOT, "fire: the sword slot must be selected again on the client, got " + slot);
		check(context.computeOnClient(client -> clientItem(client, WATER_SLOT)) == Items.WATER_BUCKET,
				"fire: the water must be picked back up into slot " + WATER_SLOT);
		check(server.computeOnServer(s -> player(s).getInventory().getSelectedSlot()) == SWORD_SLOT,
				"fire: the server must see the sword slot selected again");
		check(server.computeOnServer(s -> player(s).getEntityWorld().getBlockState(feet).isAir()),
				"fire: no water may be left at the feet");
		check(server.computeOnServer(s -> player(s).getFireTicks() <= 0),
				"fire: the player must not be burning any more");
		int fires = context.computeOnClient(client -> StatsManager.getData().fireExtinguishes);
		check(fires == 1, "fire: one fire extinguish must be counted, got " + fires);
		LOGGER.info("[gametest] fire mode passed");
	}

	private static void refillMode(ClientGameTestContext context, TestServerContext server, BlockPos feet) {
		BlockPos hole = feet.down().east(2);
		server.runOnServer(s -> {
			ServerPlayerEntity p = player(s);
			p.setFireTicks(0);
			p.getEntityWorld().setBlockState(hole, Blocks.WATER.getDefaultState());
			p.getInventory().setStack(EMPTY_SLOT, new ItemStack(Items.BUCKET));
		});
		context.waitFor(client -> clientItem(client, EMPTY_SLOT) == Items.BUCKET, 100);
		context.waitFor(client -> !client.player.isOnFire(), 100);
		// This version of the test API can't aim the camera, so turn the player toward the water.
		context.runOnClient(client -> client.player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, Vec3d.ofCenter(hole)));
		context.waitTicks(2);

		pressPanicKey(context);
		context.waitTicks(5);

		int slot = context.computeOnClient(client -> client.player.getInventory().getSelectedSlot());
		check(slot == SWORD_SLOT, "refill: the sword slot must be selected again, got " + slot);
		check(context.computeOnClient(client -> clientItem(client, EMPTY_SLOT)) == Items.WATER_BUCKET,
				"refill: the empty bucket in slot " + EMPTY_SLOT + " must be filled");
		check(context.computeOnClient(client -> clientItem(client, WATER_SLOT)) == Items.WATER_BUCKET,
				"refill: the full bucket in slot " + WATER_SLOT + " must be left alone");
		check(server.computeOnServer(s -> player(s).getEntityWorld().getBlockState(hole).isAir()),
				"refill: the water source must be gone");
		int pickups = context.computeOnClient(client -> StatsManager.getData().waterPickups);
		check(pickups == 1, "refill: one pickup must be counted, got " + pickups);
		LOGGER.info("[gametest] refill mode passed");
	}

	private static void cobwebMode(ClientGameTestContext context, TestServerContext server, BlockPos feet) {
		server.runOnServer(s -> {
			ServerPlayerEntity p = player(s);
			p.getInventory().setStack(EMPTY_SLOT, ItemStack.EMPTY);
			p.getEntityWorld().setBlockState(feet, Blocks.COBWEB.getDefaultState());
		});
		context.waitFor(client -> clientItem(client, EMPTY_SLOT) == Items.AIR, 100);
		context.waitFor(client -> client.world.getBlockState(feet).isOf(Blocks.COBWEB), 100);
		context.runOnClient(client -> client.player.setPitch(90f));
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
		boolean webGone = server.computeOnServer(s -> !player(s).getEntityWorld().getBlockState(feet).isOf(Blocks.COBWEB));
		LOGGER.info("[gametest] cobweb mode passed (cobweb washed away after 20 ticks: {})", webGone);

		server.runOnServer(s -> {
			ServerPlayerEntity p = player(s);
			p.getEntityWorld().setBlockState(feet, Blocks.AIR.getDefaultState());
			p.getEntityWorld().setBlockState(feet.up(), Blocks.AIR.getDefaultState());
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

		// Clear toasts first: the recipe toast sits exactly where the top-right HUD label is.
		context.runOnClient(client -> {
			client.player.setPitch(20f);
			client.getToastManager().clear();
		});
		context.waitTicks(5);
		LOGGER.info("[gametest] HUD screenshot: {}", context.takeScreenshot("waterbucket-hud-top-right"));

		ModMenuApi modMenu = FabricLoader.getInstance().getEntrypointContainers("modmenu", ModMenuApi.class).stream()
				.filter(container -> container.getProvider().getMetadata().getId().equals("waterbucket"))
				.findFirst()
				.orElseThrow(() -> new AssertionError("waterbucket must register a Mod Menu entrypoint"))
				.getEntrypoint();
		context.setScreen(() -> modMenu.getModConfigScreenFactory().create(null));
		context.waitTicks(5);
		String screen = context.computeOnClient(client -> client.currentScreen == null ? null : client.currentScreen.getClass().getName());
		check(screen != null, "the config screen must open through Mod Menu");
		LOGGER.info("[gametest] config screen {} screenshot: {}", screen, context.takeScreenshot("waterbucket-config-screen"));
		context.setScreen(() -> null);

		int reset = runClientCommand(context, "waterbucket config hudpos reset");
		check(reset == 1 && ModConfig.INSTANCE.hudPosition == ModConfig.HudPosition.TOP_LEFT, "hudpos reset must work");
		LOGGER.info("[gametest] commands, HUD and config screen passed");
	}

	/** Runs a client command the way the chat box does, on the client thread. */
	private static int runClientCommand(ClientGameTestContext context, String command) {
		return context.computeOnClient(client -> {
			try {
				return ClientCommandManager.getActiveDispatcher().execute(command,
						(FabricClientCommandSource) client.getNetworkHandler().getCommandSource());
			} catch (CommandSyntaxException e) {
				throw new AssertionError("/" + command + " was rejected: " + e.getMessage(), e);
			}
		});
	}

	private static void pressPanicKey(ClientGameTestContext context) {
		context.getInput().pressKey(options -> Arrays.stream(options.allKeys)
				.filter(binding -> binding.getId().equals("key.waterbucket.activate"))
				.findFirst()
				.orElseThrow(() -> new AssertionError("the panic key binding must be registered")));
	}

	private static ServerPlayerEntity player(MinecraftServer server) {
		return server.getPlayerManager().getPlayerList().get(0);
	}

	private static Item clientItem(MinecraftClient client, int slot) {
		return client.player.getInventory().getStack(slot).getItem();
	}

	private static void check(boolean condition, String message) {
		if (!condition) throw new AssertionError(message);
	}
}
