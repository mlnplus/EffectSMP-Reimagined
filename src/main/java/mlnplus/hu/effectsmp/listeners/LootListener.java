package mlnplus.hu.effectsmp.listeners;

import mlnplus.hu.effectsmp.Effectsmp;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("null")
public class LootListener implements Listener {

    private final Effectsmp plugin;

    public LootListener(Effectsmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        if (event.getLootTable() == null)
            return;
        String lootTableKey = event.getLootTable().getKey().toString();
        double chance = 0.0;

        if (lootTableKey.equals("minecraft:chests/ancient_city")) {
            chance = 0.075; // 2.5%
        } else if (lootTableKey.equals("minecraft:chests/end_city_treasure")) {
            chance = 0.075; // 5%
        } else if (lootTableKey.equals("minecraft:chests/woodland_mansion")) {
            chance = 0.075; // 2%
        }

        if (chance > 0.0 && Math.random() < chance) {
            ItemStack shard = plugin.getCustomItems().getItemByName("shard");
            if (shard != null) {
                event.getLoot().add(shard);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.WARDEN) {
            if (Math.random() < 0.15) { // 5% chance
                ItemStack shard = plugin.getCustomItems().getItemByName("shard");
                if (shard != null) {
                    event.getDrops().add(shard);
                }
            }
        }
    }
}
