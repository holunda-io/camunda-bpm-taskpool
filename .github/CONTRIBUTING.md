# Contributing Guide

There are several ways in which you may contribute to this project.

* [File issues](https://github.com/holuda-io/camunda-bpm-taskpool/issues)
* [Submit a pull requests](#submit-a-pull-request)

Read more on [how to get the project up and running](#project-setup).


## Submit a Pull Request

If you would like to submit a pull request make sure to 

- add test cases for the problem you are solving
- stick to project coding conventions


## Project Setup

_Perform the following steps to get a development setup up and running._

- `git clone https://github.com/holunda-io/camunda-bpm-taskpool.git`
- `cd camunda-bpm-taskpool`
- `./mvnw clean install`

## Version control

We are using gitflow in our git SCM. That means that you should start from `develop` branch,
create a `feature/<name>` out of it and once it is completed create a pull request containing 
it. Please squash your commits before submitting and use semantic commit messages, if possible. 
 
 



## Release management

See [RELEASE.MD](docs/RELEASE.MD)

## Profiles

### Camunda Version

You can choose the used Camunda version by specifying the profile camunda-ee or camunda-ce.
Specify `-Pcamunda-ee` to switch to Camunda Enterprise edition.

### Skip Frontend

If you are interested in backend only, specify the `-DskipFrontend` switch.
