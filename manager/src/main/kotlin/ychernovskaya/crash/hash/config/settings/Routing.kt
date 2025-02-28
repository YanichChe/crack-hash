package ychernovskaya.crash.hash.config.settings

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import ychernovskaya.crash.hash.config.routing.hash
import ychernovskaya.crash.hash.config.routing.internal

fun Application.configureRouting() {
    install(AutoHeadResponse)
    install(StatusPages) {
        exception<JsonProcessingException> { call, cause ->
            call.respondText(
                text = "400: Incorrect JSON format. More: ${cause.message}",
                status = HttpStatusCode.BadRequest
            )
        }

        exception<JsonMappingException> { call, cause ->
            call.respondText(
                text = "400: Error in mapping JSON. More: ${cause.message}",
                status = HttpStatusCode.BadRequest
            )
        }

        exception<RequestValidationException> { call, cause ->
            call.respondText(
                text = "400: Invalid input data: ${cause.reasons.joinToString(", ")}",
                status = HttpStatusCode.BadRequest
            )
        }

        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        configureWebRouting()
    }
}

fun Routing.configureWebRouting() {
    api()
    internalApi()
}

fun Route.internalApi() {
    route("internal/api") {
        internal()
    }
}

fun Route.api() {
    route("api") {
        hash()
    }
}