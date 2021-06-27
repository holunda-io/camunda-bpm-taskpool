[![Build Status](https://github.com/holunda-io/camunda-bpm-taskpool/workflows/Development%20braches/badge.svg)](https://github.com/holunda-io/camunda-bpm-taskpool/actions)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.holunda.taskpool/camunda-bpm-taskpool/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.holunda.taskpool/camunda-bpm-taskpool)
[![Code Coverage](https://codecov.io/gh/holunda-io/camunda-bpm-taskpool/branch/master/graph/badge.svg)](https://codecov.io/gh/holunda-io/camunda-bpm-taskpool)
[![Codacy](https://api.codacy.com/project/badge/Grade/653136bd5cad48c8a9f2621ee304ff26)](https://app.codacy.com/app/zambrovski/camunda-bpm-taskpool?utm_source=github.com&utm_medium=referral&utm_content=holunda-io/camunda-bpm-taskpool&utm_campaign=Badge_Grade_Dashboard)
[![OpenHUB](https://www.openhub.net/p/camunda-bpm-taskpool/widgets/project_thin_badge.gif)](https://www.openhub.net/p/camunda-bpm-taskpool)

![Logo](docs/img/Positive@2x.png)
## Polyflow Taskpool and Datapool

> A component library for building enterprise-wide process platforms with multiple process engines like Camunda BPM.

<img src="docs/img/polyflow-hero-530x406.png?raw=true" alt="Polyflow Hero" title="Polyflow Hero" align="right" />

In the last five years, we built different process applications on behalf of the customer several times. It turned out that some of the issues occurred every
time during the implementation.

These were:

* coping with performance issues if big amount of tasks is available
* creating high-performance custom queries for pre-loading process variables for tasks
* creating high-performance custom queries to pre-load business data associated with the process instance
* high-performance retrieving a list of tasks from several process engines
* repetitive queries with same result
* creating an archive view for business data items handled during the process execution
* creating an audit log of changes performed on business data items

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

Sounds interesting for you? A good starting point for reading is the
[Introduction](https://www.holunda.io/camunda-bpm-taskpool/introduction/motivation.html) section.

### Getting more help

If you have any questions regarding the main concepts, configuration of individual components of Polyflow, please have a look on
the [Reference Guide](https://www.holunda.io/camunda-bpm-taskpool/reference-guide/) acting as a primary documentation. If you want to have a technical
discussion on any issue, feel free to look into [GitHub Project Discussion](https://github.com/holunda-io/camunda-bpm-taskpool/discussions/)

Finally, there are:
- [![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/holunda-io/camunda-bpm-taskpool?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
- [![Slack](https://img.shields.io/badge/slack-@holunda/taskpool-green.svg?logo=slack")](https://holunda.slack.com/messages/taskpool/)
  
available, if you want to discuss more issues with the developers.

### Working Example

Studying and understanding the functionality is easier if you start with our working example described in
the [Example Section](https://www.holunda.io/camunda-bpm-taskpool/examples/example-approval.html).

### License

This library is developed under

[![Apache 2.0 License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.holunda.io/camunda-bpm-taskpool/license)

### Contribution

This project is open source and we love if the community contributes to this project. If you are willing to help, start
with [Developer Guide](https://www.holunda.io/camunda-bpm-taskpool/developer-guide/contribution.html).

### Sponsors and Customers

[![sponsored](https://img.shields.io/badge/sponsoredBy-Holisticon-red.svg)](https://holisticon.de/)
