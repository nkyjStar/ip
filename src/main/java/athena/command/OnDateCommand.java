package athena.command;

import java.time.LocalDate;

import athena.io.Output;
import athena.task.TaskManager;

/** Lists deadlines and events occurring on a specified date. */
public class OnDateCommand extends Command {
    private final LocalDate date;

    /** Creates a query for deadlines and events occurring on the given date. */
    public OnDateCommand(LocalDate date) {
        this.date = date;
    }

    /** Displays deadlines and events occurring on this command's date. */
    @Override
    public void execute(TaskManager taskManager, Output output) {
        taskManager.listTasksOn(date);
    }
}
