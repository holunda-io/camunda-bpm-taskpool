package io.holunda.polyflow.view.mongo.service

import io.holunda.polyflow.view.mongo.utils.MongoLauncher
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@TestPropertySource(
  properties = [
    "polyflow.view.mongo.changeTrackingMode=CHANGE_STREAM",
    "spring.data.mongodb.database=TaskPoolMongoServiceChangeStreamChangeTrackingITest"
  ]
)
@ActiveProfiles("itest-replicated")
class PolyflowMongoServiceChangeStreamChangeTrackingITest : PolyflowMongoServiceITestBase() {
  companion object {
    private val mongo = MongoLauncher.MongoInstance(true, "TaskPoolMongoServiceChangeStreamChangeTrackingITest")

    @BeforeAll
    @JvmStatic
    fun initMongo() {
      mongo.init()
    }

    @AfterAll
    @JvmStatic
    fun stop() {
      mongo.stop()
    }
  }

  @AfterEach
  fun clearMongo() {
    mongo.clear()
  }
}
