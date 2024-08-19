package io.holunda.polyflow.view.mongo.task

import io.holunda.polyflow.view.mongo.TaskPoolMongoViewConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*


@TestPropertySource(
  properties = [
    "polyflow.view.mongo.changeTrackingMode=EVENT_HANDLER",
  ]
)
@Testcontainers
@DataMongoTest
@ActiveProfiles("itest-standalone")
@ContextConfiguration(classes = [TaskPoolMongoViewConfiguration::class])
class TaskRepositoryExtensionImplITest {

  companion object {
    const val USER_ONE = "user_1"
    const val USER_TWO = "user_2"
    const val GROUP_ONE = "group_1"
    const val GROUP_TWO = "group_2"
    const val GROUP_THREE = "group_3"
    const val BUSINESS_KEY_ONE = "business_key_1"
    const val BUSINESS_KEY_TWO = "business_key_2"
    const val PRIORITY_ONE = 80
    const val PRIORITY_TWO = 90
    const val PRIORITY_THREE = 100

    @Container
    @JvmStatic
    var mongoDBContainer: MongoDBContainer = MongoDBContainer("mongo:4.4.2")

    @DynamicPropertySource
    @JvmStatic
    fun setProperties(registry: DynamicPropertyRegistry) {
      registry.add("spring.data.mongodb.uri") { mongoDBContainer.replicaSetUrl }
    }

  }

  @BeforeEach
  fun beforeEach() {
    taskRepository.deleteAll().block()
  }

  @Autowired
  lateinit var taskRepository: TaskRepository

  @Test
  fun finds_by_everything() {
    val documents = prepareDocuments(task().build())
    val result = taskRepository.findForUser(
      USER_ONE,
      listOf(GROUP_ONE),
      BUSINESS_KEY_ONE,
      listOf(PRIORITY_ONE),
      defaultPageRequest()
    ).collectList().block()!!
    assertThat(result).containsOnly(documents[0])
  }

  @Test
  fun finds_by_nothing() {
    val documents = prepareDocuments(task().build())
    val result = taskRepository.findForUser(USER_ONE, listOf(GROUP_ONE), null, null, defaultPageRequest()).collectList().block()
    assertThat(result).containsOnly(documents[0])
  }

  @Test
  fun finds_by_user() {
    val documents = prepareDocuments(
      task().candidateUsers(setOf(USER_ONE)).candidateGroups(setOf()).build(), task().candidateUsers(setOf(USER_TWO)).candidateGroups(setOf()).build()
    )
    val result = taskRepository.findForUser(USER_ONE, listOf(GROUP_ONE), null, null, defaultPageRequest()).collectList().block()
    assertThat(result).containsOnly(documents[0])
  }

  @Test
  fun finds_by_group() {
    val documents = prepareDocuments(
      task().candidateUsers(setOf()).candidateGroups(setOf(GROUP_ONE)).build(),
      task().candidateUsers(setOf()).candidateGroups(setOf(GROUP_ONE, GROUP_TWO)).build(),
      task().candidateUsers(setOf()).candidateGroups(setOf(GROUP_TWO, GROUP_THREE)).build(),
      task().candidateUsers(setOf()).candidateGroups(setOf(GROUP_THREE)).build()
    )
    val result = taskRepository.findForUser(
      USER_ONE, listOf(GROUP_ONE, GROUP_TWO), null, null, defaultPageRequest()
    ).collectList().block()
    assertThat(result).containsOnly(documents[0], documents[1], documents[2])
  }

  @Test
  fun finds_by_business_key() {
    val documents = prepareDocuments(
      task().businessKey(BUSINESS_KEY_ONE).build(), task().businessKey(BUSINESS_KEY_TWO).build(), task().businessKey(BUSINESS_KEY_ONE).build()
    )
    val result = taskRepository.findForUser(
      USER_ONE, listOf(GROUP_ONE), BUSINESS_KEY_ONE, null, defaultPageRequest()
    ).collectList().block()!!
    assertThat(result).containsOnly(documents[0], documents[2])
  }

  @Test
  fun finds_by_priority() {
    val documents = prepareDocuments(
      task().priority(PRIORITY_ONE).build(),
      task().priority(PRIORITY_TWO).build(),
      task().priority(PRIORITY_THREE).build(),
      task().priority(PRIORITY_ONE).build()
    )
    val result = taskRepository.findForUser(
      USER_ONE, listOf(GROUP_ONE), null, listOf(
        PRIORITY_ONE, PRIORITY_TWO
      ), defaultPageRequest()
    ).collectList().block()!!
    assertThat(result).containsOnly(documents[0], documents[1], documents[3])
  }

  @Test
  fun sorts_by_createTime_and_pages() {
    val documents = prepareDocuments(*LongRange(0, 100).map { task().createTime(Instant.EPOCH.plusSeconds(it)).build() }.toTypedArray())
    assertThat(findPage(0, 15, Sort.Direction.ASC, "createTime")).containsExactlyElementsOf(documents.subList(0, 15))
    assertThat(findPage(1, 15, Sort.Direction.ASC, "createTime")).containsExactlyElementsOf(documents.subList(15, 30))
    assertThat(findPage(1, 15, Sort.Direction.DESC, "createTime")).containsExactlyElementsOf(reverse(documents).subList(15, 30))
    assertThat(findPage(6, 15, Sort.Direction.ASC, "createTime")).containsExactlyElementsOf(documents.subList(90, 101))
    assertThat(findPage(7, 15, Sort.Direction.ASC, "createTime")).isEmpty()
  }

  @Test
  fun finds_deleted_tasks() {
    val documents = prepareDocuments(
      task().deleted().deleteTime(Instant.EPOCH).build(),
      task().deleted().deleteTime(null).build(),
      task().deleted().deleteTime(Instant.EPOCH.minusSeconds(10)).build(),
      task().deleted().deleteTime(Instant.EPOCH.plusSeconds(10)).build()
    )
    val result = taskRepository.findDeletedBefore(Instant.EPOCH).collectList().block()!!
    assertThat(result).hasSize(3).extracting("deleteTime").containsOnly(Instant.EPOCH, null, Instant.EPOCH.minusSeconds(10))
    assertThat(result).hasSize(3).containsOnly(documents[0], documents[1], documents[2])
  }

  private fun <T> reverse(list: List<T?>): List<T?> {
    val reversed = ArrayList(list)
    reversed.reverse()
    return reversed
  }

  private fun findPage(page: Int, size: Int, direction: Sort.Direction, field: String): List<TaskDocument> {
    return taskRepository.findForUser(
      USER_ONE, listOf(GROUP_ONE), null, null, PageRequest.of(page, size, direction, field)
    ).collectList().block()!!
  }

  private fun task() = TaskDocumentBuilder()
    .id(UUID.randomUUID().toString())
    .candidateUsers(setOf(USER_ONE))
    .candidateGroups(setOf(GROUP_ONE))
    .businessKey(BUSINESS_KEY_ONE)
    .priority(PRIORITY_ONE)

  private fun prepareDocuments(vararg documents: TaskDocument): List<TaskDocument> {
    return taskRepository.saveAll(listOf(*documents)).then(Mono.just(listOf(*documents))).block()!!
  }

  private fun defaultPageRequest(): PageRequest {
    return PageRequest.of(0, Int.MAX_VALUE)
  }
}
