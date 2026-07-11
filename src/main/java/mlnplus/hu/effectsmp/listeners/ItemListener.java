package mlnplus.hu.effectsmp.listeners;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.data.PlayerData;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import java.util.Set;
import java.util.UUID;

public class ItemListener implements Listener {

    private final Effectsmp plugin;

    public ItemListener(Effectsmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    @SuppressWarnings("null")
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && item.getType().name().contains("SPEAR") && item.containsEnchantment(org.bukkit.enchantments.Enchantment.LUNGE)) {
                plugin.getItemAbilityManager().recordLunge(player);
            }
        }
        if (player.isSneaking() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (data.getEffect() == mlnplus.hu.effectsmp.effects.EffectType.WIND_CHARGED && data.isAbilityActive()) {
                event.setCancelled(true);
                plugin.getEffectAbilityManager().triggerWindBurst(player);
                return;
            }
        }

        ItemStack interactItem = event.getItem();
        if (interactItem != null && plugin.getCustomItems().isInfiniteWindCharge(interactItem)) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                if (data.getEffect() == mlnplus.hu.effectsmp.effects.EffectType.WIND_CHARGED && data.getEffectHearts() >= 1) {
                    event.setCancelled(true);
                    
                    // Check native cooldown (3 seconds = 60 ticks)
                    if (player.hasCooldown(Material.WIND_CHARGE)) {
                        int remainingTicks = player.getCooldown(Material.WIND_CHARGE);
                        long remainingMs = remainingTicks * 50L;
                        plugin.getMessageUtils().sendMessage(player, "wind-charge-cooldown",
                                "%time%", plugin.getMessageUtils().formatTime(remainingMs));
                        return;
                    }
                    
                    // Spawn the projectile manually
                    Location loc = player.getLocation();
                    if (loc != null) {
                        org.bukkit.entity.WindCharge wc = player.launchProjectile(org.bukkit.entity.WindCharge.class);
                        if (wc != null) {
                            wc.setVelocity(loc.getDirection().multiply(1.5));
                        }
                        
                        // Play throw sound
                        player.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_WIND_CHARGE_THROW, 1.0f, 1.0f);
                    }
                    
                    // Set native cooldown
                    player.setCooldown(Material.WIND_CHARGE, 60);
                    
                    // Swing hand
                    if (event.getHand() == EquipmentSlot.OFF_HAND) {
                        player.swingOffHand();
                    } else {
                        player.swingMainHand();
                    }
                    return;
                }
            }
        }

        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        ItemStack item = event.getItem();
        if (item == null)
            return;

        String itemType = plugin.getCustomItems().getItemType(item);
        if (itemType == null)
            return;

        if ("effect_bow".equals(itemType)) {
            return;
        }

        if ("effect_spear".equals(itemType)) {
            if (player.isSneaking()) {
                if (plugin.getItemAbilityManager().isSpearOnCooldown(player.getUniqueId())) {
                    long remaining = plugin.getItemAbilityManager().getSpearRemainingCooldown(player.getUniqueId());
                    plugin.getMessageUtils().sendMessage(player, "spear-cooldown",
                            "%time%", plugin.getMessageUtils().formatTime(remaining));
                    event.setCancelled(true);
                    return;
                }
                PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                if (data.getEffectHearts() < 2) {
                    plugin.getMessageUtils().sendMessage(player, "not-enough-hearts-item");
                    event.setCancelled(true);
                    return;
                }
                plugin.getItemAbilityManager().startSpearCharge(player);
                return;
            } else {
                plugin.getItemAbilityManager().startVanillaSpearChargeAnimation(player);
                return;
            }
        }

        event.setCancelled(true);

        switch (itemType) {
            case "effect_heart" -> useEffectHeart(player, item);
            case "reroll" -> useReroll(player, item);
            case "op_reroll" -> useOPReroll(player, item);
            case "effect_mace" -> plugin.getItemAbilityManager().activateMace(player);
            case "effect_sword" -> {
                if (player.isSneaking()) {
                    plugin.getItemAbilityManager().activateSword(player);
                } else {
                    plugin.getMessageUtils().sendMessage(player, "sword-usage-hint");
                }
            }
            case "effect_scythe" -> {
                if (player.isSneaking()) {
                    plugin.getItemAbilityManager().activateScythe(player);
                } else {
                    plugin.getMessageUtils().sendMessage(player, "scythe-usage-hint");
                }
            }
        }
    }

    private void useEffectHeart(Player player, ItemStack item) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getEffectHearts() >= 3) {
            plugin.getMessageUtils().sendMessage(player, "heart-limit-reached");
            return;
        }

        int oldHearts = data.getEffectHearts();

        data.addEffectHearts(1);

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        plugin.getMessageUtils().sendMessage(player, "heart-used",
                "%hearts%", String.valueOf(data.getEffectHearts()));

        if (data.isPassiveEnabled() && data.getEffect() != null) {
            plugin.getEffectAbilityManager().removePassiveEffect(player);
            plugin.getEffectAbilityManager().applyPassiveEffect(player);

            if (oldHearts < 2 && data.getEffectHearts() >= 2) {
                plugin.getMessageUtils().sendMessage(player, "heart-level-up");
            }
        }

        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
    }

    private void useReroll(Player player, ItemStack item) {
        if (plugin.getEffectAbilityManager().isRolling(player)) {
            plugin.getMessageUtils().sendMessage(player, "reroll-in-progress");
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getEffect() == null) {
            plugin.getMessageUtils().sendMessage(player, "reroll-no-effect");
            return;
        }

        if (data.getEffect().isOP()) {
            plugin.getMessageUtils().sendMessage(player, "reroll-op-error");
            return;
        }

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        plugin.getEffectAbilityManager().removePassiveEffect(player);

        plugin.getEffectAbilityManager().assignRandomEffect(player, false);
    }

    private void useOPReroll(Player player, ItemStack item) {
        if (plugin.getEffectAbilityManager().isRolling(player)) {
            plugin.getMessageUtils().sendMessage(player, "reroll-in-progress");
            return;
        }

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        plugin.getEffectAbilityManager().removePassiveEffect(player);

        plugin.getEffectAbilityManager().assignRandomEffect(player, true);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow))
            return;
        if (!(arrow.getShooter() instanceof Player player))
            return;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        String itemType = plugin.getCustomItems().getItemType(mainHand);

        if ("effect_bow".equals(itemType)) {
            if (Math.random() < 0.10) {
                Location loc = event.getEntity().getLocation();
                plugin.getItemAbilityManager().triggerBowDebuffs(player, loc);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker))
            return;

        ItemStack mainHand = attacker.getInventory().getItemInMainHand();
        String itemType = plugin.getCustomItems().getItemType(mainHand);

        if ("effect_sword".equals(itemType)) {
            if (plugin.getItemAbilityManager().isSwordAbilityActive(attacker)) {
                event.setDamage(event.getDamage() * 1.5);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null)
            return;

        Player player = event.getPlayer();
        plugin.getItemAbilityManager().checkMaceLanding(player);

        // Check if player is lunging with spear to damage nearby entities
        if (plugin.getItemAbilityManager().isSpearLunging(player.getUniqueId())) {
            Set<UUID> damaged = plugin.getItemAbilityManager().getSpearLungeDamaged(player.getUniqueId());
            for (org.bukkit.entity.Entity entity : player.getNearbyEntities(1.5, 1.5, 1.5)) {
                if (entity instanceof org.bukkit.entity.LivingEntity living && !entity.equals(player)) {
                    UUID entityUuid = entity.getUniqueId();
                    if (damaged != null && damaged.contains(entityUuid)) continue;

                    // Exclude trusted players
                    if (living instanceof Player targetPlayer) {
                        mlnplus.hu.effectsmp.data.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                        if (data != null && data.hasTrusted(targetPlayer.getUniqueId())) {
                            continue;
                        }
                    }

                    // Deal heavy damage (15.0 = 7.5 hearts)
                    living.damage(15.0, player);
                    if (damaged != null) {
                        damaged.add(entityUuid);
                    }

                    // Visuals & Sound
                    Location eloc = living.getLocation();
                    if (eloc != null) {
                        eloc.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, eloc, 1);
                        eloc.getWorld().playSound(eloc, org.bukkit.Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
                    }
                }
            }
        }

        if (plugin.getItemAbilityManager().wasRecentlyLunging(player.getUniqueId())) {
            Float sat = plugin.getItemAbilityManager().getInitialSaturation(player.getUniqueId());
            Integer food = plugin.getItemAbilityManager().getInitialFood(player.getUniqueId());
            if (sat != null) player.setSaturation(sat);
            if (food != null) player.setFoodLevel(food);
            player.setExhaustion(0.0f);
        }
    }

    @EventHandler
    public void onProjectileLaunch(org.bukkit.event.entity.ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player) {
            if (plugin.getItemAbilityManager().isSpearCharging(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
