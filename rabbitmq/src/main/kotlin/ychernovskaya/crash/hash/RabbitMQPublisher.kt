package ychernovskaya.crash.hash

interface RabbitMQPublisher {
    suspend fun publish(message: ByteArray)
}

data class PublishContext(
    val exchange: String,
    val routingKey: String
)

class RabbitMQPublisherImpl(
    private val publishContext: PublishContext, connection: com.rabbitmq.client.Connection) : RabbitMQPublisher {
    var channel = connection.createChannel()

    override suspend fun publish(message: ByteArray) {
        channel.basicPublish(
            publishContext.exchange,
            publishContext.routingKey,
            null,
            message
        )
    }
}