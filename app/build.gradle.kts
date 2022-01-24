buildscript { repositories { mavenCentral() } }

plugins {
    id("dagpenger.common")
    id("dagpenger.rapid-and-rivers")
}

dependencies {
    implementation(project(":infrastructure"))
    implementation(Database.Kotlinquery)
    testImplementation(TestContainers.postgresql)
}

application {
    mainClass.set("no.nav.dagpenger.arena.trakt.AppKt")
}
