# Spring Boot 4 Migration Issue Breakdown

Local issue files for the revised Spring Boot 4 migration. The plan assumes a compact all-at-once migration with final broad validation, not a long sequence of separately validated migration tracks.

| Order | Issue | Type | Blocked by |
|---:|---|---|---|
| 1 | [Perform a minimal Spring Boot 4 migration pass](01-minimal-spring-boot-4-migration-pass.md) | AFK | None |
| 2 | [Default to Jackson 3 while keeping the collector on Jackson 2](02-jackson3-default-collector-jackson2.md) | AFK | Issue 1 |
| 3 | [Fix observed Spring Boot 4 test and runtime breaks](03-fix-observed-boot4-test-and-runtime-breaks.md) | AFK | Issues 1, 2 |
| 4 | [Run final Spring Boot 4 verification and update docs](04-final-verification-and-docs.md) | AFK | Issue 3 |
