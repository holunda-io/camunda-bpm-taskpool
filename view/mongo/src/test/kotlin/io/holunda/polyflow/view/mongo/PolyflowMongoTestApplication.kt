package io.holunda.polyflow.view.mongo

import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(exclude = [EmbeddedMongoAutoConfiguration::class])
class PolyflowMongoTestApplication
