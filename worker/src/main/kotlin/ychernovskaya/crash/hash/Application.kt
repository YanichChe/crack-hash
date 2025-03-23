package ychernovskaya.crash.hash

import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import org.koin.ktor.ext.inject
import ychernovskaya.crash.hash.config.setting.configureFrameworks
import ychernovskaya.crash.hash.config.setting.configureMonitoring
import kotlin.getValue

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    val config: RabbitMQConfiguration by inject()

    configureMonitoring()
    configureFrameworks()

    configureRabbitMQ(config)
}
