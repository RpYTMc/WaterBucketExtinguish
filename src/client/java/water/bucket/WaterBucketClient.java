package water.bucket;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import org.lwjgl.glfw.GLFW;

import water.bucket.config.ModConfig;
import water.bucket.hud.HudRenderer;
import water.bucket.logic.Mode;
import water.bucket.util.HotbarUtils;
import water.bucket.logic.ExtinguishRules;
import water.bucket.command.WaterBucketCommand;
import water.bucket.stats.StatsManager;

public class WaterBucketClient implements ClientModInitializer {

	private static KeyBinding panicKey;
	private static KeyBinding configKey;

	private int clickTimer = 0;
	private boolean secondClickPending = false;
	private int previousSlot = -1;

	private static final KeyBinding.Category WATERBUCKET_CATEGORY =
			KeyBinding.Category.create(
					Identifier.of("waterbucket", "general")
			);


	@Override
	public void onInitializeClient() {

		StatsManager.load();

		HudRenderer.register();

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			WaterBucketCommand.register(dispatcher);
		});

		configKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding(
						"key.waterbucket.config",
						InputUtil.Type.KEYSYM,
						GLFW.GLFW_KEY_P,
						WATERBUCKET_CATEGORY
				)
		);

		panicKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding(
						"key.waterbucket.activate",
						InputUtil.Type.KEYSYM,
						GLFW.GLFW_KEY_RIGHT_BRACKET,
						WATERBUCKET_CATEGORY
				)
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (client.player == null || client.interactionManager == null || client.world == null)
				return;

			while (configKey.wasPressed()) {
				client.setScreen(
						water.bucket.config.ModConfigScreen.create(client.currentScreen)
				);
			}

			while (panicKey.wasPressed()) {

				if (secondClickPending) continue;

				int currentSlot = client.player.getInventory().getSelectedSlot();
				int bucketSlot = HotbarUtils.findBucketSlot(client.player);

				Mode mode = ExtinguishRules.resolveMode(client);

				switch (mode) {

					case COBWEB -> {

						previousSlot = currentSlot;

						client.player.getInventory().setSelectedSlot(bucketSlot);

						var before = client.player.getMainHandStack().getItem();

						client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);

						var after = client.player.getMainHandStack().getItem();

						// Placement (water bucket -> empty bucket)
						if (before == Items.WATER_BUCKET && after == Items.BUCKET) {

							StatsManager.incrementWeb();
							StatsManager.incrementActivation();

							if (ModConfig.INSTANCE.enableSound) {
								client.player.playSound(
										SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(),
										0.7f,
										1.2f
								);
							}
						}

						// Pickup (empty bucket -> water bucket)
						if (before == Items.BUCKET && after == Items.WATER_BUCKET) {

							StatsManager.incrementPickup();

							if (!secondClickPending) {
								StatsManager.incrementActivation();
							}

							if (ModConfig.INSTANCE.enableSound) {
								client.player.playSound(
										SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(),
										0.7f,
										1.2f
								);
							}
						}

						client.player.getInventory().setSelectedSlot(previousSlot);
					}

					case FIRE -> {

						previousSlot = currentSlot;

						client.player.getInventory().setSelectedSlot(bucketSlot);

						var before = client.player.getMainHandStack().getItem();

						client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);

						var after = client.player.getMainHandStack().getItem();

						if (before == Items.WATER_BUCKET && after == Items.BUCKET) {

							StatsManager.incrementFire();
							StatsManager.incrementActivation();

							if (ModConfig.INSTANCE.enableSound) {
								client.player.playSound(
										SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(),
										0.7f,
										1.2f
								);
							}

							if (ModConfig.INSTANCE.enableActionBar) {
								client.player.sendMessage(
										Text.literal("Poof")
												.formatted(Formatting.ITALIC, Formatting.GRAY),
										true
								);
							}

							clickTimer = 2;
							secondClickPending = true;
						}
					}

					case PICKUP -> {

						previousSlot = currentSlot;

						client.player.getInventory().setSelectedSlot(bucketSlot);

						var before = client.player.getMainHandStack().getItem();

						client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);

						var after = client.player.getMainHandStack().getItem();

						if (before == Items.BUCKET && after == Items.WATER_BUCKET) {
							StatsManager.incrementPickup();
							StatsManager.incrementActivation();
						}

						client.player.getInventory().setSelectedSlot(previousSlot);
					}

					case NONE -> {
						// do nothing
					}
				}

			}

			if (secondClickPending) {

				clickTimer--;

				if (clickTimer <= 0) {

					// Second click (pickup)
					var before = client.player.getMainHandStack().getItem();

					client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);

					var after = client.player.getMainHandStack().getItem();

					if (before == Items.BUCKET && after == Items.WATER_BUCKET) {

						if (ModConfig.INSTANCE.enableSound) {
							client.player.playSound(
									SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(),
									0.6f,
									1.0f
							);
						}
					}

					if (previousSlot != -1) {
						client.player.getInventory().setSelectedSlot(previousSlot);
						previousSlot = -1;
					}

					secondClickPending = false;
				}
			}
		});
	}
}