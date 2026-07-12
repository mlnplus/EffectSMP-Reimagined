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
    private final Map<UUID, Long> bowCooldowns = new HashMap<>();
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

    public long getBowCooldown() {
        return plugin.getConfigManager().getItemsConfig().getLong("effect_bow.cooldown", 90) * 1000L;
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

    public boolean canBowApplyDebuffs(Player player) {
        UUID uuid = player.getUniqueId();
        return !isOnCooldown(bowCooldowns, uuid, getBowCooldown());
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
            case "effect_spear" -> spearCooldowns;
            default -> null;
        };

        if (cooldownMap == null)
            return 0;

        long duration = switch (itemType) {
            case "effect_mace" -> getMaceCooldown();
            case "effect_sword" -> getSwordCooldown();
            case "effect_bow" -> getBowCooldown();
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
        bowCooldowns.remove(uuid);
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

                double ratio = Math.min(1.0, ticks / 30.0);

                Location loc = player.getLocation();
                if (loc != null) {
                    double radius = 0.8;
                    double angle = ticks * 0.4;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    double y = ratio * 1.8;
                    
                    Location pLoc = loc.clone().add(x, y, z);
                    loc.getWorld().spawnParticle(Particle.PORTAL, pLoc, 2, 0.01, 0.01, 0.01, 0.01);
                    loc.getWorld().spawnParticle(Particle.CRIT, pLoc, 1, 0.01, 0.01, 0.01, 0.01);
                    
                    float pitch = 0.5f + (float)ratio * 1.2f;
                    if (ticks % 2 == 0) {
                        loc.getWorld().playSound(loc, Sound.ENTITY_BREEZE_CHARGE, 0.6f, pitch);
                    }
                }

                String color = "§2";
                if (ratio > 0.8) {
                    color = "§c";
                } else if (ratio > 0.6) {
                    color = "§6";
                } else if (ratio > 0.4) {
                    color = "§e";
                } else if (ratio > 0.2) {
                    color = "§a";
                }

                String chargeText = plugin.getMessageUtils().getMessage("spear-charging-bar");
                int filled = (int) (ratio * 10);
                int empty = 10 - filled;
                String bar = color + chargeText + ": [" + "█".repeat(filled) + "░".repeat(empty) + "] " + (int)(ratio * 100) + "%";
                plugin.getMessageUtils().sendActionBar(player, bar);

                // Keep slowness potion refreshed while holding charge
                if (ticks % 20 == 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2, false, false, false));
                }

                ticks++;
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
        return (System.currentTimeMillis() - time) < 3000L;
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
        long elapsed = (start != null) ? (System.currentTimeMillis() - start) : 500;

        double ratio = Math.min(1.0, elapsed / 1500.0);
        double basePower = 2.0;
        double extraPower = ratio * 10.0;
        double power = basePower + extraPower;

        Location loc = player.getLocation();
        if (loc == null) return;

        Vector direction = loc.getDirection().normalize();
        if (direction.getY() < 0.2) {
            direction.setY(0.2);
            direction.normalize();
        }
        direction.multiply(power);
        player.setVelocity(direction);

        spearCooldowns.put(uuid, System.currentTimeMillis());
        spearLunging.add(uuid);
        recordLunge(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            spearLunging.remove(uuid);
        }, 30L);

        loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 5, 1.0, 1.0, 1.0, 0.1);
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 25, 0.5, 0.5, 0.5, 0.25);
        loc.getWorld().playSound(loc, Sound.ITEM_TRIDENT_RIPTIDE_3, 1.2f, 0.9f);
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
