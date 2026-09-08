import java.util.Locale;

/** Runs the Athena command-line task manager. */
public class Athena {
    private static final int MAX_TASKS = 100;
    private final Task[] tasks = new Task[MAX_TASKS];
    private int taskCount = 0;

    private final Output output = new Output();

    /**
     * Interprets a command and invokes the corresponding task-manager operation.
     */
    private void handleCommand(String line) {
        String lowerCaseLine = line.toLowerCase(Locale.ROOT);

        if (line.equalsIgnoreCase("list")) {
            output.printList(tasks, taskCount);
        } else if (lowerCaseLine.startsWith("mark ")) {
            Task.changeTaskStatus(tasks, taskCount, line, true, output);
        } else if (lowerCaseLine.startsWith("unmark ")) {
            Task.changeTaskStatus(tasks, taskCount, line, false, output);
        } else if (taskCount >= tasks.length) {
            output.println(
                    "Perhaps one should first fulfill their responsibilities before adding more beyond their current limit.");
        } else if (addTask(line)) {
            output.println(
                    "Noted. I have added this task:\n    " + tasks[taskCount - 1] + "\n  Now you have " + taskCount
                            + " tasks in the list");
        }
    }

    private boolean addTask(String line) {
        String formattedLine = line.trim().toLowerCase();

        try {
            if (formattedLine.equals("todo") || formattedLine.startsWith("todo ")) {
                String description = line.trim().substring("todo".length()).trim();
                tasks[taskCount++] = new Todo(description);
            } else if (formattedLine.equals("deadline") || formattedLine.startsWith("deadline ")) {
                Task deadline = new Deadline(line);
                tasks[taskCount++] = deadline;
            } else if (formattedLine.equals("event") || formattedLine.startsWith("event ")) {
                Task event = new Event(line);
                tasks[taskCount++] = event;
            } else {
                output.println(
                        "Such insolence! It is rare for one to witness humans spout such nonsense in the presence of the goddess of wisdom.");
                return false;
            }
        } catch (IllegalArgumentException exception) {
            output.println(exception.getMessage());
            return false;
        }

        return true;
    }

    /** Starts Athena and processes commands until the user says goodbye. */
    public static void main(String[] args) {
        Athena athena = new Athena();
        athena.output.greeting();

        // Create input scanner
        Input input = new Input();

        // Keep querying input till "bye"
        while (input.hasNextLine() && input.parseInput(athena.output, athena::handleCommand)) {
            // Continue querying input
        }

        athena.output.farewell();
        input.close();
    }
}
