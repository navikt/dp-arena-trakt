buildscript { repositories { mavenCentral() } }

plugins {
    application
    id("common")
}

dependencies {
    implementation(project(":modell"))
    implementation(libs.rapids.and.rivers)
    implementation(libs.kotlin.logging)
    implementation(libs.konfig)
    implementation(libs.bundles.postgres)

    testImplementation(libs.bundles.postgres.test)
    testImplementation(libs.mockk)
    testImplementation(libs.rapids.and.rivers.test)
}

application {
    mainClass.set("no.nav.dagpenger.arena.trakt.AppKt")
}
