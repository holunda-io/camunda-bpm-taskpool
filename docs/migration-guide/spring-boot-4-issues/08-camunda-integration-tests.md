# Stabilize Camunda collector and job sender integration tests

## What to build

Stabilize Camunda collector and job sender integration behavior on Camunda Enterprise Spring Boot 4 artifacts. This slice proves that the Camunda-bound Jackson 2 lane works in the integration modules and that collector/job sender behavior remains compatible.

## Acceptance criteria

- [ ] Camunda collector integration tests compile and run against Enterprise Spring Boot 4 artifacts.
- [ ] Camunda job sender integration tests compile and run against Enterprise Spring Boot 4 artifacts.
- [ ] Camunda Spin JSON/Jackson usage remains on the Jackson 2 lane and does not become the default application JSON stack.
- [ ] Existing task, process-definition, process-instance, and variable collection behavior is covered by passing tests.
- [ ] JUnit Vintage exclusions and Camunda test starter behavior are revalidated on the Boot 4 artifact line.

## Blocked by

- Issue 1: Prepare Camunda Enterprise access for Boot 4 artifacts
- Issue 2: Create the Spring Boot 4 baseline with Camunda Enterprise artifacts
- Issue 3: Isolate the Camunda Jackson 2 lane
