plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        dependsOn("ktlintFormat")
    }
}
