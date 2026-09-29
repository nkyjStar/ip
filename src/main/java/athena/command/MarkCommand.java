package athena.command;

import athena.io.Output;
import athena.task.TaskManager;

/** Marks the task selected by the user's command as done. */
public class MarkCommand extends Command {
    private final String fullCommand;

    /** Creates a mark command from the complete normalized command text. */
    public MarkCommand(String fullCommand) {
        this.fullCommand = fullCommand;
    }

    @Override
    public void execute(TaskManager taskManager, Output output) {
        taskManager.changeTaskStatus(fullCommand, true);
    }
}
