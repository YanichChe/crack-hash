package ychernovskaya.crash.hash.pubsub

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope

data class SubscriberContext(
    val queueName: String,
    val consumerTag: String,
    val routingKey: String
)

interface RabbitMQSubscriber {
    suspend fun subscribe()
}

class RabbitMQSubscriberImpl(
    private val subscriberContext: SubscriberContext,
    connection: Connection
) : RabbitMQSubscriber {
    var channel = connection.createChannel()

    val autoAck = false
    override suspend fun subscribe() {
        channel.basicConsume(subscriberContext.queueName, autoAck, subscriberContext.consumerTag,
            object : DefaultConsumer(channel) {
                override fun handleDelivery(
                    consumerTag: String?,
                    envelope: Envelope,
                    properties: AMQP.BasicProperties?,
                    body: ByteArray
                ) {
                    val routingKey = envelope.routingKey
                    val deliveryTag = envelope.deliveryTag

                    if (subscriberContext.routingKey == routingKey) {
                        channel.basicAck(deliveryTag, false)
                    } else {
                        channel.basicNack(deliveryTag, false, true)
                    }
                }
            }
        )
    }
}