package io.holunda.polyflow.view.jpa

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.process.ProcessDefinitionsStartableByUserQuery
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("itest")
internal class JpaPolyflowViewServiceProcessDefinitionITest {

  @MockBean
  lateinit var queryUpdateEmitter: QueryUpdateEmitter

  @Autowired
  lateinit var jpaPolyflowViewService: JpaPolyflowViewService

  private val procDefId = UUID.randomUUID().toString()

  @Before
  internal fun `ingest events`() {

    jpaPolyflowViewService.on(
      event = ProcessDefinitionRegisteredEvent(
        processDefinitionId = procDefId,
        processDefinitionKey = "my-process-key",
        processDefinitionVersion = 13,
        applicationName = "test-application",
        processName = "My process",
        processVersionTag = "tag1",
        processDescription = "This is my process",
        formKey = "start-form",
        startableFromTasklist = true,
        candidateStarterGroups = setOf("muppets")
      )
    )

  }

  @After
  internal fun `cleanup projection`() {
    jpaPolyflowViewService.processDefinitionRepository.deleteAll()
  }

  @Test
  internal fun `should find process definition startable for user`() {
    val result = jpaPolyflowViewService.query(
      ProcessDefinitionsStartableByUserQuery(user = User("kermit", setOf("muppets")))
    )
    assertThat(result.size).isEqualTo(1)
    assertThat(result[0].processDefinitionId).isEqualTo(procDefId)
    assertThat(result[0].processDefinitionKey).isEqualTo("my-process-key")
    assertThat(result[0].processDefinitionVersion).isEqualTo(13)
    assertThat(result[0].processName).isEqualTo("My process")
    assertThat(result[0].processVersionTag).isEqualTo("tag1")
    assertThat(result[0].processDescription).isEqualTo("This is my process")
    assertThat(result[0].startableFromTasklist).isEqualTo(true)
    assertThat(result[0].formKey).isEqualTo("start-form")
    assertThat(result[0].candidateStarterUsers).isEmpty()
    assertThat(result[0].candidateStarterGroups).containsExactly("muppets")
  }

}
