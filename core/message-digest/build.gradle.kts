val koin_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
}

group = "ychernovskaya.crash.hash.messagedigest"
version = "0.0.1"

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

dependencies {
    implementation("io.insert-koin:koin-ktor:$koin_version")
}
