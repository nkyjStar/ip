package athena.command;

import athena.io.Output;
import athena.task.TaskManager;

/** Represents a user command that can be executed against Athena's task manager. */
public abstract class Command {
    /** Executes this command using the supplied task manager and output interface. */
    public abstract void execute(TaskManager taskManager, Output output);

    /** Returns whether this command should end the application. */
    public boolean isExit() {
        return false;
    }
}
