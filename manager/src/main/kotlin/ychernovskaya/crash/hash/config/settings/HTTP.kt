package ychernovskaya.crash.hash.config.settings

import com.ucasoft.ktor.simpleCache.SimpleCache
import com.ucasoft.ktor.simpleMemoryCache.memoryCache
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing
import kotlin.time.Duration.Companion.seconds

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Head)
        anyHost() // @TODO: Don't do this in production if possible.
    }
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }

    install(SimpleCache) {
        memoryCache {
            invalidateAt = 15.seconds
        }
    }

    routing {
        swaggerUI(path = "openapi")
    }
}
