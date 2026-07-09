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
                    event.setCancelled(true);

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (player.isOnline() && plugin.getItemAbilityManager().isFrozen(player.getUniqueId())) {
                            Location targetLoc = frozenLoc.clone();
                            Location currentLoc = player.getLocation();
                            if (currentLoc != null) {
                                targetLoc.setYaw(currentLoc.getYaw());
                                targetLoc.setPitch(currentLoc.getPitch());
                            }
                            player.teleport(targetLoc);
                            player.setVelocity(new Vector(0, 0, 0));
                        }
                    });
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }
}
