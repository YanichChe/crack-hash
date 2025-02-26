package ychernovskaya.crash.hash.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import ychernovskaya.crash.hash.Configuration
import ychernovskaya.crash.hash.HashData
import ychernovskaya.crash.hash.api.WorkerApi
import java.util.concurrent.ConcurrentHashMap

data class Result(
    val data: List<String>?,
    val polledNodesCount: Int
)

interface ManagerService {
    suspend fun addTask(callId: String, hash: String, maxLength: Int)
    fun checkResult(callId: String): Result?
}

class ManagerServiceImpl(
    private val workerApi: WorkerApi,
    private val configuration: Configuration
) : ManagerService {
    private val logger = LoggerFactory.getLogger(ManagerServiceImpl::class.java)
    private val results: ConcurrentHashMap<String, Result> = ConcurrentHashMap()
    private val symbols = ('a'..'z').map { it.toString() } + ('0'..'9').map { it.toString() }

    override suspend fun addTask(callId: String, hash: String, maxLength: Int) {
        if (results.containsKey(callId)) {
            logger.info("Existing task $callId")
            return
        }

        results.put(callId, Result(data = emptyList<String>(), 0))
        logger.info("Task added")
        withContext(Dispatchers.IO) {
            try {
                val encodedData = workerApi.sendEncodeTask(
                    workerUrl = configuration.workerUrl,
                    hashData = HashData(hash, maxLength, symbols),
                    partNumber = 0,
                    partCount = 10,
                    requestId = callId
                )
                logger.info("Encoded data $encodedData")
            } catch (e: Exception) {
                logger.error("Error processing task: ${e.message}")
            }
        }
        logger.info("End of task")
    }

    override fun checkResult(callId: String): Result? {
        logger.info("Results data: $results")
        return results.get(callId)
    }
}