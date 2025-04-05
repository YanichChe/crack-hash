package ychernovskaya.crash.hash.config.di

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ychernovskaya.crack.hach.messagedigest.messageDigestModule
import ychernovskaya.crack.hash.storageModule
import ychernovskaya.crash.hash.pubsub.PublishContext
import ychernovskaya.crash.hash.pubsub.SubscriberContext
import ychernovskaya.crash.hash.rabbitMQModule
import ychernovskaya.crash.hash.services.ManagerService
import ychernovskaya.crash.hash.services.ManagerServiceImpl
import ychernovskaya.crash.hash.services.PeriodicTask
import ychernovskaya.crash.hash.services.ProcessMessage
import ychernovskaya.crash.hash.services.SenderTaskService
import ychernovskaya.crash.hash.services.SenderTaskServiceImpl

fun appModule() = module {
    includes(rabbitMQModule())
    includes(storageModule())
    includes(messageDigestModule())
    services()
    queue()
}

private fun Module.queue() {
    single {
        PublishContext(
            exchange = "crash-hash-exchange",
            routingKey = "add-task-routing-key"
        )
    } bind PublishContext::class

    single {
        SubscriberContext(
            queueName = "encoded-queue",
            consumerTag = "manager",
            routingKey = "encoded-routing-key",
            prefetchCount = 10
        )
    } bind SubscriberContext::class
}
private fun Module.services() {
    factoryOf(::ManagerServiceImpl) bind ManagerService::class
    factoryOf(::SenderTaskServiceImpl) bind SenderTaskService::class
    single { ProcessMessage(get(), get(), get()) }
    single { PeriodicTask(get(), get(), get()) }
    single { XmlMapper() } bind XmlMapper::class
}