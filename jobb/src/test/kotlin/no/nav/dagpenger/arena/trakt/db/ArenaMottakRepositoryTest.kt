package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class ArenaMottakRepositoryTest {
    private val dataRepository by lazy {
        ArenaMottakRepository(3)
    }

    @Test
    fun `Kan lagre JSON blobber som kommer fra Arena`() {
        withMigratedDb {
            dataRepository.leggTil("t", "1", LocalDateTime.now(), LocalDateTime.now(), "{}")
            dataRepository.leggTil("t", "2", LocalDateTime.now(), LocalDateTime.now(), "{}")
            assertEquals(0, antallRader(), "Ingenting lagres før vi når batch størrelse")

            dataRepository.leggTil("t", "2", LocalDateTime.now(), LocalDateTime.now(), "{}")
            assertEquals(2, antallRader(), "Tabell + pos har duplikatkontroll")
        }
    }

    private fun antallRader() =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf("SELECT COUNT(id) FROM arena_data").map { it.int(1) }.asSingle)
        }
}
