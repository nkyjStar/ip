import java.util.Scanner;

public class Athena {
    private static final String BANNER = "    _    _   _                    \n"
            + "   / \\  | |_| |__   ___ _ __   __ _ \n"
            + "  / _ \\ | __| '_ \\ / _ \\ '_ \\ / _` |\n"
            + " / ___ \\| |_| | | |  __/ | | | (_| |\n"
            + "/_/   \\_\\\\__|_| |_|\\___|_| |_|\\__,_|\n";
    private static final String PAGE_BREAK = "____________________________________________________________";
    private static final String[] inputList = new String[100];
    private static int inputListSize = 0;

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
            System.out.println("\tI shall take my leave.");
            System.out.println(PAGE_BREAK);
        }

        // Output message
        private static void println(String message) {
            System.out.print("\t");
            System.out.println(message);
            System.out.println(PAGE_BREAK);
        }

        // Print list
        private static void printList() {
            System.out.println("\tYou have added...");
            for (int i = 0; i < inputListSize; i++) {
                System.out.printf("\t%d. %s%n", i + 1, inputList[i]);
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
            Output.printList();
        } else if (line.isBlank()) {
            Output.println("Please enter a non-empty task.");
        } else if (inputListSize >= inputList.length) {
            Output.println("There is insufficient space in your list!");
        } else {
            inputList[inputListSize++] = line;
            Output.println("Added: " + line);
        }

        return true;
    }

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
