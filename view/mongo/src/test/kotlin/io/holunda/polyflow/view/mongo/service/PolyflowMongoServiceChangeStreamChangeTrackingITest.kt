package io.holunda.polyflow.view.mongo.service

import io.holunda.polyflow.view.mongo.utils.MongoLauncher
import org.junit.After
import org.junit.AfterClass
import org.junit.BeforeClass
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
