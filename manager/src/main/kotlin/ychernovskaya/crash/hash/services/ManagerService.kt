package ychernovskaya.crash.hash.services

import co.touchlab.stately.concurrency.AtomicInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import ychernovskaya.crash.hash.Configuration
import ychernovskaya.crash.hash.HashData
import ychernovskaya.crash.hash.api.WorkerApi
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

data class Result(
    val data: MutableList<String>,
    val allPartsNumberCount: Int,
    val currentPartsNumberCount: AtomicInt
)

interface ManagerService {
    suspend fun addTask(callId: String, hash: String, maxLength: Int)
    fun checkResult(callId: String): Result?
    fun addResult(callId: String, encodedData: List<String>)
}

private const val PartCount = 1_000_000

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

        val sequenceSize = calcSize(maxLength)
        val allPartsNumberCount = ((sequenceSize - 1) / PartCount).toInt()

        results.put(
            callId,
            Result(data = mutableListOf(), allPartsNumberCount = allPartsNumberCount + 1, currentPartsNumberCount = AtomicInt(0))
        )
        logger.info("Task added: $callId")
        withContext(Dispatchers.IO) {
            for (i in 0..allPartsNumberCount) {
                try {
                    logger.debug("Part Number: ${i.toInt()}")
                    workerApi.sendEncodeTask(
                        workerUrl = configuration.workerUrl,
                        hashData = HashData(hash, maxLength, symbols),
                        partNumber = i.toInt(),
                        partCount = PartCount,
                        requestId = callId
                    )
                    logger.info("Encoded data")
                } catch (e: Exception) {
                    logger.error("Error processing task: ${e.message}")
                }
            }
        }
        logger.info("End of task")
    }

    override fun checkResult(callId: String): Result? {
        logger.info("Results data: $results")
        return results[callId]
    }

    override fun addResult(callId: String, encodedData: List<String>) {
        results[callId]?.apply {
            currentPartsNumberCount.incrementAndGet()
            data.addAll(encodedData)
        }
        logger.debug("Result: {}", results[callId])
    }
}

private fun calcSize(maxLength: Int, symbolsCount: Double = 36.0): Long {
    if (maxLength < 1) return 0

    return (1..maxLength).asSequence()
        .map { symbolsCount.pow(it.toDouble()).toLong() }
        .sum()
}