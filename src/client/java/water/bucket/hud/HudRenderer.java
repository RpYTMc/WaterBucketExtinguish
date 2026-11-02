package water.bucket.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import water.bucket.config.ModConfig;
import water.bucket.util.HotbarUtils;

public class HudRenderer {

    private static final int MARGIN = 5;
    private static final int LINE_HEIGHT = 10;

    private static final Identifier ELEMENT_ID =
            Identifier.fromNamespaceAndPath("waterbucket", "bucket_status");

    public static void register() {
        // Drawn after every vanilla HUD element, like the old HudRenderCallback was.
        HudElementRegistry.addLast(ELEMENT_ID, (context, tickCounter) -> {

            if (!ModConfig.INSTANCE.enableHud) return;

            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;

            // Respect F1 (hidden HUD) and stay out of the way of the F3 debug screen.
            if (client.gui.hud.isHidden()) return;
            if (client.getDebugOverlay().showDebugScreen()) return;

            boolean hasHotbarBucket = HotbarUtils.findBucketSlot(client.player) != -1;
            boolean hasInventoryBucket = false;

            for (int i = 0; i < client.player.getInventory().getContainerSize(); i++) {
                var stack = client.player.getInventory().getItem(i);
                if (stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET)) {
                    hasInventoryBucket = true;
                    break;
                }
            }

            String statusText;
            int statusColor;

            if (hasHotbarBucket) {
                statusText = "✔ Bucket Ready";
                statusColor = 0xFF55FF55;
            } else if (hasInventoryBucket) {
                statusText = "⚠ Bucket in Inventory";
                statusColor = 0xFFFFFF55;
            } else {
                statusText = "✖ No Bucket";
                statusColor = 0xFFFF5555;
            }

            int screenWidth = client.getWindow().getGuiScaledWidth();
            int screenHeight = client.getWindow().getGuiScaledHeight();
            int textWidth = client.font.width(statusText);

            int x = switch (ModConfig.INSTANCE.hudPosition) {
                case TOP_LEFT, BOTTOM_LEFT -> MARGIN;
                case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - textWidth - MARGIN;
            };
            int y = switch (ModConfig.INSTANCE.hudPosition) {
                case TOP_LEFT, TOP_RIGHT -> MARGIN;
                case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - MARGIN - LINE_HEIGHT;
            };

            context.text(client.font, statusText, x, y, statusColor, true);
        });
    }
}
