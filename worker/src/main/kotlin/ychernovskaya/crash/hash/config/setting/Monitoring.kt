package ychernovskaya.crash.hash.config.setting

import io.ktor.http.HttpHeaders
import io.ktor.server.application.install
import io.ktor.server.application.Application
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import java.util.UUID

fun Application.configureMonitoring() {
    install(CallId) {
        header(HttpHeaders.XRequestId)
        verify { callId: String ->
            callId.isNotEmpty()
        }
        generate { UUID.randomUUID().toString() }
    }
    install(CallLogging) {
        callIdMdc("call-id")
    }
}
