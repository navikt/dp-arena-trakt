buildscript { repositories { mavenCentral() } }

plugins {
    id("dagpenger.common")
    id("dagpenger.rapid-and-rivers")
}

dependencies {
    implementation(Database.Kotlinquery)
    implementation(Konfig.konfig)
    implementation(Database.Flyway)
    implementation(Database.HikariCP)
    implementation(Database.Postgres)
    testImplementation(TestContainers.postgresql)
    testImplementation(Junit5.api)
    testImplementation(Mockk.mockk)
}

application {
    mainClass.set("no.nav.dagpenger.arena.trakt.AppKt")
}
