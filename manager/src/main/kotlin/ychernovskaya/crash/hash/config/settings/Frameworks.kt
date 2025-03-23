package ychernovskaya.crash.hash.config.settings

import io.ktor.server.application.install
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import ychernovskaya.crash.hash.config.di.appModule
import ychernovskaya.crash.hash.services.ProcessMessage
import kotlin.getValue

fun Application.configureFrameworks() {
    install(Koin) {
        modules(appModule())
        slf4jLogger()
    }

    environment.monitor.subscribe(ApplicationStarted) {
        val processMessage: ProcessMessage by inject()
        processMessage.start()
    }
}
