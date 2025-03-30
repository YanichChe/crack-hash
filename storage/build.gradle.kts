val koin_version: String by project
val kmongo_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
}

group = "ychernovskaya.crash.hash"
version = "0.0.1"

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

dependencies {
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.litote.kmongo:kmongo:$kmongo_version")
    implementation("io.ktor:ktor-server-core")
}
