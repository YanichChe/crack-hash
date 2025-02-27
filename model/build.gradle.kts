import org.gradle.kotlin.dsl.sourceSets
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool

plugins {
    kotlin("jvm") version "2.1.10"
    java
}

repositories {
    mavenCentral()
}
val generateJaxbClasses by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Generate Java classes from XSD using JAXB"

    val schemaDirectory = file("${projectDir}/src/main/resources")
    val outputDirectory = file("${buildDir}/generated-sources/JAVAClassesOfXSDFile")

    outputs.dir(outputDirectory)

    doFirst {
        outputDirectory.mkdirs()
    }

    mainClass.set("com.sun.tools.xjc.XJCFacade")
    classpath = files(
        configurations.detachedConfiguration(
            project.dependencies.create("org.glassfish.jaxb:jaxb-xjc:4.0.0"),
            project.dependencies.create("org.glassfish.jaxb:jaxb-runtime:4.0.0")
        )
    )

    args(
        "-d", outputDirectory.absolutePath,
        "-p", "ychernovskaya.crash.hash.model",
        "${schemaDirectory}/CrackHashManagerSchema.xsd",
        "${schemaDirectory}/CrackHashWorkerSchema.xsd"
    )
}

dependencies {
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.0")
}

sourceSets {
    main {
        java {
            srcDirs("${buildDir}/generated-sources/JAVAClassesOfXSDFile")
        }
    }
}

afterEvaluate {
    tasks.withType<KotlinCompileTool> {
        dependsOn(generateJaxbClasses)
    }
}
