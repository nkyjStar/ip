package athena;

import athena.command.Command;
import athena.io.Input;
import athena.io.Output;
import athena.task.TaskManager;

/** Runs the Athena command-line task manager. */
public class Athena {
    private final Output output = new Output();
    private final TaskManager taskManager = new TaskManager(output);

    /** Parses and executes one user command. */
    private void executeCommand(String line) {
        Command command = Parser.parse(line);
        command.execute(taskManager, output);
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
        while (input.hasNextLine() && input.parseInput(athena.output, athena::executeCommand)) {
            // Continue querying input
        }

        athena.output.farewell();
        input.close();
    }
}
