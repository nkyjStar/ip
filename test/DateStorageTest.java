import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import athena.io.Storage;
import athena.task.Deadline;
import athena.task.Event;
import athena.task.Task;
import athena.task.TaskDateTime;
import athena.task.TaskList;

/** Checks date-time persistence using isolated temporary save files. */
public class DateStorageTest {
    /** Verifies round trips and migration of the earlier comma-separated storage format. */
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("athena-date-storage-");
        Path path = directory.resolve("athena.txt");
        Storage storage = new Storage(path);
        storage.load(100);
        TaskList tasks = new TaskList();
        tasks.add(new Deadline("Timed deadline", TaskDateTime.parse("5/10/2026 18:00")));
        tasks.add(new Event("Overnight event", TaskDateTime.parse("2026-10-05 2330"),
                TaskDateTime.parse("2026-10-06 0000")));
        tasks.add(new Deadline("Date only", TaskDateTime.parse("2026-10-07")));
        tasks.get(0).markAsDone();
        storage.save(tasks);
        String canonical = Files.readString(path);
        check(canonical.contains("2026-10-05 1800"), "Timed deadline must use a space separator");
        check(!canonical.contains(","), "Canonical storage must not contain date-time commas");
        verifyTasks(tasks, new Storage(path).load(100));
        check(Files.readString(path).equals(canonical), "Loading must not modify the save file");

        String previousFormat = canonical.replaceAll("(\\d{4}-\\d{2}-\\d{2}) (\\d{4})", "$1, $2");
        Files.writeString(path, previousFormat);
        Storage previousStorage = new Storage(path);
        List<Task> loaded = previousStorage.load(100);
        verifyTasks(tasks, loaded);
        check(Files.readString(path).equals(previousFormat), "Loading old saves must preserve their bytes");
        previousStorage.save(new TaskList(loaded));
        check(Files.readString(path).equals(canonical), "Saving old tasks must migrate their date-time format");
        verifyTasks(tasks, new Storage(path).load(100));

        Files.writeString(path, "ATHENA\t1\nD\t0\tInvalid time\t2026-10-05, 2500\n");
        boolean rejected = false;
        try {
            new Storage(path).load(100);
        } catch (java.io.IOException exception) {
            rejected = true;
        }
        check(rejected, "Invalid stored times must still be rejected");
        System.out.println("PASS: date-time round trips, old-save migration, and invalid-time rejection");
    }

    /** Checks that reloaded tasks preserve their order, descriptions, statuses, and displayed dates. */
    private static void verifyTasks(TaskList expected, List<Task> actual) {
        check(expected.size() == actual.size(), "Task count changed after loading");
        for (int i = 0; i < expected.size(); i++) {
            check(expected.get(i).toString().equals(actual.get(i).toString()), "Task changed at index " + i);
        }
    }

    /** Fails the regression test when a required condition is not met. */
    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
