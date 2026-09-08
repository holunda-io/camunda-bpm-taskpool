package io.holunda.polyflow.view.mongo.service

import io.holunda.polyflow.view.mongo.TaskPoolMongoViewConfiguration
import org.junit.jupiter.api.AfterEach
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest
import org.springframework.test.context.*
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mongodb.MongoDBContainer

@TestPropertySource(
  properties = [
    "polyflow.view.mongo.changeTrackingMode=CHANGE_STREAM",
  ]
)
@ActiveProfiles("itest-replicated")
@Testcontainers
@DataMongoTest
@ContextConfiguration(classes = [TaskPoolMongoViewConfiguration::class])
class PolyflowMongoServiceChangeStreamChangeTrackingITest : PolyflowMongoServiceITestBase() {
  companion object {
    @Container
    @JvmStatic
    var mongoDBContainer: MongoDBContainer = MongoDBContainer("mongo:4.4.2")
      .withReplicaSet()

    @DynamicPropertySource
    @JvmStatic
    fun setProperties(registry: DynamicPropertyRegistry) {
      registry.add("spring.mongodb.uri") { mongoDBContainer.replicaSetUrl }
    }
  }

  @AfterEach
  fun clearMongo() {
    mongoDBContainer.clear()
  }
}
