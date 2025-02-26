package ychernovskaya.crash.hash.services

import co.touchlab.stately.collections.ConcurrentMutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import org.paukov.combinatorics3.Generator
import org.slf4j.LoggerFactory
import ychernovskaya.crash.hash.Configuration
import ychernovskaya.crash.hash.HashData
import ychernovskaya.crash.hash.api.ManagerApi
import java.security.MessageDigest

interface WorkerService {
    fun addEncodeHashTask(hashData: HashData, requestId: String, partNumber: Int, partCount: Int): Boolean
}

class WorkerServiceImpl(
    private val managerApi: ManagerApi,
    private val configuration: Configuration
) : WorkerService {
    val logger = LoggerFactory.getLogger(WorkerServiceImpl::class.java)
    var resultList = ConcurrentMutableList<String>()

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
                        resultList.add(result)
                    }
                }
            }
            managerApi.sentEncodedData(
                managerUrl = configuration.managerUrl,
                requestId = requestId,
                partNumber = partNumber,
                encodedData = resultList
            )
        }

        return true
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
}
