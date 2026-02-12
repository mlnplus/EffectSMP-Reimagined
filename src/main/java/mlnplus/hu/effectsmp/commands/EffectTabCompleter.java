package mlnplus.hu.effectsmp.commands;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.effects.EffectType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EffectTabCompleter implements TabCompleter {

    private final Effectsmp plugin;

    private static final List<String> MAIN_COMMANDS = Arrays.asList(
            "activate", "info", "withdraw", "trust", "untrust", "toggle",
            "set", "give", "reload", "start", "removecooldown", "rc", "craftreset");

    private static final List<String> ITEMS = Arrays.asList(
            "heart", "shard", "reroll", "op_reroll", "mace", "sword", "bow", "scythe");

    private static final List<String> LIMITED_ITEMS = Arrays.asList(
            "effect_sword", "effect_mace", "effect_bow", "effect_scythe", "all");

    private static final List<String> COOLDOWN_TYPES = Arrays.asList("item", "effect", "all");

    public EffectTabCompleter(Effectsmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        boolean isAdmin = player.hasPermission("effectsmp.admin");
        boolean isTester = player.hasPermission("effectsmp.teszter");

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            completions = MAIN_COMMANDS.stream()
                    .filter(cmd -> cmd.startsWith(input))
                    .collect(Collectors.toList());

            if (!isAdmin && !isTester) {
                completions.removeAll(
                        Arrays.asList("set", "give", "reload", "start", "removecooldown", "rc", "craftreset"));
            } else if (isTester && !isAdmin) {
                completions.remove("reload");
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            String input = args[1].toLowerCase();

            switch (subCommand) {
                case "trust", "untrust" -> {
                    completions = Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());
                }
                case "set" -> {
                    if (isAdmin || isTester) {
                        completions = Arrays.stream(EffectType.values())
                                .map(e -> e.name().toLowerCase())
                                .filter(name -> name.startsWith(input))
                                .collect(Collectors.toList());
                    }
                }
                case "give" -> {
                    if (isAdmin || isTester) {
                        completions = ITEMS.stream()
                                .filter(item -> item.startsWith(input))
                                .collect(Collectors.toList());
                    }
                }
                case "withdraw" -> {
                    completions = Arrays.asList("1", "2", "3", "5", "10");
                }
                case "removecooldown", "rc" -> {
                    if (isAdmin || isTester) {
                        completions = COOLDOWN_TYPES.stream()
                                .filter(type -> type.startsWith(input))
                                .collect(Collectors.toList());
                    }
                }
                case "craftreset" -> {
                    if (isAdmin || isTester) {
                        completions = LIMITED_ITEMS.stream()
                                .filter(item -> item.startsWith(input))
                                .collect(Collectors.toList());
                    }
                }
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            String input = args[2].toLowerCase();

            if (isAdmin) {
                if (subCommand.equals("set") || subCommand.equals("give") ||
                        subCommand.equals("removecooldown") || subCommand.equals("rc")) {
                    completions = Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());
                }
            }
        }

        return completions;
    }
}
