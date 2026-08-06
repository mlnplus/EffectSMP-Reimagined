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
        Inventory inv = Bukkit.createInventory(null, 45,
                plugin.getMessageUtils().getMessageComponent("gui-items-title"));

        // Gold & Orange accent border pattern for Items GUI
        ItemStack border = createFiller(Material.ORANGE_STAINED_GLASS_PANE);
        ItemStack corner = createFiller(Material.YELLOW_STAINED_GLASS_PANE);
        ItemStack innerFiller = createFiller(Material.BLACK_STAINED_GLASS_PANE);

        for (int i = 0; i < 45; i++) {
            if (i == 0 || i == 8 || i == 36 || i == 44) {
                inv.setItem(i, corner);
            } else if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border);
            } else {
                inv.setItem(i, innerFiller);
            }
        }

        // Title book at slot 4
        inv.setItem(4, createTitleItem());

        // Consumables Row (slots 10, 12, 14, 16)
        inv.setItem(10, createGuiItem("heart"));
        inv.setItem(12, createGuiItem("shard"));
        inv.setItem(14, createGuiItem("reroll"));
        inv.setItem(16, createGuiItem("op_reroll"));

        // Weapons Row (slots 29, 30, 31, 32, 33)
        inv.setItem(29, createGuiItem("mace"));
        inv.setItem(30, createGuiItem("sword"));
        inv.setItem(31, createGuiItem("bow"));
        inv.setItem(32, createGuiItem("scythe"));
        inv.setItem(33, createGuiItem("spear"));

        // Back button at slot 40
        inv.setItem(40, createBackItem());

        return inv;
    }

    private ItemStack createFiller(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTitleItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(getNoItalic("gui-items-info-book-name"));
        List<Component> lore = new ArrayList<>();
        lore.add(getNoItalic("gui-items-info-book-lore"));
        meta.lore(lore);
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
        if (slot == 40) {
            player.closeInventory();
            plugin.getGuiManager().openInfoGUI(player);
            return;
        }

        String targetItem = null;
        switch (slot) {
            case 10 -> targetItem = "effect_heart";
            case 12 -> targetItem = "effect_shard";
            case 14 -> targetItem = "reroll";
            case 16 -> targetItem = "op_reroll";
            case 29 -> targetItem = "effect_mace";
            case 30 -> targetItem = "effect_sword";
            case 31 -> targetItem = "effect_bow";
            case 32 -> targetItem = "effect_scythe";
            case 33 -> targetItem = "effect_spear";
        }

        if (targetItem != null) {
            player.closeInventory();
            plugin.getGuiManager().openRecipeGUI(player, targetItem);
        }
    }
}
