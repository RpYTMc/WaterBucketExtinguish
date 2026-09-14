package water.bucket;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import org.lwjgl.glfw.GLFW;

import water.bucket.command.WaterBucketCommand;
import water.bucket.config.ModConfig;
import water.bucket.config.ModConfigScreen;
import water.bucket.hud.HudRenderer;
import water.bucket.logic.ExtinguishRules;
import water.bucket.logic.Mode;
import water.bucket.stats.StatsManager;
import water.bucket.util.HotbarUtils;

public class WaterBucketClient implements ClientModInitializer {

	private static final String WATERBUCKET_CATEGORY = "key.category.waterbucket";

	/** Client ticks between placing the water in fire mode and picking it back up. */
	private static final int PICKUP_DELAY_TICKS = 1;
	/** Minimum ticks between two "can't use" hints so key spam doesn't spam the action bar. */
	private static final int FAIL_HINT_COOLDOWN_TICKS = 12;
	/** Stats are flushed to disk at most this often (200 ticks = 10 s). */
	private static final int STATS_FLUSH_INTERVAL_TICKS = 200;

	private static KeyBinding panicKey;
	private static KeyBinding configKey;

	// Fire mode is a two-step sequence: place now, pick the water back up on a later tick.
	private boolean pickupPending = false;
	private boolean placedThisTick = false;
	private int pickupTimer = 0;
	private int previousSlot = -1;

	private int failHintCooldown = 0;
	private int statsFlushTimer = 0;

	@Override
	public void onInitializeClient() {

		StatsManager.load();

		// Flush unsaved stats when the game closes or the player leaves a world/server,
		// and never carry a half-finished fire sequence into the next world.
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> StatsManager.saveIfDirty());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			resetSequence();
			StatsManager.saveIfDirty();
		});

		HudRenderer.register();

		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) -> WaterBucketCommand.register(dispatcher));

		configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.waterbucket.config",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_P,
				WATERBUCKET_CATEGORY));

		panicKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.waterbucket.activate",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_BRACKET,
				WATERBUCKET_CATEGORY));

		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
	}

	private void onClientTick(MinecraftClient client) {

		if (client.player == null || client.interactionManager == null || client.world == null) {
			return;
		}

		if (++statsFlushTimer >= STATS_FLUSH_INTERVAL_TICKS) {
			statsFlushTimer = 0;
			StatsManager.saveIfDirty();
		}

		if (failHintCooldown > 0) failHintCooldown--;

		while (configKey.wasPressed()) {
			client.setScreen(ModConfigScreen.create(client.currentScreen));
		}

		while (panicKey.wasPressed()) {
			if (pickupPending) continue; // still finishing the previous fire sequence
			activate(client);
		}

		tickPendingPickup(client);
	}

	private void activate(MinecraftClient client) {

		Mode mode = ExtinguishRules.resolveMode(client);

		if (mode == Mode.NONE) {
			explainFailure(client);
			return;
		}

		int bucketSlot = HotbarUtils.findBucketSlotFor(client.player, mode);
		if (bucketSlot == -1) return; // resolveMode already guarantees a bucket; purely defensive

		previousSlot = client.player.getInventory().selectedSlot;
		selectSlot(client, bucketSlot);

		UseResult result = useHeldItem(client);

		switch (mode) {

			case COBWEB -> {
				if (result.placedWater()) {
					StatsManager.incrementWeb();
					StatsManager.incrementActivation();
					playFeedbackSound(client, SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), 0.7f, 1.2f);
				} else if (result.pickedUpWater()) {
					StatsManager.incrementPickup();
					StatsManager.incrementActivation();
					playFeedbackSound(client, SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), 0.7f, 1.2f);
				}
				restoreSlot(client);
			}

			case FIRE -> {
				if (result.placedWater()) {
					StatsManager.incrementFire();
					StatsManager.incrementActivation();
					playFeedbackSound(client, SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), 0.7f, 1.2f);
					sendActionBar(client, "Poof");

					// Keep the bucket selected; the pickup follows on a later tick.
					pickupPending = true;
					placedThisTick = true;
					pickupTimer = PICKUP_DELAY_TICKS;
				} else {
					// Nothing was placed (blocked spot, water already there, ...):
					// don't leave the player holding the bucket instead of their weapon.
					restoreSlot(client);
				}
			}

			case PICKUP -> {
				if (result.pickedUpWater()) {
					StatsManager.incrementPickup();
					StatsManager.incrementActivation();
				}
				restoreSlot(client);
			}

			case NONE -> restoreSlot(client);
		}
	}

	private void tickPendingPickup(MinecraftClient client) {

		if (!pickupPending) return;

		// The tick the water was placed in doesn't count towards the delay.
		if (placedThisTick) {
			placedThisTick = false;
			return;
		}

		if (--pickupTimer > 0) return;

		UseResult result = useHeldItem(client);

		if (result.pickedUpWater()) {
			playFeedbackSound(client, SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), 0.6f, 1.0f);
		}

		restoreSlot(client);
		pickupPending = false;
	}

	private void resetSequence() {
		pickupPending = false;
		placedThisTick = false;
		pickupTimer = 0;
		previousSlot = -1;
	}

	// ------------------------------------------------------------------
	// Interaction helpers
	// ------------------------------------------------------------------

	/**
	 * Switches the held hotbar slot exactly like a vanilla scroll does: only the
	 * client-side selection changes here.
	 *
	 * The game sends the UpdateSelectedSlot packet itself right before the next
	 * interaction ({@code ClientPlayerInteractionManager#interactItem} calls
	 * {@code syncSelectedSlot}) and again on the next client tick for the restore,
	 * so the server sees the same packet sequence a normal player produces.
	 *
	 * Do NOT send {@code UpdateSelectedSlotC2SPacket} manually here: together with
	 * the vanilla sync that yields two identical slot packets in a row, which
	 * anti-cheats (Grim, Frequency, ...) flag as impossible for a vanilla client.
	 */
	private static void selectSlot(MinecraftClient client, int slot) {
		if (slot < 0 || slot > 8) return;
		if (client.player.getInventory().selectedSlot == slot) return;
		client.player.getInventory().selectedSlot = slot;
	}

	private void restoreSlot(MinecraftClient client) {
		if (previousSlot != -1) {
			selectSlot(client, previousSlot);
			previousSlot = -1;
		}
	}

	/** One right-click with the held item, through the vanilla interaction manager. */
	private static UseResult useHeldItem(MinecraftClient client) {
		Item before = client.player.getMainHandStack().getItem();
		client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
		Item after = client.player.getMainHandStack().getItem();
		return new UseResult(before, after);
	}

	private record UseResult(Item before, Item after) {
		boolean placedWater() {
			return before == Items.WATER_BUCKET && after == Items.BUCKET;
		}

		boolean pickedUpWater() {
			return before == Items.BUCKET && after == Items.WATER_BUCKET;
		}
	}

	// ------------------------------------------------------------------
	// Feedback
	// ------------------------------------------------------------------

	private void explainFailure(MinecraftClient client) {

		if (failHintCooldown > 0) return;
		failHintCooldown = FAIL_HINT_COOLDOWN_TICKS;

		var player = client.player;
		String reason;

		if (ModConfig.INSTANCE.disableInNether && client.world.getRegistryKey() == World.NETHER) {
			reason = "Disabled in Nether";
		} else if (HotbarUtils.findBucketSlot(player) == -1) {
			reason = "No bucket in hotbar";
		} else if (!player.isOnFire()) {
			reason = "Not on fire";
		} else if (player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
			reason = "Fire Resistance active";
		} else if (!player.isOnGround()) {
			reason = "Must be on ground";
		} else if (player.getPitch() < ModConfig.INSTANCE.pitchThreshold) {
			reason = "Look down to place water";
		} else {
			reason = "Can't use WaterBucket";
		}

		playFeedbackSound(client, SoundEvents.UI_BUTTON_CLICK.value(), 0.45f, 0.8f);
		sendActionBar(client, reason);
	}

	private static void playFeedbackSound(MinecraftClient client, SoundEvent sound, float volume, float pitch) {
		if (!ModConfig.INSTANCE.enableSound) return;
		client.player.playSound(sound, volume, pitch);
	}

	private static void sendActionBar(MinecraftClient client, String message) {
		if (!ModConfig.INSTANCE.enableActionBar) return;
		client.player.sendMessage(Text.literal(message).formatted(Formatting.GRAY, Formatting.ITALIC), true);
	}
}
