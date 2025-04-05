package ychernovskaya.crash.hash.config.di

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ychernovskaya.crack.hash.storageModule
import ychernovskaya.crash.hash.pubsub.PublishContext
import ychernovskaya.crash.hash.pubsub.SubscriberContext
import ychernovskaya.crash.hash.rabbitMQModule
import ychernovskaya.crash.hash.services.ProcessMessage
import ychernovskaya.crash.hash.services.SenderEncodedDataService
import ychernovskaya.crash.hash.services.SenderEncodedDataServiceImpl
import ychernovskaya.crash.hash.services.WorkerService
import ychernovskaya.crash.hash.services.WorkerServiceImpl

fun appModule() = module {
    includes(rabbitMQModule())
    includes(storageModule())
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
            routingKey = "add-task-routing-key",
            prefetchCount = 3
        )
    } bind SubscriberContext::class
}

private fun Module.services() {
    factoryOf(::WorkerServiceImpl) bind WorkerService::class
    factoryOf(::SenderEncodedDataServiceImpl) bind SenderEncodedDataService::class
    single { XmlMapper() } bind XmlMapper::class
    single { ProcessMessage(get(), get(), get(), get()) }
}