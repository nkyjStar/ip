import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import athena.io.Output;
import athena.task.Task;
import athena.task.TaskList;
import athena.task.Todo;

/** Checks terminal wrapping without changing task descriptions. */
public class OutputWrappingTest {
    /** Exercises short lines, boundary lengths, long words, indentation, and every list view. */
    public static void main(String[] args) {
        Output output = new Output();
        String separator = "_".repeat(70) + "\n";
        check(capture(() -> output.println("Short message")).equals("  Short message\n" + separator),
                "Short messages should retain their layout");
        check(capture(() -> output.println("a".repeat(68))).equals("  " + "a".repeat(68) + "\n" + separator),
                "A line that fits exactly should not wrap");
        check(capture(() -> output.println("a".repeat(69))).equals(
                "  " + "a".repeat(68) + "\n  a\n" + separator), "Long words must wrap within the boundary");
        check(capture(() -> output.println("a".repeat(65) + " word")).equals(
                "  " + "a".repeat(65) + "\n  word\n" + separator), "Wrapping should prefer word boundaries");
        String multiline = capture(() -> output.println("Header\n    " + "b".repeat(67) + "\n\nEnd"));
        check(multiline.equals("  Header\n    " + "b".repeat(66) + "\n    b\n\nEnd\n" + separator),
                "Explicit line breaks and indentation must be preserved");
        Task task = new Todo("A long description with words ".repeat(8));
        TaskList tasks = new TaskList(List.of(task));
        String list = capture(() -> output.printList(tasks));
        String matches = capture(() -> output.printMatchingTasks(List.of(task)));
        String onDate = capture(() -> output.printTasksOn(LocalDate.of(2026, 10, 5), List.of(task)));
        for (String printed : List.of(list, matches, onDate)) {
            check(printed.lines().allMatch(line -> line.length() <= 70), "List lines must fit the separator");
            check(printed.contains("1.[T][ ]"), "Task numbering and status must remain visible");
        }
        check(task.getDescription().equals("A long description with words ".repeat(8)),
                "Wrapping must not change the stored description");
        System.out.println("PASS: message and list wrapping at 70 characters");
    }

    /** Captures printed output and normalizes platform line endings for comparisons. */
    private static String capture(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream captured = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setOut(captured);
            action.run();
        } finally {
            System.setOut(original);
        }
        return bytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    /** Fails the test if an output requirement is not met. */
    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
