package io.holunda.polyflow.view.mongo.utils

import com.mongodb.*
import com.mongodb.client.MongoClients
import de.flapdoodle.embed.mongo.Command
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.*
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.io.Processors
import de.flapdoodle.embed.process.io.Slf4jLevel
import mu.KLogging
import org.awaitility.Awaitility.await
import org.bson.Document
import org.mockito.Mockito.mock
import org.slf4j.Logger
import java.io.IOException
import java.util.concurrent.TimeUnit
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

  fun prepareExecutable(asReplicaSet: Boolean, logger: Logger): MongodExecutable {
    if (isMongoRunning) {
      return mock(MongodExecutable::class.java)
    }

    val mongodConfig = ImmutableMongodConfig
      .builder()
      .version(Version.Main.PRODUCTION)
      .replication(if (asReplicaSet) Storage(null, "repembedded", 16) else Storage())
      .net(Net(MONGO_DEFAULT_PORT, false))
      .cmdOptions(
        MongoCmdOptions
          .builder()
          .useNoJournal(!asReplicaSet)
          .build()
      )
      // Increase timeout as default timeout seems not to be sufficient for shutdown in replicaSet mode
      .stopTimeoutInMillis(11000)
      .build()

    val processOutput = ProcessOutput(
      Processors.logTo(logger, Slf4jLevel.DEBUG),
      Processors.logTo(logger, Slf4jLevel.ERROR),
      Processors.named("[console>]", Processors.logTo(logger, Slf4jLevel.TRACE))
    )

    val command = Command.MongoD

    val downloadConfig = Defaults
      .downloadConfigFor(command)
      .fileNaming { prefix, postfix -> prefix + "_mongo_taskview_" + counter.getAndIncrement() + "_" + postfix }
      .build()

    val artifactStore = Defaults
      .extractedArtifactStoreFor(command)
      .withDownloadConfig(downloadConfig)

    val runtimeConfig = Defaults
      .runtimeConfigFor(command, logger)
      .processOutput(processOutput)
      .artifactStore(artifactStore)
      .build()

    val runtime = MongodStarter.getInstance(runtimeConfig)
    return runtime.prepare(mongodConfig)
  }

  open class MongoInstance(
    private val asReplicaSet: Boolean,
    private val databaseName: String
  ) {

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
        this.mongoExecutable = prepareExecutable(asReplicaSet, logger)
        this.mongod = mongoExecutable!!.start()
        if (asReplicaSet) {

          MongoClients.create(
            MongoClientSettings
              .builder()
              .applyConnectionString(ConnectionString("mongodb://$LOCALHOST:$MONGO_DEFAULT_PORT"))
              .readPreference(ReadPreference.nearest())
              .writeConcern(WriteConcern.W2)
              .build()
          ).use { mongo ->

            val config = Document(mapOf("_id" to "repembedded",
              "members" to BasicDBList().apply {
                add(Document("_id", 0).append("host", "$LOCALHOST:$MONGO_DEFAULT_PORT"))
              }))
            logger.info { "MongoDB Replica Config: $config" }

            val adminDatabase = mongo.getDatabase("admin")
            adminDatabase.runCommand(Document("replSetInitiate", config))

            await().atMost(5, TimeUnit.SECONDS).until {
              val replStatus = adminDatabase.runCommand(Document("replSetGetStatus", 1))
              logger.info { "MongoDB Replica Set Status: $replStatus" }
              "PRIMARY" == replStatus.getList("members", Document::class.java).first().getString("stateStr")
            }
          }
        }
      }
    }

    /**
     * Clear client and db.
     */
    fun clear() {
      MongoClients.create("mongodb://$LOCALHOST:$MONGO_DEFAULT_PORT").use {
        val database = it.getDatabase(databaseName)
        database.drop()
      }
    }

    /**
     * Stops server.
     */
    fun stop() {
      mongod?.stop()
      mongoExecutable?.stop()
      await().atMost(5, TimeUnit.SECONDS).until {
        !isMongoRunning
      }
    }
  }
}
