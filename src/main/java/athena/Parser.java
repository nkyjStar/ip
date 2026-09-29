package athena;

import java.time.LocalDate;
import java.util.Locale;

import athena.command.AddCommand;
import athena.command.DeleteCommand;
import athena.command.Command;
import athena.command.ExitCommand;
import athena.command.FindCommand;
import athena.command.InvalidCommand;
import athena.command.ListCommand;
import athena.command.MarkCommand;
import athena.command.OnDateCommand;
import athena.command.UnmarkCommand;
import athena.task.TaskDateTime;

/** Interprets user input and creates the corresponding command. */
public final class Parser {
    private static final String UNKNOWN_COMMAND_MESSAGE =
            "Unknown command. Use todo, deadline, event, list, find, on, mark, unmark, delete, or bye.";
    private static final String INVALID_DATE_MESSAGE =
            "Please provide a valid date in d/M/yyyy or yyyy-MM-dd format after on.";

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
        case "find":
            if (argument.isEmpty()) {
                return new InvalidCommand("Please provide a keyword after find.");
            }
            return new FindCommand(argument);
        case "on":
            try {
                LocalDate date = TaskDateTime.parseDate(argument);
                return new OnDateCommand(date);
            } catch (IllegalArgumentException exception) {
                return new InvalidCommand(INVALID_DATE_MESSAGE);
            }
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
