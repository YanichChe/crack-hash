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
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import ychernovskaya.crash.hash.RequestId
import ychernovskaya.crash.hash.services.ManagerService

fun Route.hash() {
    val managerService by application.inject<ManagerService>()
    LoggerFactory.getLogger(Route::class.java)

    route("hash-crack") {
        get("status/{requestId}") {
            call.requestId()
                ?.let {
                    managerService.checkResult(it)
                }
                ?.let { (data, allPartsNumberCount, currentPartsNumberCount) ->
                    call.respond(
                        StatusResponse(
                            status = if (allPartsNumberCount == currentPartsNumberCount.get()) {
                                STATUS.READY
                            } else {
                                STATUS.IN_PROGRESS
                            },
                            data = data
                        )
                    )
                }
                ?: call.respond(
                    StatusResponse(
                        status = STATUS.ERROR,
                        data = null
                    )
                )
        }

        post {
            val hashData = call.receive<HashData>()
            call.callId
                ?.let { callId ->
                    managerService.addTask(
                        callId = callId,
                        hash = hashData.hash,
                        maxLength = hashData.maxLength
                    )
                    call.respond(RequestResponse(callId))
                }
                ?: call.respond(HttpStatusCode.BadRequest)
        }
    }
}

private fun ApplicationCall.requestId(): RequestId? {
    return parameters["requestId"]
}

@Serializable
private data class HashData(
    val hash: String,
    val maxLength: Int
)

@Serializable
private data class RequestResponse(
    val requestId: String
)

private enum class STATUS {
    IN_PROGRESS,
    READY,
    ERROR
}

@Serializable
private data class StatusResponse(
    val status: STATUS,
    val data: List<String>? = null
)