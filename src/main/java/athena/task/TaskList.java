package athena.task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Owns Athena's ordered, fixed-capacity collection of tasks. */
public class TaskList {
    private static final int MAX_TASKS = 100;
    private final Task[] tasks = new Task[MAX_TASKS];
    private int size = 0;

    /** Creates an empty task list. */
    public TaskList() {
    }

    /** Creates a task list containing the supplied tasks in their current order. */
    public TaskList(List<Task> initialTasks) {
        if (initialTasks.size() > MAX_TASKS) {
            throw new IllegalArgumentException("A task list cannot contain more than " + MAX_TASKS + " tasks.");
        }
        for (Task task : initialTasks) {
            add(task);
        }
    }

    /** Returns the maximum number of tasks this list can hold. */
    public int getCapacity() {
        return MAX_TASKS;
    }

    /** Returns the number of tasks currently in the list. */
    public int size() {
        return size;
    }

    /** Returns whether the list contains no tasks. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Returns whether the list has reached its capacity. */
    public boolean isFull() {
        return size == MAX_TASKS;
    }

    /** Returns the task at the given zero-based index. */
    public Task get(int index) {
        checkTaskIndex(index);
        return tasks[index];
    }

    /** Adds a task to the end of the list. */
    public void add(Task task) {
        add(size, task);
    }

    /** Inserts a task at the given zero-based index. */
    public void add(int index, Task task) {
        if (isFull()) {
            throw new IllegalStateException("The task list is full.");
        }
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Task index: " + index);
        }
        if (task == null) {
            throw new IllegalArgumentException("A task list cannot contain null tasks.");
        }
        for (int i = size; i > index; i--) {
            tasks[i] = tasks[i - 1];
        }
        tasks[index] = task;
        size++;
    }

    /** Removes and returns the task at the given zero-based index. */
    public Task remove(int index) {
        checkTaskIndex(index);
        Task removedTask = tasks[index];
        for (int i = index; i < size - 1; i++) {
            tasks[i] = tasks[i + 1];
        }
        tasks[--size] = null;
        return removedTask;
    }

    /** Returns the deadlines due on the given date in their current list order. */
    public List<Deadline> findDeadlinesOn(LocalDate date) {
        List<Deadline> matches = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (tasks[i] instanceof Deadline deadline && deadline.getDeadline().equals(date)) {
                matches.add(deadline);
            }
        }
        return matches;
    }

    /** Ensures an index identifies a task currently in the list. */
    private void checkTaskIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Task index: " + index);
        }
    }
}
