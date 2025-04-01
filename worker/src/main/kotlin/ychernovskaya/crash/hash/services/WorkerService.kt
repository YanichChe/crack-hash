package ychernovskaya.crash.hash.services

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.ktor.util.collections.ConcurrentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import org.paukov.combinatorics3.Generator
import org.slf4j.LoggerFactory
import ychernovskaya.crash.hash.HashData
import ychernovskaya.crash.hash.exception.InvalidHashDataFormat
import ychernovskaya.crash.hash.model.CrackHashWorkerResponse
import java.security.MessageDigest

interface WorkerService {
    fun addEncodeHashTask(hashData: HashData, callId: String, partNumber: Int, partCount: Int)
}

class WorkerServiceImpl(
    private val senderEncodedDataService: SenderEncodedDataService,
    private val xmlMapper: XmlMapper,
) : WorkerService {
    val logger = LoggerFactory.getLogger(WorkerServiceImpl::class.java)

    override fun addEncodeHashTask(
        hashData: HashData,
        callId: String,
        partNumber: Int,
        partCount: Int
    ) {
        var resultSet = ConcurrentSet<String>()

        if (hashData.symbols.isEmpty() || hashData.maxLength <= 0) {
            logger.warn("Invalid input data for encoding task")

            throw InvalidHashDataFormat("Hash data symbols are empty or max length less than 0")
        }

        CoroutineScope(Dispatchers.Default).launch {
            launchWithSemaphore(
                permits = 10,
                hashData = hashData,
                skip = partNumber * partCount,
                limit = partCount
            ) { result ->
                logger.debug("Checking new combination $result")
                if (result.md5() == hashData.hash) {
                    withContext(Dispatchers.Default) {
                        logger.info("Encoded data found: $result")
                        resultSet.add(result)
                    }
                }
            }

            val encodedDataMessage = CrackHashWorkerResponse()
            encodedDataMessage.requestId = callId
            encodedDataMessage.partNumber = partNumber
            encodedDataMessage.answers = CrackHashWorkerResponse.Answers().apply {
                words.addAll(resultSet)
            }

            senderEncodedDataService.send(xmlMapper.writeValueAsBytes(encodedDataMessage))
            logger.debug("Finished encoding task with result {}", resultSet)
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray())
    return digest.toHexString()
}

private suspend fun CoroutineScope.launchWithSemaphore(
    permits: Int,
    hashData: HashData,
    skip: Int,
    limit: Int,
    onCombinationChecked: suspend (String) -> Unit
) = coroutineScope {
    val semaphore = Semaphore(permits)

    val combinations = (hashData.maxLength downTo 1)
        .asSequence()
        .flatMap { length ->
            Generator.permutation(hashData.symbols).withRepetitions(length)
        }
        .drop(skip)
        .take(limit)

    combinations.forEach { combination ->
        launch {
            semaphore.acquire()
            try {
                onCombinationChecked(combination.joinToString(""))
            } finally {
                semaphore.release()
            }
        }
    }
}