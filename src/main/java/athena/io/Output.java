package athena.io;

import java.time.LocalDate;
import java.util.List;

import athena.task.Task;
import athena.task.TaskDateTime;
import athena.task.TaskList;

/** Formats and prints Athena's messages. */
public class Output {
    private static final String BANNER = "    _    _   _                    \n"
            + "   / \\  | |_| |__   ___ _ __   __ _ \n"
            + "  / _ \\ | __| '_ \\ / _ \\ '_ \\ / _` |\n"
            + " / ___ \\| |_| | | |  __/ | | | (_| |\n"
            + "/_/   \\_\\\\__|_| |_|\\___|_| |_|\\__,_|\n";
    private static final int LINE_WIDTH = 70;
    private static final String PAGE_BREAK = "_".repeat(LINE_WIDTH);

    /** Prints text within the separator width, preserving line breaks and leading indentation. */
    private void printWrapped(String text) {
        for (String line : text.replace("\t", "    ").split("\\R", -1)) {
            int leadingSpaces = line.length() - line.stripLeading().length();
            String indent = " ".repeat(Math.min(leadingSpaces, LINE_WIDTH - 1));
            String remaining = line.stripLeading();
            int available = LINE_WIDTH - indent.length();
            while (remaining.length() > available) {
                int splitAt = remaining.lastIndexOf(' ', available);
                if (splitAt <= 0) {
                    splitAt = available;
                }
                System.out.println(indent + remaining.substring(0, splitAt));
                remaining = remaining.substring(splitAt).stripLeading();
            }
            System.out.println(indent + remaining);
        }
    }

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
        printWrapped("  " + message);
        System.out.println(PAGE_BREAK);
    }

    /** Prints all tasks in the supplied task list. */
    public void printList(TaskList tasks) {
        System.out.println("  Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            printWrapped("  " + (i + 1) + "." + tasks.get(i));
        }
        System.out.println(PAGE_BREAK);
    }

    /** Prints tasks matching a search keyword, or reports that none match. */
    public void printMatchingTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            println("There are no matching tasks.");
            return;
        }

        System.out.println("  Here are the matching tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            printWrapped("  " + (i + 1) + "." + tasks.get(i));
        }
        System.out.println(PAGE_BREAK);
    }

    /** Prints deadlines and events occurring on the date, or reports that none occur. */
    public void printTasksOn(LocalDate date, List<Task> tasks) {
        String formattedDate = TaskDateTime.formatDate(date);
        if (tasks.isEmpty()) {
            println("There are no deadlines or events on " + formattedDate + ".");
            return;
        }

        printWrapped("  Here are the deadlines and events on " + formattedDate + ":");
        for (int i = 0; i < tasks.size(); i++) {
            printWrapped("  " + (i + 1) + "." + tasks.get(i));
        }
        System.out.println(PAGE_BREAK);
    }
}
