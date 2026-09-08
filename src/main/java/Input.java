import java.util.Scanner;
import java.util.function.Consumer;

/** Reads commands from the user. */
public class Input {
    private final Scanner scanner;

    /** Creates an input reader that reads from standard input. */
    public Input() {
        scanner = new Scanner(System.in);
    }

    /** Returns whether another input line is available. */
    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }

    /** Reads and returns the next input line. */
    public String nextLine() {
        return scanner.nextLine();
    }

    /** Reads and processes one command, returning false when the user says goodbye. */
    public boolean parseInput(Output output, Consumer<String> commandHandler) {
        output.prepareForInput();
        String line = nextLine();
        output.finishInput();

        if (line.equalsIgnoreCase("bye")) {
            return false;
        }

        commandHandler.accept(line);
        return true;
    }

    /** Closes the underlying input scanner. */
    public void close() {
        scanner.close();
    }
}
