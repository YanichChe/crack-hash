package ychernovskaya.crash.hash.config.di

import com.typesafe.config.ConfigFactory
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import io.ktor.server.config.HoconApplicationConfig
import ychernovskaya.crash.hash.Configuration
import ychernovskaya.crash.hash.api.WorkerApi
import ychernovskaya.crash.hash.api.WorkerApiImpl
import ychernovskaya.crash.hash.services.ManagerService
import ychernovskaya.crash.hash.services.ManagerServiceImpl
import java.io.BufferedReader
import java.io.InputStreamReader

fun appModule() = module {
    services()
    api()
    configuration()
}

private fun Module.services() {
    single { ManagerServiceImpl(workerApi = get(), configuration = get()) } bind ManagerService::class
}

private fun Module.api() {
    factoryOf(::WorkerApiImpl) bind WorkerApi::class
}

private fun Module.configuration() {
    val config = loadConfig()

    val nodesCount =
        config.propertyOrNull("nodes.count")
        ?.getString()
        ?: System.getenv("NODES_COUNT")

    val workerUrl =
        config.propertyOrNull("worker.url")
            ?.getString()
            ?: System.getenv("WORKER_URL")
            ?: "worker-url"

    val configuration = object : Configuration {
        override val nodesCount: Int
            get() = nodesCount.toInt()
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