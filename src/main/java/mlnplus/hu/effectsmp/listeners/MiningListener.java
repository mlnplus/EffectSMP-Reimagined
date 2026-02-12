package mlnplus.hu.effectsmp.listeners;

import mlnplus.hu.effectsmp.Effectsmp;
import mlnplus.hu.effectsmp.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class MiningListener implements Listener {

    private final Effectsmp plugin;
    private final Set<Block> processingBlocks = new HashSet<>();

    public MiningListener(Effectsmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (processingBlocks.contains(block))
            return;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (!data.isHaste3x3Active())
            return;

        BlockFace face = getPlayerFacingFace(player);

        Set<Block> blocksToBreak = get3x3Blocks(block, face);
        ItemStack tool = player.getInventory().getItemInMainHand();

        for (Block b : blocksToBreak) {
            if (b.equals(block))
                continue;

            Material type = b.getType();
            if (type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR)
                continue;

            if (isUnbreakable(type))
                continue;

            processingBlocks.add(b);

            b.breakNaturally(tool);

            processingBlocks.remove(b);
        }
    }

    private boolean isUnbreakable(Material material) {
        return material == Material.BEDROCK ||
                material == Material.END_PORTAL_FRAME ||
                material == Material.BARRIER ||
                material == Material.COMMAND_BLOCK ||
                material == Material.CHAIN_COMMAND_BLOCK ||
                material == Material.REPEATING_COMMAND_BLOCK ||
                material == Material.STRUCTURE_BLOCK ||
                material == Material.JIGSAW ||
                material == Material.LIGHT ||
                material == Material.REINFORCED_DEEPSLATE ||
                material == Material.END_PORTAL ||
                material == Material.NETHER_PORTAL;
    }

    private BlockFace getPlayerFacingFace(Player player) {
        float pitch = player.getLocation().getPitch();
        float yaw = player.getLocation().getYaw();

        if (pitch < -45)
            return BlockFace.DOWN;
        if (pitch > 45)
            return BlockFace.UP;

        yaw = (yaw % 360 + 360) % 360;
        if (yaw >= 315 || yaw < 45)
            return BlockFace.SOUTH;
        if (yaw >= 45 && yaw < 135)
            return BlockFace.WEST;
        if (yaw >= 135 && yaw < 225)
            return BlockFace.NORTH;
        return BlockFace.EAST;
    }

    private Set<Block> get3x3Blocks(Block center, BlockFace face) {
        Set<Block> blocks = new HashSet<>();
        blocks.add(center);

        switch (face) {
            case UP, DOWN -> {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        blocks.add(center.getRelative(x, 0, z));
                    }
                }
            }
            case NORTH, SOUTH -> {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        blocks.add(center.getRelative(x, y, 0));
                    }
                }
            }
            case EAST, WEST -> {
                for (int z = -1; z <= 1; z++) {
                    for (int y = -1; y <= 1; y++) {
                        blocks.add(center.getRelative(0, y, z));
                    }
                }
            }
            default -> {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        blocks.add(center.getRelative(x, 0, z));
                    }
                }
            }
        }

        return blocks;
    }
}
