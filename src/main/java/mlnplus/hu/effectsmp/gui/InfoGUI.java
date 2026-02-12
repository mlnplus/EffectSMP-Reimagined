package mlnplus.hu.effectsmp.gui;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.data.PlayerData;
import mlnplus.hu.effectsmp.effects.EffectType;
import net.kyori.adventure.text.Component;

import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InfoGUI {

        private final Effectsmp plugin;

        private Component getNoItalic(String key) {
                return plugin.getMessageUtils().getMessageComponent(key).decoration(TextDecoration.ITALIC, false);
        }

        private Component parseNoItalic(String message) {
                return plugin.getMessageUtils().parse(message).decoration(TextDecoration.ITALIC, false);
        }

        public InfoGUI(Effectsmp plugin) {
                this.plugin = plugin;
        }

        public Inventory create(Player player) {
                Inventory inv = Bukkit.createInventory(null, 45,
                                plugin.getMessageUtils().getMessageComponent("gui-info-title"));

                PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

                ItemStack filler = createFiller();
                for (int i = 0; i < 45; i++) {
                        inv.setItem(i, filler);
                }

                inv.setItem(10, createEffectItem(data));
                inv.setItem(12, createShardItem(data));
                inv.setItem(14, createHeartsItem(data));
                inv.setItem(16, createKillsItem(data));

                inv.setItem(31, createTrustedHeaderItem(data));

                inv.setItem(40, createBackItem());

                return inv;
        }

        private ItemStack createFiller() {
                ItemStack item = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
                ItemMeta meta = item.getItemMeta();
                meta.displayName(getNoItalic("gui-info-filler-name"));
                item.setItemMeta(meta);
                return item;
        }

        private ItemStack createEffectItem(PlayerData data) {
                ItemStack item = new ItemStack(Material.POTION);
                ItemMeta meta = item.getItemMeta();

                meta.displayName(getNoItalic("gui-info-effect-name"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());

                EffectType effect = data.getEffect();
                if (effect != null) {
                        lore.add(parseNoItalic(
                                        plugin.getMessageUtils().getMessage("gui-info-effect-lore-effect").replace(
                                                        "%effect%",
                                                        effect.getDisplayName())));
                        lore.add(Component.empty());
                        lore.add(getNoItalic(
                                        effect.isOP() ? "gui-info-effect-lore-op" : "gui-info-effect-lore-normal"));
                } else {
                        lore.add(getNoItalic("gui-info-effect-lore-none"));
                }

                meta.lore(lore);
                item.setItemMeta(meta);
                return item;
        }

        private ItemStack createShardItem(PlayerData data) {
                ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
                ItemMeta meta = item.getItemMeta();

                meta.displayName(getNoItalic("gui-info-shard-name"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());

                String key = data.hasEffectShard() ? "gui-info-shard-status-yes" : "gui-info-shard-status-no";
                String status = plugin.getMessageUtils().getMessage(key);

                lore.add(parseNoItalic(
                                plugin.getMessageUtils().getMessage("gui-info-shard-lore-status").replace("%status%",
                                                status)));
                lore.add(Component.empty());
                lore.add(getNoItalic("gui-info-shard-lore-desc"));

                meta.lore(lore);
                item.setItemMeta(meta);
                return item;
        }

        private ItemStack createHeartsItem(PlayerData data) {
                ItemStack item = new ItemStack(Material.NETHER_STAR);
                ItemMeta meta = item.getItemMeta();

                meta.displayName(getNoItalic("gui-info-hearts-name"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(parseNoItalic(
                                plugin.getMessageUtils().getMessage("gui-info-hearts-lore-count").replace("%count%",
                                                String.valueOf(data.getEffectHearts()))));
                lore.add(Component.empty());
                lore.add(getNoItalic("gui-info-hearts-lore-1"));
                lore.add(getNoItalic("gui-info-hearts-lore-2"));

                meta.lore(lore);
                item.setItemMeta(meta);
                return item;
        }

        private ItemStack createKillsItem(PlayerData data) {
                ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
                ItemMeta meta = item.getItemMeta();

                meta.displayName(getNoItalic("gui-info-stats-name"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(parseNoItalic(
                                plugin.getMessageUtils().getMessage("gui-info-stats-lore-kills").replace("%kills%",
                                                String.valueOf(data.getKills()))));
                lore.add(parseNoItalic(
                                plugin.getMessageUtils().getMessage("gui-info-stats-lore-deaths").replace("%deaths%",
                                                String.valueOf(data.getDeaths()))));

                double kd = data.getDeaths() > 0 ? (double) data.getKills() / data.getDeaths() : data.getKills();
                String kdFormatted = String.format("%.2f", kd);
                lore.add(parseNoItalic(
                                plugin.getMessageUtils().getMessage("gui-info-stats-lore-kd").replace("%kd%",
                                                kdFormatted)));

                meta.lore(lore);
                item.setItemMeta(meta);
                return item;
        }

        private ItemStack createTrustedHeaderItem(PlayerData data) {
                ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta meta = item.getItemMeta();

                List<UUID> mutualTrusted = plugin.getPlayerDataManager().getMutualTrustedPlayers(data.getUuid());

                meta.displayName(getNoItalic("gui-info-trust-name"));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(parseNoItalic(
                                plugin.getMessageUtils().getMessage("gui-info-trust-lore-count").replace("%count%",
                                                String.valueOf(mutualTrusted.size()))));
                lore.add(Component.empty());

                if (mutualTrusted.isEmpty()) {
                        lore.add(getNoItalic("gui-info-trust-none"));
                } else {
                        for (UUID uuid : mutualTrusted) {
                                OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                                String name = op.getName() != null ? op.getName() : "Unknown";
                                lore.add(parseNoItalic(
                                                plugin.getMessageUtils().getMessage("gui-info-trust-entry")
                                                                .replace("%player%", name)));
                        }
                }

                lore.add(Component.empty());
                lore.add(getNoItalic("gui-info-trust-lore-add"));
                lore.add(getNoItalic("gui-info-trust-lore-remove"));

                meta.lore(lore);
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

        public void handleClick(InventoryClickEvent event, Player player) {
                int slot = event.getRawSlot();

                if (slot == 40) {
                        player.closeInventory();
                        plugin.getGuiManager().openMainGUI(player);
                }
        }
}
