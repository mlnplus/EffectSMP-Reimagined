package mlnplus.hu.effectsmp;

import mlnplus.hu.effectsmp.commands.EffectCommand;
import mlnplus.hu.effectsmp.commands.EffectTabCompleter;
import mlnplus.hu.effectsmp.config.ConfigManager;
import mlnplus.hu.effectsmp.data.PlayerDataManager;
import mlnplus.hu.effectsmp.effects.EffectAbilityManager;
import mlnplus.hu.effectsmp.effects.RollAnimationManager;
import mlnplus.hu.effectsmp.gui.GUIManager;
import mlnplus.hu.effectsmp.items.CustomItems;
import mlnplus.hu.effectsmp.items.ItemAbilityManager;
import mlnplus.hu.effectsmp.listeners.*;
import mlnplus.hu.effectsmp.effects.DashManager;
import mlnplus.hu.effectsmp.utils.ActionBarManager;
import mlnplus.hu.effectsmp.utils.MessageUtils;
import mlnplus.hu.effectsmp.updater.UpdateManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Effectsmp extends JavaPlugin {

    private static Effectsmp instance;

    private ConfigManager configManager;
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

        org.bukkit.Bukkit.getConsoleSender().sendMessage("§d=================================================================================");
        org.bukkit.Bukkit.getConsoleSender().sendMessage("§d  ███████╗███████╗███████╗███████╗ ██████╗████████╗   ███████╗███╗   ███╗██████╗ ");
        org.bukkit.Bukkit.getConsoleSender().sendMessage("§d  ██╔════╝██╔════╝██╔════╝██╔════╝██╔════╝╚══██╔══╝   ██╔════╝████╗ ████║██╔══██╗");
        org.bukkit.Bukkit.getConsoleSender().sendMessage("§5  █████╗  █████╗  █████╗  █████╗  ██║        ██║      ███████╗██╔████╔██║██████╔╝");
        org.bukkit.Bukkit.getConsoleSender().sendMessage("§5  ██╔══╝  ██╔══╝  ██╔══╝  ██╔══╝  ██║        ██║      ╚════██║██║╚██╔╝██║██╔═══╝ ");
        org.bukkit.Bukkit.getConsoleSender().sendMessage("§6  ███████╗██║     ██║     ███████╗╚██████╗   ██║      ███████║██║ ╚═╝ ██║██║     ");
        org.bukkit.Bukkit.getConsoleSender().sendMessage("§6  ╚══════╝╚═╝     ╚═╝     ╚══════╝ ╚═════╝   ╚═╝      ╚══════╝╚═╝     ╚═╝╚═╝     ");
        org.bukkit.Bukkit.getConsoleSender().sendMessage("§5                                                                               ");
        org.bukkit.Bukkit.getConsoleSender().sendMessage("§d                              EffectSMP Reimagined                             ");
        org.bukkit.Bukkit.getConsoleSender().sendMessage("§e                        [★] Plugin successfully enabled! [★]                      ");
        if (gameStarted) {
            org.bukkit.Bukkit.getConsoleSender().sendMessage("§a                        [✔] Játék elindítva, effektek aktívak!                   ");
        } else {
            org.bukkit.Bukkit.getConsoleSender().sendMessage("§c                        [⌛] Játék még nincs elindítva! (/e start)                ");
        }
        if (updateStatus != null) {
            org.bukkit.Bukkit.getConsoleSender().sendMessage(updateStatus);
        }
        org.bukkit.Bukkit.getConsoleSender().sendMessage("§d=================================================================================");
    }

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
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

        this.gameStarted = configManager.isGameStarted();

        // Safety fallback to print banner after 1.5 seconds if update check hangs
        getServer().getScheduler().runTaskLater(this, () -> {
            printEnableBanner("§c                        [!] Update check timed out or failed.                    ");
        }, 30L);
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }

        if (actionBarManager != null) {
            actionBarManager.stopTask();
        }

        getLogger().info("§cEffectSMP Plugin disabled!");

        // Debug utility to log active non-daemon threads that could hang the JVM on shutdown
        try {
            getLogger().info("--- Active non-daemon threads check ---");
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                if (!t.isDaemon() && t.isAlive() && t != Thread.currentThread()) {
                    getLogger().info("Thread: " + t.getName() + " | State: " + t.getState());
                    StackTraceElement[] trace = t.getStackTrace();
                    for (int i = 0; i < Math.min(3, trace.length); i++) {
                        getLogger().info("   at " + trace[i]);
                    }
                }
            }
            getLogger().info("--------------------------------------");
        } catch (Exception ignored) {
        }
    }

    public void reload() {
        configManager.reload();
        messageUtils.reload();
        customItems.registerRecipes();
        getLogger().info("§aEffectSMP Config reloaded!");
    }

    public static Effectsmp getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
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
