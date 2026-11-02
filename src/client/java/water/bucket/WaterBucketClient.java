package water.bucket;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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

	private static final KeyMapping.Category WATERBUCKET_CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath("waterbucket", "general"));

	/** Client ticks between placing the water in fire mode and picking it back up. */
	private static final int PICKUP_DELAY_TICKS = 1;
	/** Minimum ticks between two "can't use" hints so key spam doesn't spam the action bar. */
	private static final int FAIL_HINT_COOLDOWN_TICKS = 12;
	/** Stats are flushed to disk at most this often (200 ticks = 10 s). */
	private static final int STATS_FLUSH_INTERVAL_TICKS = 200;

	private static KeyMapping panicKey;
	private static KeyMapping configKey;

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

		configKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.waterbucket.config",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_P,
				WATERBUCKET_CATEGORY));

		panicKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.waterbucket.activate",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_BRACKET,
				WATERBUCKET_CATEGORY));

		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
	}

	private void onClientTick(Minecraft client) {

		if (client.player == null || client.gameMode == null || client.level == null) {
			return;
		}

		if (++statsFlushTimer >= STATS_FLUSH_INTERVAL_TICKS) {
			statsFlushTimer = 0;
			StatsManager.saveIfDirty();
		}

		if (failHintCooldown > 0) failHintCooldown--;

		while (configKey.consumeClick()) {
			client.setScreen(ModConfigScreen.create(client.screen));
		}

		while (panicKey.consumeClick()) {
			if (pickupPending) continue; // still finishing the previous fire sequence
			activate(client);
		}

		tickPendingPickup(client);
	}

	private void activate(Minecraft client) {

		Mode mode = ExtinguishRules.resolveMode(client);

		if (mode == Mode.NONE) {
			explainFailure(client);
			return;
		}

		int bucketSlot = HotbarUtils.findBucketSlotFor(client.player, mode);
		if (bucketSlot == -1) return; // resolveMode already guarantees a bucket; purely defensive

		previousSlot = client.player.getInventory().getSelectedSlot();
		selectSlot(client, bucketSlot);

		UseResult result = useHeldItem(client);

		switch (mode) {

			case COBWEB -> {
				if (result.placedWater()) {
					StatsManager.incrementWeb();
					StatsManager.incrementActivation();
					playFeedbackSound(client, SoundEvents.NOTE_BLOCK_HAT.value(), 0.7f, 1.2f);
				} else if (result.pickedUpWater()) {
					StatsManager.incrementPickup();
					StatsManager.incrementActivation();
					playFeedbackSound(client, SoundEvents.NOTE_BLOCK_HAT.value(), 0.7f, 1.2f);
				}
				restoreSlot(client);
			}

			case FIRE -> {
				if (result.placedWater()) {
					StatsManager.incrementFire();
					StatsManager.incrementActivation();
					playFeedbackSound(client, SoundEvents.NOTE_BLOCK_HAT.value(), 0.7f, 1.2f);
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

	private void tickPendingPickup(Minecraft client) {

		if (!pickupPending) return;

		// The tick the water was placed in doesn't count towards the delay.
		if (placedThisTick) {
			placedThisTick = false;
			return;
		}

		if (--pickupTimer > 0) return;

		UseResult result = useHeldItem(client);

		if (result.pickedUpWater()) {
			playFeedbackSound(client, SoundEvents.NOTE_BLOCK_HAT.value(), 0.6f, 1.0f);
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
	private static void selectSlot(Minecraft client, int slot) {
		if (slot < 0 || slot > 8) return;
		if (client.player.getInventory().getSelectedSlot() == slot) return;
		client.player.getInventory().setSelectedSlot(slot);
	}

	private void restoreSlot(Minecraft client) {
		if (previousSlot != -1) {
			selectSlot(client, previousSlot);
			previousSlot = -1;
		}
	}

	/** One right-click with the held item, through the vanilla interaction manager. */
	private static UseResult useHeldItem(Minecraft client) {
		Item before = client.player.getMainHandItem().getItem();
		client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
		Item after = client.player.getMainHandItem().getItem();
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

	private void explainFailure(Minecraft client) {

		if (failHintCooldown > 0) return;
		failHintCooldown = FAIL_HINT_COOLDOWN_TICKS;

		var player = client.player;
		String reason;

		if (ModConfig.INSTANCE.disableInNether && client.level.dimension() == Level.NETHER) {
			reason = "Disabled in Nether";
		} else if (HotbarUtils.findBucketSlot(player) == -1) {
			reason = "No bucket in hotbar";
		} else if (!player.isOnFire()) {
			reason = "Not on fire";
		} else if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
			reason = "Fire Resistance active";
		} else if (!player.onGround()) {
			reason = "Must be on ground";
		} else if (player.getXRot() < ModConfig.INSTANCE.pitchThreshold) {
			reason = "Look down to place water";
		} else {
			reason = "Can't use WaterBucket";
		}

		playFeedbackSound(client, SoundEvents.UI_BUTTON_CLICK.value(), 0.45f, 0.8f);
		sendActionBar(client, reason);
	}

	private static void playFeedbackSound(Minecraft client, SoundEvent sound, float volume, float pitch) {
		if (!ModConfig.INSTANCE.enableSound) return;
		client.player.playSound(sound, volume, pitch);
	}

	private static void sendActionBar(Minecraft client, String message) {
		if (!ModConfig.INSTANCE.enableActionBar) return;
		client.player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
	}
}
