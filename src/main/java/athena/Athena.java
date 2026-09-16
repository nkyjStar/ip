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
        String lowerCaseLine = line.toLowerCase(Locale.ROOT);

        if (line.equalsIgnoreCase("list")) {
            taskManager.listTasks();
        } else if (lowerCaseLine.startsWith("mark ")) {
            taskManager.changeTaskStatus(line, true);
        } else if (lowerCaseLine.startsWith("unmark ")) {
            taskManager.changeTaskStatus(line, false);
        } else if (lowerCaseLine.equals("delete") || lowerCaseLine.startsWith("delete ")) {
            taskManager.deleteTask(line);
        } else {
            taskManager.addTask(line);
        }
    }

    /** Starts Athena and processes commands until the user says goodbye. */
    public static void main(String[] args) {
        Athena athena = new Athena();
        athena.output.greeting();

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
