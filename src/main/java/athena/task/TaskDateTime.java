package athena.task;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;

/** Represents a parsed calendar date with an optional time. */
public final class TaskDateTime {
    private static final String INVALID_FORMAT_MESSAGE = "Dates and times must use d/M/yyyy or yyyy-MM-dd, "
            + "optionally followed by HHmm or H:mm.";
    private static final DateTimeFormatter SLASH_DATE_FORMAT = new DateTimeFormatterBuilder()
            .parseStrict()
            .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
            .appendLiteral('/')
            .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE)
            .appendLiteral('/')
            .appendValue(ChronoField.YEAR, 4)
            .toFormatter(Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter COMPACT_TIME_FORMAT = new DateTimeFormatterBuilder()
            .parseStrict()
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .toFormatter(Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter COLON_TIME_FORMAT = new DateTimeFormatterBuilder()
            .parseStrict()
            .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .toFormatter(Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter STORAGE_TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");

    private final LocalDate date;
    private final LocalTime time;

    /** Creates a date value with an optional time; null represents no supplied time. */
    public TaskDateTime(LocalDate date, LocalTime time) {
        if (date == null) {
            throw new IllegalArgumentException("A task date cannot be empty.");
        }
        this.date = date;
        this.time = time;
    }

    /** Parses a supported date with an optional time. */
    public static TaskDateTime parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(INVALID_FORMAT_MESSAGE);
        }
        String[] parts = value.trim().split("\\s+");
        if (parts.length > 2) {
            throw new IllegalArgumentException(INVALID_FORMAT_MESSAGE);
        }
        LocalDate date = parseDate(parts[0]);
        LocalTime time = parts.length == 2 ? parseTime(parts[1]) : null;
        return new TaskDateTime(date, time);
    }

    /** Parses a date in either supported input format. */
    public static LocalDate parseDate(String value) {
        if (value == null || value.isBlank() || value.trim().contains(" ")) {
            throw new IllegalArgumentException(INVALID_FORMAT_MESSAGE);
        }
        String date = value.trim();
        try {
            return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException isoException) {
            try {
                return LocalDate.parse(date, SLASH_DATE_FORMAT);
            } catch (DateTimeParseException slashException) {
                throw new IllegalArgumentException(INVALID_FORMAT_MESSAGE, slashException);
            }
        }
    }

    /** Parses a 24-hour time in compact or colon-separated form. */
    private static LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value, COMPACT_TIME_FORMAT);
        } catch (DateTimeParseException compactException) {
            try {
                return LocalTime.parse(value, COLON_TIME_FORMAT);
            } catch (DateTimeParseException colonException) {
                throw new IllegalArgumentException(INVALID_FORMAT_MESSAGE, colonException);
            }
        }
    }

    /** Returns the calendar date. */
    public LocalDate getDate() {
        return date;
    }

    /** Returns whether this value includes a time. */
    public boolean hasTime() {
        return time != null;
    }

    /** Returns whether this value is unambiguously after the other value. */
    public boolean isAfter(TaskDateTime other) {
        if (!date.equals(other.date)) {
            return date.isAfter(other.date);
        }
        return time != null && other.time != null && time.isAfter(other.time);
    }

    /** Returns a canonical representation suitable for storage. */
    public String toStorageString() {
        return date + (hasTime() ? ", " + time.format(STORAGE_TIME_FORMAT) : "");
    }

    /** Formats a calendar date consistently for user-facing output. */
    public static String formatDate(LocalDate date) {
        return date.format(DISPLAY_DATE_FORMAT);
    }

    /** Returns the date and optional time formatted for display. */
    @Override
    public String toString() {
        return formatDate(date) + (hasTime() ? ", " + time.format(DISPLAY_TIME_FORMAT) : "");
    }
}
