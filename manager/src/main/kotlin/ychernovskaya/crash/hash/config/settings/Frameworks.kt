package ychernovskaya.crash.hash.config.settings

import io.ktor.server.application.install
import io.ktor.server.application.Application
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import ychernovskaya.crash.hash.config.di.appModule

fun Application.configureFrameworks() {
    install(Koin) {
        modules(appModule())
        slf4jLogger()
    }
}
