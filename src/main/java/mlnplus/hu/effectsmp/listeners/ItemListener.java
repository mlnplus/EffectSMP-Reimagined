package mlnplus.hu.effectsmp.listeners;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.data.PlayerData;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Location;
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

public class ItemListener implements Listener {

    private final Effectsmp plugin;

    public ItemListener(Effectsmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
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
            if (plugin.getItemAbilityManager().isSpearOnCooldown(player.getUniqueId())) {
                long remaining = plugin.getItemAbilityManager().getSpearRemainingCooldown(player.getUniqueId());
                plugin.getMessageUtils().sendMessage(player, "spear-cooldown",
                        "%time%", plugin.getMessageUtils().formatTime(remaining));
                event.setCancelled(true);
                return;
            }
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (data.getEffectHearts() < 3) {
                plugin.getMessageUtils().sendMessage(player, "not-enough-hearts-active");
                event.setCancelled(true);
                return;
            }
            plugin.getItemAbilityManager().startSpearCharge(player.getUniqueId());
            return;
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
        plugin.getItemAbilityManager().checkMaceLanding(event.getPlayer());
    }

    @EventHandler
    public void onProjectileLaunch(org.bukkit.event.entity.ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Trident trident) {
            if (trident.getShooter() instanceof Player player) {
                ItemStack item = player.getInventory().getItemInMainHand();
                String itemType = plugin.getCustomItems().getItemType(item);
                if (!"effect_spear".equals(itemType)) {
                    item = player.getInventory().getItemInOffHand();
                    itemType = plugin.getCustomItems().getItemType(item);
                }
                if ("effect_spear".equals(itemType)) {
                    event.setCancelled(true);
                    plugin.getItemAbilityManager().performSpearLunge(player);
                }
            }
        } else if (event.getEntity() instanceof org.bukkit.entity.WindCharge windCharge) {
            if (windCharge.getShooter() instanceof Player player) {
                PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                if (data.getEffect() == mlnplus.hu.effectsmp.effects.EffectType.WIND_CHARGED && data.getEffectHearts() >= 1) {
                    EquipmentSlot slot = player.getInventory().getItemInMainHand().getType() == Material.WIND_CHARGE ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;
                    ItemStack item = (slot == EquipmentSlot.HAND) ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
                    if (item != null && item.getType() == Material.WIND_CHARGE) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            ItemStack current = (slot == EquipmentSlot.HAND) ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
                            if (current == null || current.getType() == Material.AIR) {
                                if (slot == EquipmentSlot.HAND) {
                                    player.getInventory().setItemInMainHand(plugin.getCustomItems().createInfiniteWindCharge());
                                } else {
                                    player.getInventory().setItemInOffHand(plugin.getCustomItems().createInfiniteWindCharge());
                                }
                            } else {
                                current.setAmount(Math.min(current.getMaxStackSize(), current.getAmount() + 1));
                            }
                        });
                    }
                }
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (plugin.getItemAbilityManager().isSpearLunging(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
