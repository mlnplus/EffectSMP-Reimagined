package mlnplus.hu.effectsmp.listeners;

import mlnplus.hu.effectsmp.Effectsmp;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GUIListener implements Listener {

    private final Effectsmp plugin;

    public GUIListener(Effectsmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (plugin.getGuiManager().hasOpenGUI(player)) {
            plugin.getGuiManager().handleClick(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player))
            return;

        plugin.getGuiManager().handleClose(player);
    }
}
