val ktor_version: String by project
val koin_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val kmongo_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.0.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    id("application")
}

group = "ychernovskaya.crash.hash"
version = "0.0.1"

application {
    mainClass.set("ychernovskaya.crash.hash.ApplicationKt")
}

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

dependencies {
    implementation("io.ktor:ktor-server-auto-head-response")
    implementation("io.ktor:ktor-server-request-validation")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-host-common")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("com.ucasoft.ktor:ktor-simple-cache:0.51.2")
    implementation("com.ucasoft.ktor:ktor-simple-memory-cache:0.51.2")
    implementation("io.ktor:ktor-server-swagger")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-call-id")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-serialization-kotlinx-xml")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    implementation("io.github.flaxoos:ktor-server-rate-limiting:2.1.2")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-xml:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson:2.3.5")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.0")

    implementation(project(":model"))

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    implementation("org.litote.kmongo:kmongo:$kmongo_version")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "ychernovskaya.crash.hash.ApplicationKt"
    }
}