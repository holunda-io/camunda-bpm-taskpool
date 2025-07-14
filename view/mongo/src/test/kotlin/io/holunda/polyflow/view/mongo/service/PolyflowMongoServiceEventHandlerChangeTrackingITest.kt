package io.holunda.polyflow.view.mongo.service

import io.holunda.polyflow.view.mongo.TaskPoolMongoViewConfiguration
import org.axonframework.extensions.mongo.MongoTemplate
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.*
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@TestPropertySource(
  properties = [
    "polyflow.view.mongo.changeTrackingMode=EVENT_HANDLER",
  ]
)
@ActiveProfiles("itest-standalone")
@Testcontainers
@DataMongoTest
@ContextConfiguration(classes = [TaskPoolMongoViewConfiguration::class])
class PolyflowMongoServiceEventHandlerChangeTrackingITest : PolyflowMongoServiceITestBase() {
  companion object {
    @Container
    @JvmStatic
    var mongoDBContainer: MongoDBContainer = MongoDBContainer("mongo:4.4.2")

    @DynamicPropertySource
    @JvmStatic
    fun setProperties(registry: DynamicPropertyRegistry) {
      registry.add("spring.data.mongodb.uri") { mongoDBContainer.replicaSetUrl }
    }
  }

  @Autowired
  var mongoTemplate: MongoTemplate? = null

  @AfterEach
  fun clearMongo() {
    mongoDBContainer.clear()
  }
}

