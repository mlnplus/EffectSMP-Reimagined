package mlnplus.hu.effectsmp.effects;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DashManager {

    private final Effectsmp plugin;
    private final Map<UUID, Integer> dashCharges = new HashMap<>();
    private final Map<UUID, Long> lastSneakTime = new HashMap<>();
    private final Map<UUID, Long> lastChargeTime = new HashMap<>();

    private static final int MAX_DASHES = 3;
    private static final long DOUBLE_SNEAK_WINDOW = 300; // 300ms for double-shift detection
    private static final double DASH_POWER = 1.8;

    public DashManager(Effectsmp plugin) {
        this.plugin = plugin;
        startChargeTask();
    }

    private void startChargeTask() {
        // Recharge 1 dash every 30 seconds
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);

                // Only Speed effect players
                if (data.getEffect() != EffectType.SPEED)
                    continue;
                if (data.getEffectHearts() < 3)
                    continue;

                int charges = dashCharges.getOrDefault(uuid, MAX_DASHES);

                // Check if enough time has passed since last charge/use
                Long lastTime = lastChargeTime.get(uuid);
                if (lastTime == null)
                    continue;

                long elapsed = System.currentTimeMillis() - lastTime;
                if (elapsed >= 30000 && charges < MAX_DASHES) {
                    // Recharge one dash
                    charges++;
                    dashCharges.put(uuid, charges);

                    // If still not full, restart timer for next charge
                    if (charges < MAX_DASHES) {
                        lastChargeTime.put(uuid, System.currentTimeMillis());
                    } else {
                        // Full - remove timer
                        lastChargeTime.remove(uuid);
                    }

                    // Notify player of recharge
                    String readyColor = "&a";
                    String emptyColor = "&8";
                    String display = readyColor + "⚡".repeat(charges) + emptyColor
                            + "⚡".repeat(MAX_DASHES - charges);
                    String msg = plugin.getMessageUtils().getMessage("dash-charged").replace("%status%", display);
                    plugin.getMessageUtils().sendActionBar(player, msg);
                }
            }
        }, 20L, 20L); // Check every 1 second (20 ticks) for more accurate timing
        // Note: Continuous action bar for Speed is now handled by ActionBarManager
    }

    public void onSneak(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Only Speed effect can dash
        if (data.getEffect() != EffectType.SPEED) {
            return;
        }

        // Need at least 3 hearts
        if (data.getEffectHearts() < 3) {
            return;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastSneak = lastSneakTime.get(uuid);

        // Check for double-shift
        if (lastSneak != null && (now - lastSneak) <= DOUBLE_SNEAK_WINDOW) {
            // Double shift detected - perform dash
            performDash(player);
            lastSneakTime.remove(uuid); // Reset to prevent triple
        } else {
            // First shift - wait for second
            lastSneakTime.put(uuid, now);
        }
    }

    private void performDash(Player player) {
        UUID uuid = player.getUniqueId();
        int charges = dashCharges.getOrDefault(uuid, MAX_DASHES);

        if (charges <= 0) {
            // Calculate remaining time until next charge
            Long lastCharge = lastChargeTime.get(uuid);
            long remainingMs = lastCharge != null
                    ? Math.max(0, 30000 - (System.currentTimeMillis() - lastCharge))
                    : 30000;
            int remainingSec = (int) Math.ceil(remainingMs / 1000.0);
            plugin.getMessageUtils().sendMessage(player, "dash-empty", "%time%", String.valueOf(remainingSec));
            return;
        }

        // Perform dash in looking direction
        Vector direction = player.getLocation().getDirection();
        direction.setY(Math.max(0.1, direction.getY() * 0.3)); // Limit vertical boost
        direction.normalize().multiply(DASH_POWER);

        player.setVelocity(direction);

        // Effects
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);

        // Update charges
        charges--;
        dashCharges.put(uuid, charges);

        // Start recharge timer if we just went below max (first dash used)
        // OR update timer if already recharging
        if (!lastChargeTime.containsKey(uuid) || charges == MAX_DASHES - 1) {
            lastChargeTime.put(uuid, System.currentTimeMillis());
        }

        // Message
        String readyColor = "&a";
        String emptyColor = "&8";
        String chargeDisplay = charges > 0
                ? readyColor + "⚡".repeat(charges) + emptyColor + "⚡".repeat(MAX_DASHES - charges)
                : plugin.getMessageUtils().getMessage("dash-status-recharging");

        String msg = plugin.getMessageUtils().getMessage("dash-activated").replace("%status%", chargeDisplay);
        plugin.getMessageUtils().sendActionBar(player, msg);
    }

    public int getRemainingDashes(UUID uuid) {
        return dashCharges.getOrDefault(uuid, MAX_DASHES);
    }

    public long getRemainingRechargeTime(UUID uuid) {
        Long lastCharge = lastChargeTime.get(uuid);
        if (lastCharge == null) {
            return 0;
        }
        long remainingMs = 30000 - (System.currentTimeMillis() - lastCharge);
        return Math.max(0, remainingMs);
    }
}
