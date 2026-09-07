# Spring Boot 4 Migration Hints

This repository is already on the right side of the Spring Boot 2 to 3 migration:

- Spring Boot is managed as `3.5.16` in `bom/parent/pom.xml`.
- Java is configured as `17` in `.java-version` and the root `pom.xml`; local checks used Java 21 and Maven 3.9.2.
- Source code already mostly uses `jakarta.*`; the remaining `javax.*` imports are JDK XML types in `view/view-api/src/main/kotlin/sort/*`, not Jakarta EE APIs.
- Auto-configuration registration already uses `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- Camunda engine and Camunda Spring Boot starter dependencies in the shipped Camunda integration modules are `provided`; Camunda starter/test dependencies used by this repository are mostly test support, not runtime dependencies exported to consumers.

The working hypothesis is therefore: **the Spring Boot 4 migration should be small and mostly build/test cleanup**, not a large enterprise-style phased migration.

## Current migration hypothesis

Do a compact, all-at-once migration in one branch:

1. Bump the managed Spring Boot version.
2. Run the OpenRewrite Boot 4 recipe.
3. Keep Camunda 7 CE dependencies as-is unless the compile/test result proves an API incompatibility.
4. Keep Jackson 2 only for the Camunda collector path where Camunda Spin/Jackson compatibility is needed.
5. Move the rest of Polyflow-owned serialization to Jackson 3.
6. Fix the small set of compile/test failures that actually appears.
7. Run broad verification after the full migration pass, rather than treating every small sub-step as a fully validated release candidate.

This differs from the earlier conservative plan: **do not switch to Camunda Enterprise just because Spring Boot 4 exists**. Since Camunda is a provided dependency and the used Camunda API is expected to be stable, the repository should not force an EE dependency line unless concrete build/runtime evidence requires it.

## Suggested first migration pass

Run OpenRewrite first, then hand-fix the remaining compile errors:

```bash
./mvnw org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:RELEASE \
  -Drewrite.activeRecipes=org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0
```

Then update:

```xml
<!-- bom/parent/pom.xml -->
<springboot.version>4.x.y</springboot.version>
```

Start with normal Boot 4 dependencies. Avoid compatibility bridges unless the build proves they are needed:

- Do **not** start with `spring-boot-starter-classic`.
- Do **not** start with `spring-boot-starter-test-classic`.
- Do **not** add `spring-boot-jackson2` globally.
- Add Jackson 2 support only where the collector/Camunda Spin path requires it.

## Area-specific hints

| Area | Current evidence | Migration hint |
|---|---|---|
| Camunda 7 | `camunda-engine` and `camunda-bpm-spring-boot-starter` are `provided` in the collector, job sender, and engine client modules. Autoconfigure/starter modules use Camunda starter only in `test` scope. | Keep Camunda CE coordinates for the first migration pass. Do not switch to EE or `-4` artifacts unless tests show the provided API assumption is wrong. If test-only Camunda starter compatibility breaks, solve it as test infrastructure, not as a product dependency decision. |
| Jackson 2 collector lane | Collector/job-sender code and tests use Camunda Spin and Jackson 2 APIs. Camunda 7.24.x artifacts resolve `com.fasterxml.jackson.*`. | Keep Jackson 2 where Camunda Spin/collector serialization needs it. Avoid exposing Jackson 2 mapper types as default application JSON infrastructure. |
| Jackson 3 default | Many Polyflow-owned modules import `com.fasterxml.jackson.*`: `core/bus-jackson`, `core/taskpool/taskpool-event`, `integration/common/*`, `view/jpa`, `view/form-url-resolver`. | Migrate non-Camunda-owned serialization to Jackson 3 (`tools.jackson.*` for core/databind/datatype/module packages; annotations remain `com.fasterxml.jackson.annotation`). |
| Boot starter modularization | Generic `spring-boot-starter` and `spring-boot-starter-test` appear across modules. | Leave existing starters alone initially if Boot 4 accepts them. Replace with modular starters only when required by compile/test failures or to remove deprecation warnings after the baseline works. |
| Direct `spring-boot-autoconfigure` | `integration/common/tasklist-url-resolver` and `view/form-url-resolver` depend on it directly. | Check compile results first. If Boot 4 rejects direct use or relocated APIs, replace with the smallest supported public API dependency. |
| JPA / Hibernate | `view/jpa` has custom DDL generation, Hibernate dialect references, and `SpringPhysicalNamingStrategy`. | Expect possible small fixes: naming strategy package, dialect names, JAXB artifact in the DDL profile, or Hibernate plugin compatibility. Validate DDL after compile succeeds. |
| MongoDB tests | Test code registers `spring.data.mongodb.uri`. | Rename to `spring.mongodb.uri` if Boot 4 property binding requires it. |
| Testcontainers | `view/jpa` and `view/mongo` use Testcontainers 1-style module names. | Update to Testcontainers 2 module coordinates only if managed Boot 4 dependencies require it or compile fails. |
| Spring Security / HTTP clients | No direct Security DSL or Spring HTTP client migration surface was found. | Do not create migration work here unless the build reveals transitive failures. |

## Jackson 2 and 3 coexistence rule

Jackson 2 and 3 can coexist because their core packages differ:

- Jackson 2 core/databind/datatype/module APIs use `com.fasterxml.jackson.*`.
- Jackson 3 core/databind/datatype/module APIs use `tools.jackson.*`.
- Jackson annotations remain under `com.fasterxml.jackson.annotation`.

Use this split intentionally:

- **Default lane:** Polyflow-owned serialization moves to Jackson 3.
- **Collector lane:** Camunda collector/Camunda Spin serialization remains on Jackson 2.
- **Boundary rule:** Do not pass Jackson concrete types (`ObjectMapper`, `JsonNode`, `TypeReference`, modules, serializers) between the Jackson 2 and Jackson 3 lanes. Pass strings, byte arrays, maps, or domain DTOs instead.
- **Bean rule:** Do not let a Jackson 2 `ObjectMapper` become the default application mapper for non-Camunda code.

## Suggested validation flow

Use lighter feedback during the migration pass, then broad verification at the end:

1. Compile the full reactor:

   ```bash
   ./mvnw clean compile
   ```

2. Run focused tests for changed surfaces:

   ```bash
   ./mvnw -pl core/bus-jackson test
   ./mvnw -pl integration/camunda-bpm/taskpool-collector test
   ./mvnw -pl integration/camunda-bpm/taskpool-job-sender test
   ./mvnw -pl view/mongo test
   ./mvnw -pl view/jpa -Pitest verify
   ```

3. Run the full validation once the migration compiles and focused tests are stable:

   ```bash
   ./mvnw test
   ./mvnw -Pitest verify
   ```

## Completion criteria

The migration is done when:

- The repository builds on the target Spring Boot 4 version.
- Camunda CE remains a provided dependency unless concrete evidence forced a different decision.
- The collector/Camunda Spin path can still read and write its existing Jackson 2 payloads.
- Non-Camunda Polyflow-owned serialization defaults to Jackson 3.
- Jackson 2 concrete types do not leak into Jackson 3-owned module APIs.
- Any Boot 4 property changes discovered by tests are fixed.
- JPA DDL generation still works or has a documented replacement path.
- Full unit and integration validation passes.
