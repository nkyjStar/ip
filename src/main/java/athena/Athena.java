package athena;

import java.util.Locale;

import athena.io.Input;
import athena.io.Output;
import athena.task.TaskManager;

/** Runs the Athena command-line task manager. */
public class Athena {
    private final Output output = new Output();
    private final TaskManager taskManager = new TaskManager(output);

    /**
     * Interprets a command and invokes the corresponding task-manager operation.
     */
    private void handleCommand(String line) {
        String[] parts = line.trim().split("\\s+", 2);
        String command = parts[0].toLowerCase(Locale.ROOT);
        String argument = parts.length == 2 ? parts[1] : "";
        switch (command) {
            case "list":
                if (!argument.isEmpty()) {
                    output.println("The list command does not accept arguments.");
                } else {
                    taskManager.listTasks();
                }
                break;
            case "mark":
            case "unmark":
                taskManager.changeTaskStatus(command + " " + argument, command.equals("mark"));
                break;
            case "delete":
                taskManager.deleteTask(command + " " + argument);
                break;
            case "todo":
            case "deadline":
            case "event":
                taskManager.addTask(command + " " + argument);
                break;
            default:
                output.println("Unknown command. Use todo, deadline, event, list, mark, unmark, delete, or bye.");
        }
    }

    /** Starts Athena and processes commands until the user says goodbye. */
    public static void main(String[] args) {
        Athena athena = new Athena();
        athena.output.greeting();
        if (!athena.taskManager.loadTasks()) {
            athena.output.farewell();
            return;
        }

        // Create input scanner
        Input input = new Input();

        // Keep querying input till "bye"
        while (input.hasNextLine() && input.parseInput(athena.output, athena::handleCommand)) {
            // Continue querying input
        }

        athena.output.farewell();
        input.close();
    }
}
