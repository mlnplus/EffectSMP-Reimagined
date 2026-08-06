package mlnplus.hu.effectsmp.effects;

import mlnplus.hu.effectsmp.Effectsmp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.Random;

@SuppressWarnings("null")
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

                Location loc = player.getLocation();
                if (loc != null) {
                    // Spawn spinning particle spiral around player
                    double radius = 1.2;
                    double angle = tick * 0.4;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    double y = (tick % 20) * 0.1;
                    Location particleLoc = loc.clone().add(x, y, z);
                    
                    Particle pType = isOP ? Particle.END_ROD : Particle.WITCH;
                    loc.getWorld().spawnParticle(pType, particleLoc, 2, 0.02, 0.02, 0.02, 0.01);
                }

                if (tick % 4 == 0) {
                    EffectType display = effects[random.nextInt(effects.length)];

                    String displayName = display.getDisplayName();
                    Component titleComp = plugin.getMessageUtils()
                            .parse("🎲 " + displayName + "...");

                    player.showTitle(Title.title(titleComp, Component.empty(),
                            Title.Times.times(Duration.ZERO, Duration.ofMillis(350), Duration.ofMillis(100))));

                    if (loc != null) {
                        float pitch = 0.8f + ((float) tick / ROLL_TICKS) * 1.2f;
                        player.playSound(loc, Sound.UI_BUTTON_CLICK, 0.8f, pitch);
                    }
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
