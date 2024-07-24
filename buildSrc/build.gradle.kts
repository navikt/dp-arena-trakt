plugins {
    `kotlin-dsl`
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("org.jlleitschuh.gradle:ktlint-gradle:12.1.0")
}
