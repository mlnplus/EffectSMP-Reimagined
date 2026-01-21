package mlnplus.hu.effectsmp.gui;

import mlnplus.hu.effectsmp.Effectsmp;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager {

    private final Effectsmp plugin;
    private final Map<UUID, GUIType> openGUIs = new HashMap<>();

    private final MainGUI mainGUI;
    private final InfoGUI infoGUI;

    public GUIManager(Effectsmp plugin) {
        this.plugin = plugin;
        this.mainGUI = new MainGUI(plugin);
        this.infoGUI = new InfoGUI(plugin);
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
        }
    }

    public void handleClose(Player player) {
        openGUIs.remove(player.getUniqueId());
    }

    public boolean hasOpenGUI(Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }

    public enum GUIType {
        MAIN, INFO
    }
}
