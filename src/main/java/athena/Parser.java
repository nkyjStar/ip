package athena;

import java.util.Locale;

import athena.command.AddCommand;
import athena.command.DeleteCommand;
import athena.command.Command;
import athena.command.ExitCommand;
import athena.command.InvalidCommand;
import athena.command.ListCommand;
import athena.command.MarkCommand;
import athena.command.UnmarkCommand;

/** Interprets user input and creates the corresponding command. */
public final class Parser {
    private static final String UNKNOWN_COMMAND_MESSAGE =
            "Unknown command. Use todo, deadline, event, list, mark, unmark, delete, or bye.";

    private Parser() {
        // Prevent instantiation because this class only provides parsing behavior.
    }

    /** Returns the command represented by the given user input. */
    public static Command parse(String line) {
        String[] parts = line.trim().split("\\s+", 2);
        String command = parts[0].toLowerCase(Locale.ROOT);
        String argument = parts.length == 2 ? parts[1] : "";
        switch (command) {
        case "list":
            if (!argument.isEmpty()) {
                return new InvalidCommand("The list command does not accept arguments.");
            }
            return new ListCommand();
        case "mark":
            return new MarkCommand(command + " " + argument);
        case "unmark":
            return new UnmarkCommand(command + " " + argument);
        case "delete":
            return new DeleteCommand(command + " " + argument);
        case "bye":
            if (!argument.isEmpty()) {
                return new InvalidCommand(UNKNOWN_COMMAND_MESSAGE);
            }
            return new ExitCommand();
        case "todo":
        case "deadline":
        case "event":
            return new AddCommand(command + " " + argument);
        default:
            return new InvalidCommand(UNKNOWN_COMMAND_MESSAGE);
        }
    }
}
