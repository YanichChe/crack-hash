package ychernovskaya.crash.hash.config.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import ychernovskaya.crash.hash.HashData
import ychernovskaya.crash.hash.model.CrackHashManagerRequest
import ychernovskaya.crash.hash.services.WorkerService
import kotlin.getValue

fun Route.encode() {
    val workerService by application.inject<WorkerService>()
    val logger =  LoggerFactory.getLogger(Route::class.java)

    route("hash-crack") {
        post<CrackHashManagerRequest>("task") { crackHashManagerRequest ->
            call.callId
                ?.let {
                    logger.info("Post request: $crackHashManagerRequest")
                    workerService.addEncodeHashTask(
                        hashData = HashData(
                            hash = crackHashManagerRequest.hash,
                            maxLength = crackHashManagerRequest.maxLength,
                            symbols = crackHashManagerRequest.alphabet.symbols,
                        ),
                        requestId = it,
                        partNumber = crackHashManagerRequest.partNumber,
                        partCount = crackHashManagerRequest.partCount
                    )
                    call.respond(HttpStatusCode.OK)
                }
                ?: call.respond(HttpStatusCode.BadRequest)
        }
    }
}