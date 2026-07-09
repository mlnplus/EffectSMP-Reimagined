package mlnplus.hu.effectsmp.gui;

import mlnplus.hu.effectsmp.Effectsmp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemsGUI {

    private final Effectsmp plugin;

    public ItemsGUI(Effectsmp plugin) {
        this.plugin = plugin;
    }

    private Component getNoItalic(String key) {
        return plugin.getMessageUtils().getMessageComponent(key).decoration(TextDecoration.ITALIC, false);
    }

    public Inventory create(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
                plugin.getMessageUtils().getMessageComponent("gui-items-title"));

        ItemStack filler = createFiller();
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        // Consumables & Utility Row
        inv.setItem(10, createGuiItem("heart"));
        inv.setItem(11, createGuiItem("shard"));
        inv.setItem(12, createGuiItem("reroll"));
        inv.setItem(13, createGuiItem("op_reroll"));

        // Weapons Row
        inv.setItem(18, createGuiItem("mace"));
        inv.setItem(19, createGuiItem("sword"));
        inv.setItem(20, createGuiItem("bow"));
        inv.setItem(21, createGuiItem("scythe"));
        inv.setItem(22, createGuiItem("spear"));

        inv.setItem(26, createBackItem());

        return inv;
    }

    private ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(getNoItalic("gui-back-name"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createGuiItem(String name) {
        ItemStack baseItem = plugin.getCustomItems().getItemByName(name);
        if (baseItem == null) return new ItemStack(Material.AIR);

        ItemStack item = baseItem.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<Component> lore = meta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        } else {
            lore = new ArrayList<>(lore);
        }

        lore.add(Component.empty());
        if ("shard".equals(name)) {
            lore.add(getNoItalic("gui-items-click-how-to-get"));
        } else {
            lore.add(getNoItalic("gui-items-click-recipe"));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();
        if (slot == 26) {
            player.closeInventory();
            return;
        }

        String targetItem = null;
        switch (slot) {
            case 10 -> targetItem = "effect_heart";
            case 11 -> targetItem = "effect_shard";
            case 12 -> targetItem = "reroll";
            case 13 -> targetItem = "op_reroll";
            case 18 -> targetItem = "effect_mace";
            case 19 -> targetItem = "effect_sword";
            case 20 -> targetItem = "effect_bow";
            case 21 -> targetItem = "effect_scythe";
            case 22 -> targetItem = "effect_spear";
        }

        if (targetItem != null) {
            player.closeInventory();
            plugin.getGuiManager().openRecipeGUI(player, targetItem);
        }
    }
}
