package ychernovskaya.crash.hash.services

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ychernovskaya.crash.hash.model.CrackHashWorkerResponse
import ychernovskaya.crash.hash.pubsub.RabbitMQSubscriber

class ProcessMessage(
    private val rabbitMQSubscriber: RabbitMQSubscriber,
    private val xmlMapper: XmlMapper,
    private val managerService: ManagerService
) {
    init {
        CoroutineScope(Dispatchers.Default).launch {
            rabbitMQSubscriber.subscribe { message ->
                val response = xmlMapper.readValue(message, CrackHashWorkerResponse::class.java)
                println(response)
                managerService.addResult(
                    callId = response.requestId,
                    partNumber = response.partNumber,
                    encodedData = response.answers.words
                )
            }
        }
    }
}