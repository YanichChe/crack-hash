package ychernovskaya.crash.hash.pubsub

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import org.slf4j.LoggerFactory

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
    val logger = LoggerFactory.getLogger(RabbitMQPublisherImpl::class.java)

    override suspend fun publish(message: ByteArray) {
        val properties = AMQP.BasicProperties.Builder()
            .deliveryMode(2)
            .build()

        try {
            if (channel.isOpen) {
                channel.basicPublish(
                    publishContext.exchange,
                    publishContext.routingKey,
                    properties,
                    message
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to publish message: ${e.message}")
        }
    }
}