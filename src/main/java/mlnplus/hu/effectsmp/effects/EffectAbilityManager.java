package mlnplus.hu.effectsmp.effects;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.data.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EffectAbilityManager {

    private final Effectsmp plugin;
    private final Random random = new Random();

    public EffectAbilityManager(Effectsmp plugin) {
        this.plugin = plugin;
    }

    private final java.util.Set<UUID> rollingPlayers = new java.util.HashSet<>();

    public boolean isRolling(Player player) {
        return rollingPlayers.contains(player.getUniqueId());
    }

    public void removeRolling(UUID uuid) {
        rollingPlayers.remove(uuid);
    }

    public void assignRandomEffect(Player player, boolean isOP) {
        if (isRolling(player)) {
            plugin.getMessageUtils().sendMessage(player, "reroll-in-progress");
            return;
        }

        rollingPlayers.add(player.getUniqueId());

        // Use roll animation for effect assignment
        plugin.getRollAnimationManager().playRollAnimation(player, isOP, () -> {
            rollingPlayers.remove(player.getUniqueId());
        });
    }

    public void assignRandomEffectNoAnimation(Player player, boolean isOP) {
        // Quick assignment without animation (for bulk operations)
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        EffectType currentEffect = data.getEffect();

        EffectType[] pool = isOP ? EffectType.getOPEffects() : EffectType.getNormalEffects();
        EffectType newEffect;

        do {
            newEffect = pool[random.nextInt(pool.length)];
        } while (newEffect == currentEffect && pool.length > 1);

        data.setEffect(newEffect);
        data.setPassiveEnabled(true);
        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
        applyPassiveEffect(player);

        // Simple message without animation
        plugin.getMessageUtils().sendMessage(player, "reroll-success",
                "%effect%", newEffect.getDisplayName());
    }

    public void applyPassiveEffect(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getEffect() == null || !data.isPassiveEnabled() || data.getEffectHearts() < 1) {
            return;
        }

        EffectType effect = data.getEffect();

        // Heart-based amplifier: 2+ hearts = level 2
        int amplifier = data.getPassiveAmplifier();

        // Safety check: Remove ANY other plugin effects that might be stuck
        // This prevents "multiple effects" bug
        for (PotionEffectType type : PotionEffectType.values()) {
            PotionEffect active = player.getPotionEffect(type);
            if (active != null) {
                // If it's infinite (or very long), we assume it's our passive
                // And if it's NOT the one we are about to apply... remove it!
                if (active.getDuration() > 100000) {
                    if (!type.equals(effect.getPotionEffect())) {
                        player.removePotionEffect(type);
                    }
                }
            }
        }

        // Apply infinite duration passive effect
        player.addPotionEffect(new PotionEffect(
                effect.getPotionEffect(),
                Integer.MAX_VALUE,
                amplifier, // 0 for level 1, 1 for level 2
                true, // ambient
                false, // particles
                true // icon
        ));
    }

    public void removePassiveEffect(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getEffect() == null) {
            // Even if data returns null, strict cleanup to prevent bugs
            // Loop through known EffectTypes and remove if they have long duration
            for (EffectType type : EffectType.values()) {
                PotionEffect active = player.getPotionEffect(type.getPotionEffect());
                if (active != null && active.getDuration() > 100000) {
                    player.removePotionEffect(type.getPotionEffect());
                }
            }
            return;
        }

        player.removePotionEffect(data.getEffect().getPotionEffect());

        // Extra cleanup: also remove any other stuck effects
        for (EffectType type : EffectType.values()) {
            PotionEffect active = player.getPotionEffect(type.getPotionEffect());
            if (active != null && active.getDuration() > 100000) {
                player.removePotionEffect(type.getPotionEffect());
            }
        }
    }

    public boolean activateAbility(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        UUID uuid = player.getUniqueId();

        if (data.getEffect() == null) {
            plugin.getMessageUtils().sendMessage(player, "no-effect");
            return false;
        }

        if (data.getEffectHearts() < 3) {
            plugin.getMessageUtils().sendMessage(player, "not-enough-hearts-active");
            return false;
        }

        if (data.isAbilityOnCooldown()) {
            long remaining = data.getRemainingCooldown();
            plugin.getMessageUtils().sendMessage(player, "active-cooldown",
                    "%time%", plugin.getMessageUtils().formatTime(remaining));
            return false;
        }

        if (data.isAbilityActive()) {
            plugin.getMessageUtils().sendMessage(player, "ability-already-active");
            return false;
        }

        // Execute ability based on effect type
        boolean success = executeAbility(player, data.getEffect());

        if (success) {
            data.setLastAbilityCooldown(System.currentTimeMillis());
            plugin.getPlayerDataManager().savePlayerData(uuid);

            // Schedule cooldown ready notification (skip for SPEED - uses dash action bar)
            if (data.getEffect() != EffectType.SPEED) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        plugin.getMessageUtils().sendActionBar(player,
                                plugin.getMessageUtils().getMessage("ability-ready"));
                        plugin.getMessageUtils().sendMessage(player, "ability-ready");
                    }
                }, data.getEffect().getCooldownSeconds() * 20L);
            }
        }

        return success;
    }

    private boolean executeAbility(Player player, EffectType effect) {
        UUID uuid = player.getUniqueId();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);

        switch (effect) {
            case INVISIBILITY -> {
                // TRUE INVISIBILITY: Vanish-like, armor hidden, no footsteps
                int duration = 15000; // 15 seconds

                // Hide player from all non-trusted players
                for (Player online : player.getServer().getOnlinePlayers()) {
                    if (!online.equals(player)) {
                        if (!plugin.getPlayerDataManager().isMutualTrust(uuid, online.getUniqueId())) {
                            online.hidePlayer(plugin, player);
                        }
                    }
                }

                // Schedule to show player again after duration
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        for (Player online : player.getServer().getOnlinePlayers()) {
                            if (!online.equals(player)) {
                                online.showPlayer(plugin, player);
                            }
                        }
                        plugin.getMessageUtils().sendMessage(player, "ability-invisibility-expired");
                    }
                }, duration / 50L); // Convert ms to ticks

                setAbilityDuration(data, duration);
                plugin.getMessageUtils().sendMessage(player, "ability-invisibility-activated");
                return true;
            }

            case HERO_OF_VILLAGE -> {
                // Hero of the Village 5 for 2 minutes - balanced, no change
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.HERO_OF_THE_VILLAGE, 2400, 4, false, true, true));
                setAbilityDuration(data, 120000);
                plugin.getMessageUtils().sendMessage(player, "ability-hero-activated");
                return true;
            }

            case HASTE -> {
                // 3x3 block mining for 20 seconds
                int duration = 20000; // 20 seconds
                data.setHaste3x3ActiveUntil(System.currentTimeMillis() + duration);

                // Also give haste 3 for faster mining
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.HASTE, 400, 2, false, true, true)); // Level 3

                setAbilityDuration(data, duration);
                plugin.getMessageUtils().sendMessage(player, "ability-haste-activated");

                // Schedule end notification
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        plugin.getMessageUtils().sendMessage(player, "ability-haste-expired");
                    }
                }, 400L);
                return true;
            }

            case FIRE_RESISTANCE -> {
                // Set all nearby enemies on unextinguishable fire for 15 seconds
                int affected = 0;
                for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
                    if (entity instanceof Player target && !target.equals(player)) {
                        if (!plugin.getPlayerDataManager().isMutualTrust(uuid, target.getUniqueId())) {
                            // Set on fire for 15 seconds (300 ticks)
                            target.setFireTicks(300);

                            // Schedule to re-ignite every second to make it unextinguishable
                            UUID targetUuid = target.getUniqueId();
                            for (int i = 1; i <= 15; i++) {
                                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                    Player t = Bukkit.getPlayer(targetUuid);
                                    if (t != null && t.isOnline()) {
                                        t.setFireTicks(Math.max(t.getFireTicks(), 40));
                                    }
                                }, i * 20L);
                            }

                            plugin.getMessageUtils().sendMessage(target, "ability-fireres-victim");
                            affected++;
                        }
                    }
                }

                setAbilityDuration(data, 15000);
                plugin.getMessageUtils().sendMessage(player, "ability-fireres-activated",
                        "%count%", String.valueOf(affected));
                return true;
            }

            case SPEED -> {
                // Speed now uses DASH (Shift-based) - this ability just reminds them
                plugin.getMessageUtils().sendMessage(player, "ability-speed-info");
                plugin.getMessageUtils().sendMessage(player, "ability-speed-hint");
                return false; // Don't trigger cooldown
            }

            case DOLPHIN_GRACE -> {
                // BUFFED: Water Breathing + Conduit Power for 1 minute
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.WATER_BREATHING, 1200, 0, false, true, true)); // 1 minute
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.CONDUIT_POWER, 1200, 0, false, true, true)); // 1 minute
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.REGENERATION, 1200, 2, false, true, true)); // 1 minute
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.DOLPHINS_GRACE, 1200, 2, false, true, true)); // 1 minute
                setAbilityDuration(data, 60000);
                plugin.getMessageUtils().sendMessage(player, "ability-dolphin-activated");
                return true;
            }

            case HEALTH_BOOST -> {
                // +10 hearts (amplifier 9 = 10 extra hearts, 20 total)
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.HEALTH_BOOST, 1200, 4, false, true, true)); // +10 hearts
                setAbilityDuration(data, 60000);
                plugin.getMessageUtils().sendMessage(player, "ability-health-activated");
                return true;
            }

            // OP Effects
            case RESISTANCE -> {
                // COMPLETE INVINCIBILITY for 5 seconds
                int duration = 5000; // 5 seconds

                // Resistance 255 = complete damage immunity
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.RESISTANCE, 100, 254, false, true, true)); // Amplifier 254 = level 255

                setAbilityDuration(data, duration);
                plugin.getMessageUtils().sendMessage(player, "ability-resistance-activated");

                // Schedule end notification
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        plugin.getMessageUtils().sendMessage(player, "ability-resistance-expired");
                    }
                }, 100L);
                return true;
            }

            case STRENGTH -> {
                // Strength 3 + Extended Reach for 5 seconds
                int duration = 10000; // 10 seconds

                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.STRENGTH, 100, 2, false, true, true)); // Level 3

                // Add extended reach using generic attribute (1.21+ compatible)
                try {
                    // Try to add reach modifiers using reflection-safe approach
                    AttributeInstance entityReach = player
                            .getAttribute(Attribute.valueOf("PLAYER_ENTITY_INTERACTION_RANGE"));
                    AttributeInstance blockReach = player
                            .getAttribute(Attribute.valueOf("PLAYER_BLOCK_INTERACTION_RANGE"));

                    org.bukkit.NamespacedKey modifierKey = new org.bukkit.NamespacedKey(plugin,
                            "strength_reach_" + uuid);
                    AttributeModifier reachMod = new AttributeModifier(modifierKey, 3.0,
                            AttributeModifier.Operation.ADD_NUMBER);

                    if (entityReach != null)
                        entityReach.addModifier(reachMod);
                    if (blockReach != null)
                        blockReach.addModifier(reachMod);

                    // Schedule to remove reach modifier
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline()) {
                            try {
                                AttributeInstance er = player
                                        .getAttribute(Attribute.valueOf("PLAYER_ENTITY_INTERACTION_RANGE"));
                                AttributeInstance br = player
                                        .getAttribute(Attribute.valueOf("PLAYER_BLOCK_INTERACTION_RANGE"));
                                if (er != null)
                                    er.removeModifier(modifierKey);
                                if (br != null)
                                    br.removeModifier(modifierKey);
                            } catch (Exception ignored) {
                            }
                            plugin.getMessageUtils().sendMessage(player, "ability-strength-expired");
                        }
                    }, 100L);
                } catch (Exception e) {
                    // Fallback: just give speed for mobility if reach isn't available
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.SPEED, 100, 1, false, true, true));
                }

                setAbilityDuration(data, duration);
                plugin.getMessageUtils().sendMessage(player, "ability-strength-activated");
                return true;
            }

            case REGENERATION -> {
                // BALANCED: Regeneration 2 for 45 seconds to self and trusts
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.REGENERATION, 900, 1, false, true, true)); // 45 seconds

                List<UUID> mutualTrusted = plugin.getPlayerDataManager().getMutualTrustedPlayers(uuid);
                int affected = 0;
                for (UUID trustedUuid : mutualTrusted) {
                    Player trusted = Bukkit.getPlayer(trustedUuid);
                    if (trusted != null && trusted.isOnline()) {
                        trusted.addPotionEffect(new PotionEffect(
                                PotionEffectType.REGENERATION, 900, 1, false, true, true));
                        plugin.getMessageUtils().sendMessage(trusted, "ability-regen-received",
                                "%player%", player.getName());
                        affected++;
                    }
                }

                setAbilityDuration(data, 45000);
                plugin.getMessageUtils().sendMessage(player, "ability-regen-activated",
                        "%count%", String.valueOf(affected));
                return true;
            }

            default -> {
                return false;
            }
        }
    }

    private void setAbilityDuration(PlayerData data, long durationMs) {
        data.setAbilityActiveUntil(System.currentTimeMillis() + durationMs);
    }

    public void togglePassive(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getEffect() == null) {
            plugin.getMessageUtils().sendMessage(player, "no-effect");
            return;
        }

        if (data.isPassiveEnabled()) {
            data.setPassiveEnabled(false);
            removePassiveEffect(player);
            plugin.getMessageUtils().sendMessage(player, "passive-toggle-off");
        } else {
            data.setPassiveEnabled(true);
            applyPassiveEffect(player);
            plugin.getMessageUtils().sendMessage(player, "passive-toggle-on");
        }

        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
    }

    public void clearAbilityCooldown(UUID uuid) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        data.clearAbilityCooldown();
    }
}
