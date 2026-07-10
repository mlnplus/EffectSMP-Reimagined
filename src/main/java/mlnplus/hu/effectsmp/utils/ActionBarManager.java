package mlnplus.hu.effectsmp.utils;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.data.PlayerData;
import mlnplus.hu.effectsmp.effects.EffectType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ActionBarManager {

    private final Effectsmp plugin;
    private BukkitTask task;

    private static final String HEART = "❤";

    public ActionBarManager(Effectsmp plugin) {
        this.plugin = plugin;
    }

    public void startTask() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAllPlayers, 0L, 20L);
    }

    public void stopTask() {
        if (task != null) {
            task.cancel();
        }
    }

    private void updateAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    private void updatePlayer(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getEffect() == null)
            return;

        StringBuilder actionBar = new StringBuilder();
        MessageUtils msg = plugin.getMessageUtils();

        actionBar.append("§d").append(data.getEffect().getDisplayName());

        actionBar.append(" §8│  ");

        String heartColor = data.getEffectHearts() >= 3 ? "§d" : (data.getEffectHearts() >= 1 ? "§6" : "§8");
        actionBar.append(heartColor).append(HEART).append(" §f").append(data.getEffectHearts());

        actionBar.append(" §8│  ");

        if (data.isAbilityActive()) {
            long remaining = data.getRemainingAbilityDuration();
            String progressBar = createMiniProgressBar(remaining, getAbilityDuration(data), "§a", "§2");
            String activeText = msg.getMessage("actionbar-active-format")
                    .replace("%time%", msg.formatTimeShort(remaining))
                    .replace("%progress%", progressBar);
            actionBar.append(activeText);
        } else if (data.isAbilityOnCooldown()) {
            long remaining = data.getRemainingCooldown();
            long total = data.getEffect().getCooldownSeconds() * 1000L;
            String progressBar = createMiniProgressBar(total - remaining, total, "§c", "§4");
            String cooldownText = msg.getMessage("actionbar-cooldown-format")
                    .replace("%time%", msg.formatTimeShort(remaining))
                    .replace("%progress%", progressBar);
            actionBar.append(cooldownText);
        } else if (data.getEffectHearts() >= 3) {
            if (data.getEffect() == EffectType.SPEED) {
                int charges = plugin.getDashManager().getRemainingDashes(player.getUniqueId());
                String readyColor = "§a";
                String emptyColor = "§8";
                String chargeDisplay = readyColor + "⚡".repeat(charges) + emptyColor + "⚡".repeat(3 - charges);

                String timeDisplay = "";
                if (charges < 3) {
                    long remainingMs = plugin.getDashManager().getRemainingRechargeTime(player.getUniqueId());
                    int remainingSec = (int) Math.ceil(remainingMs / 1000.0);
                    if (remainingSec > 0) {
                        timeDisplay = " §7(" + remainingSec + "s)";
                    }
                }
                String dashText = msg.getMessage("actionbar-dash-format")
                        .replace("%charges%", chargeDisplay)
                        .replace("%time%", timeDisplay);
                actionBar.append(dashText);
            } else {
                actionBar.append(msg.getMessage("actionbar-ready-format"));
            }
        } else if (data.getEffectHearts() >= 1) {
            actionBar.append(msg.getMessage("actionbar-locked-format"));
        } else {
            actionBar.append(msg.getMessage("actionbar-no-hearts"));
        }

        msg.sendActionBar(player, actionBar.toString());
    }

    private long getAbilityDuration(PlayerData data) {
        return switch (data.getEffect()) {
            case INVISIBILITY -> 10000;
            case HERO_OF_VILLAGE -> 120000;
            case HASTE, SPEED -> 30000;
            case FIRE_RESISTANCE, DOLPHIN_GRACE -> 15000;
            case HEALTH_BOOST -> 30000;
            case WIND_CHARGED -> 0;
            case RESISTANCE -> 20000;
            case STRENGTH -> 15000;
            case REGENERATION -> 30000;
        };
    }

    private String createMiniProgressBar(long current, long total, String filledColor, String emptyColor) {
        int length = 8;
        float progress = Math.min(1.0f, (float) current / total);
        int filled = (int) (progress * length);

        StringBuilder bar = new StringBuilder("§8[");
        for (int i = 0; i < length; i++) {
            bar.append(i < filled ? filledColor + "▮" : emptyColor + "▯");
        }
        bar.append("§8]");
        return bar.toString();
    }

    public void sendCooldownReady(Player player) {
        plugin.getMessageUtils().sendActionBar(player,
                plugin.getMessageUtils().getMessage("actionbar-ready-alert"));
    }

    public void sendAbilityExpired(Player player) {
        plugin.getMessageUtils().sendActionBar(player,
                plugin.getMessageUtils().getMessage("actionbar-expired-alert"));
    }

    public void sendCustomMessage(Player player, String message) {
        plugin.getMessageUtils().sendActionBar(player, message);
    }
}
