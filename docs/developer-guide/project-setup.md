---
title: Project Setup
pageId: 'project-setup'
---

To develop and build the project, follow these instructions.

## Version control

To obtain the project sources, run:

```bash
git clone https://github.com/holunda-io/camunda-bpm-taskpool.git
cd camunda-bpm-taskpool
```

We use Gitflow. Start from the `develop` branch, create a `feature/<name>` branch,
and create a pull request when the feature is complete. Please squash your commits before submitting and use semantic commit messages where possible.

## Project Build

Run the following command to set up the development environment:

```bash
./mvnw clean install
```

## Integration Tests

By default, the build does not run the `failsafe` Maven plugin, which executes integration tests
(JUnit tests with class names ending in `ITest`). To run integration tests, execute:

```bash
./mvnw integration-test failsafe:verify -Pitest
```

## Project build modes and profiles

### Camunda Version

Choose the Camunda version by specifying the `camunda-ee` or `camunda-ce` profile. The default
is the Community Edition. Specify `-Pcamunda-ee` to switch to Camunda Enterprise Edition. This
requires a valid Camunda license. Place it in `~/.camunda/license.txt` and it will be detected
automatically.

### Database schema

Polyflow publishes PostgreSQL database changes through the
`polyflow-liquibase` module. Do not generate DDL from the JPA model. Consumer
applications include the required Polyflow master changelogs from their
central Liquibase master changelog; see the [Persistence configuration](../reference-guide/configuration/persistence.md).

Integration tests follow the same rule: use the `polyflow-liquibase` test
dependency and the appropriate core or view master changelog. Do not add
Flyway migrations for Polyflow tables.



### Build Documentation

We use MkDocs to generate the static documentation site and rely on Markdown where possible.
MkDocs is written in Python 3 and must be installed on your machine. Run the following commands:

```bash
python3 -m pip install --upgrade pip
python3 -m pip install -r ./docs/requirements.txt
```

To build the documentation, run:

```bash
mkdocs build
```

The documentation is generated in the `site` directory.

!!! note
    To develop the documentation in live mode, run `mkdocs serve` and open [http://localhost:8000/](http://localhost:8000/) in your browser.

## Continuous Integration

Travis CI builds all branches on each commit. In addition, a privately hosted Jenkins CI
builds releases.

## Release Management

Release management is configured for Sonatype Nexus (Maven Central).

### Which modules are deployed to the repository

Every module is enabled by default. To change this, add the property

```xml
<maven.deploy.skip>true</maven.deploy.skip>
```

inside the corresponding `pom.xml`. Currently, all examples are _EXCLUDED_ from publication into Maven Central.

### Trigger new release

!!! warning
    This operation requires special permissions.

We use Gitflow for development (see [A successful git branching model](http://nvie.com/posts/a-successful-git-branching-model/) for details). You can use Gitflow with native Git commands, but must then change the versions in the POMs manually. Therefore, we use the [mvn gitflow plugin](https://github.com/aleksandr-m/gitflow-maven-plugin/), which handles these tasks.

You can build a release with:

```bash
./mvnw gitflow:release-start
./mvnw gitflow:release-finish
```

This updates the versions in the `pom.xml` files and pushes the release tag to the `master` branch.

When changing the major or minor version, also update the Liquibase baseline
and `tagDatabase` tags in `polyflow-liquibase` to the same `major.minor`
value. The Liquibase integration tests derive this value from the module POM
version and fail the build when the database tag differs. Patch releases do not
change the Liquibase tag. For example, `4.7.1-SNAPSHOT` requires database tag
`4.7`. Run `./mvnw verify` before releasing; the release workflow runs these
tests and cannot publish a mismatched Liquibase tag.
It also updates the `develop` branch for the next development version.

### Trigger a deploy

!!! warning
    This operation requires special permissions.

CI currently deploys artifacts to Maven Central by using GitHub Actions.
A push to the `master` branch starts the corresponding build job; when it succeeds, the
artifacts are placed in OSS Sonatype staging repositories without manual intervention.

### Run deploy from local machine

!!! warning
    This operation requires special permissions.

To deploy from your local machine, you need GPG keys configured and must run the following command on the `master` branch:

```bash
export GPG_KEYNAME="<keyname>"
export GPG_PASSPHRASE="<secret>"
./mvnw clean deploy -B -DskipTests -DskipExamples -Prelease -Dgpg.keyname=$GPG_KEYNAME -Dgpg.passphrase=$GPG_PASSPHRASE
```

### Release to public repositories

!!! warning
     This operation requires special permissions.

The deployment job publishes artifacts to Nexus OSS staging repositories. Snapshots are published to the OSS Sonatype Snapshot
repository, and releases are published to Maven Central automatically.
