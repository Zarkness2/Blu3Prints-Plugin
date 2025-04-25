package io.github.bl3rune.blu3printPlugin.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import io.github.bl3rune.blu3printPlugin.config.Blu3printConfiguration;
import io.github.bl3rune.blu3printPlugin.data.Blu3printData;
import io.github.bl3rune.blu3printPlugin.enums.CommandType;
import io.github.bl3rune.blu3printPlugin.items.Blu3printItem;
import io.github.bl3rune.blu3printPlugin.items.Hologram;

import static io.github.bl3rune.blu3printPlugin.utils.LocationUtils.locationStringFormat;

public class PlayerInteractListener implements Listener {

    private static final List<Material> IGNORE_BLOCKS = List.of(Material.LECTERN);
    private static Map<String, Long> lastInteractionPerPlayer = new HashMap<>();
    private static Map<String, List<String>> blockIgnoreListPerPlayer = new HashMap<>();
    private Blu3PrintPlugin instance;

    public static List<String> getIgnoreList(Player player) {
        return blockIgnoreListPerPlayer.getOrDefault(player.getUniqueId().toString(), new ArrayList<>());
    }

    public PlayerInteractListener () {
        instance = Blu3PrintPlugin.getBlu3PrintPlugin();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Action action = event.getAction();
        
        ItemStack item = player.getInventory().getItemInMainHand();

        // Exit if item is not a Blu3print
        if (!Blu3printItem.isBlu3print(item, null)) {
            return;
        }

        Integer cooldown = Blu3printConfiguration.getCooldown();
        String playerKey = player.getUniqueId().toString();
        long currentTime = System.currentTimeMillis();
        if (lastInteractionPerPlayer.getOrDefault(playerKey, currentTime - cooldown) + cooldown > currentTime) {
            event.setCancelled(true);
            return; // Whoa, Slow Down Maurice!
        }
        lastInteractionPerPlayer.put(playerKey, currentTime);

        Block block = event.getClickedBlock();

        if (block != null) {
            if (block.getType().equals(Material.CARTOGRAPHY_TABLE)) {
                if (actionEquals(action, Action.LEFT_CLICK_BLOCK)) {
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
            if (actionEquals(action, Action.LEFT_CLICK_BLOCK)) {
                firstBlockSelected(player, block, item);
            } else if (actionEquals(action, Action.RIGHT_CLICK_BLOCK)) {
                secondBlockSelected(player, block, item);
            }
        } else if (Blu3printItem.isBlu3print(item, false)) {
            event.setCancelled(true);
            if (actionEquals(action, Action.LEFT_CLICK_BLOCK)) {
                if (player.isSneaking()) {
                    placeBlu3print(player, block, item, true, true);
                } else {
                    placeBlu3print(player, block, item, false, true);
                }
            } else if (actionEquals(action, Action.RIGHT_CLICK_BLOCK)) {
                if (player.isSneaking()) {
                    placeBlu3print(player, block, item, true, false);
                } else {
                    placeHologram(player, block, item);
                }
            } else if (actionEquals(action, Action.RIGHT_CLICK_AIR)) {
                explainBlu3print(player, item);
            }
        }
    }

    private boolean actionEquals(Action action1, Action action2) {
       return action1.name().equals(action2.name());
    }

    private void firstBlockSelected(Player player, Block block, ItemStack item) {
        String playerUUID = player.getUniqueId().toString();
        Location location = block.getLocation();
        String locationString = locationStringFormat(location);
        if (player.isSneaking()) {
            List<String> ignoreList = blockIgnoreListPerPlayer.getOrDefault(playerUUID, new ArrayList<>());
            if (ignoreList.stream().anyMatch(l -> l.equals(locationString))) {
                player.sendMessage(ChatColor.RED + "Block already ignored, removing from ignore list");
                ignoreList = ignoreList.stream().filter(l -> !l.equals(locationString)).collect(Collectors.toList());
            } else {
                player.sendMessage(ChatColor.GREEN + "Adding block to ignore list");
                ignoreList.add(locationString);
            }
            blockIgnoreListPerPlayer.put(playerUUID, ignoreList);
            return;
        }
        player.sendMessage("First block selected " + locationString);
        if (blockIgnoreListPerPlayer.containsKey(playerUUID)) {
            player.sendMessage(ChatColor.GRAY + "Cleared block ignore list");
            blockIgnoreListPerPlayer.remove(playerUUID);
        }
        item = persistDataKey(item, "location1-" + playerUUID, locationString);
    }

    private void secondBlockSelected(Player player, Block block, ItemStack item) {
        String playerUUID = player.getUniqueId().toString();
        if (player.isSneaking()) {
            List<String> ignoreList = blockIgnoreListPerPlayer.getOrDefault(playerUUID, new ArrayList<>());
            if (ignoreList.isEmpty()) {
                player.sendMessage(ChatColor.RED + "No blocks on ignore list");
            } else {
                player.sendMessage("Blocks on ignore list");
                ignoreList.forEach(i -> player.sendMessage(ChatColor.GRAY + " - " + i));
            }
            player.closeInventory();
            return;
        }
        player.closeInventory();
        Location location = block.getLocation();
        player.sendMessage("Second block selected " + locationStringFormat(location));
        if (blockIgnoreListPerPlayer.containsKey(playerUUID)) {
            player.sendMessage(ChatColor.GRAY + "Cleared block ignore list");
            blockIgnoreListPerPlayer.remove(playerUUID);
        }
        item = persistDataKey(item, "location2-" + player.getUniqueId().toString(), locationStringFormat(location));
    }

    private void placeBlu3print(Player player, Block block, ItemStack item, boolean forced, boolean onTop) {
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
        blu3printItem.placeBlocks(player, startLocation, forced, onTop);
    }

    private void placeHologram(Player player, Block block, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        if (lore.size() < 2) {
            player.sendMessage(ChatColor.RED + "Blu3print missing ID");
            return;
        }

        Blu3printData blu3printData = instance.getBlu3printFrpmCache(lore.get(1));
        
        if (blu3printData == null) {
            player.sendMessage(ChatColor.RED + "Blu3print ID missing from cache");
            return;
        }

        if (!player.hasPermission("blu3print.holograms")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to place hologram previews!");
            return;
        }

        Location startLocation = block.getLocation();
        Hologram hologram = new Hologram(startLocation, blu3printData);
        hologram.placeHologram();
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