package athena.task;

import java.time.LocalDate;

/**
 * Represents a task that occurs between a specified start and end date or time.
 */
public class Event extends Task {
    private final TaskDateTime start;
    private final TaskDateTime end;

    /** Creates an event from separately stored date and optional time fields. */
    public Event(String description, String start, String end) {
        this(description, TaskDateTime.parse(start), TaskDateTime.parse(end));
    }

    /** Creates an event with parsed start and end values. */
    public Event(String description, TaskDateTime start, TaskDateTime end) {
        super(description);
        if (start == null || end == null) {
            throw new IllegalArgumentException("An event must have both a start and an end date.");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("An event cannot end before it starts.");
        }
        this.start = start;
        this.end = end;
    }

    /** Returns the event's parsed start value. */
    public TaskDateTime getStart() {
        return start;
    }

    /** Returns the event's parsed end value. */
    public TaskDateTime getEnd() {
        return end;
    }

    /** Returns whether this event includes the given date. */
    public boolean occursOn(LocalDate date) {
        return !date.isBefore(start.getDate()) && !date.isAfter(end.getDate());
    }

    /**
     * Creates an event from a command in the form
     * {@code event description /from start /to end}.
     */
    public Event(String args) {
        String[] argList = parseArgs(args);
        super(argList[0]);
        start = TaskDateTime.parse(argList[1]);
        end = TaskDateTime.parse(argList[2]);
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("An event cannot end before it starts.");
        }
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
