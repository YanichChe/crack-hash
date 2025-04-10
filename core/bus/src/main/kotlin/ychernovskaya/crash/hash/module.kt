package ychernovskaya.crash.hash

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.typesafe.config.ConfigFactory
import io.github.damir.denis.tudor.ktor.server.rabbitmq.RabbitMQ
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.exchangeDeclare
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.queueBind
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.queueDeclare
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.rabbitmq
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.HoconApplicationConfig
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ychernovskaya.crash.hash.pubsub.RabbitMQPublisher
import ychernovskaya.crash.hash.pubsub.RabbitMQPublisherImpl
import ychernovskaya.crash.hash.pubsub.RabbitMQSubscriber
import ychernovskaya.crash.hash.pubsub.RabbitMQSubscriberImpl
import java.io.BufferedReader
import java.io.InputStreamReader

fun rabbitMQModule(): Module = module {
    val configuration = loadConfiguration()
    val rabbitMQConnection = RabbitMQConnection(configuration)

    single { rabbitMQConnection.getConnection() } bind Connection::class
    single { configuration } bind RabbitMQConfiguration::class

    singleOf(::RabbitMQPublisherImpl) bind RabbitMQPublisher::class
    singleOf(::RabbitMQSubscriberImpl) bind RabbitMQSubscriber::class
}

internal class RabbitMQConnection(configuration: RabbitMQConfiguration) {
    var factory: ConnectionFactory = ConnectionFactory()
        .apply {
            username = configuration.login
            password = configuration.password
            host = configuration.host
            port = configuration.port
            isAutomaticRecoveryEnabled = true
            requestedHeartbeat = 180
            setNetworkRecoveryInterval(10000)
        }

    fun getConnection(): Connection {
        return factory.newConnection()
    }
}

fun Application.configureRabbitMQ(
    configuration: RabbitMQConfiguration,
    queuesToExchange: Map<QueueConfiguration, ExchangeConfiguration>
) {
    install(RabbitMQ) {
        uri = "amqp://${configuration.login}:${configuration.password}@${configuration.host}:${configuration.port}"
        defaultConnectionName = "default-connection"
        dispatcherThreadPollSize = 4
        tlsEnabled = false
    }

    rabbitmq {
        queuesToExchange.values.forEach {
            exchangeDeclare {
                exchange = it.exchangeName
                type = "direct"
                durable = true
            }

        }

        queuesToExchange.entries.forEach { (queueConfig, exchangeConfig) ->
            queueDeclare {
                queue = queueConfig.queueName
                durable = true
                exclusive = false
                autoDelete = false
                arguments = mapOf(
                    "x-consumer-timeout" to queueConfig.consumerTimeout
                )
            }

            queueBind {
                queue = queueConfig.queueName
                exchange = exchangeConfig.exchangeName
                routingKey = queueConfig.routingKey
            }
        }
    }
}

private fun loadConfiguration(): RabbitMQConfiguration {
    val config = loadConfig()

    val login = System.getenv("RABBITMQ_LOGIN")
        ?: config.propertyOrNull("rabbitmq.login")
            ?.getString()

    val password = System.getenv("RABBITMQ_PASSWORD")
        ?: config.propertyOrNull("rabbitmq.password")
            ?.getString()

    val port = System.getenv("RABBITMQ_PORT")
        ?: config.propertyOrNull("rabbitmq.port")
            ?.getString()

    val host = System.getenv("RABBITMQ_HOST")
        ?: config.propertyOrNull("rabbitmq.host")
            ?.getString()

    val consumerTimeout = System.getenv("RABBITMQ_TIMEOUT")
        ?: config.propertyOrNull("rabbitmq.timeout")
            ?.getString()

    require(login != null && password != null && port != null && host != null && consumerTimeout != null) {}

    return object : RabbitMQConfiguration {
        override val login: String
            get() = login
        override val password: String
            get() = password
        override val host: String
            get() = host
        override val port: Int
            get() = port.toInt()
        override val consumerTimeout: Int
            get() = consumerTimeout.toInt()
    }
}

private fun loadConfig(): HoconApplicationConfig {
    val classLoader = Thread.currentThread().contextClassLoader
    val configStream = classLoader.getResourceAsStream("application.conf")
        ?: throw IllegalStateException("Configuration file not found")
    val reader = BufferedReader(InputStreamReader(configStream))
    return HoconApplicationConfig(ConfigFactory.parseReader(reader))
}