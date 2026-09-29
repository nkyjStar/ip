"""Run the UI plan on Java 25 and check save files after every command."""

import json
from datetime import date
from pathlib import Path
import queue
import re
import subprocess
import tempfile
import threading
import time


ROOT = Path(__file__).resolve().parents[1]
PAGE_BREAK = "_" * 60


def displayed_tasks(saved):
    """Render explicit stored fields for the restart comparison."""
    if not saved or not saved.startswith("ATHENA\t1\n"):
        return (saved or "").splitlines()
    result = []
    escapes = {"t": "\t", "n": "\n", "r": "\r", "\\": "\\"}
    for line in saved.splitlines()[1:]:
        fields = line.split("\t")
        fields[2:] = [re.sub(r"\\(.)", lambda m: escapes[m[1]], value) for value in fields[2:]]
        task = f"[{fields[0]}][{'X' if fields[1] == '1' else ' '}] {fields[2]}"
        if fields[0] == "D":
            deadline = date.fromisoformat(fields[3])
            task += f" (by: {deadline.strftime('%b %d %Y')})"
        elif fields[0] == "E":
            task += f" (from: {fields[3]} to: {fields[4]})"
        result.append(task)
    return result


def run_case(case, classes, work):
    """Capture a complete session and compare console fragments and save checkpoints."""
    title = case.splitlines()[0]
    commands_text, expected = re.findall(r"```text\n(.*?)\n```", case, re.S)
    commands = commands_text.splitlines()
    checkpoints = json.loads(re.search(r"```json\n(.*?)\n```", case, re.S)[1])
    assert len(commands) == len(checkpoints), "One checkpoint is required per command"
    case_dir = Path(tempfile.mkdtemp(prefix="case-", dir=work))
    save_path = case_dir / "data" / "athena.txt"
    initial = re.search(r"```initial\n(.*?)\n```", case, re.S)
    if initial:
        save_path.parent.mkdir(parents=True)
        save_path.write_text(json.loads(initial[1]), encoding="utf-8")
    process = subprocess.Popen(
        ["java", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8",
         "-cp", str(classes), "athena.Athena"],
        cwd=case_dir, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT, text=True, encoding="utf-8",
    )
    output_lines = []
    pending = queue.Queue()
    sent = []
    error = None
    restart_record = ""

    def collect_output():
        for line in process.stdout:
            output_lines.append(line)
            pending.put(line)
        pending.put(None)

    reader = threading.Thread(target=collect_output, daemon=True)
    reader.start()

    def read_separator():
        while True:
            line = pending.get(timeout=30)
            assert line is not None, "Application exited before completing its response"
            if line.rstrip("\r\n") == PAGE_BREAK:
                return

    try:
        read_separator()
        read_separator()
        for command, expected_file in zip(commands, checkpoints):
            sent.append(command)
            process.stdin.write(command + "\n")
            process.stdin.flush()
            read_separator()
            read_separator()
            if command.lower() == "bye":
                assert process.wait(timeout=30) == 0, "Application exited unsuccessfully"
            save_path = case_dir / "data" / "athena.txt"
            deadline = time.monotonic() + 5
            while True:
                actual_file = save_path.read_text(encoding="utf-8") if save_path.exists() else None
                if actual_file == expected_file:
                    break
                assert time.monotonic() < deadline, (
                    f"After {command!r}: expected file {expected_file!r}, got {actual_file!r}"
                )
                time.sleep(0.01)
        reader.join(timeout=30)
        actual = "".join(output_lines)
        position = 0
        for fragment in expected.splitlines():
            found = actual.find(fragment, position)
            assert found >= 0, f"Missing expected output fragment: {fragment!r}"
            position = found + len(fragment)
        saved_before_restart = save_path.read_text(encoding="utf-8") if save_path.exists() else None
        restart = subprocess.run(
            ["java", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8",
             "-cp", str(classes), "athena.Athena"],
            cwd=case_dir, input="list\nbye\n", capture_output=True,
            text=True, encoding="utf-8", timeout=30,
        )
        restart_record = "RESTART INPUT:\nlist\nbye\nRESTART OUTPUT:\n" + restart.stdout + restart.stderr
        expected_list = "  Here are the tasks in your list:\n"
        for number, task in enumerate(displayed_tasks(saved_before_restart), 1):
            expected_list += f"  {number}.{task}\n"
        expected_list += PAGE_BREAK
        assert restart.returncode == 0 and not restart.stderr, "Restart failed"
        assert expected_list in restart.stdout, f"Restart list mismatch; expected:\n{expected_list}"
        saved_after_restart = save_path.read_text(encoding="utf-8") if save_path.exists() else None
        assert saved_after_restart == saved_before_restart, "Startup modified the save file"
    except (AssertionError, OSError, queue.Empty, subprocess.TimeoutExpired) as exception:
        error = str(exception) or "Timed out waiting for application output"
    finally:
        if process.poll() is None:
            process.kill()
        process.wait()
        reader.join(timeout=30)
        process.stdin.close()
        process.stdout.close()

    record = (f"Test case {title}\nINPUT:\n" + "\n".join(sent)
              + "\nOUTPUT:\n" + "".join(output_lines) + restart_record)
    if error:
        record += f"FAIL: {error}\nEXPECTED OUTPUT:\n{expected}\n"
    else:
        record += "PASS: console output, every file checkpoint, and restart loading\n"
    return record, error


def run_error_case(case, classes, work):
    """Inject documented file faults and verify rollback and preservation of disk data."""
    directory = Path(tempfile.mkdtemp(prefix="error-", dir=work))
    path = directory / "data" / "athena.txt"
    path.parent.mkdir()
    if "initial" in case:
        path.write_text(case["initial"], encoding="utf-8")
    elif "hex" in case:
        path.write_bytes(bytes.fromhex(case["hex"]))
    elif "repeat" in case:
        path.write_text("[T][ ] original\n" * case["repeat"], encoding="utf-8")
    elif "size" in case:
        path.write_bytes(b"x" * case["size"])
    elif case.get("kind") == "directory":
        path.mkdir()
    elif case.get("kind") == "parent_file":
        path.parent.rmdir()
        path.parent.write_text("keep this file", encoding="utf-8")
    command = ["java", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8",
               "-cp", str(classes), "athena.Athena"]
    process = subprocess.Popen(command, cwd=directory, stdin=subprocess.PIPE,
                               stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                               text=True, encoding="utf-8")
    output = []
    pending = queue.Queue()

    def collect():
        for line in process.stdout:
            output.append(line)
            pending.put(line)
        pending.put(None)

    reader = threading.Thread(target=collect, daemon=True)
    reader.start()
    error = None
    inputs = []
    try:
        if "runtime" in case:
            inputs.append("list")
            process.stdin.write("list\n")
            process.stdin.flush()
            separators = 0
            while separators < 4:
                line = pending.get(timeout=30)
                assert line is not None, "Application exited before fault injection"
                separators += line.rstrip("\n") == PAGE_BREAK
            if case["runtime"] == "directory":
                # Move only this test's known file within its isolated directory.
                path.rename(directory / "original.txt")
                path.mkdir()
            elif case["runtime"] == "external":
                path.write_text("[T][ ] external edit\n", encoding="utf-8")
        protected_path = path.parent if case.get("kind") == "parent_file" else path
        before = protected_path.read_bytes() if protected_path.is_file() else None
        inputs.extend(case.get("commands", ["todo must not be added", "bye"]))
        try:
            process.stdin.write("\n".join(inputs[1:] if "runtime" in case else inputs) + "\n")
            process.stdin.close()
        except BrokenPipeError:
            pass
        assert process.wait(timeout=30) == 0, "Unexpected application exit"
        reader.join(timeout=30)
        actual = "".join(output)
        position = 0
        for fragment in case["expected"]:
            found = actual.find(fragment, position)
            assert found >= 0, f"Missing expected fragment: {fragment!r}"
            position = found + len(fragment)
        for fragment in case.get("forbidden", []):
            assert fragment not in actual, f"Unexpected output: {fragment!r}"
        if before is None:
            assert protected_path.is_dir(), "Directory was replaced"
        else:
            assert protected_path.read_bytes() == before, "Existing bytes were changed"
        assert not list(path.parent.glob("athena-*.tmp")), "Temporary save file was left behind"
    except (AssertionError, OSError, queue.Empty, subprocess.TimeoutExpired) as exception:
        error = str(exception) or "Timed out waiting for output"
    finally:
        if process.poll() is None:
            process.kill()
        process.wait()
        reader.join(timeout=30)
        process.stdout.close()
    record = (f"Storage scenario: {case['name']}\nINPUT:\n" + "\n".join(inputs)
              + "\nOUTPUT:\n" + "".join(output))
    record += f"FAIL: {error}\nEXPECTED: {case['expected']}\n" if error else "PASS: fault handling and file preservation\n"
    return record, error


def main():
    """Compile and run cases in order, stopping at the first failure."""
    for executable in ("java", "javac"):
        version = subprocess.run([executable, "-version"], capture_output=True, text=True, check=True)
        version_text = version.stdout + version.stderr
        if not re.search(r'(?:version\s+")?25(?:\.|\s|\")', version_text):
            raise SystemExit(f"Java 25 is required: {version_text}")
        print(version_text.strip())
    work = ROOT / "_temp" / "save-ui"
    classes = work / "classes"
    classes.mkdir(parents=True, exist_ok=True)
    sources = sorted((ROOT / "src" / "main" / "java").rglob("*.java"))
    subprocess.run(["javac", "-d", str(classes), *map(str, sources)], check=True)
    cases = (ROOT / "test" / "ui-test-plan.md").read_text(encoding="utf-8").split("## Test case ")[1:]
    records = []
    for case in cases:
        record, error = run_case(case, classes, work)
        print(record)
        records.append(record)
        (work / "session.txt").write_text("\n".join(records), encoding="utf-8")
        if error:
            raise SystemExit(1)
    plan = (ROOT / "test" / "ui-test-plan.md").read_text(encoding="utf-8")
    error_cases = json.loads(re.search(r"```errors\n(.*?)\n```", plan, re.S)[1])
    for case in error_cases:
        record, error = run_error_case(case, classes, work)
        print(record)
        records.append(record)
        (work / "session.txt").write_text("\n".join(records), encoding="utf-8")
        if error:
            raise SystemExit(1)
    summary = f"PASS: {len(cases)} UI cases and {len(error_cases)} storage scenarios on Java 25."
    print(summary)
    with (work / "session.txt").open("a", encoding="utf-8") as record_file:
        record_file.write(summary + "\n")
    print(f"Complete session record: {work / 'session.txt'}")


if __name__ == "__main__":
    main()
