package mlnplus.hu.effectsmp.effects;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.Random;

public class RollAnimationManager {

    private final Effectsmp plugin;
    private final Random random = new Random();

    // Roll animation settings
    private static final int ROLL_TICKS = 40; // 2 seconds of rolling

    // Cool symbols for animation
    private static final String[] ROLL_SYMBOLS = { "" };
    private static final String[] COLORS = { "§d", "§b", "§e", "§a", "§c", "§6", "§5", "§3" };

    public RollAnimationManager(Effectsmp plugin) {
        this.plugin = plugin;
    }

    public void playRollAnimation(Player player, boolean isOP, Runnable onComplete) {
        EffectType[] pool = isOP ? EffectType.getOPEffects() : EffectType.getNormalEffects();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        EffectType currentEffect = data.getEffect();

        // Determine final effect
        EffectType finalEffect;
        do {
            finalEffect = pool[random.nextInt(pool.length)];
        } while (finalEffect == currentEffect && pool.length > 1);

        final EffectType targetEffect = finalEffect;

        new BukkitRunnable() {
            int tick = 0;
            int currentRollTick = 0;

            @Override
            public void run() {
                if (tick >= ROLL_TICKS) {
                    // Final reveal
                    showFinalEffect(player, targetEffect, isOP);

                    // Set the effect
                    data.setEffect(targetEffect);
                    data.setPassiveEnabled(true);
                    plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
                    plugin.getEffectAbilityManager().applyPassiveEffect(player);

                    if (onComplete != null) {
                        onComplete.run();
                    }

                    cancel();
                    return;
                }

                currentRollTick++;

                // Slow down as we approach the end
                int delay = calculateDelay(tick, ROLL_TICKS);
                if (currentRollTick < delay) {
                    return;
                }
                currentRollTick = 0;

                // Show random rolling effect
                EffectType displayEffect = pool[random.nextInt(pool.length)];
                showRollingEffect(player, displayEffect, tick, ROLL_TICKS, isOP);

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private int calculateDelay(int currentTick, int totalTicks) {
        // Start fast (1 tick), slow down to 4 ticks at the end
        float progress = (float) currentTick / totalTicks;
        return Math.max(1, (int) (1 + progress * 4));
    }

    private void showRollingEffect(Player player, EffectType effect, int tick, int totalTicks, boolean isOP) {
        String symbol = ROLL_SYMBOLS[tick % ROLL_SYMBOLS.length];
        String color = COLORS[tick % COLORS.length];

        // Calculate progress bar
        int progressLength = 20;
        float progress = (float) tick / totalTicks;
        int filledLength = (int) (progress * progressLength);

        StringBuilder progressBar = new StringBuilder();
        progressBar.append("§8[");
        for (int i = 0; i < progressLength; i++) {
            if (i < filledLength) {
                progressBar.append(plugin.getMessageUtils()
                        .getMessage(isOP ? "roll-progress-filled-op" : "roll-progress-filled-normal"));
            } else {
                progressBar.append(plugin.getMessageUtils().getMessage("roll-progress-empty"));
            }
        }
        progressBar.append("§8]");

        // Title animation

        String titleColor = isOP ? "§6§l" : "§d§l";
        String titleText = color + symbol + " " + titleColor + effect.getDisplayName() + " " + color + symbol;

        Component title = plugin.getMessageUtils().parse(titleText);
        Component subtitle = plugin.getMessageUtils().parse(progressBar.toString());

        Title.Times times = Title.Times.times(
                Duration.ZERO,
                Duration.ofMillis(200),
                Duration.ofMillis(50));

        player.showTitle(Title.title(title, subtitle, times));

        // Sound effect
        if (tick % 2 == 0) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.5f + progress * 1.5f);
        }
    }

    private void showFinalEffect(Player player, EffectType effect, boolean isOP) {
        // Epic final reveal
        String titleKey = isOP ? "roll-title-op" : "roll-title-normal";
        String subtitleKey = isOP ? "roll-subtitle-op" : "roll-subtitle-normal";

        String titleText = plugin.getMessageUtils().getMessage(titleKey).replace("%effect%", effect.getDisplayName());
        String subtitleText = plugin.getMessageUtils().getMessage(subtitleKey);

        Component title = plugin.getMessageUtils().parse(titleText);
        Component subtitle = plugin.getMessageUtils().parse(subtitleText);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(100),
                Duration.ofMillis(3000),
                Duration.ofMillis(500));

        player.showTitle(Title.title(title, subtitle, times));

        // Epic sound combo
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);

        // Send fancy chat message
        String border = plugin.getMessageUtils().getMessage(isOP ? "roll-chat-border-op" : "roll-chat-border-normal");
        String effectLine = plugin.getMessageUtils()
                .getMessage(isOP ? "roll-chat-line-effect-op" : "roll-chat-line-effect-normal")
                .replace("%effect%", effect.getDisplayName());

        String passiveLine = plugin.getMessageUtils()
                .getMessage(isOP ? "roll-chat-line-passive" : "roll-chat-line-passive-normal")
                .replace("%effect%", effect.getDisplayName());
        String activeLine = plugin.getMessageUtils()
                .getMessage(isOP ? "roll-chat-line-active" : "roll-chat-line-active-normal");

        player.sendMessage(plugin.getMessageUtils().parse(""));
        player.sendMessage(plugin.getMessageUtils().parse(border));
        player.sendMessage(plugin.getMessageUtils().parse(""));
        player.sendMessage(plugin.getMessageUtils().parse(effectLine));
        player.sendMessage(plugin.getMessageUtils().parse(""));
        player.sendMessage(plugin.getMessageUtils().parse(passiveLine));
        player.sendMessage(plugin.getMessageUtils().parse(activeLine));
        player.sendMessage(plugin.getMessageUtils().parse(""));
        player.sendMessage(plugin.getMessageUtils().parse(border));
        player.sendMessage(plugin.getMessageUtils().parse(""));
    }

    public void playQuickRoll(Player player, boolean isOP) {
        // Shorter animation for reroll items
        EffectType[] pool = isOP ? EffectType.getOPEffects() : EffectType.getNormalEffects();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        EffectType currentEffect = data.getEffect();

        EffectType finalEffect;
        do {
            finalEffect = pool[random.nextInt(pool.length)];
        } while (finalEffect == currentEffect && pool.length > 1);

        final EffectType targetEffect = finalEffect;

        new BukkitRunnable() {
            int tick = 0;
            final int totalTicks = 25; // Shorter animation

            @Override
            public void run() {
                if (tick >= totalTicks) {
                    showFinalEffect(player, targetEffect, isOP);

                    data.setEffect(targetEffect);
                    data.setPassiveEnabled(true);
                    plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
                    plugin.getEffectAbilityManager().applyPassiveEffect(player);

                    cancel();
                    return;
                }

                EffectType displayEffect = pool[random.nextInt(pool.length)];
                showRollingEffect(player, displayEffect, tick, totalTicks, isOP);
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
