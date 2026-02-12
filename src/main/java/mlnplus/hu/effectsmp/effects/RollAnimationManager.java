package mlnplus.hu.effectsmp.effects;

import mlnplus.hu.effectsmp.Effectsmp;
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

    private static final int ROLL_TICKS = 60;

    private final java.util.Set<java.util.UUID> rollingPlayers = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public RollAnimationManager(Effectsmp plugin) {
        this.plugin = plugin;
    }

    public boolean isRolling(java.util.UUID uuid) {
        return rollingPlayers.contains(uuid);
    }

    public void removeRolling(java.util.UUID uuid) {
        rollingPlayers.remove(uuid);
    }

    public void playRollAnimation(Player player, boolean isOP, Runnable onComplete) {
        if (isRolling(player.getUniqueId()))
            return;
        rollingPlayers.add(player.getUniqueId());

        EffectType[] effects = isOP ? EffectType.getOPEffects() : EffectType.values();

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    removeRolling(player.getUniqueId());
                    cancel();
                    return;
                }

                if (tick >= ROLL_TICKS) {
                    removeRolling(player.getUniqueId());
                    onComplete.run();
                    cancel();
                    return;
                }

                if (tick % 5 == 0) {
                    EffectType display = effects[random.nextInt(effects.length)];
                    String color = isOP ? "§6" : "§d";

                    String titleText = display.getName() + "...";
                    Component titleComp = plugin.getMessageUtils()
                            .parse(isOP ? "<gold>🎲 " + titleText : "<light_purple>🎲 " + titleText);

                    player.showTitle(Title.title(titleComp, Component.empty(),
                            Title.Times.times(Duration.ZERO, Duration.ofMillis(500), Duration.ZERO)));

                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 2.0f);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
