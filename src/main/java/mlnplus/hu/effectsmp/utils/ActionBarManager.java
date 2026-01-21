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

    // Cool symbols and colors
    private static final String HEART = "❤";
    private static final String DIAMOND = "◆";

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

        // Effect display
        actionBar.append("§d").append(data.getEffect().getDisplayName());

        actionBar.append(" §8│  ");

        // Heart count with color based on amount
        String heartColor = data.getEffectHearts() >= 3 ? "§d" : (data.getEffectHearts() >= 1 ? "§6" : "§8");
        actionBar.append(heartColor).append(HEART).append(" §f").append(data.getEffectHearts());

        actionBar.append(" §8│  ");

        // Show ability status
        if (data.isAbilityActive()) {
            long remaining = data.getRemainingAbilityDuration();
            String progressBar = createMiniProgressBar(remaining, getAbilityDuration(data), "§a", "§2");
            actionBar.append("§a").append(DIAMOND).append(" §fAktív §a")
                    .append(msg.formatTimeShort(remaining))
                    .append(" ").append(progressBar);
        } else if (data.isAbilityOnCooldown()) {
            long remaining = data.getRemainingCooldown();
            long total = data.getEffect().getCooldownSeconds() * 1000L;
            String progressBar = createMiniProgressBar(total - remaining, total, "§c", "§4");
            actionBar.append("§c").append(DIAMOND).append(" §fCooldown §c")
                    .append(msg.formatTimeShort(remaining))
                    .append(" ").append(progressBar);
        } else if (data.getEffectHearts() >= 3) {
            // For SPEED effect, show dash status instead of "Kész (/e activate)"
            if (data.getEffect() == EffectType.SPEED) {
                int charges = plugin.getDashManager().getRemainingDashes(player.getUniqueId());
                String readyColor = "§a";
                String emptyColor = "§8";
                String chargeDisplay = readyColor + "⚡".repeat(charges) + emptyColor + "⚡".repeat(3 - charges);

                // Show remaining time until next charge if not full
                String timeDisplay = "";
                if (charges < 3) {
                    long remainingMs = plugin.getDashManager().getRemainingRechargeTime(player.getUniqueId());
                    int remainingSec = (int) Math.ceil(remainingMs / 1000.0);
                    if (remainingSec > 0) {
                        timeDisplay = " §7(" + remainingSec + "s)";
                    }
                }

                actionBar.append("§a◆ &fDash: ").append(chargeDisplay).append(timeDisplay);
            } else {
                actionBar.append("§a").append(DIAMOND).append(" §fElérhető! §7(/e activate)");
            }
        } else if (data.getEffectHearts() >= 1) {
            actionBar.append("&7🔒 &7(&f3 §c").append(HEART).append(" §fkell&7)");
        } else {
            actionBar.append("§c✖ §7Nincs szíved!");
        }

        msg.sendActionBar(player, actionBar.toString());
    }

    private long getAbilityDuration(PlayerData data) {
        // Return typical ability durations for progress bar
        return switch (data.getEffect()) {
            case INVISIBILITY -> 10000;
            case HERO_OF_VILLAGE -> 120000;
            case HASTE, SPEED -> 30000;
            case FIRE_RESISTANCE, DOLPHIN_GRACE -> 15000;
            case HEALTH_BOOST -> 30000;
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
                "§a§l⚡ ABILITY ELÉRHETŐ! ⚡");
    }

    public void sendAbilityExpired(Player player) {
        plugin.getMessageUtils().sendActionBar(player,
                "§c§l⏱ ABILITY LEJÁRT! ⏱");
    }

    public void sendCustomMessage(Player player, String message) {
        plugin.getMessageUtils().sendActionBar(player, message);
    }
}
