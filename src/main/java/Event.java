public class Event extends Task {
    private static String start;
    private static String end;

    public Event(String args) {
        String[] argList = parseArgs(args);
        super(argList[0]);
        start = argList[1];
        end = argList[2];
    }

    /**
     * Split input string by delimiter and return resultant list
     * List Items: [description, from, to]
     */
    private static String[] parseArgs(String args) {
        String[] argList = args.splitWithDelimiters(args, '/');
        argList[0].trim();
        argList[1].replace("from ", "").trim();
        argList[2].replace("to ", "").trim();
        return argList;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + start + " to: " + end + ")";
    }
}
