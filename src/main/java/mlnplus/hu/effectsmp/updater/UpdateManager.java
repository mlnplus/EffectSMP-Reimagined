package mlnplus.hu.effectsmp.updater;

import mlnplus.hu.effectsmp.Effectsmp;
import org.bukkit.Bukkit;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("deprecation")
public class UpdateManager {

    private final Effectsmp plugin;
    private static final String GITHUB_API_URL = "https://api.github.com/repos/mlnplus/EffectSMP-Reimagined/releases/latest";
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
                HttpURLConnection connection = (HttpURLConnection) java.net.URI.create(GITHUB_API_URL).toURL().openConnection();
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

                if (!rootElement.isJsonObject()) {
                    return;
                }

                JsonObject json = rootElement.getAsJsonObject();
                if (!json.has("tag_name")) {
                    return;
                }

                String latestVersionRaw = json.get("tag_name").getAsString();
                String latestVersion = latestVersionRaw.replace("v", "").trim();
                String currentVersion = plugin.getDescription().getVersion().replace("v", "").trim();

                if (isNewerVersion(currentVersion, latestVersion)) {
                    plugin.getLogger().warning("==================================================");
                    plugin.getLogger().warning("A new version of EffectSMP is available!");
                    plugin.getLogger().warning("Latest Version: v" + latestVersion);
                    plugin.getLogger().warning("Current Version: v" + currentVersion);
                    plugin.getLogger().warning("Download here: " + MODRINTH_URL);
                    plugin.getLogger().warning("==================================================");

                    if (plugin.getConfigManager().getConfig().getBoolean("updater.auto-download", false)) {
                        downloadUpdate(json);
                    }
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

    private void downloadUpdate(JsonObject releaseJson) {
        if (!releaseJson.has("assets")) {
            return;
        }

        JsonArray assets = releaseJson.getAsJsonArray("assets");
        String downloadUrl = null;
        for (JsonElement assetElement : assets) {
            if (assetElement.isJsonObject()) {
                JsonObject asset = assetElement.getAsJsonObject();
                if (asset.has("name") && asset.get("name").getAsString().endsWith(".jar")) {
                    downloadUrl = asset.get("browser_download_url").getAsString();
                    break;
                }
            }
        }

        if (downloadUrl == null) {
            plugin.getLogger().warning("No jar file asset found in the latest release. Auto-download aborted.");
            return;
        }

        plugin.getLogger().info("Downloading new update from GitHub Releases...");

        try {
            File runningJar = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            File updateFolder = new File(plugin.getDataFolder().getParentFile(), "update");
            if (!updateFolder.exists()) {
                updateFolder.mkdirs();
            }

            File targetFile = new File(updateFolder, runningJar.getName());

            HttpURLConnection connection = (HttpURLConnection) java.net.URI.create(downloadUrl).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "EffectSMP-Updater");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // Handle redirect if any
            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == 307 || status == 308) {
                String newUrl = connection.getHeaderField("Location");
                connection = (HttpURLConnection) java.net.URI.create(newUrl).toURL().openConnection();
                connection.setRequestProperty("User-Agent", "EffectSMP-Updater");
            }

            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            }

            plugin.getLogger().info("Update successfully downloaded to " + targetFile.getPath());
            plugin.getLogger().info("The update will be applied automatically on the next server restart.");

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to auto-download update: " + e.getMessage());
        }
    }
}
