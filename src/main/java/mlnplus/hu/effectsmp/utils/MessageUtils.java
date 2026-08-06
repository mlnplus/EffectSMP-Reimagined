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
        this.prefix = messages.getString("prefix", "<dark_gray>「</dark_gray><gradient:#8A00E6:#DF00FF><b>Effect</b></gradient><gradient:#FF8C00:#FFC800><b>SMP</b></gradient><dark_gray>」</dark_gray> ");
    }

    public Component parse(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        // Normalize legacy section symbols § to ampersands &
        String clean = message.replace('§', '&');

        // Convert legacy color codes (&0-&f, &l-&r) into MiniMessage tags
        clean = convertLegacyToMiniMessage(clean);

        try {
            return miniMessage.deserialize(clean);
        } catch (Exception e) {
            try {
                return legacySerializer.deserialize(message.replace('§', '&'));
            } catch (Exception ex) {
                return Component.text(message);
            }
        }
    }

    private String convertLegacyToMiniMessage(String text) {
        if (text == null || !text.contains("&")) return text;

        String result = text;
        String[] legacy = {
                "&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9",
                "&a", "&b", "&c", "&d", "&e", "&f",
                "&A", "&B", "&C", "&D", "&E", "&F",
                "&k", "&K", "&l", "&L", "&m", "&M", "&n", "&N", "&o", "&O", "&r", "&R"
        };
        String[] mini = {
                "<black>", "<dark_blue>", "<dark_green>", "<dark_aqua>", "<dark_red>", "<dark_purple>", "<gold>", "<gray>", "<dark_gray>", "<blue>",
                "<green>", "<aqua>", "<red>", "<light_purple>", "<yellow>", "<white>",
                "<green>", "<aqua>", "<red>", "<light_purple>", "<yellow>", "<white>",
                "<obfuscated>", "<obfuscated>", "<b>", "<b>", "<strikethrough>", "<strikethrough>", "<u>", "<u>", "<i>", "<i>", "<reset>", "<reset>"
        };

        for (int i = 0; i < legacy.length; i++) {
            result = result.replace(legacy[i], mini[i]);
        }
        return result;
    }

    public String getMessage(String key) {
        FileConfiguration messages = plugin.getConfigManager().getMessages();
        return messages.getString(key, "<red>Message not found: " + key + "</red>");
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

    public void sendRaw(Player player, String message) {
        player.sendMessage(parse(message));
    }

    public void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(parse(message));
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
