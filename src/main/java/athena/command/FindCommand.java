package athena.command;

import athena.io.Output;
import athena.task.TaskManager;

/** Finds tasks whose descriptions contain a keyword. */
public class FindCommand extends Command {
    private final String keyword;

    /** Creates a task search for the given keyword. */
    public FindCommand(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public void execute(TaskManager taskManager, Output output) {
        taskManager.findTasks(keyword);
    }
}
