package ychernovskaya.crash.hash.config.di

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.litote.kmongo.KMongo
import ychernovskaya.crash.hash.MongoConfiguration
import ychernovskaya.crash.hash.pubsub.PublishContext
import ychernovskaya.crash.hash.pubsub.SubscriberContext
import ychernovskaya.crash.hash.rabbitMQModule
import ychernovskaya.crash.hash.services.ManagerService
import ychernovskaya.crash.hash.services.ManagerServiceImpl
import ychernovskaya.crash.hash.services.ProcessMessage
import ychernovskaya.crash.hash.services.SenderTaskService
import ychernovskaya.crash.hash.services.SenderTaskServiceImpl
import ychernovskaya.crash.hash.storage.HashStorage
import ychernovskaya.crash.hash.storage.HashStorageImpl
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.text.toInt

fun appModule() = module {
    includes(rabbitMQModule())
    services()
    storage()
    queue()
}

private fun Module.queue() {
    single {
        PublishContext(
            exchange = "crash-hash-exchange",
            routingKey = "add-task-routing-key"
        )
    } bind PublishContext::class

    single {
        SubscriberContext(
            queueName = "encoded-queue",
            consumerTag = "manager",
            routingKey = "encoded-routing-key"
        )
    } bind SubscriberContext::class
}
private fun Module.services() {
    factoryOf(::ManagerServiceImpl) bind ManagerService::class
    factoryOf(::SenderTaskServiceImpl) bind SenderTaskService::class
    single { ProcessMessage(get(), get(), get()) }
    single { XmlMapper() } bind XmlMapper::class
}

private fun Module.storage() {
    val configuration = loadConfiguration()
    val connectionString = ConnectionString(configuration.getUrl())
    val client = KMongo.createClient(connectionString)

    single { client } bind MongoClient::class
    factoryOf(::HashStorageImpl) bind HashStorage::class
}

private fun MongoConfiguration.getUrl(): String {
    return "mongodb://${mongoLogin}:${mongoPassword}@${mongoHost}:${mongoPort}/?authSource=admin"
}

private fun loadConfiguration(): MongoConfiguration {
    val config = loadConfig()

    val mongoLogin =
        config.propertyOrNull("mongo.login")
            ?.getString()
            ?: System.getenv("MONGO_LOGIN")

    val mongoPassword =
        config.propertyOrNull("mongo.password")
            ?.getString()
            ?: System.getenv("MONGO_PASSWORD")

    val mongoPort =
        config.propertyOrNull("mongo.port")
            ?.getString()
            ?: System.getenv("MONGO_PORT")

    val mongoHost =
        config.propertyOrNull("mongo.host")
            ?.getString()
            ?: System.getenv("MONGO_HOST")

    return object : MongoConfiguration {
        override val mongoLogin: String
            get() = mongoLogin
        override val mongoPassword: String
            get() = mongoPassword
        override val mongoPort: Int
            get() = mongoPort.toInt()
        override val mongoHost: String
            get() = mongoHost
    }
}

private fun loadConfig(): HoconApplicationConfig {
    val classLoader = Thread.currentThread().contextClassLoader
    val configStream = classLoader.getResourceAsStream("application.conf")
        ?: throw IllegalStateException("Configuration file not found")
    val reader = BufferedReader(InputStreamReader(configStream))
    return HoconApplicationConfig(ConfigFactory.parseReader(reader))
}