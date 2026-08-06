package mlnplus.hu.effectsmp.gui;

import mlnplus.hu.effectsmp.Effectsmp;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("null")
public class GUIManager {

    private final Effectsmp plugin;
    private final Map<UUID, GUIType> openGUIs = new HashMap<>();
    private final Map<UUID, String> viewedItemIds = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();

    private final MainGUI mainGUI;
    private final InfoGUI infoGUI;
    private final EffectsGUI effectsGUI;
    private final ItemsGUI itemsGUI;
    private final RecipeGUI recipeGUI;

    public GUIManager(Effectsmp plugin) {
        this.plugin = plugin;
        this.mainGUI = new MainGUI(plugin);
        this.infoGUI = new InfoGUI(plugin);
        this.effectsGUI = new EffectsGUI(plugin);
        this.itemsGUI = new ItemsGUI(plugin);
        this.recipeGUI = new RecipeGUI(plugin);
    }

    private void applyBottomCover(Player player) {
        UUID uuid = player.getUniqueId();
        if (!savedInventories.containsKey(uuid)) {
            savedInventories.put(uuid, player.getInventory().getContents());
            savedArmor.put(uuid, player.getInventory().getArmorContents());
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!openGUIs.containsKey(uuid)) return;
            InventoryView view = player.getOpenInventory();
            if (view != null && view.getBottomInventory() != null) {
                Inventory bottom = view.getBottomInventory();
                ItemStack glass = createBottomFiller();
                for (int i = 0; i < bottom.getSize(); i++) {
                    bottom.setItem(i, glass);
                }
            }
        }, 1L);
    }

    private ItemStack createBottomFiller() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openMainGUI(Player player) {
        Inventory inv = mainGUI.create(player);
        player.openInventory(inv);
        openGUIs.put(player.getUniqueId(), GUIType.MAIN);
        applyBottomCover(player);
    }

    public void openInfoGUI(Player player) {
        Inventory inv = infoGUI.create(player);
        player.openInventory(inv);
        openGUIs.put(player.getUniqueId(), GUIType.INFO);
        applyBottomCover(player);
    }

    public void openEffectsGUI(Player player) {
        Inventory inv = effectsGUI.create(player);
        player.openInventory(inv);
        openGUIs.put(player.getUniqueId(), GUIType.EFFECTS);
        applyBottomCover(player);
    }

    public void openItemsGUI(Player player) {
        Inventory inv = itemsGUI.create(player);
        player.openInventory(inv);
        openGUIs.put(player.getUniqueId(), GUIType.ITEMS);
        applyBottomCover(player);
    }

    public void openRecipeGUI(Player player, String itemId) {
        viewedItemIds.put(player.getUniqueId(), itemId);
        Inventory inv = recipeGUI.create(player, itemId);
        player.openInventory(inv);
        openGUIs.put(player.getUniqueId(), GUIType.RECIPE);
        applyBottomCover(player);
    }

    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        GUIType type = openGUIs.get(player.getUniqueId());
        if (type == null)
            return;

        event.setCancelled(true);

        switch (type) {
            case MAIN -> mainGUI.handleClick(event, player);
            case INFO -> infoGUI.handleClick(event, player);
            case EFFECTS -> effectsGUI.handleClick(event, player);
            case ITEMS -> itemsGUI.handleClick(event, player);
            case RECIPE -> recipeGUI.handleClick(event, player);
        }
    }

    public void handleClose(Player player) {
        UUID uuid = player.getUniqueId();
        openGUIs.remove(uuid);
        viewedItemIds.remove(uuid);

        restorePlayerInventory(player);
    }

    public void restorePlayerInventory(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack[] contents = savedInventories.remove(uuid);
        ItemStack[] armor = savedArmor.remove(uuid);

        if (contents != null) {
            player.getInventory().setContents(contents);
        }
        if (armor != null) {
            player.getInventory().setArmorContents(armor);
        }
        player.updateInventory();
    }

    public void restoreAll() {
        for (UUID uuid : new java.util.ArrayList<>(savedInventories.keySet())) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                restorePlayerInventory(player);
            }
        }
    }

    public boolean hasOpenGUI(Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }

    public enum GUIType {
        MAIN, INFO, EFFECTS, ITEMS, RECIPE
    }
}
