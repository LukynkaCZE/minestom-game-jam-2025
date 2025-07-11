plugins {
    kotlin("jvm") version "2.1.21"
}

group = "cz.lukynka.minestom.gamejam"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://mvn.devos.one/releases")
    maven("https://mvn.devos.one/snapshots")
}

dependencies {
    testImplementation(kotlin("test"))

    // stom
    implementation("net.kyori:adventure-text-minimessage:4.23.0")
    implementation("net.minestom:minestom-snapshots:4fe2993057")

    // logging
    implementation("org.slf4j:slf4j-nop:2.0.9")
    api("cz.lukynka:pretty-log:1.5")

    // other
    api("cz.lukynka:kotlin-bindables:2.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}