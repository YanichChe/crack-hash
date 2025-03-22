package ychernovskaya.crash.hash

import ychernovskaya.crash.hash.config.settings.configureAdministration
import ychernovskaya.crash.hash.config.settings.configureFrameworks
import io.ktor.server.netty.EngineMain
import ychernovskaya.crash.hash.config.settings.configureHTTP
import ychernovskaya.crash.hash.config.settings.configureMonitoring
import ychernovskaya.crash.hash.config.settings.configureSerialization
import ychernovskaya.crash.hash.config.settings.configureRouting
import io.ktor.server.application.Application
import org.koin.ktor.ext.inject
import ychernovskaya.crash.hash.config.settings.configureValidation

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    val config: RabbitMQConfiguration by inject()

    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureValidation()
    configureFrameworks()
    configureAdministration()
    configureRouting()
    configureRabbitMQ(config)
}
