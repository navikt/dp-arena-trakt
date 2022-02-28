plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(Junit5.api)
}

tasks.test {
    useJUnitPlatform()
}
