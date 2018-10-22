package io.holunda.camunda.taskpool.example.tasklist.rest.mapper

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.example.tasklist.rest.model.DataEntryDto
import io.holunda.camunda.taskpool.example.tasklist.rest.model.TaskDto
import io.holunda.camunda.taskpool.example.tasklist.rest.model.TaskWithDataEntriesDto
import io.holunda.camunda.taskpool.view.DataEntry
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import javax.validation.Valid

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
abstract class TaskWithDataEntriesMapper {

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @Mappings(
    Mapping(target = "processName", ignore = true),
    Mapping(target = "url", ignore = true)
  )
  abstract fun dto(task: Task): TaskDto

  @Mappings(
    Mapping(target = "payload", source="dataEntry.payload")
  )
  @Throws(JsonProcessingException::class)
  abstract fun dto(dataEntry: DataEntry): DataEntryDto

  @Mappings(
    Mapping(target = "task", source = "task"),
    Mapping(target = "dataEntries", source = "dataEntries")
  )
  abstract fun dto(taskWithDataEntries: TaskWithDataEntries): TaskWithDataEntriesDto


  fun toLocalDateTime(@Valid time: OffsetDateTime?): LocalDateTime? {
    return time?.toLocalDateTime()
  }

  fun toOffsetDateTime(@Valid time: LocalDateTime?): OffsetDateTime? {
    return time?.atOffset(ZoneOffset.UTC)
  }

  fun toOffsetDateTime(@Valid time: Date?): OffsetDateTime? {
    return if (time == null) null else OffsetDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC)
  }

  fun toDate(@Valid time: OffsetDateTime?): Date? {
    return if (time == null) null else Date.from(time.toInstant())
  }
}
