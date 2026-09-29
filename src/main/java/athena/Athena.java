package athena;

import athena.io.Input;
import athena.io.Output;
import athena.task.TaskManager;

/** Runs the Athena command-line task manager. */
public class Athena {
    private final Output output = new Output();
    private final TaskManager taskManager = new TaskManager(output);
    private final Parser parser = new Parser(output, taskManager);

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
        while (input.hasNextLine() && input.parseInput(athena.output, athena.parser::parse)) {
            // Continue querying input
        }

        athena.output.farewell();
        input.close();
    }
}
