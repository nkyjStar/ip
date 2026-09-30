package athena.command;

import athena.io.Output;
import athena.task.TaskManager;

/** Deletes the task selected by the user's command. */
public class DeleteCommand extends Command {
    private final String fullCommand;

    /** Creates a delete command from the complete normalized command text. */
    public DeleteCommand(String fullCommand) {
        this.fullCommand = fullCommand;
    }

    /** Deletes the selected task through the task manager. */
    @Override
    public void execute(TaskManager taskManager, Output output) {
        taskManager.deleteTask(fullCommand);
    }
}
