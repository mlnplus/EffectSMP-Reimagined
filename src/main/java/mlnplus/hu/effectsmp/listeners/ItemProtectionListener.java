package mlnplus.hu.effectsmp.listeners;

import mlnplus.hu.effectsmp.Effectsmp;
import org.bukkit.Bukkit;
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
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
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

    private boolean cleanBundle(Player player, ItemStack bundle) {
        if (bundle == null || bundle.getType() != Material.BUNDLE) return false;
        if (bundle.getItemMeta() instanceof org.bukkit.inventory.meta.BundleMeta meta) {
            boolean changed = false;
            java.util.List<ItemStack> newItems = new java.util.ArrayList<>();
            for (ItemStack item : meta.getItems()) {
                if (plugin.getCustomItems().isCustomItem(item)) {
                    changed = true;
                    // Drop the custom item on the ground so it's not lost
                    player.getWorld().dropItem(player.getLocation(), item);
                } else {
                    newItems.add(item);
                }
            }
            if (changed) {
                meta.setItems(newItems);
                bundle.setItemMeta(meta);
                return true;
            }
        }
        return false;
    }

    private boolean hasCustomItemInsideBundle(ItemStack bundle) {
        if (bundle == null || bundle.getType() != Material.BUNDLE) return false;
        if (bundle.getItemMeta() instanceof org.bukkit.inventory.meta.BundleMeta meta) {
            for (ItemStack item : meta.getItems()) {
                if (plugin.getCustomItems().isCustomItem(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        Inventory topInv = event.getView().getTopInventory();
        InventoryType type = topInv.getType();

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        boolean illegalBundleFound = false;

        if (hasCustomItemInsideBundle(cursor) || hasCustomItemInsideBundle(current)) {
            illegalBundleFound = true;
        }

        if (event.getClick().name().contains("NUMBER_KEY")) {
            int hotbarSlot = event.getHotbarButton();
            if (hotbarSlot >= 0 && hotbarSlot < 9) {
                ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);
                if (hasCustomItemInsideBundle(hotbarItem)) {
                    illegalBundleFound = true;
                }
            }
        }

        if (illegalBundleFound) {
            // DO NOT cancel the event. Spigot 1.21.2 cancellation for bundles causes duplication/locking bugs.
            // Clean it INSTANTLY so they cannot drop it before a tick passes.
            plugin.getMessageUtils().sendMessage(player, "item-clean-bundle");
            
            boolean cleanedAny = false;
            
            if (cleanBundle(player, cursor)) cleanedAny = true;
            if (cleanBundle(player, current)) cleanedAny = true;
            
            if (event.getClick().name().contains("NUMBER_KEY")) {
                int hotbarSlot = event.getHotbarButton();
                if (hotbarSlot >= 0 && hotbarSlot < 9) {
                    ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);
                    if (cleanBundle(player, hotbarItem)) cleanedAny = true;
                }
            }

            // Also scan open inventory just in case Spigot swapped references
            for (ItemStack item : player.getInventory().getContents()) {
                if (cleanBundle(player, item)) cleanedAny = true;
            }
            if (topInv != null) {
                for (ItemStack item : topInv.getContents()) {
                    if (cleanBundle(player, item)) cleanedAny = true;
                }
            }
            
            if (cleanedAny) {
                // Ensure client sees the instant wipe
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) player.updateInventory();
                });
            }
            return;
        }

        boolean currentCustom = plugin.getCustomItems().isCustomItem(current);

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (currentCustom && playerHasBundle(player)) {
                event.setCancelled(true);
                plugin.getMessageUtils().sendMessage(player, "item-clean-bundle");
                return;
            }
        }

        if (isStorageInventory(type)) {
            ItemStack currentItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();

            InventoryAction action = event.getAction();

            if (event.getRawSlot() < topInv.getSize()) {
                if (plugin.getCustomItems().isCustomItem(cursorItem)) {
                    event.setCancelled(true);
                    plugin.getMessageUtils().sendMessage(player, "item-clean-storage");
                    return;
                }
            }

            if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (plugin.getCustomItems().isCustomItem(currentItem)) {
                    event.setCancelled(true);
                    plugin.getMessageUtils().sendMessage(player, "item-clean-storage");
                    return;
                }
            }

            if (event.getClick().name().contains("NUMBER_KEY")) {
                int hotbarSlot = event.getHotbarButton();
                if (hotbarSlot >= 0 && hotbarSlot < 9) {
                    ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);
                    if (event.getRawSlot() < topInv.getSize() && plugin.getCustomItems().isCustomItem(hotbarItem)) {
                        event.setCancelled(true);
                        plugin.getMessageUtils().sendMessage(player, "item-clean-storage");
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        ItemStack oldCursor = event.getOldCursor();
        boolean dragCustom = plugin.getCustomItems().isCustomItem(oldCursor);
        boolean dragBundle = oldCursor != null && oldCursor.getType() == Material.BUNDLE;

        if (dragCustom) {
            for (int slot : event.getRawSlots()) {
                ItemStack item = event.getView().getItem(slot);
                if (item != null && item.getType() == Material.BUNDLE) {
                    event.setCancelled(true);
                    plugin.getMessageUtils().sendMessage(player, "item-clean-bundle");
                    return;
                }
            }
        } else if (dragBundle) {
            for (int slot : event.getRawSlots()) {
                ItemStack item = event.getView().getItem(slot);
                if (plugin.getCustomItems().isCustomItem(item)) {
                    event.setCancelled(true);
                    plugin.getMessageUtils().sendMessage(player, "item-clean-bundle");
                    return;
                }
            }
        }

        Inventory topInv = event.getView().getTopInventory();
        InventoryType type = topInv.getType();

        if (!isStorageInventory(type))
            return;

        if (!plugin.getCustomItems().isCustomItem(event.getOldCursor()))
            return;

        int topSize = topInv.getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < topSize) {
                event.setCancelled(true);
                plugin.getMessageUtils().sendMessage(player, "item-clean-storage");
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemBurn(EntityCombustEvent event) {
        if (!(event.getEntity() instanceof Item item))
            return;

        if (plugin.getCustomItems().isCustomItem(item.getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item))
            return;

        if (plugin.getCustomItems().isCustomItem(item.getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDespawn(ItemDespawnEvent event) {
        if (plugin.getCustomItems().isCustomItem(event.getEntity().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBundleUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(org.bukkit.event.player.PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (item != null && plugin.getCustomItems().isInfiniteWindCharge(item)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(event.getPlayer(), "infinite-wind-charge-cannot-drop");
            return;
        }
        if (item != null && item.getType() == Material.BUNDLE) {
            if (hasCustomItemInsideBundle(item)) {
                cleanBundle(event.getPlayer(), item);
                event.getItemDrop().setItemStack(item);
                plugin.getMessageUtils().sendMessage(event.getPlayer(), "item-clean-bundle");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHopperPickup(InventoryPickupItemEvent event) {
        if (plugin.getCustomItems().isCustomItem(event.getItem().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHopperMove(InventoryMoveItemEvent event) {
        if (plugin.getCustomItems().isCustomItem(event.getItem())) {
            event.setCancelled(true);
        }
    }

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

    private boolean playerHasBundle(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.BUNDLE) {
                return true;
            }
        }
        return false;
    }
}
