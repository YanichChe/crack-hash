package ychernovskaya.crash.hash

data class HashData(
    val hash: String,
    val maxLength: Int,
    val symbols: List<String>
)

typealias RequestId = String