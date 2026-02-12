package mlnplus.hu.effectsmp.data;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.effects.EffectType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final Effectsmp plugin;
    private final Map<UUID, PlayerData> playerDataCache;
    private final File playersFolder;

    public PlayerDataManager(Effectsmp plugin) {
        this.plugin = plugin;
        this.playerDataCache = new ConcurrentHashMap<>();
        this.playersFolder = new File(plugin.getDataFolder(), "players");

        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getConfigManager().isGameStarted()) {
                plugin.setGameStarted(true);
            }
        }, 1L);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.computeIfAbsent(uuid, this::loadPlayerData);
    }

    public PlayerData loadPlayerData(UUID uuid) {
        File playerFile = new File(playersFolder, uuid.toString() + ".yml");
        PlayerData data = new PlayerData(uuid);

        if (playerFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

            data.setPlayerName(config.getString("name", "Unknown"));

            String effectName = config.getString("effect");
            if (effectName != null && !effectName.isEmpty()) {
                data.setEffect(EffectType.fromString(effectName));
            }

            data.setPassiveEnabled(config.getBoolean("passive-enabled", true));
            data.setEffectHearts(config.getInt("effect-hearts", 1));
            data.setHasEffectShard(config.getBoolean("has-effect-shard", true));
            data.setKills(config.getInt("kills", 0));
            data.setDeaths(config.getInt("deaths", 0));
            data.setFirstDeathOccurred(config.getBoolean("first-death-occurred", false));
            data.setLastAbilityCooldown(config.getLong("last-ability-cooldown", 0));
            data.setAbilityActiveUntil(config.getLong("ability-active-until", 0));

            List<String> trustedList = config.getStringList("trusted-players");
            for (String uuidStr : trustedList) {
                try {
                    data.addTrustedPlayer(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return data;
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data == null)
            return;

        File playerFile = new File(playersFolder, uuid.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("name", data.getPlayerName());
        config.set("effect", data.getEffect() != null ? data.getEffect().name() : null);
        config.set("passive-enabled", data.isPassiveEnabled());
        config.set("effect-hearts", data.getEffectHearts());
        config.set("has-effect-shard", data.hasEffectShard());
        config.set("kills", data.getKills());
        config.set("deaths", data.getDeaths());
        config.set("first-death-occurred", data.isFirstDeathOccurred());
        config.set("last-ability-cooldown", data.getLastAbilityCooldown());
        config.set("ability-active-until", data.getAbilityActiveUntil());

        List<String> trustedList = new ArrayList<>();
        for (UUID trusted : data.getTrustedPlayers()) {
            trustedList.add(trusted.toString());
        }
        config.set("trusted-players", trustedList);

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player data for " + uuid + ": " + e.getMessage());
        }
    }

    public void saveAll() {
        for (UUID uuid : playerDataCache.keySet()) {
            savePlayerData(uuid);
        }
    }

    public void unloadPlayer(UUID uuid) {
        savePlayerData(uuid);
        playerDataCache.remove(uuid);
    }

    public boolean isMutualTrust(UUID player1, UUID player2) {
        PlayerData data1 = getPlayerData(player1);
        PlayerData data2 = getPlayerData(player2);
        return data1.hasTrusted(player2) && data2.hasTrusted(player1);
    }

    public List<UUID> getMutualTrustedPlayers(UUID player) {
        PlayerData data = getPlayerData(player);
        List<UUID> mutual = new ArrayList<>();
        for (UUID trusted : data.getTrustedPlayers()) {
            PlayerData trustedData = getPlayerData(trusted);
            if (trustedData.hasTrusted(player)) {
                mutual.add(trusted);
            }
        }
        return mutual;
    }

    public Collection<PlayerData> getAllCachedData() {
        return playerDataCache.values();
    }
}
