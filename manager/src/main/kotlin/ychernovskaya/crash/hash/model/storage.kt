package ychernovskaya.crash.hash.model

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class HashModel(
    @BsonId val id: ObjectId = ObjectId(),
    val requestId: String,
    val hash: String,
    val maxLength: Int,
    val processInfo: Map<String, MutableList<String>?>
)

data class PartInfo(
    val partNumber: Int,
    val partCount: Int
)