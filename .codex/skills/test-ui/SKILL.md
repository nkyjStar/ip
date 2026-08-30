---
name: test-ui
description: Run command-line UI test cases from test/ui-test-plan.md and compare actual output with expected output.
---

# UI testing

Use this skill when testing the Athena command-line interface.

## Workflow

1. Read `test/ui-test-plan.md` and use its test cases as the source of truth.
2. Ensure Java 25 is used, compile the application, and run each test case with its listed commands as standard input.
3. Compare the relevant program output with the expected output exactly, preserving meaningful whitespace and line breaks. Ignore only the explicitly documented dynamic or formatting fields.
4. Print a console record containing the complete input and output for every test case that ran.
5. Stop immediately at the first failure. Report the test case, expected output, and actual output; do not continue to later cases.
6. If all cases pass, report the complete session record and a concise pass summary.

When adding or changing tests, update `test/ui-test-plan.md` first. Each case must state its aim, input commands, expected output, and any comparison notes. Do not silently change expected output to match an implementation failure.
