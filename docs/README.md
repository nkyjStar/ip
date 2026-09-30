# Athena User Guide

Athena is a command-line chatbot that helps you keep track of todos, deadlines, and events.
Type one command at a time and press Enter. You can keep up to 100 tasks.

## Getting started

1. Install JDK 25. Check that `java -version` and `javac -version` both show version 25.
2. Build `athena.jar` using the [build instructions](#building-athena).
3. From the project folder, run `java -jar build/libs/athena.jar`.
4. Try `todo Read a book`, then `list`. Enter `bye` when you are finished.

### Building Athena

Download or clone the project, then open a terminal in the project folder (the folder containing
`build.gradle`). With JDK 25 installed, run:

On Windows (PowerShell):

```powershell
.\gradlew.bat shadowJar
```

On macOS or Linux:

```sh
sh ./gradlew shadowJar
```

The first build needs internet access to download build tools. When the build succeeds, the
application is available at `build/libs/athena.jar`.

## Command format

- Words in `UPPER_CASE` are values you supply: `todo DESCRIPTION` becomes `todo Read a book`.
- Square brackets indicate optional input: `DATE [TIME]` accepts a date alone or a date and time.
  Do not type the brackets.
- Command words are case-insensitive: `LIST` and `list` work the same way.
- **Use the parameter order shown.** For example, an event requires `/from` before `/to`.
- **Use `list` and `bye` without extra arguments.** Extra arguments are rejected, not ignored.
  Athena does not have `help`, `exit`, or `clear` commands.
- Descriptions can contain spaces and must not be empty. Do not surround them with quotation marks.

### Dates and times

Use `d/M/yyyy` or `yyyy-MM-dd`, for example `5/10/2026` or `2026-10-05`.
An optional 24-hour time follows a space: `2026-10-05 1800` or `5/10/2026 18:00`.
Dates must be valid calendar dates; words such as `tomorrow` are not supported.

## Adding a todo: `todo`

Adds a task without a date.

Format: `todo DESCRIPTION`

Example: `todo Read a book` adds an unfinished task named “Read a book”.

## Adding a deadline: `deadline`

Adds a task that is due on a particular date, optionally at a particular time.

Format: `deadline DESCRIPTION /by DATE [TIME]`

Both the description and the date are required.

Examples:

- `deadline Submit assignment /by 5/10/2026` adds a task due on October 5, 2026.
- `deadline Submit assignment /by 5/10/2026 1800` adds a task due on October 5, 2026 at 6:00 PM.

## Adding an event: `event`

Adds a task with a start and an end.

Format: `event DESCRIPTION /from START_DATE [START_TIME] /to END_DATE [END_TIME]`

- Supply both dates, even for a one-day event.
- The end must not be before the start. When both times are supplied on the same date, the end time
  must not be earlier than the start time.

Example: `event Study camp /from 5/10/2026 /to 7/10/2026` adds an event spanning October 5–7.

## Viewing all tasks: `list`

Shows all tasks, including completed ones, in their current order.

Format and example: `list`

Example task display:

```text
1.[T][ ] Read a book
2.[D][X] Submit assignment (by: Oct 05 2026)
```

`[T]`, `[D]`, and `[E]` mean todo, deadline, and event. `[X]` means completed; `[ ]` means unfinished.

## Marking a task: `mark` / `unmark`

Marks a task as completed or changes it back to unfinished.

Formats: `mark INDEX` and `unmark INDEX`

- `INDEX` is a positive whole number from the full `list`, starting at 1.
- Run `list` before choosing an index: numbers in `find` and `on` results are local to those results
  and may refer to different tasks.

Examples:

- `mark 1` marks the first task in the full list as completed.
- `unmark 1` marks that task as unfinished again.

## Finding tasks: `find`

Shows tasks whose descriptions contain the supplied text, ignoring case. Completed tasks are included.

Format: `find KEYWORD`

Example: `find book` matches “Read a book” and “Book a room”.
You can supply a phrase such as `find read a book`; the whole phrase must occur in the description.

## Viewing tasks on a date: `on`

Shows deadlines due on the date and events spanning that date, including their start and end dates.
Completed tasks are included; todos are not.

Format: `on DATE`

Example: `on 6/10/2026` includes the study camp above. Supply a date without a time.

## Deleting a task: `delete`

Removes a task from the full list.

Format: `delete INDEX`

Example: `delete 1` removes the first task shown by `list`.
The remaining tasks are renumbered. There is no undo command, so check the full list first.

## Exiting: `bye`

Closes Athena.

Format and example: `bye`

## Saving your tasks and handling errors

Athena automatically saves successful additions, status changes, and deletions to `data/athena.txt`
inside the folder you launch it from. It loads that file at startup. Always launch from the same
folder to use the same task list; a missing file starts an empty list.

- Back up the `data` folder while Athena is closed. Use one Athena session at a time.
- If a save fails, Athena reports the error and rolls back the attempted change. Check folder
  permissions and available disk space before retrying.
- If the file changes outside Athena, restart before making more changes.
- If loading fails, Athena exits without overwriting the file. Keep a backup and check the reported
  error or restore a known-good copy.
- For an invalid command, check the format in this guide and try again. If the list is full,
  delete an unneeded task before adding another.
