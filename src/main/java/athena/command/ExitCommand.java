package athena.command;

import athena.io.Output;
import athena.task.TaskManager;

/** Signals that Athena should stop accepting commands. */
public class ExitCommand extends Command {
    @Override
    public void execute(TaskManager taskManager, Output output) {
        // Exiting does not require a task-list operation.
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
