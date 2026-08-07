package mlnplus.hu.effectsmp;

import mlnplus.hu.effectsmp.commands.EffectCommand;
import mlnplus.hu.effectsmp.commands.EffectTabCompleter;
import mlnplus.hu.effectsmp.config.ConfigManager;
import mlnplus.hu.effectsmp.data.DatabaseManager;
import mlnplus.hu.effectsmp.data.PlayerDataManager;
import mlnplus.hu.effectsmp.effects.EffectAbilityManager;
import mlnplus.hu.effectsmp.effects.RollAnimationManager;
import mlnplus.hu.effectsmp.gui.GUIManager;
import mlnplus.hu.effectsmp.items.CustomItems;
import mlnplus.hu.effectsmp.items.ItemAbilityManager;
import mlnplus.hu.effectsmp.listeners.CraftingListener;
import mlnplus.hu.effectsmp.listeners.FreezeListener;
import mlnplus.hu.effectsmp.listeners.GUIListener;
import mlnplus.hu.effectsmp.listeners.ItemListener;
import mlnplus.hu.effectsmp.listeners.ItemProtectionListener;
import mlnplus.hu.effectsmp.listeners.LootListener;
import mlnplus.hu.effectsmp.listeners.MiningListener;
import mlnplus.hu.effectsmp.listeners.PlayerListener;
import mlnplus.hu.effectsmp.effects.DashManager;
import mlnplus.hu.effectsmp.utils.ActionBarManager;
import mlnplus.hu.effectsmp.utils.MessageUtils;
import mlnplus.hu.effectsmp.updater.UpdateManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Effectsmp extends JavaPlugin {

    private static Effectsmp instance;

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private EffectAbilityManager effectAbilityManager;
    private RollAnimationManager rollAnimationManager;
    private ItemAbilityManager itemAbilityManager;
    private GUIManager guiManager;
    private CustomItems customItems;
    private ActionBarManager actionBarManager;
    private MessageUtils messageUtils;
    private DashManager dashManager;
    private UpdateManager updateManager;

    private boolean gameStarted = false;

    private boolean bannerPrinted = false;

    public synchronized void printEnableBanner(String updateStatus) {
        if (bannerPrinted) return;
        bannerPrinted = true;

        String lang = configManager != null ? configManager.getConfig().getString("language", "en") : "en";
        boolean isHu = "hu".equalsIgnoreCase(lang);

        final String R = "\033[0m";  // Reset
        final String B = "\033[1m";  // Bold
        final String c1 = "\033[38;2;217;70;239m"; // #D946EF — Magenta/Pink
        final String c2 = "\033[38;2;168;85;247m";  // #A855F7 — Purple
        final String c3 = "\033[38;2;139;92;246m";  // #8B5CF6 — Violet
        final String c4 = "\033[38;2;99;102;241m";   // #6366F1 - Indigo

        org.bukkit.Bukkit.getConsoleSender().sendMessage("");
        org.bukkit.Bukkit.getConsoleSender().sendMessage(c1 + B + "  ███████╗███████╗███████╗███████╗ ██████╗████████╗   ███████╗███╗   ███╗██████╗ " + R);
        org.bukkit.Bukkit.getConsoleSender().sendMessage(c1 + B + "  ██╔════╝██╔════╝██╔════╝██╔════╝██╔════╝╚══██╔══╝   ██╔════╝████╗ ████║██╔══██╗" + R);
        org.bukkit.Bukkit.getConsoleSender().sendMessage(c2 + B + "  █████╗  █████╗  █████╗  █████╗  ██║        ██║      ███████╗██╔████╔██║██████╔╝" + R);
        org.bukkit.Bukkit.getConsoleSender().sendMessage(c3 + B + "  ██╔══╝  ██╔══╝  ██╔══╝  ██╔══╝  ██║        ██║      ╚════██║██║╚██╔╝██║██╔═══╝ " + R);
        org.bukkit.Bukkit.getConsoleSender().sendMessage(c3 + B + "  ███████╗██║     ██║     ███████╗╚██████╗   ██║      ███████║██║ ╚═╝ ██║██║     " + R);
        org.bukkit.Bukkit.getConsoleSender().sendMessage(c4 + B + "  ╚══════╝╚═╝     ╚═╝     ╚══════╝ ╚═════╝   ╚═╝      ╚══════╝╚═╝     ╚═╝╚═╝     " + R);
        org.bukkit.Bukkit.getConsoleSender().sendMessage("");

        int width = 77;
        org.bukkit.Bukkit.getConsoleSender().sendMessage(c1 + "\u256d" + "\u2500".repeat(width + 2) + "\u256e" + R);
        printBoxedLine("&dEffectSMP &8| &7Reimagined Minecraft Vanilla Effects", width, c1, R);
        org.bukkit.Bukkit.getConsoleSender().sendMessage(c1 + "\u251c" + "\u2500".repeat(width + 2) + "\u2524" + R);
        printBoxedLine(" &e\u2726 &fVersion: &b" + getPluginMeta().getVersion(), width, c1, R);
        if (isHu) {
            printBoxedLine(" &e\u2726 &fNyelv: &7Magyar (HU)", width, c1, R);
            if (gameStarted) {            printBoxedLine(" &e\u2726 &fWebsite: &bmlnplus.hu", width, c1, R);

                printBoxedLine(" &e\u2726 &fStátusz: &aJáték elindítva, effektek aktívak! &7(v" + getPluginMeta().getVersion() + ")", width, c1, R);
            } else {            printBoxedLine(" &e\u2726 &fWebsite: &bmlnplus.hu", width, c1, R);

                printBoxedLine(" &e\u2726 &fStátusz: &cKészenlét - Játék még nincs elindítva! (/e start)", width, c1, R);
            }
        } else {
            printBoxedLine(" &e\u2726 &fLanguage: &7English (EN)", width, c1, R);
            if (gameStarted) {            printBoxedLine(" &e\u2726 &fWebsite: &bmlnplus.hu", width, c1, R);

                printBoxedLine(" &e\u2726 &fStatus: &aGame started, effects active! &7(v" + getPluginMeta().getVersion() + ")", width, c1, R);
            } else {            printBoxedLine(" &e\u2726 &fWebsite: &bmlnplus.hu", width, c1, R);

                printBoxedLine(" &e\u2726 &fStatus: &cWaiting - Game is not started yet! (/e start)", width, c1, R);
            }
        }
        if (updateStatus != null && !updateStatus.trim().isEmpty()) {
            printBoxedLine(" &e\u2726 &fUpdate: " + updateStatus, width, c1, R);
        }
        org.bukkit.Bukkit.getConsoleSender().sendMessage(c1 + "\u2570" + "\u2500".repeat(width + 2) + "\u256f" + R);
        org.bukkit.Bukkit.getConsoleSender().sendMessage("");
    }

            private void printBoxedLine(String text, int width, String borderColorCode, String resetCode) {
        String coloredText = text.replaceAll("&([0-9a-fk-orA-FK-ORxX])", "\u00A7$1");
        int visibleLength = stripColors(coloredText).length();
        int padding = Math.max(0, width - visibleLength);
        String line = borderColorCode + "\u2502 " + resetCode + coloredText + " ".repeat(padding) + borderColorCode + " \u2502" + resetCode;
        org.bukkit.Bukkit.getConsoleSender().sendMessage(line);
    }

        private String stripColors(String text) {
        if (text == null) return "";
        String stripped = text.replaceAll("[\u00A7&][0-9a-fk-orA-FK-ORxX]", "");
        stripped = stripped.replaceAll("\033\\[[0-9;]*[mK]", "");
        return stripped;
    }

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initialize();
        this.messageUtils = new MessageUtils(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.customItems = new CustomItems(this);
        this.effectAbilityManager = new EffectAbilityManager(this);
        this.rollAnimationManager = new RollAnimationManager(this);
        this.itemAbilityManager = new ItemAbilityManager(this);
        this.guiManager = new GUIManager(this);
        this.actionBarManager = new ActionBarManager(this);
        this.dashManager = new DashManager(this);
        this.updateManager = new UpdateManager(this);
        this.updateManager.checkForUpdates();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new MiningListener(this), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(this), this);
        getServer().getPluginManager().registerEvents(new LootListener(this), this);

        PluginCommand effectCommand = getCommand("e");
        if (effectCommand != null) {
            EffectCommand executor = new EffectCommand(this);
            effectCommand.setExecutor(executor);
            effectCommand.setTabCompleter(new EffectTabCompleter(this));
        }

        customItems.registerRecipes();

        actionBarManager.startTask();

        // Auto-save task every 5 minutes (6000L ticks) for seamless data protection
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (playerDataManager != null) {
                playerDataManager.saveAll();
            }
        }, 6000L, 6000L);

        this.gameStarted = configManager.isGameStarted();

        // Safety fallback to print banner after 1.5 seconds if update check hangs
        getServer().getScheduler().runTaskLater(this, () -> {
            String lang = configManager != null ? configManager.getConfig().getString("language", "en") : "en";
            boolean isHu = "hu".equalsIgnoreCase(lang);
            String timeoutMsg = isHu ? "\u00A7c[!] A friss\u00EDt\u00E9s ellen\u0151rz\u00E9se id\u0151t\u00FAll\u00E9p\u00E9s miatt sikertelen." : "\u00A7c[!] Update check timed out or failed.";
            printEnableBanner(timeoutMsg);
        }, 30L);
    }

    @Override
    public void onDisable() {
        if (guiManager != null) {
            guiManager.restoreAll();
        }

        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        if (actionBarManager != null) {
            actionBarManager.stopTask();
        }

        String lang = configManager != null ? configManager.getConfig().getString("language", "en") : "en";
        boolean isHu = "hu".equalsIgnoreCase(lang);
        getLogger().info(isHu ? "\u00A7c\u25cf EffectSMP Plugin kikapcsolva!" : "\u00A7c\u25cf EffectSMP Plugin disabled!");
    }

    public void reload() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }

        configManager.reload();
        messageUtils.reload();
        customItems.registerRecipes();

        if (isGameStarted() && effectAbilityManager != null) {
            for (org.bukkit.entity.Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
                if (online == null) continue;
                mlnplus.hu.effectsmp.data.PlayerData pdata = playerDataManager.getPlayerData(online.getUniqueId());
                if (pdata != null && pdata.isPassiveEnabled() && pdata.getEffectHearts() >= 1) {
                    effectAbilityManager.removePassiveEffect(online);
                    effectAbilityManager.applyPassiveEffect(online);
                }
            }
        }

        String lang = configManager != null ? configManager.getConfig().getString("language", "en") : "en";
        boolean isHu = "hu".equalsIgnoreCase(lang);
        getLogger().info(isHu ? "\u00A7aEffectSMP konfiguráció újratöltve és játékos adatok elmentve!" : "\u00A7aEffectSMP Config reloaded and player data saved!");
    }

    public static Effectsmp getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public EffectAbilityManager getEffectAbilityManager() {
        return effectAbilityManager;
    }

    public ItemAbilityManager getItemAbilityManager() {
        return itemAbilityManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public CustomItems getCustomItems() {
        return customItems;
    }

    public ActionBarManager getActionBarManager() {
        return actionBarManager;
    }

    public MessageUtils getMessageUtils() {
        return messageUtils;
    }

    public RollAnimationManager getRollAnimationManager() {
        return rollAnimationManager;
    }

    public DashManager getDashManager() {
        return dashManager;
    }

    public UpdateManager getUpdateManager() {
        return updateManager;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean started) {
        this.gameStarted = started;
        configManager.setGameStarted(started);
    }

}
