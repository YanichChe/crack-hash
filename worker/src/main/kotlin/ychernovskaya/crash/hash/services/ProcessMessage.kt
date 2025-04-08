package ychernovskaya.crash.hash.services

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import ychernovskaya.crash.hash.HashData
import ychernovskaya.crash.hash.model.CrackHashManagerRequest
import ychernovskaya.crash.hash.pubsub.RabbitMQSubscriber

class ProcessMessage(
    private val rabbitMQSubscriber: RabbitMQSubscriber,
    private val xmlMapper: XmlMapper,
    private val workerService: WorkerService,
) {
    val logger = LoggerFactory.getLogger(ProcessMessage::class.java)

    suspend fun start() {
        rabbitMQSubscriber.subscribe { message ->
            val request = xmlMapper.readValue(message, CrackHashManagerRequest::class.java)
            logger.info("Get new task ${request.requestId}")
            logger.info(request.toString())

            val result = withContext(Dispatchers.Default) {
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

            logger.debug("Task ended ${result.partNumber}")
        }
    }
}