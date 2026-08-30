/** Represents a task that must be completed by a specified date or time. */
public class Deadline extends Task {
    private final String deadline;

    /** Creates a deadline from a command in the form {@code deadline description /by date}. */
    public Deadline(String args) {
        String[] argList = parseArgs(args);
        super(argList[0]);
        deadline = argList[1];
    }

    /**
     * Splits a deadline command into its description and deadline.
     *
     * @param args the complete deadline command
     * @return an array containing the description and deadline
     * @throws IllegalArgumentException if the command does not contain a valid {@code /by} delimiter
     */
    private static String[] parseArgs(String args) {
        String command = args.trim();
        String descriptionAndDeadline = command.substring("deadline".length()).trim();
        String[] argList = descriptionAndDeadline.split("/", 2);

        if (argList.length < 2 || !argList[1].trim().toLowerCase().startsWith("by ")) {
            throw new IllegalArgumentException("A deadline must use the format: deadline description /by date.");
        }

        argList[0] = argList[0].trim();
        argList[1] = argList[1].trim().substring("by".length()).trim();
        return argList;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + deadline + ")";
    }
}
