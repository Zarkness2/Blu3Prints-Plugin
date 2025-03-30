package io.github.bl3rune.blu3printPlugin.enums;

import org.bukkit.command.CommandExecutor;

import io.github.bl3rune.blu3printPlugin.commands.Blu3printCommand;
import io.github.bl3rune.blu3printPlugin.commands.FaceCommand;
import io.github.bl3rune.blu3printPlugin.commands.ScaleCommand;
import io.github.bl3rune.blu3printPlugin.commands.DuplicateCommand;
import io.github.bl3rune.blu3printPlugin.commands.ExportCommand;
import io.github.bl3rune.blu3printPlugin.commands.GiveCommand;
import io.github.bl3rune.blu3printPlugin.commands.HelpCommand;
import io.github.bl3rune.blu3printPlugin.commands.ImportCommand;
import io.github.bl3rune.blu3printPlugin.commands.NameCommand;
import io.github.bl3rune.blu3printPlugin.commands.RotateCommand;

/**
 * Enum for the different command types.
 */
public enum CommandType {

    BLU3PRINT("blu3print", new Blu3printCommand()),
    DUPLICATE("duplicate", new DuplicateCommand()),
    FACE("face", new FaceCommand()),
    ROTATE("rotate", new RotateCommand()),
    IMPORT("import", new ImportCommand()),
    EXPORT("export", new ExportCommand()),
    SCALE("scale", new ScaleCommand()),
    NAME("name", new NameCommand()),
    GIVE("give", new GiveCommand()),
    HELP("help", new HelpCommand());
    ;

    private final String commandString;
    private final CommandExecutor commandExecutor;

    CommandType(String commandString, CommandExecutor commandExecutor) {
        this.commandString = commandString;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public String toString() {
        return commandString;
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
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
