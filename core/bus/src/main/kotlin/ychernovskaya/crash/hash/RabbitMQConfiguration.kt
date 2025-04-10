package ychernovskaya.crash.hash

interface RabbitMQConfiguration {
    val login: String
    val password: String
    val host: String
    val port: Int
    val consumerTimeout: Int
}

data class QueueConfiguration (
    val queueName: String,
    val consumerTimeout: Int,
    val routingKey: String,
)

data class ExchangeConfiguration (
    val exchangeName: String
)