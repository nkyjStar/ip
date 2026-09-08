package athena.task;

/** Represents a task with no associated date or time. */
public class Todo extends Task {
    /** Creates a Todo with the given description. */
    public Todo(String description) {
        super(description);
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
