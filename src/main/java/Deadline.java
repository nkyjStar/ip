public class Deadline extends Task {
    private String deadline;

    public Deadline(String args) {
        String[] argList = parseArgs(args);
        super(argList[0]);
        deadline = argList[1];
    }

    /**
     * Split input String by '/' delimiter and return resultant arrray
     * 
     * @param args
     * @return [description, deadline]
     */
    private static String[] parseArgs(String args) {
        String[] argList = args.splitWithDelimiters(args, '/');
        argList[1].replace("by ", "").trim();
        argList[0].trim();
        return argList;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + deadline + ")";
    }
}