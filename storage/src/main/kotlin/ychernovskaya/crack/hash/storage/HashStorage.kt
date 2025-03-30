package ychernovskaya.crack.hash.storage

import com.mongodb.WriteConcern
import com.mongodb.client.MongoClient
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

interface HashStorage {
    fun findByRequestId(requestId: String): HashModel?
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
}