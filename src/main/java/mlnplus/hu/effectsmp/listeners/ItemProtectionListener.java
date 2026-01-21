package mlnplus.hu.effectsmp.listeners;

import mlnplus.hu.effectsmp.Effectsmp;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.DecoratedPot;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemProtectionListener implements Listener {

    private final Effectsmp plugin;

    public ItemProtectionListener(Effectsmp plugin) {
        this.plugin = plugin;
    }

    // Prevent putting items in containers via click
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        Inventory topInv = event.getView().getTopInventory();
        InventoryType type = topInv.getType();

        // Bundle Protection: Check for ANY interactions between Bundle and Custom Item
        // This can happen in any inventory (including player's)
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        boolean cursorBundle = cursor != null && cursor.getType() == Material.BUNDLE;
        boolean currentBundle = current != null && current.getType() == Material.BUNDLE;

        boolean cursorCustom = plugin.getCustomItems().isCustomItem(cursor);
        boolean currentCustom = plugin.getCustomItems().isCustomItem(current);

        // Block ALL interactions between Bundle and Custom Item
        // Case 1: Cursor is Bundle, Clicked is Custom Item
        // Case 2: Cursor is Custom Item, Clicked is Bundle
        if ((cursorBundle && currentCustom) || (currentBundle && cursorCustom)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-clean-bundle");
            return;
        }

        // Additional Bundle Protection: Block hotbar key swaps involving Bundle +
        // Custom Item
        if (event.getClick().name().contains("NUMBER_KEY")) {
            int hotbarSlot = event.getHotbarButton();
            if (hotbarSlot >= 0 && hotbarSlot < 9) {
                ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);

                boolean hotbarBundle = hotbarItem != null && hotbarItem.getType() == Material.BUNDLE;
                boolean hotbarCustom = plugin.getCustomItems().isCustomItem(hotbarItem);

                // Block if one is bundle and other is custom
                if ((hotbarBundle && currentCustom) || (currentBundle && hotbarCustom) ||
                        (hotbarCustom && currentBundle) || (hotbarBundle && cursorCustom)) {
                    event.setCancelled(true);
                    plugin.getMessageUtils().sendMessage(player, "item-clean-bundle");
                    return;
                }
            }
        }

        // Additional Bundle Protection: Block shift-click into a bundle
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            // If custom item is shift-clicked and there's a bundle anywhere in inventory,
            // might go into it
            // For safety, just block shift-click on custom items if player has ANY bundle
            if (currentCustom && playerHasBundle(player)) {
                event.setCancelled(true);
                plugin.getMessageUtils().sendMessage(player, "item-clean-bundle");
                return;
            }
        }

        // Check if this is a storage inventory (not player's own)
        if (isStorageInventory(type)) {
            ItemStack currentItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();

            // Check action types that could move items into container
            InventoryAction action = event.getAction();

            // Clicking with custom item on cursor into container slot
            if (event.getRawSlot() < topInv.getSize()) {
                if (plugin.getCustomItems().isCustomItem(cursorItem)) {
                    event.setCancelled(true);
                    plugin.getMessageUtils().sendMessage(player, "item-clean-storage");
                    return;
                }
            }

            // Shift-clicking custom item from player inventory into container
            if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (plugin.getCustomItems().isCustomItem(currentItem)) {
                    event.setCancelled(true);
                    plugin.getMessageUtils().sendMessage(player, "item-clean-storage");
                    return;
                }
            }

            // Hotbar number keys to swap items
            if (event.getClick().name().contains("NUMBER_KEY")) {
                int hotbarSlot = event.getHotbarButton();
                if (hotbarSlot >= 0 && hotbarSlot < 9) {
                    ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);
                    // Moving from hotbar into container
                    if (event.getRawSlot() < topInv.getSize() && plugin.getCustomItems().isCustomItem(hotbarItem)) {
                        event.setCancelled(true);
                        plugin.getMessageUtils().sendMessage(player, "item-clean-storage");
                        return;
                    }
                }
            }

            // Double-click collect action
            if (action == InventoryAction.COLLECT_TO_CURSOR) {
                // This action collects items from both inventories
                // If cursor has custom item, it won't pull from container anyway
                // No action needed
            }
        }
    }

    // Prevent putting items via drag
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        Inventory topInv = event.getView().getTopInventory();
        InventoryType type = topInv.getType();

        if (!isStorageInventory(type))
            return;

        // Check if dragging custom item
        if (!plugin.getCustomItems().isCustomItem(event.getOldCursor()))
            return;

        // Check if any drag slot is in the storage inventory
        int topSize = topInv.getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < topSize) {
                event.setCancelled(true);
                plugin.getMessageUtils().sendMessage(player, "item-clean-storage");
                return;
            }
        }
    }

    // Prevent custom dropped items from burning
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemBurn(EntityCombustEvent event) {
        if (!(event.getEntity() instanceof Item item))
            return;

        if (plugin.getCustomItems().isCustomItem(item.getItemStack())) {
            event.setCancelled(true);
        }
    }

    // Prevent custom dropped items from taking damage (explosions, cactus, etc.)
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item))
            return;

        if (plugin.getCustomItems().isCustomItem(item.getItemStack())) {
            event.setCancelled(true);
        }
    }

    // Prevent custom items from despawning
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDespawn(ItemDespawnEvent event) {
        if (plugin.getCustomItems().isCustomItem(event.getEntity().getItemStack())) {
            event.setCancelled(true);
        }
    }

    // Prevent putting custom items into bundles
    @EventHandler(priority = EventPriority.HIGH)
    public void onBundleUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        // Check if player is trying to put custom item into bundle
        if (mainHand.getType() == Material.BUNDLE && plugin.getCustomItems().isCustomItem(offHand)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-clean-bundle");
            return;
        }
        if (offHand.getType() == Material.BUNDLE && plugin.getCustomItems().isCustomItem(mainHand)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-clean-bundle");
            return;
        }

        // Prevent putting custom items in decorated pots
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            if (block.getState() instanceof DecoratedPot) {
                if (plugin.getCustomItems().isCustomItem(mainHand) || plugin.getCustomItems().isCustomItem(offHand)) {
                    event.setCancelled(true);
                    plugin.getMessageUtils().sendMessage(player, "item-clean-flowerpot");
                }
            }
        }
    }

    // Prevent putting custom items into item frames
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame))
            return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (plugin.getCustomItems().isCustomItem(mainHand) || plugin.getCustomItems().isCustomItem(offHand)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-clean-itemframe");
        }
    }

    // Helper to check if inventory is a storage type
    private boolean isStorageInventory(InventoryType type) {
        return switch (type) {
            case CHEST, ENDER_CHEST, SHULKER_BOX, BARREL, HOPPER,
                    DROPPER, DISPENSER, FURNACE, BLAST_FURNACE, SMOKER,
                    BREWING, BEACON, MERCHANT, CARTOGRAPHY, GRINDSTONE,
                    STONECUTTER, LOOM, SMITHING, ANVIL, LECTERN,
                    COMPOSTER, CHISELED_BOOKSHELF ->
                true;
            default -> false;
        };
    }

    // Helper to check if player has any bundle in inventory
    private boolean playerHasBundle(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.BUNDLE) {
                return true;
            }
        }
        return false;
    }
}
