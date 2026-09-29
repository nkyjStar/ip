package athena;

import java.util.Locale;

import athena.command.AddCommand;
import athena.command.DeleteCommand;
import athena.command.ListCommand;
import athena.io.Output;
import athena.task.TaskManager;

/** Interprets user commands and dispatches them to the task manager. */
public class Parser {
    private final Output output;
    private final TaskManager taskManager;

    /** Creates a parser that reports through the given output and operates on the given task manager. */
    public Parser(Output output, TaskManager taskManager) {
        this.output = output;
        this.taskManager = taskManager;
    }

    /** Interprets one command and invokes the corresponding task-manager operation. */
    public void parse(String line) {
        String[] parts = line.trim().split("\\s+", 2);
        String command = parts[0].toLowerCase(Locale.ROOT);
        String argument = parts.length == 2 ? parts[1] : "";
        switch (command) {
            case "list":
                if (!argument.isEmpty()) {
                    output.println("The list command does not accept arguments.");
                } else {
                    new ListCommand().execute(taskManager);
                }
                break;
            case "mark":
            case "unmark":
                taskManager.changeTaskStatus(command + " " + argument, command.equals("mark"));
                break;
            case "delete":
                new DeleteCommand(command + " " + argument).execute(taskManager);
                break;
            case "todo":
            case "deadline":
            case "event":
                new AddCommand(command + " " + argument).execute(taskManager);
                break;
            default:
                output.println("Unknown command. Use todo, deadline, event, list, mark, unmark, delete, or bye.");
        }
    }
}
