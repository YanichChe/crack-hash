package ychernovskaya.crash.hash.config

import io.ktor.server.application.install
import io.ktor.server.application.Application
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
    }
}
