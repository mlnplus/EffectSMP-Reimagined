package mlnplus.hu.effectsmp.config;

import mlnplus.hu.effectsmp.Effectsmp;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {

    private final Effectsmp plugin;

    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration items;
    private FileConfiguration effects;

    private File configFile;
    private File messagesFile;
    private File itemsFile;
    private File effectsFile;

    public ConfigManager(Effectsmp plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private YamlConfiguration loadUTF8Yaml(File file) {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not load YAML file " + file.getName() + ": " + e.getMessage());
            return new YamlConfiguration();
        }
    }

    private void loadConfigs() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = loadUTF8Yaml(configFile);

        String lang = config.getString("language", "en");
        String messageFileName = "messages_" + lang + ".yml";
        checkAndLoadMessages(messageFileName);

        itemsFile = new File(plugin.getDataFolder(), "items.yml");
        if (!itemsFile.exists()) {
            plugin.saveResource("items.yml", false);
        }
        items = loadUTF8Yaml(itemsFile);

        effectsFile = new File(plugin.getDataFolder(), "effects.yml");
        if (!effectsFile.exists()) {
            plugin.saveResource("effects.yml", false);
        }
        effects = loadUTF8Yaml(effectsFile);
    }

    @SuppressWarnings("null")
    private void checkAndLoadMessages(String messageFileName) {
        messagesFile = new File(plugin.getDataFolder(), messageFileName);
        boolean updateNeeded = false;
        if (messagesFile.exists()) {
            FileConfiguration temp = loadUTF8Yaml(messagesFile);
            if (temp.getInt("version", 0) < 7) {
                updateNeeded = true;
                File backupFile = new File(plugin.getDataFolder(), messageFileName + ".old");
                if (backupFile.exists()) {
                    backupFile.delete();
                }
                messagesFile.renameTo(backupFile);
                plugin.getLogger().info("Outdated " + messageFileName + " backed up as " + messageFileName + ".old and updated to version 7.");
            }
        }
        if (!messagesFile.exists() || updateNeeded) {
            try {
                plugin.saveResource(messageFileName, true);
                messagesFile = new File(plugin.getDataFolder(), messageFileName);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning(
                        "Language file " + messageFileName + " not found in JAR. Falling back to messages_en.yml");
                messageFileName = "messages_en.yml";
                messagesFile = new File(plugin.getDataFolder(), messageFileName);
                if (!messagesFile.exists()) {
                    plugin.saveResource("messages_en.yml", false);
                }
            }
        }
        messages = loadUTF8Yaml(messagesFile);
    }

    public void reload() {
        config = loadUTF8Yaml(configFile);

        String lang = config.getString("language", "en");
        String messageFileName = "messages_" + lang + ".yml";
        checkAndLoadMessages(messageFileName);

        items = loadUTF8Yaml(itemsFile);
        effects = loadUTF8Yaml(effectsFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getItemsConfig() {
        return items;
    }

    public FileConfiguration getEffectsConfig() {
        return effects;
    }

    public boolean isGameStarted() {
        return Boolean.parseBoolean(plugin.getDatabaseManager().getServerData("game-started", "false"));
    }

    public void setGameStarted(boolean started) {
        plugin.getDatabaseManager().setServerData("game-started", String.valueOf(started));
    }

    public boolean isGlobalItemCrafted(String itemId) {
        String dataStr = plugin.getDatabaseManager().getServerData("global-crafted-items", "");
        if (dataStr.isEmpty()) return false;
        List<String> craftedItems = Arrays.asList(dataStr.split(","));
        return craftedItems.contains(itemId);
    }

    public void setGlobalItemCrafted(String itemId, boolean crafted) {
        String dataStr = plugin.getDatabaseManager().getServerData("global-crafted-items", "");
        List<String> craftedItems = new ArrayList<>();
        if (!dataStr.isEmpty()) {
            craftedItems.addAll(Arrays.asList(dataStr.split(",")));
        }

        if (crafted) {
            if (!craftedItems.contains(itemId)) {
                craftedItems.add(itemId);
            }
        } else {
            craftedItems.remove(itemId);
        }
        plugin.getDatabaseManager().setServerData("global-crafted-items", String.join(",", craftedItems));
    }

    public void resetAllGlobalCraftedItems() {
        plugin.getDatabaseManager().setServerData("global-crafted-items", "");
    }

    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }

    public String getDatabaseHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    public int getDatabasePort() {
        return config.getInt("database.mysql.port", 3306);
    }

    public String getDatabaseName() {
        return config.getString("database.mysql.database", "effectsmp");
    }

    public String getDatabaseUser() {
        return config.getString("database.mysql.username", "root");
    }

    public String getDatabasePassword() {
        return config.getString("database.mysql.password", "");
    }
}
