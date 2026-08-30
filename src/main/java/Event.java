/** Represents a task that occurs between a specified start and end date or time. */
public class Event extends Task {
    private final String start;
    private final String end;

    /** Creates an event from a command in the form {@code event description /from start /to end}. */
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
     * @throws IllegalArgumentException if the command does not contain valid {@code /from} and {@code /to} parts
     */
    private static String[] parseArgs(String args) {
        String command = args.trim();
        String descriptionAndTimes = command.substring("event".length()).trim();
        String[] argList = descriptionAndTimes.split("/", 3);

        if (argList.length < 3 || !argList[1].trim().toLowerCase().startsWith("from ")
                || !argList[2].trim().toLowerCase().startsWith("to ")) {
            throw new IllegalArgumentException("An event must use the format: event description /from start /to end.");
        }

        argList[0] = argList[0].trim();
        argList[1] = argList[1].trim().substring("from".length()).trim();
        argList[2] = argList[2].trim().substring("to".length()).trim();
        return argList;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + start + " to: " + end + ")";
    }
}
