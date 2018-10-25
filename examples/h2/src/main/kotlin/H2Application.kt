package io.holunda.camunda.tasklist

import org.h2.tools.Server
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import javax.annotation.PostConstruct

fun main(args: Array<String>) = runApplication<H2Application>(*args).let { Unit }

@SpringBootApplication
open class H2Application(@Value("\${tcp.port:9092}") val tcpPort: String) {

  private val logger = LoggerFactory.getLogger(this.javaClass)

  @PostConstruct
  open fun init(): Server {
    logger.info("Enabling TCP-Port 9092 of H2")
    return Server.createTcpServer("-tcpPort", tcpPort, "-tcpAllowOthers").start()
  }
}
