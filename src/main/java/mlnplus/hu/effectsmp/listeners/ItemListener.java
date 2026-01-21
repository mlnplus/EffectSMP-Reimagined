package mlnplus.hu.effectsmp.listeners;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.data.PlayerData;

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

        // Don't cancel bow - it needs to shoot arrows normally
        if ("effect_bow".equals(itemType)) {
            return; // Let bow work normally, debuffs applied on projectile hit
        }

        event.setCancelled(true);

        switch (itemType) {
            case "effect_heart" -> useEffectHeart(player, item);
            case "reroll" -> useReroll(player, item);
            case "op_reroll" -> useOPReroll(player, item);
            case "effect_mace" -> plugin.getItemAbilityManager().activateMace(player);
            case "effect_sword" -> {
                // Effect Sword requires shift+right click
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

        // Add heart to player
        data.addEffectHearts(1);

        // Remove one from inventory
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        plugin.getMessageUtils().sendMessage(player, "heart-used",
                "%hearts%", String.valueOf(data.getEffectHearts()));

        // Re-apply passive effect to update level (1 heart = level 1, 2+ hearts = level
        // 2)
        if (data.isPassiveEnabled() && data.getEffect() != null) {
            // Remove old effect first
            plugin.getEffectAbilityManager().removePassiveEffect(player);
            // Apply with new level
            plugin.getEffectAbilityManager().applyPassiveEffect(player);

            // Notify if level changed
            if (oldHearts < 2 && data.getEffectHearts() >= 2) {
                plugin.getMessageUtils().sendMessage(player, "heart-level-up");
            }
        }

        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
    }

    private void useReroll(Player player, ItemStack item) {
        // Prevent multiple rerolls
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

        // Remove the reroll item
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        // Remove old passive effect
        plugin.getEffectAbilityManager().removePassiveEffect(player);

        // Assign new random effect (not OP)
        plugin.getEffectAbilityManager().assignRandomEffect(player, false);
    }

    private void useOPReroll(Player player, ItemStack item) {
        // Prevent multiple rerolls
        if (plugin.getEffectAbilityManager().isRolling(player)) {
            plugin.getMessageUtils().sendMessage(player, "reroll-in-progress");
            return;
        }

        // Remove the OP reroll item
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        // Remove old passive effect
        plugin.getEffectAbilityManager().removePassiveEffect(player);

        // Assign OP effect
        plugin.getEffectAbilityManager().assignRandomEffect(player, true);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow))
            return;
        if (!(arrow.getShooter() instanceof Player player))
            return;

        // Check if shot from Effect Bow
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        String itemType = plugin.getCustomItems().getItemType(mainHand);

        if ("effect_bow".equals(itemType)) {
            // 10% chance to fire cursed arrow
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

        // Effect Sword - 1.5x damage when ability is active
        if ("effect_sword".equals(itemType)) {
            if (plugin.getItemAbilityManager().isSwordAbilityActive(attacker)) {
                // Apply 1.5x damage multiplier
                event.setDamage(event.getDamage() * 1.5);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Check for mace landing
        plugin.getItemAbilityManager().checkMaceLanding(event.getPlayer());
    }
}
