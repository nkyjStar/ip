# Athena UI Test Plan

The UI tests exercise Athena through standard input. Each test case starts a
fresh application session. Expected output below focuses on the task-manager
responses and excludes the fixed greeting, page-break separators, and farewell
message. Expected console lines are ordered literal fragments with surrounding
indentation omitted; matching within each fragment is exact.

Run `python test/run-ui-tests.py` with Java 25. Each case uses a fresh temporary
working directory under `_temp`, so it cannot overwrite real task data.
The JSON array in each case gives the exact UTF-8 file contents after each input
command, including `list` and `bye`. `null` means the file must not exist. Only
CRLF versus LF line endings are normalized. Checkpoints are checked while the
application is still running, before sending the next command.
An optional `initial` block contains a JSON string to write as the save file
before startup (otherwise the file is absent). After each case, restart Athena
in the same directory with `list` and `bye`: compare the entire numbered list
with the decoded final saved tasks and verify that startup does not change the file.
The session record includes both runs. New files start with `ATHENA\t1` and use
escaped tab-separated fields; initial legacy files remain unchanged until a
successful mutation. The error scenarios below run separately in isolated
folders. Runtime faults are injected after a `list` response, ensuring startup
has completed. Compare expected fragments in order, reject forbidden fragments,
and require the fixture bytes (or directory) to remain unchanged.

### Packaged JAR smoke test

After building with `shadowJar`, run `java -jar` with the generated
`build/libs/athena.jar` in a fresh temporary working directory, using Java 25.
Aim: Verify the executable manifest, packaged classes, first-run saving, and
loading after restart independently of compiled classes in the project.

First session input: `todo packaged task`, `list`, `bye` (one command per line).
Expected response fragments, in order:

```text
  Noted. I have added this task:
    [T][ ] packaged task
  Now you have 1 tasks in the list
  Here are the tasks in your list:
  1.[T][ ] packaged task
```

After exit, require `data/athena.txt` to contain exactly
`ATHENA\t1\nT\t0\tpackaged task\n` (normalizing CRLF to LF).
Restart the same JAR in the same directory with `list`, `bye`. Require the
complete empty-or-numbered-list response to contain exactly this single task,
and require the saved bytes to remain unchanged. Both sessions must exit with
code zero without stderr. Print and retain the complete input/output record.
Inspect the archive's manifest for `Main-Class: athena.Athena` and verify all
application classes are included; local task data must not be packaged.

Storage error scenarios (JSON fixtures):

```errors
[
  {
    "name": "invalid type",
    "initial": "[Q][ ] bad\n",
    "expected": [
      "Could not load tasks:"
    ],
    "forbidden": [
      "Here are the tasks",
      "Noted. I have added"
    ]
  },
  {
    "name": "invalid status",
    "initial": "ATHENA\t1\nT\t2\tbad\n",
    "expected": [
      "Could not load tasks:"
    ],
    "forbidden": [
      "Here are the tasks",
      "Noted. I have added"
    ]
  },
  {
    "name": "missing fields",
    "initial": "ATHENA\t1\nD\t0\ttask\n",
    "expected": [
      "Could not load tasks:"
    ],
    "forbidden": [
      "Here are the tasks",
      "Noted. I have added"
    ]
  },
  {
    "name": "extra fields",
    "initial": "ATHENA\t1\nT\t0\ttask\textra\n",
    "expected": [
      "Could not load tasks:"
    ],
    "forbidden": [
      "Here are the tasks",
      "Noted. I have added"
    ]
  },
  {
    "name": "invalid escape",
    "initial": "ATHENA\t1\nT\t0\tbad\\q\n",
    "expected": [
      "Could not load tasks:"
    ],
    "forbidden": [
      "Here are the tasks",
      "Noted. I have added"
    ]
  },
  {
    "name": "unfinished escape",
    "initial": "ATHENA\t1\nT\t0\tbad\\\n",
    "expected": [
      "Could not load tasks:"
    ],
    "forbidden": [
      "Here are the tasks",
      "Noted. I have added"
    ]
  },
  {
    "name": "blank description",
    "initial": "ATHENA\t1\nT\t0\t   \n",
    "expected": [
      "Could not load tasks:"
    ],
    "forbidden": [
      "Here are the tasks",
      "Noted. I have added"
    ]
  },
  {
    "name": "blank date",
    "initial": "ATHENA\t1\nD\t0\ttask\t\n",
    "expected": [
      "Could not load tasks:"
    ],
    "forbidden": [
      "Here are the tasks",
      "Noted. I have added"
    ]
  },
  {
    "name": "invalid deadline date",
    "initial": "ATHENA\t1\nD\t0\ttask\t2026-02-30\n",
    "expected": [
      "Could not load tasks:",
      "yyyy-MM-dd"
    ],
    "forbidden": [
      "Here are the tasks",
      "Noted. I have added"
    ]
  },
  {
    "name": "unknown version",
    "initial": "ATHENA\t2\n",
    "expected": [
      "Could not load tasks:"
    ],
    "forbidden": [
      "Here are the tasks",
      "Noted. I have added"
    ]
  },
  {
    "name": "partial valid file",
    "initial": "[T][ ] valid\ncorrupt\n",
    "expected": [
      "Could not load tasks:"
    ],
    "forbidden": [
      "Here are the tasks",
      "Noted. I have added"
    ]
  },
  {
    "name": "invalid UTF-8",
    "hex": "fffe",
    "expected": [
      "Could not load tasks:"
    ]
  },
  {
    "name": "too many tasks",
    "repeat": 101,
    "expected": [
      "Could not load tasks:",
      "limit of 100"
    ]
  },
  {
    "name": "oversized file",
    "size": 1048577,
    "expected": [
      "Could not load tasks:",
      "1 MiB"
    ]
  },
  {
    "name": "save path is a directory",
    "kind": "directory",
    "expected": [
      "Could not load tasks:"
    ]
  },
  {
    "name": "data path is a file",
    "kind": "parent_file",
    "expected": [
      "Could not load tasks:"
    ]
  },
  {
    "name": "rollback todo new task",
    "initial": "[T][ ] original\n",
    "runtime": "directory",
    "commands": [
      "todo new task",
      "list",
      "bye"
    ],
    "expected": [
      "Could not save tasks. Change was not applied:",
      "1.[T][ ] original"
    ],
    "forbidden": [
      "Noted. I have added",
      "marked this task",
      "2."
    ]
  },
  {
    "name": "rollback mark 1",
    "initial": "[T][ ] original\n",
    "runtime": "directory",
    "commands": [
      "mark 1",
      "list",
      "bye"
    ],
    "expected": [
      "Could not save tasks. Change was not applied:",
      "1.[T][ ] original"
    ],
    "forbidden": [
      "Noted. I have added",
      "marked this task",
      "2."
    ]
  },
  {
    "name": "rollback unmark 1",
    "initial": "[T][X] original\n",
    "runtime": "directory",
    "commands": [
      "unmark 1",
      "list",
      "bye"
    ],
    "expected": [
      "Could not save tasks. Change was not applied:",
      "1.[T][X] original"
    ],
    "forbidden": [
      "Noted. I have added",
      "marked this task",
      "2."
    ]
  },
  {
    "name": "external edit preserved",
    "initial": "[T][ ] original\n",
    "runtime": "external",
    "commands": [
      "todo new task",
      "list",
      "bye"
    ],
    "expected": [
      "Could not save tasks. Change was not applied:",
      "changed outside Athena",
      "1.[T][ ] original"
    ],
    "forbidden": [
      "Noted. I have added",
      "2."
    ]
  },
  {
    "name": "full list rejects additions",
    "repeat": 100,
    "runtime": "none",
    "commands": [
      "todo extra",
      "mark 101",
      "list",
      "bye"
    ],
    "expected": [
      "The list is full (100 tasks).",
      "Task number must be between 1 and 100.",
      "100.[T][ ] original"
    ],
    "forbidden": [
      "Noted. I have added",
      "101."
    ]
  },
  {
    "name": "BOM and blank lines",
    "initial": "\ufeff\n[T][X] original\n\n",
    "runtime": "none",
    "commands": [
      "list",
      "bye"
    ],
    "expected": [
      "1.[T][X] original"
    ],
    "forbidden": [
      "Could not load"
    ]
  },
  {
    "name": "whitespace and unchanged status",
    "initial": "[T][X] original\n",
    "runtime": "none",
    "commands": [
      "  MARK\t1  ",
      "  list  ",
      "bye"
    ],
    "expected": [
      "Nice! I've marked this task as done:",
      "1.[T][X] original"
    ],
    "forbidden": [
      "Could not save",
      "Unknown command"
    ]
  },
  {
    "name": "rollback middle deletion",
    "initial": "[T][X] first\n[D][ ] middle (by: 2026-06-05)\n[T][ ] last\n",
    "runtime": "directory",
    "commands": [
      "delete 2",
      "list",
      "bye"
    ],
    "expected": [
      "Could not save tasks. Change was not applied:",
      "1.[T][X] first",
      "2.[D][ ] middle (by: Jun 05 2026)",
      "3.[T][ ] last"
    ],
    "forbidden": [
      "Noted. I've removed"
    ]
  }
]
```

## Test case 1: Add and list a Todo

Aim: Verify that a Todo stores only its description and appears in the list.

Input:

```text
todo borrow book
list
bye
```

Expected output:

```text
[T][ ] borrow book
1.[T][ ] borrow book
```

Expected file checkpoints (`data/athena.txt`):

```json
[
  "ATHENA\t1\nT\t0\tborrow book\n",
  "ATHENA\t1\nT\t0\tborrow book\n",
  "ATHENA\t1\nT\t0\tborrow book\n"
]
```

## Test case 2: Add and list a Deadline

Aim: Verify that a Deadline stores an ISO date and displays it in a friendlier format.

Input:

```text
deadline return book /by 2/12/2019 1800
list
bye
```

Expected output:

```text
[D][ ] return book (by: Dec 02 2019 6:00 PM)
1.[D][ ] return book (by: Dec 02 2019 6:00 PM)
```

Expected file checkpoints (`data/athena.txt`):

```json
[
  "ATHENA\t1\nD\t0\treturn book\t2019-12-02 1800\n",
  "ATHENA\t1\nD\t0\treturn book\t2019-12-02 1800\n",
  "ATHENA\t1\nD\t0\treturn book\t2019-12-02 1800\n"
]
```

## Test case 3: Add multiple Events

Aim: Verify that Events parse both supported date formats and optional time syntax.

Input:

```text
event first meeting /from 2/12/2019 1400 /to 2/12/2019 1600
event second meeting /from 2019-12-03 15:00 /to 2019-12-03 1700
list
bye
```

Expected output:

```text
[E][ ] first meeting (from: Dec 02 2019 2:00 PM to: Dec 02 2019 4:00 PM)
[E][ ] second meeting (from: Dec 03 2019 3:00 PM to: Dec 03 2019 5:00 PM)
1.[E][ ] first meeting (from: Dec 02 2019 2:00 PM to: Dec 02 2019 4:00 PM)
2.[E][ ] second meeting (from: Dec 03 2019 3:00 PM to: Dec 03 2019 5:00 PM)
```

Expected file checkpoints (`data/athena.txt`):

```json
[
  "ATHENA\t1\nE\t0\tfirst meeting\t2019-12-02 1400\t2019-12-02 1600\n",
  "ATHENA\t1\nE\t0\tfirst meeting\t2019-12-02 1400\t2019-12-02 1600\nE\t0\tsecond meeting\t2019-12-03 1500\t2019-12-03 1700\n",
  "ATHENA\t1\nE\t0\tfirst meeting\t2019-12-02 1400\t2019-12-02 1600\nE\t0\tsecond meeting\t2019-12-03 1500\t2019-12-03 1700\n",
  "ATHENA\t1\nE\t0\tfirst meeting\t2019-12-02 1400\t2019-12-02 1600\nE\t0\tsecond meeting\t2019-12-03 1500\t2019-12-03 1700\n"
]
```

## Test case 4: Mark and unmark a task

Aim: Verify that task completion status changes are reflected in output.

Input:

```text
todo read book
mark 1
unmark 1
bye
```

Expected output:

```text
[T][ ] read book
[T][X] read book
[T][ ] read book
```

Expected file checkpoints (`data/athena.txt`):

```json
[
  "ATHENA\t1\nT\t0\tread book\n",
  "ATHENA\t1\nT\t1\tread book\n",
  "ATHENA\t1\nT\t0\tread book\n",
  "ATHENA\t1\nT\t0\tread book\n"
]
```

## Test case 5: Reject malformed Event input

Aim: Verify that missing delimiters, impossible dates, and reversed ranges do
not terminate the application or add a null task.

Input:

```text
event incomplete
event invalid /from 2026-02-30 1000 /to 2026-03-01 1000
event reverse /from 2026-03-02 1000 /to 2026-03-01 1000
list
bye
```

Expected output:

```text
An event must use the format: event description /from start /to end.
Dates and times must use d/M/yyyy or yyyy-MM-dd, optionally followed by HHmm or H:mm.
An event cannot end before it starts.
Here are the tasks in your list:
```

Expected file checkpoints (`data/athena.txt`):

```json
[
  null,
  null,
  null,
  null,
  null
]
```

## Test case 6: Save a mixed list and preserve it after rejected commands

Aim: Verify all task types coexist in one save file, status changes rewrite the
file instead of appending, Unicode survives, and invalid commands leave it intact.
An empty Todo must not leave a null entry that prevents the next save.

Input:

```text
todo
todo café
deadline return book /by 2026-06-07
event meeting /from 2026-06-07 1400 /to 2026-06-07 1600
mark 2
unmark 2
mark 99
unmark abc
list
bye
```

Expected output:

```text
The description of a task cannot be empty.
[T][ ] café
[D][ ] return book (by: Jun 07 2026)
[E][ ] meeting (from: Jun 07 2026 2:00 PM to: Jun 07 2026 4:00 PM)
[D][X] return book (by: Jun 07 2026)
[D][ ] return book (by: Jun 07 2026)
Task number must be between 1 and 3.
Please provide a valid task number after unmark.
1.[T][ ] café
2.[D][ ] return book (by: Jun 07 2026)
3.[E][ ] meeting (from: Jun 07 2026 2:00 PM to: Jun 07 2026 4:00 PM)
```

Expected file checkpoints (`data/athena.txt`):

```json
[
  null,
  "ATHENA\t1\nT\t0\tcaf\u00e9\n",
  "ATHENA\t1\nT\t0\tcaf\u00e9\nD\t0\treturn book\t2026-06-07\n",
  "ATHENA\t1\nT\t0\tcaf\u00e9\nD\t0\treturn book\t2026-06-07\nE\t0\tmeeting\t2026-06-07 1400\t2026-06-07 1600\n",
  "ATHENA\t1\nT\t0\tcaf\u00e9\nD\t1\treturn book\t2026-06-07\nE\t0\tmeeting\t2026-06-07 1400\t2026-06-07 1600\n",
  "ATHENA\t1\nT\t0\tcaf\u00e9\nD\t0\treturn book\t2026-06-07\nE\t0\tmeeting\t2026-06-07 1400\t2026-06-07 1600\n",
  "ATHENA\t1\nT\t0\tcaf\u00e9\nD\t0\treturn book\t2026-06-07\nE\t0\tmeeting\t2026-06-07 1400\t2026-06-07 1600\n",
  "ATHENA\t1\nT\t0\tcaf\u00e9\nD\t0\treturn book\t2026-06-07\nE\t0\tmeeting\t2026-06-07 1400\t2026-06-07 1600\n",
  "ATHENA\t1\nT\t0\tcaf\u00e9\nD\t0\treturn book\t2026-06-07\nE\t0\tmeeting\t2026-06-07 1400\t2026-06-07 1600\n",
  "ATHENA\t1\nT\t0\tcaf\u00e9\nD\t0\treturn book\t2026-06-07\nE\t0\tmeeting\t2026-06-07 1400\t2026-06-07 1600\n"
]
```

## Test case 7: Load and modify an existing mixed list

Aim: Verify startup restores order, all task types, Unicode, dates, and completion status. Adding and changing loaded tasks must retain the existing list.

Initial file:

```initial
"[T][X] caf\u00e9\n[D][ ] return book (by: 2026-06-06)\n[E][X] meeting (from: 2026-08-06 1400 to: 2026-08-06 1600)\n"
```

Input:

```text
list
todo new task
unmark 3
mark 2
list
bye
```

Expected output:

```text
1.[T][X] café
2.[D][ ] return book (by: Jun 06 2026)
3.[E][X] meeting (from: Aug 06 2026 2:00 PM to: Aug 06 2026 4:00 PM)
[T][ ] new task
Now you have 4 tasks in the list
[E][ ] meeting (from: Aug 06 2026 2:00 PM to: Aug 06 2026 4:00 PM)
[D][X] return book (by: Jun 06 2026)
1.[T][X] café
2.[D][X] return book (by: Jun 06 2026)
3.[E][ ] meeting (from: Aug 06 2026 2:00 PM to: Aug 06 2026 4:00 PM)
4.[T][ ] new task
```

Expected file checkpoints (`data/athena.txt`):

```json
[
  "[T][X] caf\u00e9\n[D][ ] return book (by: 2026-06-06)\n[E][X] meeting (from: 2026-08-06 1400 to: 2026-08-06 1600)\n",
  "ATHENA\t1\nT\t1\tcaf\u00e9\nD\t0\treturn book\t2026-06-06\nE\t1\tmeeting\t2026-08-06 1400\t2026-08-06 1600\nT\t0\tnew task\n",
  "ATHENA\t1\nT\t1\tcaf\u00e9\nD\t0\treturn book\t2026-06-06\nE\t0\tmeeting\t2026-08-06 1400\t2026-08-06 1600\nT\t0\tnew task\n",
  "ATHENA\t1\nT\t1\tcaf\u00e9\nD\t1\treturn book\t2026-06-06\nE\t0\tmeeting\t2026-08-06 1400\t2026-08-06 1600\nT\t0\tnew task\n",
  "ATHENA\t1\nT\t1\tcaf\u00e9\nD\t1\treturn book\t2026-06-06\nE\t0\tmeeting\t2026-08-06 1400\t2026-08-06 1600\nT\t0\tnew task\n",
  "ATHENA\t1\nT\t1\tcaf\u00e9\nD\t1\treturn book\t2026-06-06\nE\t0\tmeeting\t2026-08-06 1400\t2026-08-06 1600\nT\t0\tnew task\n"
]
```

## Test case 8: Start with an empty save file

Aim: Verify an empty file loads as an empty list and accepts new tasks.

Initial file:

```initial
""
```

Input:

```text
list
todo first task
list
bye
```

Expected output:

```text
Here are the tasks in your list:
[T][ ] first task
Now you have 1 tasks in the list
1.[T][ ] first task
```

Expected file checkpoints (`data/athena.txt`):

```json
[
  "",
  "ATHENA\t1\nT\t0\tfirst task\n",
  "ATHENA\t1\nT\t0\tfirst task\n",
  "ATHENA\t1\nT\t0\tfirst task\n"
]
```

## Test case 9: Invalid commands and delimiter-safe round trips

Aim: Reject malformed commands without mutation and preserve slashes, backslashes, pipes, brackets, and display delimiters across restart.

Input:

```text
mark
unmark abc
mark 1
todo
deadline incomplete
deadline impossible /by 2026-02-30
event incomplete
todo path C:\notes | [X]
deadline read / notes /by 2026-07-06
event tricky to: later (from: literal) /from 06/07/2026 1400 /to 06/07/2026 16:00
mark 99999999999999999999
list
bye
```

Expected output:

```text
Please provide a valid task number after mark.
Please provide a valid task number after unmark.
There are no tasks in the list.
The description of a task cannot be empty.
A deadline must use the format: deadline description /by date.
Dates and times must use d/M/yyyy or yyyy-MM-dd, optionally followed by HHmm or H:mm.
An event must use the format: event description /from start /to end.
[T][ ] path C:\notes | [X]
[D][ ] read / notes (by: Jul 06 2026)
[E][ ] tricky to: later (from: literal) (from: Jul 06 2026 2:00 PM to: Jul 06 2026 4:00 PM)
Please provide a valid task number after mark.
1.[T][ ] path C:\notes | [X]
2.[D][ ] read / notes (by: Jul 06 2026)
3.[E][ ] tricky to: later (from: literal) (from: Jul 06 2026 2:00 PM to: Jul 06 2026 4:00 PM)
```

Expected file checkpoints:

```json
[
  null,
  null,
  null,
  null,
  null,
  null,
  null,
  "ATHENA\t1\nT\t0\tpath C:\\\\notes | [X]\n",
  "ATHENA\t1\nT\t0\tpath C:\\\\notes | [X]\nD\t0\tread / notes\t2026-07-06\n",
  "ATHENA\t1\nT\t0\tpath C:\\\\notes | [X]\nD\t0\tread / notes\t2026-07-06\nE\t0\ttricky to: later (from: literal)\t2026-07-06 1400\t2026-07-06 1600\n",
  "ATHENA\t1\nT\t0\tpath C:\\\\notes | [X]\nD\t0\tread / notes\t2026-07-06\nE\t0\ttricky to: later (from: literal)\t2026-07-06 1400\t2026-07-06 1600\n",
  "ATHENA\t1\nT\t0\tpath C:\\\\notes | [X]\nD\t0\tread / notes\t2026-07-06\nE\t0\ttricky to: later (from: literal)\t2026-07-06 1400\t2026-07-06 1600\n",
  "ATHENA\t1\nT\t0\tpath C:\\\\notes | [X]\nD\t0\tread / notes\t2026-07-06\nE\t0\ttricky to: later (from: literal)\t2026-07-06 1400\t2026-07-06 1600\n"
]
```

## Test case 10: Delete a middle task and use the renumbered list

Aim: Verify the example deletion message, remaining order and count, and that
marking and unmarking use the new task numbers.

Input:

```text
todo read book
deadline return book /by 2026-06-06
event project meeting /from 2026-08-06 1400 /to 2026-08-06 1600
todo join sports club
todo borrow book
delete 3
list
mark 3
unmark 3
bye
```

Expected output:

```text
  Noted. I've removed this task:
    [E][ ] project meeting (from: Aug 06 2026 2:00 PM to: Aug 06 2026 4:00 PM)
  Now you have 4 tasks in the list.

  Here are the tasks in your list:
  1.[T][ ] read book
  2.[D][ ] return book (by: Jun 06 2026)
  3.[T][ ] join sports club
  4.[T][ ] borrow book

  Nice! I've marked this task as done:
  [T][X] join sports club

  OK, I've marked this task as not done yet:
  [T][ ] join sports club
```


Expected file checkpoints (`data/athena.txt`):

```json
[
  "ATHENA\t1\nT\t0\tread book\n",
  "ATHENA\t1\nT\t0\tread book\nD\t0\treturn book\t2026-06-06\n",
  "ATHENA\t1\nT\t0\tread book\nD\t0\treturn book\t2026-06-06\nE\t0\tproject meeting\t2026-08-06 1400\t2026-08-06 1600\n",
  "ATHENA\t1\nT\t0\tread book\nD\t0\treturn book\t2026-06-06\nE\t0\tproject meeting\t2026-08-06 1400\t2026-08-06 1600\nT\t0\tjoin sports club\n",
  "ATHENA\t1\nT\t0\tread book\nD\t0\treturn book\t2026-06-06\nE\t0\tproject meeting\t2026-08-06 1400\t2026-08-06 1600\nT\t0\tjoin sports club\nT\t0\tborrow book\n",
  "ATHENA\t1\nT\t0\tread book\nD\t0\treturn book\t2026-06-06\nT\t0\tjoin sports club\nT\t0\tborrow book\n",
  "ATHENA\t1\nT\t0\tread book\nD\t0\treturn book\t2026-06-06\nT\t0\tjoin sports club\nT\t0\tborrow book\n",
  "ATHENA\t1\nT\t0\tread book\nD\t0\treturn book\t2026-06-06\nT\t1\tjoin sports club\nT\t0\tborrow book\n",
  "ATHENA\t1\nT\t0\tread book\nD\t0\treturn book\t2026-06-06\nT\t0\tjoin sports club\nT\t0\tborrow book\n",
  "ATHENA\t1\nT\t0\tread book\nD\t0\treturn book\t2026-06-06\nT\t0\tjoin sports club\nT\t0\tborrow book\n"
]
```

## Test case 11: Delete first, last, and only tasks and add again

Aim: Verify deletion of all task types, preservation of completion status, command
case handling, empty-list behavior, and reuse of the list after deletion.

Input:

```text
todo read book
event meeting /from 2026-06-07 1400 /to 2026-06-07 1600
deadline return book /by 2026-06-07
mark 1
DELETE 1
delete 2
delete 1
list
delete 1
todo new task
list
bye
```

Expected output:

```text
  Nice! I've marked this task as done:
  [T][X] read book

  Noted. I've removed this task:
    [T][X] read book
  Now you have 2 tasks in the list.

  Noted. I've removed this task:
    [D][ ] return book (by: Jun 07 2026)
  Now you have 1 tasks in the list.

  Noted. I've removed this task:
    [E][ ] meeting (from: Jun 07 2026 2:00 PM to: Jun 07 2026 4:00 PM)
  Now you have 0 tasks in the list.

  Here are the tasks in your list:

  There are no tasks to delete.

  Here are the tasks in your list:
  1.[T][ ] new task
```


Expected file checkpoints (`data/athena.txt`):

```json
[
  "ATHENA\t1\nT\t0\tread book\n",
  "ATHENA\t1\nT\t0\tread book\nE\t0\tmeeting\t2026-06-07 1400\t2026-06-07 1600\n",
  "ATHENA\t1\nT\t0\tread book\nE\t0\tmeeting\t2026-06-07 1400\t2026-06-07 1600\nD\t0\treturn book\t2026-06-07\n",
  "ATHENA\t1\nT\t1\tread book\nE\t0\tmeeting\t2026-06-07 1400\t2026-06-07 1600\nD\t0\treturn book\t2026-06-07\n",
  "ATHENA\t1\nE\t0\tmeeting\t2026-06-07 1400\t2026-06-07 1600\nD\t0\treturn book\t2026-06-07\n",
  "ATHENA\t1\nE\t0\tmeeting\t2026-06-07 1400\t2026-06-07 1600\n",
  "ATHENA\t1\n",
  "ATHENA\t1\n",
  "ATHENA\t1\n",
  "ATHENA\t1\nT\t0\tnew task\n",
  "ATHENA\t1\nT\t0\tnew task\n",
  "ATHENA\t1\nT\t0\tnew task\n"
]
```

## Test case 12: Reject invalid deletion commands

Aim: Verify missing, nonnumeric, overflowing, extra, and out-of-range arguments
produce errors without changing the list or terminating the application.

Input:

```text
todo keep me
delete
delete abc
delete 999999999999999999999
delete 1 2
delete 0
delete -1
delete 2
list
bye
```

Expected output:

```text
  Please provide a valid task number after delete.

  Please provide a valid task number after delete.

  Please provide a valid task number after delete.

  Please provide a valid task number after delete.

  Task number must be between 1 and 1.

  Task number must be between 1 and 1.

  Task number must be between 1 and 1.

  Here are the tasks in your list:
  1.[T][ ] keep me
```


Expected file checkpoints (`data/athena.txt`):

```json
[
  "ATHENA\t1\nT\t0\tkeep me\n",
  "ATHENA\t1\nT\t0\tkeep me\n",
  "ATHENA\t1\nT\t0\tkeep me\n",
  "ATHENA\t1\nT\t0\tkeep me\n",
  "ATHENA\t1\nT\t0\tkeep me\n",
  "ATHENA\t1\nT\t0\tkeep me\n",
  "ATHENA\t1\nT\t0\tkeep me\n",
  "ATHENA\t1\nT\t0\tkeep me\n",
  "ATHENA\t1\nT\t0\tkeep me\n",
  "ATHENA\t1\nT\t0\tkeep me\n"
]
```

## Test case 13: Delete from a full list and reuse the freed slot

Aim: Verify the capacity check allows deletion and the freed slot accepts another
task without losing the remaining tasks.

Initial file:

```initial
"ATHENA\t1\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\n"
```

Input:

```text
delete 50
todo replacement
delete 100
delete 99
bye
```

Expected output:

```text
  Noted. I've removed this task:
    [T][ ] original
  Now you have 99 tasks in the list.

  Noted. I've removed this task:
    [T][ ] replacement
  Now you have 99 tasks in the list.

  Noted. I've removed this task:
    [T][ ] original
  Now you have 98 tasks in the list.
```

Expected file checkpoints (`data/athena.txt`):

```json
[
  "ATHENA\t1\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\n",
  "ATHENA\t1\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\treplacement\n",
  "ATHENA\t1\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\n",
  "ATHENA\t1\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\n",
  "ATHENA\t1\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\nT\t0\toriginal\n"
]
```

## Test case 14: Find deadlines and events by date

Aim: Verify that `on` accepts either date format, includes timed and untimed
deadlines, includes same-day and spanning Events, excludes other dates, displays
times when supplied, rejects invalid dates, and never changes the save file.

Input:

```text
todo unrelated
deadline timed /by 7/6/2026 0900
deadline date only /by 2026-06-07
event same day /from 2026-06-07 1000 /to 2026-06-07 12:00
event spanning /from 6/6/2026 2300 /to 8/6/2026 0100
event outside /from 2026-06-08 /to 2026-06-09
on 7/6/2026
on 2026-06-10
on 2026-02-30
on
bye
```

Expected output:

```text
  Here are the deadlines and events on Jun 07 2026:
  1.[D][ ] timed (by: Jun 07 2026 9:00 AM)
  2.[D][ ] date only (by: Jun 07 2026)
  3.[E][ ] same day (from: Jun 07 2026 10:00 AM to: Jun 07 2026 12:00 PM)
  4.[E][ ] spanning (from: Jun 06 2026 11:00 PM to: Jun 08 2026 1:00 AM)

  There are no deadlines or events on Jun 10 2026.

  Please provide a valid date in d/M/yyyy or yyyy-MM-dd format after on.

  Please provide a valid date in d/M/yyyy or yyyy-MM-dd format after on.
```

Expected file checkpoints (`data/athena.txt`):

```json
[
  "ATHENA\t1\nT\t0\tunrelated\n",
  "ATHENA\t1\nT\t0\tunrelated\nD\t0\ttimed\t2026-06-07 0900\n",
  "ATHENA\t1\nT\t0\tunrelated\nD\t0\ttimed\t2026-06-07 0900\nD\t0\tdate only\t2026-06-07\n",
  "ATHENA\t1\nT\t0\tunrelated\nD\t0\ttimed\t2026-06-07 0900\nD\t0\tdate only\t2026-06-07\nE\t0\tsame day\t2026-06-07 1000\t2026-06-07 1200\n",
  "ATHENA\t1\nT\t0\tunrelated\nD\t0\ttimed\t2026-06-07 0900\nD\t0\tdate only\t2026-06-07\nE\t0\tsame day\t2026-06-07 1000\t2026-06-07 1200\nE\t0\tspanning\t2026-06-06 2300\t2026-06-08 0100\n",
  "ATHENA\t1\nT\t0\tunrelated\nD\t0\ttimed\t2026-06-07 0900\nD\t0\tdate only\t2026-06-07\nE\t0\tsame day\t2026-06-07 1000\t2026-06-07 1200\nE\t0\tspanning\t2026-06-06 2300\t2026-06-08 0100\nE\t0\toutside\t2026-06-08\t2026-06-09\n",
  "ATHENA\t1\nT\t0\tunrelated\nD\t0\ttimed\t2026-06-07 0900\nD\t0\tdate only\t2026-06-07\nE\t0\tsame day\t2026-06-07 1000\t2026-06-07 1200\nE\t0\tspanning\t2026-06-06 2300\t2026-06-08 0100\nE\t0\toutside\t2026-06-08\t2026-06-09\n",
  "ATHENA\t1\nT\t0\tunrelated\nD\t0\ttimed\t2026-06-07 0900\nD\t0\tdate only\t2026-06-07\nE\t0\tsame day\t2026-06-07 1000\t2026-06-07 1200\nE\t0\tspanning\t2026-06-06 2300\t2026-06-08 0100\nE\t0\toutside\t2026-06-08\t2026-06-09\n",
  "ATHENA\t1\nT\t0\tunrelated\nD\t0\ttimed\t2026-06-07 0900\nD\t0\tdate only\t2026-06-07\nE\t0\tsame day\t2026-06-07 1000\t2026-06-07 1200\nE\t0\tspanning\t2026-06-06 2300\t2026-06-08 0100\nE\t0\toutside\t2026-06-08\t2026-06-09\n",
  "ATHENA\t1\nT\t0\tunrelated\nD\t0\ttimed\t2026-06-07 0900\nD\t0\tdate only\t2026-06-07\nE\t0\tsame day\t2026-06-07 1000\t2026-06-07 1200\nE\t0\tspanning\t2026-06-06 2300\t2026-06-08 0100\nE\t0\toutside\t2026-06-08\t2026-06-09\n",
  "ATHENA\t1\nT\t0\tunrelated\nD\t0\ttimed\t2026-06-07 0900\nD\t0\tdate only\t2026-06-07\nE\t0\tsame day\t2026-06-07 1000\t2026-06-07 1200\nE\t0\tspanning\t2026-06-06 2300\t2026-06-08 0100\nE\t0\toutside\t2026-06-08\t2026-06-09\n"
]
```

## Test case 15: Find tasks by description keyword

Aim: Verify that `find` searches every task type by description, ignores case,
preserves task order and status, does not search date fields, handles no matches,
rejects a missing keyword, and never changes the save file.

Input:

```text
todo read book
deadline return BOOK /by 2026-06-06
event book club /from 2026-06-07 1400 /to 2026-06-07 1600
todo write report
mark 2
find book
find BOOK
find 2026
find missing
find
bye
```

Expected output:

```text
  Here are the matching tasks in your list:
  1.[T][ ] read book
  2.[D][X] return BOOK (by: Jun 06 2026)
  3.[E][ ] book club (from: Jun 07 2026 2:00 PM to: Jun 07 2026 4:00 PM)

  Here are the matching tasks in your list:
  1.[T][ ] read book
  2.[D][X] return BOOK (by: Jun 06 2026)
  3.[E][ ] book club (from: Jun 07 2026 2:00 PM to: Jun 07 2026 4:00 PM)

  There are no matching tasks.

  There are no matching tasks.

  Please provide a keyword after find.
```

Expected file checkpoints (`data/athena.txt`):

```json
[
  "ATHENA\t1\nT\t0\tread book\n",
  "ATHENA\t1\nT\t0\tread book\nD\t0\treturn BOOK\t2026-06-06\n",
  "ATHENA\t1\nT\t0\tread book\nD\t0\treturn BOOK\t2026-06-06\nE\t0\tbook club\t2026-06-07 1400\t2026-06-07 1600\n",
  "ATHENA\t1\nT\t0\tread book\nD\t0\treturn BOOK\t2026-06-06\nE\t0\tbook club\t2026-06-07 1400\t2026-06-07 1600\nT\t0\twrite report\n",
  "ATHENA\t1\nT\t0\tread book\nD\t1\treturn BOOK\t2026-06-06\nE\t0\tbook club\t2026-06-07 1400\t2026-06-07 1600\nT\t0\twrite report\n",
  "ATHENA\t1\nT\t0\tread book\nD\t1\treturn BOOK\t2026-06-06\nE\t0\tbook club\t2026-06-07 1400\t2026-06-07 1600\nT\t0\twrite report\n",
  "ATHENA\t1\nT\t0\tread book\nD\t1\treturn BOOK\t2026-06-06\nE\t0\tbook club\t2026-06-07 1400\t2026-06-07 1600\nT\t0\twrite report\n",
  "ATHENA\t1\nT\t0\tread book\nD\t1\treturn BOOK\t2026-06-06\nE\t0\tbook club\t2026-06-07 1400\t2026-06-07 1600\nT\t0\twrite report\n",
  "ATHENA\t1\nT\t0\tread book\nD\t1\treturn BOOK\t2026-06-06\nE\t0\tbook club\t2026-06-07 1400\t2026-06-07 1600\nT\t0\twrite report\n",
  "ATHENA\t1\nT\t0\tread book\nD\t1\treturn BOOK\t2026-06-06\nE\t0\tbook club\t2026-06-07 1400\t2026-06-07 1600\nT\t0\twrite report\n",
  "ATHENA\t1\nT\t0\tread book\nD\t1\treturn BOOK\t2026-06-06\nE\t0\tbook club\t2026-06-07 1400\t2026-06-07 1600\nT\t0\twrite report\n"
]
```
