package mlnplus.hu.effectsmp.items;

import mlnplus.hu.effectsmp.Effectsmp;
import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.format.TextDecoration;

@SuppressWarnings({ "deprecation", "null" })
public class CustomItems {

        private final Effectsmp plugin;

        public static final int CMD_EFFECT_HEART = 1001;
        public static final int CMD_EFFECT_SHARD = 1002;
        public static final int CMD_REROLL = 1003;
        public static final int CMD_OP_REROLL = 1004;
        public static final int CMD_EFFECT_MACE = 1005;
        public static final int CMD_EFFECT_SWORD = 1006;
        public static final int CMD_EFFECT_BOW = 1007;
        public static final int CMD_EFFECT_SCYTHE = 1008;
        public static final int CMD_EFFECT_SPEAR = 1009;

        public final NamespacedKey ITEM_KEY;
        public final NamespacedKey COOLDOWN_KEY;

        public CustomItems(Effectsmp plugin) {
                this.plugin = plugin;
                this.ITEM_KEY = new NamespacedKey(plugin, "custom_item");
                this.COOLDOWN_KEY = new NamespacedKey(plugin, "cooldown_until");
        }

        private Component getNoItalic(String key) {
                return plugin.getMessageUtils().getMessageComponent(key).decoration(TextDecoration.ITALIC, false);
        }

        private Component getNoItalicWithTime(String key, long cooldownMs) {
                String timeStr = plugin.getMessageUtils().formatTime(cooldownMs);
                String message = plugin.getMessageUtils().getMessage(key).replace("%time%", timeStr);
                return plugin.getMessageUtils().parse(message)
                                .decoration(TextDecoration.ITALIC, false);
        }

        public ItemStack createInfiniteWindCharge() {
                ItemStack item = new ItemStack(Material.WIND_CHARGE);
                ItemMeta meta = item.getItemMeta();
                meta.displayName(getNoItalic("item-infinite-wind-charge-name"));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(getNoItalic("item-infinite-wind-charge-lore-1"));
                lore.add(getNoItalic("item-infinite-wind-charge-lore-2"));
                meta.lore(lore);
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "infinite_wind_charge"),
                                PersistentDataType.STRING, "true");
                item.setItemMeta(meta);
                return item;
        }

        public boolean isInfiniteWindCharge(ItemStack item) {
                if (item == null || !item.hasItemMeta())
                        return false;
                return item.getItemMeta().getPersistentDataContainer()
                                .has(new NamespacedKey(plugin, "infinite_wind_charge"), PersistentDataType.STRING);
        }

        public Material getItemMaterial(String itemKey, Material defaultMat) {
                org.bukkit.configuration.file.FileConfiguration config = plugin.getConfigManager().getItemsConfig();
                if (config != null && config.contains(itemKey + ".material")) {
                        String matStr = config.getString(itemKey + ".material");
                        if (matStr != null) {
                                Material mat = Material.matchMaterial(matStr);
                                if (mat != null) return mat;
                        }
                }
                return defaultMat;
        }

        public int getItemCustomModelData(String itemKey, int defaultCmd) {
                org.bukkit.configuration.file.FileConfiguration config = plugin.getConfigManager().getItemsConfig();
                if (config != null && config.contains(itemKey + ".custom_model_data")) {
                        return config.getInt(itemKey + ".custom_model_data", defaultCmd);
                }
                return defaultCmd;
        }

        public ItemStack createEffectHeart() {
                ItemStack item = new ItemStack(getItemMaterial("effect_heart", Material.NETHER_STAR));
                ItemMeta meta = item.getItemMeta();

                meta.displayName(getNoItalic("item-heart-name"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(getNoItalic("item-heart-lore-1"));
                lore.add(getNoItalic("item-heart-lore-2"));
                lore.add(Component.empty());
                lore.add(getNoItalic("item-heart-lore-usage"));
                meta.lore(lore);

                meta.setCustomModelData(getItemCustomModelData("effect_heart", CMD_EFFECT_HEART));
                meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "effect_heart");
                meta.setMaxStackSize(1);

                item.setItemMeta(meta);
                return item;
        }

        public ItemStack createEffectShard() {
                ItemStack item = new ItemStack(getItemMaterial("effect_shard", Material.PRISMARINE_SHARD));
                ItemMeta meta = item.getItemMeta();

                meta.displayName(getNoItalic("item-shard-name"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(getNoItalic("item-shard-lore-1"));
                lore.add(getNoItalic("item-shard-lore-2"));
                lore.add(Component.empty());
                lore.add(getNoItalic("item-shard-lore-usage"));
                meta.lore(lore);

                meta.setCustomModelData(getItemCustomModelData("effect_shard", CMD_EFFECT_SHARD));
                meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "effect_shard");
                meta.setMaxStackSize(1);

                item.setItemMeta(meta);
                return item;
        }

        public ItemStack createReroll() {
                ItemStack item = new ItemStack(getItemMaterial("reroll", Material.ENDER_EYE));
                ItemMeta meta = item.getItemMeta();

                meta.displayName(getNoItalic("item-reroll-name"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(getNoItalic("item-reroll-lore-1"));
                lore.add(getNoItalic("item-reroll-lore-2"));
                lore.add(Component.empty());
                lore.add(getNoItalic("item-reroll-lore-usage"));
                meta.lore(lore);

                meta.setCustomModelData(getItemCustomModelData("reroll", CMD_REROLL));
                meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "reroll");
                meta.setMaxStackSize(1);

                item.setItemMeta(meta);
                return item;
        }

        public ItemStack createOPReroll() {
                ItemStack item = new ItemStack(getItemMaterial("op_reroll", Material.NETHER_STAR));
                ItemMeta meta = item.getItemMeta();

                meta.displayName(getNoItalic("item-op-reroll-name"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(getNoItalic("item-op-reroll-lore-1"));
                lore.add(getNoItalic("item-op-reroll-lore-2"));
                lore.add(Component.empty());
                lore.add(getNoItalic("item-op-reroll-lore-usage"));
                meta.lore(lore);

                meta.setCustomModelData(getItemCustomModelData("op_reroll", CMD_OP_REROLL));
                meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "op_reroll");
                meta.setMaxStackSize(1);

                item.setItemMeta(meta);
                return item;
        }

        public ItemStack createEffectMace() {
                ItemStack item = new ItemStack(getItemMaterial("effect_mace", Material.MACE));
                ItemMeta meta = item.getItemMeta();

                meta.displayName(getNoItalic("item-mace-name"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(getNoItalic("item-mace-lore-ability"));
                lore.add(getNoItalic("item-mace-lore-1"));
                lore.add(getNoItalic("item-mace-lore-2"));
                lore.add(Component.empty());
                lore.add(getNoItalicWithTime("item-mace-lore-cooldown",
                                plugin.getItemAbilityManager().getMaceCooldown()));
                lore.add(Component.empty());
                lore.add(getNoItalic("item-mace-lore-usage"));
                meta.lore(lore);

                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                applyEnchantments(meta, "effect_mace", java.util.Map.of(
                        Enchantment.SHARPNESS, 5,
                        Enchantment.DENSITY, 3,
                        Enchantment.WIND_BURST, 2,
                        Enchantment.BREACH, 4
                ));

                meta.setCustomModelData(getItemCustomModelData("effect_mace", CMD_EFFECT_MACE));
                meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "effect_mace");
                meta.setMaxStackSize(1);

                item.setItemMeta(meta);
                return item;
        }

        public ItemStack createEffectSword() {
                ItemStack item = new ItemStack(getItemMaterial("effect_sword", Material.NETHERITE_SWORD));
                ItemMeta meta = item.getItemMeta();

                meta.displayName(getNoItalic("item-sword-name"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(getNoItalic("item-sword-lore-ability"));
                lore.add(getNoItalic("item-sword-lore-1"));
                lore.add(getNoItalic("item-sword-lore-2"));
                lore.add(Component.empty());
                lore.add(getNoItalicWithTime("item-sword-lore-cooldown",
                                plugin.getItemAbilityManager().getSwordCooldown()));
                lore.add(Component.empty());
                lore.add(getNoItalic("item-sword-lore-usage"));
                meta.lore(lore);

                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                applyEnchantments(meta, "effect_sword", java.util.Map.of(
                        Enchantment.SHARPNESS, 5,
                        Enchantment.SWEEPING_EDGE, 3,
                        Enchantment.FIRE_ASPECT, 2,
                        Enchantment.LOOTING, 3,
                        Enchantment.BANE_OF_ARTHROPODS, 5
                ));

                meta.setCustomModelData(getItemCustomModelData("effect_sword", CMD_EFFECT_SWORD));
                meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "effect_sword");
                meta.setMaxStackSize(1);

                item.setItemMeta(meta);
                return item;
        }

        public ItemStack createEffectBow() {
                ItemStack item = new ItemStack(getItemMaterial("effect_bow", Material.BOW));
                ItemMeta meta = item.getItemMeta();

                meta.displayName(getNoItalic("item-bow-name"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(getNoItalic("item-bow-lore-ability"));
                lore.add(getNoItalic("item-bow-lore-1"));
                lore.add(getNoItalic("item-bow-lore-2"));
                lore.add(Component.empty());
                lore.add(getNoItalic("item-bow-lore-chance"));
                lore.add(Component.empty());
                lore.add(getNoItalic("item-bow-lore-usage"));
                meta.lore(lore);

                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                applyEnchantments(meta, "effect_bow", java.util.Map.of(
                        Enchantment.POWER, 5,
                        Enchantment.FLAME, 1,
                        Enchantment.INFINITY, 1,
                        Enchantment.PUNCH, 2
                ));

                meta.setCustomModelData(getItemCustomModelData("effect_bow", CMD_EFFECT_BOW));
                meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "effect_bow");
                meta.setMaxStackSize(1);

                item.setItemMeta(meta);
                return item;
        }

        public ItemStack createEffectScythe() {
                ItemStack item = new ItemStack(getItemMaterial("effect_scythe", Material.NETHERITE_HOE));
                ItemMeta meta = item.getItemMeta();

                meta.displayName(getNoItalic("item-scythe-name"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(getNoItalic("item-scythe-lore-ability"));
                lore.add(getNoItalic("item-scythe-lore-1"));
                lore.add(getNoItalic("item-scythe-lore-2"));
                lore.add(Component.empty());
                lore.add(getNoItalicWithTime("item-scythe-lore-cooldown",
                                plugin.getItemAbilityManager().getScytheCooldown()));
                lore.add(Component.empty());
                lore.add(getNoItalic("item-scythe-lore-usage"));
                meta.lore(lore);

                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                applyEnchantments(meta, "effect_scythe", java.util.Map.of(
                        Enchantment.SHARPNESS, 10
                ));

                // Add base bonus attack damage attribute modifier to scythe
                double bonusDmg = plugin.getConfigManager().getItemsConfig().getDouble("effect_scythe.base_bonus_damage", 14.0);
                NamespacedKey dmgKey = new NamespacedKey(plugin, "scythe_damage");
                AttributeModifier dmgMod = new AttributeModifier(
                        dmgKey,
                        bonusDmg,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND
                );
                meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, dmgMod);

                meta.setCustomModelData(getItemCustomModelData("effect_scythe", CMD_EFFECT_SCYTHE));
                meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "effect_scythe");
                meta.setMaxStackSize(1);

                item.setItemMeta(meta);
                return item;
        }

        public ItemStack createEffectSpear() {
                ItemStack item = new ItemStack(getItemMaterial("effect_spear", Material.NETHERITE_SPEAR));
                ItemMeta meta = item.getItemMeta();

                meta.displayName(getNoItalic("item-spear-name"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(getNoItalic("item-spear-lore-ability"));
                lore.add(getNoItalic("item-spear-lore-1"));
                lore.add(getNoItalic("item-spear-lore-2"));
                lore.add(Component.empty());
                lore.add(getNoItalicWithTime("item-spear-lore-cooldown",
                                plugin.getItemAbilityManager().getSpearCooldown()));
                lore.add(Component.empty());
                lore.add(getNoItalic("item-spear-lore-usage"));
                meta.lore(lore);

                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                applyEnchantments(meta, "effect_spear", java.util.Map.of(
                        Enchantment.SHARPNESS, 5,
                        Enchantment.IMPALING, 5,
                        Enchantment.LUNGE, 3
                ));

                meta.setCustomModelData(getItemCustomModelData("effect_spear", CMD_EFFECT_SPEAR));
                meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "effect_spear");
                meta.setMaxStackSize(1);

                item.setItemMeta(meta);
                return item;
        }

        private void applyEnchantments(ItemMeta meta, String itemKey, java.util.Map<Enchantment, Integer> defaults) {
                FileConfiguration itemsConfig = plugin.getConfigManager().getItemsConfig();
                ConfigurationSection itemSection = itemsConfig != null ? itemsConfig.getConfigurationSection(itemKey) : null;

                if (itemSection != null && itemSection.contains("enchants")) {
                        ConfigurationSection enchantsSec = itemSection.getConfigurationSection("enchants");
                        if (enchantsSec != null) {
                                for (String keyStr : enchantsSec.getKeys(false)) {
                                        int level = enchantsSec.getInt(keyStr);
                                        Enchantment enchant = parseEnchantment(keyStr);
                                        if (enchant != null) {
                                                if (level > 0) {
                                                        meta.addEnchant(enchant, level, true);
                                                }
                                        } else {
                                                plugin.getLogger().warning("Unknown enchantment '" + keyStr + "' in items.yml for item '" + itemKey + "'");
                                        }
                                }
                        }
                } else if (defaults != null) {
                        for (java.util.Map.Entry<Enchantment, Integer> entry : defaults.entrySet()) {
                                if (entry.getValue() > 0) {
                                        meta.addEnchant(entry.getKey(), entry.getValue(), true);
                                }
                        }
                }
        }

        private Enchantment parseEnchantment(String keyStr) {
                if (keyStr == null || keyStr.trim().isEmpty()) return null;
                String cleanKey = keyStr.trim().toLowerCase();

                try {
                        NamespacedKey nsKey;
                        if (cleanKey.contains(":")) {
                                nsKey = NamespacedKey.fromString(cleanKey);
                        } else {
                                nsKey = NamespacedKey.minecraft(cleanKey);
                        }
                        if (nsKey != null) {
                                Enchantment enchant = org.bukkit.Registry.ENCHANTMENT.get(nsKey);
                                if (enchant != null) return enchant;
                                enchant = Enchantment.getByKey(nsKey);
                                if (enchant != null) return enchant;
                        }
                } catch (Exception ignored) {}

                try {
                        Enchantment enchant = Enchantment.getByName(keyStr.trim().toUpperCase());
                        if (enchant != null) return enchant;
                } catch (Exception ignored) {}

                try {
                        for (Enchantment e : Enchantment.values()) {
                                if (e.getKey().getKey().equalsIgnoreCase(cleanKey) ||
                                    e.getName().equalsIgnoreCase(keyStr.trim()) ||
                                    e.getKey().toString().equalsIgnoreCase(cleanKey)) {
                                        return e;
                                }
                        }
                } catch (Exception ignored) {}

                return null;
        }

        public String getItemType(ItemStack item) {
                if (item == null || !item.hasItemMeta())
                        return null;
                ItemMeta meta = item.getItemMeta();
                return meta.getPersistentDataContainer().get(ITEM_KEY, PersistentDataType.STRING);
        }

        public boolean isCustomItem(ItemStack item) {
                return getItemType(item) != null || isInfiniteWindCharge(item);
        }

        public ItemStack getItemByName(String name) {
                return switch (name.toLowerCase()) {
                        case "effect_heart", "heart" -> createEffectHeart();
                        case "effect_shard", "shard" -> createEffectShard();
                        case "reroll" -> createReroll();
                        case "op_reroll" -> createOPReroll();
                        case "effect_mace", "mace" -> createEffectMace();
                        case "effect_sword", "sword" -> createEffectSword();
                        case "effect_bow", "bow" -> createEffectBow();
                        case "effect_scythe", "scythe" -> createEffectScythe();
                        case "effect_spear", "spear" -> createEffectSpear();
                        case "infinite_wind_charge", "wind_charge" -> createInfiniteWindCharge();
                        default -> null;
                };
        }

        public void registerRecipes() {
                FileConfiguration itemsConfig = plugin.getConfigManager().getItemsConfig();

                registerRecipe("reroll", createReroll(), itemsConfig);
                registerRecipe("op_reroll", createOPReroll(), itemsConfig);
                registerRecipe("effect_mace", createEffectMace(), itemsConfig);
                registerRecipe("effect_sword", createEffectSword(), itemsConfig);
                registerRecipe("effect_bow", createEffectBow(), itemsConfig);
                registerRecipe("effect_scythe", createEffectScythe(), itemsConfig);
                registerRecipe("effect_spear", createEffectSpear(), itemsConfig);
                registerRecipe("effect_heart", createEffectHeart(), itemsConfig);
        }

        private void registerRecipe(String name, ItemStack result, FileConfiguration itemsConfig) {
                ConfigurationSection section = itemsConfig.getConfigurationSection(name);
                if (section == null)
                        return;

                if (!section.getBoolean("enabled", true)) {
                        plugin.getServer().removeRecipe(new NamespacedKey(plugin, name));
                        return;
                }

                NamespacedKey key = new NamespacedKey(plugin, name);

                plugin.getServer().removeRecipe(key);

                ShapedRecipe recipe = new ShapedRecipe(key, result);

                List<String> shape = section.getStringList("shape");
                if (shape.size() == 3) {
                        recipe.shape(shape.get(0), shape.get(1), shape.get(2));
                }

                ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
                if (ingredients != null) {
                        for (String keyChar : ingredients.getKeys(false)) {
                                String materialName = ingredients.getString(keyChar);
                                if (materialName != null) {
                                        ItemStack customItem = getItemByName(materialName.toLowerCase());
                                        if (customItem != null) {
                                                recipe.setIngredient(keyChar.charAt(0),
                                                                new org.bukkit.inventory.RecipeChoice.ExactChoice(
                                                                                customItem));
                                        } else {
                                                Material material = Material.matchMaterial(materialName);
                                                if (material != null) {
                                                        recipe.setIngredient(keyChar.charAt(0), material);
                                                }
                                        }
                                }
                        }
                }

                plugin.getServer().addRecipe(recipe);
        }
}
