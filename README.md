# Athena project

This is a project template for a greenfield Java project named _Athena_. Given below are instructions on how to use it.

Tasks are loaded from `data/athena.txt` at startup and saved after successful add,
mark, and unmark changes. The path is relative to the working directory and built
with `Path.of("data", "athena.txt")` so Java uses the operating system's path
separator. Run from the project root. Missing or empty files start an empty list;
the first successful task addition creates the data directory and file if needed.
UTF-8 BOMs and blank lines are accepted. Invalid records, invalid
UTF-8, unreadable files, and more than 100 tasks stop startup without changing the
file; errors identify the offending line when possible. Files are limited to 1 MiB.

New saves use a version header `ATHENA\t1`, followed by tab-separated task type,
completion flag (`0` or `1`), description, and any date fields. Backslashes, tabs,
newlines, and carriage returns are escaped as `\\`, `\t`, `\n`, and `\r`.
The older display-format files still load and migrate on the next successful
change. That older format cannot distinguish date fields containing its own
` (from: `, ` to: `, or ` (by: ` delimiters; check such legacy entries manually.

Saving writes and flushes a temporary file in the same directory, then atomically
replaces the original. If this fails (including unsupported atomic replacement),
the original file is preserved and the task change is reverted. Detected external
file edits are preserved and require restarting before further changes. The save
file and its data directory must not be symbolic links. These checks do not lock
out unrelated programs editing the file concurrently; use one Athena session at
a time. Storage failures such as permission denial or a full disk are reported.

Run `python test/run-ui-tests.py` with Java 25 for isolated UI, restart, and
storage-failure tests. No real task data is used by the tests.

## Setting up in Intellij

Prerequisites: JDK 25, update Intellij to the most recent version.

1. Open Intellij (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into Intellij as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, locate the `src/main/java/Athena.java` file, right-click it, and choose `Run Athena.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see the following output:

   ```
       _    _   _
      / \  | |_| |__   ___ _ __   __ _
     / _ \ | __| '_ \ / _ \ '_ \ / _` |
    / ___ \| |_| | | |  __/ | | | (_| |
   /_/   \_\\__|_| |_|\___|_| |_|\__,_|

   ```

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.
