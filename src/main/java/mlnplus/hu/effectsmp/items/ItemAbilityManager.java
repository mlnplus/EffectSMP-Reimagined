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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("null")
public class ItemAbilityManager {

    private final Effectsmp plugin;
    private final Map<UUID, Long> maceCooldowns = new HashMap<>();
    private final Map<UUID, Long> swordCooldowns = new HashMap<>();
    private final Map<UUID, Long> scytheCooldowns = new HashMap<>();
    private final Map<UUID, Long> spearCooldowns = new HashMap<>();
    private final Map<UUID, Long> spearChargeStart = new HashMap<>();
    private final Set<UUID> spearLunging = new HashSet<>();
    private final Set<UUID> spearCharging = new HashSet<>();
    private final Map<UUID, Long> lastLungeTime = new HashMap<>();
    private final Map<UUID, Float> initialSaturation = new HashMap<>();
    private final Map<UUID, Integer> initialFood = new HashMap<>();
    private final Map<UUID, Set<UUID>> spearLungeDamaged = new HashMap<>();

    private final Map<UUID, FreezeInfo> frozenPlayers = new HashMap<>();

    private final Map<UUID, Long> swordActiveUntil = new HashMap<>();
    private final Set<UUID> maceFlying = new HashSet<>();

    public long getMaceCooldown() {
        return plugin.getConfigManager().getItemsConfig().getLong("effect_mace.cooldown", 60) * 1000L;
    }

    public long getSwordCooldown() {
        return plugin.getConfigManager().getItemsConfig().getLong("effect_sword.cooldown", 150) * 1000L;
    }

    public long getScytheCooldown() {
        return plugin.getConfigManager().getItemsConfig().getLong("effect_scythe.cooldown", 150) * 1000L;
    }

    public long getSpearCooldown() {
        return plugin.getConfigManager().getItemsConfig().getLong("effect_spear.cooldown", 10) * 1000L;
    }

    public ItemAbilityManager(Effectsmp plugin) {
        this.plugin = plugin;
    }

    public boolean activateMace(Player player) {
        UUID uuid = player.getUniqueId();

        mlnplus.hu.effectsmp.data.PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data.getEffectHearts() < 2) {
            plugin.getMessageUtils().sendMessage(player, "not-enough-hearts-item");
            return false;
        }

        if (isOnCooldown(maceCooldowns, uuid, getMaceCooldown())) {
            long remaining = getRemainingCooldown(maceCooldowns, uuid, getMaceCooldown());
            plugin.getMessageUtils().sendMessage(player, "mace-cooldown",
                    "%time%", plugin.getMessageUtils().formatTime(remaining));
            return false;
        }

        Vector velocity = player.getVelocity();
        velocity.setY(1.8);
        player.setVelocity(velocity);

        maceFlying.add(uuid);

        maceCooldowns.put(uuid, System.currentTimeMillis());

        plugin.getMessageUtils().sendMessage(player, "mace-activated");

        return true;
    }

    @SuppressWarnings("deprecation")
    public void checkMaceLanding(Player player) {
        UUID uuid = player.getUniqueId();
        if (!maceFlying.contains(uuid))
            return;

        if (player.isOnGround()) {
            maceFlying.remove(uuid);

            Location loc = player.getLocation();
            if (loc == null) return;

            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 1);
            loc.getWorld().spawnParticle(Particle.CLOUD, loc, 30, 2, 0.5, 2, 0.1);

            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);

            for (Entity entity : player.getNearbyEntities(6, 3, 6)) {
                if (entity instanceof Player target && !target.equals(player)) {
                    if (!plugin.getPlayerDataManager().isMutualTrust(uuid, target.getUniqueId())) {
                        Location targetLoc = target.getLocation();
                        if (targetLoc != null) {
                            Vector knockback = targetLoc.toVector()
                                    .subtract(loc.toVector())
                                    .normalize()
                                    .multiply(1.2)
                                    .setY(0.4);
                            target.setVelocity(knockback);
                            plugin.getMessageUtils().sendMessage(target, "mace-victim");
                        }
                    }
                }
            }

            plugin.getMessageUtils().sendActionBar(player, plugin.getMessageUtils().getMessage("mace-landing"));
        }
    }

    public boolean activateSword(Player player) {
        UUID uuid = player.getUniqueId();

        mlnplus.hu.effectsmp.data.PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data.getEffectHearts() < 2) {
            plugin.getMessageUtils().sendMessage(player, "not-enough-hearts-item");
            return false;
        }

        if (isOnCooldown(swordCooldowns, uuid, getSwordCooldown())) {
            long remaining = getRemainingCooldown(swordCooldowns, uuid, getSwordCooldown());
            plugin.getMessageUtils().sendMessage(player, "sword-cooldown",
                    "%time%", plugin.getMessageUtils().formatTime(remaining));
            return false;
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 200, 1, false, true, true));

        swordActiveUntil.put(uuid, System.currentTimeMillis() + 10000);
        swordCooldowns.put(uuid, System.currentTimeMillis());

        plugin.getMessageUtils().sendMessage(player, "sword-activated");

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

        mlnplus.hu.effectsmp.data.PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data.getEffectHearts() < 2) {
            plugin.getMessageUtils().sendMessage(player, "not-enough-hearts-item");
            return false;
        }

        if (isOnCooldown(scytheCooldowns, uuid, getScytheCooldown())) {
            long remaining = getRemainingCooldown(scytheCooldowns, uuid, getScytheCooldown());
            plugin.getMessageUtils().sendMessage(player, "scythe-cooldown",
                    "%time%", plugin.getMessageUtils().formatTime(remaining));
            return false;
        }

        int affected = 0;

        for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
            if (entity instanceof Player target && !target.equals(player)) {
                if (!plugin.getPlayerDataManager().isMutualTrust(uuid, target.getUniqueId())) {

                    target.setVelocity(new Vector(0, 0, 0));

                    UUID targetUuid = target.getUniqueId();
                    frozenPlayers.put(targetUuid,
                            new FreezeInfo(target.getLocation()));

                    AttributeInstance kbResist = target.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
                    if (kbResist != null) {
                        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin,
                                "freeze_kb_" + targetUuid);
                        try {
                            kbResist.removeModifier(key);
                        } catch (Exception ignored) {
                        }

                        AttributeModifier modifier = new AttributeModifier(key, 1.0,
                                AttributeModifier.Operation.ADD_NUMBER);
                        kbResist.addModifier(modifier);
                    }

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {

                        frozenPlayers.remove(targetUuid);
                        if (target.isOnline()) {
                            removeFreezeAttribute(target);
                            plugin.getMessageUtils().sendMessage(target, "scythe-thaw");
                        }
                    }, 100L);

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

    public void triggerBowDebuffs(Player player, Location location) {
        UUID uuid = player.getUniqueId();

        location.getWorld().spawnParticle(
                org.bukkit.Particle.WITCH, location, 50, 2.5, 1.5, 2.5, 0.05);
        location.getWorld().spawnParticle(
                org.bukkit.Particle.LARGE_SMOKE, location, 30, 2, 1, 2, 0.02);

        int affected = 0;
        for (Entity entity : location.getWorld().getNearbyEntities(location, 5, 5, 5)) {
            if (entity instanceof Player target && !target.equals(player)) {
                if (!plugin.getPlayerDataManager().isMutualTrust(uuid, target.getUniqueId())) {
                    target.addPotionEffect(new PotionEffect(
                            PotionEffectType.SLOWNESS, 160, 1, false, true, true));
                    target.addPotionEffect(new PotionEffect(
                            PotionEffectType.WEAKNESS, 160, 0, false, true, true));
                    target.addPotionEffect(new PotionEffect(
                            PotionEffectType.GLOWING, 200, 0, false, true, true));

                    plugin.getMessageUtils().sendMessage(target, "bow-hit-victim");
                    affected++;
                }
            }
        }

        if (affected > 0) {
            plugin.getMessageUtils().sendMessage(player, "bow-activated",
                    "%count%", String.valueOf(affected));
        }
    }

    public long getItemCooldown(String itemType, UUID uuid) {
        Map<UUID, Long> cooldownMap = switch (itemType) {
            case "effect_mace" -> maceCooldowns;
            case "effect_sword" -> swordCooldowns;
            case "effect_scythe" -> scytheCooldowns;
            case "effect_spear" -> spearCooldowns;
            default -> null;
        };

        if (cooldownMap == null)
            return 0;

        long duration = switch (itemType) {
            case "effect_mace" -> getMaceCooldown();
            case "effect_sword" -> getSwordCooldown();
            case "effect_scythe" -> getScytheCooldown();
            case "effect_spear" -> getSpearCooldown();
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
        spearCooldowns.remove(uuid);
        spearChargeStart.remove(uuid);
        spearLunging.remove(uuid);
        spearCharging.remove(uuid);
        maceFlying.remove(uuid);
        frozenPlayers.remove(uuid);
    }

    public void startSpearCharge(Player player) {
        UUID uuid = player.getUniqueId();
        if (spearCharging.contains(uuid)) return;

        spearCharging.add(uuid);
        spearChargeStart.put(uuid, System.currentTimeMillis());

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2, false, false, false));

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 20; // 1 second charge for exact 5% increments up to 100%!

            @Override
            public void run() {
                if (!player.isOnline() || !spearCharging.contains(uuid)) {
                    cancel();
                    return;
                }

                if (ticks > 1 && !player.isHandRaised()) {
                    releaseSpearLunge(player);
                    cancel();
                    return;
                }

                ticks++;
                double ratio = Math.min(1.0, (double) ticks / maxTicks);

                String color = "§2";
                if (ratio >= 1.0) {
                    color = "§c";
                } else if (ratio > 0.7) {
                    color = "§6";
                } else if (ratio > 0.4) {
                    color = "§e";
                } else if (ratio > 0.1) {
                    color = "§a";
                }

                String chargeText = plugin.getMessageUtils().getMessage("spear-charging-bar");
                int filled = (int) Math.round(ratio * 10.0);
                int empty = 10 - filled;
                int percent = (int) Math.round(ratio * 100.0);
                String bar = color + chargeText + ": [" + "█".repeat(filled) + "░".repeat(empty) + "] " + percent + "%";
                plugin.getMessageUtils().sendActionBar(player, bar);

                Location loc = player.getLocation();
                if (loc != null) {
                    double radius = 0.8;
                    double angle = ticks * 0.5;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    double y = ratio * 1.8;
                    
                    Location pLoc = loc.clone().add(x, y, z);
                    loc.getWorld().spawnParticle(Particle.PORTAL, pLoc, 3, 0.01, 0.01, 0.01, 0.01);
                    loc.getWorld().spawnParticle(Particle.CRIT, pLoc, 2, 0.01, 0.01, 0.01, 0.01);
                    
                    float pitch = 0.5f + (float)ratio * 1.2f;
                    loc.getWorld().playSound(loc, Sound.ENTITY_BREEZE_CHARGE, 0.6f, pitch);
                }

                // If charge reaches 100% (20 ticks), auto release lunge instantly!
                if (ticks >= maxTicks) {
                    releaseSpearLunge(player);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public boolean isSpearOnCooldown(UUID uuid) {
        return isOnCooldown(spearCooldowns, uuid, getSpearCooldown());
    }

    public long getSpearRemainingCooldown(UUID uuid) {
        return getRemainingCooldown(spearCooldowns, uuid, getSpearCooldown());
    }

    public boolean isSpearLunging(UUID uuid) {
        return spearLunging.contains(uuid);
    }

    public boolean isSpearCharging(UUID uuid) {
        return spearCharging.contains(uuid);
    }

    public void recordLunge(Player player) {
        UUID uuid = player.getUniqueId();
        lastLungeTime.put(uuid, System.currentTimeMillis());
        initialSaturation.put(uuid, player.getSaturation());
        initialFood.put(uuid, player.getFoodLevel());
        spearLungeDamaged.put(uuid, new HashSet<>());
    }

    public Set<UUID> getSpearLungeDamaged(UUID uuid) {
        return spearLungeDamaged.computeIfAbsent(uuid, k -> new HashSet<>());
    }

    public boolean wasRecentlyLunging(UUID uuid) {
        Long time = lastLungeTime.get(uuid);
        if (time == null) return false;
        return (System.currentTimeMillis() - time) < 4000L;
    }

    public Float getInitialSaturation(UUID uuid) {
        return initialSaturation.get(uuid);
    }

    public Integer getInitialFood(UUID uuid) {
        return initialFood.get(uuid);
    }

    public void releaseSpearLunge(Player player) {
        UUID uuid = player.getUniqueId();
        if (spearCharging.remove(uuid)) {
            player.removePotionEffect(PotionEffectType.SLOWNESS);
            performSpearLunge(player);
        }
    }

    public void performSpearLunge(Player player) {
        UUID uuid = player.getUniqueId();

        mlnplus.hu.effectsmp.data.PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data.getEffectHearts() < 2) {
            plugin.getMessageUtils().sendMessage(player, "not-enough-hearts-item");
            return;
        }

        if (isSpearOnCooldown(uuid)) {
            long remaining = getSpearRemainingCooldown(uuid);
            plugin.getMessageUtils().sendMessage(player, "spear-cooldown",
                    "%time%", plugin.getMessageUtils().formatTime(remaining));
            return;
        }

        Long start = spearChargeStart.remove(uuid);
        long elapsed = (start != null) ? (System.currentTimeMillis() - start) : 1000;

        double ratio = Math.min(1.0, elapsed / 1000.0);
        // Extremely powerful launch thrust (up to 4.5 initial multiplier)
        double initialPower = 3.0 + (ratio * 3.5);

        Location loc = player.getLocation();
        if (loc == null) return;

        Vector direction = loc.getDirection().normalize();
        if (direction.getY() < 0.3) {
            direction.setY(0.3);
            direction.normalize();
        }

        player.setVelocity(direction.clone().multiply(initialPower));

        spearCooldowns.put(uuid, System.currentTimeMillis());
        spearLunging.add(uuid);
        recordLunge(player);

        // Sonic Rocket Flight Task: maintains hyper-velocity (3.2 per tick) for 25 ticks (1.25 seconds = 80-120+ blocks!)
        Set<UUID> damagedEntities = getSpearLungeDamaged(uuid);
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks >= 25 || !spearLunging.contains(uuid)) {
                    spearLunging.remove(uuid);
                    if (player.isOnline()) {
                        player.setFallDistance(0.0f);
                        // Add 5 seconds of resistance against fall damage after flight
                        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 4, false, false, false));
                    }
                    cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                if (currentLoc != null && currentLoc.getWorld() != null) {
                    // Continuous rocket thrust per tick to overcome air friction and launch 80-120+ blocks!
                    if (ticks < 20) {
                        Vector forwardVelocity = currentLoc.getDirection().normalize().multiply(3.2);
                        if (forwardVelocity.getY() < 0.15) forwardVelocity.setY(0.15);
                        player.setVelocity(forwardVelocity);
                    }

                    player.setFallDistance(0.0f);

                    currentLoc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, currentLoc, 6, 0.5, 0.5, 0.5, 0.1);
                    currentLoc.getWorld().spawnParticle(Particle.CRIT, currentLoc, 10, 0.5, 0.5, 0.5, 0.1);
                    currentLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, currentLoc, 1, 0, 0, 0, 0);

                    for (Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, 4.0, 4.0, 4.0)) {
                        if (entity instanceof org.bukkit.entity.LivingEntity living && !entity.equals(player)) {
                            UUID targetUuid = entity.getUniqueId();
                            if (damagedEntities.contains(targetUuid)) continue;

                            if (living instanceof Player targetPlayer) {
                                if (data.hasTrusted(targetPlayer.getUniqueId())) {
                                    continue;
                                }
                            }

                            // OVERPOWERED SPEAR IMPACT DAMAGE: 50.0 Raw Damage (25 Hearts) + 12.0 True Health Bypass Damage!
                            living.damage(50.0, player);
                            double newHealth = Math.max(0.5, living.getHealth() - 12.0);
                            living.setHealth(newHealth);

                            damagedEntities.add(targetUuid);

                            // Launch target into the air with heavy shockwave impulse
                            Vector knockback = living.getLocation().toVector().subtract(currentLoc.toVector()).normalize().multiply(2.5).setY(1.2);
                            living.setVelocity(knockback);

                            Location eloc = living.getLocation();
                            if (eloc != null) {
                                eloc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, eloc, 2);
                                eloc.getWorld().playSound(eloc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 0.8f);
                                eloc.getWorld().playSound(eloc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.7f);
                            }
                        }
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 2, 0.5, 0.5, 0.5, 0.2);
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.7f);
        loc.getWorld().playSound(loc, Sound.ITEM_TRIDENT_RIPTIDE_3, 2.0f, 0.6f);
    }

    public void startVanillaSpearChargeAnimation(Player player) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                
                if (!player.isHandRaised()) {
                    cancel();
                    return;
                }
                
                org.bukkit.inventory.ItemStack activeItem = player.getActiveItem();
                if (activeItem == null || !activeItem.getType().name().contains("SPEAR")) {
                    cancel();
                    return;
                }
                
                Location loc = player.getLocation();
                if (loc != null) {
                    double radius = 0.6;
                    double angle = ticks * 0.6;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    double y = 0.5 + (ticks % 10) * 0.12;
                    
                    Location pLoc = loc.clone().add(x, y, z);
                    loc.getWorld().spawnParticle(Particle.PORTAL, pLoc, 2, 0.01, 0.01, 0.01, 0.01);
                    loc.getWorld().spawnParticle(Particle.CRIT, pLoc, 1, 0.01, 0.01, 0.01, 0.01);
                    loc.getWorld().spawnParticle(Particle.CLOUD, pLoc, 1, 0.01, 0.01, 0.01, 0.01);
                    
                    if (ticks % 3 == 0) {
                        loc.getWorld().playSound(loc, Sound.BLOCK_TRIAL_SPAWNER_AMBIENT, 0.5f, 1.2f);
                    }
                }
                
                ticks++;
                if (ticks > 60) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void removeFreezeAttribute(Player player) {
        if (player == null)
            return;
        UUID uuid = player.getUniqueId();

        AttributeInstance kbResist = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (kbResist != null) {
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "freeze_kb_" + uuid);
            kbResist.removeModifier(key);
        }
    }

    private static class FreezeInfo {
        final Location location;

        FreezeInfo(Location location) {
            this.location = location;
        }
    }
}
