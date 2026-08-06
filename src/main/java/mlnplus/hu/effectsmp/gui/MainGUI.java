package mlnplus.hu.effectsmp.gui;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.data.PlayerData;
import mlnplus.hu.effectsmp.effects.EffectType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MainGUI {

    private final Effectsmp plugin;

    private static final int PASSIVE_TOGGLE_SLOT = 11;
    private static final int INFO_SLOT = 13;
    private static final int ACTIVE_ABILITY_SLOT = 15;

    private Component getNoItalic(String key) {
        return plugin.getMessageUtils().getMessageComponent(key).decoration(TextDecoration.ITALIC, false);
    }

    private Component parseNoItalic(String message) {
        return plugin.getMessageUtils().parse(message).decoration(TextDecoration.ITALIC, false);
    }

    public MainGUI(Effectsmp plugin) {
        this.plugin = plugin;
    }

    public Inventory create(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
                plugin.getMessageUtils().getMessageComponent("gui-main-title"));

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        EffectType effect = data.getEffect();

        // Dark modern border pattern (Purple & Magenta glass)
        ItemStack purpleGlass = createFiller(Material.PURPLE_STAINED_GLASS_PANE);
        ItemStack magentaGlass = createFiller(Material.MAGENTA_STAINED_GLASS_PANE);
        ItemStack darkGlass = createFiller(Material.BLACK_STAINED_GLASS_PANE);

        for (int i = 0; i < 27; i++) {
            if (i == 0 || i == 8 || i == 18 || i == 26) {
                inv.setItem(i, magentaGlass);
            } else if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, purpleGlass);
            } else {
                inv.setItem(i, darkGlass);
            }
        }

        inv.setItem(PASSIVE_TOGGLE_SLOT, createPassiveToggleItem(data, effect));
        inv.setItem(INFO_SLOT, createInfoItem(player, data));
        inv.setItem(ACTIVE_ABILITY_SLOT, createActiveAbilityItem(data, effect));

        return inv;
    }

    private ItemStack createFiller(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPassiveToggleItem(PlayerData data, EffectType effect) {
        Material material = data.isPassiveEnabled() ? Material.LIME_DYE : Material.GRAY_DYE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String statusKey = data.isPassiveEnabled() ? "gui-passive-on" : "gui-passive-off";
        String status = plugin.getMessageUtils().getMessage(statusKey);

        meta.displayName(getNoItalic("gui-passive-name"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        if (effect != null) {
            lore.add(parseNoItalic(
                    plugin.getMessageUtils().getMessage("gui-passive-lore-effect").replace("%effect%",
                            effect.getDisplayName())));
            String rarityStr = plugin.getMessageUtils().getMessage("rarity-" + effect.getRarity().getKey());
            lore.add(parseNoItalic(
                    plugin.getMessageUtils().getMessage("gui-effects-rarity-format").replace("%rarity%", rarityStr)));
        } else {
            lore.add(getNoItalic("gui-passive-lore-none"));
        }

        lore.add(Component.empty());
        lore.add(parseNoItalic(
                plugin.getMessageUtils().getMessage("gui-passive-lore-status").replace("%status%", status)));
        lore.add(Component.empty());

        if (data.getEffectHearts() >= 1) {
            lore.add(getNoItalic("gui-passive-lore-click"));
        } else {
            lore.add(getNoItalic("gui-passive-lore-locked"));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createActiveAbilityItem(PlayerData data, EffectType effect) {
        boolean available = data.getEffectHearts() >= 3 && !data.isAbilityOnCooldown();
        Material material = available ? Material.NETHER_STAR : Material.GUNPOWDER;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(getNoItalic("gui-active-name"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        if (effect != null) {
            lore.add(parseNoItalic(
                    plugin.getMessageUtils().getMessage("gui-passive-lore-effect").replace("%effect%",
                            effect.getDisplayName())));
            String rarityStr = plugin.getMessageUtils().getMessage("rarity-" + effect.getRarity().getKey());
            lore.add(parseNoItalic(
                    plugin.getMessageUtils().getMessage("gui-effects-rarity-format").replace("%rarity%", rarityStr)));
            lore.add(Component.empty());
            lore.add(getNoItalic("gui-active-lore-desc-header"));
            lore.addAll(getAbilityDescription(effect));
            lore.add(Component.empty());
            lore.add(parseNoItalic(
                    plugin.getMessageUtils().getMessage("gui-active-lore-cooldown").replace("%time%",
                            formatCooldown(data.getEffectiveCooldownSeconds()))));
        } else {
            lore.add(getNoItalic("gui-passive-lore-none"));
        }

        lore.add(Component.empty());

        if (data.getEffectHearts() < 3) {
            lore.add(getNoItalic("gui-active-lore-locked"));
        } else if (data.isAbilityOnCooldown()) {
            long remaining = data.getRemainingCooldown();
            lore.add(parseNoItalic(
                    plugin.getMessageUtils().getMessage("gui-active-lore-cooldown-active").replace("%time%",
                            plugin.getMessageUtils().formatTime(remaining))));
        } else {
            lore.add(getNoItalic("gui-active-lore-click"));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfoItem(Player player, PlayerData data) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof org.bukkit.inventory.meta.SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }

        meta.displayName(getNoItalic("gui-info-name"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(getNoItalic("gui-info-lore-1"));
        lore.add(getNoItalic("gui-info-lore-2"));
        lore.add(Component.empty());
        lore.add(getNoItalic("gui-info-lore-click"));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private List<Component> getAbilityDescription(EffectType effect) {
        List<Component> desc = new ArrayList<>();
        String key = switch (effect) {
            case INVISIBILITY -> "active-desc-invisibility";
            case HERO_OF_VILLAGE -> "active-desc-hero-of-village";
            case HASTE -> "active-desc-haste";
            case FIRE_RESISTANCE -> "active-desc-fire-resistance";
            case SPEED -> "active-desc-speed";
            case DOLPHIN_GRACE -> "active-desc-dolphin-grace";
            case HEALTH_BOOST -> "active-desc-health-boost";
            case WIND_CHARGED -> "active-desc-wind-charged";
            case RESISTANCE -> "active-desc-resistance";
            case STRENGTH -> "active-desc-strength";
            case REGENERATION -> "active-desc-regeneration";
        };
        desc.add(getNoItalic(key));
        return desc;
    }

    private String formatCooldown(int seconds) {
        String lang = plugin.getConfigManager().getConfig().getString("language", "en");
        boolean isHu = "hu".equalsIgnoreCase(lang);
        if (seconds >= 60) {
            int minutes = seconds / 60;
            int remaining = seconds % 60;
            if (isHu) {
                if (remaining > 0) {
                    return minutes + " perc " + remaining + " mp";
                }
                return minutes + " perc";
            } else {
                if (remaining > 0) {
                    return minutes + " min " + remaining + " sec";
                }
                return minutes + (minutes == 1 ? " minute" : " minutes");
            }
        }
        return seconds + (isHu ? " mp" : " sec");
    }

    public void handleClick(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        switch (slot) {
            case PASSIVE_TOGGLE_SLOT -> {
                if (data.getEffectHearts() >= 1) {
                    plugin.getEffectAbilityManager().togglePassive(player);
                    player.closeInventory();
                } else {
                    plugin.getMessageUtils().sendMessage(player, "gui-error-passive-need-heart");
                }
            }
            case ACTIVE_ABILITY_SLOT -> {
                if (data.canUseAbility()) {
                    plugin.getEffectAbilityManager().activateAbility(player);
                    player.closeInventory();
                } else if (data.getEffectHearts() < 3) {
                    plugin.getMessageUtils().sendMessage(player, "gui-error-active-need-heart");
                } else if (data.isAbilityOnCooldown()) {
                    long remaining = data.getRemainingCooldown();
                    plugin.getMessageUtils().sendMessage(player, "gui-error-active-cooldown",
                            "%time%", plugin.getMessageUtils().formatTime(remaining));
                }
            }
            case INFO_SLOT -> {
                player.closeInventory();
                plugin.getGuiManager().openInfoGUI(player);
            }
        }
    }
}
