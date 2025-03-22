package ychernovskaya.crash.hash.services

import ychernovskaya.crash.hash.pubsub.RabbitMQPublisher

interface SenderTaskService {
    suspend fun send(message: ByteArray)
}
class SenderTaskServiceImpl(private val rabbitMQPublisher: RabbitMQPublisher) : SenderTaskService {
    override suspend fun send(message: ByteArray) {
        rabbitMQPublisher.publish(message)
    }
}