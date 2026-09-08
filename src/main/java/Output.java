/** Formats and prints Athena's messages. */
public class Output {
    private static final String BANNER = "    _    _   _                    \n"
            + "   / \\  | |_| |__   ___ _ __   __ _ \n"
            + "  / _ \\ | __| '_ \\ / _ \\ '_ \\ / _` |\n"
            + " / ___ \\| |_| | | |  __/ | | | (_| |\n"
            + "/_/   \\_\\\\__|_| |_|\\___|_| |_|\\__,_|\n";
    private static final String PAGE_BREAK = "____________________________________________________________";

    /** Prints Athena's greeting. */
    public void greeting() {
        System.out.println(PAGE_BREAK);
        System.out.println(BANNER);
        System.out.println("I have answered your summons. The name is Athena.");
        System.out.println("For what purpose have you called upon me?");
        System.out.println(PAGE_BREAK);
    }

    /** Prints Athena's farewell. */
    public void farewell() {
        System.out.println("  I shall take my leave.");
        System.out.println(PAGE_BREAK);
    }

    /** Prints the separator shown before waiting for the next command. */
    public void prepareForInput() {
        System.out.println();
    }

    /** Prints the separator shown after a command has been read. */
    public void finishInput() {
        System.out.println(PAGE_BREAK);
    }

    /** Prints a formatted message followed by a page break. */
    public void println(String message) {
        System.out.print("  ");
        System.out.println(message);
        System.out.println(PAGE_BREAK);
    }

    /** Prints all tasks in the supplied task list. */
    public void printList(Task[] tasks, int taskCount) {
        System.out.println("  Here are the tasks in your list:");
        for (int i = 0; i < taskCount; i++) {
            System.out.printf("  %d.%s%n", i + 1, tasks[i]);
        }
        System.out.println(PAGE_BREAK);
    }
}
