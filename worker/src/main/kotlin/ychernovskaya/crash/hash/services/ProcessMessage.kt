package ychernovskaya.crash.hash.services

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import ychernovskaya.crack.hash.storage.ProcessInfoStorage
import ychernovskaya.crack.hash.storage.Status
import ychernovskaya.crash.hash.HashData
import ychernovskaya.crash.hash.model.CrackHashManagerRequest
import ychernovskaya.crash.hash.pubsub.RabbitMQSubscriber

class ProcessMessage(
    private val rabbitMQSubscriber: RabbitMQSubscriber,
    private val xmlMapper: XmlMapper,
    private val workerService: WorkerService,
    private val processInfoStorage: ProcessInfoStorage

) {
    val logger = LoggerFactory.getLogger(ProcessMessage::class.java)

    fun start() {
        CoroutineScope(Dispatchers.Default).launch {
            rabbitMQSubscriber.subscribe { message ->
                val request = xmlMapper.readValue(message, CrackHashManagerRequest::class.java)
                logger.info("Get new task")

                processInfoStorage.updateStatusByRequestIdAndPartNumber(
                    requestId = request.requestId,
                    partNumber = request.partNumber,
                    status = Status.Pending
                )

                workerService.addEncodeHashTask(
                    hashData = HashData(
                        hash = request.hash,
                        maxLength = request.maxLength,
                        symbols = request.alphabet.symbols
                    ),
                    callId = request.requestId,
                    partNumber = request.partNumber,
                    partCount = request.partCount,
                )
            }
        }
    }
}