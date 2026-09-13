package me.nick.shulkerception;

import org.bukkit.GameMode;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import static me.nick.shulkerception.NestingPolicy.isEmpty;
import static me.nick.shulkerception.NestingPolicy.isShulker;

final class NestingListener implements Listener {
    private final NestingPolicy policy;

    NestingListener(NestingPolicy policy) {
        this.policy = policy;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        // Check cancellation here too so direct callers obey the same contract.
        if (event.isCancelled() || event instanceof InventoryCreativeEvent
                || !(event.getWhoClicked() instanceof Player player)
                || player.getGameMode() == GameMode.SPECTATOR) return;
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof ShulkerBox)) return;

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0) return;
        if (event.isShiftClick() && event.getClickedInventory() == event.getView().getBottomInventory()
                && isShulker(event.getCurrentItem())) {
            shiftIntoBox(event, top, player);
        } else if (rawSlot < top.getSize()) {
            switch (event.getClick()) {
                case LEFT, RIGHT -> cursorIntoBox(event, top, player);
                case NUMBER_KEY, SWAP_OFFHAND -> hotbarIntoBox(event, top, player);
                default -> { /* Vanilla handles removal, dropping and creative cloning. */ }
            }
        }
    }

    private void cursorIntoBox(InventoryClickEvent event, Inventory top, Player player) {
        ItemStack cursor = event.getCursor();
        if (!isShulker(cursor)) return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        ItemStack current = top.getItem(slot);
        // Shulkers occupy one slot each, even if supplied as a stack by another plugin.
        if (!isEmpty(current) && cursor.getAmount() != 1) {
            player.updateInventory();
            return;
        }
        ItemStack inserted = one(cursor);
        if (!allows(top, slot, inserted, player)) return;
        top.setItem(slot, inserted);
        event.getView().setCursor(isEmpty(current) ? remainder(cursor, 1) : current.clone());
        player.updateInventory();
    }

    private void shiftIntoBox(InventoryClickEvent event, Inventory top, Player player) {
        event.setCancelled(true);
        ItemStack source = event.getCurrentItem();
        ItemStack[] proposed = top.getContents();
        int moved = 0;
        for (int slot = 0; slot < proposed.length && moved < source.getAmount(); slot++) {
            if (!isEmpty(proposed[slot])) continue;
            proposed[slot] = one(source);
            if (!policy.allows(proposed)) {
                proposed[slot] = null;
                player.sendMessage("That would exceed this server's shulker nesting limit.");
                break;
            }
            moved++;
        }
        if (moved > 0) {
            top.setContents(proposed);
            event.setCurrentItem(remainder(source, moved));
        }
        player.updateInventory();
    }

    private void hotbarIntoBox(InventoryClickEvent event, Inventory top, Player player) {
        if (!isEmpty(event.getCursor())) return;
        PlayerInventory inventory = player.getInventory();
        boolean offhand = event.getClick() == ClickType.SWAP_OFFHAND;
        int button = event.getHotbarButton();
        if (!offhand && (button < 0 || button > 8)) return;
        ItemStack source = offhand ? inventory.getItemInOffHand() : inventory.getItem(button);
        if (!isShulker(source)) return;
        event.setCancelled(true);
        if (source.getAmount() != 1) {
            player.updateInventory();
            return;
        }
        int slot = event.getRawSlot();
        if (!allows(top, slot, source, player)) return;
        ItemStack displaced = top.getItem(slot);
        top.setItem(slot, source.clone());
        ItemStack replacement = isEmpty(displaced) ? null : displaced.clone();
        if (offhand) inventory.setItemInOffHand(replacement);
        else inventory.setItem(button, replacement);
        player.updateInventory();
    }

    private boolean allows(Inventory top, int slot, ItemStack inserted, Player player) {
        ItemStack[] proposed = top.getContents();
        proposed[slot] = inserted;
        if (policy.allows(proposed)) return true;
        player.sendMessage("That would exceed this server's shulker nesting limit.");
        player.updateInventory();
        return false;
    }

    private static ItemStack one(ItemStack source) {
        ItemStack result = source.clone();
        result.setAmount(1);
        return result;
    }

    private static ItemStack remainder(ItemStack source, int removed) {
        if (source.getAmount() <= removed) return null;
        ItemStack result = source.clone();
        result.setAmount(source.getAmount() - removed);
        return result;
    }
}
