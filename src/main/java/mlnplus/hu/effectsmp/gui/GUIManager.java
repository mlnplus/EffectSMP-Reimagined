package mlnplus.hu.effectsmp.gui;

import mlnplus.hu.effectsmp.Effectsmp;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("null")
public class GUIManager {

    private final Map<UUID, GUIType> openGUIs = new HashMap<>();
    private final Map<UUID, String> viewedItemIds = new HashMap<>();

    private final MainGUI mainGUI;
    private final InfoGUI infoGUI;
    private final EffectsGUI effectsGUI;
    private final ItemsGUI itemsGUI;
    private final RecipeGUI recipeGUI;

    public GUIManager(Effectsmp plugin) {
        this.mainGUI = new MainGUI(plugin);
        this.infoGUI = new InfoGUI(plugin);
        this.effectsGUI = new EffectsGUI(plugin);
        this.itemsGUI = new ItemsGUI(plugin);
        this.recipeGUI = new RecipeGUI(plugin);
    }

    public void openMainGUI(Player player) {
        Inventory inv = mainGUI.create(player);
        player.openInventory(inv);
        openGUIs.put(player.getUniqueId(), GUIType.MAIN);
    }

    public void openInfoGUI(Player player) {
        Inventory inv = infoGUI.create(player);
        player.openInventory(inv);
        openGUIs.put(player.getUniqueId(), GUIType.INFO);
    }

    public void openEffectsGUI(Player player) {
        Inventory inv = effectsGUI.create(player);
        player.openInventory(inv);
        openGUIs.put(player.getUniqueId(), GUIType.EFFECTS);
    }

    public void openItemsGUI(Player player) {
        Inventory inv = itemsGUI.create(player);
        player.openInventory(inv);
        openGUIs.put(player.getUniqueId(), GUIType.ITEMS);
    }

    public void openRecipeGUI(Player player, String itemId) {
        viewedItemIds.put(player.getUniqueId(), itemId);
        Inventory inv = recipeGUI.create(player, itemId);
        player.openInventory(inv);
        openGUIs.put(player.getUniqueId(), GUIType.RECIPE);
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
        openGUIs.remove(player.getUniqueId());
        viewedItemIds.remove(player.getUniqueId());
    }

    public boolean hasOpenGUI(Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }

    public enum GUIType {
        MAIN, INFO, EFFECTS, ITEMS, RECIPE
    }
}
