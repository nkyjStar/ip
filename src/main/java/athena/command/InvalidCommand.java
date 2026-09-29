package athena.command;

import athena.io.Output;
import athena.task.TaskManager;

/** Reports why the user's input could not be interpreted as a valid command. */
public class InvalidCommand extends Command {
    private final String message;

    /** Creates an invalid command that will display the given message. */
    public InvalidCommand(String message) {
        this.message = message;
    }

    @Override
    public void execute(TaskManager taskManager, Output output) {
        output.println(message);
    }
}
