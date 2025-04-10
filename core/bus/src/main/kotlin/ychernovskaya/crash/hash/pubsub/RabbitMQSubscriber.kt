package ychernovskaya.crash.hash.pubsub

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import com.rabbitmq.client.ShutdownSignalException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.lang.Exception

data class SubscriberContext(
    val queueName: String,
    val consumerTag: String,
    val routingKey: String,
    val prefetchCount: Int
)

interface RabbitMQSubscriber {
    suspend fun subscribe(callBack: suspend (message: ByteArray) -> Unit)
}

class RabbitMQSubscriberImpl(
    private val subscriberContext: SubscriberContext,
    private val connection: Connection,
) : RabbitMQSubscriber {
    val logger = LoggerFactory.getLogger(RabbitMQSubscriberImpl::class.java)

    var channel = connection.createChannel().apply {
        basicQos(subscriberContext.prefetchCount)
    }

    val autoAck = false
    override suspend fun subscribe(callBack: suspend (message: ByteArray) -> Unit) {
        channel.addShutdownListener { cause ->
            logger.error("Channel closed unexpectedly. Reason: ${cause.reason}")
            handleChannelShutdown(cause)
        }

        channel.basicConsume(
            subscriberContext.queueName,
            autoAck,
            subscriberContext.consumerTag,
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
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                callBack(body)

                                if (channel.isOpen) {
                                    channel.basicAck(deliveryTag, false)
                                } else {
                                    logger.warn("Channel is closed, cannot send ACK.")
                                }

                            } catch (e: Exception) {
                                logger.error("Error in callback: ${e.message}")

                                if (channel.isOpen) {
                                    channel.basicReject(deliveryTag, false)
                                } else {
                                    logger.warn("Channel is closed, cannot send REJECT.")
                                }
                            }
                        }
                    } else {
                        channel.basicNack(deliveryTag, false, true)
                    }
                }
            }
        )
    }

    private fun handleChannelShutdown(cause: ShutdownSignalException) {
        logger.error("Channel shutdown detected. Initiating recovery...")
        CoroutineScope(Dispatchers.IO).launch {
            recoverChannel()
        }
    }

    private suspend fun recoverChannel() {
        try {
            logger.info("Attempting to reconnect to RabbitMQ...")
            channel = connection.createChannel()
            logger.info("Channel successfully reconnected.")
        } catch (e: Exception) {
            logger.error("Failed to recover channel: ${e.message}")
            delay(5000)
            recoverChannel()
        }
    }
}
