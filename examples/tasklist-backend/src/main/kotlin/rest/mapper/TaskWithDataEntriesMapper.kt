package io.holunda.camunda.taskpool.example.tasklist.rest.mapper

import com.fasterxml.jackson.core.JsonProcessingException
import io.holunda.camunda.taskpool.example.tasklist.rest.model.DataEntryDto
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
    Mapping(target = "payload", source = "dataEntry.payload")
  )
  @Throws(JsonProcessingException::class)
  abstract fun dto(dataEntry: DataEntry): DataEntryDto

  @Mappings(
    Mapping(target = "task", source = "task"),
    Mapping(target = "dataEntries", source = "dataEntries")
  )
  abstract fun dto(taskWithDataEntries: TaskWithDataEntries): TaskWithDataEntriesDto


  fun toOffsetDateTime(@Valid time: Date?): OffsetDateTime? {
    return if (time == null) null else OffsetDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC)
  }

}
