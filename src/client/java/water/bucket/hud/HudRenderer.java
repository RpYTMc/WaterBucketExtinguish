package water.bucket.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

import water.bucket.config.ModConfig;
import water.bucket.util.HotbarUtils;

public class HudRenderer {

    private static final int MARGIN = 5;
    private static final int LINE_HEIGHT = 10;

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> {

            if (!ModConfig.INSTANCE.enableHud) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            // Respect F1 (hidden HUD) and stay out of the way of the F3 debug screen.
            if (client.options.hudHidden) return;
            if (client.getDebugHud().shouldShowDebugHud()) return;

            HotbarUtils.BucketStatus status = HotbarUtils.status(client.player);

            String statusText = switch (status) {
                case READY -> "✔ Bucket Ready";
                case EMPTY -> "⚠ Bucket Empty";
                case INVENTORY -> "⚠ Bucket in Inventory";
                case NONE -> "✖ No Bucket";
            };
            int statusColor = switch (status) {
                case READY -> 0xFF55FF55;
                case EMPTY -> 0xFFFFAA00;
                case INVENTORY -> 0xFFFFFF55;
                case NONE -> 0xFFFF5555;
            };

            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();
            int textWidth = client.textRenderer.getWidth(statusText);

            int x = switch (ModConfig.INSTANCE.hudPosition) {
                case TOP_LEFT, BOTTOM_LEFT -> MARGIN;
                case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - textWidth - MARGIN;
            };
            int y = switch (ModConfig.INSTANCE.hudPosition) {
                case TOP_LEFT, TOP_RIGHT -> MARGIN;
                case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - MARGIN - LINE_HEIGHT;
            };

            context.drawText(client.textRenderer, statusText, x, y, statusColor, true);
        });
    }
}
