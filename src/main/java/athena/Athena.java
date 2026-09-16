package athena;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import athena.io.Input;
import athena.io.Output;
import athena.io.Storage;
import athena.task.Deadline;
import athena.task.Event;
import athena.task.Task;
import athena.task.Todo;

/** Runs the Athena command-line task manager. */
public class Athena {
    private static final int MAX_TASKS = 100;
    private static final Path SAVE_PATH = Path.of("data", "athena.txt");
    private final Task[] tasks = new Task[MAX_TASKS];
    private int taskCount = 0;

    private final Output output = new Output();
    private final Storage storage = new Storage(SAVE_PATH);

    /**
     * Interprets a command and invokes the corresponding task-manager operation.
     */
    private void handleCommand(String line) {
        String[] parts = line.trim().split("\\s+", 2);
        String command = parts[0].toLowerCase(Locale.ROOT);
        String argument = parts.length == 2 ? parts[1] : "";
        switch (command) {
            case "list":
                if (!argument.isEmpty()) {
                    output.println("The list command does not accept arguments.");
                } else {
                    output.printList(tasks, taskCount);
                }
                break;
            case "mark":
            case "unmark":
                changeStatus(argument, command.equals("mark"));
                break;
            case "todo":
            case "deadline":
            case "event":
                if (taskCount == MAX_TASKS) {
                    output.println("The list is full (" + MAX_TASKS + " tasks).");
                } else if (addTask(command + " " + argument)) {
                    if (saveTasks()) {
                        output.println("Noted. I have added this task:\n    " + tasks[taskCount - 1]
                                + "\n  Now you have " + taskCount + " tasks in the list");
                    } else {
                        tasks[--taskCount] = null;
                    }
                }
                break;
            default:
                output.println("Unknown command. Use todo, deadline, event, list, mark, unmark, or bye.");
        }
    }

    /** Validates the index and commits a status change only if it can be saved. */
    private void changeStatus(String argument, boolean isDone) {
        String command = isDone ? "mark" : "unmark";
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
    private boolean loadTasks() {
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
    private boolean addTask(String line) {
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

    /** Starts Athena and processes commands until the user says goodbye. */
    public static void main(String[] args) {
        Athena athena = new Athena();
        athena.output.greeting();
        if (!athena.loadTasks()) {
            athena.output.farewell();
            return;
        }

        // Create input scanner
        Input input = new Input();

        // Keep querying input till "bye"
        while (input.hasNextLine() && input.parseInput(athena.output, athena::handleCommand)) {
            // Continue querying input
        }

        athena.output.farewell();
        input.close();
    }
}
