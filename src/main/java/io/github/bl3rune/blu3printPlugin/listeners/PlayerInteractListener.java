package io.github.bl3rune.blu3printPlugin.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin;
import io.github.bl3rune.blu3printPlugin.data.Blu3printData;
import io.github.bl3rune.blu3printPlugin.enums.CommandType;
import io.github.bl3rune.blu3printPlugin.items.Blu3printItem;

public class PlayerInteractListener implements Listener {

    private static final List<Material> IGNORE_BLOCKS = List.of(Material.LECTERN);
    private static Map<String, Long> lastInteractionPerPlayer = new HashMap<>();
    private static Long cooldown;
    private Blu3PrintPlugin instance;

    public PlayerInteractListener () {
        instance = Blu3PrintPlugin.getBlu3PrintPlugin();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Action action = event.getAction();

        // Exit on invalid action
        if (!isLeftClickBlockEvent(action) && !isRightClickBlockEvent(action)) {
            return;
        }
        
        ItemStack item = player.getInventory().getItemInMainHand();

        // Exit if item is not a Blu3print
        if (!Blu3printItem.isBlu3print(item)) {
            return;
        }

        if (cooldown == null) {
            try {
                cooldown = Blu3PrintPlugin.getBlu3PrintPlugin().getConfig().getLong("blu3print.cooldown");
            } catch (Exception e) {
                cooldown = Long.valueOf(-1);
            }
        }

        String playerKey = player.getUniqueId().toString();
        long currentTime = System.currentTimeMillis();
        if (lastInteractionPerPlayer.getOrDefault(playerKey, currentTime - cooldown) + cooldown > currentTime) {
            return; // Whoa, Slow Down Maurice!
        }
        lastInteractionPerPlayer.put(playerKey, currentTime);

        Block block = event.getClickedBlock();

        if (block != null) {
            if (block.getType().equals(Material.CARTOGRAPHY_TABLE)) {
                if (isLeftClickBlockEvent(action)) {
                    return;
                } // open menu on right click
                event.setCancelled(true);
                player.performCommand(CommandType.BLU3PRINT.getFullCommandName());
                return;
            } else if (IGNORE_BLOCKS.contains(block.getType())) {
                return; // allow interaction with ignored blocks
            }
        }

        if (Blu3printItem.isBlu3print(item, true)) {
            event.setCancelled(true);
            if (isLeftClickBlockEvent(action)) {
                firstBlockSelected(player, block, item);
            } else if (isRightClickBlockEvent(action)) {
                secondBlockSelected(player, block, item);
            }
        } else if (Blu3printItem.isBlu3print(item, false)) {
            if (isLeftClickBlockEvent(action)) {
                event.setCancelled(true);
                placeBlu3print(player, block, item);
            } else if (isRightClickBlockEvent(action)) {
                event.setCancelled(true);
                explainBlu3print(player, item);
            }
        }
    }

    private boolean isLeftClickBlockEvent(Action action) {
       return action.name().equals(Action.LEFT_CLICK_BLOCK.name());
    }

    private boolean isRightClickBlockEvent(Action action) {
        return action.name().equals(Action.RIGHT_CLICK_BLOCK.name());
    }

    private String locationStringFormat(Location location) {
        return String.format("%s:%d:%d:%d", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private void firstBlockSelected(Player player, Block block, ItemStack item) {
        Location location = block.getLocation();
        player.sendMessage("First block selected " + locationStringFormat(location));
        item = persistDataKey(item, "location1-" + player.getUniqueId().toString(), locationStringFormat(location));
    }

    private void secondBlockSelected(Player player, Block block, ItemStack item) {
        if (player.isSneaking()) {
            return; // allow them to read the book
        }
        player.closeInventory();
        Location location = block.getLocation();
        player.sendMessage("Second block selected " + locationStringFormat(location));
        item = persistDataKey(item, "location2-" + player.getUniqueId().toString(), locationStringFormat(location));
    }

    private void placeBlu3print(Player player, Block block, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore.size() < 2) {
            player.sendMessage(ChatColor.RED + "Blu3print missing ID");
            return;
        }
        Blu3printData blu3printItem = instance.getBlu3printFrpmCache(lore.get(1));
        if (blu3printItem == null) {
            player.sendMessage(ChatColor.RED + "Blu3print ID missing from cache");
            return;
        }

        Location startLocation = block.getLocation();
        blu3printItem.placeBlocks(player, startLocation);
    }

    private void explainBlu3print(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore.size() < 2) {
            player.sendMessage(ChatColor.RED + "Blu3print missing ID");
            return;
        }
        Blu3printData blu3printItem = instance.getBlu3printFrpmCache(lore.get(1));
        if (blu3printItem == null) {
            player.sendMessage(ChatColor.RED + "Blu3print ID missing from cache");
            return;
        }
        player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + meta.getDisplayName());
        player.sendMessage(blu3printItem.toString());
    }

    private ItemStack persistDataKey(ItemStack item, String key, String message) {
        ItemMeta meta = item.getItemMeta();
        NamespacedKey nsKey = new NamespacedKey(instance, key);
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        dataContainer.set(nsKey, PersistentDataType.STRING, message);
        item.setItemMeta(meta);
        return item;
    }

}