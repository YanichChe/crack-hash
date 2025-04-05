package ychernovskaya.crash.hash

interface RabbitMQConfiguration {
    val login: String
    val password: String
    val host: String
    val port: Int
    val consumerTimeout: Int
}
