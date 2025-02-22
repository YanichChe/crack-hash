package ychernovskaya.crash.hash

import ychernovskaya.crash.hash.config.configureFrameworks
import ychernovskaya.crash.hash.config.configureHTTP
import ychernovskaya.crash.hash.config.configureSerialization
import ychernovskaya.crash.hash.config.configureRouting
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureFrameworks()
    configureRouting()
}
