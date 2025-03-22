package ychernovskaya.crash.hash

interface RabbitMQPublisher {
    suspend fun publish(message: String)
}

data class PublishContext(
    val exchange: String,
    val routingKey: String
)

class RabbitMQPublisherImpl(
    private val publishContext: PublishContext,
    private val connection: com.rabbitmq.client.Connection
) : RabbitMQPublisher {
    var channel = connection.createChannel()

    override suspend fun publish(message: String) {
        channel.basicPublish(publishContext.exchange, publishContext.routingKey, null, message.encodeToByteArray());
    }
}