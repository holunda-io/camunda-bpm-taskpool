# Spring Boot 4 Migration Hints

This repository is already on the right side of the Spring Boot 2 to 3 migration:

- Spring Boot is managed as `3.5.16` in `bom/parent/pom.xml`.
- Java is configured as `17` in `.java-version` and the root `pom.xml`; local verification used Java 21 and Maven 3.9.2.
- Source code already mostly uses `jakarta.*`; the remaining `javax.*` imports are JDK XML types in `view/view-api/src/main/kotlin/sort/*` and are not part of the Jakarta EE migration.
- Auto-configuration registration already uses `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

The migration should therefore be treated as a Spring Boot **3.5.x to 4.x** migration, not as a full 2.x to 4.x jump.

## Recommended strategy

Use a gradual migration. First get a Boot 4 compile/test baseline with compatibility bridges, then remove the bridges track by track. Jackson is the exception: keep Jackson 2 where Camunda 7.24.x-ee requires it, while making Jackson 3 the default for Polyflow-owned code.

1. Upgrade the managed Spring Boot BOM in `bom/parent/pom.xml` from `3.5.16` to the target Boot 4 patch version.
2. Run OpenRewrite before hand-editing:

   ```bash
   ./mvnw org.openrewrite.maven:rewrite-maven-plugin:run \
     -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:RELEASE \
     -Drewrite.activeRecipes=org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0
   ```

3. Add short-lived compatibility bridges only if the first Boot 4 compile fails broadly:

   ```xml
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-classic</artifactId>
   </dependency>
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-test-classic</artifactId>
     <scope>test</scope>
   </dependency>
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-jackson2</artifactId>
   </dependency>
   ```

4. Remove `spring-boot-starter-classic` and `spring-boot-starter-test-classic` again before considering the migration complete. Keep `spring-boot-jackson2` only if it is scoped to Camunda-bound modules that still require Jackson 2.

## Highest-risk blockers in this repository

| Area | Evidence | Migration hint |
|---|---|---|
| Camunda 7 Spring Boot starter | `integration/camunda-bpm/pom.xml` manages `camunda-bpm.version` as `7.24.0`; several modules depend on `org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter`. | Camunda 7 CE does not provide Spring Boot 4-compatible artifacts. From the Boot 4 line onward, Camunda dependencies must be Enterprise Edition artifacts. |
| Jackson 2 and 3 side-by-side | 93 Kotlin imports of `com.fasterxml.jackson.*`; Jackson dependencies appear in `core/bus-jackson`, `core/taskpool/taskpool-event`, `integration/common/*`, `integration/camunda-bpm/*`, `view/jpa`, and `view/form-url-resolver`. Camunda 7.24.12-ee `camunda-bpm-spring-boot-starter-4` resolves Jackson 2 (`com.fasterxml.jackson.*`) dependencies. | Default Polyflow-owned code to Jackson 3 (`tools.jackson.*` for core/databind/datatype/module packages; annotations stay under `com.fasterxml.jackson.annotation`). Keep Jackson 2 only for Camunda/Spin/REST integration code paths, using `spring-boot-jackson2` as a scoped bridge rather than a global migration shortcut. |
| Boot starter modularization | Plain `spring-boot-starter` and `spring-boot-starter-test` are used across core, integration, and view modules. | Replace generic starters with Boot 4 modular starters where appropriate. Likely mappings here: `spring-boot-starter` -> `spring-boot-starter-classic` initially, then specific starters; `spring-boot-starter-test` -> `spring-boot-starter-test-classic` initially, then add test starters for JPA, MongoDB, Reactor, and Spring MVC as needed. |
| Direct `spring-boot-autoconfigure` dependency | `integration/common/tasklist-url-resolver/pom.xml` and `view/form-url-resolver/pom.xml`. | Boot 4 treats direct auto-configuration dependencies as non-public. Audit these modules and replace direct use with specific starters or the minimum public Spring Boot APIs needed for library auto-configuration. |
| Hibernate 7.1 / JPA | `view/jpa` uses `spring-boot-starter-data-jpa`, Hibernate annotations, custom test dialects, schema generation via `de.juplo:hibernate-maven-plugin`, and old dialect class names in the `generate-sql` profile. | Expect work in `view/jpa`. Update dialect names (`SQLServer2012Dialect`, `PostgreSQL10Dialect`) if Hibernate 7 rejects them, replace `org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy` with `org.springframework.boot.hibernate.SpringPhysicalNamingStrategy`, and verify generated DDL for H2, SQL Server, PostgreSQL, and MariaDB. |
| Testcontainers 2 | `view/jpa` uses `org.testcontainers:mariadb`; `view/mongo` uses `org.testcontainers:mongodb` and `MongoDBContainer`. | Update Testcontainers modules to Boot 4/Testcontainers 2 names, e.g. `org.testcontainers:testcontainers-mariadb` and `org.testcontainers:testcontainers-mongodb`, then fix any package relocations in `view/mongo/src/test/kotlin`. |
| JUnit vintage exclusions | `integration/camunda-bpm/taskpool-collector/pom.xml` and `integration/camunda-bpm/taskpool-job-sender/pom.xml` exclude `junit-vintage-engine` from Camunda test starter. | Keep checking this after the Camunda dependency decision. If Camunda test support still pulls JUnit 4/Vintage assumptions, that may block clean JUnit 6 alignment. |

## Camunda Enterprise-only decision

The Spring Boot 4 migration changes the support baseline for this repository:

- Camunda 7 Community Edition is no longer a supported dependency source for the Spring Boot 4 line.
- Camunda 7 Enterprise Edition is required because it provides the Spring Boot 4-compatible artifacts.
- The first Boot 4 migration task must switch Camunda dependency management and repository access to the Enterprise artifact source.
- CI and local developer builds need access to the Enterprise Maven repository before the Boot version is bumped.
- Release notes and consumer-facing documentation should explicitly state that Spring Boot 4-compatible Polyflow artifacts require Camunda Enterprise dependencies.
- If CE compatibility must remain available for users, it should stay on a separate Spring Boot 3.5-compatible maintenance line rather than being mixed into the Boot 4 branch.

## Build and dependency changes to make first

Update the central version only after third-party compatibility is checked:

```xml
<!-- bom/parent/pom.xml -->
<springboot.version>4.0.x</springboot.version>
```

Then review these dependencies:

| Current dependency | Where | Boot 4 action |
|---|---|---|
| `spring-boot-starter` | `core/datapool/datapool-core`, `core/taskpool/taskpool-core`, `core/spring-utils`, `integration/common/*`, `integration/camunda-bpm/*`, `view/simple` | Use `spring-boot-starter-classic` only as a temporary baseline, then replace with specific module starters. |
| `spring-boot-starter-test` | parent dependency management plus several module poms | Use `spring-boot-starter-test-classic` only temporarily, then add technology-specific test starters. |
| `spring-boot-starter-data-jpa` | `view/jpa`, plus test scope in taskpool/camunda modules | Keep, but add `spring-boot-starter-data-jpa-test` where tests rely on Boot JPA test support. |
| `spring-boot-starter-data-mongodb-reactive` | `view/mongo` | Keep, but add `spring-boot-starter-data-mongodb-reactive-test` if Boot 4 test auto-configuration is needed. |
| Raw Jackson 2 artifacts | multiple modules | Migrate to Jackson 3 artifacts/packages or bridge with `spring-boot-jackson2` during the transition. |
| `spring-boot-autoconfigure` | `integration/common/tasklist-url-resolver`, `view/form-url-resolver` | Remove or isolate behind Boot 4-supported public APIs. |

## Jackson 2 and 3 coexistence strategy

This project has a lot of intentional Jackson code, and Camunda 7.24.x-ee Boot 4 artifacts are Spring Boot 4-compatible but still Jackson 2-bound. Use both Jackson versions side by side:

- **Default:** Polyflow-owned modules migrate to Jackson 3.
- **Exception:** Camunda-bound modules keep Jackson 2 for Camunda Spin, Camunda REST, and Camunda job serialization code paths.
- **Boundary rule:** Do not pass Jackson concrete types (`ObjectMapper`, `JsonNode`, `TypeReference`, modules, serializers) across module boundaries between Jackson 2 and Jackson 3 lanes. Convert at the boundary using strings, byte arrays, maps, or domain DTOs.
- **Dependency rule:** Declare Jackson 2 dependencies only in modules that directly need Camunda/Spin compatibility. Do not keep Jackson 2 as a parent-level default for all modules.
- **Auto-configuration rule:** Keep Boot's default JSON stack on Jackson 3. Add `spring-boot-jackson2` only where Boot needs to auto-configure Jackson 2 for Camunda integration.

Camunda-specific evidence checked with EE repository access:

```text
org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-4:7.24.12-ee
  -> com.fasterxml.jackson.core:jackson-databind:2.21.4
  -> com.fasterxml.jackson.core:jackson-core:2.21.4
  -> com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.21.4
  -> com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.21.4
  -> com.fasterxml.jackson.module:jackson-module-parameter-names:2.21.4
```

### Jackson 3 lane

Migrate these modules to Jackson 3 unless a compile/runtime dependency proves they are Camunda-bound:

Start with these modules:

- `core/bus-jackson`: custom modules and polymorphic mapping.
- `core/taskpool/taskpool-event`: event serialization dependencies.
- `integration/common/variable-serializer`: `ObjectMapper`, `TypeReference`, Kotlin module, Java Time module.
- `integration/common/taskpool-sender` and `integration/common/datapool-sender`: command serialization.
- `view/jpa` and `view/form-url-resolver`: persistence conversion and JSON-based URL resolution.

Expected actions:

```text
com.fasterxml.jackson.core:*       -> tools.jackson.core:* except annotations
com.fasterxml.jackson.databind.*   -> tools.jackson.databind.*
com.fasterxml.jackson.datatype.*   -> tools.jackson.datatype.*
com.fasterxml.jackson.module.*     -> tools.jackson.module.*
com.fasterxml.jackson.annotation.* -> remains com.fasterxml.jackson.annotation.*
```

Review behavior, not just imports:

- Jackson 3 writes ISO date/time values by default instead of timestamps.
- Jackson 3 sorts properties alphabetically by default.
- `JavaTimeModule` is built into Jackson 3; remove manual registration when no longer needed.
- Re-check polymorphic typing in `core/bus-jackson` and Camunda job sender code. Prefer named subtypes over class-name based typing where possible.
- If serialized event payload compatibility is required, add golden-file tests before changing mapper defaults.

### Jackson 2 lane

Keep Jackson 2 in modules that directly depend on Camunda 7.24.x-ee JSON behavior:

- `integration/camunda-bpm/taskpool-collector`
- `integration/camunda-bpm/taskpool-job-sender`
- `integration/camunda-bpm/engine-client`
- `integration/camunda-bpm/springboot-autoconfigure`
- `integration/camunda-bpm/springboot-starter`

Expected actions:

- Replace Camunda dependencies with the `-4` Spring Boot starter artifacts where applicable, for example `camunda-bpm-spring-boot-starter-4`.
- Keep `camunda-spin-dataformat-json-jackson` on Jackson 2.
- Keep Jackson 2 imports (`com.fasterxml.jackson.core`, `com.fasterxml.jackson.databind`, `com.fasterxml.jackson.datatype`, `com.fasterxml.jackson.module`) only inside these Camunda-bound modules.
- Add tests proving Camunda job payload serialization/deserialization still reads existing data.
- Avoid exporting Jackson 2 `ObjectMapper` beans as general application defaults.

Compatibility option for the Jackson 2 lane:

```properties
spring.jackson.use-jackson2-defaults=true
```

Use that only for contexts where Jackson 2-compatible defaults are required. Do not enable it globally if the application default should be Jackson 3 behavior.

## JPA and Hibernate checklist

The main work is in `view/jpa`.

Update the `generate-sql` profile in `view/jpa/pom.xml`:

- Replace `org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy` with `org.springframework.boot.hibernate.SpringPhysicalNamingStrategy`.
- Check whether `org.hibernate.dialect.SQLServer2012Dialect` and `org.hibernate.dialect.PostgreSQL10Dialect` are still accepted under Hibernate 7.1; use the supported generic dialects if not.
- Replace `javax.xml.bind:jaxb-api` in the Hibernate plugin dependencies with a Jakarta JAXB artifact if the plugin still needs JAXB.
- Verify `de.juplo:hibernate-maven-plugin:2.1.1` works with Hibernate 7.1. If not, replace the schema-generation path before changing entity mappings.

Then regenerate and diff DDL for:

```bash
./mvnw -pl view/jpa -Pgenerate-sql clean process-classes
```

Also run the JPA integration tests against both local and Testcontainers-backed profiles:

```bash
./mvnw -pl view/jpa -Pitest verify
```

## MongoDB property changes

Runtime test code currently registers:

```kotlin
registry.add("spring.data.mongodb.uri") { mongoDBContainer.replicaSetUrl }
```

Boot 4 renames this to:

```kotlin
registry.add("spring.mongodb.uri") { mongoDBContainer.replicaSetUrl }
```

Update the occurrences in:

- `view/mongo/src/test/kotlin/io/holunda/polyflow/view/mongo/service/PolyflowMongoServiceChangeStreamChangeTrackingITest.kt`
- `view/mongo/src/test/kotlin/io/holunda/polyflow/view/mongo/service/PolyflowMongoServiceEventHandlerChangeTrackingITest.kt`
- `view/mongo/src/test/kotlin/io/holunda/polyflow/view/mongo/task/TaskRepositoryExtensionImplITest.kt`

Also decide whether the application needs explicit MongoDB UUID or decimal representation settings. Boot 4 no longer supplies every MongoDB codec default through Spring Data.

## Testing checklist

No current `@MockBean` or `@SpyBean` usage was found, so the `@MockitoBean` migration is probably not a large task.

Do expect these changes:

- Replace `org.testcontainers:mariadb` with `org.testcontainers:testcontainers-mariadb`.
- Replace `org.testcontainers:mongodb` with `org.testcontainers:testcontainers-mongodb`.
- Verify `MongoDBContainer` package names and APIs under Testcontainers 2.
- Add explicit Boot 4 test starters where required:
  - `spring-boot-starter-data-jpa-test` for JPA tests.
  - `spring-boot-starter-data-mongodb-reactive-test` for MongoDB reactive tests.
  - `spring-boot-starter-reactor-test` if replacing direct `reactor-test` with Boot-managed modular test support.
- Re-check Camunda test dependencies because the Camunda starter currently needs explicit `junit-vintage-engine` exclusions.

Suggested smoke commands:

```bash
./mvnw -pl core/bus-jackson test
./mvnw -pl view/mongo test
./mvnw -pl view/jpa -Pitest verify
./mvnw -pl integration/camunda-bpm -Pitest verify
```

## Spring Security

No direct Spring Security dependency or old DSL usage was found in this repository. Unless transitive starters add security during the Boot 4 upgrade, this should not be a primary migration track.

If a downstream application uses this library with Spring Security, document that Boot 4 implies Spring Security 7 and consumers must update to lambda-only DSL, `PathPatternRequestMatcher`, and `AuthorizationManager#authorize`.

## HTTP clients

No direct `RestTemplate`, `WebClient`, `RestClient`, Feign, `MockMvc`, or `TestRestTemplate` usage was found. There is no obvious HTTP-client migration in this repository.

Keep an eye on transitive Camunda REST/client usage, but do not spend migration effort here unless compilation or tests point to it.

## Suggested task order

1. Configure access to the Camunda Enterprise Maven repository in CI and local builds.
2. Switch Camunda dependency management from the CE artifact line to the Spring Boot 4-compatible Enterprise artifact line.
3. Confirm upstream support for Axon 4.13.x, Axon Kotlin 4.12.x, Axon Mongo 4.5, and `io.holixon.axon.gateway` with Spring Boot 4 / Spring Framework 7.
4. Create a Boot 4 branch and run the OpenRewrite Boot 4 recipe.
5. Bump `springboot.version` in `bom/parent/pom.xml`.
6. Add temporary `spring-boot-starter-classic` and `spring-boot-starter-test-classic` only if the initial compile is too noisy. Add `spring-boot-jackson2` only to the Camunda-bound modules that require Jackson 2 auto-configuration.
7. Compile the full reactor:

   ```bash
   ./mvnw clean compile
   ```

8. Fix dependency resolution and compile errors in this order: Camunda integration, Boot autoconfigure modules, Jackson 2/3 boundaries, JPA/Hibernate, Testcontainers.
9. Remove starter compatibility bridges. Keep the Jackson 2 bridge only where the Camunda lane still needs it.
10. Run focused module tests first, then the full build:

   ```bash
   ./mvnw test
   ./mvnw -Pitest verify
   ```

## Completion criteria

The migration is not done until:

- No `spring-boot-starter-classic` or `spring-boot-starter-test-classic` dependency remains.
- `spring-boot-jackson2` and Jackson 2 artifacts remain only in explicitly Camunda-bound modules; all other Polyflow-owned modules default to Jackson 3.
- Camunda dependencies resolve from the Enterprise artifact source, and documentation no longer implies Camunda CE support for the Boot 4 line.
- No non-Camunda production code imports `com.fasterxml.jackson.core`, `com.fasterxml.jackson.databind`, `com.fasterxml.jackson.datatype`, or `com.fasterxml.jackson.module`.
- No old MongoDB keys such as `spring.data.mongodb.uri` remain in tests or documentation unless intentionally documenting old versions.
- `view/jpa` DDL generation still works or is replaced with a maintained Hibernate 7-compatible path.
- Full unit and integration builds pass on Java 17 and Java 21.
