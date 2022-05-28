import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0-RC"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.0-RC"
}

group = "dev.gamer153"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}