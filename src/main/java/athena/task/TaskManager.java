package athena.task;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;

import athena.io.Output;
import athena.io.Storage;

/** Coordinates task operations, persistence, and user feedback. */
public class TaskManager {
    private static final Path SAVE_PATH = Path.of("data", "athena.txt");
    private TaskList tasks = new TaskList();
    private final Output output;
    private final Storage storage = new Storage(SAVE_PATH);

    /** Creates a task manager that uses the supplied output formatter. */
    public TaskManager(Output output) {
        this.output = output;
    }

    /** Displays the tasks in their current list order. */
    public void listTasks() {
        output.printList(tasks);
    }

    /** Displays tasks whose descriptions contain the given keyword. */
    public void findTasks(String keyword) {
        output.printMatchingTasks(tasks.findByDescription(keyword));
    }

    /** Displays deadlines and events occurring on the specified date in list order. */
    public void listTasksOn(LocalDate date) {
        output.printTasksOn(date, tasks.findTasksOn(date));
    }

    /** Adds a validated task and reports success only after saving it. */
    public void addTask(String line) {
        if (tasks.isFull()) {
            output.println("The list is full (" + tasks.getCapacity() + " tasks).");
        } else {
            Task task = createTask(line);
            if (task == null) {
                return;
            }
            tasks.add(task);
            if (saveTasks()) {
                output.println("Noted. I have added this task:\n    " + task
                        + "\n  Now you have " + tasks.size() + " tasks in the list");
            } else {
                tasks.remove(tasks.size() - 1);
            }
        }
    }

    /** Validates the index and commits a status change only if it can be saved. */
    public void changeTaskStatus(String line, boolean isDone) {
        String command = isDone ? "mark" : "unmark";
        String argument = line.substring(command.length()).trim();
        int number;
        try {
            number = Integer.parseInt(argument.trim());
        } catch (NumberFormatException exception) {
            output.println("Please provide a valid task number after " + command + ".");
            return;
        }
        if (tasks.isEmpty()) {
            output.println("There are no tasks in the list.");
            return;
        }
        if (number < 1 || number > tasks.size()) {
            output.println("Task number must be between 1 and " + tasks.size() + ".");
            return;
        }
        Task task = tasks.get(number - 1);
        boolean wasDone = task.isDone();
        if (isDone) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
        if (wasDone != isDone && !saveTasks()) {
            if (wasDone) {
                task.markAsDone();
            } else {
                task.markAsNotDone();
            }
            return;
        }
        output.println((isDone ? "Nice! I've marked this task as done:\n  "
                : "OK, I've marked this task as not done yet:\n  ") + task);
    }

    /** Validates the entire saved list before installing it into the running application. */
    public boolean loadTasks() {
        try {
            tasks = new TaskList(storage.load(tasks.getCapacity()));
            return true;
        } catch (IOException | SecurityException exception) {
            output.println("Could not load tasks: " + exception.getMessage());
            return false;
        }
    }

    /** Reports save failures so the caller can revert the pending change. */
    private boolean saveTasks() {
        try {
            storage.save(tasks);
            return true;
        } catch (IOException | SecurityException exception) {
            output.println("Could not save tasks. Change was not applied: " + exception.getMessage());
            return false;
        }
    }

    /** Creates a task only after its input fields have been validated. */
    private Task createTask(String line) {
        String formattedLine = line.trim().toLowerCase(Locale.ROOT);

        try {
            if (formattedLine.equals("todo") || formattedLine.startsWith("todo ")) {
                String description = line.trim().substring("todo".length()).trim();
                return new Todo(description);
            } else if (formattedLine.equals("deadline") || formattedLine.startsWith("deadline ")) {
                return new Deadline(line);
            } else if (formattedLine.equals("event") || formattedLine.startsWith("event ")) {
                return new Event(line);
            } else {
                output.println("Such insolence! It is rare for one to witness humans spout such nonsense "
                        + "in the presence of the goddess of wisdom.");
                return null;
            }
        } catch (IllegalArgumentException exception) {
            output.println(exception.getMessage());
            return null;
        }
    }

    /** Removes the selected task and closes the gap so list numbers remain consecutive. */
    public void deleteTask(String line) {
        String numberText = line.substring("delete".length()).trim();
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(numberText);
        } catch (NumberFormatException exception) {
            output.println("Please provide a valid task number after delete.");
            return;
        }

        if (tasks.isEmpty()) {
            output.println("There are no tasks to delete.");
            return;
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            output.println("Task number must be between 1 and " + tasks.size() + ".");
            return;
        }

        int taskIndex = taskNumber - 1;
        Task removedTask = tasks.remove(taskIndex);

        if (!saveTasks()) {
            tasks.add(taskIndex, removedTask);
            return;
        }

        output.println("Noted. I've removed this task:\n    " + removedTask
                + "\n  Now you have " + tasks.size() + " tasks in the list.");
    }
}
