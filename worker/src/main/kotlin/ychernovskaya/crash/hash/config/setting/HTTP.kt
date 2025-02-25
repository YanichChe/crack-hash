package ychernovskaya.crash.hash.config.setting

import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Post)
        anyHost() // @TODO: Don't do this in production if possible.
    }
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }

    routing {
        swaggerUI(path = "openapi")
    }
}
