package mlnplus.hu.effectsmp.utils;

import mlnplus.hu.effectsmp.Effectsmp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.Duration;

@SuppressWarnings("null")
public class MessageUtils {

    private final Effectsmp plugin;
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;

    private String prefix;

    public MessageUtils(Effectsmp plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();
        reload();
    }

    public void reload() {
        FileConfiguration messages = plugin.getConfigManager().getMessages();
        this.prefix = messages.getString("prefix", "§8[§dEffectSMP§8] §7");
    }

    private boolean isMiniMessage(String message) {
        if (message == null)
            return false;
        if (!message.contains("<") || !message.contains(">"))
            return false;

        // If it contains legacy colors, it's legacy, unless it has a gradient or
        // rainbow tag
        if (message.contains("&0") || message.contains("&1") || message.contains("&2") || message.contains("&3") ||
                message.contains("&4") || message.contains("&5") || message.contains("&6") || message.contains("&7") ||
                message.contains("&8") || message.contains("&9") || message.contains("&a") || message.contains("&b") ||
                message.contains("&c") || message.contains("&d") || message.contains("&e") || message.contains("&f") ||
                message.contains("&l") || message.contains("&m") || message.contains("&n") || message.contains("&o") ||
                message.contains("&r") || message.contains("&A") || message.contains("&B") || message.contains("&C") ||
                message.contains("&D") || message.contains("&E") || message.contains("&F") || message.contains("&L") ||
                message.contains("&M") || message.contains("&N") || message.contains("&O") || message.contains("&R")) {

            return message.contains("<gradient") || message.contains("<rainbow");
        }

        // Check for common MiniMessage tags to be sure it's meant to be MiniMessage
        return message.contains("<gradient") || message.contains("<rainbow") ||
                message.contains("<red>") || message.contains("<green>") || message.contains("<blue>") ||
                message.contains("<yellow>") || message.contains("<gold>") || message.contains("<aqua>") ||
                message.contains("<gray>") || message.contains("<white>") || message.contains("<black>") ||
                message.contains("<bold>") || message.contains("<italic>") || message.contains("<underlined>") ||
                message.contains("<hover:") || message.contains("<click:") || message.contains("<transition") ||
                message.contains("<font");
    }

    private String translateLegacyToMiniMessage(String message) {
        if (message == null)
            return "";
        String result = message;
        String[] legacy = { "&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9",
                "&a", "&b", "&c", "&d", "&e", "&f", "&k", "&l", "&m", "&n", "&o", "&r",
                "&A", "&B", "&C", "&D", "&E", "&F", "&K", "&L", "&M", "&N", "&O", "&R" };
        String[] mini = { "<black>", "<dark_blue>", "<dark_green>", "<dark_aqua>", "<dark_red>", "<dark_purple>",
                "<gold>", "<gray>", "<dark_gray>", "<blue>",
                "<green>", "<aqua>", "<red>", "<light_purple>", "<yellow>", "<white>", "<obfuscated>", "<bold>",
                "<strikethrough>", "<underlined>", "<italic>", "<reset>",
                "<green>", "<aqua>", "<red>", "<light_purple>", "<yellow>", "<white>", "<obfuscated>", "<bold>",
                "<strikethrough>", "<underlined>", "<italic>", "<reset>" };
        for (int i = 0; i < legacy.length; i++) {
            result = result.replace(legacy[i], mini[i]);
        }
        return result;
    }

    public Component parse(String message) {
        if (message == null) {
            return Component.empty();
        }
        if (isMiniMessage(message)) {
            try {
                String translated = translateLegacyToMiniMessage(message);
                return miniMessage.deserialize(translated);
            } catch (Exception e) {
                return legacySerializer.deserialize(message);
            }
        }
        return legacySerializer.deserialize(message);
    }

    public String getMessage(String key) {
        FileConfiguration messages = plugin.getConfigManager().getMessages();
        return messages.getString(key, "&cMessage not found: " + key);
    }

    public Component getMessageComponent(String key) {
        return parse(getMessage(key));
    }

    public Component getMessageWithPrefix(String key) {
        return parse(prefix + getMessage(key));
    }

    public void sendMessage(CommandSender sender, String key, String... placeholders) {
        String msg = getMessage(key);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        sender.sendMessage(parse(prefix + msg));
    }

    public void sendMessage(Player player, String key, String... placeholders) {
        String msg = getMessage(key);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        player.sendMessage(parse(prefix + msg));
    }

    public void broadcast(String key, String... placeholders) {
        String msg = getMessage(key);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        plugin.getServer().broadcast(parse(prefix + msg));
    }

    public void sendRawMessage(Player player, String message) {
        player.sendMessage(parse(prefix + message));
    }

    public void sendTitle(Player player, String titleKey, String subtitleKey, String... placeholders) {
        String titleMsg = getMessage(titleKey);
        String subtitleMsg = subtitleKey != null ? getMessage(subtitleKey) : "";

        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                titleMsg = titleMsg.replace(placeholders[i], placeholders[i + 1]);
                subtitleMsg = subtitleMsg.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        Component title = parse(titleMsg);
        Component subtitle = subtitleKey != null ? parse(subtitleMsg) : Component.empty();

        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(3000),
                Duration.ofMillis(500));

        player.showTitle(Title.title(title, subtitle, times));
    }

    public void sendTitle(Player player, String titleText, String subtitleText, int fadeIn, int stay, int fadeOut) {
        Component title = parse(titleText);
        Component subtitle = subtitleText != null ? parse(subtitleText) : Component.empty();

        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L));

        player.showTitle(Title.title(title, subtitle, times));
    }

    public void sendActionBar(Player player, String message) {
        player.sendActionBar(parse(message));
    }

    public String formatTime(long millis) {
        long seconds = millis / 1000;
        String lang = plugin.getConfigManager().getConfig().getString("language", "en");
        boolean isHu = "hu".equalsIgnoreCase(lang);
        if (seconds >= 60) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            if (isHu) {
                return minutes + "p " + remainingSeconds + "mp";
            } else {
                return minutes + "m " + remainingSeconds + "s";
            }
        }
        return seconds + (isHu ? "mp" : "s");
    }

    public String formatTimeShort(long millis) {
        long seconds = millis / 1000;
        if (seconds >= 60) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return String.format("%d:%02d", minutes, remainingSeconds);
        }
        return seconds + "s";
    }

    public static String colorize(String message) {
        return message.replace("&", "§");
    }

    public String getPrefix() {
        return prefix;
    }
}
