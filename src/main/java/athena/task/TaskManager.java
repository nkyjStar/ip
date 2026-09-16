package athena.task;

import athena.io.Output;

/** Owns the task list and performs task operations with user feedback. */
public class TaskManager {
    private static final int MAX_TASKS = 100;
    private final Task[] tasks = new Task[MAX_TASKS];
    private int taskCount = 0;
    private final Output output;

    /** Creates an empty task manager that uses the supplied output formatter. */
    public TaskManager(Output output) {
        this.output = output;
    }

    /** Displays the tasks in their current list order. */
    public void listTasks() {
        output.printList(tasks, taskCount);
    }

    /** Adds a task from a todo, deadline, or event command and prints the result. */
    public void addTask(String line) {
        if (taskCount >= tasks.length) {
            output.println("Perhaps one should first fulfill their responsibilities "
                    + "before adding more beyond their current limit.");
            return;
        }

        String formattedLine = line.trim().toLowerCase();
        try {
            if (formattedLine.equals("todo") || formattedLine.startsWith("todo ")) {
                String description = line.trim().substring("todo".length()).trim();
                tasks[taskCount++] = new Todo(description);
            } else if (formattedLine.equals("deadline") || formattedLine.startsWith("deadline ")) {
                Task deadline = new Deadline(line);
                tasks[taskCount++] = deadline;
            } else if (formattedLine.equals("event") || formattedLine.startsWith("event ")) {
                Task event = new Event(line);
                tasks[taskCount++] = event;
            } else {
                output.println("Such insolence! It is rare for one to witness humans spout such nonsense "
                        + "in the presence of the goddess of wisdom.");
                return;
            }
        } catch (IllegalArgumentException exception) {
            output.println(exception.getMessage());
            return;
        }

        output.println("Noted. I have added this task:\n    " + tasks[taskCount - 1]
                + "\n  Now you have " + taskCount + " tasks in the list");
    }

    /**
     * Changes the status of the selected task and prints the result.
     *
     * @param line the complete mark or unmark command
     * @param isDone whether the task should be marked as done
     */
    public void changeTaskStatus(String line, boolean isDone) {
        String command = isDone ? "mark" : "unmark";
        String numberText = line.substring(command.length()).trim();
        try {
            int taskNumber = Integer.parseInt(numberText);
            if (taskNumber < 1 || taskNumber > taskCount) {
                output.println("Task number must be between 1 and " + taskCount + ".");
                return;
            }

            Task task = tasks[taskNumber - 1];
            if (isDone) {
                task.markAsDone();
                output.println("Nice! I've marked this task as done:\n  " + task);
            } else {
                task.markAsNotDone();
                output.println("OK, I've marked this task as not done yet:\n  " + task);
            }
        } catch (NumberFormatException exception) {
            output.println("Please provide a valid task number after " + command + ".");
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

        output.println("Noted. I've removed this task:\n    " + removedTask
                + "\n  Now you have " + taskCount + " tasks in the list.");
    }
}
