package mlnplus.hu.effectsmp.listeners;

import mlnplus.hu.effectsmp.Effectsmp;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class CraftingListener implements Listener {

    private final Effectsmp plugin;

    public CraftingListener(Effectsmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareResult(PrepareItemCraftEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("settings.limited-crafting-enabled", true))
            return;

        if (event.getRecipe() == null || event.getRecipe().getResult().getType().isAir())
            return;

        ItemStack result = event.getRecipe().getResult();
        if (!plugin.getCustomItems().isCustomItem(result))
            return;

        String itemId = plugin.getCustomItems().getItemType(result);
        if (isLimitedItem(itemId)) {
            if (plugin.getConfigManager().isGlobalItemCrafted(itemId)) {
                // Already crafted by someone, block it
                event.getInventory().setResult(null);
            }
        }
    }

    @EventHandler
    public void onCraft(org.bukkit.event.inventory.CraftItemEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("settings.limited-crafting-enabled", true))
            return;

        ItemStack result = event.getCurrentItem();
        if (result == null || !plugin.getCustomItems().isCustomItem(result))
            return;

        String itemId = plugin.getCustomItems().getItemType(result);
        if (isLimitedItem(itemId)) {
            if (plugin.getConfigManager().isGlobalItemCrafted(itemId)) {
                // Just in case check
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof org.bukkit.entity.Player player) {
                    plugin.getMessageUtils().sendMessage(player, "craft-limit-reached");
                }
            } else {
                // Register craft
                plugin.getConfigManager().setGlobalItemCrafted(itemId, true);

                // Broadcast msg
                if (event.getWhoClicked() instanceof org.bukkit.entity.Player player) {
                    String itemName = getDisplayName(itemId);
                    plugin.getMessageUtils().broadcast("craft-legendary-broadcast",
                            "%player%", player.getName(),
                            "%item%", itemName);
                }
            }
        }
    }

    private boolean isLimitedItem(String itemId) {
        if (itemId == null)
            return false;
        return switch (itemId) {
            case "effect_sword", "effect_mace", "effect_bow", "effect_scythe" -> true;
            default -> false;
        };
    }

    private String getDisplayName(String itemId) {
        String key = switch (itemId) {
            case "effect_sword" -> "item-sword-name";
            case "effect_mace" -> "item-mace-name";
            case "effect_bow" -> "item-bow-name";
            case "effect_scythe" -> "item-scythe-name";
            default -> null;
        };

        if (key != null) {
            return plugin.getMessageUtils().getMessage(key);
        }
        return itemId;
    }
}
