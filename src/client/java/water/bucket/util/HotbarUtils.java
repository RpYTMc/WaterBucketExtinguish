package water.bucket.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import water.bucket.logic.Mode;

public class HotbarUtils {

    private static final int HOTBAR_SIZE = 9;

    /** First hotbar slot with any bucket (water preferred), or -1. */
    public static int findBucketSlot(PlayerEntity player) {
        int water = findWaterBucketSlot(player);
        return water != -1 ? water : findEmptyBucketSlot(player);
    }

    public static int findWaterBucketSlot(PlayerEntity player) {
        return findSlot(player, Items.WATER_BUCKET);
    }

    public static int findEmptyBucketSlot(PlayerEntity player) {
        return findSlot(player, Items.BUCKET);
    }

    /** The slot to switch to for a mode: fire wants water, pickup wants an empty bucket, cobweb takes either. */
    public static int findBucketSlotFor(PlayerEntity player, Mode mode) {
        return switch (mode) {
            case FIRE -> findWaterBucketSlot(player);
            case PICKUP -> findEmptyBucketSlot(player);
            case COBWEB -> findBucketSlot(player);
            case NONE -> -1;
        };
    }

    private static int findSlot(PlayerEntity player, Item item) {
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            if (player.getInventory().getStack(i).isOf(item)) {
                return i;
            }
        }
        return -1;
    }
}
