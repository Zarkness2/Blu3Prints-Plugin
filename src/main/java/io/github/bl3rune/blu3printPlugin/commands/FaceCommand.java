package io.github.bl3rune.blu3printPlugin.commands;

import java.util.Arrays;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin;
import io.github.bl3rune.blu3printPlugin.data.Blu3printData;
import io.github.bl3rune.blu3printPlugin.enums.Orientation;
import io.github.bl3rune.blu3printPlugin.items.Blu3printItem;
import io.github.bl3rune.blu3printPlugin.utils.InventoryUtils;

public class FaceCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            ItemStack item = InventoryUtils.getHeldBlu3print(player, false);
            if (item == null) {
                sender.sendMessage("You must be holding a blu3print to change what side faces you");
                return true;
            }

            Blu3printData data = Blu3PrintPlugin.getBlu3PrintPlugin().getBlu3printFrpmCache(item, player);
            if (data == null) {
                player.sendMessage("Blu3print data not found");
            }
            Orientation orientation = data.getPosition().getOrientation().getNextOrientation();
            if (args.length > 0) {
                orientation = Orientation.valueOf(args[0]);
                if  (orientation == null)  {
                    sender.sendMessage("Invalid orientation argument, try one of the following: " + Arrays.toString(Orientation.values()));
                    return true;
                }
            }
            String newKey = data.updateOrientation(orientation);
            Blu3printItem.updateLore(Arrays.asList("updated by " + player.getDisplayName(), newKey), item);
            player.getInventory().setItemInMainHand(item);
        }
        return true;
    }

}
