buildscript { repositories { mavenCentral() } }

plugins {
    id("dagpenger.common")
    id("dagpenger.rapid-and-rivers")
}

application {
    mainClass.set("no.nav.dagpenger.arena.trakt.AppKt")
}
