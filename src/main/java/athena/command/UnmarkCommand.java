package athena.command;

import athena.io.Output;
import athena.task.TaskManager;

/** Marks the task selected by the user's command as not done. */
public class UnmarkCommand extends Command {
    private final String fullCommand;

    /** Creates an unmark command from the complete normalized command text. */
    public UnmarkCommand(String fullCommand) {
        this.fullCommand = fullCommand;
    }

    /** Marks the selected task as unfinished through the task manager. */
    @Override
    public void execute(TaskManager taskManager, Output output) {
        taskManager.changeTaskStatus(fullCommand, false);
    }
}
