package athena.task;

/** Represents a task that must be completed by a specified date and optional time. */
public class Deadline extends Task {
    private final TaskDateTime deadline;

    /** Creates a deadline from a description and a supported date with an optional time. */
    public Deadline(String description, String deadline) {
        this(description, TaskDateTime.parse(deadline));
    }

    /** Creates a deadline with the given description, date, and optional time. */
    public Deadline(String description, TaskDateTime deadline) {
        super(description);
        if (deadline == null) {
            throw new IllegalArgumentException("The date of a deadline cannot be empty.");
        }
        this.deadline = deadline;
    }

    /** Returns the date and optional time represented by this deadline. */
    public TaskDateTime getDeadline() {
        return deadline;
    }

    /**
     * Creates a deadline from a command in the form
     * {@code deadline description /by date}.
     */
    public Deadline(String args) {
        String[] argList = parseArgs(args);
        super(argList[0]);
        deadline = TaskDateTime.parse(argList[1]);
    }

    /**
     * Splits a deadline command into its description and deadline.
     *
     * @param args the complete deadline command
     * @return an array containing the description and deadline
     * @throws IllegalArgumentException if the command does not contain a valid
     *                                  {@code /by} delimiter
     */
    private static String[] parseArgs(String args) {
        String command = args.trim();
        String descriptionAndDeadline = command.substring("deadline".length()).trim();
        String[] argList = descriptionAndDeadline.split("(?i)\\s+/by(?:\\s+|$)", 2);

        if (argList.length < 2) {
            throw new IllegalArgumentException("A deadline must use the format: deadline description /by date.");
        }

        argList[0] = argList[0].trim();
        argList[1] = argList[1].trim();
        if (argList[0].isEmpty()) {
            throw new IllegalArgumentException("The description of a deadline cannot be empty.");
        }
        if (argList[1].isEmpty()) {
            throw new IllegalArgumentException("The date of a deadline cannot be empty.");
        }
        return argList;
    }

    /** Returns this deadline in display format, including its due date and optional time. */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + deadline + ")";
    }
}
