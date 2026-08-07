package mlnplus.hu.effectsmp.data;

import mlnplus.hu.effectsmp.Effectsmp;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final Effectsmp plugin;
    private final Map<UUID, PlayerData> playerDataCache;

    public PlayerDataManager(Effectsmp plugin) {
        this.plugin = plugin;
        this.playerDataCache = new ConcurrentHashMap<>();

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
        return plugin.getDatabaseManager().loadPlayerData(uuid);
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            plugin.getDatabaseManager().savePlayerData(data);
        }
    }

    public void saveAll() {
        for (PlayerData data : playerDataCache.values()) {
            if (data != null) {
                plugin.getDatabaseManager().savePlayerData(data);
            }
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

    public void resetAll() {
        playerDataCache.clear();
        plugin.getDatabaseManager().resetAllPlayerData();
    }
}
