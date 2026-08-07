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

    private YamlConfiguration loadAndMigrateYaml(File file, String resourceName) {
        if (!file.exists()) {
            try {
                plugin.saveResource(resourceName, false);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not save default resource " + resourceName + ": " + e.getMessage());
            }
        }

        YamlConfiguration existingConfig = loadUTF8Yaml(file);

        try (java.io.InputStream defaultStream = plugin.getResource(resourceName)) {
            if (defaultStream != null) {
                try (InputStreamReader reader = new InputStreamReader(defaultStream, StandardCharsets.UTF_8)) {
                    YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
                    boolean modified = false;

                    for (String key : defaultConfig.getKeys(true)) {
                        if (!existingConfig.contains(key)) {
                            existingConfig.set(key, defaultConfig.get(key));
                            modified = true;
                        }
                    }

                    if (modified) {
                        existingConfig.save(file);
                        plugin.getLogger().info("Updated " + file.getName() + " with new update options. Existing settings preserved.");
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not auto-migrate options for " + file.getName() + ": " + e.getMessage());
        }

        return existingConfig;
    }

    private void loadConfigs() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        config = loadAndMigrateYaml(configFile, "config.yml");

        String lang = config.getString("language", "en");
        String messageFileName = "messages_" + lang + ".yml";
        messagesFile = new File(plugin.getDataFolder(), messageFileName);

        if (plugin.getResource(messageFileName) == null) {
            messageFileName = "messages_en.yml";
            messagesFile = new File(plugin.getDataFolder(), messageFileName);
        }
        messages = loadAndMigrateYaml(messagesFile, messageFileName);

        itemsFile = new File(plugin.getDataFolder(), "items.yml");
        items = loadAndMigrateYaml(itemsFile, "items.yml");

        effectsFile = new File(plugin.getDataFolder(), "effects.yml");
        effects = loadAndMigrateYaml(effectsFile, "effects.yml");
    }

    public void reload() {
        loadConfigs();
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
