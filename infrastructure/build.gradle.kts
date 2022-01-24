plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(Konfig.konfig)
    api(Database.Flyway)
    implementation(Database.Flyway)
    implementation(Database.HikariCP)
    implementation(Database.Postgres)
}
