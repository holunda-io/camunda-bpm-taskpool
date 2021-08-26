### Purpose

Building a central Task List and Business Data Entry List requires a UI integration logic between the use case agnostic
central component and use case specific forms for display of the User Tasks and Business Data Entries. There
exist multiple options to provide this UI integration, like dynamic loading of UI components from the distributed
use case specific application and process engines or simple redirection to those applications being able to 
display the requested form. In many those implementations, the process platform components like Task List and Business Data Entry List
need to resolve the particular URL of the process application endpoint. The `form-url-resolver` component
is designed exactly for this purpose, if this resolution is static and can be performed based on configuration. 

### Configuration

In order to use the `form-url-resolver` please add the following dependency to your project:

```xml
<dependency>
  <groupId>io.holunda.polyflow</groupId>
  <artifactId>polyflow-form-url-resolver</artifactId>
  <version>${polyflow.version}</version>
</dependency>
```

In your configuration, please add the following annotation to enable the resolver:

```kotlin
@EnablePropertyBasedFormUrlResolver
@Configuration
class MyTasklistConfiguration {
    
}
```

Using your `application.yaml` or corresponding properties file, please provide the configuration of the
URLs. Te configuration is separated into provision of some defaults (for user tasks, data entries, processes or even entire applications)
In general the resolution of the URL happens from most specific to most generic, see the example below. If the
specific request matches the configuration (e.g. URL for the User Task with process definition `task1` of the
application with application name `app1`) it will be returned (`https://app1.server.io/app/forms/task1/foo/${id}`), otherwise the
default for the application or even the default template is taken.

```yaml
polyflow:
  integration:
    form-url-resolver:
      defaultApplicationTemplate: "http://localhost:8080/${applicationName}"
      defaultDataEntryTemplate: "/${entryType}/${entryId}"
      defaultProcessTemplate: "/${processDefinitionKey}/${formKey}"
      defaultTaskTemplate:  "/forms/${formKey}/${id}"
      applications:
      - app1:
        url: "https://app1.server.io/app"
        tasks:
        - task1: "/forms/task1/foo/${id}"
        - task2: "/bar/2/foo/${id}"
        processes:
        - process1: "/proc-1/start"
        - process2: "/proc/2/begin"
      - app2:
        url: "https://foo.app2.com"
        tasks:
        - otherTask1: "/views/task1/${id}"
        - otherTask2: "/other/2/foo/${id}"
```
As you can see in the example above, the component supports simple text templating using `${}` to indicate
the template variable. The variables which can be used are direct attributes of the object for which the URL is 
resolved (see [View API](view-api.md)). The keys in the configuration are:

* Value of attribute `applicationName` for applications
* Value of attribute `processDefinitionKey` for processes
* Value of attribute `taskDefinitionKey` for tasks
* Value of attribute `entryType` for data entries




