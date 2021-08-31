package io.holunda.polyflow.example.tasklist.rest.mapper

import io.holunda.polyflow.example.tasklist.rest.model.ProcessDefinitionDto
import io.holunda.polyflow.view.FormUrlResolver
import io.holunda.polyflow.view.ProcessDefinition
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy
import org.springframework.beans.factory.annotation.Autowired

/**
 * REST Mapper for process definitions.
 */
@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.ERROR
)
abstract class ProcessDefinitionMapper {

  /**
   * Maps to DTO.
   */
  @Mappings(
    Mapping(target = "definitionId", source = "processDefinitionId"),
    Mapping(target = "definitionKey", source = "processDefinitionKey"),
    Mapping(target = "definitionVersion", source = "processDefinitionVersion"),
    Mapping(target = "versionTag", source = "processVersionTag"),
    Mapping(target = "applicationName", source = "applicationName"),
    Mapping(target = "processName", source = "processName"),
    Mapping(target = "description", source = "processDescription"),
    Mapping(target = "formKey", source = "formKey"),
    Mapping(target = "candidateUsers", source = "candidateStarterUsers"),
    Mapping(target = "candidateGroups", source = "candidateStarterGroups"),
    Mapping(target = "url", expression = "java(formUrlResolver.resolveUrl(processDefinition))")
  )
  abstract fun dto(processDefinition: ProcessDefinition): ProcessDefinitionDto

  @Suppress("unused")
  @Autowired
  lateinit var formUrlResolver: FormUrlResolver
}

