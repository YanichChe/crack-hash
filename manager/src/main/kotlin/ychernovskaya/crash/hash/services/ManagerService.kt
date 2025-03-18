package ychernovskaya.crash.hash.services

import org.slf4j.LoggerFactory
import ychernovskaya.crash.hash.excepton.CallIdAlreadyExistsException
import ychernovskaya.crash.hash.excepton.NotSuchCallIdException
import ychernovskaya.crash.hash.model.HashModel
import ychernovskaya.crash.hash.model.PartInfo
import ychernovskaya.crash.hash.model.Progress
import ychernovskaya.crash.hash.storage.HashStorage
import kotlin.math.pow

interface ManagerService {
    suspend fun addTask(callId: String, hash: String, maxLength: Int): Result<Boolean>
    fun checkResult(callId: String): Result<Progress>
    fun addResult(callId: String, partInfo: PartInfo, encodedData: List<String>): Result<Boolean>
}

private const val PartCount = 1_000_000
private val symbols = ('a'..'z').map { it.toString() } + ('0'..'9').map { it.toString() }

class ManagerServiceImpl(
    private val hashStorage: HashStorage
) : ManagerService {
    private val logger = LoggerFactory.getLogger(ManagerServiceImpl::class.java)

    override suspend fun addTask(callId: String, hash: String, maxLength: Int): Result<Boolean> {
        return if (hashStorage.findByRequestId(callId) === null) {
            hashStorage.create(
                HashModel(
                    requestId = callId,
                    hash = hash,
                    maxLength = maxLength,
                    processInfo = generatePartInfoToResult(maxLength)
                )
            )
            logger.info("Task added: $callId")
            //TODO: add to queue
            logger.info("End of task")
            return Result.success(true)
        } else {
            return Result.failure(CallIdAlreadyExistsException("Call id already exists"))
        }
    }

    override fun checkResult(callId: String): Result<Progress> {
        hashStorage.findByRequestId(callId)
            ?.let { hashModel ->
                val results = hashModel.processInfo
                    .filter { it.value !== null }
                    .map { it.value!!.toList() }
                    .toList()
                    .flatten()

                val total = hashModel.processInfo.keys.size
                val currentCount = hashModel.processInfo.values.count { it !== null }
                return Result.success(Progress(currentCount, total, results))
            }
            ?: return Result.failure(NotSuchCallIdException("Call id $callId not found"))
    }

    override fun addResult(callId: String, partInfo: PartInfo, encodedData: List<String>): Result<Boolean> {
        hashStorage.findByRequestId(callId)
            ?.let { hashModel ->
                hashStorage.updateByRequestId(requestId = callId, partInfo = partInfo, encodedData)
                return Result.success(true)
            }
            ?: return Result.failure(NotSuchCallIdException("Call id $callId not found"))
    }
}

private fun generatePartInfoToResult(maxLength: Int): Map<String, MutableList<String>?> {
    val sequenceSize = calcSize(maxLength)
    val allPartsNumberCount = ((sequenceSize - 1) / PartCount).toInt()

    return buildMap<String, MutableList<String>?> {
        for (i in 0..allPartsNumberCount) {
            put( "${i.toInt()}_${PartCount}", null)
        }
    }
}

private fun calcSize(maxLength: Int, symbolsCount: Double = symbols.size.toDouble()): Long {
    if (maxLength < 1) return 0

    return (1..maxLength).asSequence()
        .map { symbolsCount.pow(it.toDouble()).toLong() }
        .sum()
}
