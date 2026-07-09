package mlnplus.hu.effectsmp.updater;

import mlnplus.hu.effectsmp.Effectsmp;
import org.bukkit.Bukkit;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("deprecation")
public class UpdateManager {

    private final Effectsmp plugin;
    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/project/effectsmp-reimagined/version";
    private static final String MODRINTH_URL = "https://modrinth.com/plugin/effectsmp-reimagined";

    public UpdateManager(Effectsmp plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        if (!plugin.getConfigManager().getConfig().getBoolean("updater.check-updates", true)) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) java.net.URI.create(MODRINTH_API_URL).toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "EffectSMP-Updater");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() != 200) {
                    plugin.getLogger().warning("Failed to check for updates: HTTP " + connection.getResponseCode());
                    return;
                }

                InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
                JsonElement rootElement = new JsonParser().parse(reader);
                reader.close();

                if (!rootElement.isJsonArray()) {
                    return;
                }

                JsonArray versionsArray = rootElement.getAsJsonArray();
                if (versionsArray.isEmpty()) {
                    return;
                }

                // First version returned by Modrinth API is the latest one
                JsonObject latestRelease = versionsArray.get(0).getAsJsonObject();
                if (!latestRelease.has("version_number")) {
                    return;
                }

                String latestVersionRaw = latestRelease.get("version_number").getAsString();
                String latestVersion = latestVersionRaw.replace("v", "").trim();
                String currentVersion = plugin.getDescription().getVersion().replace("v", "").trim();

                if (isNewerVersion(currentVersion, latestVersion)) {
                    plugin.getLogger().warning("==================================================");
                    plugin.getLogger().warning("A new version of EffectSMP is available on Modrinth!");
                    plugin.getLogger().warning("Latest Version: v" + latestVersion);
                    plugin.getLogger().warning("Current Version: v" + currentVersion);
                    plugin.getLogger().warning("Download here: " + MODRINTH_URL);
                    plugin.getLogger().warning("==================================================");
                } else {
                    plugin.getLogger().info("EffectSMP is up to date (v" + currentVersion + ").");
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Could not check for updates: " + e.getMessage());
            }
        });
    }

    private boolean isNewerVersion(String current, String latest) {
        try {
            String[] currentParts = current.split("\\.");
            String[] latestParts = latest.split("\\.");
            int length = Math.max(currentParts.length, latestParts.length);
            for (int i = 0; i < length; i++) {
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
                int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
                if (latestPart > currentPart) {
                    return true;
                }
                if (currentPart > latestPart) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            // Fallback to basic string comparison if version structure is weird
            return !current.equalsIgnoreCase(latest);
        }
        return false;
    }
}
