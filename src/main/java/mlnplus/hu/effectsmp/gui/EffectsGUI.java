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
        Inventory inv = Bukkit.createInventory(null, 27,
                plugin.getMessageUtils().getMessageComponent("gui-effects-title"));

        ItemStack filler = createFiller();
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        // Normal effects
        EffectType[] normals = EffectType.getNormalEffects();
        int[] normalSlots = {9, 10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < Math.min(normals.length, normalSlots.length); i++) {
            inv.setItem(normalSlots[i], createEffectItem(normals[i]));
        }

        // OP effects
        EffectType[] ops = EffectType.getOPEffects();
        int[] opSlots = {21, 22, 23};
        for (int i = 0; i < Math.min(ops.length, opSlots.length); i++) {
            inv.setItem(opSlots[i], createEffectItem(ops[i]));
        }

        inv.setItem(26, createBackItem());

        return inv;
    }

    private ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(getNoItalic("gui-back-name"));
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
        if (slot == 26) {
            player.closeInventory();
        }
    }
}
