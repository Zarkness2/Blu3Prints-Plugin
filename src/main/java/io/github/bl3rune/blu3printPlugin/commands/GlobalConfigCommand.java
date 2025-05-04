package io.github.bl3rune.blu3printPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin;
import io.github.bl3rune.blu3printPlugin.config.GlobalConfig;
import io.github.bl3rune.blu3printPlugin.enums.Alignment;
import io.github.bl3rune.blu3printPlugin.enums.GConfig;

public class GlobalConfigCommand implements CommandExecutor {

    private Blu3PrintPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GConfig config = null;
        if (plugin == null) {
            plugin = Blu3PrintPlugin.getBlu3PrintPlugin();
        }

        if (args.length == 0) {
            printCurrentConfig(sender);
            return true;
        }

        try {
            String c = args[0];
            config = GConfig.valueOf(c.toUpperCase());
        } finally {
            if (config == null) {
                StringBuilder sb = new StringBuilder();
                for (GConfig cc : GConfig.values()) {
                    sb.append(cc.name()).append(" ");
                }
                sender.sendMessage(ChatColor.RED + "Not valid subcommand try : " + sb.toString());
                return true;
            }
        }
        try {
            switch (config) {
                case MAX_SCALE:
                case MAX_SIZE:
                case MAX_OVERALL_SIZE:
                case HOLOGRAM_TTL:
                case COOLDOWN:
                    plugin.getConfig().set(config.getConfigPath(), Integer.parseInt(args[1]));
                    break;
                case ALIGNMENT:
                    plugin.getConfig().set(config.getConfigPath(), Alignment.valueOf(args[1]));
                    break;
                case RELATIVE:
                    plugin.getConfig().set(config.getConfigPath(), Boolean.parseBoolean(args[1]));
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            sender.sendMessage("Failed to set value " + config.name());
            return false;
        }
        GlobalConfig.refreshConfiguration();

        return true;
    }

    private void printCurrentConfig(CommandSender sender) {
        sender.sendMessage("Current Configurations:");
        for (GConfig config : GConfig.values()) {
            sender.sendMessage(config.getConfigPath() + ": " + getConfigValue(config));
        }
    }

    private String getConfigValue(GConfig config) {
        try {
            switch (config) {
                case ALIGNMENT:
                    return "" + GlobalConfig.getAlignment().name();
                case COOLDOWN:
                    return "" + GlobalConfig.getCooldown();
                case HOLOGRAM_TTL:
                    return "" + GlobalConfig.getHologramTtl();
                case MAX_OVERALL_SIZE:
                    return "" + GlobalConfig.getMaxOverallSize();
                case MAX_SCALE:
                    return "" + GlobalConfig.getMaxScale();
                case MAX_SIZE:
                    return "" + GlobalConfig.getMaxSize();
                case RELATIVE:
                    return "" + GlobalConfig.getRelativePlacement();
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

}
