package ychernovskaya.crash.hash

import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import ychernovskaya.crash.hash.config.setting.configureFrameworks
import ychernovskaya.crash.hash.config.setting.configureHTTP
import ychernovskaya.crash.hash.config.setting.configureMonitoring
import ychernovskaya.crash.hash.config.setting.configureRouting
import ychernovskaya.crash.hash.config.setting.configureSerialization

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureFrameworks()
    configureRouting()
}
