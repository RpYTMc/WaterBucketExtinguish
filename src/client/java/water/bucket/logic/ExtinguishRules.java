package water.bucket.logic;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

import water.bucket.util.HotbarUtils;
import water.bucket.config.ModConfig;

public class ExtinguishRules {

    // =========================
    // COBWEB DETECTION
    // =========================
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

                    if (client.world.getBlockState(new BlockPos(x, y, z))
                            .isOf(Blocks.COBWEB)) {
                        return true;
                    }

                }
            }
        }

        return false;
    }

    // =========================
    // FIRE EXTINGUISH CONDITIONS
    // =========================
    public static boolean canActivate(MinecraftClient client) {

        if (client.player == null || client.world == null)
            return false;

        // Must be on fire
        if (!client.player.isOnFire())
            return false;

        // Must not have Fire Resistance
        if (client.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))
            return false;

        // Must be grounded
        if (!client.player.isOnGround())
            return false;

        // Must look down enough
        if (client.player.getPitch() < ModConfig.INSTANCE.pitchThreshold)
            return false;

        return true;
    }

    // =========================
    // MODE RESOLVER (ENUM LOGIC)
    // =========================
    public static Mode resolveMode(MinecraftClient client) {

        if (ModConfig.INSTANCE.disableInNether &&
                client.world.getRegistryKey() == World.NETHER) {
            return Mode.NONE;
        }

        if (client.player == null || client.world == null)
            return Mode.NONE;

        int bucketSlot = HotbarUtils.findBucketSlot(client.player);

        // No bucket at all
        if (bucketSlot == -1)
            return Mode.NONE;

        // Cobweb overrides everything
        if (isInWeb(client))
            return Mode.COBWEB;

        // Fire extinguish mode
        if (canActivate(client))
            return Mode.FIRE;

        // Pickup mode (only if empty bucket)
        if (client.player.getInventory()
                .getStack(bucketSlot)
                .getItem() == Items.BUCKET)
            return Mode.PICKUP;

        return Mode.NONE;
    }
}