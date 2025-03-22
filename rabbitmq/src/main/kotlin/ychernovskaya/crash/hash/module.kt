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
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ychernovskaya.crash.hash.pubsub.PublishContext
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

    single {
        PublishContext(
            exchange = "crash-hash-exchange",
            routingKey = "add-task-routing-key"
        )
    } bind PublishContext::class

    factoryOf(::RabbitMQPublisherImpl) bind RabbitMQPublisher::class
    factoryOf(::RabbitMQSubscriberImpl) bind RabbitMQSubscriber::class
}

internal class RabbitMQConnection(configuration: RabbitMQConfiguration) {
    var factory: ConnectionFactory = ConnectionFactory()
        .apply {
            username = configuration.login
            password = configuration.password
            host = configuration.host
            port = configuration.port
            isAutomaticRecoveryEnabled = true
            setNetworkRecoveryInterval(10000)
        }

    fun getConnection(): Connection {
        return factory.newConnection()
    }
}

fun Application.configureRabbitMQ(configuration: RabbitMQConfiguration) {
    install(RabbitMQ) {
        uri = "amqp://${configuration.login}:${configuration.password}@${configuration.host}:${configuration.port}"
        defaultConnectionName = "default-connection"
        dispatcherThreadPollSize = 4
        tlsEnabled = false
    }

    rabbitmq {
        queueBind {
            queue = "dlq"
            exchange = "dlx"
            routingKey = "dlq-dlx"
            exchangeDeclare {
                exchange = "dlx"
                type = "direct"
            }
            queueDeclare {
                queue = "dlq"
                durable = true
            }
        }
    }

    rabbitmq {
        exchangeDeclare {
            exchange = "crash-hash-exchange"
            type = "direct"
            durable = true
        }

        queueDeclare {
            queue = "add-task-queue"
            durable = true
            exclusive = false
            autoDelete = false
            arguments = mapOf(
                "x-dead-letter-exchange" to "dlx",
                "x-dead-letter-routing-key" to "dlq-dlx"
            )
        }

        queueDeclare {
            queue = "encoded-queue"
            durable = true
            exclusive = false
            autoDelete = false
            arguments = mapOf(
                "x-dead-letter-exchange" to "dlx",
                "x-dead-letter-routing-key" to "dlq-dlx"
            )
        }

        queueBind {
            queue = "add-task-queue"
            exchange = "crash-hash-exchange"
            routingKey = "add-task-routing-key"
        }

        queueBind {
            queue = "encoded-queue"
            exchange = "crash-hash-exchange"
            routingKey = "encoded-routing-key"
        }
    }
}

private fun loadConfiguration(): RabbitMQConfiguration {
    val config = loadConfig()

    val login =
        config.propertyOrNull("rabbitmq.login")
            ?.getString()
            ?: System.getenv("RABBITMQ_LOGIN")

    val password =
        config.propertyOrNull("rabbitmq.password")
            ?.getString()
            ?: System.getenv("RABBITMQ_PASSWORD")

    val port =
        config.propertyOrNull("rabbitmq.port")
            ?.getString()
            ?: System.getenv("RABBITMQ_PORT")

    val host =
        config.propertyOrNull("rabbitmq.host")
            ?.getString()
            ?: System.getenv("RABBITMQ_HOST")

    return object : RabbitMQConfiguration {
        override val login: String
            get() = login
        override val password: String
            get() = password
        override val host: String
            get() = host
        override val port: Int
            get() = port.toInt()
    }
}

private fun loadConfig(): HoconApplicationConfig {
    val classLoader = Thread.currentThread().contextClassLoader
    val configStream = classLoader.getResourceAsStream("application.conf")
        ?: throw IllegalStateException("Configuration file not found")
    val reader = BufferedReader(InputStreamReader(configStream))
    return HoconApplicationConfig(ConfigFactory.parseReader(reader))
}