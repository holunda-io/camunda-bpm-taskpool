package io.holunda.polyflow.view.mongo.service

import io.holunda.polyflow.view.mongo.TaskPoolMongoViewConfiguration
import org.junit.jupiter.api.AfterEach
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.*
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

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
      .withCommand("mongod", "--replSet", "myReplicaSet")

    @DynamicPropertySource
    @JvmStatic
    fun setProperties(registry: DynamicPropertyRegistry) {
      registry.add("spring.data.mongodb.uri") { mongoDBContainer.replicaSetUrl }
    }
  }

  @AfterEach
  fun clearMongo() {
    mongoDBContainer.clear()
  }
}
