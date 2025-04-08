package ychernovskaya.crack.hash.storage

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

enum class Status {
    Created, End, Error
}

data class HashModel(
    @BsonId val id: ObjectId = ObjectId(),
    val requestId: String,
    val hash: String,
    val maxLength: Int,
    val symbols: List<String>,
)

data class ProcessInfo (
    @BsonId val id: ObjectId = ObjectId(),
    val requestId: String,
    val partNumber: Int,
    val result: MutableList<String>?,
    val status: Status,
    val createdAt: Instant? = Instant.now(),
)