package mlnplus.hu.effectsmp.data;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.effects.EffectType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private final Effectsmp plugin;
    private Connection connection;
    private String dbType;

    public DatabaseManager(Effectsmp plugin) {
        this.plugin = plugin;
    }

    public synchronized boolean initialize() {
        this.dbType = plugin.getConfigManager().getDatabaseType().toLowerCase();
        try {
            if ("mysql".equalsIgnoreCase(dbType)) {
                String host = plugin.getConfigManager().getDatabaseHost();
                int port = plugin.getConfigManager().getDatabasePort();
                String database = plugin.getConfigManager().getDatabaseName();
                String username = plugin.getConfigManager().getDatabaseUser();
                String password = plugin.getConfigManager().getDatabasePassword();

                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    Class.forName("com.mysql.jdbc.Driver");
                }

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
                this.connection = DriverManager.getConnection(url, username, password);
                plugin.getLogger().info("Connected to MySQL database: " + database);
            } else {
                // Default: SQLite localdb
                Class.forName("org.sqlite.JDBC");
                File dbFile = new File(plugin.getDataFolder(), "database.db");
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }
                if (dbFile.exists()) {
                    try {
                        File backupFile = new File(plugin.getDataFolder(), "database.db.bak");
                        java.nio.file.Files.copy(dbFile.toPath(), backupFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception ignored) {}
                }
                this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
                plugin.getLogger().info("Connected to local SQLite database: database.db (Backup saved to database.db.bak)");
            }

            createTables();
            checkAndMigrateSchema();
            migrateLegacyYaml();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database (" + dbType + "): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initialize();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database connection error: " + e.getMessage());
        }
        return connection;
    }

    private void createTables() throws SQLException {
        Connection conn = getConnection();
        if (conn == null) return;

        try (Statement stmt = conn.createStatement()) {
            // Player data table
            stmt.execute("CREATE TABLE IF NOT EXISTS player_data (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "name VARCHAR(32), " +
                    "effect VARCHAR(32), " +
                    "passive_enabled INT, " +
                    "effect_hearts INT, " +
                    "has_effect_shard INT, " +
                    "kills INT, " +
                    "deaths INT, " +
                    "first_death_occurred INT, " +
                    "last_ability_cooldown BIGINT, " +
                    "ability_active_until BIGINT, " +
                    "trusted_players TEXT" +
                    ")");

            // Server metadata table (game-started, global-crafted-items, etc.)
            stmt.execute("CREATE TABLE IF NOT EXISTS server_data (" +
                    "data_key VARCHAR(64) PRIMARY KEY, " +
                    "data_value TEXT" +
                    ")");
        }
    }

    private void checkAndMigrateSchema() {
        Connection conn = getConnection();
        if (conn == null) return;

        String[] columnDefs = {
            "name VARCHAR(32)",
            "effect VARCHAR(32)",
            "passive_enabled INT DEFAULT 1",
            "effect_hearts INT DEFAULT 3",
            "has_effect_shard INT DEFAULT 1",
            "kills INT DEFAULT 0",
            "deaths INT DEFAULT 0",
            "first_death_occurred INT DEFAULT 0",
            "last_ability_cooldown BIGINT DEFAULT 0",
            "ability_active_until BIGINT DEFAULT 0",
            "trusted_players TEXT"
        };

        for (String colDef : columnDefs) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE player_data ADD COLUMN " + colDef);
            } catch (SQLException ignored) {
                // Column already exists, safe to ignore
            }
        }
    }

    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error closing database connection: " + e.getMessage());
        }
    }

    // --- Player Data Queries ---

    public synchronized PlayerData loadPlayerData(UUID uuid) {
        Connection conn = getConnection();
        if (conn == null) return new PlayerData(uuid);

        String sql = "SELECT * FROM player_data WHERE uuid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PlayerData data = new PlayerData(uuid);
                    data.setPlayerName(rs.getString("name"));
                    String effectStr = rs.getString("effect");
                    if (effectStr != null && !effectStr.isEmpty()) {
                        data.setEffect(EffectType.fromString(effectStr));
                    }
                    data.setPassiveEnabled(rs.getInt("passive_enabled") != 0);
                    data.setEffectHearts(rs.getInt("effect_hearts"));
                    data.setHasEffectShard(rs.getInt("has_effect_shard") != 0);
                    data.setKills(rs.getInt("kills"));
                    data.setDeaths(rs.getInt("deaths"));
                    data.setFirstDeathOccurred(rs.getInt("first_death_occurred") != 0);
                    data.setLastAbilityCooldown(rs.getLong("last_ability_cooldown"));
                    data.setAbilityActiveUntil(rs.getLong("ability_active_until"));

                    String trustedStr = rs.getString("trusted_players");
                    if (trustedStr != null && !trustedStr.isEmpty()) {
                        for (String tUuid : trustedStr.split(",")) {
                            try {
                                if (!tUuid.trim().isEmpty()) {
                                    data.addTrustedPlayer(UUID.fromString(tUuid.trim()));
                                }
                            } catch (IllegalArgumentException ignored) {}
                        }
                    }
                    return data;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error loading player data for " + uuid + ": " + e.getMessage());
        }
        return new PlayerData(uuid);
    }

    public synchronized void savePlayerData(PlayerData data) {
        Connection conn = getConnection();
        if (conn == null || data == null) return;

        String sql;
        if ("mysql".equalsIgnoreCase(dbType)) {
            sql = "INSERT INTO player_data (uuid, name, effect, passive_enabled, effect_hearts, has_effect_shard, kills, deaths, first_death_occurred, last_ability_cooldown, ability_active_until, trusted_players) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                  "ON DUPLICATE KEY UPDATE name=?, effect=?, passive_enabled=?, effect_hearts=?, has_effect_shard=?, kills=?, deaths=?, first_death_occurred=?, last_ability_cooldown=?, ability_active_until=?, trusted_players=?";
        } else {
            sql = "INSERT OR REPLACE INTO player_data (uuid, name, effect, passive_enabled, effect_hearts, has_effect_shard, kills, deaths, first_death_occurred, last_ability_cooldown, ability_active_until, trusted_players) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }

        StringBuilder trustedBuilder = new StringBuilder();
        for (UUID t : data.getTrustedPlayers()) {
            if (trustedBuilder.length() > 0) trustedBuilder.append(",");
            trustedBuilder.append(t.toString());
        }
        String trustedStr = trustedBuilder.toString();
        String effectStr = data.getEffect() != null ? data.getEffect().name() : "";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, data.getUuid().toString());
            ps.setString(2, data.getPlayerName() != null ? data.getPlayerName() : "Unknown");
            ps.setString(3, effectStr);
            ps.setInt(4, data.isPassiveEnabled() ? 1 : 0);
            ps.setInt(5, data.getEffectHearts());
            ps.setInt(6, data.hasEffectShard() ? 1 : 0);
            ps.setInt(7, data.getKills());
            ps.setInt(8, data.getDeaths());
            ps.setInt(9, data.isFirstDeathOccurred() ? 1 : 0);
            ps.setLong(10, data.getLastAbilityCooldown());
            ps.setLong(11, data.getAbilityActiveUntil());
            ps.setString(12, trustedStr);

            if ("mysql".equalsIgnoreCase(dbType)) {
                ps.setString(13, data.getPlayerName() != null ? data.getPlayerName() : "Unknown");
                ps.setString(14, effectStr);
                ps.setInt(15, data.isPassiveEnabled() ? 1 : 0);
                ps.setInt(16, data.getEffectHearts());
                ps.setInt(17, data.hasEffectShard() ? 1 : 0);
                ps.setInt(18, data.getKills());
                ps.setInt(19, data.getDeaths());
                ps.setInt(20, data.isFirstDeathOccurred() ? 1 : 0);
                ps.setLong(21, data.getLastAbilityCooldown());
                ps.setLong(22, data.getAbilityActiveUntil());
                ps.setString(23, trustedStr);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error saving player data for " + data.getUuid() + ": " + e.getMessage());
        }
    }

    // --- Server Metadata Queries ---

    public synchronized String getServerData(String key, String defaultValue) {
        Connection conn = getConnection();
        if (conn == null) return defaultValue;

        String sql = "SELECT data_value FROM server_data WHERE data_key = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("data_value");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error reading server data key " + key + ": " + e.getMessage());
        }
        return defaultValue;
    }

    public synchronized void setServerData(String key, String value) {
        Connection conn = getConnection();
        if (conn == null) return;

        String sql;
        if ("mysql".equalsIgnoreCase(dbType)) {
            sql = "INSERT INTO server_data (data_key, data_value) VALUES (?, ?) ON DUPLICATE KEY UPDATE data_value = ?";
        } else {
            sql = "INSERT OR REPLACE INTO server_data (data_key, data_value) VALUES (?, ?)";
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            if ("mysql".equalsIgnoreCase(dbType)) {
                ps.setString(3, value);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error saving server data key " + key + ": " + e.getMessage());
        }
    }

    // --- Legacy YAML Migration ---

    private void migrateLegacyYaml() {
        // 1. Migrate data.yml if present
        File oldDataFile = new File(plugin.getDataFolder(), "data.yml");
        if (oldDataFile.exists()) {
            try {
                FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldDataFile);
                if (oldConfig.contains("game-started")) {
                    setServerData("game-started", String.valueOf(oldConfig.getBoolean("game-started")));
                }
                if (oldConfig.contains("global-crafted-items")) {
                    List<String> list = oldConfig.getStringList("global-crafted-items");
                    setServerData("global-crafted-items", String.join(",", list));
                }
                File backup = new File(plugin.getDataFolder(), "data.yml.old");
                if (backup.exists()) backup.delete();
                oldDataFile.renameTo(backup);
                plugin.getLogger().info("Migrated legacy data.yml to database.");
            } catch (Exception e) {
                plugin.getLogger().warning("Could not migrate data.yml: " + e.getMessage());
            }
        }

        // 2. Migrate players/ folder if present
        File oldPlayersFolder = new File(plugin.getDataFolder(), "players");
        if (oldPlayersFolder.exists() && oldPlayersFolder.isDirectory()) {
            File[] files = oldPlayersFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null && files.length > 0) {
                int count = 0;
                for (File pFile : files) {
                    try {
                        String nameStr = pFile.getName().replace(".yml", "");
                        UUID uuid = UUID.fromString(nameStr);
                        FileConfiguration pConfig = YamlConfiguration.loadConfiguration(pFile);

                        PlayerData data = new PlayerData(uuid);
                        data.setPlayerName(pConfig.getString("name", "Unknown"));
                        String effectName = pConfig.getString("effect");
                        if (effectName != null && !effectName.isEmpty()) {
                            data.setEffect(EffectType.fromString(effectName));
                        }
                        data.setPassiveEnabled(pConfig.getBoolean("passive-enabled", true));
                        data.setEffectHearts(pConfig.getInt("effect-hearts", 1));
                        data.setHasEffectShard(pConfig.getBoolean("has-effect-shard", true));
                        data.setKills(pConfig.getInt("kills", 0));
                        data.setDeaths(pConfig.getInt("deaths", 0));
                        data.setFirstDeathOccurred(pConfig.getBoolean("first-death-occurred", false));
                        data.setLastAbilityCooldown(pConfig.getLong("last-ability-cooldown", 0));
                        data.setAbilityActiveUntil(pConfig.getLong("ability-active-until", 0));

                        List<String> trustedList = pConfig.getStringList("trusted-players");
                        for (String uStr : trustedList) {
                            try {
                                data.addTrustedPlayer(UUID.fromString(uStr));
                            } catch (IllegalArgumentException ignored) {}
                        }

                        savePlayerData(data);
                        File pBackup = new File(oldPlayersFolder, pFile.getName() + ".old");
                        if (pBackup.exists()) pBackup.delete();
                        pFile.renameTo(pBackup);
                        count++;
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to migrate player file " + pFile.getName() + ": " + e.getMessage());
                    }
                }
                plugin.getLogger().info("Migrated " + count + " legacy player YAML files to database.");
            }
        }
    }

    public synchronized void resetAllPlayerData() {
        Connection conn = getConnection();
        if (conn == null) return;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM player_data");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error resetting all player data: " + e.getMessage());
        }
    }
}
