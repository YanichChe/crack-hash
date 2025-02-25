package ychernovskaya.crash.hash.config.di

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ychernovskaya.crash.hash.Configuration
import ychernovskaya.crash.hash.api.ManagerApi
import ychernovskaya.crash.hash.api.ManagerApiImpl
import ychernovskaya.crash.hash.services.WorkerService
import ychernovskaya.crash.hash.services.WorkerServiceImpl
import java.io.BufferedReader
import java.io.InputStreamReader

fun appModule() = module {
    services()
    api()
    configuration()
}


private fun Module.api() {
    factoryOf(::ManagerApiImpl) bind ManagerApi::class
}

private fun Module.services() {
    factoryOf(::WorkerServiceImpl) bind WorkerService::class
}

private fun Module.configuration() {
    val config = loadConfig()

    val managerUrl =
        config.propertyOrNull("manager.url")
            ?.getString()
            ?: System.getenv("MANAGER_URL")
            ?: "manager-url"

    val configuration = object : Configuration {
        override val managerUrl: String
            get() = managerUrl
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