package ychernovskaya.crash.hash.model

data class Progress(
    val currentCount: Int,
    val total: Int,
    val currentResult: List<String>
)