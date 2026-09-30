package athena.command;

import athena.io.Output;
import athena.task.TaskManager;

/** Signals that Athena should stop accepting commands. */
public class ExitCommand extends Command {
    /** Performs no task operation; the caller uses {@link #isExit()} to end the application. */
    @Override
    public void execute(TaskManager taskManager, Output output) {
        // Exiting does not require a task-list operation.
    }

    /** Returns true to signal that the application should exit. */
    @Override
    public boolean isExit() {
        return true;
    }
}
