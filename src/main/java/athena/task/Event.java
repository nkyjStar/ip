package athena.task;

/**
 * Represents a task that occurs between a specified start and end date or time.
 */
public class Event extends Task {
    private final String start;
    private final String end;

    /** Creates an event from separately stored fields, without interpreting command delimiters. */
    public Event(String description, String start, String end) {
        super(description);
        if (start == null || start.isBlank() || end == null || end.isBlank()) {
            throw new IllegalArgumentException("An event must have both a start and an end time.");
        }
        this.start = start;
        this.end = end;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    /**
     * Creates an event from a command in the form
     * {@code event description /from start /to end}.
     */
    public Event(String args) {
        String[] argList = parseArgs(args);
        super(argList[0]);
        start = argList[1];
        end = argList[2];
    }

    /**
     * Splits an event command into its description, start, and end.
     *
     * @param args the complete event command
     * @return an array containing the description, start, and end
     * @throws IllegalArgumentException if the command does not contain valid
     *                                  {@code /from} and {@code /to} parts
     */
    private static String[] parseArgs(String args) {
        String command = args.trim();
        String descriptionAndTimes = command.substring("event".length()).trim();
        String[] descriptionAndRange = descriptionAndTimes.split("(?i)\\s+/from(?:\\s+|$)", 2);
        if (descriptionAndRange.length < 2) {
            throw new IllegalArgumentException("An event must use the format: event description /from start /to end.");
        }
        String[] range = descriptionAndRange[1].split("(?i)\\s+/to(?:\\s+|$)", 2);
        if (range.length < 2) {
            throw new IllegalArgumentException("An event must use the format: event description /from start /to end.");
        }
        String[] argList = {descriptionAndRange[0].trim(), range[0].trim(), range[1].trim()};
        if (argList[0].isEmpty()) {
            throw new IllegalArgumentException("The description of an event cannot be empty.");
        }
        if (argList[1].isEmpty() || argList[2].isEmpty()) {
            throw new IllegalArgumentException("An event must have both a start and an end time.");
        }
        return argList;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + start + " to: " + end + ")";
    }
}
