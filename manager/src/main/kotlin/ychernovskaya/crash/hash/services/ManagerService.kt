package ychernovskaya.crash.hash.services

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import ychernovskaya.crash.hash.HashData
import ychernovskaya.crash.hash.excepton.CallIdAlreadyExistsException
import ychernovskaya.crash.hash.excepton.NotSuchCallIdException
import ychernovskaya.crash.hash.model.CrackHashManagerRequest
import ychernovskaya.crash.hash.model.HashModel
import ychernovskaya.crash.hash.model.PartInfo
import ychernovskaya.crash.hash.model.Progress
import ychernovskaya.crash.hash.storage.HashStorage
import kotlin.math.pow

interface ManagerService {
    suspend fun addTask(callId: String, hashData: HashData): Result<Boolean>
    fun checkResult(callId: String): Result<Progress>
    fun addResult(callId: String, partInfo: PartInfo, encodedData: List<String>): Result<Boolean>
}

private const val PartCount = 1_000_000

class ManagerServiceImpl(
    private val hashStorage: HashStorage,
    private val senderTaskServer: SenderTaskServer,
    private val xmlMapper: XmlMapper
) : ManagerService {
    private val logger = LoggerFactory.getLogger(ManagerServiceImpl::class.java)

    override suspend fun addTask(callId: String, hashData: HashData): Result<Boolean> {
        return if (hashStorage.findByRequestId(callId) === null) {
            val sequenceSize = calcSize(hashData.maxLength, hashData.symbols.size.toDouble())

            hashStorage.create(
                HashModel(
                    requestId = callId,
                    hash = hashData.hash,
                    maxLength = hashData.maxLength,
                    processInfo = generatePartInfoToResult(hashData.maxLength, sequenceSize)
                )
            )
            logger.info("Task added: $callId")


            val allPartsNumberCount = ((sequenceSize - 1) / PartCount).toInt()

            withContext(Dispatchers.IO) {
                for (i in 0..allPartsNumberCount) {

                    logger.debug("Part Number: ${i.toInt()}")

                    val message = CrackHashManagerRequest().apply {
                        requestId = callId
                        partNumber = i
                        partCount = PartCount
                        hash = hashData.hash
                        maxLength = hashData.maxLength
                    }
                    senderTaskServer.send(xmlMapper.writeValueAsBytes(message))

                    logger.info("Encoded data")
                }
                logger.info("End of task")
            }

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

private fun generatePartInfoToResult(maxLength: Int, symbolsCount: Long): Map<String, MutableList<String>?> {
    val sequenceSize = calcSize(maxLength, symbolsCount.toDouble())
    val allPartsNumberCount = ((sequenceSize - 1) / PartCount).toInt()

    return buildMap<String, MutableList<String>?> {
        for (i in 0..allPartsNumberCount) {
            put("${i.toInt()}_${PartCount}", null)
        }
    }
}

private fun calcSize(maxLength: Int, symbolsCount: Double): Long {
    if (maxLength < 1) return 0

    return (1..maxLength).asSequence()
        .map { symbolsCount.pow(it.toDouble()).toLong() }
        .sum()
}
