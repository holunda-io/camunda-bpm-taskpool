---
title: Project Setup
pageId: 'project-setup'
---

If you are interested in developing and building the project please follow the following instruction.

## Version control

To get sources of the project, please execute:

```bash
git clone https://github.com/holunda-io/camunda-bpm-taskpool.git
cd camunda-bpm-taskpool
```

We are using gitflow in our git SCM. That means that you should start from `develop` branch,
create a `feature/<name>` out of it and once it is completed create a pull request containing
it. Please squash your commits before submitting and use semantic commit messages, if possible.

## Project Build

Perform the following steps to get a development setup up and running.

```bash
./mvnw clean install
```

## Integration Tests

By default, the build command will ignore the run of `failsafe` Maven plugin executing the integration tests
(usual JUnit tests with class names ending with ITest). In order to run integration tests, please
call from your command line:

```bash
./mvnw integration-test failsafe:verify -Pitest
```

## Project build modes and profiles

### Camunda Version

You can choose the used Camunda version by specifying the profile `camunda-ee` or `camunda-ce`. The default
version is a Community Edition. Specify `-Pcamunda-ee` to switch to Camunda Enterprise edition. This will
require a valid Camunda license. You can put it into a file `~/.camunda/license.txt` and it will be detected
automatically.

### Generate SQL DDL

If you are using RDBMS (for example for the Polyflow JPA View or/and JPA storage of Axon Entities), you will require the SQL DDL.
Consider to edit the `view/jpa/src/sql/persistence.xml` descriptor to control what to include into DDL generation.
You can generate this by executing the following build command:

```bash
./mvnw -Pgenerate-sql -f view/jpa
```



### Build Documentation

We are using MkDocs for generation of a static site documentation and rely on Markdown as much as possible.
MkDocs is a written in Python 3 and needs to be installed on your machine. For the installation please run the following
command from your command line:

```bash
python3 -m pip install --upgrade pip
python3 -m pip install -r ./docs/requirements.txt
```

For creation of documentation, please run:

```bash
mkdocs build
```

The docs are generated into `site` directory.

!!! note
    If you want to develop your docs in 'live' mode, run `mkdocs serve` and access the [http://localhost:8000/](http://localhost:8000/) from your browser.

## Continuous Integration

Travis CI is building all branches on commit hook. In addition, a private-hosted Jenkins CI
is used to build the releases.

## Release Management

Release management has been set up for use of Sonatype Nexus (= Maven Central)

### What modules get deployed to repository

Every module is enabled by default. If you want to change this, please provide the property

```xml
<maven.deploy.skip>true</maven.deploy.skip>
```

inside the corresponding `pom.xml`. Currently, all examples are _EXCLUDED_ from publication into Maven Central.

### Trigger new release

!!! warning
    This operation requires special permissions.

We use gitflow for development (see [A successful git branching model](http://nvie.com/posts/a-successful-git-branching-model/) for more details). You could use gitflow with native git commands, but then you would have to change the versions in the poms manually. Therefore, we use the [mvn gitflow plugin](https://github.com/aleksandr-m/gitflow-maven-plugin/), which handles this and other things nicely.

You can build a release with:

```bash
./mvnw gitflow:release-start
./mvnw gitflow:release-finish
```

This will update the versions in the `pom.xml` s accordingly and push the release tag to the `master` branch
and update the `develop` branch for the new development version.

### Trigger a deploy

!!! warning
    This operation requires special permissions.

Currently, CI allows for deployment of artifacts to Maven Central and is executed using github actions.
This means, that a push to `master` branch will start the corresponding build job, and if successful the
artifacts will get into `Staging Repositories` of OSS Sonatype without manual intervention.

### Run deploy from local machine

!!! warning
    This operation requires special permissions.

If you still want to execute the deployment from your local machine, you need to have GPG keys at place and
to execute the following command on the `master` branch:

```bash
export GPG_KEYNAME="<keyname>"
export GPG_PASSPHRASE="<secret>"
./mvnw clean deploy -B -DskipTests -DskipExamples -Prelease -Dgpg.keyname=$GPG_KEYNAME -Dgpg.passphrase=$GPG_PASSPHRASE
```

### Release to public repositories

!!! warning
     This operation requires special permissions.

The deployment job will publish the artifacts to Nexus OSS staging repositories. Currently, all snapshots get into OSS Sonatype Snapshot
repository and all releases to Maven Central automatically.
