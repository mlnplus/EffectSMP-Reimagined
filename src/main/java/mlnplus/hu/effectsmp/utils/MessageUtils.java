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

    public Component parse(String message) {
        if (message.contains("<") && message.contains(">")) {
            return miniMessage.deserialize(message);
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
        if (seconds >= 60) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + "p " + remainingSeconds + "mp";
        }
        return seconds + "mp";
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
