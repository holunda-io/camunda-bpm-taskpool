package io.holunda.camunda.taskpool.view.mongo.utils

import com.mongodb.BasicDBList
import com.mongodb.MongoClient
import de.flapdoodle.embed.mongo.Command
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.*
import de.flapdoodle.embed.mongo.distribution.Version
import mu.KLogging
import org.bson.Document
import org.mockito.Mockito.mock
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import javax.net.SocketFactory

/**
 * Mongo Launcher... See https://github.com/AxonFramework/extension-mongo/blob/master/mongo/src/test/java/org/axonframework/extensions/mongo/utils/MongoLauncher.java
 * @author Allard Buijze
 * @author Simon Zambrovski
 * @author Lars Bilger
 */
object MongoLauncher {

  const val MONGO_DEFAULT_PORT = 27017
  const val LOCALHOST = "127.0.0.1"
  private val counter = AtomicInteger()

  private val isMongoRunning: Boolean
    get() {
      return try {
        val mongoSocket = SocketFactory.getDefault().createSocket(LOCALHOST, MONGO_DEFAULT_PORT)
        if (mongoSocket.isConnected) {
          mongoSocket.close()
          true
        } else {
          false
        }
      } catch (e: IOException) {
        false
      }
    }

  fun prepareExecutable(asReplicaSet: Boolean): MongodExecutable {
    if (isMongoRunning) {
      return mock(MongodExecutable::class.java)
    }

    val mongodConfig = MongodConfigBuilder()
      .version(Version.Main.PRODUCTION)
      .replication(if (asReplicaSet) Storage(null, "repembedded", 16) else Storage())
      .net(Net(MONGO_DEFAULT_PORT, false))
      .cmdOptions(MongoCmdOptionsBuilder().useNoJournal(!asReplicaSet).build())
      // Increase timeout as default timeout seems not to be sufficient for shutdown in replicaSet mode
      .stopTimeoutInMillis(10000)
      .build()

    val command = Command.MongoD
    val runtimeConfig = RuntimeConfigBuilder()
      .defaults(command)
      .artifactStore(ExtractedArtifactStoreBuilder()
        .defaults(command)
        .download(DownloadConfigBuilder().defaultsForCommand(command).build())
        .executableNaming { prefix, postfix -> prefix + "_mongo_taskview_" + counter.getAndIncrement() + "_" + postfix })
      .build()

    val runtime = MongodStarter.getInstance(runtimeConfig)

    return runtime.prepare(mongodConfig)
  }

  open class MongoInstance(val asReplicaSet: Boolean, val databaseName: String) {

    companion object : KLogging()

    private var mongod: MongodProcess? = null
    private var mongoExecutable: MongodExecutable? = null

    /**
     * Inits server.
     */
    fun init() {
      if (isMongoRunning) {
        // There was already an existing mongo instance that we are reusing. Clear it in case there is any leftover data from a previous test run
        clear()
      } else {
        this.mongoExecutable = prepareExecutable(asReplicaSet)
        this.mongod = mongoExecutable!!.start()
        if (asReplicaSet) {
          MongoClient(LOCALHOST, MONGO_DEFAULT_PORT).use { mongo ->
            val adminDatabase = mongo.getDatabase("admin")

            val config = Document(mapOf("_id" to "repembedded",
              "members" to BasicDBList().apply {
                add(Document("_id", 0).append("host", "${LOCALHOST}:${MONGO_DEFAULT_PORT}"))
              }))
            logger.info { "MongoDB Replica Config: $config" }
            adminDatabase.runCommand(Document("replSetInitiate", config))

            logger.info { "MongoDB Replica Set Status: ${adminDatabase.runCommand(Document("replSetGetStatus", 1))}" }
          }
        }
      }
    }

    /**
     * Clear client and db.
     */
    fun clear() {
      MongoClient(LOCALHOST, MONGO_DEFAULT_PORT).use { it.dropDatabase(databaseName) }
    }

    /**
     * Stops server.
     */
    fun stop() {
      mongod?.stop()
      mongoExecutable?.stop()
    }
  }
}
