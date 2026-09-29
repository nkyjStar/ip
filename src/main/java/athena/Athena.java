package athena;

import athena.command.Command;
import athena.io.Input;
import athena.io.Output;
import athena.task.TaskManager;

/** Runs the Athena command-line task manager. */
public class Athena {
    private final Output output = new Output();
    private final TaskManager taskManager = new TaskManager(output);

    /** Parses and executes one user command, returning whether it should exit the application. */
    private boolean executeCommand(String line) {
        Command command = Parser.parse(line);
        command.execute(taskManager, output);
        return command.isExit();
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

        boolean isExit = false;
        while (!isExit && input.hasNextLine()) {
            String line = input.readCommand(athena.output);
            isExit = athena.executeCommand(line);
        }

        athena.output.farewell();
        input.close();
    }
}
