package mlnplus.hu.effectsmp.effects;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class EffectAbilityManager {

    private final Effectsmp plugin;
    private final Random random = new Random();

    public EffectAbilityManager(Effectsmp plugin) {
        this.plugin = plugin;
    }

    public void assignRandomEffect(Player player, boolean isOP) {
        EffectType[] effects = isOP ? EffectType.getOPEffects() : EffectType.getNormalEffects();
        EffectType selected = effects[random.nextInt(effects.length)];

        plugin.getRollAnimationManager().playRollAnimation(player, isOP, () -> {
            setPlayerEffect(player, selected);
        });
    }

    public void setPlayerEffect(Player player, EffectType effect) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        removePassiveEffect(player);

        data.setEffect(effect);
        data.setPassiveEnabled(true);
        data.setEffectHearts(1);
        data.setHasEffectShard(true);
        data.setFirstDeathOccurred(false);
        data.clearAbilityCooldown();

        applyPassiveEffect(player);
        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());

        plugin.getMessageUtils().sendMessage(player, "effect-assigned", "%effect%", effect.getDisplayName());
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    public void applyPassiveEffect(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getEffect() == null || !data.isPassiveEnabled()) {
            return;
        }

        PotionEffectType type = data.getEffect().getPotionEffect();
        int amplifier = data.getPassiveAmplifier() + data.getEffect().getPassiveAmplifier();

        if (player.hasPotionEffect(type)) {
            player.removePotionEffect(type);
        }

        player.addPotionEffect(new PotionEffect(type, PotionEffect.INFINITE_DURATION, amplifier, false, false));
    }

    public void removePassiveEffect(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getEffect() != null) {
            PotionEffectType type = data.getEffect().getPotionEffect();
            if (player.hasPotionEffect(type)) {
                player.removePotionEffect(type);
            }
        }
    }

    public void togglePassive(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getEffect() == null) {
            plugin.getMessageUtils().sendMessage(player, "no-effect");
            return;
        }

        boolean newState = !data.isPassiveEnabled();
        data.setPassiveEnabled(newState);

        if (newState) {
            applyPassiveEffect(player);
            plugin.getMessageUtils().sendMessage(player, "passive-enabled");
        } else {
            removePassiveEffect(player);
            plugin.getMessageUtils().sendMessage(player, "passive-disabled");
        }

        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
    }

    public void activateAbility(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getEffect() == null) {
            plugin.getMessageUtils().sendMessage(player, "no-effect");
            return;
        }

        if (!data.canUseAbility()) {
            if (data.getEffectHearts() < 2) {
                plugin.getMessageUtils().sendMessage(player, "ability-level-too-low");
            } else if (data.isAbilityActive()) {
                plugin.getMessageUtils().sendMessage(player, "ability-already-active");
            } else {
                long remaining = data.getRemainingCooldown();
                plugin.getMessageUtils().sendMessage(player, "ability-cooldown",
                        "%time%", plugin.getMessageUtils().formatTime(remaining));
            }
            return;
        }

        boolean success = executeAbility(player, data.getEffect());

        if (success) {
            data.setLastAbilityCooldown(System.currentTimeMillis());
            plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());

            int cooldown = data.getEffectiveCooldownSeconds();

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        plugin.getActionBarManager().sendCooldownReady(player);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    }
                }
            }.runTaskLater(plugin, cooldown * 20L);
        }
    }

    private boolean executeAbility(Player player, EffectType effect) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        long duration;

        switch (effect) {
            case INVISIBILITY -> {
                duration = 10000;
                setAbilityActive(player, data, duration);

                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.INVISIBILITY, (int) (duration / 50), 0, false, false));

                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!online.equals(player) && !plugin.getPlayerDataManager().isMutualTrust(player.getUniqueId(),
                            online.getUniqueId())) {
                        online.hidePlayer(plugin, player);
                    }
                }

                plugin.getMessageUtils().sendMessage(player, "ability-invisibility-activated");
                player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.0f);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                online.showPlayer(plugin, player);
                            }
                            plugin.getActionBarManager().sendAbilityExpired(player);
                            plugin.getMessageUtils().sendMessage(player, "ability-invisibility-expired");
                        }
                    }
                }.runTaskLater(plugin, duration / 50);
                return true;
            }

            case HERO_OF_VILLAGE -> {
                duration = 120000;
                setAbilityActive(player, data, duration);

                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, (int) (duration / 50), 4, false, false));
                plugin.getMessageUtils().sendMessage(player, "ability-hero-activated");
                player.playSound(player.getLocation(), Sound.EVENT_RAID_HORN, 1.0f, 1.0f);
                return true;
            }

            case HASTE -> {
                duration = 30000;
                setAbilityActive(player, data, duration);
                data.setHaste3x3ActiveUntil(System.currentTimeMillis() + duration);

                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.HASTE, (int) (duration / 50), 2, false, false));
                plugin.getMessageUtils().sendMessage(player, "ability-haste-activated");
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
                return true;
            }

            case FIRE_RESISTANCE -> {
                duration = 15000;
                setAbilityActive(player, data, duration);

                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int) (duration / 50), 0, false, false));
                player.setFireTicks(0);
                plugin.getMessageUtils().sendMessage(player, "ability-fire-activated");
                player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
                return true;
            }

            case SPEED -> {
                plugin.getMessageUtils().sendMessage(player, "ability-speed-info");
                return false;
            }

            case DOLPHIN_GRACE -> {
                duration = 15000;
                setAbilityActive(player, data, duration);

                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.DOLPHINS_GRACE, (int) (duration / 50), 0, false, false));
                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.WATER_BREATHING, (int) (duration / 50), 0, false, false));
                plugin.getMessageUtils().sendMessage(player, "ability-dolphin-activated");
                player.playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_SPLASH, 1.0f, 1.0f);
                return true;
            }

            case HEALTH_BOOST -> {
                duration = 30000;
                setAbilityActive(player, data, duration);

                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.HEALTH_BOOST, (int) (duration / 50), 4, false, false)); // +10
                                                                                                                  // hearts
                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.REGENERATION, 100, 2, false, false)); // Quick heal
                plugin.getMessageUtils().sendMessage(player, "ability-health-activated");
                player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);
                return true;
            }

            case RESISTANCE -> {
                duration = 20000;
                setAbilityActive(player, data, duration);

                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.RESISTANCE, (int) (duration / 50), 2, false, false));
                plugin.getMessageUtils().sendMessage(player, "ability-resistance-activated");
                player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
                return true;
            }

            case STRENGTH -> {
                duration = 15000;
                setAbilityActive(player, data, duration);

                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.STRENGTH, (int) (duration / 50), 2, false, false));
                plugin.getMessageUtils().sendMessage(player, "ability-strength-activated");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                return true;
            }

            case REGENERATION -> {
                duration = 30000;
                setAbilityActive(player, data, duration);

                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.REGENERATION, (int) (duration / 50), 2, false, false));
                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.ABSORPTION, (int) (duration / 50), 1, false, false));
                plugin.getMessageUtils().sendMessage(player, "ability-regeneration-activated");
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
                return true;
            }
        }

        return false;
    }

    private void setAbilityActive(Player player, PlayerData data, long durationMillis) {
        data.setAbilityActiveUntil(System.currentTimeMillis() + durationMillis);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && data.getEffect() != null && data.getEffect() != EffectType.INVISIBILITY) {
                    plugin.getActionBarManager().sendAbilityExpired(player);
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.5f);
                }
            }
        }.runTaskLater(plugin, durationMillis / 50);
    }

    public void clearAbilityCooldown(UUID uuid) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        data.clearAbilityCooldown();
        plugin.getPlayerDataManager().savePlayerData(uuid);
    }

    public void removeRolling(UUID uuid) {
        plugin.getRollAnimationManager().removeRolling(uuid);
    }

    public boolean isRolling(Player player) {
        return plugin.getRollAnimationManager().isRolling(player.getUniqueId());
    }
}
