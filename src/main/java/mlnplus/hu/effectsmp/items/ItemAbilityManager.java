package mlnplus.hu.effectsmp.items;

import mlnplus.hu.effectsmp.Effectsmp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ItemAbilityManager {

    private final Effectsmp plugin;
    private final Map<UUID, Long> maceCooldowns = new HashMap<>();
    private final Map<UUID, Long> swordCooldowns = new HashMap<>();
    private final Map<UUID, Long> bowCooldowns = new HashMap<>();
    private final Map<UUID, Long> scytheCooldowns = new HashMap<>();

    // Freeze management
    private final Map<UUID, FreezeInfo> frozenPlayers = new HashMap<>();

    private final Map<UUID, Long> swordActiveUntil = new HashMap<>();
    private final Set<UUID> maceFlying = new HashSet<>();

    private static final long MACE_COOLDOWN = 60 * 1000; // 1 minute
    private static final long SWORD_COOLDOWN = 150 * 1000; // 2.5 minutes
    private static final long SCYTHE_COOLDOWN = 150 * 1000; // 2.5 minutes
    private static final long BOW_COOLDOWN = 90 * 1000; // 1.5 minutes

    public ItemAbilityManager(Effectsmp plugin) {
        this.plugin = plugin;
    }

    public boolean activateMace(Player player) {
        UUID uuid = player.getUniqueId();

        // Check heart requirement
        mlnplus.hu.effectsmp.data.PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data.getEffectHearts() < 3) {
            plugin.getMessageUtils().sendMessage(player, "not-enough-hearts-active");
            return false;
        }

        if (isOnCooldown(maceCooldowns, uuid, MACE_COOLDOWN)) {
            long remaining = getRemainingCooldown(maceCooldowns, uuid, MACE_COOLDOWN);
            plugin.getMessageUtils().sendMessage(player, "mace-cooldown",
                    "%time%", plugin.getMessageUtils().formatTime(remaining));
            return false;
        }

        // Launch player upward (nerfed from 2.5)
        Vector velocity = player.getVelocity();
        velocity.setY(1.8); // Moderate upward boost
        player.setVelocity(velocity);

        // Track player for landing shockwave
        maceFlying.add(uuid);

        maceCooldowns.put(uuid, System.currentTimeMillis());

        plugin.getMessageUtils().sendMessage(player, "mace-activated");

        return true;
    }

    public void checkMaceLanding(Player player) {
        UUID uuid = player.getUniqueId();
        if (!maceFlying.contains(uuid))
            return;

        // Check if player is on ground
        if (player.isOnGround()) {
            maceFlying.remove(uuid);

            // Create shockwave
            Location loc = player.getLocation();

            // Visual effect
            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 1);
            loc.getWorld().spawnParticle(Particle.CLOUD, loc, 30, 2, 0.5, 2, 0.1);

            // Sound
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);

            // Knockback nearby enemies
            for (Entity entity : player.getNearbyEntities(6, 3, 6)) {
                if (entity instanceof Player target && !target.equals(player)) {
                    if (!plugin.getPlayerDataManager().isMutualTrust(uuid, target.getUniqueId())) {
                        // Calculate knockback direction (nerfed from 1.5)
                        Vector knockback = target.getLocation().toVector()
                                .subtract(loc.toVector())
                                .normalize()
                                .multiply(1.2)
                                .setY(0.4);
                        target.setVelocity(knockback);
                        plugin.getMessageUtils().sendMessage(target, "mace-victim");
                    }
                }
            }

            plugin.getMessageUtils().sendActionBar(player, plugin.getMessageUtils().getMessage("mace-landing"));
        }
    }

    public boolean activateSword(Player player) {
        UUID uuid = player.getUniqueId();

        // Check heart requirement
        mlnplus.hu.effectsmp.data.PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data.getEffectHearts() < 3) {
            plugin.getMessageUtils().sendMessage(player, "not-enough-hearts-active");
            return false;
        }

        if (isOnCooldown(swordCooldowns, uuid, SWORD_COOLDOWN)) {
            long remaining = getRemainingCooldown(swordCooldowns, uuid, SWORD_COOLDOWN);
            plugin.getMessageUtils().sendMessage(player, "sword-cooldown",
                    "%time%", plugin.getMessageUtils().formatTime(remaining));
            return false;
        }

        // Give haste for attack speed
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 200, 1, false, true, true));

        swordActiveUntil.put(uuid, System.currentTimeMillis() + 10000); // 10 seconds
        swordCooldowns.put(uuid, System.currentTimeMillis());

        plugin.getMessageUtils().sendMessage(player, "sword-activated");

        // Schedule end notification
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.getMessageUtils().sendMessage(player, "sword-expired");
            }
        }, 200L);

        return true;
    }

    public boolean isSwordAbilityActive(Player player) {
        UUID uuid = player.getUniqueId();
        Long activeUntil = swordActiveUntil.get(uuid);
        return activeUntil != null && System.currentTimeMillis() < activeUntil;
    }

    public boolean isMaceFlying(UUID uuid) {
        return maceFlying.contains(uuid);
    }

    public boolean isFrozen(UUID uuid) {
        return frozenPlayers.containsKey(uuid);
    }

    public Location getFreezeLocation(UUID uuid) {
        FreezeInfo info = frozenPlayers.get(uuid);
        return info != null ? info.location : null;
    }

    public boolean activateScythe(Player player) {
        UUID uuid = player.getUniqueId();

        // Check heart requirement
        mlnplus.hu.effectsmp.data.PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data.getEffectHearts() < 3) {
            plugin.getMessageUtils().sendMessage(player, "not-enough-hearts-active");
            return false;
        }

        if (isOnCooldown(scytheCooldowns, uuid, SCYTHE_COOLDOWN)) {
            long remaining = getRemainingCooldown(scytheCooldowns, uuid, SCYTHE_COOLDOWN);
            plugin.getMessageUtils().sendMessage(player, "scythe-cooldown",
                    "%time%", plugin.getMessageUtils().formatTime(remaining));
            return false;
        }

        int affected = 0;

        // Full freeze all non-trusted players in radius
        for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
            if (entity instanceof Player target && !target.equals(player)) {
                // Check if mutually trusted
                if (!plugin.getPlayerDataManager().isMutualTrust(uuid, target.getUniqueId())) {

                    // TRUE FREEZE IMPLEMENTATION - Only freeze, no extra effects

                    // 1. Stop all movement immediately
                    target.setVelocity(new Vector(0, 0, 0));

                    // 2. Track freeze state
                    UUID targetUuid = target.getUniqueId();
                    frozenPlayers.put(targetUuid,
                            new FreezeInfo(target.getLocation()));

                    // 3. Apply Knockback Resistance to prevent flying on hit (1.21 compatible)
                    try {
                        // Use string lookup for compatibility
                        AttributeInstance kbResist = target
                                .getAttribute(Attribute.valueOf("GENERIC_KNOCKBACK_RESISTANCE"));
                        if (kbResist != null) {
                            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin,
                                    "freeze_kb_" + targetUuid);
                            // Remove existing if present (refresh)
                            try {
                                kbResist.removeModifier(key);
                            } catch (Exception ignored) {
                            }

                            // Add 1.0 resistance (100%)
                            AttributeModifier modifier = new AttributeModifier(key, 1.0,
                                    AttributeModifier.Operation.ADD_NUMBER);
                            kbResist.addModifier(modifier);
                        }
                    } catch (Exception ignored) {
                        try {
                            // Fallback for older/different names
                            AttributeInstance kbResist = target.getAttribute(Attribute.valueOf("KNOCKBACK_RESISTANCE"));
                            if (kbResist != null) {
                                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin,
                                        "freeze_kb_" + targetUuid);
                                AttributeModifier modifier = new AttributeModifier(key, 1.0,
                                        AttributeModifier.Operation.ADD_NUMBER);
                                kbResist.addModifier(modifier);
                            }
                        } catch (Exception e) {
                        }
                    }

                    // 4. Note: No periodic velocity reset - FreezeListener handles movement
                    // Knockback resistance attribute prevents flying from hits

                    // 5. Schedule removal
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {

                        frozenPlayers.remove(targetUuid);
                        // Remove attribute modifier
                        if (target.isOnline()) {
                            try {
                                AttributeInstance kb = target
                                        .getAttribute(Attribute.valueOf("GENERIC_KNOCKBACK_RESISTANCE"));
                                if (kb != null) {
                                    org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin,
                                            "freeze_kb_" + targetUuid);
                                    kb.removeModifier(key);
                                }
                                plugin.getMessageUtils().sendMessage(target, "scythe-thaw");
                            } catch (Exception ignored) {
                                try {
                                    AttributeInstance kb = target
                                            .getAttribute(Attribute.valueOf("KNOCKBACK_RESISTANCE"));
                                    if (kb != null) {
                                        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin,
                                                "freeze_kb_" + targetUuid);
                                        kb.removeModifier(key);
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                    }, 100L); // 5 seconds

                    plugin.getMessageUtils().sendMessage(target, "scythe-victim");
                    affected++;
                }
            }
        }

        scytheCooldowns.put(uuid, System.currentTimeMillis());

        if (affected > 0) {
            plugin.getMessageUtils().sendMessage(player, "scythe-activated",
                    "%count%", String.valueOf(affected));
        } else {
            plugin.getMessageUtils().sendMessage(player, "scythe-no-enemies");
        }

        return true;
    }

    public boolean canBowApplyDebuffs(Player player) {
        // Bow's debuff ability is passive when equipped, but on cooldown after use
        UUID uuid = player.getUniqueId();
        return !isOnCooldown(bowCooldowns, uuid, BOW_COOLDOWN);
    }

    public void triggerBowDebuffs(Player player, Location location) {
        UUID uuid = player.getUniqueId();

        // Spawn particle effect
        location.getWorld().spawnParticle(
                org.bukkit.Particle.WITCH, location, 50, 2.5, 1.5, 2.5, 0.05);
        location.getWorld().spawnParticle(
                org.bukkit.Particle.LARGE_SMOKE, location, 30, 2, 1, 2, 0.02);

        // Apply debuffs to enemies in radius
        int affected = 0;
        for (Entity entity : location.getWorld().getNearbyEntities(location, 5, 5, 5)) {
            if (entity instanceof Player target && !target.equals(player)) {
                // Check if not mutually trusted
                if (!plugin.getPlayerDataManager().isMutualTrust(uuid, target.getUniqueId())) {
                    // Apply debuffs
                    target.addPotionEffect(new PotionEffect(
                            PotionEffectType.SLOWNESS, 160, 1, false, true, true)); // 8 seconds
                    target.addPotionEffect(new PotionEffect(
                            PotionEffectType.WEAKNESS, 160, 0, false, true, true)); // 8 seconds
                    target.addPotionEffect(new PotionEffect(
                            PotionEffectType.GLOWING, 200, 0, false, true, true)); // 10 seconds

                    plugin.getMessageUtils().sendMessage(target, "bow-hit-victim");
                    affected++;
                }
            }
        }

        bowCooldowns.put(uuid, System.currentTimeMillis());

        if (affected > 0) {
            plugin.getMessageUtils().sendMessage(player, "bow-activated",
                    "%count%", String.valueOf(affected));
        }
    }

    public long getItemCooldown(String itemType, UUID uuid) {
        Map<UUID, Long> cooldownMap = switch (itemType) {
            case "effect_mace" -> maceCooldowns;
            case "effect_sword" -> swordCooldowns;
            case "effect_bow" -> bowCooldowns;
            case "effect_scythe" -> scytheCooldowns;
            default -> null;
        };

        if (cooldownMap == null)
            return 0;

        long duration = switch (itemType) {
            case "effect_mace" -> MACE_COOLDOWN;
            case "effect_sword" -> SWORD_COOLDOWN;
            case "effect_bow" -> BOW_COOLDOWN;
            case "effect_scythe" -> SCYTHE_COOLDOWN;
            default -> 0;
        };

        return getRemainingCooldown(cooldownMap, uuid, duration);
    }

    private boolean isOnCooldown(Map<UUID, Long> cooldownMap, UUID uuid, long duration) {
        Long lastUse = cooldownMap.get(uuid);
        if (lastUse == null)
            return false;
        return System.currentTimeMillis() < lastUse + duration;
    }

    private long getRemainingCooldown(Map<UUID, Long> cooldownMap, UUID uuid, long duration) {
        Long lastUse = cooldownMap.get(uuid);
        if (lastUse == null)
            return 0;
        long end = lastUse + duration;
        return Math.max(0, end - System.currentTimeMillis());
    }

    public void clearAllItemCooldowns(UUID uuid) {
        maceCooldowns.remove(uuid);
        swordCooldowns.remove(uuid);
        swordActiveUntil.remove(uuid);
        scytheCooldowns.remove(uuid);
        bowCooldowns.remove(uuid);
        maceFlying.remove(uuid);
        frozenPlayers.remove(uuid);
    }

    // Helper record for freeze info
    // Helper record for freeze info
    private static class FreezeInfo {
        final Location location;

        FreezeInfo(Location location) {
            this.location = location;
        }
    }
}
