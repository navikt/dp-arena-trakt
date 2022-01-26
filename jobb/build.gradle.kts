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
    implementation(Database.Kotlinquery)
    implementation(Database.HikariCP)
    testImplementation(TestContainers.postgresql)
}
