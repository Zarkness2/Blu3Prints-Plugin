package io.github.bl3rune.blu3printPlugin.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin;
import io.github.bl3rune.blu3printPlugin.data.Blu3printData;
import io.github.bl3rune.blu3printPlugin.utils.InventoryUtils;

public class ExportCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack item = InventoryUtils.getHeldBlu3print(player, false);
            if (InventoryUtils.itemIsBlank(item)) {
                sender.sendMessage("You must be holding a blu3print to export it.");
                return true;
            }

            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            if  (lore.size() < 2) {
                sender.sendMessage("Lore is missing from blu3print to export it.");
                return true;
            }

            sender.sendMessage(ChatColor.BLUE + lore.get(0));
            Blu3printData data = Blu3PrintPlugin.getBlu3PrintPlugin().getBlu3printFrpmCache(lore.get(1));
            sender.sendMessage(ChatColor.GRAY + data.getEncodedString());
            
        }
        return true;
    }

}
