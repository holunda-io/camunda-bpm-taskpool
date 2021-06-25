package io.holunda.polyflow.view.mongo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration

@SpringBootApplication(exclude = [EmbeddedMongoAutoConfiguration::class])
class PolyflowMongoTestApplication
