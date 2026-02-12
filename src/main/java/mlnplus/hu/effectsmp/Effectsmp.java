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

    private boolean gameStarted = false;

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

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new MiningListener(this), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(this), this);

        PluginCommand effectCommand = getCommand("e");
        if (effectCommand != null) {
            EffectCommand executor = new EffectCommand(this);
            effectCommand.setExecutor(executor);
            effectCommand.setTabCompleter(new EffectTabCompleter(this));
        }

        customItems.registerRecipes();

        actionBarManager.startTask();

        this.gameStarted = configManager.isGameStarted();

        getLogger().info("§aEffectSMP Plugin enabled!");
        if (gameStarted) {
            getLogger().info("§eJáték már elindítva, effectek aktívak!");
        }
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

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean started) {
        this.gameStarted = started;
        configManager.setGameStarted(started);
    }
}
