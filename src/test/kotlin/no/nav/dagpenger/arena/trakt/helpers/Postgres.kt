package no.nav.dagpenger.arena.trakt.helpers

import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder.db
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLContainer.POSTGRESQL_PORT
import java.time.LocalDateTime
import java.util.UUID

internal object Postgres {
    private val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:12.8").apply {
            start()
        }
    }

    fun withMigratedDb(block: () -> Unit) {
        withCleanDb {
            PostgresDataSourceBuilder.runMigration()
            block()
        }
    }

    private fun withCleanDb(block: () -> Unit) {
        System.setProperty(db.host.name, instance.host)
        System.setProperty(db.port.name, instance.getMappedPort(POSTGRESQL_PORT).toString())
        System.setProperty(db.database.name, instance.databaseName)
        System.setProperty(db.username.name, instance.username)
        System.setProperty(db.password.name, instance.password)
        PostgresDataSourceBuilder.clean().run {
            block()
        }
    }
}

internal fun DataRepository.lagre(json: String) {
    lagre("t", UUID.randomUUID().toString(), LocalDateTime.now(), LocalDateTime.now(), json)
}
