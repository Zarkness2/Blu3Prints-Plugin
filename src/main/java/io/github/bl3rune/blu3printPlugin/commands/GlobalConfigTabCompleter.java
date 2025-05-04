package io.github.bl3rune.blu3printPlugin.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import io.github.bl3rune.blu3printPlugin.enums.Alignment;
import io.github.bl3rune.blu3printPlugin.enums.GConfig;

public class GlobalConfigTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList(GConfig.values()).stream().map(c -> c.name()).collect(Collectors.toList());
        } else if (args.length == 2) {
            try {
                GConfig config = GConfig.valueOf(args[0]);
                switch (config) {
                    case HOLOGRAM_TTL:
                    case MAX_SCALE:
                        return IntStream.rangeClosed(1, 15).boxed().map(i -> 
                            i.toString()
                        ).collect(Collectors.toList());
                    case MAX_OVERALL_SIZE:
                    case MAX_SIZE:
                    case COOLDOWN:
                        return IntStream.rangeClosed(1, 9).boxed().map(i -> {
                            i = i * 100;
                            return i.toString();
                        }).collect(Collectors.toList());
                    case ALIGNMENT:
                        return Arrays.asList(Alignment.values()).stream().map(a -> a.name()).collect(Collectors.toList());
                    case RELATIVE:
                        return Arrays.asList("true", "false");
                    default:
                        break;
                }
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

}
