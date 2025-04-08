package ychernovskaya.crash.hash.pubsub

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection

interface RabbitMQPublisher {
    suspend fun publish(message: ByteArray)
}

data class PublishContext(
    val exchange: String,
    val routingKey: String
)

class RabbitMQPublisherImpl(
    private val publishContext: PublishContext, connection: Connection
) : RabbitMQPublisher {
    var channel = connection.createChannel()

    override suspend fun publish(message: ByteArray) {
        var propertyBuilder = AMQP.BasicProperties.Builder()
        propertyBuilder.deliveryMode(2)

        if (channel.isOpen) {
            channel.basicPublish(
                publishContext.exchange,
                publishContext.routingKey,
                propertyBuilder.build(),
                message
            )
        }
    }
}