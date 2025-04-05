package ychernovskaya.crash.hash.model

import kotlinx.serialization.Serializable

@Serializable
data class EncodeInfo(
    val encodeString: String,
)

@Serializable
data class HashInfo(
    val hash: String,
    val maxLength: Int
)

@Serializable
data class RequestResponse(
    val requestId: String
)

enum class ResponseStatus {
    InProgress,
    Ready,
    Error
}

@Serializable
data class StatusResponse(
    val status: ResponseStatus,
    val data: List<String>? = null
)