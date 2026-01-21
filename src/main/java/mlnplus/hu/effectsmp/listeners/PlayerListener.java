package mlnplus.hu.effectsmp.listeners;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.data.PlayerData;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final Effectsmp plugin;

    public PlayerListener(Effectsmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        data.setPlayerName(player.getName());

        // If game has started and player has no effect yet, assign one
        if (plugin.isGameStarted() && data.getEffect() == null) {
            plugin.getEffectAbilityManager().assignRandomEffect(player, false);
        }

        // Re-apply passive effect if enabled
        if (data.getEffect() != null && data.isPassiveEnabled() && data.getEffectHearts() >= 1) {
            plugin.getEffectAbilityManager().applyPassiveEffect(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
        plugin.getEffectAbilityManager().removeRolling(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        data.addDeath();

        // Only drop items if killed by another player (PvP death)
        if (killer != null && !killer.equals(player)) {
            // Track killer's kills
            PlayerData killerData = plugin.getPlayerDataManager().getPlayerData(killer.getUniqueId());
            killerData.addKill();
            plugin.getPlayerDataManager().savePlayerData(killer.getUniqueId());

            // First death drops shard
            if (!data.isFirstDeathOccurred()) {
                data.setFirstDeathOccurred(true);
                if (data.hasEffectShard()) {
                    data.setHasEffectShard(false);

                    // Drop the shard
                    ItemStack shard = plugin.getCustomItems().createEffectShard();
                    player.getWorld().dropItemNaturally(player.getLocation(), shard);

                    plugin.getMessageUtils().sendMessage(player, "shard-lost");
                    plugin.getMessageUtils().sendMessage(killer, "shard-gained");
                }
            }

            // All PvP deaths drop heart IF player has one
            if (data.getEffectHearts() > 0) {
                int oldHearts = data.getEffectHearts();
                data.removeEffectHearts(1);

                // Drop the heart
                ItemStack heart = plugin.getCustomItems().createEffectHeart();
                player.getWorld().dropItemNaturally(player.getLocation(), heart);

                plugin.getMessageUtils().sendMessage(player, "heart-lost",
                        "%hearts%", String.valueOf(data.getEffectHearts()));
                plugin.getMessageUtils().sendMessage(killer, "heart-gained");

                // If no hearts left, disable passive
                if (data.getEffectHearts() == 0) {
                    data.setPassiveEnabled(false);
                    plugin.getEffectAbilityManager().removePassiveEffect(player);
                    plugin.getMessageUtils().sendMessage(player, "heart-depleted");
                } else if (oldHearts >= 3 && data.getEffectHearts() < 3) {
                    // Lost active ability access (3+ -> 2)
                    plugin.getMessageUtils().sendMessage(player, "heart-ability-lost");
                } else if (oldHearts >= 2 && data.getEffectHearts() < 2) {
                    // Downgrade from level 2 to level 1
                    plugin.getEffectAbilityManager().removePassiveEffect(player);
                    plugin.getEffectAbilityManager().applyPassiveEffect(player);
                    plugin.getMessageUtils().sendMessage(player, "heart-level-down");
                }
            }
        }

        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking())
            return; // Only trigger on sneak start

        Player player = event.getPlayer();
        plugin.getDashManager().onSneak(player);
    }

    // Prevent fall damage when landing from mace jump
    @EventHandler(priority = EventPriority.HIGH)
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;

        // Check if player is mace flying (used mace ability)
        if (plugin.getItemAbilityManager().isMaceFlying(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
