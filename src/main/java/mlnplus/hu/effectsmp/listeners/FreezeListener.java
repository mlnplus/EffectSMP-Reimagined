package mlnplus.hu.effectsmp.listeners;

import mlnplus.hu.effectsmp.Effectsmp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

@SuppressWarnings("null")
public class FreezeListener implements Listener {

    private final Effectsmp plugin;

    public FreezeListener(Effectsmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (plugin.getItemAbilityManager().isFrozen(player.getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();

            if (to == null)
                return;

            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                Location frozenLoc = plugin.getItemAbilityManager().getFreezeLocation(player.getUniqueId());
                if (frozenLoc != null) {
                    Location targetLoc = frozenLoc.clone();
                    targetLoc.setYaw(to.getYaw());
                    targetLoc.setPitch(to.getPitch());
                    event.setTo(targetLoc);
                } else {
                    Location targetLoc = from.clone();
                    targetLoc.setYaw(to.getYaw());
                    targetLoc.setPitch(to.getPitch());
                    event.setTo(targetLoc);
                }
            }
        }
    }
}
