val rabbitmq_version: String by project
val ktor_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.0.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    id("application")
}

group = "ychernovskaya.crash.hash"
version = "0.0.1"

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

dependencies {
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")


    implementation("com.rabbitmq:amqp-client:$rabbitmq_version")
    implementation("io.github.damirdenis-tudor:ktor-server-rabbitmq:1.3.3")
    implementation("io.ktor:ktor-server-core")
}

