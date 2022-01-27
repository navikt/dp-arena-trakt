plugins {
    kotlin("jvm")
    id("dagpenger.common")
    id("dagpenger.rapid-and-rivers")
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("no.nav.dagpenger.arena.trakt.AppKt")
}

dependencies {
    implementation(kotlin("test"))
    implementation(project(":infrastructure"))
    implementation("com.google.cloud.sql:postgres-socket-factory:1.4.2")
    implementation(Database.Kotlinquery)
    testImplementation(TestContainers.postgresql)
}
