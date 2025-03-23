package ychernovskaya.crash.hash.config.di

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.rabbitmq.client.Connection
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ychernovskaya.crash.hash.RabbitMQConfiguration
import ychernovskaya.crash.hash.RabbitMQConnection
import ychernovskaya.crash.hash.loadRabbitMQConfiguration
import ychernovskaya.crash.hash.pubsub.PublishContext
import ychernovskaya.crash.hash.pubsub.RabbitMQPublisher
import ychernovskaya.crash.hash.pubsub.RabbitMQPublisherImpl
import ychernovskaya.crash.hash.pubsub.RabbitMQSubscriber
import ychernovskaya.crash.hash.pubsub.RabbitMQSubscriberImpl
import ychernovskaya.crash.hash.pubsub.SubscriberContext
import ychernovskaya.crash.hash.services.ProcessMessage
import ychernovskaya.crash.hash.services.WorkerService
import ychernovskaya.crash.hash.services.WorkerServiceImpl

fun appModule() = module {
    services()
    queue()
}

private fun Module.queue() {
    single {
        PublishContext(
            exchange = "crash-hash-exchange",
            routingKey = "encoded-routing-key"
        )
    } bind PublishContext::class

    single {
        SubscriberContext(
            queueName = "add-task-queue",
            consumerTag = "worker",
            routingKey = "add-task-routing-key"
        )
    } bind SubscriberContext::class

    factoryOf(::RabbitMQPublisherImpl) bind RabbitMQPublisher::class
    factoryOf(::RabbitMQSubscriberImpl) bind RabbitMQSubscriber::class

    val config = loadRabbitMQConfiguration()
    single { config } bind RabbitMQConfiguration::class

    factoryOf(::RabbitMQPublisherImpl) bind RabbitMQPublisher::class
    factoryOf(::RabbitMQSubscriberImpl) bind RabbitMQSubscriber::class

    val rabbitMQConnection = RabbitMQConnection(config)
    single { rabbitMQConnection.getConnection() } bind Connection::class
}

private fun Module.services() {
    factoryOf(::WorkerServiceImpl) bind WorkerService::class
    single { XmlMapper() } bind XmlMapper::class
    single { ProcessMessage(get(), get()) }
}