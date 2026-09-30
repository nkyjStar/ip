package athena.task;

/** Represents a task with no associated date or time. */
public class Todo extends Task {
    /** Creates a Todo with the given description. */
    public Todo(String description) {
        super(description);
    }

    /** Returns this todo in display format. */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
