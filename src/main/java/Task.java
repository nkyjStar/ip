/** Represents a task and whether it has been completed. */
public class Task {
    protected String description;
    protected boolean isDone;

    /** Creates an unfinished task with the given description. */
    public Task(String description) {
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
