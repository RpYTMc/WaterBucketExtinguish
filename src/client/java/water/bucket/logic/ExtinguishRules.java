package water.bucket.logic;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import water.bucket.config.ModConfig;
import water.bucket.util.HotbarUtils;

public class ExtinguishRules {

    /** True if any block overlapping the player's bounding box is a cobweb. */
    public static boolean isInWeb(MinecraftClient client) {

        var box = client.player.getBoundingBox();

        int minX = (int) Math.floor(box.minX);
        int maxX = (int) Math.floor(box.maxX);
        int minY = (int) Math.floor(box.minY);
        int maxY = (int) Math.floor(box.maxY);
        int minZ = (int) Math.floor(box.minZ);
        int maxZ = (int) Math.floor(box.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (client.world.getBlockState(new BlockPos(x, y, z)).isOf(Blocks.COBWEB)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Fire-mode conditions, independent of which bucket is in the hotbar. */
    public static boolean canExtinguish(MinecraftClient client) {

        var player = client.player;

        if (!player.isOnFire()) return false;
        if (player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) return false;
        if (!player.isOnGround()) return false;

        // Positive pitch = looking down; the water has to land at the feet.
        return player.getPitch() >= ModConfig.INSTANCE.pitchThreshold;
    }

    public static Mode resolveMode(MinecraftClient client) {

        if (client.player == null || client.world == null) return Mode.NONE;

        if (ModConfig.INSTANCE.disableInNether && client.world.getRegistryKey() == World.NETHER) {
            return Mode.NONE;
        }

        boolean hasWater = HotbarUtils.findWaterBucketSlot(client.player) != -1;
        boolean hasEmpty = HotbarUtils.findEmptyBucketSlot(client.player) != -1;

        if (!hasWater && !hasEmpty) return Mode.NONE;

        // Cobweb overrides everything: water to get out, empty bucket to pick it back up.
        if (isInWeb(client)) return Mode.COBWEB;

        if (hasWater && canExtinguish(client)) return Mode.FIRE;

        if (hasEmpty) return Mode.PICKUP;

        return Mode.NONE;
    }
}
