package ychernovskaya.crash.hash.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import org.paukov.combinatorics3.Generator
import org.slf4j.LoggerFactory
import ychernovskaya.crash.hash.HashData
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

interface WorkerService {
    fun addEncodeHashTask(hashData: HashData, requestId: String, partNumber: Int, partCount: Int): Boolean
}

class WorkerServiceImpl() : WorkerService {
    val logger = LoggerFactory.getLogger(WorkerServiceImpl::class.java)
    var resultMap = ConcurrentHashMap<String, MutableList<String>>()

    override fun addEncodeHashTask(
        hashData: HashData,
        requestId: String,
        partNumber: Int,
        partCount: Int
    ): Boolean {
        if (hashData.symbols.isEmpty() || hashData.maxLength <= 0) {
            logger.warn("Invalid input data for encoding task")
            return false
        }

        resultMap.put(requestId, mutableListOf())
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
                        resultMap.get(requestId)!!.add(result)
                    }
                }
            }
            //TODO add sent to queue
            logger.debug("Finished encoding task with result {}", resultMap.get(requestId))

            resultMap.remove(requestId)
        }

        return true
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

    (hashData.maxLength downTo 1)
        .asSequence()
        .flatMap { length ->
            Generator.permutation(hashData.symbols).withRepetitions(length)
        }
        .drop(skip)
        .take(limit)

    for (length in hashData.maxLength downTo 1) {
        Generator.permutation(hashData.symbols)
            .withRepetitions(length)
            .forEach { combination ->
                launch {
                    semaphore.acquire()
                    try {
                        val result = combination.joinToString("")
                        onCombinationChecked(result)
                    } finally {
                        semaphore.release()
                    }
                }
            }
    }
}
