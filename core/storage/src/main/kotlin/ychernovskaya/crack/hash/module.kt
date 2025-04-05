package ychernovskaya.crack.hash

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.litote.kmongo.KMongo
import ychernovskaya.crack.hash.storage.HashStorage
import ychernovskaya.crack.hash.storage.HashStorageImpl
import ychernovskaya.crack.hash.storage.MongoConfiguration
import ychernovskaya.crack.hash.storage.ProcessInfoStorage
import ychernovskaya.crack.hash.storage.ProcessInfoStorageImpl
import java.io.BufferedReader
import java.io.InputStreamReader

fun storageModule(): Module = module {
    val configuration = loadConfiguration()
    val connectionString = ConnectionString(configuration.getUrl())
    val client = KMongo.createClient(connectionString)

    single { client } bind MongoClient::class
    factoryOf(::HashStorageImpl) bind HashStorage::class
    factoryOf(::ProcessInfoStorageImpl) bind ProcessInfoStorage::class
}

private fun MongoConfiguration.getUrl(): String {
    return "mongodb://${mongoLogin}:${mongoPassword}@${mongoHost}:${mongoPort}/?authSource=admin"
}

private fun loadConfiguration(): MongoConfiguration {
    val config = loadConfig()

    val mongoLogin = System.getenv("MONGO_LOGIN")
        ?: config.propertyOrNull("mongo.login")
            ?.getString()

    val mongoPassword = System.getenv("MONGO_PASSWORD")
        ?: config.propertyOrNull("mongo.password")
            ?.getString()

    val mongoPort = System.getenv("MONGO_PORT")
        ?: config.propertyOrNull("mongo.port")
            ?.getString()

    val mongoHost = System.getenv("MONGO_HOST")
        ?: config.propertyOrNull("mongo.host")
            ?.getString()

    require(mongoLogin != null && mongoPort != null && mongoHost != null && mongoPassword != null)

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