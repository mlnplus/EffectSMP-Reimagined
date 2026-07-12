package mlnplus.hu.effectsmp.effects;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@SuppressWarnings("null")
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

        String titleKey = effect.isOP() ? "roll-title-op" : "roll-title-normal";
        String subtitleKey = effect.isOP() ? "roll-subtitle-op" : "roll-subtitle-normal";
        plugin.getMessageUtils().sendTitle(player, titleKey, subtitleKey, "%effect%", effect.getDisplayName());

        String borderKey = effect.isOP() ? "roll-chat-border-op" : "roll-chat-border-normal";
        String lineEffectKey = effect.isOP() ? "roll-chat-line-effect-op" : "roll-chat-line-effect-normal";
        String linePassiveKey = effect.isOP() ? "roll-chat-line-passive" : "roll-chat-line-passive-normal";
        String lineActiveKey = effect.isOP() ? "roll-chat-line-active" : "roll-chat-line-active-normal";

        plugin.getMessageUtils().sendMessage(player, borderKey);
        plugin.getMessageUtils().sendMessage(player, lineEffectKey, "%effect%", effect.getDisplayName());
        plugin.getMessageUtils().sendMessage(player, linePassiveKey, "%effect%", effect.getDisplayName());
        plugin.getMessageUtils().sendMessage(player, lineActiveKey);
        plugin.getMessageUtils().sendMessage(player, borderKey);

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    public void applyPassiveEffect(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getEffect() == null || !data.isPassiveEnabled()) {
            return;
        }

        if (data.getEffect() == EffectType.WIND_CHARGED) {
            giveInfiniteWindChargeIfNeeded(player);
            return;
        }

        PotionEffectType type = data.getEffect().getPotionEffect();
        if (type == null) {
            return;
        }

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
            if (type != null && player.hasPotionEffect(type)) {
                player.removePotionEffect(type);
            }
            if (data.getEffect() == EffectType.WIND_CHARGED) {
                removeInfiniteWindCharge(player);
            }
        }
    }

    private void giveInfiniteWindChargeIfNeeded(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (plugin.getCustomItems().isInfiniteWindCharge(item)) {
                return;
            }
        }
        HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(plugin.getCustomItems().createInfiniteWindCharge());
        if (remaining != null) {
            for (ItemStack item : remaining.values()) {
                if (item != null) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
        }
    }

    private void removeInfiniteWindCharge(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (plugin.getCustomItems().isInfiniteWindCharge(contents[i])) {
                player.getInventory().setItem(i, null);
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
            plugin.getMessageUtils().sendMessage(player, "passive-toggle-on");
        } else {
            removePassiveEffect(player);
            plugin.getMessageUtils().sendMessage(player, "passive-toggle-off");
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
            if (data.getEffectHearts() < 3) {
                plugin.getMessageUtils().sendMessage(player, "not-enough-hearts-active");
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
            case WIND_CHARGED -> {
                duration = 15000;
                setAbilityActive(player, data, duration);
                plugin.getMessageUtils().sendMessage(player, "wind-charged-armed");
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
                return true;
            }

            case INVISIBILITY -> {
                duration = 10000;
                setAbilityActive(player, data, duration);

                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.INVISIBILITY, (int) (duration / 50), 0, false, false));

                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online == null) continue;
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
                                if (online == null) continue;
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

                int affected = 0;
                UUID uuid = player.getUniqueId();
                for (org.bukkit.entity.Entity entity : player.getNearbyEntities(10, 10, 10)) {
                    if (entity instanceof Player target && !target.equals(player)) {
                        if (!plugin.getPlayerDataManager().isMutualTrust(uuid, target.getUniqueId())) {
                            target.setFireTicks(300); // 15 seconds
                            plugin.getMessageUtils().sendMessage(target, "ability-fireres-victim");
                            affected++;
                        }
                    } else if (entity instanceof org.bukkit.entity.LivingEntity living && !(entity instanceof Player)) {
                        living.setFireTicks(300);
                        affected++;
                    }
                }

                plugin.getMessageUtils().sendMessage(player, "ability-fireres-activated",
                        "%count%", String.valueOf(affected));
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
                        new PotionEffect(PotionEffectType.HEALTH_BOOST, (int) (duration / 50), 4, false, false));

                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.REGENERATION, 100, 2, false, false));
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
                applyStrengthReach(player);

                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.STRENGTH, (int) (duration / 50), 2, false, false));
                plugin.getMessageUtils().sendMessage(player, "ability-strength-activated");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                return true;
            }

            case REGENERATION -> {
                duration = 45000;
                setAbilityActive(player, data, duration);

                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.REGENERATION, (int) (duration / 50), 2, false, false));
                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.ABSORPTION, (int) (duration / 50), 1, false, false));

                int affected = 0;
                List<UUID> mutualTrusted = plugin.getPlayerDataManager().getMutualTrustedPlayers(player.getUniqueId());
                for (UUID trustedUuid : mutualTrusted) {
                    Player trustedPlayer = Bukkit.getPlayer(trustedUuid);
                    if (trustedPlayer != null && trustedPlayer.isOnline()) {
                        trustedPlayer.addPotionEffect(
                                new PotionEffect(PotionEffectType.REGENERATION, (int) (duration / 50), 2, false, false));
                        plugin.getMessageUtils().sendMessage(trustedPlayer, "ability-regen-received",
                                "%player%", player.getName());
                        affected++;
                    }
                }

                plugin.getMessageUtils().sendMessage(player, "ability-regen-activated",
                        "%count%", String.valueOf(affected));
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
                if (player.isOnline()) {
                    removeStrengthReach(player);
                    if (data.getEffect() != null && data.getEffect() != EffectType.INVISIBILITY) {
                        plugin.getActionBarManager().sendAbilityExpired(player);
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.5f);
                    }
                }
            }
        }.runTaskLater(plugin, durationMillis / 50);
    }

    public void triggerWindBurst(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        data.setAbilityActiveUntil(0);
        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());

        org.bukkit.Location loc = player.getLocation();
        if (loc == null) return;

        loc.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, loc, 1);
        loc.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, loc, 40, 3.0, 1.0, 3.0, 0.2);
        loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_BREEZE_WIND_BURST, 1.5f, 0.8f);

        UUID uuid = player.getUniqueId();
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(8, 4, 8)) {
            if (entity instanceof Player target && !target.equals(player)) {
                if (!plugin.getPlayerDataManager().isMutualTrust(uuid, target.getUniqueId())) {
                    org.bukkit.util.Vector diff = target.getLocation().toVector().subtract(loc.toVector()).normalize();
                    diff.setY(0.55);
                    diff.multiply(2.2);
                    target.setVelocity(diff);
                    plugin.getMessageUtils().sendMessage(target, "wind-charged-victim");
                }
            }
        }

        org.bukkit.util.Vector vel = player.getVelocity();
        vel.setY(2.2);
        player.setVelocity(vel);

        plugin.getMessageUtils().sendMessage(player, "wind-charged-activated");
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

    public void applyStrengthReach(Player player) {
        if (player == null) return;
        
        removeStrengthReach(player);

        org.bukkit.attribute.AttributeInstance blockRange = player.getAttribute(org.bukkit.attribute.Attribute.BLOCK_INTERACTION_RANGE);
        if (blockRange != null) {
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "strength_block_reach");
            org.bukkit.attribute.AttributeModifier modifier = new org.bukkit.attribute.AttributeModifier(key, 3.0, org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER);
            blockRange.addModifier(modifier);
        }

        org.bukkit.attribute.AttributeInstance entityRange = player.getAttribute(org.bukkit.attribute.Attribute.ENTITY_INTERACTION_RANGE);
        if (entityRange != null) {
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "strength_entity_reach");
            org.bukkit.attribute.AttributeModifier modifier = new org.bukkit.attribute.AttributeModifier(key, 3.0, org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER);
            entityRange.addModifier(modifier);
        }
    }

    public void removeStrengthReach(Player player) {
        if (player == null) return;

        org.bukkit.attribute.AttributeInstance blockRange = player.getAttribute(org.bukkit.attribute.Attribute.BLOCK_INTERACTION_RANGE);
        if (blockRange != null) {
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "strength_block_reach");
            blockRange.removeModifier(key);
        }

        org.bukkit.attribute.AttributeInstance entityRange = player.getAttribute(org.bukkit.attribute.Attribute.ENTITY_INTERACTION_RANGE);
        if (entityRange != null) {
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "strength_entity_reach");
            entityRange.removeModifier(key);
        }
    }
}
