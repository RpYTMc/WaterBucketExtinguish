package water.bucket.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

import water.bucket.config.ModConfig;
import water.bucket.util.HotbarUtils;

public class HudRenderer {

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> {

            if (!ModConfig.INSTANCE.enableHud) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            // ===== STATIC BUCKET STATUS =====
            int hotbarSlot = HotbarUtils.findBucketSlot(client.player);

            boolean hasHotbarBucket = hotbarSlot != -1;
            boolean hasInventoryBucket = false;

            for (int i = 0; i < client.player.getInventory().size(); i++) {
                if (client.player.getInventory().getStack(i).isOf(Items.WATER_BUCKET)) {
                    hasInventoryBucket = true;
                    break;
                }
            }

            String statusText;
            int statusColor;

            if (hasHotbarBucket) {
                statusText = "✔ Bucket Ready";
                statusColor = 0xFF55FF55; // green
            }
            else if (hasInventoryBucket) {
                statusText = "⚠ Bucket in Inventory";
                statusColor = 0xFFFFFF55; // yellow
            }
            else {
                statusText = "✖ No Bucket";
                statusColor = 0xFFFF5555; // red
            }

            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            int x=5;
            int y=5;

            switch (ModConfig.INSTANCE.hudPosition) {
                case TOP_LEFT -> {
                    x = 5;
                    y = 5;
                }
                case TOP_RIGHT -> {
                    x = screenWidth - client.textRenderer.getWidth(statusText) - 5;
                    y = 5;
                }
                case BOTTOM_LEFT -> {
                    x = 5;
                    y = screenHeight - 15;
                }
                case BOTTOM_RIGHT -> {
                    x = screenWidth - client.textRenderer.getWidth(statusText) - 5;
                    y = screenHeight - 15;
                }
            }
            context.drawText(
                    client.textRenderer,
                    statusText,
                    x,
                    y,
                    statusColor,
                    true
            );
        });
    }
}