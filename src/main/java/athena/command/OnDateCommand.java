package athena.command;

import java.time.LocalDate;

import athena.io.Output;
import athena.task.TaskManager;

/** Lists deadlines due on a specified date. */
public class OnDateCommand extends Command {
    private final LocalDate date;

    /** Creates a query for deadlines due on the given date. */
    public OnDateCommand(LocalDate date) {
        this.date = date;
    }

    @Override
    public void execute(TaskManager taskManager, Output output) {
        taskManager.listDeadlinesOn(date);
    }
}
