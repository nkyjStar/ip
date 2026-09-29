package athena.command;

import athena.task.TaskManager;

/** Represents a user command that can be executed against Athena's task manager. */
public abstract class Command {
    /** Executes this command against the supplied task manager. */
    public abstract void execute(TaskManager taskManager);

    /** Returns whether this command should end the application. */
    public boolean isExit() {
        return false;
    }
}
