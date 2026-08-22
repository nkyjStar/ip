import java.util.Scanner;

public class Athena {
    private static String banner = "    _    _   _                    \n"
            + "   / \\  | |_| |__   ___ _ __   __ _ \n"
            + "  / _ \\ | __| '_ \\ / _ \\ '_ \\ / _` |\n"
            + " / ___ \\| |_| | | |  __/ | | | (_| |\n"
            + "/_/   \\_\\\\__|_| |_|\\___|_| |_|\\__,_|\n";
    private static String pageBreak = "____________________________________________________________";

    private static class out {
        // Send greeting message
        private static void greeting() {
            System.out.println(pageBreak);
            System.out.println(banner);
            System.out.println("I have answered your summons. The name is Athena.");
            System.out.println("For what purpose have you called upon me?");
            System.out.println(pageBreak);
        }

        // Send farewell message
        private static void farewell() {
            System.out.println("\tI shall take my leave.");
            System.out.println(pageBreak);
        }

        // Output message
        private static void println(String message) {
            System.out.print("\t");
            System.out.println(message);
            System.out.println(pageBreak);
        }
    }

    // Get input from user using Scanner
    private static String getInput(Scanner in) {
        // Echo user input
        System.out.println();
        String line = in.nextLine();
        System.out.println(pageBreak);

        return line;
    }

    // Echoes user input
    private static boolean isSessionEnd(Scanner in) {
        String line = getInput(in);

        // Check if user typed bye
        if (line.equalsIgnoreCase("bye")) {
            return true;
        }

        // Parse input and invoke respective function
        if (line.equalsIgnoreCase("list")) {

        }

        return false;
    }

    public static void main(String[] args) {
        out.greeting();

        // Create input scanner
        Scanner in = new Scanner(System.in);

        // Keep querying input till "bye"
        while (!isSessionEnd(in))
            ;

        out.farewell();
        in.close();
    }
}
