package ychernovskaya.crash.hash.services

import ychernovskaya.crash.hash.pubsub.RabbitMQPublisher

interface SenderEncodedDataService {
    suspend fun send(message: ByteArray)
}

class SenderEncodedDataServiceImpl(private val rabbitMQPublisher: RabbitMQPublisher) : SenderEncodedDataService {
    override suspend fun send(message: ByteArray) {
        rabbitMQPublisher.publish(message)
    }
}