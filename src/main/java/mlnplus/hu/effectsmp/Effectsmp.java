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

        cleanupPaperRegistry(getName());

        // --- CLASS LOADER CLEANUP ---
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            java.lang.reflect.Field clGroupField = null;
            Class<?> clClass = cl.getClass();
            while (clClass != null) {
                try {
                    clGroupField = clClass.getDeclaredField("classLoaderGroup");
                    break;
                } catch (NoSuchFieldException e) {
                    clClass = clClass.getSuperclass();
                }
            }
            if (clGroupField != null) {
                clGroupField.setAccessible(true);
                Object classLoaderGroup = clGroupField.get(cl);
                if (classLoaderGroup != null) {
                    if (classLoaderGroup.getClass().getName().contains("LockingClassLoaderGroup")) {
                        java.lang.reflect.Field parentField = classLoaderGroup.getClass().getDeclaredField("parent");
                        parentField.setAccessible(true);
                        classLoaderGroup = parentField.get(classLoaderGroup);
                    }
                    if (classLoaderGroup != null) {
                        Class<?> simpleListClass = classLoaderGroup.getClass();
                        while (simpleListClass != null && !simpleListClass.getName().contains("SimpleListPluginClassLoaderGroup")) {
                            simpleListClass = simpleListClass.getSuperclass();
                        }
                        if (simpleListClass != null) {
                            java.lang.reflect.Field classloadersField = simpleListClass.getDeclaredField("classloaders");
                            classloadersField.setAccessible(true);
                            java.util.List<?> classloaders = (java.util.List<?>) classloadersField.get(classLoaderGroup);
                            if (classloaders != null) {
                                classloaders.remove(cl);
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

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

    private void cleanupPaperRegistry(String pluginName) {
        try {
            org.bukkit.plugin.PluginManager pm = org.bukkit.Bukkit.getPluginManager();
            if (pm.getClass().getName().contains("PaperPluginManagerImpl")) {
                java.util.Set<Object> visited = new java.util.HashSet<>();
                cleanFields(pm, pluginName, visited);
                try {
                    java.lang.reflect.Field instMgrField = pm.getClass().getDeclaredField("instanceManager");
                    instMgrField.setAccessible(true);
                    Object instanceManager = instMgrField.get(pm);
                    if (instanceManager != null) {
                        cleanFields(instanceManager, pluginName, visited);
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        try {
            Class<?> entryPointHandlerClass = Class.forName("io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler");
            java.lang.reflect.Field handlerInstanceField = entryPointHandlerClass.getDeclaredField("INSTANCE");
            handlerInstanceField.setAccessible(true);
            Object handlerInstance = handlerInstanceField.get(null);
            if (handlerInstance != null) {
                java.lang.reflect.Method getStorageMethod = entryPointHandlerClass.getMethod("getStorage");
                java.util.Map<?, ?> storage = (java.util.Map<?, ?>) getStorageMethod.invoke(handlerInstance);
                if (storage != null) {
                    java.util.Set<Object> visited = new java.util.HashSet<>();
                    for (Object providerStorage : storage.values()) {
                        cleanFields(providerStorage, pluginName, visited);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    private void cleanFields(Object obj, String pluginName, java.util.Set<Object> visited) {
        if (obj == null) return;
        if (!visited.add(obj)) return;
        Class<?> clazz = obj.getClass();
        while (clazz != null && !clazz.getName().equals("java.lang.Object")) {
            if (clazz.getName().startsWith("java.lang.") && !(obj instanceof java.util.Collection || obj instanceof java.util.Map)) {
                break;
            }
            for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    Object val = f.get(obj);
                    if (val == null) continue;
                    
                    if (val instanceof java.util.Map<?, ?> map) {
                        java.util.Iterator<?> it = map.entrySet().iterator();
                        while (it.hasNext()) {
                            java.util.Map.Entry<?, ?> entry = (java.util.Map.Entry<?, ?>) it.next();
                            if (entry != null) {
                                boolean remove = false;
                                Object key = entry.getKey();
                                Object valObj = entry.getValue();
                                if (key != null) {
                                    if (key.toString().toLowerCase().contains(pluginName.toLowerCase())) {
                                        remove = true;
                                    }
                                }
                                if (valObj != null) {
                                    if (valObj.toString().toLowerCase().contains(pluginName.toLowerCase())) {
                                        remove = true;
                                    }
                                    if (checkProviderMeta(valObj, pluginName)) {
                                        remove = true;
                                    }
                                }
                                if (remove) {
                                    it.remove();
                                } else {
                                    cleanFields(key, pluginName, visited);
                                    cleanFields(valObj, pluginName, visited);
                                }
                            }
                        }
                    } else if (val instanceof java.util.Set<?> set) {
                        java.util.Iterator<?> it = set.iterator();
                        while (it.hasNext()) {
                            Object element = it.next();
                            if (element != null) {
                                boolean remove = false;
                                if (element.toString().toLowerCase().contains(pluginName.toLowerCase())) {
                                    remove = true;
                                }
                                if (checkProviderMeta(element, pluginName)) {
                                    remove = true;
                                }
                                if (remove) {
                                    it.remove();
                                } else {
                                    cleanFields(element, pluginName, visited);
                                }
                            }
                        }
                    } else if (val instanceof java.util.List<?> list) {
                        java.util.Iterator<?> it = list.iterator();
                        while (it.hasNext()) {
                            Object element = it.next();
                            if (element != null) {
                                boolean remove = false;
                                if (element.toString().toLowerCase().contains(pluginName.toLowerCase())) {
                                    remove = true;
                                }
                                if (checkProviderMeta(element, pluginName)) {
                                    remove = true;
                                }
                                if (remove) {
                                    it.remove();
                                } else {
                                    cleanFields(element, pluginName, visited);
                                }
                            }
                        }
                    } else {
                        if (!f.getType().isPrimitive() && !f.getType().getName().startsWith("java.lang.")) {
                            cleanFields(val, pluginName, visited);
                        }
                    }
                } catch (Throwable ignored) {}
            }
            clazz = clazz.getSuperclass();
        }
    }

    private boolean checkProviderMeta(Object element, String pluginName) {
        if (element == null) return false;
        ClassLoader pluginCL = this.getClass().getClassLoader();
        if (element.getClass().getClassLoader() == pluginCL) {
            return true;
        }
        try {
            java.lang.reflect.Method getMetaMethod = element.getClass().getMethod("getMeta");
            Object meta = getMetaMethod.invoke(element);
            java.lang.reflect.Method getNameMethod = meta.getClass().getMethod("getName");
            String name = (String) getNameMethod.invoke(meta);
            if (name != null && name.equalsIgnoreCase(pluginName)) {
                return true;
            }
        } catch (Throwable ignored) {}
        try {
            java.lang.reflect.Method getDescriptorMethod = element.getClass().getMethod("getDescriptor");
            Object desc = getDescriptorMethod.invoke(element);
            java.lang.reflect.Method getNameMethod = desc.getClass().getMethod("getName");
            String name = (String) getNameMethod.invoke(desc);
            if (name != null && name.equalsIgnoreCase(pluginName)) {
                return true;
            }
        } catch (Throwable ignored) {}
        try {
            java.lang.reflect.Method getSourceMethod = element.getClass().getMethod("getSource");
            Object source = getSourceMethod.invoke(element);
            if (source != null && source.toString().toLowerCase().contains(pluginName.toLowerCase())) {
                return true;
            }
        } catch (Throwable ignored) {}
        try {
            Class<?> clazz = element.getClass();
            while (clazz != null && !clazz.getName().equals("java.lang.Object")) {
                for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
                    try {
                        f.setAccessible(true);
                        Object val = f.get(element);
                        if (val != null) {
                            if (val.getClass().getClassLoader() == pluginCL) {
                                return true;
                            }
                            if (val.toString().toLowerCase().contains(pluginName.toLowerCase())) {
                                String fName = f.getName().toLowerCase();
                                if (fName.contains("name") || fName.contains("id") || fName.contains("provider") || fName.contains("meta")) {
                                    return true;
                                }
                            }
                        }
                    } catch (Throwable ignored) {}
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
