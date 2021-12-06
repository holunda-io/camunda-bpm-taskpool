package io.holunda.polyflow.view.mongo.service

import io.holunda.polyflow.view.mongo.utils.MongoLauncher
import org.junit.After
import org.junit.AfterClass
import org.junit.BeforeClass
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@TestPropertySource(
  properties = [
    "polyflow.view.mongo.changeTrackingMode=EVENT_HANDLER",
    "spring.data.mongodb.database=TaskPoolMongoServiceEventHandlerChangeTrackingITest"
  ]
)
@ActiveProfiles("itest-standalone")
class PolyflowMongoServiceEventHandlerChangeTrackingITest : PolyflowMongoServiceITestBase() {
  companion object {
    private val mongo = MongoLauncher.MongoInstance(false, "TaskPoolMongoServiceEventHandlerChangeTrackingITest")

    @BeforeClass
    @JvmStatic
    fun initMongo() {
      mongo.init()
    }

    @AfterClass
    @JvmStatic
    fun stop() {
      mongo.stop()
    }
  }

  @After
  fun clearMongo() {
    mongo.clear()
  }
}
