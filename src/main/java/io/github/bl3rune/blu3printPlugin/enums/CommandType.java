package io.github.bl3rune.blu3printPlugin.enums;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

import io.github.bl3rune.blu3printPlugin.commands.Blu3printCommand;
import io.github.bl3rune.blu3printPlugin.commands.ConfigCommand;
import io.github.bl3rune.blu3printPlugin.commands.ConfigTabCompleter;
import io.github.bl3rune.blu3printPlugin.commands.FaceCommand;
import io.github.bl3rune.blu3printPlugin.commands.FaceTabCompleter;
import io.github.bl3rune.blu3printPlugin.commands.ScaleCommand;
import io.github.bl3rune.blu3printPlugin.commands.ScaleTabCompleter;
import io.github.bl3rune.blu3printPlugin.commands.TurnCommand;
import io.github.bl3rune.blu3printPlugin.commands.TurnTabCompleter;
import io.github.bl3rune.blu3printPlugin.commands.DuplicateCommand;
import io.github.bl3rune.blu3printPlugin.commands.ExportCommand;
import io.github.bl3rune.blu3printPlugin.commands.GiveCommand;
import io.github.bl3rune.blu3printPlugin.commands.HelpCommand;
import io.github.bl3rune.blu3printPlugin.commands.ImportCommand;
import io.github.bl3rune.blu3printPlugin.commands.NameCommand;
import io.github.bl3rune.blu3printPlugin.commands.RotateCommand;
import io.github.bl3rune.blu3printPlugin.commands.RotateTabCompleter;

/**
 * Enum for the different command types.
 */
public enum CommandType {

    BLU3PRINT("blu3print", new Blu3printCommand()),
    DUPLICATE("duplicate", new DuplicateCommand()),
    FACE("face", new FaceCommand(), new FaceTabCompleter()),
    ROTATE("rotate", new RotateCommand(), new RotateTabCompleter()),
    TURN("turn", new TurnCommand(), new TurnTabCompleter()),
    IMPORT("import", new ImportCommand()),
    EXPORT("export", new ExportCommand()),
    SCALE("scale", new ScaleCommand(), new ScaleTabCompleter()),
    NAME("name", new NameCommand()),
    GIVE("give", new GiveCommand()),
    HELP("help", new HelpCommand()),
    CONFIG("config", new ConfigCommand(), new ConfigTabCompleter())
    ;

    private final String commandString;
    private final CommandExecutor commandExecutor;
    private final TabCompleter tabCompleter;

    CommandType(String commandString, CommandExecutor commandExecutor) {
        this.commandString = commandString;
        this.commandExecutor = commandExecutor;
        this.tabCompleter = null;
    }

    CommandType(String commandString, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        this.commandString = commandString;
        this.commandExecutor = commandExecutor;
        this.tabCompleter = tabCompleter;
    }

    @Override
    public String toString() {
        return commandString;
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }

    /**
     * Returns the full command name, including the plugin name.
     * @return The full command name.
     */
    public String getFullCommandName() {
        if (commandString.equals("blu3print")) {
            return commandString;
        }
        return "blu3print." + commandString;
    }

}
