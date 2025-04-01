package io.github.bl3rune.blu3printPlugin.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin;
import io.github.bl3rune.blu3printPlugin.data.Blu3printData;
import io.github.bl3rune.blu3printPlugin.data.ManipulatablePosition;
import io.github.bl3rune.blu3printPlugin.items.Blu3printItem;
import io.github.bl3rune.blu3printPlugin.utils.InventoryUtils;

public class ScaleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack item = InventoryUtils.getHeldBlu3print(player, false);
            if (item == null) {
                sender.sendMessage("You must be holding a blu3print to change its scale");
                return true;
            }

            Blu3printData data = Blu3PrintPlugin.getBlu3PrintPlugin().getBlu3printFrpmCache(item, player);
            if (data == null) {
                player.sendMessage("Blu3print data not found");
            }
            
            ManipulatablePosition position = data.getPosition();

            int scale = position.doubledScale();
            if (args.length > 0) {
                try {
                    scale = Integer.parseInt(args[0]);
                } catch(Exception e) {
                    sender.sendMessage("Invalid scale argument: Must be an integer at least 1");
                    return true;
                }
                if (scale < 1) {
                    sender.sendMessage("Invalid scale argument: Must be an integer at least 1");
                    return true;
                }
            }

            String newKey = data.updateScale(player, scale);
            if ("".equals(newKey)) return true;
            Blu3printItem.updateLore(Arrays.asList("updated by " + player.getDisplayName(), newKey), item);
            player.getInventory().setItemInMainHand(item);
        }
        return true;
    }

}
