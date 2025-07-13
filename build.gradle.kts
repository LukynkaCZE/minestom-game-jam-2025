plugins {
    kotlin("jvm") version "2.1.21"
    id("com.gradleup.shadow") version "9.0.0-rc1"
    application
}

repositories {
    mavenCentral()
    maven("https://mvn.devos.one/releases")
    maven("https://mvn.devos.one/snapshots")
}

application.mainClass = "cz.lukynka.minestom.gamejam.MainKt"

dependencies {
    testImplementation(kotlin("test"))

    // stom
    implementation("net.kyori:adventure-text-minimessage:4.23.0")
    implementation("net.minestom:minestom:2025.07.11-1.21.7")

    // logging
    implementation("org.slf4j:slf4j-simple:2.0.9")
    api("cz.lukynka:pretty-log:1.5")

    // box
    implementation("cz.lukynka.shulkerbox:minestom:3.4")
    implementation("cz.lukynka.shulkerbox:common:3.4")

    // other
    api("cz.lukynka:kotlin-bindables:2.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}