package io.github.bl3rune.blu3printPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin;
import io.github.bl3rune.blu3printPlugin.items.Blu3printItem;

public class GiveCommand implements CommandExecutor {

    private Blu3PrintPlugin instance;

    public GiveCommand () {
        this.instance = Blu3PrintPlugin.getBlu3PrintPlugin();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                player.getInventory().addItem(Blu3printItem.getBlankBlu3print());
            } else {
                return false;
            }
        } else {
            String playerName = args[0];
            if (playerName == null) {
                return false;
            }
            Player player = instance.getServer().getPlayerExact(playerName);
            if (player == null) {
                sender.sendMessage("Not a valid player name");
            } else {
                player.getInventory().addItem(Blu3printItem.getBlankBlu3print());
            }
        }
        return true;
    }

}
