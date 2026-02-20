package water.bucket.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

public class HotbarUtils {

    public static int findBucketSlot(PlayerEntity player) {

        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).getItem() == Items.WATER_BUCKET
                    || player.getInventory().getStack(i).getItem() == Items.BUCKET) {
                return i;
            }
        }

        return -1;
    }
}