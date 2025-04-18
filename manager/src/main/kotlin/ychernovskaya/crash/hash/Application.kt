package ychernovskaya.crash.hash

import ychernovskaya.crash.hash.config.settings.configureAdministration
import ychernovskaya.crash.hash.config.settings.configureFrameworks
import io.ktor.server.netty.EngineMain
import ychernovskaya.crash.hash.config.settings.configureHTTP
import ychernovskaya.crash.hash.config.settings.configureMonitoring
import ychernovskaya.crash.hash.config.settings.configureSerialization
import ychernovskaya.crash.hash.config.settings.configureRouting
import io.ktor.server.application.Application
import org.koin.ktor.ext.inject
import ychernovskaya.crash.hash.config.settings.configureValidation
import ychernovskaya.crash.hash.config.settings.web

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    val config: RabbitMQConfiguration by inject()
    val exchangeConfig = ExchangeConfiguration(exchangeName = "crash-hash-exchange")
    val addTaskQueueConfig = QueueConfiguration(queueName = "add-task-queue", consumerTimeout = 60_000, routingKey = "add-task-routing-key")
    val encodeQueueConfig = QueueConfiguration(queueName = "encoded-queue", consumerTimeout = 60_000, routingKey = "encoded-routing-key")

    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureValidation()
    configureFrameworks()
    configureAdministration()
    configureRouting()
    configureRabbitMQ(config, mapOf(addTaskQueueConfig to exchangeConfig, encodeQueueConfig to exchangeConfig))
    web()
}
