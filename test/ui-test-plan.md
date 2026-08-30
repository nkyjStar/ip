# Athena UI Test Plan

The UI tests exercise Athena through standard input. Each test case starts a
fresh application session. Expected output below focuses on the task-manager
responses and excludes the fixed greeting, page-break separators, and farewell
message. Matching is otherwise case-sensitive and whitespace-sensitive.

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
