package athena.command;

import athena.io.Output;
import athena.task.TaskManager;

/** Displays the tasks in their current list order. */
public class ListCommand extends Command {
    @Override
    public void execute(TaskManager taskManager, Output output) {
        taskManager.listTasks();
    }
}
