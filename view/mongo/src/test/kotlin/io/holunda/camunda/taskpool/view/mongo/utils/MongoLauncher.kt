package io.holunda.camunda.taskpool.view.mongo.utils

import de.flapdoodle.embed.mongo.Command
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.*
import de.flapdoodle.embed.mongo.distribution.Version
import org.mockito.Mockito.mock
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import javax.net.SocketFactory

/**
 * Mongo Launcher... See https://github.com/AxonFramework/extension-mongo/blob/master/mongo/src/test/java/org/axonframework/extensions/mongo/utils/MongoLauncher.java
 * @author Allard Buijze
 */
object MongoLauncher {

  const val MONGO_DEFAULT_PORT = 27017
  const val LOCALHOST = "127.0.0.1"
  private val counter = AtomicInteger()

  private val isMongoRunning: Boolean
    get() {
      try {
        val mongoSocket = SocketFactory.getDefault().createSocket(LOCALHOST, MONGO_DEFAULT_PORT)

        if (mongoSocket.isConnected) {
          mongoSocket.close()
          return true
        }
      } catch (e: IOException) {
        return false
      }

      return false
    }

  @Throws(IOException::class)
  fun prepareExecutable(): MongodExecutable {
    if (isMongoRunning) {
      return mock(MongodExecutable::class.java)
    }

    val mongodConfig = MongodConfigBuilder()
      .version(Version.Main.PRODUCTION)
      .net(Net(MONGO_DEFAULT_PORT, false))
      .build()

    val command = Command.MongoD
    val runtimeConfig = RuntimeConfigBuilder()
      .defaults(command)
      .artifactStore(ArtifactStoreBuilder()
        .defaults(command)
        .download(DownloadConfigBuilder()
          .defaultsForCommand(command))
        .executableNaming { prefix, postfix -> prefix + "_axontest_" + counter.getAndIncrement() + "_" + postfix })
      .build()

    val runtime = MongodStarter.getInstance(runtimeConfig)

    return runtime.prepare(mongodConfig)
  }
}
