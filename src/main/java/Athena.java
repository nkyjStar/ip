import java.util.Scanner;

/** Runs the Athena command-line task manager. */
public class Athena {
    private static final String BANNER = "    _    _   _                    \n"
            + "   / \\  | |_| |__   ___ _ __   __ _ \n"
            + "  / _ \\ | __| '_ \\ / _ \\ '_ \\ / _` |\n"
            + " / ___ \\| |_| | | |  __/ | | | (_| |\n"
            + "/_/   \\_\\\\__|_| |_|\\___|_| |_|\\__,_|\n";
    private static final String PAGE_BREAK = "____________________________________________________________";
    private static final Task[] tasks = new Task[100];
    private static int taskCount = 0;

    /** Formats and prints Athena's messages. */
    private static class Output {
        // Send greeting message
        private static void greeting() {
            System.out.println(PAGE_BREAK);
            System.out.println(BANNER);
            System.out.println("I have answered your summons. The name is Athena.");
            System.out.println("For what purpose have you called upon me?");
            System.out.println(PAGE_BREAK);
        }

        // Send farewell message
        private static void farewell() {
            System.out.println("  I shall take my leave.");
            System.out.println(PAGE_BREAK);
        }

        // Output message
        private static void println(String message) {
            System.out.print("  ");
            System.out.println(message);
            System.out.println(PAGE_BREAK);
        }

        // Print list
        private static void printList() {
            System.out.println("  Here are the tasks in your list:");
            for (int i = 0; i < taskCount; i++) {
                System.out.printf("  %d.%s%n", i + 1, tasks[i]);
            }
            System.out.println(PAGE_BREAK);
        }
    }

    // Get input from user using Scanner
    private static String getInput(Scanner in) {
        // Echo user input
        System.out.println();
        String line = in.nextLine();
        System.out.println(PAGE_BREAK);

        return line;
    }

    // Echoes user input
    private static boolean parseInput(Scanner in) {
        String line = getInput(in);

        // Check if user typed bye
        if (line.equalsIgnoreCase("bye")) {
            return false;
        }

        // Parse input and invoke respective function
        if (line.equalsIgnoreCase("list")) {
            // Print list
            Output.printList();
        } else if (line.toLowerCase().startsWith("mark ")) {
            // Mark task
            changeTaskStatus(line, true);
        } else if (line.toLowerCase().startsWith("unmark ")) {
            // Unmark task
            changeTaskStatus(line, false);
        } else if (taskCount >= tasks.length) {
            Output.println("There is insufficient space in your list!");
        } else {
            if (addTask(line))
                Output.println(
                        "Noted. I have added this task:\n    " + tasks[taskCount - 1] + "\n  Now you have " + taskCount
                                + " tasks in the list");
        }

        return true;
    }

    private static boolean addTask(String line) {
        String formattedLine = line.trim().toLowerCase();

        if (formattedLine.equals("todo") || formattedLine.startsWith("todo ")) {
            String description = line.trim().substring("todo".length()).trim();
            tasks[taskCount++] = new Todo(description);
        } else if (formattedLine.startsWith("deadline")) {
            tasks[taskCount++] = new Deadline(line);
        } else if (formattedLine.startsWith("event")) {
            tasks[taskCount++] = new Event(line);
        } else {
            Output.println("Incorrect task type!");
            return false;
        }

        return true;
    }

    // Changes the completion status of the task identified by its one-based number.
    private static void changeTaskStatus(String line, boolean done) {
        String command = done ? "mark" : "unmark";
        String numberText = line.substring(command.length()).trim();
        try {
            int taskNumber = Integer.parseInt(numberText);
            if (taskNumber < 1 || taskNumber > taskCount) {
                Output.println("Task number must be between 1 and " + taskCount + ".");
                return;
            }

            int taskIndex = taskNumber - 1;
            Task task = tasks[taskIndex];
            if (done) {
                task.markAsDone();
                Output.println("Nice! I've marked this task as done:\n  " + task);
            } else {
                task.markAsNotDone();
                Output.println("OK, I've marked this task as not done yet:\n  " + task);
            }
        } catch (NumberFormatException exception) {
            Output.println("Please provide a valid task number after " + command + ".");
        }
    }

    /** Starts Athena and processes commands until the user says goodbye. */
    public static void main(String[] args) {
        Output.greeting();

        // Create input scanner
        Scanner in = new Scanner(System.in);

        // Keep querying input till "bye"
        while (parseInput(in)) {
            // Continue querying input
        }

        Output.farewell();
        in.close();
    }
}
