package athena.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** Represents a task that must be completed by a specified date. */
public class Deadline extends Task {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);
    private final LocalDate deadline;

    /** Creates a deadline from separately stored fields using an ISO date. */
    public Deadline(String description, String deadline) {
        this(description, parseDate(deadline));
    }

    /** Creates a deadline with the given description and due date. */
    public Deadline(String description, LocalDate deadline) {
        super(description);
        if (deadline == null) {
            throw new IllegalArgumentException("The date of a deadline cannot be empty.");
        }
        this.deadline = deadline;
    }

    /** Returns the due date represented by this deadline. */
    public LocalDate getDeadline() {
        return deadline;
    }

    /**
     * Creates a deadline from a command in the form
     * {@code deadline description /by date}.
     */
    public Deadline(String args) {
        String[] argList = parseArgs(args);
        super(argList[0]);
        deadline = parseDate(argList[1]);
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

    /** Parses a strict ISO date so impossible calendar dates are rejected. */
    private static LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("The date of a deadline cannot be empty.");
        }
        try {
            return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("The deadline date must use yyyy-MM-dd.", exception);
        }
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + deadline.format(DISPLAY_FORMAT) + ")";
    }
}
