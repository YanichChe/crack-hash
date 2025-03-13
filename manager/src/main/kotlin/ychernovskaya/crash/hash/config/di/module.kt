package ychernovskaya.crash.hash.config.di

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.litote.kmongo.KMongo
import ychernovskaya.crash.hash.Configuration
import ychernovskaya.crash.hash.api.WorkerApi
import ychernovskaya.crash.hash.api.WorkerApiImpl
import ychernovskaya.crash.hash.services.ManagerService
import ychernovskaya.crash.hash.services.ManagerServiceImpl
import ychernovskaya.crash.hash.storage.HashStorage
import ychernovskaya.crash.hash.storage.HashStorageImpl
import java.io.BufferedReader
import java.io.InputStreamReader

fun appModule() = module {
    services()
    api()
    configuration()
    storage()
}

private fun Module.services() {
    factoryOf(::ManagerServiceImpl) bind ManagerService::class
}

private fun Module.api() {
    factoryOf(::WorkerApiImpl) bind WorkerApi::class
}

private fun Module.storage() {
    //TODO change connection
    val client = KMongo.createClient(ConnectionString("mongodb://127.0.0.1:27017"))
    single { client } bind MongoClient::class

    factoryOf(::HashStorageImpl) bind HashStorage::class
}

private fun Module.configuration() {
    val config = loadConfig()

    val workerUrl = System.getenv("WORKER_URL")
        ?: config.propertyOrNull("worker.url")?.getString()
        ?: "worker-url"

    val configuration = object : Configuration {
        override val workerUrl: String
            get() = workerUrl
    }
    factory<Configuration> { configuration }
}

fun loadConfig(): HoconApplicationConfig {
    val classLoader = Thread.currentThread().contextClassLoader
    val configStream = classLoader.getResourceAsStream("application.conf")
        ?: throw IllegalStateException("Configuration file not found")
    val reader = BufferedReader(InputStreamReader(configStream))
    return HoconApplicationConfig(ConfigFactory.parseReader(reader))
}