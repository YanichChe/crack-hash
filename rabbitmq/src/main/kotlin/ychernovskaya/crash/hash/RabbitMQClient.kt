package ychernovskaya.crash.hash

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import io.github.damir.denis.tudor.ktor.server.rabbitmq.RabbitMQ
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.exchangeDeclare
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.queueBind
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.queueDeclare
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.rabbitmq
import io.ktor.server.application.Application
import io.ktor.server.application.install

class RabbitMQConnection() {
    var factory: ConnectionFactory = ConnectionFactory()
        .apply {
            username = "admin"
            password = "password"
            host = "localhost"
            port = 5672
            isAutomaticRecoveryEnabled = true
            setNetworkRecoveryInterval(10000)
        }

    fun getConnection(): Connection {
        return factory.newConnection()
    }
}

fun Application.configureRabbitMQ() {
    install(RabbitMQ) {
        uri = "amqp://admin:password@localhost:5672"
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
