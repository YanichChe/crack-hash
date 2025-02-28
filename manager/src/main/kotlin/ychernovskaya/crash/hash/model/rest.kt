package ychernovskaya.crash.hash.model

import kotlinx.serialization.Serializable

@Serializable
data class HashInfo(
    val hash: String,
    val maxLength: Int
)

@Serializable
data class RequestResponse(
    val requestId: String
)

enum class STATUS {
    IN_PROGRESS,
    READY,
    ERROR
}

@Serializable
data class StatusResponse(
    val status: STATUS,
    val data: List<String>? = null
)