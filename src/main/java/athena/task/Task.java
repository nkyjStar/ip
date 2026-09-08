package athena.task;

import athena.io.Output;

/** Represents a task and whether it has been completed. */
public class Task {
    private final String description;
    private boolean isDone;

    /** Creates an unfinished task with the given description. */
    public Task(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("The description of a task cannot be empty.");
        }
        this.description = description;
        this.isDone = false;
    }

    /** Marks this task as done. */
    public void markAsDone() {
        isDone = true;
    }

    /** Marks this task as not done. */
    public void markAsNotDone() {
        isDone = false;
    }

    /**
     * Changes the status of the selected task and prints the result.
     *
     * @param tasks     the task list
     * @param taskCount the number of tasks currently in the list
     * @param line      the complete mark or unmark command
     * @param done      whether the task should be marked as done
     * @param output    the output formatter used for feedback
     */
    public static void changeTaskStatus(Task[] tasks, int taskCount, String line, boolean done, Output output) {
        String command = done ? "mark" : "unmark";
        String numberText = line.substring(command.length()).trim();
        try {
            int taskNumber = Integer.parseInt(numberText);
            if (taskNumber < 1 || taskNumber > taskCount) {
                output.println("Task number must be between 1 and " + taskCount + ".");
                return;
            }

            Task task = tasks[taskNumber - 1];
            if (done) {
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

    /** Returns the status icon used when displaying this task. */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /** Returns the task description. */
    public String getDescription() {
        return description;
    }

    /** Returns the task in display format. */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
