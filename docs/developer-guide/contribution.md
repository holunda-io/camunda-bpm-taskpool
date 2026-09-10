---
title: Contribution
---

There are several ways in which you may contribute to this project.

* [File issues](https://github.com/holunda-io/camunda-bpm-taskpool/issues)
* Submit a pull requests

## Found a bug or missing feature?

Please [file an issue](https://github.com/holunda-io/camunda-bpm-taskpool/issues) in our
issue tracking system.

## Submit a Pull Request

If you found a solution to an [open issue](https://github.com/holunda-io/camunda-bpm-taskpool/issues)
and implemented it, we would be happy to add your contribution in the code base. For doing so,
please create a pull request. Prior to that, please make sure you:

- rebase against the `develop` branch
- stick to project coding conventions
- added test cases for the problem you are solving
- added docs, describing the change
- generally comply with codacy report

## Dependency Scopes

Published Polyflow libraries must not re-export Spring Framework or Spring Boot dependencies. Maven Enforcer rejects `org.springframework` and `org.springframework.boot` dependencies with `compile` scope.

Use `provided` scope when production code requires Spring APIs. Test dependencies may use `test` scope. The rule is intentionally limited to Spring dependencies until further consumer-provided library groups are agreed.
