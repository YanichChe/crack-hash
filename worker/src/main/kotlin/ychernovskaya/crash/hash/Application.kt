package ychernovskaya.crash.hash

import ychernovskaya.crash.hash.config.setting.configureFrameworks
import ychernovskaya.crash.hash.config.setting.configureHTTP
import ychernovskaya.crash.hash.config.setting.configureSerialization
import ychernovskaya.crash.hash.config.setting.configureRouting
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
