package mlnplus.hu.effectsmp.commands;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.data.PlayerData;
import mlnplus.hu.effectsmp.effects.EffectType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class EffectCommand implements CommandExecutor {

    private final Effectsmp plugin;

    public EffectCommand(Effectsmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageUtils().getMessage("only-players"));
            return true;
        }

        if (args.length == 0) {
            return openMainGUI(player);
        }

        switch (args[0].toLowerCase()) {
            case "activate" -> {
                return activateAbility(player);
            }
            case "info" -> {
                plugin.getGuiManager().openInfoGUI(player);
                return true;
            }
            case "withdraw" -> {
                return withdrawHearts(player, args);
            }
            case "trust" -> {
                return trustPlayer(player, args);
            }
            case "untrust" -> {
                return untrustPlayer(player, args);
            }
            case "toggle" -> {
                return togglePassive(player);
            }
            case "set" -> {
                return adminSetEffect(player, args);
            }
            case "give" -> {
                return adminGiveItem(player, args);
            }
            case "reload" -> {
                return adminReload(player);
            }
            case "start" -> {
                return adminStart(player);
            }
            case "removecooldown", "rc" -> {
                return removeCooldown(player, args);
            }
            case "craftreset" -> {
                return craftReset(player, args);
            }
            default -> {
                plugin.getMessageUtils().sendMessage(player, "unknown-command");
                return true;
            }
        }
    }

    private boolean openMainGUI(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getEffectHearts() < 1) {
            plugin.getMessageUtils().sendMessage(player, "no-hearts-menu");
            return true;
        }

        plugin.getGuiManager().openMainGUI(player);
        return true;
    }

    private boolean activateAbility(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getEffectHearts() < 1) {
            plugin.getMessageUtils().sendMessage(player, "no-hearts-simple");
            return true;
        }

        plugin.getEffectAbilityManager().activateAbility(player);
        return true;
    }

    private boolean withdrawHearts(Player player, String[] args) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        int amount = 1;
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                plugin.getMessageUtils().sendMessage(player, "invalid-number");
                return true;
            }
        }

        if (amount <= 0) {
            plugin.getMessageUtils().sendMessage(player, "must-be-positive");
            return true;
        }

        if (data.getEffectHearts() < amount) {
            plugin.getMessageUtils().sendMessage(player, "not-enough-hearts",
                    "%hearts%", String.valueOf(data.getEffectHearts()));
            return true;
        }

        int oldHearts = data.getEffectHearts();

        data.removeEffectHearts(amount);

        ItemStack hearts = plugin.getCustomItems().createEffectHeart();
        hearts.setAmount(amount);

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), hearts);
            plugin.getMessageUtils().sendMessage(player, "heart-dropped",
                    "%amount%", String.valueOf(amount));
        } else {
            player.getInventory().addItem(hearts);
            plugin.getMessageUtils().sendMessage(player, "heart-withdrawn",
                    "%amount%", String.valueOf(amount));
        }

        if (data.getEffectHearts() == 0) {
            data.setPassiveEnabled(false);
            plugin.getEffectAbilityManager().removePassiveEffect(player);
            plugin.getMessageUtils().sendMessage(player, "heart-passive-disabled");
        } else if (oldHearts >= 3 && data.getEffectHearts() < 3) {
            plugin.getMessageUtils().sendMessage(player, "heart-ability-lost");
        } else if (oldHearts >= 2 && data.getEffectHearts() < 2) {
            plugin.getEffectAbilityManager().removePassiveEffect(player);
            plugin.getEffectAbilityManager().applyPassiveEffect(player);
            plugin.getMessageUtils().sendMessage(player, "heart-downgrade-warning");
        }

        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
        return true;
    }

    private boolean trustPlayer(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getMessageUtils().sendMessage(player, "usage-trust");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageUtils().sendMessage(player, "player-not-found");
            return true;
        }

        if (target.equals(player)) {
            plugin.getMessageUtils().sendMessage(player, "trust-self");
            return true;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.hasTrusted(target.getUniqueId())) {
            plugin.getMessageUtils().sendMessage(player, "trust-already-trusted");
            return true;
        }

        if (data.getTrustedPlayers().size() >= 5) {
            plugin.getMessageUtils().sendMessage(player, "trust-limit-reached");
            return true;
        }

        data.addTrustedPlayer(target.getUniqueId());
        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());

        PlayerData targetData = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
        if (targetData.hasTrusted(player.getUniqueId())) {
            plugin.getMessageUtils().sendMessage(player, "trust-mutual-active-sender",
                    "%player%", target.getName());
            plugin.getMessageUtils().sendMessage(target, "trust-mutual-active-target",
                    "%player%", player.getName());
        } else {
            plugin.getMessageUtils().sendMessage(player, "trust-pending-sender",
                    "%player%", target.getName());
            plugin.getMessageUtils().sendMessage(target, "trust-pending-target",
                    "%player%", player.getName());
        }

        return true;
    }

    private boolean untrustPlayer(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getMessageUtils().sendMessage(player, "usage-untrust");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        UUID targetUuid = target != null ? target.getUniqueId() : null;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (targetUuid == null) {
            for (UUID uuid : data.getTrustedPlayers()) {
                PlayerData trustedData = plugin.getPlayerDataManager().getPlayerData(uuid);
                if (trustedData.getPlayerName() != null &&
                        trustedData.getPlayerName().equalsIgnoreCase(args[1])) {
                    targetUuid = uuid;
                    break;
                }
            }
        }

        if (targetUuid == null || !data.hasTrusted(targetUuid)) {
            plugin.getMessageUtils().sendMessage(player, "trust-not-trusted");
            return true;
        }

        data.removeTrustedPlayer(targetUuid);
        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());

        plugin.getMessageUtils().sendMessage(player, "trust-removed",
                "%player%", args[1]);

        return true;
    }

    private boolean togglePassive(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getEffectHearts() < 1) {
            plugin.getMessageUtils().sendMessage(player, "no-hearts-simple");
            return true;
        }

        plugin.getEffectAbilityManager().togglePassive(player);
        return true;
    }

    private boolean adminSetEffect(Player player, String[] args) {
        boolean isAdmin = player.hasPermission("effectsmp.admin");
        boolean isTester = player.hasPermission("effectsmp.teszter");

        if (!isAdmin && !isTester) {
            plugin.getMessageUtils().sendMessage(player, "admin-no-permission");
            return true;
        }

        if (args.length < 2) {
            plugin.getMessageUtils().sendMessage(player, "usage-set");
            return true;
        }

        EffectType effect = EffectType.fromString(args[1]);
        if (effect == null) {
            plugin.getMessageUtils().sendMessage(player, "unknown-effect",
                    "%effect%", args[1]);
            return true;
        }

        Player target;
        if (args.length >= 3) {
            if (isTester && !isAdmin) {
                target = player;
                plugin.getMessageUtils().sendMessage(player, "admin-tester-self-only");
            } else {
                target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    plugin.getMessageUtils().sendMessage(player, "player-not-found");
                    return true;
                }
            }
        } else {
            target = player;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());

        plugin.getEffectAbilityManager().removePassiveEffect(target);

        data.setEffect(effect);
        data.setPassiveEnabled(true);

        plugin.getEffectAbilityManager().applyPassiveEffect(target);

        plugin.getPlayerDataManager().savePlayerData(target.getUniqueId());

        if (target.equals(player)) {
            plugin.getMessageUtils().sendMessage(player, "admin-set-success",
                    "%effect%", effect.getDisplayName());
        } else {
            plugin.getMessageUtils().sendMessage(player, "admin-set-other",
                    "%player%", target.getName(),
                    "%effect%", effect.getDisplayName());
            plugin.getMessageUtils().sendMessage(target, "admin-set-target",
                    "%effect%", effect.getDisplayName());
        }

        return true;
    }

    private boolean adminGiveItem(Player player, String[] args) {
        if (!player.hasPermission("effectsmp.admin") && !player.hasPermission("effectsmp.teszter")) {
            plugin.getMessageUtils().sendMessage(player, "admin-no-permission");
            return true;
        }

        if (args.length < 2) {
            plugin.getMessageUtils().sendMessage(player, "usage-give");
            plugin.getMessageUtils().sendMessage(player, "usage-give-list");
            return true;
        }

        ItemStack item = plugin.getCustomItems().getItemByName(args[1]);
        if (item == null) {
            plugin.getMessageUtils().sendMessage(player, "unknown-item",
                    "%item%", args[1]);
            return true;
        }

        Player target = player;
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                plugin.getMessageUtils().sendRawMessage(player, "&cNincs ilyen játékos online!");
                return true;
            }
        }

        target.getInventory().addItem(item);

        plugin.getMessageUtils().sendMessage(player, "admin-give-sender",
                "%item%", args[1],
                "%player%", target.getName());
        if (!target.equals(player)) {
            plugin.getMessageUtils().sendMessage(target, "admin-give-target",
                    "%item%", args[1]);
        }

        return true;
    }

    private boolean adminReload(Player player) {
        if (!player.hasPermission("effectsmp.admin")) {
            plugin.getMessageUtils().sendMessage(player, "admin-no-permission");
            return true;
        }

        plugin.reload();
        plugin.reload();
        plugin.getMessageUtils().sendMessage(player, "admin-reload-success");

        return true;
    }

    private boolean adminStart(Player player) {
        if (!player.hasPermission("effectsmp.admin") && !player.hasPermission("effectsmp.teszter")) {
            plugin.getMessageUtils().sendMessage(player, "admin-no-permission");
            return true;
        }

        if (plugin.isGameStarted()) {
            plugin.getMessageUtils().sendMessage(player, "admin-already-started");
            return true;
        }

        plugin.setGameStarted(true);

        for (Player online : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(online.getUniqueId());
            if (data.getEffect() == null) {
                plugin.getEffectAbilityManager().assignRandomEffect(online, false);
            }
        }

        plugin.getMessageUtils().sendMessage(player, "admin-start-success");
        Bukkit.broadcast(plugin.getMessageUtils().parse(
                plugin.getMessageUtils().getMessage("game-started-broadcast")));

        return true;
    }

    private boolean removeCooldown(Player player, String[] args) {
        boolean isAdmin = player.hasPermission("effectsmp.admin");
        boolean isTester = player.hasPermission("effectsmp.teszter");

        if (!isAdmin && !isTester) {
            plugin.getMessageUtils().sendMessage(player, "admin-no-permission");
            return true;
        }

        String type = "all";
        Player target = player;

        if (args.length >= 2) {
            type = args[1].toLowerCase();
        }

        if (args.length >= 3) {
            if (isTester && !isAdmin) {
                plugin.getMessageUtils().sendMessage(player, "admin-tester-self-only");
            } else {
                target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    plugin.getMessageUtils().sendMessage(player, "player-not-found");
                    return true;
                }
            }
        }

        UUID uuid = target.getUniqueId();
        boolean removedItem = false;
        boolean removedEffect = false;

        switch (type) {
            case "item", "items" -> {
                plugin.getItemAbilityManager().clearAllItemCooldowns(uuid);
                removedItem = true;
            }
            case "effect", "effects" -> {
                plugin.getEffectAbilityManager().clearAbilityCooldown(uuid);
                removedEffect = true;
            }
            case "all" -> {
                plugin.getItemAbilityManager().clearAllItemCooldowns(uuid);
                plugin.getEffectAbilityManager().clearAbilityCooldown(uuid);
                removedItem = true;
                removedEffect = true;
            }
            default -> {
                plugin.getMessageUtils().sendMessage(player, "usage-removecooldown");
                return true;
            }
        }

        String what = removedItem && removedEffect ? "Minden" : (removedItem ? "Item" : "Effect");

        if (target.equals(player)) {
            plugin.getMessageUtils().sendMessage(player, "admin-cooldown-cleared-self",
                    "%type%", what);
        } else {
            plugin.getMessageUtils().sendMessage(player, "admin-cooldown-cleared-sender",
                    "%player%", target.getName(),
                    "%type%", what.toLowerCase());
            plugin.getMessageUtils().sendMessage(target, "admin-cooldown-cleared-target",
                    "%type%", what);
        }

        return true;
    }

    private boolean craftReset(Player player, String[] args) {
        if (!player.hasPermission("effectsmp.admin") && !player.hasPermission("effectsmp.teszter")) {
            plugin.getMessageUtils().sendMessage(player, "admin-no-permission");
            return true;
        }

        if (args.length < 2) {
            plugin.getMessageUtils().sendMessage(player, "usage-craftreset");
            return true;
        }

        String target = args[1].toLowerCase();

        if (target.equals("all")) {
            plugin.getConfigManager().resetAllGlobalCraftedItems();
            plugin.getMessageUtils().sendMessage(player, "admin-craftreset-all");
        } else {
            if (!target.equals("effect_sword") && !target.equals("effect_mace") &&
                    !target.equals("effect_bow") && !target.equals("effect_scythe")) {
                plugin.getMessageUtils().sendMessage(player, "invalid-limited-item");
                return true;
            }

            plugin.getConfigManager().setGlobalItemCrafted(target, false);
            plugin.getMessageUtils().sendMessage(player, "admin-craftreset-item",
                    "%item%", target);
        }

        return true;
    }
}
