package ychernovskaya.crash.hash.services

import ychernovskaya.crash.hash.pubsub.RabbitMQPublisher

interface SenderTaskServer {
    suspend fun send(message: ByteArray)
}
class SenderTaskServerImpl(private val rabbitMQPublisher: RabbitMQPublisher) : SenderTaskServer {
    override suspend fun send(message: ByteArray) {
        rabbitMQPublisher.publish(message)
    }
}