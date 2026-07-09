package mlnplus.hu.effectsmp.gui;

import mlnplus.hu.effectsmp.Effectsmp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RecipeGUI {

    private final Effectsmp plugin;

    public RecipeGUI(Effectsmp plugin) {
        this.plugin = plugin;
    }

    private Component getNoItalic(String key) {
        return plugin.getMessageUtils().getMessageComponent(key).decoration(TextDecoration.ITALIC, false);
    }

    private Component parseNoItalic(String text) {
        return plugin.getMessageUtils().parse(text).decoration(TextDecoration.ITALIC, false);
    }

    public Inventory create(Player player, String itemId) {
        if ("effect_shard".equals(itemId)) {
            return createShardInfoGUI(player);
        }

        Inventory inv = Bukkit.createInventory(null, 27,
                plugin.getMessageUtils().getMessageComponent("gui-recipe-title"));

        ItemStack filler = createFiller();
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        // Center visual indicator and result
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrow.getItemMeta();
        arrowMeta.displayName(getNoItalic("gui-recipe-arrow-name"));
        arrow.setItemMeta(arrowMeta);
        inv.setItem(15, arrow);

        ItemStack result = plugin.getCustomItems().getItemByName(itemId);
        if (result != null) {
            inv.setItem(16, result);
        }

        // Load recipe from items.yml
        ConfigurationSection section = plugin.getConfigManager().getItemsConfig().getConfigurationSection(itemId);
        if (section != null) {
            List<String> shape = section.getStringList("shape");
            ConfigurationSection ingredients = section.getConfigurationSection("ingredients");

            if (shape.size() == 3 && ingredients != null) {
                int[][] gridSlots = {
                    {2, 3, 4},
                    {11, 12, 13},
                    {20, 21, 22}
                };

                for (int r = 0; r < 3; r++) {
                    String rowString = shape.get(r);
                    for (int c = 0; c < 3; c++) {
                        if (c < rowString.length()) {
                            char keyChar = rowString.charAt(c);
                            if (keyChar != ' ') {
                                String ingredientName = ingredients.getString(String.valueOf(keyChar));
                                if (ingredientName != null) {
                                    ItemStack ingredientItem = plugin.getCustomItems().getItemByName(ingredientName);
                                    if (ingredientItem == null) {
                                        Material mat = Material.matchMaterial(ingredientName);
                                        if (mat != null) {
                                            ingredientItem = new ItemStack(mat);
                                        }
                                    }
                                    if (ingredientItem != null) {
                                        inv.setItem(gridSlots[r][c], ingredientItem);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        inv.setItem(26, createBackItem());

        return inv;
    }

    private Inventory createShardInfoGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
                plugin.getMessageUtils().getMessageComponent("gui-shard-info-title"));

        ItemStack filler = createFiller();
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        // Ancient City
        ItemStack ancient = new ItemStack(Material.SCULK_SENSOR);
        ItemMeta ancientMeta = ancient.getItemMeta();
        ancientMeta.displayName(getNoItalic("gui-shard-info-ancient-name"));
        List<Component> ancientLore = new ArrayList<>();
        ancientLore.add(Component.empty());
        ancientLore.add(getNoItalic("gui-shard-info-ancient-lore"));
        ancientMeta.lore(ancientLore);
        ancient.setItemMeta(ancientMeta);
        inv.setItem(9, ancient);

        // End City Ship
        ItemStack endCity = new ItemStack(Material.ELYTRA);
        ItemMeta endMeta = endCity.getItemMeta();
        endMeta.displayName(getNoItalic("gui-shard-info-end-name"));
        List<Component> endLore = new ArrayList<>();
        endLore.add(Component.empty());
        endLore.add(getNoItalic("gui-shard-info-end-lore"));
        endMeta.lore(endLore);
        endCity.setItemMeta(endMeta);
        inv.setItem(11, endCity);

        // Woodland Mansion
        ItemStack mansion = new ItemStack(Material.DARK_OAK_LOG);
        ItemMeta mansionMeta = mansion.getItemMeta();
        mansionMeta.displayName(getNoItalic("gui-shard-info-mansion-name"));
        List<Component> mansionLore = new ArrayList<>();
        mansionLore.add(Component.empty());
        mansionLore.add(getNoItalic("gui-shard-info-mansion-lore"));
        mansionMeta.lore(mansionLore);
        mansion.setItemMeta(mansionMeta);
        inv.setItem(13, mansion);

        // Warden Drop
        ItemStack warden = new ItemStack(Material.ECHO_SHARD);
        ItemMeta wardenMeta = warden.getItemMeta();
        wardenMeta.displayName(getNoItalic("gui-shard-info-warden-name"));
        List<Component> wardenLore = new ArrayList<>();
        wardenLore.add(Component.empty());
        wardenLore.add(getNoItalic("gui-shard-info-warden-lore"));
        wardenMeta.lore(wardenLore);
        warden.setItemMeta(wardenMeta);
        inv.setItem(15, warden);

        // Target shard
        ItemStack shard = plugin.getCustomItems().getItemByName("shard");
        if (shard != null) {
            inv.setItem(17, shard);
        }

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
        meta.displayName(getNoItalic("gui-back-items-name"));
        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();
        if (slot == 26) {
            player.closeInventory();
            plugin.getGuiManager().openItemsGUI(player);
        }
    }
}
