plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "com.lizz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    implementation(libs.kotlin.stdlib.jdk8)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(23)
}

application {
    mainClass.set("com.lizz.client.ClientMainKt")
}