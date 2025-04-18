package ychernovskaya.crash.hash.services

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import ychernovskaya.crack.hach.messagedigest.md5
import ychernovskaya.crack.hash.storage.HashModel
import ychernovskaya.crack.hash.storage.HashStorage
import ychernovskaya.crack.hash.storage.ProcessInfo
import ychernovskaya.crack.hash.storage.ProcessInfoStorage
import ychernovskaya.crack.hash.storage.Status
import ychernovskaya.crash.hash.HashData
import ychernovskaya.crash.hash.excepton.CallIdAlreadyExistsException
import ychernovskaya.crash.hash.excepton.NotSuchCallIdException
import ychernovskaya.crash.hash.excepton.WorkerException
import ychernovskaya.crash.hash.model.CrackHashManagerRequest
import ychernovskaya.crash.hash.model.Progress
import kotlin.math.pow

interface ManagerService {
    suspend fun addTask(callId: String, hashData: HashData): Result<Unit>
    fun checkResult(callId: String): Result<Progress>
    fun addResult(callId: String, partNumber: Int, encodedData: List<String>): Result<Boolean>
    fun encode(encodeString: String): String
    suspend fun sendMessage(callId: String, partNumberValue: Int, hashData: HashData)
}

private const val PartCount = 10_000_000

class ManagerServiceImpl(
    private val hashStorage: HashStorage,
    private val processInfoStorage: ProcessInfoStorage,
    private val senderTaskService: SenderTaskService,
    private val xmlMapper: XmlMapper,
) : ManagerService {
    private val logger = LoggerFactory.getLogger(ManagerServiceImpl::class.java)

    override suspend fun addTask(callId: String, hashData: HashData): Result<Unit> {
        return if (hashStorage.findByRequestId(callId) === null) {
            val sequenceSize = calcSize(hashData.maxLength, hashData.symbols.size.toDouble())
            val allPartsNumberCount = ((sequenceSize - 1) / PartCount).toInt()

            logger.info("Total count: $allPartsNumberCount")
            logger.info("Sequence size: $sequenceSize")

            hashStorage.create(
                HashModel(
                    requestId = callId,
                    hash = hashData.hash,
                    maxLength = hashData.maxLength,
                    symbols = hashData.symbols,
                )
            )

            logger.info("Task added: $callId")

            withContext(Dispatchers.IO) {
                for (i in 0..allPartsNumberCount) {
                    logger.debug("Part Number: ${i.toInt()}")

                    processInfoStorage.create(
                        ProcessInfo(
                            requestId = callId,
                            partNumber = i,
                            result = null,
                            status = Status.Created
                        )
                    )

                    sendMessage(callId = callId, hashData = hashData, partNumberValue = i)
                }
            }

            logger.info("End of created task")
            return Result.success(Unit)
        } else {
            return Result.failure(CallIdAlreadyExistsException("Call id already exists"))
        }
    }

    override fun checkResult(callId: String): Result<Progress> {
        hashStorage.findByRequestId(callId)
            ?.let {
                var total = 0
                var currentCount = 0
                var currentResult = emptyList<String>().toMutableList()

                processInfoStorage.findAllByHashRequestId(callId).forEach { processInfo ->
                    total = total.inc()
                    when (processInfo.status) {
                        Status.Error -> return Result.failure(WorkerException("Error in the server :("))
                        Status.End -> {
                            currentCount = currentCount.inc()
                            currentResult.addAll(processInfo.result!!)
                        }

                        Status.Created -> Unit
                    }
                }

                if (total == 0) {
                    return Result.failure(WorkerException("Error in the server :("))
                }

                return Result.success(Progress(currentCount, total, currentResult))
            }
            ?: return Result.failure(NotSuchCallIdException("Call id $callId not found"))
    }

    override fun addResult(callId: String, partNumber: Int, encodedData: List<String>): Result<Boolean> {
        hashStorage.findByRequestId(callId)
            ?.let {
                val result = processInfoStorage.updateResultByRequestIdAndPartNumber(callId, partNumber, encodedData)
                return Result.success(result)
            }
            ?: return Result.failure(NotSuchCallIdException("Call id $callId not found"))
    }

    override fun encode(encodeString: String): String {
        return encodeString.md5()
    }

    override suspend fun sendMessage(callId: String, partNumberValue: Int, hashData: HashData) {
        val message = CrackHashManagerRequest().apply {
            requestId = callId
            partNumber = partNumberValue
            partCount = PartCount
            hash = hashData.hash
            maxLength = hashData.maxLength
            alphabet = CrackHashManagerRequest.Alphabet().apply {
                symbols.addAll(hashData.symbols)
            }
        }

        try {
            senderTaskService.send(xmlMapper.writeValueAsBytes(message))
        } catch (_: Exception) {
            logger.error("Error with sending message in the queue")
        }
    }
}

private fun calcSize(maxLength: Int, symbolsCount: Double): Long {
    if (maxLength < 1) return 0

    return (1..maxLength).asSequence()
        .map { symbolsCount.pow(it.toDouble()).toLong() }
        .sum()
}