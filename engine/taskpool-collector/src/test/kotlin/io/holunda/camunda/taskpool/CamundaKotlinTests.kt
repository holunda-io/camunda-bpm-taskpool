package io.holunda.camunda.taskpool

import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.extension.mockito.QueryMocks
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit

class CamundaKotlinTests {

  @get: Rule
  val thrown = ExpectedException.none()

  @get: Rule
  val mockitoRule = MockitoJUnit.rule()

  @Mock
  lateinit var repositoryService: RepositoryService

  @Test
  fun `should extract process definition key`() {
    assertThat(extractKey("myProcess")).isEqualTo("myProcess")
    assertThat(extractKey("myProcess:123")).isEqualTo("myProcess")
  }

  @Test
  fun `should handle null`() {
    thrown.expectMessage("Process definition id must not be null.")
    thrown.expect(IllegalArgumentException::class.java)
    extractKey(null)
  }

  @Test
  fun `should load process name`() {

    // FIXME: replace with ProcessDefinitionFake from camunda-bpm-mockito > 4.1
    val processDefinition = ProcessDefinitionFake.builder()
      .id("process:1")
      .name("My Process")
      .build()
    val query = QueryMocks.mockProcessDefinitionQuery(repositoryService).singleResult(processDefinition)

    assertThat(loadProcessName("process:1", repositoryService)).isEqualTo("My Process")
    verify(query).processDefinitionId(processDefinition.id)
  }

  @Test
  fun `should throw exception if process is not found`() {

    val id = "process:1"
    QueryMocks.mockProcessDefinitionQuery(repositoryService).singleResult(null)
    thrown.expectMessage("Process definition could not be resolved for id $id")
    thrown.expect(IllegalArgumentException::class.java)

    loadProcessName(id, repositoryService)

  }

}
