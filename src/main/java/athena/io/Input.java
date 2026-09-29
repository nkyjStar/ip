package athena.io;

import java.util.Scanner;

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

    /** Reads and returns the next command after displaying the input separators. */
    public String readCommand(Output output) {
        output.prepareForInput();
        String line = scanner.nextLine().trim();
        output.finishInput();
        return line;
    }

    /** Closes the underlying input scanner. */
    public void close() {
        scanner.close();
    }
}
