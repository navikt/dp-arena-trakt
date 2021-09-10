buildscript { repositories { mavenCentral() } }

plugins {
    id("dagpenger.common")
    id("dagpenger.rapid-and-rivers")
}

dependencies {
    implementation(Database.Flyway)
    implementation(Database.HikariCP)
    implementation(Database.Kotlinquery)
    implementation(Database.Postgres)
}

application {
    mainClass.set("no.nav.dagpenger.arena.trakt.AppKt")
}
