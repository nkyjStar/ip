package athena.task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import athena.io.Output;
import athena.io.Storage;

/** Owns the task list and persists successful task operations with user feedback. */
public class TaskManager {
    private static final int MAX_TASKS = 100;
    private static final Path SAVE_PATH = Path.of("data", "athena.txt");
    private final Task[] tasks = new Task[MAX_TASKS];
    private int taskCount = 0;
    private final Output output;
    private final Storage storage = new Storage(SAVE_PATH);

    /** Creates a task manager that uses the supplied output formatter. */
    public TaskManager(Output output) {
        this.output = output;
    }

    /** Displays the tasks in their current list order. */
    public void listTasks() {
        output.printList(tasks, taskCount);
    }

    /** Adds a validated task and reports success only after saving it. */
    public void addTask(String line) {
        if (taskCount == MAX_TASKS) {
            output.println("The list is full (" + MAX_TASKS + " tasks).");
        } else if (createTask(line)) {
            if (saveTasks()) {
                output.println("Noted. I have added this task:\n    " + tasks[taskCount - 1]
                        + "\n  Now you have " + taskCount + " tasks in the list");
            } else {
                tasks[--taskCount] = null;
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
        if (taskCount == 0) {
            output.println("There are no tasks in the list.");
            return;
        }
        if (number < 1 || number > taskCount) {
            output.println("Task number must be between 1 and " + taskCount + ".");
            return;
        }
        Task task = tasks[number - 1];
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
            List<Task> loaded = storage.load(MAX_TASKS);
            for (Task task : loaded) {
                tasks[taskCount++] = task;
            }
            return true;
        } catch (IOException | SecurityException exception) {
            output.println("Could not load tasks: " + exception.getMessage());
            return false;
        }
    }

    /** Reports save failures so the caller can revert the pending change. */
    private boolean saveTasks() {
        try {
            storage.save(tasks, taskCount);
            return true;
        } catch (IOException | SecurityException exception) {
            output.println("Could not save tasks. Change was not applied: " + exception.getMessage());
            return false;
        }
    }

    /** Creates a task only after its input fields have been validated. */
    private boolean createTask(String line) {
        String formattedLine = line.trim().toLowerCase(Locale.ROOT);

        try {
            if (formattedLine.equals("todo") || formattedLine.startsWith("todo ")) {
                String description = line.trim().substring("todo".length()).trim();
                Task todo = new Todo(description);
                tasks[taskCount++] = todo;
            } else if (formattedLine.equals("deadline") || formattedLine.startsWith("deadline ")) {
                Task deadline = new Deadline(line);
                tasks[taskCount++] = deadline;
            } else if (formattedLine.equals("event") || formattedLine.startsWith("event ")) {
                Task event = new Event(line);
                tasks[taskCount++] = event;
            } else {
                output.println("Such insolence! It is rare for one to witness humans spout such nonsense "
                        + "in the presence of the goddess of wisdom.");
                return false;
            }
        } catch (IllegalArgumentException exception) {
            output.println(exception.getMessage());
            return false;
        }

        return true;
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

        if (taskCount == 0) {
            output.println("There are no tasks to delete.");
            return;
        }
        if (taskNumber < 1 || taskNumber > taskCount) {
            output.println("Task number must be between 1 and " + taskCount + ".");
            return;
        }

        Task removedTask = tasks[taskNumber - 1];
        for (int i = taskNumber - 1; i < taskCount - 1; i++) {
            tasks[i] = tasks[i + 1];
        }
        taskCount--;
        tasks[taskCount] = null;

        if (!saveTasks()) {
            for (int i = taskCount; i >= taskNumber; i--) {
                tasks[i] = tasks[i - 1];
            }
            tasks[taskNumber - 1] = removedTask;
            taskCount++;
            return;
        }

        output.println("Noted. I've removed this task:\n    " + removedTask
                + "\n  Now you have " + taskCount + " tasks in the list.");
    }
}
