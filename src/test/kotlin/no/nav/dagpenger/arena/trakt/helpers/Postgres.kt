package no.nav.dagpenger.arena.trakt.helpers

import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder.dbt
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
        System.setProperty(dbt.host.name, instance.host)
        System.setProperty(dbt.port.name, instance.getMappedPort(POSTGRESQL_PORT).toString())
        System.setProperty(dbt.database.name, instance.databaseName)
        System.setProperty(dbt.username.name, instance.username)
        System.setProperty(dbt.password.name, instance.password)
        PostgresDataSourceBuilder.clean().run {
            block()
        }
    }
}

internal fun DataRepository.lagre(json: String) {
    lagre("t", UUID.randomUUID().toString(), LocalDateTime.now(), LocalDateTime.now(), json)
}
