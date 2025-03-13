package ychernovskaya.crash.hash.storage

import com.mongodb.WriteConcern
import com.mongodb.client.MongoClient
import com.mongodb.client.model.Updates
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.id.toId
import ychernovskaya.crash.hash.model.HashModel
import ychernovskaya.crash.hash.model.PartInfo

interface HashStorage {
    fun findById(requestId: String): HashModel?
    fun updateById(requestId: String, partInfo: PartInfo, result: List<String>): Boolean
    fun create(hashModel: HashModel): Boolean
    fun deleteById(requestId: String): Boolean
}

private const val DatabaseName = "hash"

class HashStorageImpl(
    mongoClient: MongoClient
) : HashStorage {
    private val database = mongoClient.getDatabase(DatabaseName)
    private val hashCollection = database.getCollection<HashModel>()

    override fun findById(requestId: String): HashModel? {
        val bsonId: Id<HashModel> = ObjectId(requestId).toId()
        return hashCollection.findOne(HashModel::requestId eq bsonId)
    }

    override fun updateById(requestId: String, partInfo: PartInfo, result: List<String>): Boolean {
        val collectionWithWriteConcern = hashCollection.withWriteConcern(WriteConcern.MAJORITY)
        val bsonId: Id<HashModel> = ObjectId(requestId).toId()
        val result = collectionWithWriteConcern.updateOne(
            HashModel::requestId eq bsonId,
            Updates.pushEach("processInfo.$partInfo", result)
        )
        return result.wasAcknowledged()
    }

    override fun create(hashModel: HashModel): Boolean {
        val collectionWithWriteConcern = hashCollection.withWriteConcern(WriteConcern.MAJORITY)
        val result = collectionWithWriteConcern.insertOne(hashModel)
        return result.wasAcknowledged()
    }

    override fun deleteById(requestId: String): Boolean {
        val collectionWithWriteConcern = hashCollection.withWriteConcern(WriteConcern.MAJORITY)
        val bsonId: Id<HashModel> = ObjectId(requestId).toId()
        val result = collectionWithWriteConcern.deleteOne(HashModel::requestId eq bsonId)
        return result.wasAcknowledged()
    }
}