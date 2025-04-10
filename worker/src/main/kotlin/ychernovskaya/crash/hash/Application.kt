package ychernovskaya.crash.hash

import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import org.koin.ktor.ext.inject
import ychernovskaya.crash.hash.config.setting.configureFrameworks
import ychernovskaya.crash.hash.config.setting.configureMonitoring
import kotlin.getValue

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    val config: RabbitMQConfiguration by inject()
    val exchangeConfig = ExchangeConfiguration(exchangeName = "crash-hash-exchange")
    val addTaskQueueConfig = QueueConfiguration(queueName = "add-task-queue", consumerTimeout = 60_000, routingKey = "add-task-routing-key")
    val encodeQueueConfig = QueueConfiguration(queueName = "encoded-queue", consumerTimeout = 60_000, routingKey = "encoded-routing-key")

    configureMonitoring()
    configureFrameworks()

    configureRabbitMQ(config, mapOf(addTaskQueueConfig to exchangeConfig, encodeQueueConfig to exchangeConfig))
}
