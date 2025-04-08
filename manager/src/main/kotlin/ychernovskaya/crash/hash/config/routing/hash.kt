package ychernovskaya.crash.hash.config.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import ychernovskaya.crash.hash.HashData
import ychernovskaya.crash.hash.model.EncodeInfo
import ychernovskaya.crash.hash.model.HashInfo
import ychernovskaya.crash.hash.model.RequestResponse
import ychernovskaya.crash.hash.model.ResponseStatus
import ychernovskaya.crash.hash.model.StatusResponse
import ychernovskaya.crash.hash.services.ManagerService

private val symbols = ('a'..'z').map { it.toString() } + ('0'..'9').map { it.toString() }

fun Route.hash() {
    val managerService by application.inject<ManagerService>()
    LoggerFactory.getLogger(Route::class.java)

    route("hash-crack") {
        get("status/{requestId}") {
            call.requestId()
                ?.let {
                    val result = managerService.checkResult(it)

                    if (result.isFailure) {
                        call.respond(StatusResponse(status = ResponseStatus.Error, data = null))
                    }

                    val progress = result.getOrNull()!!
                    call.respond(
                        StatusResponse(
                            status = if (progress.currentCount == progress.total && progress.total != 0) ResponseStatus.Ready else ResponseStatus.InProgress,
                            data = progress.currentResult
                        )
                    )
                }
        }

        post("encode") {
            val encodeInfo = call.receive<EncodeInfo>()
            call.respond(managerService.encode(encodeInfo.encodeString))
        }

        post {
            val hashInfo = call.receive<HashInfo>()
            call.callId
                ?.let { callId ->
                    managerService.addTask(
                        callId = callId,
                        hashData = HashData(
                            hash = hashInfo.hash,
                            maxLength = hashInfo.maxLength,
                            symbols = symbols
                        )
                    )
                    call.respond(RequestResponse(callId))
                }
                ?: call.respond(HttpStatusCode.BadRequest)
        }
    }
}

private fun ApplicationCall.requestId(): String? {
    return parameters["requestId"]
}