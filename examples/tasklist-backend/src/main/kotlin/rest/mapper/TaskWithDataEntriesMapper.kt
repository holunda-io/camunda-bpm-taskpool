package io.holunda.camunda.taskpool.example.tasklist.rest.mapper

import com.fasterxml.jackson.core.JsonProcessingException
import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.camunda.taskpool.example.tasklist.rest.model.DataEntryDto
import io.holunda.camunda.taskpool.example.tasklist.rest.model.DataEntryProtocolDto
import io.holunda.camunda.taskpool.example.tasklist.rest.model.TaskDto
import io.holunda.camunda.taskpool.example.tasklist.rest.model.TaskWithDataEntriesDto
import io.holunda.camunda.taskpool.view.DataEntry
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.FormUrlResolver
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy
import org.springframework.beans.factory.annotation.Autowired
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import javax.validation.Valid

@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.ERROR
)
abstract class TaskWithDataEntriesMapper {

  @Suppress("unused")
  @Autowired
  lateinit var formUrlResolver: FormUrlResolver

  @Mappings(
    Mapping(target = "processName", source = "sourceReference.name"),
    Mapping(target = "url", expression = "java(formUrlResolver.resolveUrl(task))")
  )
  abstract fun dto(task: Task): TaskDto

  @Mappings(
    Mapping(target = "payload", source = "dataEntry.payload"),
    Mapping(target = "url", expression = "java(formUrlResolver.resolveUrl(dataEntry))"),
    Mapping(target = "state", source = "dataEntry.state.state"),
    Mapping(target = "stateType", source = "dataEntry.state.processingType"),
    Mapping(target = "protocol", source="protocol")
  )
  @Throws(JsonProcessingException::class)
  abstract fun dto(dataEntry: DataEntry, protocol: List<Modification>): DataEntryDto

  @Mappings(
    Mapping(target = "payload", source = "dataEntry.payload"),
    Mapping(target = "url", expression = "java(formUrlResolver.resolveUrl(dataEntry))"),
    Mapping(target = "state", source = "dataEntry.state.state"),
    Mapping(target = "stateType", source = "dataEntry.state.processingType"),
    Mapping(target = "protocol", expression="java(java.util.Collections.emptyList())")
  )
  @Throws(JsonProcessingException::class)
  abstract fun dto(dataEntry: DataEntry): DataEntryDto

  @Mappings(
    Mapping(target = "timestamp", source = "time"),
    Mapping(target = "user", source = "username"),
    Mapping(target = "log", source = "log"),
    Mapping(target = "logDetails", source = "logNotes")
  )
  @Throws(JsonProcessingException::class)
  abstract fun dto(modification: Modification): DataEntryProtocolDto

  @Mappings(
    Mapping(target = "task", source = "task"),
    Mapping(target = "dataEntries", source = "dataEntries")
  )
  abstract fun dto(taskWithDataEntries: TaskWithDataEntries): TaskWithDataEntriesDto


  fun toOffsetDateTime(@Valid time: Date?): OffsetDateTime? {
    return if (time == null) null else OffsetDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC)
  }

}
