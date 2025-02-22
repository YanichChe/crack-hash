package ychernovskaya.crash.hash

import ychernovskaya.crash.hash.config.configureAdministration
import ychernovskaya.crash.hash.config.configureFrameworks
import io.ktor.server.netty.EngineMain
import ychernovskaya.crash.hash.config.configureHTTP
import ychernovskaya.crash.hash.config.configureMonitoring
import ychernovskaya.crash.hash.config.configureSerialization
import ychernovskaya.crash.hash.config.configureRouting
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureFrameworks()
    configureAdministration()
    configureRouting()
}
