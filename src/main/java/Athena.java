import java.util.Scanner;

public class Athena {
    private static String banner = "    _    _   _                    \n"
            + "   / \\  | |_| |__   ___ _ __   __ _ \n"
            + "  / _ \\ | __| '_ \\ / _ \\ '_ \\ / _` |\n"
            + " / ___ \\| |_| | | |  __/ | | | (_| |\n"
            + "/_/   \\_\\\\__|_| |_|\\___|_| |_|\\__,_|\n";
    private static String pageBreak = "____________________________________________________________";

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

    // Echoes user input
    private static boolean isEchoInput(Scanner in) {
        // Echo user input
        System.out.println();
        String line = in.nextLine();
        System.out.println(pageBreak);

        // Check if user typed bye
        if (line.equalsIgnoreCase("bye")) {
            return false;
        }

        System.out.print("\t");
        System.out.println(line);
        System.out.println(pageBreak);
        return true;
    }

    public static void main(String[] args) {
        greeting();

        // Create input scanner
        Scanner in = new Scanner(System.in);

        while (isEchoInput(in))
            ;

        farewell();
        in.close();
    }
}
