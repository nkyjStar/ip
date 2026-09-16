package athena.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import athena.task.Deadline;
import athena.task.Event;
import athena.task.Task;
import athena.task.Todo;

/** Validates task files and replaces them atomically so failed saves preserve existing data. */
public class Storage {
    private static final String HEADER = "ATHENA\t1";
    private static final int MAX_BYTES = 1_048_576;
    private static final Pattern LEGACY_TASK = Pattern.compile("\\[([TDE])\\]\\[([ X])\\] (.+)");
    private static final Pattern LEGACY_DEADLINE = Pattern.compile("(.+) \\(by: (.+)\\)");
    private static final Pattern LEGACY_EVENT = Pattern.compile("(.+) \\(from: (.+) to: (.+)\\)");
    private final Path path;
    private byte[] lastSaved;

    /** Uses the supplied path for both loading and saving. */
    public Storage(Path path) {
        this.path = path;
    }

    /** Reads a bounded, regular file; null represents a missing file, not an unreadable file. */
    private byte[] readBytes() throws IOException {
        if (Files.isSymbolicLink(path) || Files.isSymbolicLink(path.getParent())) {
            throw new IOException("The data directory and save file must not be symbolic links.");
        }
        try (var input = Files.newInputStream(path)) {
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("The save path is not a regular file.");
            }
            byte[] bytes = input.readNBytes(MAX_BYTES + 1);
            if (bytes.length > MAX_BYTES) {
                throw new IOException("The save file exceeds the 1 MiB limit.");
            }
            return bytes;
        } catch (NoSuchFileException exception) {
            if (Files.exists(path.getParent()) && !Files.isDirectory(path.getParent())) {
                throw new IOException("The data path is not a directory.", exception);
            }
            return null;
        }
    }

    /** Loads the entire file before returning any tasks; malformed records include their line number. */
    public List<Task> load(int capacity) throws IOException {
        byte[] bytes = readBytes();
        List<Task> tasks = new ArrayList<>();
        if (bytes == null) {
            lastSaved = null;
            return tasks;
        }
        String text = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1);
        }
        List<String> lines = text.lines().toList();
        boolean isStructured = !lines.isEmpty() && lines.getFirst().equals(HEADER);
        for (int i = isStructured ? 1 : 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            try {
                if (tasks.size() == capacity) {
                    throw new IllegalArgumentException("The save file exceeds the limit of " + capacity + " tasks.");
                }
                tasks.add(isStructured ? parseRecord(line) : parseLegacy(line));
            } catch (IllegalArgumentException exception) {
                throw new IOException("Line " + (i + 1) + ": " + exception.getMessage(), exception);
            }
        }
        lastSaved = bytes;
        return tasks;
    }

    /** Parses a tab-separated record whose fields escape backslashes and control characters. */
    private Task parseRecord(String line) {
        String[] fields = line.split("\t", -1);
        if (fields.length < 3 || !(fields[1].equals("0") || fields[1].equals("1"))) {
            throw new IllegalArgumentException("Invalid task fields or completion status.");
        }
        for (int i = 2; i < fields.length; i++) {
            fields[i] = unescape(fields[i]);
        }
        Task task;
        if (fields[0].equals("T") && fields.length == 3) {
            task = new Todo(fields[2]);
        } else if (fields[0].equals("D") && fields.length == 4) {
            task = new Deadline(fields[2], fields[3]);
        } else if (fields[0].equals("E") && fields.length == 5) {
            task = new Event(fields[2], fields[3], fields[4]);
        } else {
            throw new IllegalArgumentException("Invalid task type or field count.");
        }
        if (fields[1].equals("1")) {
            task.markAsDone();
        }
        return task;
    }

    /** Reads the previous display format for compatibility with existing saves. */
    private Task parseLegacy(String line) {
        Matcher record = LEGACY_TASK.matcher(line);
        if (!record.matches()) {
            throw new IllegalArgumentException("Invalid task or unsupported file version.");
        }
        String description = record.group(3);
        Task task;
        if (record.group(1).equals("T")) {
            task = new Todo(description);
        } else if (record.group(1).equals("D")) {
            Matcher date = LEGACY_DEADLINE.matcher(description);
            if (!date.matches()) {
                throw new IllegalArgumentException("Invalid saved deadline.");
            }
            task = new Deadline(date.group(1), date.group(2));
        } else {
            Matcher dates = LEGACY_EVENT.matcher(description);
            if (!dates.matches()) {
                throw new IllegalArgumentException("Invalid saved event.");
            }
            task = new Event(dates.group(1), dates.group(2), dates.group(3));
        }
        if (record.group(2).equals("X")) {
            task.markAsDone();
        }
        return task;
    }

    /** Escapes characters that would otherwise change record boundaries. */
    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r");
    }

    /** Decodes fields strictly, rejecting unknown and unfinished escape sequences. */
    private String unescape(String value) {
        StringBuilder decoded = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character != '\\') {
                decoded.append(character);
                continue;
            }
            if (++i == value.length()) {
                throw new IllegalArgumentException("Unfinished escape sequence.");
            }
            decoded.append(switch (value.charAt(i)) {
                case '\\' -> '\\';
                case 't' -> '\t';
                case 'n' -> '\n';
                case 'r' -> '\r';
                default -> throw new IllegalArgumentException("Unknown escape sequence.");
            });
        }
        return decoded.toString();
    }

    /** Writes a complete temporary file, then atomically replaces the unchanged original. */
    public void save(Task[] tasks, int count) throws IOException {
        StringBuilder text = new StringBuilder(HEADER).append('\n');
        for (int i = 0; i < count; i++) {
            Task task = tasks[i];
            String type = task instanceof Deadline ? "D" : task instanceof Event ? "E" : "T";
            text.append(type).append('\t').append(task.isDone() ? "1" : "0").append('\t');
            text.append(escape(task.getDescription()));
            if (task instanceof Deadline deadline) {
                text.append('\t').append(escape(deadline.getDeadline()));
            } else if (task instanceof Event event) {
                text.append('\t').append(escape(event.getStart())).append('\t').append(escape(event.getEnd()));
            }
            text.append('\n');
        }
        byte[] bytes = text.toString().getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_BYTES) {
            throw new IOException("The save file would exceed the 1 MiB limit.");
        }
        if (!Arrays.equals(readBytes(), lastSaved)) {
            throw new IOException("The save file changed outside Athena. Restart before making changes.");
        }
        Files.createDirectories(path.getParent());
        Path temporary = Files.createTempFile(path.getParent(), "athena-", ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
            }
            if (!Arrays.equals(readBytes(), lastSaved)) {
                throw new IOException("The save file changed outside Athena. Restart before making changes.");
            }
            Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            lastSaved = bytes;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
