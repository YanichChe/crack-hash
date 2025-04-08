package ychernovskaya.crash.hash.services

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import ychernovskaya.crack.hash.storage.HashStorageImpl
import ychernovskaya.crack.hash.storage.ProcessInfoStorage
import ychernovskaya.crash.hash.HashData

class PeriodicTask(
    private val hashStorageImpl: HashStorageImpl,
    private val processInfoStorage: ProcessInfoStorage,
    private val managerService: ManagerService
) : AutoCloseable {
    private val logger = LoggerFactory.getLogger(PeriodicTask::class.java)
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    fun start() {
        scope.launch {
            while (isActive) {
                logger.info("Trying restart...")
                processInfoStorage.getNotStartedTasks().forEach { task ->
                    val hashModel = hashStorageImpl.findByRequestId(task.requestId)
                    logger.info("Restarting ${task.requestId} ${task.partNumber}")
                    hashModel?.let {
                        managerService.sendMessage(
                            callId = task.requestId,
                            hashData = HashData(
                                hash = hashModel.hash,
                                maxLength = hashModel.maxLength,
                                symbols = hashModel.symbols
                            ),
                            partNumberValue = task.partNumber
                        )
                    }
                }

                delay(5 * 60 * 1000L)
            }
        }
    }

    override fun close() {
        scope.cancel()
    }
}