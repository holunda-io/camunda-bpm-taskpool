[![stable](https://img.shields.io/badge/lifecycle-STABLE-green.svg)](https://github.com/holisticon#open-source-lifecycle)
[![Master and Snapshot release](https://github.com/holunda-io/camunda-bpm-taskpool/actions/workflows/release.yml/badge.svg)](https://github.com/holunda-io/camunda-bpm-taskpool/actions/workflows/release.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/io.holunda.polyflow/polyflow-taskpool-dependencies)](https://central.sonatype.com/artifact/io.holunda.polyflow/polyflow-taskpool-dependencies)
[![Code Coverage](https://codecov.io/gh/holunda-io/camunda-bpm-taskpool/branch/master/graph/badge.svg)](https://codecov.io/gh/holunda-io/camunda-bpm-taskpool)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/6e15ca2f4ab64cfe85a736f324136086)](https://www.codacy.com/gh/holunda-io/camunda-bpm-taskpool/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=holunda-io/camunda-bpm-taskpool&amp;utm_campaign=Badge_Grade)
[![OpenHUB](https://www.openhub.net/p/camunda-bpm-taskpool/widgets/project_thin_badge.gif)](https://www.openhub.net/p/camunda-bpm-taskpool)

![Logo](docs/img/Positive@2x.png)
## Polyflow Taskpool and Datapool

This is an experimental branch, as a POC to remove Camunda as a dependency from the collector and using [Process Engine API](https://github.com/bpm-crafters/process-engine-api) instead.
This gives the using application the freedom to choose any supported process engine. Of course this comes with some limitations. Currently only simple task sender is supported and there is no support for process definitions at all. 
The resulting version has been testes with [process-engine-adapter-camunda-7](https://github.com/bpm-crafters/process-engine-adapters-camunda-7) 2026.06.01 (embedded) and
[process-engine-adapter-camunda-platform-c8](https://github.com/bpm-crafters/process-engine-adapters-camunda-8) 2025.05.2 (due to spring version compatibility issues) and some limitations (e.g. canidates is a string containing an array (might already be fixed in a newer version of the c8 adapter)).



> A component library for building enterprise-wide process platforms with multiple process engines like Camunda Platform.

In the last years, we built different process applications on behalf of the customer several times. It turned out that some issues occurred every
time during the implementation.

These were:

* coping with performance issues if big amount of tasks is available
* creating high-performance custom queries for pre-loading process variables for tasks
* creating high-performance custom queries to pre-load business data associated with the process instance
* high-performance retrieving a list of tasks from several process engines
* repetitive queries with same result
* creating an archive view for business data items handled during the process execution
* creating an audit log of changes performed on business data items

![Polyflow Hero](docs/img/polyflow-hero-530x406.png)

We decided to stop repetitive work and release an open-source library which builds a foundation for solving these problems.

### Features

* User task API providing attributes important for processing
* Mirroring tasks: provides a copy of all tasks in the system
* Reacts on all task life cycle events fired by the process engine
* High performance queries: creates read-optimized projections including task-, process- and business data
* Centralized task list: allows collecting tasks from multiple engines
* Data enrichment: enrich tasks with business data
* Data entries API providing attributes important for processing
* Audit-Trail creation on business event emission

### Where to start

Sounds interesting for you? If you want to try it out, please read the
[Getting Started](https://www.holunda.io/camunda-bpm-taskpool/stable/getting-started/) section.

### Getting more help

If you have any questions regarding the main concepts, configuration of individual components of Polyflow, please have a look on
the [Reference Guide](https://www.holunda.io/camunda-bpm-taskpool/stable/reference-guide/) acting as a primary documentation. If you want to have a technical
discussion on any issue, feel free to look into [GitHub Project Discussion](https://github.com/holunda-io/camunda-bpm-taskpool/stable/discussions/)

Finally, there is [![Slack](https://img.shields.io/badge/slack-@holunda/taskpool-green.svg?logo=slack")](https://holunda.slack.com/messages/taskpool/) available, if you want to discuss more issues with the developers.

### Working Example

Studying and understanding the functionality is easier if you start with our working example described in
the [Example Section](https://www.holunda.io/camunda-bpm-taskpool/stable/examples/).

### License

This library is developed under

[![Apache 2.0 License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](/LICENSE)

### Contribution

This project is open source, and we love if the community contributes to this project. If you are willing to help, start
with [Developer Guide](https://www.holunda.io/camunda-bpm-taskpool/stable/developer-guide/contribution.html).

### Sponsors and Customers

[![sponsored](https://img.shields.io/badge/sponsoredBy-Holisticon-red.svg)](https://holisticon.de/)
