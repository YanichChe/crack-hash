package ychernovskaya.crash.hash.config.settings

import io.ktor.server.application.Application
import io.ktor.server.http.content.ignoreFiles
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.routing.routing

fun Application.web() {
    routing {
        singlePageApplication {
            useResources = true
            filesPath = "webapp"
            defaultPage = "main.html"
            ignoreFiles { it.endsWith(".txt") }
        }
    }
}