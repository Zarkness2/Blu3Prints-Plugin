package io.github.bl3rune.blu3printPlugin.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin;
import io.github.bl3rune.blu3printPlugin.data.Blu3printData;
import io.github.bl3rune.blu3printPlugin.data.CapturedBlu3printData;
import io.github.bl3rune.blu3printPlugin.data.ImportedBlu3printData;
import io.github.bl3rune.blu3printPlugin.items.Blu3printItem;
import io.github.bl3rune.blu3printPlugin.utils.InventoryUtils;

public class BookListener implements Listener {

    private Blu3PrintPlugin instance;

    public BookListener () {
        this.instance = Blu3PrintPlugin.getBlu3PrintPlugin();
    }

    @EventHandler
    public void onBookFinished(PlayerEditBookEvent event) {

        Player player = event.getPlayer();
        if (player == null || !event.isSigning()) {
            return;
        }

        ItemStack book = InventoryUtils.getHeldBlu3print(player, true);
        if (book == null)  {
            return;
        }

        String uuid = UUID.randomUUID().toString();
        ItemMeta itemMeta = book.getItemMeta();
        BookMeta bookMeta = event.getNewBookMeta();

        Blu3printItem finishedBook = null;
        Blu3printData blu3printData = null;
        event.setCancelled(true);

        if (bookMeta.hasPages() && !bookMeta.getPages().isEmpty() && bookMeta.getPages().stream().anyMatch(p -> !p.isEmpty())) {
            List<String> pages = bookMeta.getPages();
            StringBuilder builder = new StringBuilder();
            for (String page : pages) {
                builder.append(page);
            }
            String cacheKey = instance.getKeyFromEncoding(builder.toString());
            if (cacheKey != null) {
                uuid = cacheKey;
            }
            finishedBook = Blu3printItem.getFinishedBlu3print(uuid, player, bookMeta.getTitle(), true);
            blu3printData = new ImportedBlu3printData(player, builder.toString());
            if (blu3printData.getPosition() == null) return;
            bookMeta.setPages(new ArrayList<>());
            book.setItemMeta(bookMeta);
        } else {
            String playerUuid = player.getUniqueId().toString();
            String pos1 = extractLocation(itemMeta,  "location1-" + playerUuid);
            String pos2 = extractLocation(itemMeta,  "location2-" + playerUuid);
            finishedBook = Blu3printItem.getFinishedBlu3print(uuid, player, bookMeta.getTitle(), false);
            blu3printData = new CapturedBlu3printData(player, pos1, pos2);
            if (blu3printData.getPosition() == null) return;
        }
        instance.saveOrUpdateCachedBlu3print(uuid, blu3printData);
        finishedBook.setAmount(1);
        player.getInventory().addItem(finishedBook);
        player.sendMessage("You have finished your blu3print!");
        instance.getLogger().info("Finished blu3print: " + blu3printData.getEncodedString());
    }

    private String extractLocation(ItemMeta meta, String key) {
        NamespacedKey nsKey = new NamespacedKey(instance, key);
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        return dataContainer.getOrDefault(nsKey, PersistentDataType.STRING, null);

    }

}
