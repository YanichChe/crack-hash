package ychernovskaya.crash.hash.storage

import com.mongodb.WriteConcern
import com.mongodb.client.MongoClient
import com.mongodb.client.model.Updates
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import ychernovskaya.crash.hash.model.HashModel
import ychernovskaya.crash.hash.model.PartInfo

interface HashStorage {
    fun findByRequestId(requestId: String): HashModel?
    fun updateByRequestId(requestId: String, partInfo: PartInfo, result: List<String>): Boolean
    fun create(hashModel: HashModel): Boolean
    fun deleteByRequestId(requestId: String): Boolean
}

private const val DatabaseName = "crack_hash"

class HashStorageImpl(
    mongoClient: MongoClient
) : HashStorage {
    private val database = mongoClient.getDatabase(DatabaseName)
    private val hashCollection = database.getCollection<HashModel>()

    override fun findByRequestId(requestId: String): HashModel? {
        return hashCollection.findOne(HashModel::requestId eq requestId)
    }

    override fun updateByRequestId(requestId: String, partInfo: PartInfo, result: List<String>): Boolean {
        val collectionWithWriteConcern = hashCollection.withWriteConcern(WriteConcern.MAJORITY)
        val partKey = partInfo.toKey()
        val result = collectionWithWriteConcern.updateOne(
            HashModel::requestId eq requestId,
            Updates.pushEach("processInfo.$partKey", result)
        )
        return result.wasAcknowledged() && result.modifiedCount > 0
    }

    override fun create(hashModel: HashModel): Boolean {
        val collectionWithWriteConcern = hashCollection.withWriteConcern(WriteConcern.MAJORITY)
        val result = collectionWithWriteConcern.insertOne(hashModel)
        return result.wasAcknowledged()
    }

    override fun deleteByRequestId(requestId: String): Boolean {
        val collectionWithWriteConcern = hashCollection.withWriteConcern(WriteConcern.MAJORITY)
        val result = collectionWithWriteConcern.deleteOne(HashModel::requestId eq requestId)
        return result.wasAcknowledged() && result.deletedCount > 0
    }

    private fun PartInfo.toKey(): String = "${partNumber}_${partCount}"
}