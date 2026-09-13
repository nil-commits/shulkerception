package me.nick.shulkerception;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

final class NestingPolicy {
    private final int maxDepth;
    private final int maxBoxes;

    NestingPolicy(int maxDepth, int maxBoxes) {
        this.maxDepth = maxDepth;
        this.maxBoxes = maxBoxes;
    }

    static boolean isEmpty(ItemStack item) {
        if (item == null || item.getAmount() <= 0) return true;
        Material type = item.getType();
        return type == Material.AIR || "CAVE_AIR".equals(type.name()) || "VOID_AIR".equals(type.name());
    }

    static boolean isShulker(ItemStack item) {
        if (isEmpty(item)) return false;
        Material type = item.getType();
        return !type.name().startsWith("LEGACY_")
                && ("SHULKER_BOX".equals(type.name()) || type.name().endsWith("_SHULKER_BOX"));
    }

    boolean allows(ItemStack[] contents) {
        return check(contents, 2, new int[]{0});
    }

    private boolean check(ItemStack[] contents, int depth, int[] count) {
        for (ItemStack item : contents) {
            if (!isShulker(item)) continue;
            if (depth > maxDepth || item.getAmount() > maxBoxes - count[0]) return false;
            count[0] += item.getAmount();
            if (item.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
                if (!meta.hasBlockState() || !(meta.getBlockState() instanceof ShulkerBox)) continue;
                ShulkerBox box = (ShulkerBox) meta.getBlockState();
                // Account for each copy if another plugin supplies stacked shulkers.
                for (int copy = 0; copy < item.getAmount(); copy++) {
                    // The state attached to item metadata is unplaced: getInventory()
                    // reads its snapshot, including on 1.11 where Container did not exist.
                    if (!check(box.getInventory().getContents(), depth + 1, count)) return false;
                }
            }
        }
        return true;
    }
}
