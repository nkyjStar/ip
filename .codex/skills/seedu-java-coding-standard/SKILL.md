---
name: seedu-java-coding-standard
description: Apply the SE-EDU basic and intermediate Java coding standard when creating, reviewing, or refactoring Java code in this project.
---

# SE-EDU Java Coding Standard

Apply the SE-EDU Java coding standard at https://se-education.org/guides/conventions/java/intermediate.html to all Java code in this project.

In particular:

- Put every class in a meaningful lowercase package unless the repository explicitly requires the default package or a fixed source layout that is incompatible with package declarations. For this project, keep the default package because Java files must remain directly under `src/main/java`.
- Use PascalCase for classes, camelCase for variables and methods, and SCREAMING_SNAKE_CASE for constants. Name booleans with prefixes such as `is`, `has`, or `can`.
- Use four spaces for indentation, K&R braces, spaces around operators and after commas, and a hard maximum line length of 120 characters.
- Initialize variables at declaration where practical, keep them in the smallest scope, and use explicit imports.
- Keep fields private unless there is a compelling design reason otherwise; expose behavior through methods.
- Always use braces for loops and conditionals.
- Add descriptive Javadocs to public classes and public methods, except getters/setters and valid overrides.
- Keep comments in English using American spelling, and separate logical units with blank lines.

When the guide does not cover a topic, follow the Google Java Style Guide.
