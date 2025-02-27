package ychernovskaya.crash.hash.config.routing

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.patch
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import ychernovskaya.crash.hash.model.CrackHashWorkerResponse
import ychernovskaya.crash.hash.services.ManagerService
import kotlin.getValue

fun Route.internal() {
    val managerService by application.inject<ManagerService>()
    val logger = LoggerFactory.getLogger(Route::class.java)
    val xmlMapper = XmlMapper()

    route("hash-crack") {
        patch<String>("request") { crackHashWorkerResponseStr ->
            call.callId
                ?.let {
                    val crackHashWorkerResponse = xmlMapper.readValue(
                        crackHashWorkerResponseStr,
                        CrackHashWorkerResponse::class.java
                    )
                    logger.debug(crackHashWorkerResponse.answers.toString())

                    managerService.addResult(crackHashWorkerResponse.requestId, crackHashWorkerResponse.answers.words)
                    call.respond(HttpStatusCode.OK)
                }
                ?: call.respond(HttpStatusCode.BadRequest)
        }
    }
}