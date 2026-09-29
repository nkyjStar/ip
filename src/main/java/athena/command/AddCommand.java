package athena.command;

import athena.task.TaskManager;

/** Adds a Todo, Deadline, or Event described by the user's command. */
public class AddCommand extends Command {
    private final String fullCommand;

    /** Creates an add command from the complete normalized command text. */
    public AddCommand(String fullCommand) {
        this.fullCommand = fullCommand;
    }

    @Override
    public void execute(TaskManager taskManager) {
        taskManager.addTask(fullCommand);
    }
}
