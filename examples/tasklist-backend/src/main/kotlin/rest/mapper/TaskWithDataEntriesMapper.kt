package io.holunda.camunda.taskpool.example.tasklist.rest.mapper

import com.fasterxml.jackson.core.JsonProcessingException
import io.holunda.camunda.taskpool.api.business.DataEntryState
import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.camunda.taskpool.example.tasklist.rest.model.*
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
    Mapping(target = "currentState", source = "dataEntry.state.state"),
    Mapping(target = "currentStateType", source = "dataEntry.state.processingType"),
    Mapping(target = "protocol", expression="java(protocol.stream().map(entry -> dto(entry, dataEntry.getState())).collect(java.util.stream.Collectors.toList()))")
  )
  abstract fun dto(dataEntry: DataEntry, protocol: List<Modification>): DataEntryDto

  @Mappings(
    Mapping(target = "payload", source = "dataEntry.payload"),
    Mapping(target = "url", expression = "java(formUrlResolver.resolveUrl(dataEntry))"),
    Mapping(target = "currentState", source = "dataEntry.state.state"),
    Mapping(target = "currentStateType", source = "dataEntry.state.processingType"),
    Mapping(target = "protocol", expression="java(java.util.Collections.emptyList())")
  )
  abstract fun dto(dataEntry: DataEntry): DataEntryDto


  @Mappings(
    Mapping(target = "timestamp", source = "modification.time"),
    Mapping(target = "user", source = "modification.username"),
    Mapping(target = "state", source = "state.state"),
    Mapping(target = "stateType", source = "state.processingType"),
    Mapping(target = "log", source = "modification.log"),
    Mapping(target = "logDetails", source = "modification.logNotes")
  )
  abstract fun dto(modification: Modification, state: DataEntryState): ProtocolEntryDto

  @Mappings(
    Mapping(target = "task", source = "task"),
    Mapping(target = "dataEntries", source = "dataEntries")
  )
  abstract fun dto(taskWithDataEntries: TaskWithDataEntries): TaskWithDataEntriesDto


  fun toOffsetDateTime(@Valid time: Date?): OffsetDateTime? {
    return if (time == null) null else OffsetDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC)
  }

}
