package ychernovskaya.crack.hash.storage

import com.mongodb.WriteConcern
import com.mongodb.client.MongoClient
import com.mongodb.client.model.Updates
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

interface ProcessInfoStorage {
    fun findByRequestIdAndPartNumber(requestId: String, partNumber: Int): ProcessInfo?
    fun updateResultByRequestIdAndPartNumber(requestId: String, partNumber: Int, result: List<String>): Boolean
    fun updateStatusByRequestIdAndPartNumber(requestId: String, partNumber: Int, status: Status): Boolean
    fun create(processInfo: ProcessInfo): Boolean
    fun deleteByHashRequestId(requestId: String): Boolean
    fun findAllByHashRequestId(requestId: String): List<ProcessInfo>
}

private const val DatabaseName = "process_info"

class ProcessInfoStorageImpl(mongoClient: MongoClient) : ProcessInfoStorage {
    private val database = mongoClient.getDatabase(DatabaseName)
    private val processInfoCollection = database.getCollection<ProcessInfo>()

    override fun findByRequestIdAndPartNumber(
        requestId: String,
        partNumber: Int
    ): ProcessInfo? {
        return processInfoCollection.findOne(ProcessInfo::requestId eq requestId, ProcessInfo::partNumber eq partNumber)
    }

    override fun updateResultByRequestIdAndPartNumber(
        requestId: String,
        partNumber: Int,
        result: List<String>,
    ): Boolean {
        val collectionWithWriteConcern = processInfoCollection.withWriteConcern(WriteConcern.MAJORITY)

        val filter = and(
            ProcessInfo::requestId eq requestId,
            ProcessInfo::partNumber eq partNumber
        )

        val update = Updates.combine(
            Updates.set("result", result),
            Updates.set("status", Status.End)
        )

        val updateResult = collectionWithWriteConcern.updateOne(filter, update)

        return updateResult.wasAcknowledged() && updateResult.modifiedCount > 0
    }

    override fun updateStatusByRequestIdAndPartNumber(
        requestId: String,
        partNumber: Int,
        status: Status
    ): Boolean {
        val collectionWithWriteConcern = processInfoCollection.withWriteConcern(WriteConcern.MAJORITY)

        val filter = and(
            ProcessInfo::requestId eq requestId,
            ProcessInfo::partNumber eq partNumber
        )

        val updateResult = collectionWithWriteConcern.updateOne(
            filter,
            Updates.set("status", status)
        )

        return updateResult.wasAcknowledged() && updateResult.modifiedCount > 0
    }

    override fun create(processInfo: ProcessInfo): Boolean {
        val collectionWithWriteConcern = processInfoCollection.withWriteConcern(WriteConcern.MAJORITY)
        val result = collectionWithWriteConcern.insertOne(processInfo)
        return result.wasAcknowledged()
    }

    override fun deleteByHashRequestId(requestId: String): Boolean {
        val collectionWithWriteConcern = processInfoCollection.withWriteConcern(WriteConcern.MAJORITY)
        val result = collectionWithWriteConcern.deleteMany(ProcessInfo::requestId eq requestId)
        return result.wasAcknowledged() && result.deletedCount > 0
    }

    override fun findAllByHashRequestId(requestId: String): List<ProcessInfo> {
        return processInfoCollection.find(ProcessInfo::requestId eq requestId).toList()
    }
}