package water.bucket.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import water.bucket.logic.Mode;

public class HotbarUtils {

    private static final int HOTBAR_SIZE = 9;

    /** What the bucket HUD and {@code /waterbucket bucket} report. */
    public enum BucketStatus {
        /** A water bucket is in the hotbar. */
        READY,
        /** No water bucket in the hotbar, but an empty one: it can refill, not put out fire. */
        EMPTY,
        /** No bucket in the hotbar, but a water or empty bucket elsewhere in the inventory. */
        INVENTORY,
        /** No bucket at all. */
        NONE
    }

    public static BucketStatus status(PlayerEntity player) {
        if (findWaterBucketSlot(player) != -1) return BucketStatus.READY;
        if (findEmptyBucketSlot(player) != -1) return BucketStatus.EMPTY;

        var inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            var stack = inventory.getStack(i);
            if (stack.isOf(Items.WATER_BUCKET) || stack.isOf(Items.BUCKET)) {
                return BucketStatus.INVENTORY;
            }
        }
        return BucketStatus.NONE;
    }

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
