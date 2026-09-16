# Athena UI Test Plan

The UI tests exercise Athena through standard input. Each test case starts a
fresh application session. Expected output below focuses on the task-manager
responses and excludes the fixed greeting, page-break separators, and farewell
message. Matching is otherwise case-sensitive and whitespace-sensitive.

Cases 1-5 show ordered literal output fragments, omitting surrounding indentation.
For cases 6-9, compare the complete responses to `delete`, `list`, `mark`, and
`unmark` commands, preserving indentation. Blank lines separate responses in the
expected output; responses to task additions are omitted.

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

## Test case 2: Add and list a Deadline

Aim: Verify that a Deadline stores and displays its `/by` date/time string.

Input:

```text
deadline return book /by Sunday
list
bye
```

Expected output:

```text
[D][ ] return book (by: Sunday)
1.[D][ ] return book (by: Sunday)
```

## Test case 3: Add multiple Events

Aim: Verify that each Event retains its own start and end values.

Input:

```text
event first meeting /from Monday 2pm /to Monday 4pm
event second meeting /from Tuesday 3pm /to Tuesday 5pm
list
bye
```

Expected output:

```text
[E][ ] first meeting (from: Monday 2pm to: Monday 4pm)
[E][ ] second meeting (from: Tuesday 3pm to: Tuesday 5pm)
1.[E][ ] first meeting (from: Monday 2pm to: Monday 4pm)
2.[E][ ] second meeting (from: Tuesday 3pm to: Tuesday 5pm)
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

## Test case 5: Reject malformed Event input

Aim: Verify that malformed Event input does not terminate the application or
add a null task.

Input:

```text
event incomplete
list
bye
```

Expected output:

```text
An event must use the format: event description /from start /to end.
Here are the tasks in your list:
```

## Test case 6: Delete a middle task and use the renumbered list

Aim: Verify the example deletion message, remaining order and count, and that
marking and unmarking use the new task numbers.

Input:

```text
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
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
    [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
  Now you have 4 tasks in the list.

  Here are the tasks in your list:
  1.[T][ ] read book
  2.[D][ ] return book (by: June 6th)
  3.[T][ ] join sports club
  4.[T][ ] borrow book

  Nice! I've marked this task as done:
  [T][X] join sports club

  OK, I've marked this task as not done yet:
  [T][ ] join sports club
```

## Test case 7: Delete first, last, and only tasks and add again

Aim: Verify deletion of all task types, preservation of completion status, command
case handling, empty-list behavior, and reuse of the list after deletion.

Input:

```text
todo read book
event meeting /from 2pm /to 4pm
deadline return book /by Sunday
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
    [D][ ] return book (by: Sunday)
  Now you have 1 tasks in the list.

  Noted. I've removed this task:
    [E][ ] meeting (from: 2pm to: 4pm)
  Now you have 0 tasks in the list.

  Here are the tasks in your list:

  There are no tasks to delete.

  Here are the tasks in your list:
  1.[T][ ] new task
```

## Test case 8: Reject invalid deletion commands

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

## Test case 9: Delete from a full list and reuse the freed slot

Aim: Verify the capacity check allows deletion and the freed slot accepts another
task without losing the remaining tasks.

Input (expand `REPEAT 100:` into 100 copies of the following command):

```text
REPEAT 100: todo original
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
