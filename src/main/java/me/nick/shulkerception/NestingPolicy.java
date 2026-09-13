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
        return type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR;
    }

    static boolean isShulker(ItemStack item) {
        if (isEmpty(item)) return false;
        Material type = item.getType();
        return type == Material.SHULKER_BOX || type.name().endsWith("_SHULKER_BOX");
    }

    boolean allows(ItemStack[] contents) {
        return check(contents, 2, new int[]{0});
    }

    private boolean check(ItemStack[] contents, int depth, int[] count) {
        for (ItemStack item : contents) {
            if (!isShulker(item)) continue;
            if (depth > maxDepth || item.getAmount() > maxBoxes - count[0]) return false;
            count[0] += item.getAmount();
            if (item.getItemMeta() instanceof BlockStateMeta meta && meta.hasBlockState()
                    && meta.getBlockState() instanceof ShulkerBox box) {
                // Account for each copy if another plugin supplies stacked shulkers.
                for (int copy = 0; copy < item.getAmount(); copy++) {
                    if (!check(box.getSnapshotInventory().getContents(), depth + 1, count)) return false;
                }
            }
        }
        return true;
    }
}
