package ychernovskaya.crash.hash.model

import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id

data class HashModel(
    @BsonId
    val requestId: Id<HashModel>?,
    val hash: String,
    val maxLength: Int,
    val processInfo: Map<PartInfo, MutableList<String>?>
)

data class PartInfo(
    val partNumber: Int,
    val partCount: Int
)