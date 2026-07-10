package mlnplus.hu.effectsmp.gui;

import mlnplus.hu.effectsmp.Effectsmp;
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

public class EffectsGUI {

    private final Effectsmp plugin;

    public EffectsGUI(Effectsmp plugin) {
        this.plugin = plugin;
    }

    private Component getNoItalic(String key) {
        return plugin.getMessageUtils().getMessageComponent(key).decoration(TextDecoration.ITALIC, false);
    }

    private Component parseNoItalic(String text) {
        return plugin.getMessageUtils().parse(text).decoration(TextDecoration.ITALIC, false);
    }

    public Inventory create(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45,
                plugin.getMessageUtils().getMessageComponent("gui-effects-title"));

        // Build premium background grid
        ItemStack border = createBorderFiller();
        ItemStack innerFiller = createInnerFiller();

        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border);
            } else {
                inv.setItem(i, innerFiller);
            }
        }

        // Title book at slot 4
        inv.setItem(4, createTitleItem());

        // 1. Common row (slots 11, 13, 15)
        inv.setItem(11, createEffectItem(EffectType.HERO_OF_VILLAGE));
        inv.setItem(13, createEffectItem(EffectType.FIRE_RESISTANCE));
        inv.setItem(15, createEffectItem(EffectType.DOLPHIN_GRACE));

        // 2. Rare row (slots 19, 21, 23, 25)
        inv.setItem(19, createEffectItem(EffectType.INVISIBILITY));
        inv.setItem(21, createEffectItem(EffectType.HASTE));
        inv.setItem(23, createEffectItem(EffectType.SPEED));
        inv.setItem(25, createEffectItem(EffectType.WIND_CHARGED));

        // 3. OP row (slots 28, 30, 32, 34)
        inv.setItem(28, createEffectItem(EffectType.HEALTH_BOOST));
        inv.setItem(30, createEffectItem(EffectType.REGENERATION));
        inv.setItem(32, createEffectItem(EffectType.STRENGTH));
        inv.setItem(34, createEffectItem(EffectType.RESISTANCE));

        // Back button on slot 40
        inv.setItem(40, createBackItem());

        return inv;
    }

    private ItemStack createBorderFiller() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInnerFiller() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTitleItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(getNoItalic("gui-effects-info-book-name"));
        List<Component> lore = new ArrayList<>();
        lore.add(getNoItalic("gui-effects-info-book-lore"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(getNoItalic("gui-back-items-name"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createEffectItem(EffectType type) {
        Material mat = switch (type) {
            case INVISIBILITY -> Material.POTION;
            case HERO_OF_VILLAGE -> Material.EMERALD;
            case HASTE -> Material.GOLDEN_PICKAXE;
            case FIRE_RESISTANCE -> Material.BLAZE_POWDER;
            case SPEED -> Material.FEATHER;
            case DOLPHIN_GRACE -> Material.PRISMARINE_SHARD;
            case HEALTH_BOOST -> Material.RED_DYE;
            case WIND_CHARGED -> Material.WIND_CHARGE;
            case RESISTANCE -> Material.SHIELD;
            case STRENGTH -> Material.NETHERITE_SWORD;
            case REGENERATION -> Material.GHAST_TEAR;
        };

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(parseNoItalic(type.getDisplayName()));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        // Rarity
        String rarityStr = plugin.getMessageUtils().getMessage("rarity-" + type.getRarity().getKey());
        String rarityLine = plugin.getMessageUtils().getMessage("gui-effects-rarity-format").replace("%rarity%", rarityStr);
        lore.add(parseNoItalic(rarityLine));
        lore.add(Component.empty());

        // Passive info
        String passTitle = plugin.getMessageUtils().getMessage("gui-effects-passive-title");
        lore.add(parseNoItalic(passTitle));

        String passDesc = "";
        switch (type) {
            case INVISIBILITY -> passDesc = plugin.getMessageUtils().getMessage("gui-effects-pass-invis");
            case HERO_OF_VILLAGE -> passDesc = plugin.getMessageUtils().getMessage("gui-effects-pass-hero");
            case HASTE -> passDesc = plugin.getMessageUtils().getMessage("gui-effects-pass-haste");
            case FIRE_RESISTANCE -> passDesc = plugin.getMessageUtils().getMessage("gui-effects-pass-fire");
            case SPEED -> passDesc = plugin.getMessageUtils().getMessage("gui-effects-pass-speed");
            case DOLPHIN_GRACE -> passDesc = plugin.getMessageUtils().getMessage("gui-effects-pass-dolphin");
            case HEALTH_BOOST -> passDesc = plugin.getMessageUtils().getMessage("gui-effects-pass-health");
            case WIND_CHARGED -> passDesc = plugin.getMessageUtils().getMessage("gui-effects-pass-wind");
            case RESISTANCE -> passDesc = plugin.getMessageUtils().getMessage("gui-effects-pass-resist");
            case STRENGTH -> passDesc = plugin.getMessageUtils().getMessage("gui-effects-pass-strength");
            case REGENERATION -> passDesc = plugin.getMessageUtils().getMessage("gui-effects-pass-regen");
        }
        lore.add(parseNoItalic(passDesc));
        
        lore.add(Component.empty());

        // Active info
        String actTitle = plugin.getMessageUtils().getMessage("gui-effects-active-title");
        lore.add(parseNoItalic(actTitle));

        String activeKey = "active-desc-" + type.name().toLowerCase().replace("_", "-");
        String actDesc = plugin.getMessageUtils().getMessage(activeKey);
        lore.add(parseNoItalic(actDesc));

        String cdStr = plugin.getMessageUtils().formatTime(type.getCooldownSeconds() * 1000L);
        String cdLine = plugin.getMessageUtils().getMessage("gui-effects-cooldown-format").replace("%time%", cdStr);
        lore.add(parseNoItalic(cdLine));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();
        if (slot == 40) {
            player.closeInventory();
            plugin.getGuiManager().openInfoGUI(player);
        }
    }
}
