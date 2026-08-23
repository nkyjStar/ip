---
name: present-changes-visually
description: Generate a self-contained, GitHub-style split-view HTML page that visually presents changes in this Git repository. Use when asked to show, review, share, or inspect code changes visually; compare revisions, branches, commits, or the worktree; or create an HTML diff.
---

# Present Changes Visually

Generate one interactive HTML page containing every changed file as a side-by-side before/after diff. The page folds long unchanged runs, highlights changed words within modified lines, lets readers filter files, and includes collapsed panels for unchanged files.

## Generate the page

1. Treat this repository as the target unless the user identifies another repository.
2. Use `HEAD` as the before point and `WORKTREE` as the after point unless the user specifies comparison points. `WORKTREE` includes staged, unstaged, and untracked (but not ignored) files.
3. Write to `_temp/visual-diff.html` unless the user supplies an output path.
4. From the repository root, run:

   ```bash
   python .codex/skills/present-changes-visually/scripts/generate-split-view-diff.py \
     . HEAD WORKTREE _temp/visual-diff.html
   ```

   Comparison points may be any Git commit-ish, such as `HEAD~1`, a tag, branch, or commit SHA. Use `WORKTREE` for current files.
5. Confirm that the command succeeded and report the absolute output path. Do not open a browser unless asked.

## Verify output

Check that the page exists and that the generator summary reports the expected changed-file count. For a visual review, open the generated HTML file in a browser only when requested.

## Resource

Use `scripts/generate-split-view-diff.py` for generation. It uses only Python's standard library; syntax highlighting is loaded in the generated page from a CDN when network access is available, and the page remains usable without it.
