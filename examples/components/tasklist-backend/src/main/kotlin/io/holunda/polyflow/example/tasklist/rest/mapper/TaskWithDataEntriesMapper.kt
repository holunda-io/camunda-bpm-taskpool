package io.holunda.polyflow.example.tasklist.rest.mapper

import io.holunda.polyflow.example.tasklist.rest.model.DataEntryDto
import io.holunda.polyflow.example.tasklist.rest.model.ProtocolEntryDto
import io.holunda.polyflow.example.tasklist.rest.model.TaskDto
import io.holunda.polyflow.example.tasklist.rest.model.TaskWithDataEntriesDto
import io.holunda.polyflow.view.*
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.validation.Valid

/**
 * DTO mapper.
 */
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
    Mapping(target = "url", expression = "java(formUrlResolver.resolveUrl(task))"),
    Mapping(target = "createTime", expression = "java(toOffsetDateTime(task.getCreateTime()))"),
    Mapping(target = "dueDate", expression = "java(toOffsetDateTime(task.getDueDate()))"),
    Mapping(target = "followUpDate", expression = "java(toOffsetDateTime(task.getFollowUpDate()))")
  )
  abstract fun dto(task: Task): TaskDto

  @Mappings(
    Mapping(target = "payload", source = "dataEntry.payload"),
    Mapping(target = "url", expression = "java(formUrlResolver.resolveUrl(dataEntry))"),
    Mapping(target = "currentState", source = "dataEntry.state.state"),
    Mapping(target = "currentStateType", source = "dataEntry.state.processingType"),
    Mapping(target = "protocol", source = "dataEntry.protocol")
  )
  abstract fun dto(dataEntry: DataEntry): DataEntryDto

  @Mappings(
    Mapping(target = "timestamp", expression = "java(toOffsetDateTime(entry.getTime()))"),
    Mapping(target = "user", source = "entry.username"),
    Mapping(target = "state", source = "entry.state.state"),
    Mapping(target = "stateType", source = "entry.state.processingType"),
    Mapping(target = "log", source = "entry.logMessage"),
    Mapping(target = "logDetails", source = "entry.logDetails")
  )
  abstract fun dto(entry: ProtocolEntry): ProtocolEntryDto

  @Mappings(
    Mapping(target = "task", source = "task"),
    Mapping(target = "dataEntries", source = "dataEntries")
  )
  abstract fun dto(taskWithDataEntries: TaskWithDataEntries): TaskWithDataEntriesDto

  fun toOffsetDateTime(@Valid time: Instant?): OffsetDateTime? =
    if (time == null) null else OffsetDateTime.ofInstant(time, ZoneOffset.UTC)

}
