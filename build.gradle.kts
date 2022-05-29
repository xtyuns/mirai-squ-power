import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.6.20"
    // FIXME: need 2.12.0+
    // @see: https://github.com/mamoe/mirai/issues/2054
    val miraiConsoleVersion = "2.11.0"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version miraiConsoleVersion
}

group = "com.xtyuns"
version = "2.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

mirai {
    jvmTarget = JavaVersion.VERSION_11
}

dependencies {
    val ktorVersion = "2.0.1"

    implementation("io.ktor:ktor-client-core:${ktorVersion}")
    implementation("io.ktor:ktor-client-java:${ktorVersion}")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    shadowLink("io.ktor:ktor-client-core")
    shadowLink("io.ktor:ktor-client-java")
    shadowLink("io.ktor:ktor-client-content-negotiation")
    shadowLink("io.ktor:ktor-serialization-kotlinx-json")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}