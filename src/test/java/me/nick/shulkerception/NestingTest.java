package me.nick.shulkerception;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NestingTest {
    private final Inventory top = mock(Inventory.class);
    private final PlayerInventory bottom = mock(PlayerInventory.class);
    private final InventoryView view = mock(InventoryView.class, invocation -> {
        // InventoryView.getInventory(int) was introduced after the first shulker release.
        if (invocation.getMethod().getName().equals("getInventory")) {
            return (int) invocation.getArgument(0) < 27 ? top : bottom;
        }
        return RETURNS_DEFAULTS.answer(invocation);
    });
    private final Player player = mock(Player.class);
    private final ItemStack[] contents = new ItemStack[27];
    private final ItemStack[] storage = new ItemStack[41];
    private ItemStack cursor;
    private NestingListener listener;

    @BeforeEach
    void setup() {
        listener = new NestingListener(new NestingPolicy(5, 256));
        when(view.getTopInventory()).thenReturn(top);
        when(view.getBottomInventory()).thenReturn(bottom);
        when(view.getPlayer()).thenReturn(player);
        when(view.getCursor()).thenAnswer(inv -> cursor);
        doAnswer(inv -> { cursor = inv.getArgument(0); return null; }).when(view).setCursor(any());
        when(view.convertSlot(anyInt())).thenAnswer(inv -> (int) inv.getArgument(0) < 27 ? inv.getArgument(0) : (int) inv.getArgument(0) - 27);
        when(view.getItem(anyInt())).thenAnswer(inv -> {
            int raw = inv.getArgument(0);
            return raw < 27 ? contents[raw] : storage[raw - 27];
        });
        doAnswer(inv -> {
            int raw = inv.getArgument(0);
            if (raw < 27) contents[raw] = inv.getArgument(1);
            else storage[raw - 27] = inv.getArgument(1);
            return null;
        }).when(view).setItem(anyInt(), any());
        when(top.getHolder()).thenReturn(mock(ShulkerBox.class));
        when(top.getSize()).thenReturn(27);
        when(top.getContents()).thenAnswer(inv -> contents.clone());
        when(top.getItem(anyInt())).thenAnswer(inv -> contents[(int) inv.getArgument(0)]);
        doAnswer(inv -> { contents[(int) inv.getArgument(0)] = inv.getArgument(1); return null; }).when(top).setItem(anyInt(), any());
        doAnswer(inv -> { System.arraycopy(inv.getArgument(0), 0, contents, 0, 27); return null; }).when(top).setContents(any());
        when(player.getInventory()).thenReturn(bottom);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(bottom.getItem(anyInt())).thenAnswer(inv -> storage[(int) inv.getArgument(0)]);
        doAnswer(inv -> { storage[(int) inv.getArgument(0)] = inv.getArgument(1); return null; }).when(bottom).setItem(anyInt(), any());
        when(bottom.getItemInOffHand()).thenAnswer(inv -> storage[40]);
        doAnswer(inv -> { storage[40] = inv.getArgument(0); return null; }).when(bottom).setItemInOffHand(any());
    }

    @ParameterizedTest
    @MethodSource("shulkerColors")
    void insertsEveryColorWithoutDuplicatingCursor(Material material) {
        cursor = item(material, 1, null);
        InventoryClickEvent event = click(0, ClickType.LEFT, -1);
        listener.onClick(event);
        assertTrue(event.isCancelled());
        assertNull(cursor);
        assertEquals(material, contents[0].getType());
        assertEquals(1, contents[0].getAmount());
    }

    @Test
    void swappingPreservesDisplacedItemsAndNestedMetadata() {
        contents[0] = item(Material.DIAMOND, 32, null);
        ItemStack inner = nested(item(Material.EMERALD, 12, null));
        cursor = inner;
        listener.onClick(click(0, ClickType.RIGHT, -1));
        assertSame(inner.getItemMeta(), contents[0].getItemMeta());
        assertEquals(Material.DIAMOND, cursor.getType());
        assertEquals(32, cursor.getAmount());
    }

    @Test
    void rightClickSplitsCustomStackWithoutLoss() {
        cursor = item(baseShulker(), 3, null);
        listener.onClick(click(0, ClickType.RIGHT, -1));
        assertEquals(1, contents[0].getAmount());
        assertEquals(2, cursor.getAmount());
    }

    @Test
    void shiftClickMovesOnlyWhatFits() {
        Arrays.fill(contents, item(Material.STONE, 64, null));
        contents[4] = null;
        storage[0] = item(baseShulker(), 3, null);
        listener.onClick(click(27, ClickType.SHIFT_LEFT, -1));
        assertEquals(baseShulker(), contents[4].getType());
        assertEquals(1, contents[4].getAmount());
        assertEquals(2, storage[0].getAmount());
    }

    @Test
    void fullInventoryDoesNotConsumeShiftClickedBox() {
        Arrays.fill(contents, item(Material.STONE, 64, null));
        ItemStack source = item(baseShulker(), 1, null);
        storage[0] = source;
        listener.onClick(click(27, ClickType.SHIFT_RIGHT, -1));
        assertSame(source, storage[0]);
        assertTrue(Arrays.stream(contents).allMatch(i -> i.getType() == Material.STONE));
    }

    @ParameterizedTest
    @MethodSource("swapTypes")
    void swapsHotbarOrOffhandWithoutLoss(ClickType type) {
        int source = type == ClickType.NUMBER_KEY ? 2 : 40;
        storage[source] = item(Material.RED_SHULKER_BOX, 1, null);
        contents[0] = item(Material.DIAMOND, 17, null);
        listener.onClick(click(0, type, type == ClickType.NUMBER_KEY ? 2 : -1));
        assertEquals(Material.RED_SHULKER_BOX, contents[0].getType());
        assertEquals(Material.DIAMOND, storage[source].getType());
        assertEquals(17, storage[source].getAmount());
    }

    @Test
    void cancelledProtectionEventIsUntouched() {
        ItemStack original = item(baseShulker(), 1, null);
        cursor = original;
        InventoryClickEvent event = click(0, ClickType.LEFT, -1);
        event.setCancelled(true);
        listener.onClick(event);
        assertSame(original, cursor);
        assertNull(contents[0]);
    }

    @Test
    void spectatorsCannotInsert() {
        when(player.getGameMode()).thenReturn(GameMode.SPECTATOR);
        cursor = item(baseShulker(), 1, null);
        listener.onClick(click(0, ClickType.LEFT, -1));
        assertNotNull(cursor);
        assertNull(contents[0]);
    }

    @Test
    void customGuiIsUntouched() {
        when(top.getHolder()).thenReturn(null);
        cursor = item(baseShulker(), 1, null);
        InventoryClickEvent event = click(0, ClickType.LEFT, -1);
        listener.onClick(event);
        assertFalse(event.isCancelled());
        assertNotNull(cursor);
        assertNull(contents[0]);
    }

    @Test
    void excessiveDepthRejectsInsertionWithoutChangingEitherSide() {
        listener = new NestingListener(new NestingPolicy(2, 256));
        ItemStack original = nested(item(baseShulker(), 1, null));
        cursor = original;
        contents[0] = item(Material.DIAMOND, 5, null);
        listener.onClick(click(0, ClickType.LEFT, -1));
        assertSame(original, cursor);
        assertEquals(Material.DIAMOND, contents[0].getType());
        assertEquals(5, contents[0].getAmount());
    }

    @Test
    void removalRemainsVanillaEvenOverLimit() {
        listener = new NestingListener(new NestingPolicy(2, 1));
        contents[0] = nested(item(baseShulker(), 1, null));
        InventoryClickEvent event = click(0, ClickType.LEFT, -1);
        listener.onClick(event);
        assertFalse(event.isCancelled());
    }

    @Test
    void aggregateLimitCountsAllSiblingsAndDescendants() {
        NestingPolicy policy = new NestingPolicy(5, 3);
        ItemStack pair = nested(item(baseShulker(), 1, null));
        assertTrue(policy.allows(new ItemStack[]{pair, item(baseShulker(), 1, null)}));
        assertFalse(policy.allows(new ItemStack[]{pair, pair}));
    }

    @Test
    void depthIncludesOuterBox() {
        NestingPolicy policy = new NestingPolicy(3, 256);
        ItemStack inner = item(baseShulker(), 1, null);
        assertTrue(policy.allows(new ItemStack[]{nested(inner)}));
        assertFalse(policy.allows(new ItemStack[]{nested(nested(inner))}));
    }

    @Test
    void swappingAtBoxLimitExcludesDisplacedBox() {
        listener = new NestingListener(new NestingPolicy(2, 1));
        contents[0] = item(Material.RED_SHULKER_BOX, 1, null);
        cursor = item(Material.BLUE_SHULKER_BOX, 1, null);
        listener.onClick(click(0, ClickType.LEFT, -1));
        assertEquals(Material.BLUE_SHULKER_BOX, contents[0].getType());
        assertEquals(Material.RED_SHULKER_BOX, cursor.getType());
    }

    private InventoryClickEvent click(int raw, ClickType type, int button) {
        // Vanilla reports NOTHING for rejected shulker insertions; the plugin must use ClickType.
        return new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER, raw, type, InventoryAction.NOTHING, button);
    }

    private static java.util.stream.Stream<Material> shulkerColors() {
        return Arrays.stream(Material.values()).filter(type -> !type.name().startsWith("LEGACY_")
                && (type == baseShulker() || type.name().endsWith("_SHULKER_BOX")));
    }

    private static Material baseShulker() {
        Material undyed = Material.getMaterial("SHULKER_BOX");
        return undyed == null ? Material.PURPLE_SHULKER_BOX : undyed;
    }

    private static java.util.stream.Stream<ClickType> swapTypes() {
        return Arrays.stream(ClickType.values()).filter(click -> click.name().equals("NUMBER_KEY")
                || click.name().equals("SWAP_OFFHAND"));
    }
    private static ItemStack nested(ItemStack... children) {
        Inventory inventory = mock(Inventory.class);
        when(inventory.getContents()).thenReturn(children);
        ShulkerBox box = mock(ShulkerBox.class);
        when(box.getInventory()).thenReturn(inventory);
        BlockStateMeta meta = mock(BlockStateMeta.class);
        when(meta.hasBlockState()).thenReturn(true);
        when(meta.getBlockState()).thenReturn(box);
        return item(baseShulker(), 1, meta);
    }

    private static ItemStack item(Material type, int amount, ItemMeta meta) {
        ItemStack item = mock(ItemStack.class);
        AtomicInteger count = new AtomicInteger(amount);
        when(item.getType()).thenReturn(type);
        when(item.getAmount()).thenAnswer(inv -> count.get());
        doAnswer(inv -> { count.set(inv.getArgument(0)); return null; }).when(item).setAmount(anyInt());
        when(item.getItemMeta()).thenReturn(meta);
        when(item.clone()).thenAnswer(inv -> item(type, count.get(), meta));
        return item;
    }
}
