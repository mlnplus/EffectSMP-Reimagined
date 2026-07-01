package mlnplus.hu.effectsmp.config;

import mlnplus.hu.effectsmp.Effectsmp;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ConfigManager {

    private final Effectsmp plugin;

    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration recipes;
    private FileConfiguration data;

    private File configFile;
    private File messagesFile;
    private File recipesFile;
    private File dataFile;

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

        messagesFile = new File(plugin.getDataFolder(), messageFileName);
        if (!messagesFile.exists()) {
            try {
                plugin.saveResource(messageFileName, false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning(
                        "Language file " + messageFileName + " not found in JAR. Falling back to messages_hu.yml");
                messageFileName = "messages_hu.yml";
                messagesFile = new File(plugin.getDataFolder(), messageFileName);
                if (!messagesFile.exists()) {
                    plugin.saveResource("messages_hu.yml", false);
                }
            }
        }
        messages = loadUTF8Yaml(messagesFile);

        recipesFile = new File(plugin.getDataFolder(), "recipes.yml");
        if (!recipesFile.exists()) {
            plugin.saveResource("recipes.yml", false);
        }
        recipes = loadUTF8Yaml(recipesFile);

        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml: " + e.getMessage());
            }
        }
        data = loadUTF8Yaml(dataFile);
    }

    public void reload() {
        config = loadUTF8Yaml(configFile);

        String lang = config.getString("language", "hu");
        String messageFileName = "messages_" + lang + ".yml";
        messagesFile = new File(plugin.getDataFolder(), messageFileName);
        if (!messagesFile.exists()) {
            try {
                plugin.saveResource(messageFileName, false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Language file " + messageFileName + " not found in JAR.");
            }
        }
        if (messagesFile.exists()) {
            messages = loadUTF8Yaml(messagesFile);
        }

        recipes = loadUTF8Yaml(recipesFile);
        data = loadUTF8Yaml(dataFile);
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getRecipes() {
        return recipes;
    }

    public FileConfiguration getData() {
        return data;
    }

    public boolean isGameStarted() {
        return data.getBoolean("game-started", false);
    }

    public void setGameStarted(boolean started) {
        data.set("game-started", started);
        saveData();
    }

    public boolean isGlobalItemCrafted(String itemId) {
        return data.getStringList("global-crafted-items").contains(itemId);
    }

    public void setGlobalItemCrafted(String itemId, boolean crafted) {
        List<String> craftedItems = data.getStringList("global-crafted-items");
        if (crafted) {
            if (!craftedItems.contains(itemId)) {
                craftedItems.add(itemId);
            }
        } else {
            craftedItems.remove(itemId);
        }
        data.set("global-crafted-items", craftedItems);
        saveData();
    }

    public void resetAllGlobalCraftedItems() {
        data.set("global-crafted-items", null);
        saveData();
    }

    public String getDatabaseType() {
        return config.getString("database.type", "yaml");
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
